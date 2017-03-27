import java.io.*;
import java.util.*;
import edu.stanford.nlp.ling.*;
import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.trees.*;
import edu.stanford.nlp.trees.TreeCoreAnnotations.*; 
import edu.stanford.nlp.util.*;
import intoxicant.analytics.corenlp.StopwordAnnotator;

public class StanfordCoreNlpExample {
    public static void main(String[] args) throws IOException { 
        PrintWriter xmlOut = new PrintWriter("xmlOutput.xml"); 
        String example = "An example string to parse with corenlp";
        
        Properties props = new Properties(); 
        props.put("annotators", "tokenize, ssplit, pos, lemma, stopword");
        props.setProperty("customAnnotatorClass.stopword", "intoxicant.analytics.corenlp.StopwordAnnotator");
        
        //Create a sorted set of some names
        SortedSet<String> sortedNames = new TreeSet<>();
        SortedSet<String> tokenSet = new TreeSet<>();
        sortedNames.add("Java");
        sortedNames.add("SQL");
        sortedNames.add("HTML");
        sortedNames.add("CSS");

        //Creating dictionary object
        //dictionary can be created using HashTable object
        //as dictionary is an abstract class
        LinkedHashMap<Integer,String> dictionary = new LinkedHashMap();

        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        Annotation document = new Annotation(example);
        pipeline.annotate(document);
        List<CoreLabel> tokens = document.get(CoreAnnotations.TokensAnnotation.class);
        for (CoreLabel token : tokens){
            Pair<Boolean,Boolean> stopword = token.get(StopwordAnnotator.class);
            if (!stopword.first){
                tokenSet.add(token.lemma());
            }
        }
        
        Iterator<String> iter = tokenSet.iterator();
        int id = 0;
        BufferedWriter outWordsFile = new BufferedWriter(new FileWriter("data/art/words.txt"));
        while(iter.hasNext()){
            String word = iter.next();
            outWordsFile.write(word);
            outWordsFile.newLine();
            id++;
            dictionary.put(id,word);
        }
        outWordsFile.close();
        BufferedWriter outVocabKeyFile = new BufferedWriter(new FileWriter("data/art/vocabulary-key.txt"));
        for (Integer i : dictionary.keySet()){
            String line = dictionary.get(i) + " " + i;
            outVocabKeyFile.write(line);
            outVocabKeyFile.newLine();
        }
        outVocabKeyFile.close();
//        props.setProperty("annotators","tokenize, ssplit, lemma"); 
//        Annotation annotation = new Annotation("This is a short sentence. And this is another.");
//        pipeline.annotate(annotation); 
//        pipeline.xmlPrint(annotation, xmlOut);
        // An Annotation is a Map and you can get and use the 
        // various analyses individually. For instance, this
        // gets the parse tree of the 1st sentence in the text.
//        List<CoreMap> sentences = annotation.get( CoreAnnotations.SentencesAnnotation.class);
//        if (sentences != null && sentences.size() > 0) { 
//            CoreMap sentence = sentences.get(0);
//            Tree tree = sentence.get(TreeAnnotation.class); 
//            PrintWriter out = new PrintWriter(System.out); 
//            out.println("The first sentence parsed is:"); 
//            tree.pennPrint(out);
//        } 
    }
}