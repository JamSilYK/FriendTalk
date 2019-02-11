package com.example.lhw.myapplication3;

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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class SNSCommentActivity extends AppCompatActivity {

    final String TAG = "SNSCommentActivity";
    final int MY = 88;
    final int OTHER = 99;

    ImageView profile;
    TextView name;
    TextView time;
    TextView contents_text;
    ImageView contents_uri;
    RecyclerView mRecyclerView;
    ImageView my_comment_profile;
    TextView comment_text;
    Button bt_send;

    List<SNSVo.SNSComment> SNScomments;
    User user;

    DatabaseReference SNSRef;
    DatabaseReference MyRef;
    DatabaseReference FriendRef;
    FirebaseUser currentUser;

    SNSVo comment_snsvo;

    String getkey;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_snscomment);

        SNScomments = new ArrayList<>();

        comment_snsvo = (SNSVo) getIntent().getExtras().getSerializable("comment_snsvo");//클릭한 snsvo의 정보


        /////////////////////////////////////////////뷰 아이디 세팅///////////////////////////////////
        profile = findViewById(R.id.profile);
        name = findViewById(R.id.name);
        time = findViewById(R.id.time);
        contents_text = findViewById(R.id.contents_text);
        contents_uri = findViewById(R.id.contents_uri);
        mRecyclerView = findViewById(R.id.mRecyclerView);
        my_comment_profile = findViewById(R.id.my_comment_profile);
        comment_text = findViewById(R.id.comment_text);
        bt_send = findViewById(R.id.bt_send);
        /////////////////////////////////////////////뷰 아이디 세팅///////////////////////////////////
        Picasso.get().load(Uri.parse(comment_snsvo.uri)).transform(new CircleTransform()).into(profile);
        name.setText(comment_snsvo.name);
        time.setText(comment_snsvo.date);
        contents_text.setText(comment_snsvo.contents_text);



        if(comment_snsvo.contents_uri!=null){
            Uri contents_real_uri = Uri.parse(comment_snsvo.contents_uri); //컨텐츠 유알아이 파싱
            Picasso.get().load(contents_real_uri).resize(700, 500).into(contents_uri);
        }

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        SNSRef = database.getReference("SNS");

        SNSRef.child(comment_snsvo.checkd_date).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                SNScomments.clear();
                if(dataSnapshot.hasChild("comments")){
                    dataSnapshot = dataSnapshot.child("comments");
                    for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()){
                        SNSVo.SNSComment tmp_SNSComment = dataSnapshot1.getValue(SNSVo.SNSComment.class);
                        SNScomments.add(tmp_SNSComment);
                    }
                }
                mRecyclerView.setLayoutManager(new LinearLayoutManager(SNSCommentActivity.this));
                mRecyclerView.setHasFixedSize(true);
                mRecyclerView.setAdapter(new SNSCommentsAdapter());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        MyRef = database.getReference("user").child(currentUser.getUid());
        MyRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User myuser = dataSnapshot.getValue(User.class);
                Picasso.get().load(Uri.parse(myuser.uri)).transform(new CircleTransform()).into(my_comment_profile);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



        bt_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(comment_text.getText().toString() != null && comment_text.getText().toString().equals("") == false){

                    Calendar calendar = Calendar.getInstance();
                    SimpleDateFormat df = new SimpleDateFormat("hh:mm a");
                    String date = df.format(calendar.getTime());

                    currentUser = FirebaseAuth.getInstance().getCurrentUser();

                    SNSVo.SNSComment SNS_Comment = new SNSVo.SNSComment();
                    SNS_Comment.comment = comment_text.getText().toString();
                    SNS_Comment.date = date;
                    SNS_Comment.uid = currentUser.getUid();

                    Calendar calendar2 = Calendar.getInstance();
                    SimpleDateFormat transFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String simpledate = transFormat.format(calendar.getTime());

                    bt_send.setEnabled(false);
                    SNSRef.child(comment_snsvo.checkd_date).child("comments").child(simpledate).setValue(SNS_Comment).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            bt_send.setEnabled(true);
                            comment_text.setText("");
                        }
                    });
                }
            }
        });

    }

    class SNSCommentsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

        class SNSCommentsHolder extends RecyclerView.ViewHolder {
            TextView friend_name;
            ImageView comment_profile;
            TextView time;
            TextView comment_comment;
            Button bt_comment_del;

            public SNSCommentsHolder(View view) {
                super(view);
                friend_name = view.findViewById(R.id.friend_name);
                comment_profile = view.findViewById(R.id.my_comment_profile);
                time = view.findViewById(R.id.time);
                comment_comment = view.findViewById(R.id.comment_comment);
                bt_comment_del = view.findViewById(R.id.bt_comment_del);
            }
        }

        @Override
        public int getItemViewType(int position) {
            currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if(SNScomments.get(position).uid.equals(currentUser.getUid())){
                return MY;
            }

            else {
                return OTHER;
            }

        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.sns_comment_item, parent, false);
            return new SNSCommentsHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
            final SNSCommentsHolder snsCommentsHolder = ((SNSCommentsHolder)holder);
            int viewtype = holder.getItemViewType();

            if(viewtype == MY){
                snsCommentsHolder.bt_comment_del.setVisibility(View.VISIBLE);
                snsCommentsHolder.bt_comment_del.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        SNSRef.child(comment_snsvo.checkd_date).child("comments").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for(DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()){
                                    SNSVo.SNSComment tmp_comment = dataSnapshot1.getValue(SNSVo.SNSComment.class);
                                    if(tmp_comment.date.equals(SNScomments.get(position).date) && tmp_comment.uid.equals(SNScomments.get(position).uid) && tmp_comment.comment.equals(SNScomments.get(position).comment)){
                                        getkey = dataSnapshot1.getKey();
                                        Log.d(TAG, " String getkey: " + getkey);

                                    }
                                }
                                snsCommentsHolder.bt_comment_del.setEnabled(false);
                                bt_send.setEnabled(false);
                                SNSRef.child(comment_snsvo.checkd_date).child("comments").child(getkey).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        Toast.makeText(SNSCommentActivity.this, "댓글이 정상적으로 삭제되었습니다.", Toast.LENGTH_SHORT).show();
                                        snsCommentsHolder.bt_comment_del.setEnabled(true);
                                        bt_send.setEnabled(true);
                                    }
                                });


                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                });
            }
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            FriendRef = database.getReference("user").child(SNScomments.get(position).uid);
            FriendRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    user = dataSnapshot.getValue(User.class);

                    Picasso.get().load(Uri.parse(user.uri)).transform(new CircleTransform()).into(snsCommentsHolder.comment_profile);
                    snsCommentsHolder.friend_name.setText(user.name);
                    snsCommentsHolder.time.setText(SNScomments.get(position).date);
                    snsCommentsHolder.comment_comment.setText(SNScomments.get(position).comment);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
          
        }

        @Override
        public int getItemCount() {
            return SNScomments.size();
        }
    }

}
