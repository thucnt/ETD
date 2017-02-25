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
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author thucnt
 */
public class Corpus {
    private String citationFile;
    private int[][] documents;
    //private HashSet<Integer> refList;//store reference id paper in corpus
    private HashMap<Integer,Integer> corpusId2PaperId;
    
    public Corpus(String citationFile){
        this.citationFile = citationFile;
        createDocumentMatrix(citationFile);
    }
    private Integer mappingPaperId2CorpusId(Integer idPaper){
        Integer id = null;
        for (Map.Entry<Integer, Integer> entry : corpusId2PaperId.entrySet()) {
            if (Objects.equals(idPaper, entry.getValue())) {
                id = entry.getKey();
                break;
            }
        }
        if (id == null){
            id = corpusId2PaperId.size() + 1;
            corpusId2PaperId.put(id, idPaper);
        }
        return id;
    }
    private void createDocumentMatrix(String citationFile){
        BufferedReader inputStream = null;
        HashMap<Integer,List<Integer>> papers = new HashMap<Integer,List<Integer>>();
        //refList = new HashSet<Integer>();
        corpusId2PaperId = new HashMap<>();
        try {
            inputStream = new BufferedReader(new FileReader(citationFile));
            int count = 0;
            while (count < 5){
                String line = inputStream.readLine();
                if (line != null){
                    String[] tmp = line.split(";");
                    if (tmp.length == 2){
                        Integer idPaper = new Integer(tmp[0]);
                        Integer key = this.mappingPaperId2CorpusId(idPaper);
                        String[] ref = tmp[1].split(",");
                        List<Integer> refPapers = new ArrayList();
                        for (String s : ref){
                            Integer refId = this.mappingPaperId2CorpusId(new Integer(s));
                            //refList.add(refId);
                            refPapers.add(refId);
                        }
                        papers.put(key, refPapers);
                    }
                    count++;
                }
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(CitationLda.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(CitationLda.class.getName()).log(Level.SEVERE, null, ex);
        }
        documents = new int[corpusId2PaperId.entrySet().size()][];
        
        for (Map.Entry<Integer, List<Integer>> entry : papers.entrySet()) {
            Integer key = entry.getKey();
            List<Integer> value = entry.getValue();
            int size = value.size();
            if (size != 0){
                documents[key - 1] = new int[value.size()];
                for (int j = 0; j < documents[key - 1].length; j++){
                    documents[key - 1][j] = value.get(j);               
                }
            }else{
                documents[key - 1] = new int[1];
                documents[key - 1][0] = key;
            }
        }
        validateDocumentMatrix();
    }
    private void validateDocumentMatrix(){
       
    }

    int[][] getDocumentMatrix() {
        return documents;
    }

    int getSize() {
        return corpusId2PaperId.keySet().size();
    }
}
