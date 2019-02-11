package com.example.lhw.myapplication3;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.lang.reflect.Array;
import java.net.CookieHandler;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;


public class SNSFragment extends Fragment implements SNSOnitemClick, SwipeRefreshLayout.OnRefreshListener {

    final String TAG = "SNSFragment";

    /////////////////////////////////어뎁터 뷰타입/////////////////////////////////
    final int my = 1;
    final int other = 2;
    final int DELETE = 20;
    final int MODIFY = 30;
    final int LIKEIT = 40;
    //////////////////////////////////어뎁터 뷰타입////////////////////////////////

    ////////////////////////////////View 아이디//////////////////////////
    CardView bt_write; //게시물작성
    ImageView myprofile;
    SwipeRefreshLayout swipe_layout;
    ////////////////////////////////View 아이디//////////////////////////

    ///////////////////////////////리싸이클러뷰////////////////////////////
    RecyclerView mRecyclerView;
    RecyclerView.LayoutManager mLayoutManager;
    View menu2;//아이템 메뉴
    ///////////////////////////////리싸이클러뷰////////////////////////////

    //////////////////////////////자바 변수////////////////////////////////
    ArrayList<SNSVo> snsvo_list; //snsvo클래스 리스트
    User user; //내 정보
    User frined; //친구 정보
    ArrayList<String> friends_list; //친구 등록한 리스트
    //////////////////////////////자바 변수////////////////////////////////

    //////////////////////////////데이터베이스 변수////////////////////////////////
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef;
    Query SNSRef;
    private FirebaseAuth mAuth;
    private StorageReference mStorageRef;
    FirebaseUser currentUser;
    //////////////////////////////데이터베이스 변수////////////////////////////////

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_sns, container, false);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        snsvo_list = new ArrayList<>();

        swipe_layout = v.findViewById(R.id.swipe_layout);
        mRecyclerView = v.findViewById(R.id.mRecyclerView);
        myprofile = v.findViewById(R.id.myprofile);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new WrapContentLinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        final SNSAdapter snsAdapter = new SNSAdapter(getActivity(), snsvo_list, currentUser.getEmail(),this);
        mRecyclerView.setAdapter(snsAdapter);


        friends_list = new ArrayList<>();

        myRef = database.getReference("user").child(currentUser.getUid());
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                user = dataSnapshot.getValue(User.class);
                Uri uri = Uri.parse(user.uri);
                Picasso.get().load(uri).transform(new CircleTransform()).into(myprofile);
                Log.d(TAG, "user.toString(): " + user.toString());

                DataSnapshot dataSnapshot1 = dataSnapshot.child("friends");

                for(DataSnapshot dataSnapshot2 : dataSnapshot1.getChildren()){
                    String a = dataSnapshot2.getKey();
                    Log.d(TAG, "onDataChange a: " + a);
                    friends_list.add(a);
                }

                for(int i = 0; i<friends_list.size(); i++) {
                    Log.d(TAG, "friends_list.get(i): " + friends_list.get(i));
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        SNSRef = database.getReference("SNS");
        SNSRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                snsvo_list.clear();
                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                    SNSVo snsVo = dataSnapshot1.getValue(SNSVo.class);
                    snsvo_list.add(snsVo);
                    Log.d(TAG, "snsvo_list: " + snsvo_list.get(0).likeit.toString());
                }

                Collections.reverse(snsvo_list);

                mRecyclerView.getAdapter().notifyItemInserted(snsvo_list.size() - 1);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        bt_write = v.findViewById(R.id.bt_write);
        bt_write.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { //게시물 Wirte로 이동
                Intent intent = new Intent(getActivity(), SNSWriteActivity.class);
                startActivity(intent);
            }
        });

        swipe_layout.setOnRefreshListener(this);

        return v;
    }


    @Override
    public void onClick(final View view, final int position, final ArrayList<SNSVo> snsvo_list, int viewtype) {

        if(viewtype == DELETE) {
            database.getReference("SNS").child(snsvo_list.get(position).checkd_date).removeValue(new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                    /*snsvo_list.remove(position);*/
                    mRecyclerView.getAdapter().notifyDataSetChanged();
                    Toast.makeText(getActivity(), "삭제하였습니다.", Toast.LENGTH_SHORT).show();
                }
            });

        }

        else if(viewtype == MODIFY){
            Intent intent = new Intent(getActivity(), SNSModifyActivity.class);
            SNSVo modify_snsVo = snsvo_list.get(position);
            intent.putExtra("modify_snsVo", modify_snsVo);
            startActivity(intent);
        }

    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        Log.d(TAG, "onCreateContextMenu: 1");

        menu.setHeaderTitle("따이뜰");
        menu.add(0,1,100,"빨강");
        menu.add(0,2,100,"녹색");
        menu.add(0,3,100,"파랑");



        if(v == menu2){
            Log.d(TAG, "onCreateContextMenu: 1");
            getActivity().getMenuInflater().inflate(R.menu.mymenu, menu);
        }
    }

    @Override
    public void onLongClickListener(View view, int position, ArrayList<SNSVo> snsvo_list) {

    }

    @Override
    public void onRefresh() {
        Intent intent = new Intent(getActivity(), MainActivity.class);
        intent.putExtra("check", "SNSWriteActivity");
        startActivity(intent);
    }
}
