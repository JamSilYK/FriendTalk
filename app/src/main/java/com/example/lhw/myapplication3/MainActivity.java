package com.example.lhw.myapplication3;



import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.BottomNavigationView;


import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;
import java.util.Map;


public class MainActivity extends AppCompatActivity {

    private final int FriendFragment = 1;
    private final String TAG = "MainActivity";

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef;
    Fragment fragment;
    long lastPressted;
    private FirebaseAuth mAuth;

    String email;

    User user;

    String check;


    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                    case R.id.navigation_friend: //친구 프래그
                        fragment = new FriendFragment();
                        switchFragment(fragment);
                        return true;
                    case R.id.navigation_chat: // 채팅방 프래그
                        fragment = new ChattingFragment();
                        switchFragment(fragment);
                        return true;

                    case R.id.navigation_sns: //Sns 툴바
                        fragment = new SNSFragment();
                        switchFragment(fragment);
                        return true;

                    case R.id.navigation_help: //고객센터
                        fragment = new HelpFragment();
                        switchFragment(fragment);
                        return true;

                    case R.id.navigation_game:
                        fragment = new GameFragment();
                        switchFragment(fragment);
                        return true;

            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            check = getIntent().getExtras().getString("check");
        }

        catch (NullPointerException ne){

        }

        passPushTokenToServer();

        if(check == null) {
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            FriendFragment fragment = new FriendFragment();
            fragmentTransaction.add(R.id.content, fragment);
            fragmentTransaction.commit();

            BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
            navigation.setSelectedItemId(R.id.navigation_friend);
            navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

            onBackPressed();
        }

        else if(check.equals("SNSWriteActivity")){

            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            SNSFragment fragment = new SNSFragment();
            fragmentTransaction.add(R.id.content, fragment);
            fragmentTransaction.commit();

            BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
            navigation.setSelectedItemId(R.id.navigation_sns);
            navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
            check = null;
        }

        else {
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            ChattingFragment fragment = new ChattingFragment();
            fragmentTransaction.add(R.id.content, fragment);
            fragmentTransaction.commit();

            BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
            navigation.setSelectedItemId(R.id.navigation_chat);
            navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
            check = null;
        }



    }

    void passPushTokenToServer(){
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String token = FirebaseInstanceId.getInstance().getToken();
        Map<String, Object> map = new HashMap<>();
        map.put("pushToken", token);

        FirebaseDatabase.getInstance().getReference().child("user").child(uid).updateChildren(map);
    }

    public void switchFragment(Fragment fragment){
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.content, fragment);
        transaction.commit();
    }

    @Override //뒤로가기를 눌렀을 때
    public void onBackPressed() {
        if(System.currentTimeMillis()-lastPressted<1500){
            finish();
        }

        else {
            Toast.makeText(this, "한번 더 누르면 종료됩니다.", Toast.LENGTH_SHORT).show();
            lastPressted = System.currentTimeMillis();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: ");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop: ");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: ");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "onRestart: ");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
    }
}
