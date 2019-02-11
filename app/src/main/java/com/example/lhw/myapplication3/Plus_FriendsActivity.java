package com.example.lhw.myapplication3;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;

public class Plus_FriendsActivity extends AppCompatActivity implements OnItemClick{

    final String TAG = "Plus_FriendsActivity";

    boolean check = false;
    boolean dialog_check = false;

    Button bt_search;
    TextView id_search;

    private FirebaseAuth mAuth;
    FirebaseDatabase database;
    DatabaseReference myRef;

    User user;
    User user2;

    CardView cdv;
    ImageView friend_picture;
    TextView friend_name;

    AlertDialog.Builder builder;

    FirebaseUser currentuser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plus_friends);

        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("user");
        mAuth = FirebaseAuth.getInstance();

        id_search = findViewById(R.id.id_search);

        friend_picture = (ImageView)findViewById(R.id.friend_pictrue);
        friend_name = findViewById(R.id.friend_name);


        bt_search = findViewById(R.id.bt_search);
        bt_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentuser = mAuth.getCurrentUser();
                if(id_search.getText().toString().equals("")){
                    Toast.makeText(Plus_FriendsActivity.this, "입력해주세요", Toast.LENGTH_SHORT).show();
                    return;
                }

                else {
                    Log.d(TAG, "onDataChange: 0");
                    Search_name();
                }
            }
        });
        cdv = (CardView)findViewById(R.id.search_card_view); //
        cdv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                show();

            }
        });

    }

    public void Search_name(){

        myRef.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String tmp = id_search.getText().toString();
                currentuser = mAuth.getCurrentUser();

                Log.d(TAG, "onDataChange: ");
                String value = dataSnapshot.getValue().toString();
                Log.d(TAG, "onDataChange:value: " + value);

                Log.d(TAG, "currentuser.getEmail() " + currentuser.getEmail().toString());

                for(DataSnapshot dataSnapshot1: dataSnapshot.getChildren()){
                    user = dataSnapshot1.getValue(User.class);
                    if(user.name.equals(tmp) && user.email.equals(currentuser.getEmail())==false){
                        if(dataSnapshot.child(currentuser.getUid()).child("friends").hasChild(user.name)==false){
                            user2 = user;
                            break;
                        }

                       /* Log.d(TAG, "user2 onDataChange: " + user2.toString());*/
                    }
                }

                if(user2 == null || user2.equals("")){
                    Toast.makeText(Plus_FriendsActivity.this, "친구를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();

                }

                else {
                    Picasso.get().load(user2.uri).transform(new CircleTransform()).into(friend_picture);

                    friend_name.setText(user2.name);

                    cdv.setVisibility(View.VISIBLE);
                }


            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w("Plus_FriendsActivity", "Failed to read value.", error.toException());
            }
        });
    }

    void show() {
        builder = new AlertDialog.Builder(this);

        builder.setTitle("AlertDialog Title");
        builder.setMessage("친구추가 하겠습니까?");
        builder.setNegativeButton("아니오",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getApplicationContext(),"아니오를 선택했습니다.",Toast.LENGTH_LONG).show();
                    }
                });
        builder.setPositiveButton("예",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getApplicationContext(),"친구가 추가 되었습니다.",Toast.LENGTH_LONG).show();
                        /*user2 = new User(user.email, user.name, user.password, user.phone);*/

                        //myRef.child(currentuser.getUid()).child("friends").child(user2.name).setValue(user2);

                        ArrayList<User> user_friends = new ArrayList<>();


                        SharedPreferences sharedPreferences = getSharedPreferences("shared", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        Gson gson = new Gson();

                        String json = sharedPreferences.getString("user_friends", null);
                        Log.d(TAG, "sharedPreferences: " + json);

                        if(json == null){ //처음 등록할때 바로 sharedpreference에 데이터 세팅
                            user_friends.add(user2);
                            String fjson = gson.toJson(user_friends);
                            editor.putString("user_friends", fjson);
                            editor.apply();
                        }

                        else { //이미 친구가 존재한다면 타입을 받아와서 유저 추가
                            Type type = new TypeToken<ArrayList<User>>() {}.getType();
                            user_friends = gson.fromJson(json, type);
                            for(int i = 0; i<user_friends.size(); i++) {
                                if(user_friends.get(i).name.equals(user2.name) == false){
                                    user_friends.add(user2);
                                }
                            }

                            String fjson = gson.toJson(user_friends);
                            editor.putString("user_friends", fjson);
                            editor.apply();
                        }

                        for(int i = 0; i<user_friends.size(); i++) {
                            Log.d(TAG, "onClick: " + user_friends.get(i).name);
                        }

                        myRef.child(currentuser.getUid()).child("friends").child(user2.name).setValue(user2);

                        Intent intent2 = new Intent();
                        intent2.putExtra("user", user2);
                        setResult(RESULT_OK, intent2);
                        finish();
                    }
                });

        builder.show();
    }

    @Override
    public void onClick(String value) {

    }

    @Override
    public void onClick(View view, int position, ArrayList<User> userInfoArrayList) {

    }

    @Override
    public boolean onLongClick(View view, int position, ArrayList<User> userInfoArrayList) {
        return false;
    }

    @Override
    protected void onPause() {
        super.onPause();

    }

}
