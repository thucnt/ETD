/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vn.edu.uit.etd;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author thucnt
 */
public class Paper {
    private Long id;
    private String title;
    private List<Long> refList;
    private String abs;

    
    public void addRef(Long id){
        if (refList == null)
            refList = new ArrayList();
        refList.add(id);
    }
    public String getAbs() {
        return abs;
    }

    public void setAbs(String abs) {
        this.abs = abs;
    }

    public List<Long> getRefList() {
        return refList;
    }

    public void setRefList(List<Long> refList) {
        this.refList = refList;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    
}
