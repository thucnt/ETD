/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vn.edu.uit.etd;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import static vn.edu.uit.etd.LdaGibbsSampler.inference;
import static vn.edu.uit.etd.LdaGibbsSampler.shadeDouble;

/**
 *
 * @author thucnt
 */
public class CitationLda {
    public static void main(String[] args){
        String citationFile = "/Users/thucnt/git/ETD/data/papers2005.txt"; 
        Corpus corpus = new Corpus(citationFile);
        
        int[][] documents = corpus.getDocumentMatrix();
        // vocabulary
        int V = corpus.getSize();
        int M = documents.length;
        // # topics
        int K = 2;
        // good values alpha = 2, beta = .5
        double alpha = 2;
        double beta = .5;

        System.out.println("Documnet matrix ");
//        for (i  = 0; i < documents.length; i++){
//            for (int j = 0; j < documents[i].length; j++){
//                System.out.print(documents[i][j] + "\t");
//                System.out.println();
//            }
//        }
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
        for (int m = 0; m < theta[0].length; m++)
        {
            System.out.print("   " + m % 10 + "    ");
        }
        System.out.println();
        for (int m = 0; m < theta.length; m++)
        {
            System.out.print(m + "\t");
            for (int k = 0; k < theta[m].length; k++)
            {
                // System.out.print(theta[m][k] + " ");
                System.out.print(shadeDouble(theta[m][k], 1) + " ");
            }
            System.out.println();
        }
        System.out.println();
        System.out.println("Topic--Term Associations, Phi[k][w] (beta=" + beta
                                   + ")");

        System.out.print("k\\w\t");
        for (int w = 0; w < phi[0].length; w++)
        {
            System.out.print("   " + w % 10 + "    ");
        }
        System.out.println();
        for (int k = 0; k < phi.length; k++)
        {
            System.out.print(k + "\t");
            for (int w = 0; w < phi[k].length; w++)
            {
                // System.out.print(phi[k][w] + " ");
                System.out.print(shadeDouble(phi[k][w], 1) + " ");
            }
            System.out.println();
        }
        // Let's inference a new document
        int[] aNewDocument = {2, 2, 4, 2, 4, 2, 2, 2, 2, 4, 2, 2};
        double[] newTheta = inference(alpha, beta, phi, aNewDocument);
        for (int k = 0; k < newTheta.length; k++)
        {
            // System.out.print(theta[m][k] + " ");
            System.out.print(shadeDouble(newTheta[k], 1) + " ");
        }
        System.out.println();
    }
}
