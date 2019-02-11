package com.example.lhw.myapplication3;

import android.app.Fragment;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import static android.view.View.GONE;

public class GameFragment extends Fragment {
    int check = 1;

    Button [] mButton = new Button[20];
    Animation aniFlow = null;
    Button btn_start;
    TableLayout table_layout;
    TextView myOutput;
    int complete = 0;
    int solve = 0;

    final static int Init =0;
    final static int Run =1;
    final static int Pause =2;

    int cur_Status = Init; //현재의 상태를 저장할변수를 초기화함.
    long myBaseTime;
    long myPauseTime;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v ;
        v = inflater.inflate(R.layout.fragment_game, container, false);
        btn_start = v.findViewById(R.id.btn_start);
        table_layout = v.findViewById(R.id.table_layout);
        myOutput = v.findViewById(R.id.time_out);
        table_layout.setVisibility(GONE);
        btn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                table_layout.setVisibility(View.VISIBLE);

                switch(cur_Status){
                    case Init:
                        myBaseTime = SystemClock.elapsedRealtime();
                        myTimer.sendEmptyMessage(0);
                        cur_Status = Run; //현재상태를 런상태로 변경
                        break;
                    case Run:
                        myTimer.removeMessages(0); //핸들러 메세지 제거
                        myPauseTime = SystemClock.elapsedRealtime();
                        cur_Status = Init;
                        break;
/*                    case Pause:
                        long now = SystemClock.elapsedRealtime();
                        myTimer.sendEmptyMessage(0);
                        myBaseTime += (now- myPauseTime);
                        cur_Status = Run;
                        break;*/
                }

            }
        });


        aniFlow = AnimationUtils.loadAnimation(getActivity(), R.anim.alpha);
        final int [] game_array = new int[20];

        for(int i = 0; i<game_array.length; i++) {
            game_array[i] = (int) (Math.random() * 20 + 1);

            for(int j = 0; j<i; j++){
                if(game_array[j] == game_array[i]){
                    i--;
                    break;
                }
            }
        }

        for(int i = 0; i<game_array.length; i++) {
            String a = String.valueOf(i+1);
            int k = getResources().getIdentifier("mbutton"+a, "id", getActivity().getPackageName());
            mButton[i] = v.findViewById(k);
            mButton[i].setText(String.valueOf(game_array[i]));
            final int finalI = i;
            mButton[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(check == Integer.parseInt(String.valueOf(mButton[finalI].getText()))){
                        mButton[finalI].startAnimation(aniFlow);
                        check++;
                        mButton[finalI].setVisibility(View.INVISIBLE);
                        if(check == game_array.length){
                            if(complete != 10){
                                solve = 10;
                                Toast.makeText(getActivity(), "성공했습니다.", Toast.LENGTH_SHORT).show();
                                myTimer.removeMessages(0);
                            }
                        }
                    }
                }
            });
        }



        return v;
    }

    Handler myTimer = new Handler(){
        public void handleMessage(Message msg){
            if(solve != 10){
                myOutput.setText(getTimeOut());
                //sendEmptyMessage 는 비어있는 메세지를 Handler 에게 전송하는겁니다.
                myTimer.sendEmptyMessage(0);

                if(getTimeOut().equals("00:30:00")){
                    Toast.makeText(getActivity(), "30초가 지났습니다.", Toast.LENGTH_SHORT).show();
                    complete = 10;
                    myTimer.removeMessages(0);
                    table_layout.setVisibility(View.GONE);
                }
            }

            else {
                myTimer.removeMessages(0);
            }
        }
    };

    //현재시간을 계속 구해서 출력하는 메소드
    String getTimeOut(){
        long now = SystemClock.elapsedRealtime(); //애플리케이션이 실행되고나서 실제로 경과된 시간(??)^^;
        long outTime = now - myBaseTime;
        String easy_outTime = String.format("%02d:%02d:%02d", outTime/1000 / 60, (outTime/1000)%60,(outTime%1000)/10);
        return easy_outTime;

    }

}
