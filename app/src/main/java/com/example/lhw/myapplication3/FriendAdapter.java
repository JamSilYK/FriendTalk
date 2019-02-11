package com.example.lhw.myapplication3;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;

public class FriendAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    final boolean YES = true;
    final boolean NO = false;
    boolean dialog_check = false;

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef;
    private FirebaseAuth mAuth;

    private ArrayList<User> userInfoArrayList;
    Context context;
    private OnItemClick mCallback;

    public FriendAdapter(Context context, ArrayList<User> userInfoArrayList, OnItemClick listener) {
        this.userInfoArrayList = userInfoArrayList;
        this.context = context;
        this.mCallback = listener;
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder { //MyViewHolder 클래스
        ImageView ivPicture;
        TextView user_name;
        View item_card;


        MyViewHolder(View view) { //3번째로 실행
            super(view);
            ivPicture = view.findViewById(R.id.iv_picture);
            user_name = view.findViewById(R.id.user_name);
            item_card = view.findViewById(R.id.item_card);
        }
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_view_row, parent, false);

        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, final int position) {
        final MyViewHolder myViewHolder = (MyViewHolder) viewHolder;

        if (userInfoArrayList.get(position).uri != null && userInfoArrayList.get(position).uri.equals("") == false) {
            Uri realuri = Uri.parse(userInfoArrayList.get(position).uri);

            Picasso.get().load(realuri).transform(new CircleTransform()).into(myViewHolder.ivPicture);


        } else {
            myViewHolder.ivPicture.setImageResource(R.drawable.common_full_open_on_phone);
        }
        myViewHolder.user_name.setText(userInfoArrayList.get(position).name);

        myViewHolder.item_card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {// 아이템 short 클릭
                View view = myViewHolder.item_card;
                mCallback.onClick(view, position, userInfoArrayList);
                Log.d("aaaa", "버튼을 누른 아이템의 위치는 " + position);
            }
        });

        myViewHolder.item_card.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) { //아이템 long 삭제
                View view = myViewHolder.item_card;
                mCallback.onLongClick(view, position, userInfoArrayList); //FriendFragment 콜백!!
                return true; // ??
            }
        });


    }

    @Override
    public int getItemCount() {
        return userInfoArrayList.size();
    }

    /*boolean show(int position) {
        final int position_final = position;
        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(context);

        builder.setTitle("AlertDialog Title");
        builder.setMessage("친구를 삭제하시겠습니까?");
        builder.setNegativeButton("아니오",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(context, "아니오를 선택했습니다.", Toast.LENGTH_LONG).show();
                        dialog_check = false;
                    }
                });
        builder.setPositiveButton("예",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(context, "친구를 삭제하였습니다.", Toast.LENGTH_LONG).show();

                        mAuth = FirebaseAuth.getInstance();
                        FirebaseUser currentUser = mAuth.getCurrentUser();

                        final DatabaseReference removeRef = database.getReference("user")
                                .child(currentUser.getUid())
                                .child("friends")
                                .child(userInfoArrayList.get(position_final).name);
                        removeRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                removeRef.removeValue();


                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });



                        dialog_check = true;

                    }
                });

        builder.show();
        return dialog_check;
    }*/
}


