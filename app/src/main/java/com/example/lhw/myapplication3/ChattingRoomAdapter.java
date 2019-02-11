package com.example.lhw.myapplication3;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ChattingRoomAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    final int right = 1;
    final int wrong = 2;

    private ArrayList<Chat> chat_list;
    Context context;
    String checked_email;
    User user;


    public ChattingRoomAdapter(Context context, ArrayList<Chat> chat_list, ArrayList<User> memebers) {
        Log.d("ChatAdapter", "Chat_Adapter");
        this.chat_list = chat_list;
        this.context = context;
        this.checked_email = checked_email;
        this.user = user;
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder { //MyViewHolder 클래스

        TextView chat_item_contents;
        ImageView chat_profile;
        TextView time;


        MyViewHolder(View view){ //3번째로 실행
            super(view);
            Log.d("ChatAdapter", "ChatMyViewHolder");
            //chat_item_name = view.findViewById(R.id.Chat);
            chat_item_contents = view.findViewById(R.id.mTextView);
            chat_profile = view.findViewById(R.id.my_comment_profile);
            time = view.findViewById(R.id.time);
            //item_card = view.findViewById(R.id.chat_item_text);
        }
    }

    @Override
    public int getItemViewType(int position) {

        Log.d("씨발년이?", "getItemViewType:" + checked_email);

        if(chat_list.get(position).email.equals(checked_email)){
            return right;
        }

        else {
            return wrong;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) { //onCreateViewHolder  //2번째로 실행

        View v;
        if(viewType == right) {
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.right_chat_item, parent, false);
        }

        else {
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.left_chat_item, parent, false);
        }
        /*v = LayoutInflater.from(parent.getContext()).inflate(R.layout.my_test_view, parent, false);*/
        Log.d("ChatAdapter", "onCreateViewHolder");
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Log.d("ChatAdapter", "onBindViewHolder");
        int viewtype = holder.getItemViewType();
        final MyViewHolder ChatmyViewHolder = (MyViewHolder)holder;
        if(viewtype == right){
            ChatmyViewHolder.chat_item_contents.setText(chat_list.get(position).contents);
            ChatmyViewHolder.time.setText(chat_list.get(position).hour);
        }

        else {
            ChatmyViewHolder.chat_item_contents.setText(chat_list.get(position).contents);
            Picasso.get().load(Uri.parse(chat_list.get(position).uri)).transform(new CircleTransform()).into(ChatmyViewHolder.chat_profile);
            ChatmyViewHolder.time.setText(chat_list.get(position).hour);
        }


    }

    @Override
    public int getItemCount() {
        Log.d("아씨바바바바", "getItemCount: " + chat_list.size());
        return chat_list.size();

    }



}
