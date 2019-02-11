package com.example.lhw.myapplication3;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;



public class ChangeNameActivity extends AppCompatActivity {

    final int CHANGE_ROOM_NAME = 13;
    final String TAG = "ChangeNameActivity";

    Button bt_changed_send;
    TextView changed_name;
    String name;

    int position;
    Chatting_imformation chatting_imformation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_name);
        Intent intent = getIntent();
        chatting_imformation = (Chatting_imformation)intent.getSerializableExtra("chatting_imformation");
        position = intent.getExtras().getInt("position");

        bt_changed_send = findViewById(R.id.bt_changed_send);
        changed_name = findViewById(R.id.changed_name);
        changed_name.setHint(chatting_imformation.dictionary);

        bt_changed_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(changed_name.getText().toString().equals("")){
                    Toast.makeText(ChangeNameActivity.this, "다시 입력해주세요", Toast.LENGTH_SHORT).show();
                }

                else {
                    name = changed_name.getText().toString();
                    Intent intent = new Intent();
                    intent.putExtra("changed_name", name);
                    intent.putExtra("position", position);
                    setResult(CHANGE_ROOM_NAME, intent);
                    finish();
                }

                SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);
                SharedPreferences.Editor editor = pref.edit();
                editor.putString("changename", chatting_imformation.name);
                editor.putString("changed_name", name);
                editor.commit();


            }
        });

    }
}
