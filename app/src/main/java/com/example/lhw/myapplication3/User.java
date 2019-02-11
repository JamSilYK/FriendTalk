package com.example.lhw.myapplication3;

import java.io.Serializable;

public class User implements Serializable {

    public String email;
    public String name;
    public String password;
    public String phone;
    public String uri;
    public String pushToken;


    public User(){

    }

    public User(String email, String name, String password, String phone) {
        this.email = email;
        this.name = name;
        this.password = password;
        this.phone = phone;
    }

    public User(String email, String name, String password, String phone, String uri) {
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.name = name;
        this.uri = uri;
    }

    public String toString(){

        return "email = " + email + ", " + "name = " + name+", " +"password = " + password +", " + "phone = " + phone + "uri : " + uri;
    }
}

