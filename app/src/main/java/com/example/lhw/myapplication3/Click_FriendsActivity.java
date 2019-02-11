package com.example.lhw.myapplication3;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class Click_FriendsActivity extends AppCompatActivity {

    final String TAG = "Click_FriendsActivity";

    User item_user; //클릭한 친구
    User myuser; //로그인한 유저

    String destinationUid;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_click_friends);

        Intent intent = getIntent();
        item_user = (User) intent.getSerializableExtra("item_user");
        myuser = (User) intent.getSerializableExtra("myuser"); //로그인한 유저

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("user");

        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()){
                    User tmpuser = dataSnapshot1.getValue(User.class);
                    if(tmpuser.email.equals(item_user.email)){
                        destinationUid = dataSnapshot1.getKey(); //
                        Log.d(TAG, "onDataChange: destinationUid " + destinationUid);
                        item_user = tmpuser;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });




        TextView CK_friends_name = findViewById(R.id.CK_friends_name);
        ImageView CK_friends_profile = findViewById(R.id.CK_friends_profile);
        Button CK_friends_bt_chat = findViewById(R.id.CK_friends_bt_chat);
        CK_friends_name.setText(item_user.name);
        Picasso.get().load(item_user.uri).centerInside().fit().into(CK_friends_profile);

        CK_friends_bt_chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ChattingRoomActivity.class);
                intent.putExtra("item_user", item_user);
                intent.putExtra("pagecheck", 0);
                intent.putExtra("destinationUid", destinationUid);
                startActivity(intent);
            }
        });


    }

    public void onclickbt_call(View v) {
        Toast.makeText(getApplicationContext(), "전화버튼을 눌렀습니다.",Toast.LENGTH_LONG).show();
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:"+item_user.phone));

        startActivity(intent);

    }
}
