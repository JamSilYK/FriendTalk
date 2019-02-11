package com.example.lhw.myapplication3;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class SelectFriendActivity extends AppCompatActivity {

    final String TAG = "SelectFriendActivity";

    private FirebaseAuth mAuth;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference("user");

    RecyclerView selectRecyclerView;
    Button select_bt;

    ArrayList<User> friend_list;
    FirebaseUser currentUser;

    ChatModel chatModel = new ChatModel();
    String friend_uid ;

    String destinationRoom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_friend);
        mAuth = FirebaseAuth.getInstance();
        selectRecyclerView = findViewById(R.id.SelectRecyclerView);
        selectRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        currentUser = mAuth.getCurrentUser();
        friend_list = new ArrayList<>();


        myRef.child(currentUser.getUid()).child("friends").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()){
                    User friend_User = dataSnapshot1.getValue(User.class);
                    friend_list.add(friend_User);
                    Log.d(TAG, "friend_User: " + friend_User.toString());
                }
                selectRecyclerView.setAdapter(new SelectRecyclerViewAdapter());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        select_bt = findViewById(R.id.select_bt);
        select_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String myUid = currentUser.getUid();
                chatModel.users.put(myUid, true);
                FirebaseDatabase.getInstance().getReference().child("ChattingRoom").push().setValue(chatModel).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        FirebaseDatabase.getInstance().getReference().child("ChattingRoom").orderByChild("users/" + currentUser.getUid()).equalTo(true).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for(DataSnapshot item : dataSnapshot.getChildren()){
                                    ChatModel tmpchatModel = item.getValue(ChatModel.class);
                                    if(tmpchatModel.users.keySet().equals(chatModel.users.keySet()) && tmpchatModel.users.size() == chatModel.users.size()){
                                        destinationRoom = item.getKey();
                                        Log.d(TAG, "onDataChange: key" + destinationRoom);
                                    }
                                }

                                Intent intent = new Intent(getApplicationContext(), GroupChattingRoomActivity.class);
                                intent.putExtra("destinationRoom", destinationRoom);
                                startActivity(intent);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                });

            }
        });
    }

    private class SelectRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

        public SelectRecyclerViewAdapter() {

        }

        private class SelectViewHolder extends RecyclerView.ViewHolder {
            TextView user_name;
            ImageView iv_picture;
            CheckBox friendItem_checkbox;

            public SelectViewHolder(View view) {
                super(view);
                user_name = view.findViewById(R.id.user_name);
                iv_picture = view.findViewById(R.id.iv_picture);
                friendItem_checkbox = view.findViewById(R.id.friendItem_checkbox);
            }
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

            View v;
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friend_select, parent, false);


            return new SelectViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
            SelectViewHolder selectViewHolder = (SelectViewHolder)holder;

            selectViewHolder.user_name.setText(friend_list.get(position).name);
            Picasso.get().load(Uri.parse(friend_list.get(position).uri)).transform(new CircleTransform()).into(selectViewHolder.iv_picture);
            selectViewHolder.friendItem_checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    if(isChecked){
                        myRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for(DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()){
                                    User tmpUser = dataSnapshot1.getValue(User.class);
                                    if(tmpUser.email.equals(friend_list.get(position).email)){
                                        String friend_uid = dataSnapshot1.getKey();
                                        Log.d(TAG, "friend_uid: " + friend_uid);
                                        chatModel.users.put(friend_uid, true);
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }

                    else {
                        Log.d(TAG, "chatModel.users.remove(friend_list.get(position))1 : " + chatModel.users.toString());
                        chatModel.users.remove(friend_uid);
                        Log.d(TAG, "onCheckedChanged: " + chatModel.users.get(friend_uid));
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            Log.d(TAG, "friend_list.size(): " + friend_list.size());
            return friend_list.size();

        }
    }
}
