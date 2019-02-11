package com.example.lhw.myapplication3;

import android.view.View;

import java.util.ArrayList;

public interface SNSOnitemClick {
    void onClick(View view, int position, ArrayList<SNSVo> snsvo_list, int viewtype);
    void onLongClickListener(View view, int position, ArrayList<SNSVo> snsvo_list);

}
