package com.example.lhw.myapplication3;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class Chatting_List_Adapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    Context context;
    private ArrayList<Chatting_imformation> chatting_list;
    ChatOnitemClick mCallback;

    String changename;
    String changed_name;

    static int checked = 0;

    public Chatting_List_Adapter(Context context, ArrayList<Chatting_imformation> chatting_list, ChatOnitemClick mCallback) {
        this.chatting_list = chatting_list;
        this.context = context;
        this.mCallback = mCallback;
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder { //MyViewHolder 클래스
        ImageView chat_pictrue; // 아이템 채팅 친구 프로필사진
        TextView text_email; //아이템 마지막 채팅내용
        TextView text_dictionary; //아이템 채팅 제목
        CardView item_layout; //아이템 컨테이너
        TextView hour;



        MyViewHolder(View view) { //3번째로 실행
            super(view);
            chat_pictrue = view.findViewById(R.id.chat_pictrue);
            text_email = view.findViewById(R.id.last_contents);
            text_dictionary = view.findViewById(R.id.text_dictionary);
            item_layout = view.findViewById(R.id.item_layout);
            hour = view.findViewById(R.id.hour);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.chatting_item, parent, false);

        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        final  MyViewHolder myViewHolder = (MyViewHolder) holder;

        if(checked > 0) {
            SharedPreferences pref = context.getSharedPreferences("pref", Context.MODE_PRIVATE);
            changename = pref.getString("changename", ""); //key, value(defaults)
            changed_name = pref.getString("changed_name", "");

        }

        Uri realuril = Uri.parse(chatting_list.get(position).uri);
        Picasso.get().load(realuril).transform(new CircleTransform()).into(myViewHolder.chat_pictrue);

        if(chatting_list.get(position).dictionary.equals("") || chatting_list.get(position).dictionary == null){
            myViewHolder.text_dictionary.setText(chatting_list.get(position).name);
            chatting_list.get(position).dictionary = chatting_list.get(position).name;
        }

        else {
            myViewHolder.text_dictionary.setText(chatting_list.get(position).dictionary);
        }

        if(chatting_list.get(position).dictionary.equals(changename)){
            myViewHolder.text_dictionary.setText(changed_name);
        }

        myViewHolder.hour.setText(chatting_list.get(position).hour);
        myViewHolder.text_email.setText(chatting_list.get(position).contents);



        /*SharedPreferences pref = context.getSharedPreferences("pref", Context.MODE_PRIVATE);
        String contents = pref.getString(chatting_list.get(position).name, ""); //key, value(defaults)
        String hour = pref.getString("hour", "");*/


/*        Chat chat = chat_list.get(chat_list.size());

        // Gson 인스턴스 생성
        Gson gson = new GsonBuilder().create();
        // JSON 으로 변환
        String strChat = gson.toJson(chat, Chat.class);

        SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("chat", strChat); // JSON으로 변환한 객체를 저장한다.
        editor.commit(); //완료한다.*/






        myViewHolder.item_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {// 아이템 short 클릭
                View view = myViewHolder.item_layout;
                mCallback.onClick(view, position, chatting_list);
                Log.d("aaaa", "버튼을 누른 아이템의 위치는 " + position);
            }
        });

        myViewHolder.item_layout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                View view = myViewHolder.item_layout;
                mCallback.onLongClickListener(view, position, chatting_list);
                Log.d("aaaa", "버튼을 누른 아이템의 위치는 " + position);

                return true;
            }
        });

    }

    @Override
    public int getItemCount() {
        Log.d("chatting_list", "getItemCount: " + chatting_list.size());
        return chatting_list.size();
    }
}
