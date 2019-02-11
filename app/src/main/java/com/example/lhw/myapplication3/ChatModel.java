package com.example.lhw.myapplication3;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class ChatModel {

    public Map<String, Boolean> users = new HashMap<>(); //채팅방의 유저들
    public Map<String, Comment> comments = new HashMap<>(); //채팅방의 대화내용

    public static class Comment implements Serializable {
        public String uid;
        public String message;
        public String hour;
        public String photo_uri;
        public Map<String, Object> readUsers = new HashMap<>();
    }
}
