/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vn.edu.uit.etd;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author thucnt
 */
public class AminerDataset {
    public static void main(String[] args){
        File f = new File("/Users/thucnt/Downloads/dblp/original-data/AMiner-Paper.txt");
        AMinerParser parser = new AMinerParser("data");
        long count = 0;
        try {
            Scanner aminerInput = new Scanner(new FileInputStream(f));
            PrintWriter papersOutput = new PrintWriter(new FileWriter("data/papers.txt"));
            PrintWriter paperCitationOutput = new PrintWriter(new FileWriter("data/papers_citation.txt"));
            String s = null;
            List<String> paper = null;
            while ((s = aminerInput.nextLine()) != null){
                if ((count % 10000) == 0){
                    System.out.println("Papers: " + count);
//                    papersOutput.flush();
//                    papersOutput.close();
//                    paperCitationOutput.flush();
//                    paperCitationOutput.close();
//                    break;
                }
                if (s.startsWith("#index")){
                    if (paper != null){
                        count++;
                        Paper p = parser.parse(paper);
                        String content = p.getAbs();
                        if (content == null)
                            content = p.getTitle();
                        else
                            content = p.getTitle() + "; " + content;
                        papersOutput.println(p.getId() + " " + content);
                        List<Long> refList = p.getRefList();
                        StringBuilder line = new StringBuilder();
                        line.append(p.getId() + ";");
                        for (Long l : refList){
                            line.append(l + ",");
                        }
                        paperCitationOutput.println(line);
                    }
                    paper = new ArrayList();
                }
//                System.out.println(s);
                paper.add(s);
            }
            papersOutput.flush();
            papersOutput.close();
            paperCitationOutput.flush();
            paperCitationOutput.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(AminerDataset.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(AminerDataset.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
