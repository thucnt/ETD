/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package models;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author thucnt
 */
public class ART extends GibbsSamplingLDA{
    private HashMap<Integer,List<Double>> authorsDistribution;
    private HashMap<Integer,List<Double>> recipientsDistribution;
    public static void main(String[] args) throws Exception{
        ART lda = new ART("test/corpus2.txt", 2, 0.1,
			0.01, 200, 20, "testLDA");
        lda.inference();
        lda.generateA_RMatix();
        System.out.println();
    }

    public ART(String pathToCorpus, int inNumTopics, double inAlpha, double inBeta, int inNumIterations, int inTopWords, String inExpName) throws Exception {
        super(pathToCorpus, inNumTopics, inAlpha, inBeta, inNumIterations, inTopWords, inExpName);
        authorsDistribution = new HashMap();
        recipientsDistribution = new HashMap();
    }
    public void generateA_RMatix(){
        //create distribution matrix
        try {
            BufferedReader phi_input = new BufferedReader(new FileReader("test/" + expName + ".phi"));
            BufferedReader theta_input = new BufferedReader(new FileReader("test/" + expName + ".theta"));
            BufferedReader pair_input = new BufferedReader(new FileReader("test/" + expName + ".pair"));
            while (true){
                String pair = pair_input.readLine();
                String theta = theta_input.readLine();
                if ((pair == null) || (theta == null))
                    break;
                String[] a_r = pair.split(" ");
                String[] theta_values = theta.split(" ");
                List<Double> values = new ArrayList();
                for (String v : theta_values){
                    values.add(new Double(v));
                }
                authorsDistribution.put(new Integer(a_r[0]), values);
                recipientsDistribution.put(new Integer(a_r[1]), values);
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ART.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ART.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            //save distributuion matrix to file
            PrintWriter author_output = new PrintWriter(new FileWriter("test/" + expName + ".author"));
            PrintWriter recipient_output = new PrintWriter(new FileWriter("test/" + expName + ".ricipient"));
            for (Map.Entry<Integer, List<Double>> entry : authorsDistribution.entrySet()){
                StringBuilder line = new StringBuilder();
                line.append(entry.getKey() + " ");
                for (Double d : entry.getValue())
                    line.append(d + " ");
                author_output.println(line);
            }
            for (Map.Entry<Integer, List<Double>> entry : recipientsDistribution.entrySet()){
                StringBuilder line = new StringBuilder();
                line.append(entry.getKey() + " ");
                for (Double d : entry.getValue())
                    line.append(d + " ");
                recipient_output.println(line);
            }
            author_output.close();
            recipient_output.close();
        } catch (IOException ex) {
            Logger.getLogger(ART.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
