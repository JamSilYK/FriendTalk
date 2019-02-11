package com.example.lhw.myapplication3;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class SNSVo implements Serializable {
    public String name;
    public String email;
    public String uri;
    public String contents_text;
    public String contents_uri;
    public String date;
    public String checkd_date;
    public Map<String, Boolean> likeit = new HashMap<>();
    public Boolean modify;

    public static class SNSComment{
        public String date;
        public String comment;
        public String uid;

    }

    public SNSVo(){

    }

    public SNSVo(String name, String email, String uri, String contents_text, String contents_uri, String date, String checkd_date) {
        this.name = name;
        this.email = email;
        this.uri = uri;
        this.contents_text = contents_text;
        this.contents_uri = contents_uri;
        this.date = date;
        this.checkd_date = checkd_date;
    }

    public SNSVo(String name, String email, String uri, String contents_text, String date, String checkd_date) {
        this.name = name;
        this.email = email;
        this.uri = uri;
        this.contents_text = contents_text;
        this.date = date;
        this.checkd_date = checkd_date;
    }
}
