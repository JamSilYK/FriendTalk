package com.example.lhw.myapplication3;

import android.annotation.SuppressLint;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class GroupChattingRoomActivity extends AppCompatActivity {

    final String TAG = "GroupChattingRoomAct";
    final int right = 1;
    final int left = 2;
    Map<String, User> users = new HashMap<>();
    String destinationRoom;
    String uid;
    String email;
    EditText editText;

    private DatabaseReference databaseReference;
    private ValueEventListener valueEventListener;

    List<ChatModel.Comment> comments = new ArrayList<>();

    private RecyclerView recyclerView;

    int peopleCount = 0;

    int int_read_check = 0;

    ArrayList<User> item_user_list;
    User myuser;

    int send_int = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chatting_room);

        MyFirebaseInstanceIDService.read_check = 0;


        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        email =  FirebaseAuth.getInstance().getCurrentUser().getEmail();
        int_read_check = 1;


        destinationRoom = getIntent().getStringExtra("destinationRoom");
        editText = findViewById(R.id.chat_contents);

        FirebaseDatabase.getInstance().getReference().child("user").addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("LongLogTag")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                item_user_list = new ArrayList();
                for(DataSnapshot item : dataSnapshot.getChildren()){
                    users.put(item.getKey(), item.getValue(User.class));
                    User tmp_user = item.getValue(User.class);
                    if(tmp_user.email.equals(email)){
                        myuser = tmp_user;
                    }

                    else {
                        item_user_list.add(tmp_user);
                    }
                }
                init();

                recyclerView = findViewById(R.id.chat_recyclerview);
                recyclerView.setAdapter(new GroupMessageRecyclerViewAdapter());
                recyclerView.setLayoutManager(new LinearLayoutManager(GroupChattingRoomActivity.this));
                Log.d(TAG, "users.size(): " + users.size());


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    void init(){
        Button button = (Button) findViewById(R.id.bt_changed_send);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChatModel.Comment comment = new ChatModel.Comment();

                Calendar calendar = Calendar.getInstance();
                SimpleDateFormat df = new SimpleDateFormat("hh:mm a");
                String formattedhour = df.format(calendar.getTime());

                comment.uid = uid;
                comment.message = editText.getText().toString();
                comment.hour = formattedhour;
                FirebaseDatabase.getInstance().getReference().child("ChattingRoom").child(destinationRoom).child("comments").push().setValue(comment).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Log.d(TAG, "onComplete: item_user_list.size()" + item_user_list.size());
                        for(int i = 0; i<item_user_list.size(); i++) {
                            sendGcm(myuser, item_user_list.get(i));
                        }
                        editText.setText("");
                    }
                });
            }
        });
    }

    void sendGcm(User myuser, User item_user){
        Gson gson = new Gson();
        send_int++;
        Log.d(TAG, "sendGcm: " + send_int);

        NotificationModel notificationModel = new NotificationModel();
        notificationModel.to = item_user.pushToken;
        notificationModel.notification.title = myuser.name;
        notificationModel.notification.text = editText.getText().toString();
        notificationModel.data.title = myuser.name;
        notificationModel.data.text = editText.getText().toString();

        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf8"), gson.toJson(notificationModel));
        Request request = new Request.Builder().header("Content-Type", "application/json").addHeader("Authorization","key=AIzaSyBbIcUh7c3KYKgfZ0dMeqJv2RPntD1IsT4")
                .url("https://gcm-http.googleapis.com/gcm/send").post(requestBody).build();
        OkHttpClient okHttpClient = new OkHttpClient();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {

            }
        });

    }

    class GroupMessageRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

        public GroupMessageRecyclerViewAdapter() {

            databaseReference = FirebaseDatabase.getInstance().getReference().child("ChattingRoom").child(destinationRoom).child("comments");
            valueEventListener = databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    comments.clear();
                    final Map<String, Object> readUsersMap = new HashMap<>();
                    ChatModel.Comment comment_motif;

                    ChatModel.Comment comment_motify = null;
                    for (DataSnapshot item : dataSnapshot.getChildren()) {
                        String key = item.getKey();
                        ChatModel.Comment comment_origin = item.getValue(ChatModel.Comment.class);
                        comment_motify = item.getValue(ChatModel.Comment.class);
                        if(int_read_check != 3){
                            comment_motify.readUsers.put(uid, true);
                            readUsersMap.put(key, comment_motify);
                            comments.add(comment_origin);
                        }
                    }




                    if (comments.size() > 0) {
                        if (!comments.get(comments.size() - 1).readUsers.containsKey(uid) && int_read_check != 3) {
                            Set set = comment_motify.readUsers.keySet();
                            Iterator iterator = set.iterator();
                            final ArrayList<User> readuser_list = new ArrayList<>();
                            final ArrayList<String> readuser_uid_list = new ArrayList<>();
                            while(iterator.hasNext()){
                                final String key = (String)iterator.next();
                                Log.d(TAG, "onDataChangekey: " + key);
                                readuser_uid_list.add(key);
                            }

                            FirebaseDatabase.getInstance().getReference().child("ChattingRoom").child(destinationRoom).child("comments").updateChildren(readUsersMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    notifyDataSetChanged();
                                    recyclerView.scrollToPosition(comments.size() - 1);
                                }
                            });
                        } else {
                            notifyDataSetChanged();
                            recyclerView.scrollToPosition(comments.size() - 1);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        }

        @Override
        public int getItemViewType(int position) {
            if(comments.get(position).uid.equals(uid)){
                return right;
            }
            else {
                return left;
            }
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v;
            if(viewType == right){
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.right_chat_item, parent, false);
            }

            else {
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.left_chat_item, parent, false);
            }

            return new GroupMessageViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
            final GroupMessageViewHolder messageViewHolder = ((GroupMessageViewHolder)holder);

            int viewtype = holder.getItemViewType();
            final GroupMessageViewHolder mymessageviewholder = (GroupMessageViewHolder)holder;

            if(viewtype == right){
                messageViewHolder.chat_item_contents.setText(comments.get(position).message);
                messageViewHolder.time.setText(comments.get(position).hour);
                setReadCounter(position, messageViewHolder.read_check_text);
            }

            else {
                FirebaseDatabase.getInstance().getReference("user").child(comments.get(position).uid).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        User item_user = dataSnapshot.getValue(User.class);
                        messageViewHolder.chat_item_contents.setText(comments.get(position).message);
                        messageViewHolder.time.setText(comments.get(position).hour);
                        messageViewHolder.friend_name.setText(item_user.name);
                        Picasso.get().load(Uri.parse(users.get(comments.get(position).uid).uri)).transform(new CircleTransform()).into(mymessageviewholder.chat_profile);
                        setReadCounter(position, messageViewHolder.read_check_text);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }

        }

        void setReadCounter(final int position, final TextView textView){
            if(peopleCount == 0) {
                FirebaseDatabase.getInstance().getReference().child("ChattingRoom").child(destinationRoom).child("users").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Map<String, Boolean> users = (Map<String, Boolean>) dataSnapshot.getValue();
                        peopleCount = users.size();

                        int count = peopleCount - comments.get(position).readUsers.size();

                        if (count > 0) {
                            textView.setText(String.valueOf(count));
                            textView.setVisibility(View.VISIBLE);
                        }

                        else {
                            textView.setText(String.valueOf(count));
                            textView.setVisibility(View.INVISIBLE);
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            else {
                int count = peopleCount - comments.get(position).readUsers.size();

                if (count > 0) {
                    textView.setText(String.valueOf(count));
                    textView.setVisibility(View.VISIBLE);
                }

                else {
                    textView.setText(String.valueOf(count));
                    textView.setVisibility(View.INVISIBLE);
                }
            }
        }

        @Override
        public int getItemCount() {
            return comments.size();
        }

        private class GroupMessageViewHolder extends RecyclerView.ViewHolder {
            TextView chat_item_contents;
            ImageView chat_profile;
            TextView time;
            TextView read_check_text;
            TextView friend_name;

            public GroupMessageViewHolder(View view) {
                super(view);
                chat_item_contents = view.findViewById(R.id.mTextView);
                chat_profile = view.findViewById(R.id.my_comment_profile);
                time = view.findViewById(R.id.time);
                read_check_text = view.findViewById(R.id.read_check_text);
                friend_name = view.findViewById(R.id.friend_name);

            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        int_read_check = 3;
        MyFirebaseInstanceIDService.read_check++;
        finish();
    }
}
