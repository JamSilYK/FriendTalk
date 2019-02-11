package com.example.lhw.myapplication3;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
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
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static android.content.Context.MODE_PRIVATE;
import static com.example.lhw.myapplication3.Chatting_List_Adapter.checked;
import static com.example.lhw.myapplication3.ClickMyProfileActivity.chatting_list_name;


public class ChattingFragment extends Fragment implements ChatOnitemClick {

    //태그
    final String TAG = "ChattingFragment";
    final int CHANGE_ROOM_NAME = 13;
    final int GROUP = 10;
    final int DUAL = 20;
    //태그

    //리싸이클러뷰 변수들
    RecyclerView mRecyclerView;
    RecyclerView.LayoutManager mLayoutManager;
    ArrayList<Chatting_imformation> chatting_list;
    //리싸이클러뷰 변수들

    //파이어베이스 변수들
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef;
    DatabaseReference MyChatRef;
    DatabaseReference YouChatRef;
    private FirebaseAuth mAuth;
    private StorageReference mStorageRef;
    //파이어베이스 변수들

    //로그인 사용자 정보
    String email;
    User user;
    User user2;
    //로그인 사용자 정보

    User item_user;
    User myuser;

    static String del_name = ",";




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_chatting, container, false);

        mRecyclerView = v.findViewById(R.id.mRecyclerView);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(new ChatRecyclerViewAdapter());


        return v;
    }

    class ChatRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

        private List<ChatModel> chatModels = new ArrayList<>();
        private List<String> keys = new ArrayList<>();
        private String uid;
        private ArrayList<String> destinationUsers = new ArrayList<>();
        User item_user;

        public ChatRecyclerViewAdapter() {
            uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

            FirebaseDatabase.getInstance().getReference().child("ChattingRoom").orderByChild("users/" + uid).equalTo(true).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    chatModels.clear();
                    for(DataSnapshot item : dataSnapshot.getChildren()){
                        chatModels.add(item.getValue(ChatModel.class));
                        keys.add(item.getKey());
                    }

                    notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

        public  class CustomViewHolder extends RecyclerView.ViewHolder {
            ImageView chat_pictrue; // 아이템 채팅 친구 프로필사진
            TextView last_contents; //아이템 마지막 채팅내용
            TextView text_dictionary; //아이템 채팅 제목
            CardView item_layout; //아이템 컨테이너
            TextView hour;

            public CustomViewHolder(View v) {
                super(v);
                chat_pictrue = v.findViewById(R.id.chat_pictrue);
                last_contents = v.findViewById(R.id.last_contents);
                text_dictionary = v.findViewById(R.id.text_dictionary);
                item_layout = v.findViewById(R.id.item_layout);
                hour = v.findViewById(R.id.hour);
            }
        }

        @Override
        public int getItemViewType(int position) {

            if(chatModels.get(position).users.size() <= 2){// 듀얼
                return DUAL;
            }

            else { //단체
                return GROUP;
            }

        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.chatting_item, parent, false);

            return new CustomViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {

            final CustomViewHolder customViewHolder = (CustomViewHolder)holder;
            String destinationUid = null;

            //채팅방에 있는 유저를 체크
            for(String user : chatModels.get(position).users.keySet()){
                if(!user.equals(uid)){
                    destinationUid = user;
                    destinationUsers.add(destinationUid);
                }
            }

            Log.d(TAG, "onBindViewHolder: destinationUid" + destinationUid);

            FirebaseDatabase.getInstance().getReference().child("user").child(destinationUid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    item_user = dataSnapshot.getValue(User.class);
                    Log.d(TAG, "ChattingFragement item_user: " + item_user.toString());

                    Picasso.get().load(Uri.parse(item_user.uri)).transform(new CircleTransform()).into(customViewHolder.chat_pictrue);

                    if(chatModels.get(position).users.size() > 2){
                        customViewHolder.text_dictionary.setText("단체 채팅방");
                    }

                    else {
                        customViewHolder.text_dictionary.setText(item_user.name);
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
            //메세지를 내림 차순으로 정렬 후 마지막 메세지의 키값을 가져옴
            Map<String, ChatModel.Comment> commentMap = new TreeMap<>(Collections.<String>reverseOrder());
            commentMap.putAll(chatModels.get(position).comments);
            if(commentMap.keySet().toArray().length > 0) {
                String lastMessagekey = (String) commentMap.keySet().toArray()[0];

                customViewHolder.last_contents.setText(chatModels.get(position).comments.get(lastMessagekey).message);
                if(chatModels.get(position).comments.get(lastMessagekey).message == null){
                    customViewHolder.last_contents.setText("사진");
                }
                customViewHolder.hour.setText(chatModels.get(position).comments.get(lastMessagekey).hour);

                final String finalDestinationUid = destinationUid;
                customViewHolder.item_layout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(chatModels.get(position).users.size() == 2){
                            final Intent intent = new Intent(v.getContext(), ChattingRoomActivity.class);
                            Log.d(TAG, "onClick: destinationUsers.get(position)" + finalDestinationUid);

                            database.getReference("user").child(finalDestinationUid).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    item_user = dataSnapshot.getValue(User.class);
                                    intent.putExtra("destinationUid", finalDestinationUid);
                                    intent.putExtra("item_user", item_user);
                                    intent.putExtra("pagecheck", 2);
                                    startActivity(intent);
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }

                        else {
                            customViewHolder.item_layout.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent intent = new Intent(v.getContext(), GroupChattingRoomActivity.class);
                                    intent.putExtra("destinationRoom", keys.get(position));
                                    for(int i = 0; i<keys.size(); i++) {
                                        Log.d(TAG, "keys: " + keys.get(i));
                                    }
                                    startActivity(intent);
                                }
                            });
                        }
                    }
                });
            }

            else {

            }

        }

        @Override
        public int getItemCount() {
            return chatModels.size();
        }

    }


    @Override
    public void onClick(View view, int position, ArrayList<Chatting_imformation> chatting_list) {

        Chatting_imformation ci = chatting_list.get(position);
        String item_email = ci.email;
        String item_uri = ci.uri;
        String item_name = ci.name;

        item_user = new User();

        item_user.email = item_email;
        item_user.uri = item_uri;
        item_user.name = item_name;


        Intent intent = new Intent(getActivity(), ChattingRoomActivity.class);

        intent.putExtra("myuser", myuser);
        intent.putExtra("item_user", item_user);

        startActivity(intent);

    }

    @Override
    public void onLongClickListener(View view, int position, ArrayList<Chatting_imformation> chatting_list) {
        DialogRadio(chatting_list, position);
    }

    private void DialogRadio(final ArrayList<Chatting_imformation> chatting_list, final int position){
        final CharSequence[] Selected = {"채팅방 이름 변경", "나가기"};
        AlertDialog.Builder alt_bld = new AlertDialog.Builder(getActivity());
        alt_bld.setIcon(R.drawable.chat_icon);
        alt_bld.setTitle(chatting_list.get(position).dictionary);
        alt_bld.setSingleChoiceItems(Selected, -1, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                Chatting_imformation ci = chatting_list.get(position);
                Log.d(TAG, "chatting_list.get(position).dictionary " + chatting_list.get(position).dictionary);
                if(item == 0){ //채팅방 이름 변경
                    Intent intent = new Intent(getActivity(), ChangeNameActivity.class);
                    intent.putExtra("chatting_imformation", ci);
                    intent.putExtra("position", position);
                    startActivityForResult(intent, CHANGE_ROOM_NAME);
                }

                else if(item == 1){// 채팅방 나가기
                    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                    del_name = chatting_list.get(position).name;
                    Log.d(TAG, "del_name: " + del_name);
                    chatting_list.remove(position);
                    mRecyclerView.getAdapter().notifyDataSetChanged();

                }

                Toast.makeText(getActivity(), "Phone Model = "+Selected[item] + item, Toast.LENGTH_SHORT).show();

                dialog.cancel();
            }
        });
        AlertDialog alert = alt_bld.create();
        alert.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CHANGE_ROOM_NAME && data != null){
            final String changed_name = data.getExtras().getString("changed_name");
            final int position = data.getExtras().getInt("position");
            chatting_list.get(position).dictionary = changed_name;
            checked++;
            Log.d(TAG, "checked " + checked);
            mRecyclerView.getAdapter().notifyDataSetChanged();

        }
    }

}
