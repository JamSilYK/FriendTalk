package com.example.lhw.myapplication3;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import static android.view.View.GONE;

public class SNSModifyActivity extends AppCompatActivity {

    final String TAG = "SNSModifyActivity";
    final int MODIFY = 30;

    //*******************************엑티비티 아이디*************************************//
    ImageView sns_myprofile; //내 프로필 사진
    ImageView border; //사진이나 동영상 올렸을때 보여지는 영역
    TextView sns_myname; //내 이름
    TextView contents_text;//텍스트내용
    Button bt_send;//올리기 버튼
    ConstraintLayout constraintLayout8;
    //*******************************엑티비티 아이디*************************************//
    SNSVo modify_snsVo;

    //*******************************데이터베이스****************************************//
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference myRef;
    private DatabaseReference SNSRef;
    private StorageReference mStorageRef;
    //*******************************데이터베이스****************************************//

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_snsmodify);

        modify_snsVo = (SNSVo) getIntent().getExtras().getSerializable("modify_snsVo");
        Log.d(TAG, "modify_snsVo.name: " + modify_snsVo.name);

        sns_myprofile = findViewById(R.id.sns_myprofile);
        sns_myname = findViewById(R.id.sns_myname);
        contents_text = findViewById(R.id.text_contents);
        border = findViewById(R.id.border);
        bt_send = findViewById(R.id.bt_send);


        Picasso.get().load(Uri.parse(modify_snsVo.uri)).transform(new CircleTransform()).into(sns_myprofile);
        sns_myname.setText(modify_snsVo.name);
        contents_text.setHint(modify_snsVo.contents_text);
        if(modify_snsVo.contents_uri != null){
            Picasso.get().load(Uri.parse(modify_snsVo.contents_uri)).into(border);
        }

        else {
            constraintLayout8 = findViewById(R.id.constraintLayout8);
            border.setVisibility(GONE);
            constraintLayout8.setVisibility(GONE);
        }

        bt_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                database.getReference("SNS").child(modify_snsVo.checkd_date).removeValue(new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                        SNSVo modifyVo = new SNSVo();

                        Calendar c = Calendar.getInstance();
                        SimpleDateFormat df2 = new SimpleDateFormat("hh:mm a");
                        String tmp_date = df2.format(c.getTime());

                        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        String formattedDate = df.format(c.getTime());

                        String checked_date = formattedDate;
                        String db_contents_text = contents_text.getText().toString();
                        if(modify_snsVo.contents_uri != null){
                            String contents_uri = modify_snsVo.contents_uri;
                            modifyVo.contents_uri = contents_uri;
                        }
                        String date = tmp_date;
                        String email = modify_snsVo.email;
                        String name = modify_snsVo.name;
                        String uri = modify_snsVo.uri;

                        modifyVo.checkd_date = checked_date;
                        modifyVo.contents_text = db_contents_text;
                        modifyVo.date = date;
                        modifyVo.email = email;
                        modifyVo.name = name;
                        modifyVo.uri = uri;
                        modifyVo.modify = true;

                        if(modify_snsVo.likeit.size()!=0){
                            modifyVo.likeit.putAll(modify_snsVo.likeit);
                        }

                        if(modify_snsVo.contents_uri != null){
                            String contents_uri = modify_snsVo.contents_uri;
                            modifyVo.contents_uri = contents_uri;
                        }
                        database.getReference("SNS").child(modifyVo.checkd_date).setValue(modifyVo).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                intent.putExtra("check", "SNSWriteActivity");
                                startActivity(intent);
                            }
                        });
                    }
                });
            }
        });
    }
}
