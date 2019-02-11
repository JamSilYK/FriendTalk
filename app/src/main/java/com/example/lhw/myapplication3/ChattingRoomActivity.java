package com.example.lhw.myapplication3;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class ChattingRoomActivity extends AppCompatActivity {

    final String TAG = "activity_chatting_room";
    final int REQUEST_ACT_SEND = 873;
    final int right = 1;
    final int left = 2;
    final int image_left = 3;
    final int image_right = 4;

    int backpresscheck = 0;

    /////////////////////////파이어베이스///////////////////////////
    private FirebaseAuth mAuth;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    FirebaseUser currentUser;

    /////////////////////////파이어베이스///////////////////////////

    /////////////////////////View/////////////////////
    TextView chat_contents;
    Button bt_send;
    Button bt_back;
    Button bt_send_photo;
    RecyclerView chat_recyclerview;
    RecyclerView.LayoutManager chatLayoutManager;
    /////////////////////////View/////////////////////

    ArrayList<Chat> chat_list; //여태 채팅한 내용들
    String myemail; // 현재 유저의 이메일

    User item_user; //채팅방 친구 정보
    User myuser; //로그인한 유저

    ArrayList<User> chat_members; // 채팅하는 유저들


    ChatModel chatModel;

    String uid;
    String chatRoomUid;

    String destinationUid;

    List<ChatModel.Comment> comments;

    private DatabaseReference databaseReference;
    private ValueEventListener valueEventListener;
    int peopleCount = 0; //채팅방 사람들 count
    DatabaseReference ChatRef;

    String key;



    int pagecheck = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatting_room);

        MyFirebaseInstanceIDService.read_check = 0;

        ///////////////////////////////////자신의 유저 정보와 클릭한 유저의 정보 세트/////////////////////////////////////////
        pagecheck = getIntent().getExtras().getInt("pagecheck");
        item_user = (User) getIntent().getExtras().getSerializable("item_user"); // 이 채팅방 친구 정보
        destinationUid = getIntent().getExtras().getString("destinationUid");
        Log.d(TAG, "onCreate: " + destinationUid);
        Log.d(TAG, "ClickFriendsProfileItem_user: " + item_user.toString());

        /*Log.d(TAG, "ClickFriendsProfilemyuser: " + myuser.toString());*/
        ///////////////////////////////////자신의 유저 정보와 클릭한 유저의 정보 세트/////////////////////////////////////////

        /////////////////////////////////파이어베이스변수세팅
        database = FirebaseDatabase.getInstance();
        DatabaseReference MyRef = database.getReference("user");
        ChatRef = database.getReference("ChattingRoom");
        mAuth = FirebaseAuth.getInstance();
        ////////////////////////////////파이어베이스변수세팅

        //////////////////////////////////View ID 세팅///////////////////////////////////
        TextView friend_name = findViewById(R.id.friend_name); //채팅 아이디값
        chat_contents = findViewById(R.id.chat_contents); //채팅 보내는 내용
        bt_send = findViewById(R.id.bt_changed_send); //SEND 버튼
        bt_send_photo = findViewById(R.id.bt_send_photo);
        //////////////////////////////////View ID 세팅///////////////////////////////////

        /////////////////////////////////리싸이클러뷰 세팅///////////////////////////////
        chatLayoutManager = new LinearLayoutManager(this);
        chat_recyclerview = findViewById(R.id.chat_recyclerview);

        /////////////////////////////////리싸이클러뷰 세팅///////////////////////////////
        comments = new ArrayList<>();

        currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            uid = currentUser.getUid();
        }

        FirebaseDatabase.getInstance().getReference().child("user").child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                myuser = dataSnapshot.getValue(User.class);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        FirebaseDatabase.getInstance().getReference().child("user").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()){
                    User tmp_user = dataSnapshot1.getValue(User.class);
                    if(tmp_user.email.equals(item_user)){
                        item_user = tmp_user;
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        bt_send_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, REQUEST_ACT_SEND);
            }
        });

        /*final ChattingRoomAdapter chattingRoomAdapter = new ChattingRoomAdapter(getApplicationContext(), chat_list, chat_members);
        chat_recyclerview.setAdapter(chattingRoomAdapter);*/

        checkChatRoom();

        bt_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chatModel = new ChatModel();
                chatModel.users.put(uid, true);
                chatModel.users.put(destinationUid, true);


                if (chatRoomUid == null) {
                    bt_send.setEnabled(false);
                    ChatRef.push().setValue(chatModel).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            checkChatRoom();
                            bt_send.setEnabled(true);
                            ChatModel.Comment comment = new ChatModel.Comment();
                            Calendar calendar = Calendar.getInstance();
                            SimpleDateFormat df = new SimpleDateFormat("hh:mm a");
                            String formattedhour = df.format(calendar.getTime());
                            comment.uid = uid;
                            comment.message = chat_contents.getText().toString();
                            comment.hour = formattedhour;
                            ChatRef.child(chatRoomUid).child("comments").push().setValue(comment);
                            sendGcm();
                            chat_contents.setText("");
                            chat_recyclerview.getAdapter().notifyDataSetChanged();
                        }
                    });
                } else {
                    Calendar calendar = Calendar.getInstance();
                    SimpleDateFormat df = new SimpleDateFormat("hh:mm a");
                    String formattedhour = df.format(calendar.getTime());

                    ChatModel.Comment comment = new ChatModel.Comment();
                    comment.uid = uid;
                    comment.message = chat_contents.getText().toString();
                    comment.hour = formattedhour;
                    ChatRef.child(chatRoomUid).child("comments").push().setValue(comment);
                    sendGcm();
                    chat_contents.setText("");
                }


            }
        });
    }

    void sendGcm(){
        Gson gson = new Gson();

        NotificationModel notificationModel = new NotificationModel();
        notificationModel.to = item_user.pushToken;
        notificationModel.notification.title = myuser.name;
        notificationModel.notification.text = chat_contents.getText().toString();
        notificationModel.data.title = myuser.name;
        notificationModel.data.text = chat_contents.getText().toString();

        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf8"), gson.toJson(notificationModel));
        Request request = new Request.Builder().header("Content-Type", "application/json").addHeader("Authorization","key=AIzaSyBbIcUh7c3KYKgfZ0dMeqJv2RPntD1IsT4")
                .url("https://gcm-http.googleapis.com/gcm/send").post(requestBody).build();
        OkHttpClient okHttpClient = new OkHttpClient();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(TAG, "onResponse: " + "실패");
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.d(TAG, "onResponse: " + "성공");
            }
        });

    }

    void checkChatRoom() {
        FirebaseDatabase.getInstance().getReference().child("ChattingRoom").orderByChild("users/" + uid).equalTo(true).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot item : dataSnapshot.getChildren()) {
                    ChatModel chatModel = item.getValue(ChatModel.class);
                    if (chatModel.users.containsKey(destinationUid) && chatModel.users.size() == 2) {
                        chatRoomUid = item.getKey();
                        Log.d(TAG, "chatRoomUid: " + chatRoomUid);
                    }
                }

                if (chatRoomUid != null) {
                    chat_recyclerview.setHasFixedSize(true);
                    chat_recyclerview.setLayoutManager(chatLayoutManager);
                    chat_recyclerview.setAdapter(new RecyclerViewAdapter());
                } else {

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        public RecyclerViewAdapter() {

            databaseReference = FirebaseDatabase.getInstance().getReference().child("ChattingRoom").child(chatRoomUid).child("comments");
            valueEventListener = databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    comments.clear();
                    Map<String, Object> readUsersMap = new HashMap<>();


                    for (DataSnapshot item : dataSnapshot.getChildren()) {
                        String key = item.getKey();
                        ChatModel.Comment comment_origin = item.getValue(ChatModel.Comment.class);
                        ChatModel.Comment comment_motify = item.getValue(ChatModel.Comment.class);
                        if(MyFirebaseInstanceIDService.read_check == 0){
                            comment_motify.readUsers.put(uid, true);
                        }

                        if(comment_motify.message == null && comment_motify.photo_uri == null){

                        }

                        else {
                            readUsersMap.put(key, comment_motify);
                            comments.add(comment_origin);
                        }

                    }

                    if (comments.size() > 0) {
                        if (!comments.get(comments.size() - 1).readUsers.containsKey(uid) && MyFirebaseInstanceIDService.read_check == 0) {
                            FirebaseDatabase.getInstance().getReference().child("ChattingRoom").child(chatRoomUid).child("comments").updateChildren(readUsersMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    /*notifyDataSetChanged();*/
                                    chat_recyclerview.scrollToPosition(comments.size() - 1);
                                }
                            });
                        } else {
                            /*notifyDataSetChanged();*/
                            Log.d(TAG, "chat_recyclerview.scrollToPosition ");
                            chat_recyclerview.scrollToPosition(comments.size() - 1);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        }

        private class MessageViewHolder extends RecyclerView.ViewHolder {
            TextView chat_item_contents;
            ImageView chat_profile;
            ImageView chat_photo;
            TextView time;
            TextView read_check_text;
            TextView friend_name;

            public MessageViewHolder(View view) {
                super(view);
                chat_item_contents = view.findViewById(R.id.mTextView);
                chat_profile = view.findViewById(R.id.my_comment_profile);
                time = view.findViewById(R.id.time);
                read_check_text = view.findViewById(R.id.read_check_text);
                friend_name = view.findViewById(R.id.friend_name);
                chat_photo = view.findViewById(R.id.chat_photo);

            }
        }

        void setReadCounter(final int position, final TextView textView) {
            if (peopleCount == 0) {


                FirebaseDatabase.getInstance().getReference().child("ChattingRoom").child(chatRoomUid).child("users").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Map<String, Boolean> users = (Map<String, Boolean>) dataSnapshot.getValue();
                        peopleCount = users.size();

                        int count = peopleCount - comments.get(position).readUsers.size();

                        if (count > 0) {
                            textView.setText(String.valueOf(count));
                            textView.setVisibility(View.VISIBLE);
                        } else {
                            textView.setText(String.valueOf(count));
                            textView.setVisibility(View.INVISIBLE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            } else {
                int count = peopleCount - comments.get(position).readUsers.size();

                if (count > 0) {
                    textView.setText(String.valueOf(count));
                    textView.setVisibility(View.VISIBLE);
                } else {
                    textView.setText(String.valueOf(count));
                    textView.setVisibility(View.INVISIBLE);
                }
            }
        }

        @Override
        public int getItemViewType(int position) {

            if (comments.get(position).uid.equals(uid) && comments.get(position).photo_uri == null) {
                return right;
            }

            else if(comments.get(position).uid.equals(uid) == false && comments.get(position).photo_uri == null){
                return left;
            }

            else if(comments.get(position).uid.equals(uid) && comments.get(position).photo_uri != null){
                return image_right;
            }

            else {
                return image_left;
            }

        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

            View v = null;
            if (viewType == right) {
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.right_chat_item, parent, false);
            }

            else if(viewType == left){
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.left_chat_item, parent, false);
            }

            else if(viewType == image_right){
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.right_chat_image_item, parent, false);
            }

            else if(viewType == image_left){
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.left_chat_image_item, parent, false);
            }



            return new MessageViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, final int position) {
            final MessageViewHolder messageViewHolder = ((MessageViewHolder) viewHolder);

            int viewtype = viewHolder.getItemViewType();
            MessageViewHolder mymessageviewholder = (MessageViewHolder) viewHolder;

            if (viewtype == right) {
                messageViewHolder.chat_item_contents.setText(comments.get(position).message);
                messageViewHolder.time.setText(comments.get(position).hour);
                setReadCounter(position, messageViewHolder.read_check_text);
            }

            else if(viewtype == left){
                messageViewHolder.chat_item_contents.setText(comments.get(position).message);
                messageViewHolder.time.setText(comments.get(position).hour);
                Picasso.get().load(Uri.parse(item_user.uri)).transform(new CircleTransform()).into(mymessageviewholder.chat_profile);
                setReadCounter(position, messageViewHolder.read_check_text);
                messageViewHolder.friend_name.setText(item_user.name);
            }

            else if(viewtype == image_right){
                Picasso.get().load(Uri.parse(comments.get(position).photo_uri)).centerInside().fit().into(messageViewHolder.chat_photo);
                messageViewHolder.time.setText(comments.get(position).hour);
                setReadCounter(position, messageViewHolder.read_check_text);
                messageViewHolder.chat_photo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getApplicationContext(), ChattingClickImgActivity.class);
                        intent.putExtra("comments", comments.get(position));
                        startActivity(intent);
                    }
                });
            }

            else if(viewtype == image_left){
                Picasso.get().load(Uri.parse(comments.get(position).photo_uri)).centerInside().fit().into(messageViewHolder.chat_photo);
                Picasso.get().load(Uri.parse(item_user.uri)).transform(new CircleTransform()).into(mymessageviewholder.chat_profile);
                messageViewHolder.time.setText(comments.get(position).hour);
                setReadCounter(position, messageViewHolder.read_check_text);
                messageViewHolder.friend_name.setText(item_user.name);
                messageViewHolder.chat_photo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getApplicationContext(), ChattingClickImgActivity.class);
                        intent.putExtra("comments", comments.get(position));
                        startActivity(intent);
                    }
                });
            }


        }

        @Override
        public int getItemCount() {
            return comments.size();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_ACT_SEND && data != null) { //메세지를 보냈을 때
            Uri image = data.getData();
            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), image);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                // 1. 데이터베이스에 사진 정보를 제외하고 정보를 업로드
                // 2. storage에 사진을 저장하고 URI를 가지고온 후
                // 3. 기존 정보에 URI를 업로드한다.

                Calendar calendar = Calendar.getInstance();
                SimpleDateFormat df = new SimpleDateFormat("hh:mm a");
                final String formattedhour = df.format(calendar.getTime());

                chatModel = new ChatModel();
                chatModel.users.put(uid, true);
                chatModel.users.put(destinationUid, true);
                final ChatModel.Comment comment = new ChatModel.Comment();

                if (chatRoomUid == null) { //처음 메세지를 보낼때
                    bt_send.setEnabled(false);
                    final Bitmap finalBitmap = bitmap;
                    ChatRef.push().setValue(chatModel).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            checkChatRoom();
                            bt_send.setEnabled(true);
                            comment.uid = uid;
                            comment.hour = formattedhour;
                            Log.d(TAG, "chatRoomUid 1: " + chatRoomUid);
                            ChatRef.child(chatRoomUid).child("comments").push().setValue(comment).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    ChatRef.child(chatRoomUid).child("comments").addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                                                if (dataSnapshot1.hasChild("photo_uri") == false) {
                                                    Log.d(TAG, "onDataChangekey1: ");
                                                    ChatModel.Comment tmp_comment = dataSnapshot1.getValue(ChatModel.Comment.class);
                                                    if (tmp_comment.hour.equals(comment.hour) && tmp_comment.uid.equals(comment.uid)) {
                                                        key = dataSnapshot1.getKey();
                                                        Log.d(TAG, "onDataChangekey2: " + key);
                                                    }
                                                }
                                            }

                                            if(key != null){
                                                Log.d(TAG, "key != null: " + "들어왔음");
                                                final StorageReference mStorageRef = FirebaseStorage.getInstance().getReference();
                                                StorageReference mountainsRef = mStorageRef.child("chattingRoom").child(key+".jpg");

                                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                                finalBitmap.compress(Bitmap.CompressFormat.JPEG, 30, baos);
                                                byte[] data = baos.toByteArray();

                                                UploadTask uploadTask = mountainsRef.putBytes(data);
                                                uploadTask.addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception exception) {
                                                        // Handle unsuccessful uploads
                                                    }
                                                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                                    @Override
                                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) { //사진이 업로드 됐을때
                                                        mStorageRef.child("chattingRoom/"+key+".jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                            @Override
                                                            public void onSuccess(Uri uri) {
                                                                // Got the download URL for 'users/me/profile.png'
                                                                Log.d(TAG, "mStorageRef onSuccess: ");
                                                                Log.d(TAG, "mStorageRef onSuccess: " + uri.toString());
                                                                String st_uri = uri.toString();
                                                                comment.photo_uri = st_uri;
                                                                ChatRef.child(chatRoomUid).child("comments").child(key).setValue(comment);
                                                                chat_recyclerview.getAdapter().notifyDataSetChanged();
                                                            }
                                                        }).addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception exception) {
                                                                // Handle any errors
                                                            }
                                                        });
                                                    }
                                                });


                                            }
                                        }
                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                                }
                            });

                        }
                    });
                } else { // 이미 메세지방이 있을때
                    comment.uid = uid;
                    comment.hour = formattedhour;

                    final Bitmap finalBitmap1 = bitmap;
                    ChatRef.child(chatRoomUid).child("comments").push().setValue(comment).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            ChatRef.child(chatRoomUid).child("comments").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                                        if (dataSnapshot1.hasChild("photo_uri") == false) {
                                            ChatModel.Comment tmp_comment = dataSnapshot1.getValue(ChatModel.Comment.class);
                                            if (tmp_comment.hour.equals(comment.hour) && tmp_comment.uid.equals(comment.uid)) {
                                                key = dataSnapshot1.getKey();
                                            }
                                        }
                                    }

                                    if(key != null){
                                        final StorageReference mStorageRef = FirebaseStorage.getInstance().getReference();
                                        StorageReference mountainsRef = mStorageRef.child("chattingRoom").child(key+".jpg");

                                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                        finalBitmap1.compress(Bitmap.CompressFormat.JPEG, 50, baos);
                                        byte[] data = baos.toByteArray();

                                        UploadTask uploadTask = mountainsRef.putBytes(data);
                                        uploadTask.addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception exception) {
                                                // Handle unsuccessful uploads
                                            }
                                        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                            @Override
                                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                mStorageRef.child("chattingRoom/"+key+".jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                    @Override
                                                    public void onSuccess(Uri uri) {
                                                        // Got the download URL for 'users/me/profile.png'
                                                        String st_uri = uri.toString();
                                                        comment.photo_uri = st_uri;
                                                        ChatRef.child(chatRoomUid).child("comments").child(key).setValue(comment);

                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception exception) {
                                                        // Handle any errors
                                                    }
                                                });
                                            }
                                        });
                                    }
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }
                    });

                }
                Log.d(TAG, "chatRoomUid 2: " + chatRoomUid);
            }

        }

    }

    @Override
    public void onBackPressed() {

        if (valueEventListener != null) {
            databaseReference.removeEventListener(valueEventListener);
            MyFirebaseInstanceIDService.read_check++;

            if(pagecheck == 2){
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.putExtra("check", "chat");
                startActivity(intent);
            }

            else {

            }
        }
        backpresscheck++;
        finish();
    }
}



