package models;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import utility.FuncUtils;

/**
 * Implementation of the Latent Dirichlet Allocation topic model, using
 * collapsed Gibbs sampling, as described in:
 * 
 * Thomas L. Griffiths and Mark Steyvers. 2004. Finding scientific topics.
 * Proceedings of the National Academy of Sciences of the United States of
 * America, 101(Suppl 1):5228–5235.
 * 
 * @author: Thuc Nguyen
 */

public class CitationLDA
{
	public double alpha; // Hyper-parameter alpha
	public double beta; // Hyper-parameter alpha
	public int numTopics; // Number of topics
	public int numIterations; // Number of Gibbs sampling iterations
	public int topCitations; // Number of most probable words for each topic

	public double alphaSum; // alpha * numTopics
	public double betaSum; // beta * vocabularySize

	public List<List<Integer>> corpus; // Word ID-based corpus
	public List<List<Integer>> topicAssignments; // Topics assignments for words in the corpus
	public int numDocuments; // Number of documents in the corpus
	public int numCitationsInCorpus; // Number of citations in the corpus

	public HashMap<String, Integer> citation2IdVocabulary; // Vocabulary to get ID given a word
	public HashMap<Integer, String> id2CitationVocabulary; // Vocabulary to get word given an ID
	public int vocabularySize; // The number of word types in the corpus

	// numDocuments * numTopics matrix
	// Given a document: number of its words assigned to each topic
	public int[][] docTopicCount;
	// Number of words in every document
	public int[] sumDocTopicCount;
	// numTopics * vocabularySize matrix
	// Given a topic: number of times a word type assigned to the topic
	public int[][] topicCitationCount;
	// Total number of words assigned to a topic
	public int[] sumTopicCitationCount;

	// Double array used to sample a topic
	public double[] multiPros;

	// Path to the directory containing the corpus
	public String folderPath;
	// Path to the topic modeling corpus
	public String corpusPath;

	public String expName = "LDAmodel";
	public String orgExpName = "LDAmodel";
	public String tAssignsFilePath = "";
	public int savestep = 0;

	public CitationLDA(String pathToCorpus, int inNumTopics,
		double inAlpha, double inBeta, int inNumIterations, int inTopWords)
		throws Exception
	{
		this(pathToCorpus, inNumTopics, inAlpha, inBeta, inNumIterations,
			inTopWords, "LDAmodel");
	}

	public CitationLDA(String pathToCorpus, int inNumTopics,
		double inAlpha, double inBeta, int inNumIterations, int inTopWords,
		String inExpName)
		throws Exception
	{
		this(pathToCorpus, inNumTopics, inAlpha, inBeta, inNumIterations,
			inTopWords, inExpName, "", 0);
	}

	public CitationLDA(String pathToCorpus, int inNumTopics,
		double inAlpha, double inBeta, int inNumIterations, int inTopWords,
		String inExpName, String pathToTAfile)
		throws Exception
	{
		this(pathToCorpus, inNumTopics, inAlpha, inBeta, inNumIterations,
			inTopWords, inExpName, pathToTAfile, 0);
	}

	public CitationLDA(String pathToCorpus, int inNumTopics,
		double inAlpha, double inBeta, int inNumIterations, int inTopWords,
		String inExpName, int inSaveStep)
		throws Exception
	{
		this(pathToCorpus, inNumTopics, inAlpha, inBeta, inNumIterations,
			inTopWords, inExpName, "", inSaveStep);
	}

