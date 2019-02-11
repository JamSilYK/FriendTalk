package com.example.lhw.myapplication3;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class HelpFragment extends Fragment {

    final String TAG = "HelpFragment";

    ImageView ad_img;
    TextView ad_text;
    ArrayList<AdModel> adModel_list;
    int i = 1;
    Button bt_homepage;
    Button bt_email;
    Button bt_call;
    Button bt_ad_del;
    ConstraintLayout ad_layout;

    RecyclerView mRecyclerview;

    Handler handler =null;
    int value;

    Thread t;

    boolean flag = false;

    Animation aniFlow = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_help, container, false);

        bt_homepage = v.findViewById(R.id.bt_homepage);
        bt_email = v.findViewById(R.id.bt_email);
        bt_call = v.findViewById(R.id.bt_call);
        ad_img = v.findViewById(R.id.ad_img);
        ad_text = v.findViewById(R.id.ad_text);
        bt_ad_del = v.findViewById(R.id.bt_ad_del);
        ad_layout = v.findViewById(R.id.ad_layout);
        aniFlow = AnimationUtils.loadAnimation(getActivity(), R.anim.alpha);



        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what){
                    case 1:
                        Log.d(TAG, "handleMessage1: " + msg.what);
                        ad_img.setImageResource(R.drawable.teamnovalogo);
                        ad_text.setText("왕초보 코딩 교육");
                        i = 2;
                        break;

                    case 2:
                        Log.d(TAG, "handleMessage2: " + msg.what);
                        /*ad_img.setImageResource(R.drawable.chat_bubble);*/
                        ad_text.setText("제일 잘합니다");
                        i = 1;
                        break;
                }

                super.handleMessage(msg);
            }
        };


        t = new Thread(new Runnable() {
            @Override
            public void run() {
                while(flag){

                    try {
                        Thread.sleep(1 * 5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    Log.d(TAG, "run: i " + i);

                    if(i == 1){
                        Message msg = handler.obtainMessage(1);
                        handler.sendMessage(msg);
                    }

                    else if(i == 2){
                        Message msg = handler.obtainMessage(2);
                        handler.sendMessage(msg);
                    }
                }
            }
        });
        t.setDaemon(true);
        flag = true;
        t.start();

        bt_ad_del.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(t.isAlive()){
                    if(handler != null){
                        ad_layout.startAnimation(aniFlow);
                        ad_layout.setVisibility(View.GONE);
                        handler.removeMessages(0);
                        t.interrupt();
                        flag = false;
                        t = null;
                    }
                }
            }
        });

        bt_homepage.setOnClickListener(new View.OnClickListener() { //위치
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse("http://teamnova.co.kr/");
                Intent it  = new Intent(Intent.ACTION_VIEW,uri);
                startActivity(it);
            }
        });

        bt_email.setOnClickListener(new View.OnClickListener() { //이메일
            @Override
            public void onClick(View v) {
                Intent email = new Intent(Intent.ACTION_SEND);
                email.setType("plain/text");
                // email setting 배열로 해놔서 복수 발송 가능
                String[] address = {"teamnova@address.com"};
                email.putExtra(Intent.EXTRA_EMAIL, address);
                email.putExtra(Intent.EXTRA_SUBJECT,"보내질 email 제목");
                /*email.putExtra(Intent.EXTRA_TEXT,"보낼 email 내용을 미리 적어 놓을 수 있습니다.\n");*/
                startActivity(email);
            }
        });

        bt_call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:"+"01056531752"));
                startActivity(intent);
            }
        });

        return v;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeMessages(0);
        flag = false;
        t = null;
    }
}
