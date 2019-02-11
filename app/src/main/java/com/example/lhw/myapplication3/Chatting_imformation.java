package com.example.lhw.myapplication3;

import java.io.Serializable;

public class Chatting_imformation implements Serializable {
    public String dictionary;
    public String uri;
    public String email;
    public String name;
    public String hour;
    public String contents;

    public Chatting_imformation() {

    }

    public Chatting_imformation(String dictionary, String uri, String email, String name) {
        this.dictionary = dictionary;
        this.uri = uri;
        this.email = email;
        this.name = name;
    }
}