	public CitationLDA(String pathToCorpus, int inNumTopics,
		double inAlpha, double inBeta, int inNumIterations, int inTopWords,
		String inExpName, String pathToTAfile, int inSaveStep)
		throws Exception
	{

		alpha = inAlpha;
		beta = inBeta;
		numTopics = inNumTopics;
		numIterations = inNumIterations;
		topCitations = inTopWords;
		savestep = inSaveStep;
		expName = inExpName;
		orgExpName = expName;
		corpusPath = pathToCorpus;
		folderPath = pathToCorpus.substring(
			0,
			Math.max(pathToCorpus.lastIndexOf("/"),
				pathToCorpus.lastIndexOf("\\")) + 1);

		System.out.println("Reading topic modeling corpus: " + pathToCorpus);

		citation2IdVocabulary = new HashMap<String, Integer>();
		id2CitationVocabulary = new HashMap<Integer, String>();
		corpus = new ArrayList<List<Integer>>();
		numDocuments = 0;
		numCitationsInCorpus = 0;

		BufferedReader br = null;
		try {
			int indexCitation = -1;
			br = new BufferedReader(new FileReader(pathToCorpus));
			for (String doc; (doc = br.readLine()) != null;) {

				if (doc.trim().length() == 0)
					continue;

				String[] citations = doc.trim().split("\\s+");
				List<Integer> document = new ArrayList<Integer>();
                                boolean first = true;
				for (String citation : citations) {
                                        if (first ){
                                            first = false;
                                            continue;
                                        }
					if (citation2IdVocabulary.containsKey(citation)) {
						document.add(citation2IdVocabulary.get(citation));
					}
					else {
						indexCitation += 1;
						citation2IdVocabulary.put(citation, indexCitation);
						id2CitationVocabulary.put(indexCitation, citation);
						document.add(indexCitation);
					}
				}

				numDocuments++;
				numCitationsInCorpus += document.size();
				corpus.add(document);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		vocabularySize = citation2IdVocabulary.size(); // vocabularySize = indexWord
		docTopicCount = new int[numDocuments][numTopics];
		topicCitationCount = new int[numTopics][vocabularySize];
		sumDocTopicCount = new int[numDocuments];
		sumTopicCitationCount = new int[numTopics];

		multiPros = new double[numTopics];
		for (int i = 0; i < numTopics; i++) {
			multiPros[i] = 1.0 / numTopics;
		}

		alphaSum = numTopics * alpha;
		betaSum = vocabularySize * beta;

		System.out.println("Corpus size: " + numDocuments + " docs, "
			+ numCitationsInCorpus + " citations");
		System.out.println("Vocabuary size: " + vocabularySize);
		System.out.println("Number of topics: " + numTopics);
		System.out.println("alpha: " + alpha);
		System.out.println("beta: " + beta);
		System.out.println("Number of sampling iterations: " + numIterations);
		System.out.println("Number of top topical citation: " + topCitations);

		tAssignsFilePath = pathToTAfile;
		if (tAssignsFilePath.length() > 0)
			initialize(tAssignsFilePath);
		else
			initialize();
	}

	/**
	 * Randomly initialize topic assignments
	 */
	public void initialize()
		throws IOException
	{
		System.out.println("Randomly initializing topic assignments ...");

		topicAssignments = new ArrayList<List<Integer>>();

		for (int i = 0; i < numDocuments; i++) {
			List<Integer> topics = new ArrayList<Integer>();
			int docSize = corpus.get(i).size();
			for (int j = 0; j < docSize; j++) {
				int topic = FuncUtils.nextDiscrete(multiPros); // Sample a topic
				// Increase counts
				docTopicCount[i][topic] += 1;
				topicCitationCount[topic][corpus.get(i).get(j)] += 1;
				sumDocTopicCount[i] += 1;
				sumTopicCitationCount[topic] += 1;

				topics.add(topic);
			}
			topicAssignments.add(topics);
		}
	}

	/**
	 * Initialize topic assignments from a given file
	 */
	public void initialize(String pathToTopicAssignmentFile)
	{
		System.out.println("Reading topic-assignment file: "
			+ pathToTopicAssignmentFile);

		topicAssignments = new ArrayList<List<Integer>>();

		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(pathToTopicAssignmentFile));
			int docID = 0;
			int numWords = 0;
			for (String line; (line = br.readLine()) != null;) {
				String[] strTopics = line.trim().split("\\s+");
				List<Integer> topics = new ArrayList<Integer>();
				for (int j = 0; j < strTopics.length; j++) {
					int topic = new Integer(strTopics[j]);
					// Increase counts
					docTopicCount[docID][topic] += 1;
					topicCitationCount[topic][corpus.get(docID).get(j)] += 1;
					sumDocTopicCount[docID] += 1;
					sumTopicCitationCount[topic] += 1;

					topics.add(topic);
					numWords++;
				}
				topicAssignments.add(topics);
				docID++;
			}

			if ((docID != numDocuments) || (numWords != numCitationsInCorpus)) {
				System.out
					.println("The topic modeling corpus and topic assignment file are not consistent!!!");
				throw new Exception();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void inference()
		throws IOException
	{
		writeParameters();
		writeDictionary();

		System.out.println("Running Gibbs sampling inference: ");

		for (int iter = 1; iter <= numIterations; iter++) {

			System.out.println("\tSampling iteration: " + (iter));
			// System.out.println("\t\tPerplexity: " + computePerplexity());

			sampleInSingleIteration();

			if ((savestep > 0) && (iter % savestep == 0)
				&& (iter < numIterations)) {
				System.out.println("\t\tSaving the output from the " + iter
					+ "^{th} sample");
				expName = orgExpName + "-" + iter;
				write();
			}
		}
		expName = orgExpName;

		System.out.println("Writing output from the last sample ...");
		write();

		System.out.println("Sampling completed!");

	}

	public void sampleInSingleIteration()
	{
		for (int dIndex = 0; dIndex < numDocuments; dIndex++) {
			int docSize = corpus.get(dIndex).size();
			for (int wIndex = 0; wIndex < docSize; wIndex++) {
				// Get current word and its topic
				int topic = topicAssignments.get(dIndex).get(wIndex);
				int word = corpus.get(dIndex).get(wIndex);

				// Decrease counts
				docTopicCount[dIndex][topic] -= 1;
				// docTopicSum[dIndex] -= 1;
				topicCitationCount[topic][word] -= 1;
				sumTopicCitationCount[topic] -= 1;

				// Sample a topic
				for (int tIndex = 0; tIndex < numTopics; tIndex++) {
					multiPros[tIndex] = (docTopicCount[dIndex][tIndex] + alpha)
						* ((topicCitationCount[tIndex][word] + beta) / (sumTopicCitationCount[tIndex] + betaSum));
					// multiPros[tIndex] = ((docTopicCount[dIndex][tIndex] +
					// alpha) /
					// (docTopicSum[dIndex] + alphaSum))
					// * ((topicWordCount[tIndex][word] + beta) /
					// (topicWordSum[tIndex] + betaSum));
				}
				topic = FuncUtils.nextDiscrete(multiPros);

				// Increase counts
				docTopicCount[dIndex][topic] += 1;
				// docTopicSum[dIndex] += 1;
				topicCitationCount[topic][word] += 1;
				sumTopicCitationCount[topic] += 1;

				// Update topic assignments
				topicAssignments.get(dIndex).set(wIndex, topic);
			}
		}
	}

	public void writeParameters()
		throws IOException
	{
		BufferedWriter writer = new BufferedWriter(new FileWriter(folderPath
			+ expName + ".paras"));
		writer.write("-model" + "\t" + "LDA");
		writer.write("\n-corpus" + "\t" + corpusPath);
		writer.write("\n-ntopics" + "\t" + numTopics);
		writer.write("\n-alpha" + "\t" + alpha);
		writer.write("\n-beta" + "\t" + beta);
		writer.write("\n-niters" + "\t" + numIterations);
		writer.write("\n-twords" + "\t" + topCitations);
		writer.write("\n-name" + "\t" + expName);
		if (tAssignsFilePath.length() > 0)
			writer.write("\n-initFile" + "\t" + tAssignsFilePath);
		if (savestep > 0)
			writer.write("\n-sstep" + "\t" + savestep);

		writer.close();
	}

	public void writeDictionary()
		throws IOException
	{
		BufferedWriter writer = new BufferedWriter(new FileWriter(folderPath
			+ expName + ".vocabulary"));
		for (int id = 0; id < vocabularySize; id++)
			writer.write(id2CitationVocabulary.get(id) + " " + id + "\n");
		writer.close();
	}

	public void writeIDbasedCorpus()
		throws IOException
	{
		BufferedWriter writer = new BufferedWriter(new FileWriter(folderPath
			+ expName + ".IDcorpus"));
		for (int dIndex = 0; dIndex < numDocuments; dIndex++) {
			int docSize = corpus.get(dIndex).size();
			for (int wIndex = 0; wIndex < docSize; wIndex++) {
				writer.write(corpus.get(dIndex).get(wIndex) + " ");
			}
			writer.write("\n");
		}
		writer.close();
	}

	public void writeTopicAssignments()
		throws IOException
	{
		BufferedWriter writer = new BufferedWriter(new FileWriter(folderPath
			+ expName + ".topicAssignments"));
		for (int dIndex = 0; dIndex < numDocuments; dIndex++) {
			int docSize = corpus.get(dIndex).size();
			for (int wIndex = 0; wIndex < docSize; wIndex++) {
				writer.write(topicAssignments.get(dIndex).get(wIndex) + " ");
			}
			writer.write("\n");
		}
		writer.close();
	}

	public void writeTopTopicalCitation()
		throws IOException
	{
		BufferedWriter writer = new BufferedWriter(new FileWriter(folderPath
			+ expName + ".topWords"));

		for (int tIndex = 0; tIndex < numTopics; tIndex++) {
			writer.write("Topic" + new Integer(tIndex) + ":");

			Map<Integer, Integer> wordCount = new TreeMap<Integer, Integer>();
			for (int wIndex = 0; wIndex < vocabularySize; wIndex++) {
				wordCount.put(wIndex, topicCitationCount[tIndex][wIndex]);
			}
			wordCount = FuncUtils.sortByValueDescending(wordCount);

			Set<Integer> mostLikelyWords = wordCount.keySet();
			int count = 0;
			for (Integer index : mostLikelyWords) {
				if (count < topCitations) {
					double pro = (topicCitationCount[tIndex][index] + beta)
						/ (sumTopicCitationCount[tIndex] + betaSum);
					pro = Math.round(pro * 1000000.0) / 1000000.0;
					writer.write(" " + id2CitationVocabulary.get(index) + "(" + pro
						+ ")");
					count += 1;
				}
				else {
					writer.write("\n\n");
					break;
				}
			}
		}
		writer.close();
	}

	public void writeTopicCitationPros()
		throws IOException
	{
		BufferedWriter writer = new BufferedWriter(new FileWriter(folderPath
			+ expName + ".phi"));
		for (int i = 0; i < numTopics; i++) {
			for (int j = 0; j < vocabularySize; j++) {
				double pro = (topicCitationCount[i][j] + beta)
					/ (sumTopicCitationCount[i] + betaSum);
				writer.write(pro + " ");
			}
			writer.write("\n");
		}
		writer.close();
	}

	public void writeTopicWordCount()
		throws IOException
	{
		BufferedWriter writer = new BufferedWriter(new FileWriter(folderPath
			+ expName + ".WTcount"));
		for (int i = 0; i < numTopics; i++) {
			for (int j = 0; j < vocabularySize; j++) {
				writer.write(topicCitationCount[i][j] + " ");
			}
			writer.write("\n");
		}
		writer.close();

	}

	public void writeDocTopicPros()
		throws IOException
	{
		BufferedWriter writer = new BufferedWriter(new FileWriter(folderPath
			+ expName + ".theta"));
		for (int i = 0; i < numDocuments; i++) {
			for (int j = 0; j < numTopics; j++) {
				double pro = (docTopicCount[i][j] + alpha)
					/ (sumDocTopicCount[i] + alphaSum);
				writer.write(pro + " ");
			}
			writer.write("\n");
		}
		writer.close();
	}

	public void writeDocTopicCount()
		throws IOException
	{
		BufferedWriter writer = new BufferedWriter(new FileWriter(folderPath
			+ expName + ".DTcount"));
		for (int i = 0; i < numDocuments; i++) {
			for (int j = 0; j < numTopics; j++) {
				writer.write(docTopicCount[i][j] + " ");
			}
			writer.write("\n");
		}
		writer.close();
	}

	public void write()
		throws IOException
	{
		writeTopTopicalCitation();
		writeDocTopicPros();
		writeTopicAssignments();
		writeTopicCitationPros();
	}

	public static void main(String args[])
		throws Exception
	{
		CitationLDA lda = new CitationLDA("test/corpus.txt", 2, 0.1,
			0.01, 200, 10, "citationLDA");
		lda.inference();
	}
}
