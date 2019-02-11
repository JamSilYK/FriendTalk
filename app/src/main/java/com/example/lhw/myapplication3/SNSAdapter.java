package com.example.lhw.myapplication3;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import static android.view.View.GONE;
import static com.example.lhw.myapplication3.R.drawable.ic_baseline_already_thumb_up_alt_24px;

public class SNSAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{



    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

    final int my = 1;
    final int other = 2;

    final int DELETE = 20;
    final int MODIFY = 30;
    final int LIKEIT = 40;

    private ArrayList<SNSVo> snsvo_list;
    Context context;
    private SNSOnitemClick mCallback;
    String checked_email;


    public SNSAdapter(Context context, ArrayList<SNSVo> snsvo_list, String checked_meail, SNSOnitemClick listener) {
        this.snsvo_list = snsvo_list;
        this.context = context;
        this.checked_email = checked_meail;
        this.mCallback = listener;
    }

    public static class SNSViewHolder extends RecyclerView.ViewHolder { //SNSViewHolder 클래스
        ImageView profile;
        ImageView contents_uri;
        ImageView bt_menu;
        TextView name;
        TextView time;
        TextView contents_text;
        TextView text_likeit;
        TextView count_likeit;
        Button bt_likeit;
        Button bt_add_comment;
        TextView count_comments;


        View item_card;


        public SNSViewHolder(View view) { //3번째로 실행
            super(view);
            profile = view.findViewById(R.id.profile);
            contents_uri = view.findViewById(R.id.contents_uri);
            name = view.findViewById(R.id.name);
            time = view.findViewById(R.id.time);
            contents_text = view.findViewById(R.id.contents_text);
            text_likeit = view.findViewById(R.id.text_likeit);
            bt_likeit = view.findViewById(R.id.bt_likeit);
            count_likeit = view.findViewById(R.id.count_likeit);
            item_card = view.findViewById(R.id.item_card);
            bt_menu = view.findViewById(R.id.bt_menu);
            bt_add_comment = view.findViewById(R.id.bt_add_comment);
            count_comments = view.findViewById(R.id.count_comments);


        }

    }
    @Override
    public int getItemViewType(int position) {

        if(snsvo_list.get(position).email.equals(checked_email)){
            return my;
        }

        else {
            return other;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = null;

        if(viewType == my){
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.sns_item, parent, false);
        }

        else if(viewType == other){
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.sns_item, parent, false);
        }


        return new SNSViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, final int position) {
        final SNSViewHolder snsViewHolder = (SNSViewHolder)viewHolder;

        final int viewtype = viewHolder.getItemViewType();

        snsViewHolder.name.setText(snsvo_list.get(position).name); //이름
        Uri profile_real_uri = Uri.parse(snsvo_list.get(position).uri);
        Picasso.get().load(profile_real_uri).transform(new CircleTransform()).into(snsViewHolder.profile); //내 프로필
        snsViewHolder.time.setText(snsvo_list.get(position).date); //올린 시간
        snsViewHolder.contents_text.setText(snsvo_list.get(position).contents_text); //쓴 텍스트 내용
        if(snsvo_list.get(position).contents_uri!=null){
            Uri contents_real_uri = Uri.parse(snsvo_list.get(position).contents_uri); //컨텐츠 유알아이 파싱
            Picasso.get().load(contents_real_uri).resize(700, 500).into(snsViewHolder.contents_uri);
        }

        int checked2 = 0;


        snsViewHolder.count_likeit.setText(String.valueOf("좋아요 " + snsvo_list.get(position).likeit.size() + "개"));

        FirebaseDatabase.getInstance().getReference().child("SNS").child(snsvo_list.get(position).checkd_date).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild("comments")){
                    String count = String.valueOf(dataSnapshot.child("comments").getChildrenCount());
                    snsViewHolder.count_comments.setText("댓글" + count+"개");
                }

                else {
                    snsViewHolder.count_comments.setVisibility(GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        if(snsvo_list.get(position).likeit.size() == 0){
            snsViewHolder.count_likeit.setVisibility(GONE);
        }

        else {
            snsViewHolder.count_likeit.setVisibility(View.VISIBLE);
            for(int i = 0; i < snsvo_list.get(position).likeit.size(); i++) {
                if(snsvo_list.get(position).likeit.containsKey(currentUser.getUid())){
                    snsViewHolder.bt_likeit.setBackgroundResource(R.drawable.ic_baseline_already_thumb_up_alt_24px);
                    checked2++;
                }
            }
        }

        final int finalChecked = checked2;
        snsViewHolder.bt_likeit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View view = snsViewHolder.bt_likeit;
                if(finalChecked == 0) { //내가 좋아요를 누르지 않았을때
                    snsvo_list.get(position).likeit.put(currentUser.getUid(), true);
                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    mCallback.onClick(view, position, snsvo_list, LIKEIT);
                    database.getReference("SNS").child(snsvo_list.get(position).checkd_date).child("likeit").setValue(snsvo_list.get(position).likeit);
                    snsViewHolder.bt_likeit.setBackgroundResource(R.drawable.ic_baseline_already_thumb_up_alt_24px);
                    Toast.makeText(context, "좋아요를 눌렀습니다.", Toast.LENGTH_SHORT).show();
                    snsViewHolder.count_likeit.setText(String.valueOf("좋아요 " + snsvo_list.get(position).likeit.size() + "개"));
                    snsViewHolder.count_likeit.setVisibility(View.VISIBLE);
                    notifyDataSetChanged();


                }

                else {
                    Toast.makeText(context, "이미 좋아요를 눌렀습니다.", Toast.LENGTH_SHORT).show();
                }

            }
        });

        snsViewHolder.bt_add_comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SNSVo comment_snsvo = snsvo_list.get(position);
                Intent intent = new Intent(context, SNSCommentActivity.class);
                intent.putExtra("comment_snsvo", comment_snsvo);
                context.startActivity(intent);
            }
        });

        if(viewtype == my){
            snsViewHolder.bt_menu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) { // 아이템 short 클릭

                    PopupMenu popup = new PopupMenu(context, snsViewHolder.bt_menu);
                    popup.inflate(R.menu.mymenu);
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()){
                                case R.id.sns_delete:
                                    View view = snsViewHolder.item_card;
                                    mCallback.onClick(view, position, snsvo_list, DELETE);
                                    return true;
                                case R.id.sns_modify:
                                    View view1 = snsViewHolder.item_card;
                                    mCallback.onClick(view1, position, snsvo_list, MODIFY);

                                    return true;
                                default:
                                    return false;
                            }
                        }
                    });
                    popup.show();
                }
            });
        }

        else if(viewtype == other){
            snsViewHolder.bt_menu.setVisibility(GONE);

        }
    }

    @Override
    public int getItemCount() {
        Log.d("SNSAdapter", "getItemCount: " + snsvo_list.size());
        return snsvo_list.size();
    }
}
