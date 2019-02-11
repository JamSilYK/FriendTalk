package com.example.lhw.myapplication3;

import android.view.View;

import java.lang.reflect.Array;
import java.util.ArrayList;

public interface OnItemClick {
    void onClick(String value);
    void onClick(View view, int position, ArrayList<User> userInfoArrayList);
    boolean onLongClick(View view, int position, ArrayList<User> userInfoArrayList);

}


