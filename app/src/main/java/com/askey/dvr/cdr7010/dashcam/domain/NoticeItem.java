package com.askey.dvr.cdr7010.dashcam.domain;

import java.io.Serializable;

public class NoticeItem implements Serializable {
    private static final long serialVersionUID = 1L;

    private String title;
    private String description;

    public void setDescription(String description){
        this.description = description;
    }
    public void setTitle(String title){
        this.title = title;
    }
    public String getTitle(){
        return title;
    }
    public String getDescription(){
        return description;
    }
}