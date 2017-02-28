/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vn.edu.uit.etd;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static vn.edu.uit.etd.LdaGibbsSampler.inference;
import static vn.edu.uit.etd.LdaGibbsSampler.shadeDouble;
import vn.edu.util.asn.ASNDb;

/**
 *
 * @author thucnt
 */
public class CitationLda {
    public static int[][] randomMatrix(int docNum,int vocabNum){
        int[][] matrix = new int[docNum][vocabNum];
        for (int i = 0; i < matrix.length; i++){
            for (int j = 0; j < matrix[i].length; j++){
                matrix[i][j] = (int)(Math.random()*vocabNum) + 1;
            }
        }
        return matrix;
    }

    public static void main(String[] args) {
        String citationFile = "/Users/thucnt/git/ETD/data/papers2005.txt";
        Corpus corpus = new Corpus(citationFile,1000);

        int[][] documents = corpus.getDocumentMatrix();
//        int[][] documents = {
//                {0,1,2,3},
//                {0,1,4,6},
//                {0,1,8,9},
//                {0,1,10,11},
//                {5,7,12,13},
//                {5,7,16,14},
//                {5,7,14,15},
//                {5,7,13,14},
//                {5,7,9,10}
//        };
        // vocabulary
        int V = corpus.getCorpusSize();
//        int V = 17;
        //int M = documents.length;
        // # topics
        int K = 5;
        // good values alpha = 2, beta = .5
        double alpha = 2;
        double beta = .5;

        System.out.println("Latent Dirichlet Allocation using Gibbs Sampling.");
        LdaGibbsSampler lda = new LdaGibbsSampler(documents, V);
        lda.configure(10000, 2000, 100, 10);
        lda.gibbs(K, alpha, beta);

        double[][] theta = lda.getTheta();
        double[][] phi = lda.getPhi();

        System.out.println();
        System.out.println();
        System.out.println("Document--Topic Associations, Theta[d][k] (alpha="
                + alpha + ")");
        System.out.print("d\\k\t");
        for (int m = 0; m < theta[0].length; m++) {
            System.out.print("         " + m % 10 + "           ");
        }
        System.out.println();
        HashMap<Integer,List<Integer>> topicsList = new HashMap<>();
        for (int i = 0; i < K; i++){
            topicsList.put(i, new ArrayList<>());
        }
        
        for (int m = 0; m < theta.length; m++) {
            System.out.print(m + "\t");
            for (int k = 0; k < theta[m].length; k++) {
                System.out.print(theta[m][k] + "\t");
                if (theta[m][k] >= 0.7)
                    topicsList.get(k).add(m);
                //System.out.print(shadeDouble(theta[m][k], 1) + " ");
            }
            System.out.println();
        }
        System.out.println();
        for (Map.Entry<Integer,List<Integer>> entry : topicsList.entrySet()){
            Integer topic = entry.getKey();
            List<Integer> keys = entry.getValue();
            System.out.println("Papers of topic: " + topic);
            for (Integer i : keys){
                System.out.print(i + ";");
            }
            System.out.println();
        }       
        System.out.println("Topic--Term Associations, Phi[k][w] (beta=" + beta
                + ")");

        System.out.print("k\\w\t");
        for (int w = 0; w < phi[0].length; w++) {
            System.out.print("   " + w % 10 + "    ");
        }
        System.out.println();
        Map<Integer,List<Integer>> topicsKeyPapers = new HashMap<>();
        for (int i = 0; i < K; i++){
            topicsKeyPapers.put(i, new ArrayList<>());
        }
        for (int k = 0; k < phi.length; k++) {
            System.out.print(k + "\t");
            for (int w = 0; w < phi[k].length; w++) {
                //System.out.print(phi[k][w] + " ");
                //System.out.print(shadeDouble(phi[k][w], 1) + " ");
                if (phi[k][w] >= 0.05){
                    topicsKeyPapers.get(k).add(w);
                }
            }
            System.out.println();
        }
        for (Map.Entry<Integer,List<Integer>> entry : topicsKeyPapers.entrySet()){
            Integer topic = entry.getKey();
            List<Integer> keys = entry.getValue();
            System.out.println("Key papers of topic: " + topic);
            for (Integer i : keys){
                System.out.print(i + ";");
            }
            System.out.println();
        }
        // Let's inference a new document
//        int[] aNewDocument = {2, 2, 4, 2, 4, 2, 2, 2, 2, 4, 2, 2};
//        double[] newTheta = inference(alpha, beta, phi, aNewDocument);
//        for (int k = 0; k < newTheta.length; k++) {
//            // System.out.print(theta[m][k] + " ");
//            System.out.print(shadeDouble(newTheta[k], 1) + " ");
//        }
        System.out.println("Finished");
    }
}
