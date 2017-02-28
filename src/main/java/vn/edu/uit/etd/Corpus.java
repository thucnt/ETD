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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author thucnt
 */
public class Corpus {
    private String citationFile;
    private int[][] documents;
    private HashSet<Integer> refList;//store reference id paper in corpus
    private HashMap<Integer,Integer> paperId2CorpusId;
    
    public Corpus(String citationFile,int size){
        this.citationFile = citationFile;
        this.paperId2CorpusId = new HashMap<>();
        createDocumentMatrix(citationFile,size);
    }
    private Integer mappingPaperId2CorpusId(Integer idPaper){
        Integer id = null;
        if (paperId2CorpusId.isEmpty()){
            id = 0;
            paperId2CorpusId.put(idPaper, id);
        }
        else{
            if (paperId2CorpusId.containsKey(idPaper))
                id = paperId2CorpusId.get(idPaper);
            else{
                id = Collections.max(paperId2CorpusId.values()) + 1;
                paperId2CorpusId.put(idPaper, id);
            }   
        }
        return id;
    }
    private void createDocumentMatrix(String citationFile, int size){
        BufferedReader inputStream = null;
        HashMap<Integer,List<Integer>> papers = new HashMap<Integer,List<Integer>>();
        refList = new HashSet<Integer>();
        try {
            inputStream = new BufferedReader(new FileReader(citationFile));
            int count = 0;
            while (count < size){
                String line = inputStream.readLine();
                if (line != null){
                    String[] tmp = line.split(";");
                    if (tmp.length == 2){
                        Integer idPaper = new Integer(tmp[0]);
                        String[] ref = tmp[1].split(",");
                        List<Integer> refPapers = new ArrayList();
                        for (String s : ref){
                            Integer refId = this.mappingPaperId2CorpusId(new Integer(s));
                            refPapers.add(refId);
                        }
                        papers.put(idPaper, refPapers);
                    }
                    if (size != 0)
                        count++;
                }
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(CitationLda.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(CitationLda.class.getName()).log(Level.SEVERE, null, ex);
        }
        //documents = new int[corpusId2PaperId.entrySet().size()][];
        documents = new int[papers.size()][];
        int i = 0;//index of row in matrix document
        for (Map.Entry<Integer, List<Integer>> entry : papers.entrySet()) {
            List<Integer> refId = entry.getValue();
            documents[i] = new int[refId.size()];
            for (int j = 0; j < refId.size(); j++){
                documents[i][j] = refId.get(j);
            }
            i++;
        }
    }
   
    int[][] getDocumentMatrix() {
        return documents;
    }

    int getCorpusSize() {
        return this.paperId2CorpusId.values().size();
    }

    public HashSet<Integer> getRefList() {
        return refList;
    }
    
}
