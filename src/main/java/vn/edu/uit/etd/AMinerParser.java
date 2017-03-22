/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vn.edu.uit.etd;

import java.util.List;

/**
 *
 * @author thucnt
 */
public class AMinerParser {
    private String outputFolder;

    public AMinerParser(String folder) {
        outputFolder = folder;
    }
    
    public void writeToFilePaper(){
        
    }
    
    public Paper parse(List<String> paper){
        Paper p = new Paper();
        for (String s : paper){
            int space = s.indexOf(' ');
            if (space == -1)
                continue;
            String key = s.substring(0,space);
            String content = s.substring(space + 1);
            if (key.startsWith("#index"))
                p.setId(new Long(content));
            else if (key.startsWith("#*"))
                p.setTitle(content);
            else if (key.startsWith("#!"))
                p.setAbs(content);
            else if (key.startsWith("#%"))
                p.addRef(new Long(content));
        }
        if (p.getRefList() == null){
            p.addRef(p.getId());
        }
        return p;
    }
}
