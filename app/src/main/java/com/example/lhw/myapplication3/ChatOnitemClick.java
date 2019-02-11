package com.example.lhw.myapplication3;

import android.view.View;

import java.util.ArrayList;

public interface ChatOnitemClick {

    void onClick(View view, int position, ArrayList<Chatting_imformation> chatting_list);
    void onLongClickListener(View view, int position, ArrayList<Chatting_imformation> chatting_list);
}
