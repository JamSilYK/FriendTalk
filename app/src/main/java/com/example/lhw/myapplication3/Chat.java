package com.example.lhw.myapplication3;

import android.util.Log;

public class Chat {
    public String contents;
    public String email;
    public String name;
    public String uri;
    public String date;
    public String hour;

    public Chat(){}

    public Chat(String contents, String email) {
        Log.d("Chat", "Chat Class");
        this.contents = contents;
        this.email = email;
    }

    public Chat(String contents, String email, String name, String uri, String date) {
        this.contents = contents;
        this.email = email;
        this.name = name;
        this.uri = uri;
        this.date = date;
    }

    public String getContents() {
        return contents;
    }

    public void setContents(String contents) {
        this.contents = contents;
    }
}
