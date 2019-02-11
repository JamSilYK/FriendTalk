package com.example.lhw.myapplication3;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ClickMyProfileActivity extends AppCompatActivity {

    final String TAG = "ClickMyProfileActivity";
    final int REQUEST_ACT = 3; //내프로필 사진촬영
    final int REQUEST_CLICK_MYPROFILE = 10;

    public static String chatting_list_name = null;


    Button bt_chat;
    Button bt_profile;
    TextView myprofile_name;

    long lastPressted;

    ImageView myprofile_profile_image;

    String st_uri;

    Bitmap bitmap;

    StorageReference mStorageRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_click_my_profile);

        FirebaseUser Currentuser = FirebaseAuth.getInstance().getCurrentUser();

        if(getIntent()!=null){
            String myname = getIntent().getExtras().getString("myname"); //내 프로필 클릭 후에 텍스트 이름 세팅
            myprofile_name = findViewById(R.id.myprofile_name);
            myprofile_name.setText(myname);
        }

        myprofile_profile_image = findViewById(R.id.myprofile_profile_image); //내 현재 프로필 사진

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("user").child(Currentuser.getUid());
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                Picasso.get().load(Uri.parse(user.uri)).centerInside().fit().into(myprofile_profile_image);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        bt_chat = findViewById(R.id.myprofile_bt_chat); //채팅방 입장
        bt_chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        bt_profile = findViewById(R.id.myprofile_bt_profile); //프로필 버튼 눌렀을 때
        bt_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, REQUEST_ACT);
            }
        });
        onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)  {
        super.onActivityResult(requestCode, resultCode, data);

        if(REQUEST_ACT == requestCode && resultCode == RESULT_OK && data !=null){

            if(data.getData()!=null){
                Uri image = data.getData();
                st_uri = image.toString();
                Intent intent = new Intent();
                intent.putExtra("st_uri", st_uri);
                setResult(REQUEST_CLICK_MYPROFILE, intent);
                finish();
            }
        }

        else {

            try{
                setResult(55);
                Toast.makeText(this, "사진을 선택하지 않았습니다.", Toast.LENGTH_SHORT).show();
                finish();
            }

            catch (NullPointerException be){
                be.printStackTrace();
            }
        }
    }

    @Override //뒤로가기를 눌렀을 때
    public void onBackPressed() {
        if(System.currentTimeMillis()-lastPressted<1500){
            finish();
        }

        else {
            lastPressted = System.currentTimeMillis();
        }
    }
}
