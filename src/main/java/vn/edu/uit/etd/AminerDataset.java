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
import java.io.BufferedWriter;
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
    public static void writePapers(String dataFolder, Paper p){
        Integer year = p.getYear();
        String folderName = dataFolder + File.separator;
        if (year == null){
            folderName += "all";
        }
        else{
            folderName += year;
        }
        File folder = new File(folderName);
        if (!folder.exists()){
            folder.mkdir();
        }
        File f = new File(folder,p.getId() + ".txt");
        BufferedWriter output = null;
        try {
            output = new BufferedWriter(new FileWriter(f));
            StringBuilder content = new StringBuilder();
            content.append(p.getTitle());
            String abs = p.getAbs();
            if (abs != null)
                content.append(". " + abs);
            output.write(content.toString());
            output.close();
        } catch (IOException ex) {
            Logger.getLogger(AminerDataset.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public static void writeIdYearTitleFile(PrintWriter outFile,Paper p){
        String line = p.getId() + " " + p.getYear() + " " + p.getTitle() + "\n";
        outFile.println(line);
    }
    public static void main(String[] args){
        File f = new File("/Users/thucnt/Downloads/dblp/original-data/AMiner-Paper.txt");
        AMinerParser parser = new AMinerParser("/Users/thucnt/Downloads/dblp/data");
        long count = 0;
        
        try {
            Scanner aminerInput = new Scanner(new FileInputStream(f));
            PrintWriter papersOutput = new PrintWriter(new FileWriter("D:\\aminer\\papers.txt"));
            PrintWriter paperCitationOutput = new PrintWriter(new FileWriter("D:\\aminer\\papers_citation.txt"));
            PrintWriter idYearTitleFile = new PrintWriter(new BufferedWriter(new FileWriter("/Users/thucnt/Downloads/dblp/data/IdYearTitle.txt")));
            String s = null;
            List<String> strList = null;
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
                    if (strList != null){
                        count++;
                        Paper p = parser.parse(strList);
                        writeIdYearTitleFile(idYearTitleFile,p);
//                        String content = p.getAbs();
//                        if (content == null)
//                            content = p.getTitle();
//                        else
//                            content = p.getTitle() + "; " + content;
                        //papersOutput.println(p.getId() + " " + content);
                        //writePapers("D:\\aminer",p);
//                        List<Long> refList = p.getRefList();
//                        StringBuilder line = new StringBuilder();
//                        line.append(p.getId() + ";");
//                        for (Long l : refList){
//                            line.append(l + ",");
//                        }
                        //paperCitationOutput.println(line);
                    }
                    strList = new ArrayList();
                }
                strList.add(s);
            }
            papersOutput.flush();
            papersOutput.close();
            paperCitationOutput.flush();
            paperCitationOutput.close();
            idYearTitleFile.flush();
            idYearTitleFile.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(AminerDataset.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(AminerDataset.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
