/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utility;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.Pair;
import intoxicant.analytics.corenlp.StopwordAnnotator;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.spark.ml.feature.RegexTokenizer;
import org.apache.spark.ml.feature.StopWordsRemover;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.Metadata;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;

/**
 *
 * @author thucnt
 */
public class ARTData {
    private String dataFolder;
    private List<Row> papers;
    private RegexTokenizer regexTokenizer;
    private SparkSession spark;
    private StanfordCoreNLP pipeline;
    private LinkedHashMap<String,Integer> dictionary;
    
    public ARTData(String folder){
        dataFolder = folder;
        papers = new ArrayList();
        dictionary = new LinkedHashMap();
        
//        spark = SparkSession
//            .builder().master("local")
//            .appName("JavaTokenizerExample")
//            .getOrCreate();
//        regexTokenizer = new RegexTokenizer()
//            .setInputCol("sentence")
//            .setOutputCol("words")
//            .setPattern("\\W");  // alternatively .setPattern("\\w+").setGaps(false);
        //configuration for createing coreNLP pipe line 
        Properties props = new Properties(); 
        props.put("annotators", "tokenize, ssplit, pos, lemma, stopword");
        props.setProperty("customAnnotatorClass.stopword", "intoxicant.analytics.corenlp.StopwordAnnotator");
        pipeline = new StanfordCoreNLP(props);
    }
    private void writeDocumentWordFile(BufferedWriter output, String idPaper, String content){
        String line = null;
        //Create a sorted set of some names
        LinkedHashMap<String,Integer> tokenFrequency = new LinkedHashMap();
        Annotation document = new Annotation(content);
        pipeline.annotate(document);
        List<CoreLabel> tokens = document.get(CoreAnnotations.TokensAnnotation.class);
        for (CoreLabel token : tokens){
            Pair<Boolean,Boolean> stopword = token.get(StopwordAnnotator.class);
            if (!stopword.first){//if is not stop word
                String word = token.lemma();
                if (tokenFrequency.containsKey(word)){
                    Integer val = tokenFrequency.get(word);
                    tokenFrequency.put(word, val + 1);
                }
                else{
                    tokenFrequency.put(word, 1);
                }
            }
        }
        for (String word : tokenFrequency.keySet()){
            line = idPaper + " " + dictionary.get(word) + " " + tokenFrequency.get(word);
            try {
                output.write(line);
                output.newLine();
            } catch (IOException ex) {
                Logger.getLogger(ARTData.class.getName()).log(Level.SEVERE, null, ex);
            }
        }    
    }
    public void prepareDataFile(String paperFile){
        try {
            BufferedReader inputPapers = new BufferedReader(new InputStreamReader(new FileInputStream(dataFolder + "/" + paperFile)));
            BufferedWriter outputDocWordFequency = new BufferedWriter(new FileWriter(dataFolder + "/art/art_words.txt"));
            BufferedWriter outputWordsFile = new BufferedWriter(new FileWriter(dataFolder + "/art/words.txt"));
            BufferedWriter outputVocabularyKeyFile = new BufferedWriter(new FileWriter(dataFolder + "/art/vocabulary-key.txt"));
            String line = null;
            while ((line = inputPapers.readLine()) != null){
                int idx = line.indexOf(" ");
                String idPaper = line.substring(0,idx);
                String content = line.substring(idx + 1);
                if ((idPaper != null) && (content != null)){
                    updateDictionary(content);
                    writeDocumentWordFile(outputDocWordFequency,idPaper,content);
                } 
            }
            outputDocWordFequency.close();
            inputPapers.close();
            //write words file and vocabulary-key file
            for (String word : dictionary.keySet()){
                outputWordsFile.write(word);
                outputWordsFile.newLine();
                outputVocabularyKeyFile.write(word + " " + dictionary.get(word));
                outputVocabularyKeyFile.newLine();
            }
            outputWordsFile.close();
            outputVocabularyKeyFile.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ARTData.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ARTData.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public void prepareVocabulary(){
        TreeSet<String> vocab = new TreeSet();
        try {
            Scanner input = new Scanner(new FileInputStream(dataFolder + "/papers.txt")); 
            PrintWriter output = new PrintWriter(new FileWriter(dataFolder + "/vocabulary.txt"));
            String line = null;
            long count = 0;
            while ((line = input.nextLine())!= null){
                Row row = RowFactory.create(count,line.substring(line.indexOf(" ")));
                count++;
                papers.add(row);
                StructType schema = new StructType(new StructField[]{
                    new StructField("id", DataTypes.LongType, false, Metadata.empty()),
                    new StructField("sentence", DataTypes.StringType, false, Metadata.empty())
                  });
                Dataset<Row> sentenceDataFrame = spark.createDataFrame(papers, schema);
                Dataset<Row> regexTokenized = regexTokenizer.transform(sentenceDataFrame);
                List<Row> list = regexTokenized.collectAsList();
                schema = new StructType(new StructField[]{
                    new StructField(
                      "raw", DataTypes.createArrayType(DataTypes.StringType), false, Metadata.empty())
                  });
                Dataset<Row> dataset = spark.createDataFrame(list, schema);
                StopWordsRemover remover = new StopWordsRemover()
                    .setInputCol("raw")
                    .setOutputCol("filtered");
                remover.transform(dataset).show(false);
//                regexTokenized.select("sentence", "words")
//                    .withColumn("tokens", callUDF("countTokens", col("words")))
//                    .show(false);
                String content = line.substring(line.indexOf(' ') + 1);
                String[] words = content.split(" ");
                
                for (String word : words){
                    vocab.add(word.toLowerCase());
                }
                if (count == 10){
                    Iterator<String> iter = vocab.iterator();
                    while (iter.hasNext()){
                        output.println(iter.next());
                    }
                    output.flush();
                    output.close();
                    break;
                }
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ARTData.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ARTData.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void updateDictionary(String content){
        Annotation document = new Annotation(content);
        pipeline.annotate(document);
        List<CoreLabel> tokens = document.get(CoreAnnotations.TokensAnnotation.class);
        SortedSet<String> tokenSet = new TreeSet<>();
        for (CoreLabel token : tokens){
            Pair<Boolean,Boolean> stopword = token.get(StopwordAnnotator.class);
            if (!stopword.first){
                tokenSet.add(token.lemma());
            }
        }
        Iterator<String> iter = tokenSet.iterator();
        while(iter.hasNext()){
            String word = iter.next();
            if (!dictionary.containsKey(word)){
                dictionary.put(word, dictionary.size() + 1);
            }
        }
    }
    public static void main(String[] args){
        ARTData artData = new ARTData("data");
        //artData.prepareVocabulary();
        artData.prepareDataFile("papers.txt");
    }
}
