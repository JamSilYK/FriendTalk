package com.example.lhw.myapplication3;

import android.app.Application;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;

import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.PicassoProvider;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;

public class FriendFragment extends Fragment implements OnItemClick{

    final String TAG = "FriendFragment"; //태그
    final int REQUEST_FRIENDS = 5;//친구추가
    final int REQUEST_CLICK_MYPROFILE = 10;

    boolean dialog_check = false;
    RecyclerView mRecyclerView;
    RecyclerView.LayoutManager mLayoutManager;
    CardView card_myprofile;
    TextView myprofile_name;
    ImageView mypicture;
    ProgressBar pbfriend;
    ImageButton friends_bt_plus;
    FloatingActionButton floatingActionButton2;

    String myname; //현재유저의 이름
    String email;

    ArrayList<User> userInfoArrayList;

    User user;
    User friendUser;


    Bitmap bitmap;

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef;
    private FirebaseAuth mAuth;
    private StorageReference mStorageRef;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    private List<String> keys = new ArrayList<>();


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "Friend1");
        View v = inflater.inflate(R.layout.fragment_friend, container, false);

        ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(getActivity().CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();



        Toast.makeText(getActivity(), "인터넷 상태 양호", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "onCreateView: 1");
        mAuth = FirebaseAuth.getInstance();
        final FirebaseUser currentUser = mAuth.getCurrentUser();
        mStorageRef = FirebaseStorage.getInstance().getReference();

        pbfriend = v.findViewById(R.id.pbfriend); //프로그레스바
        myprofile_name = v.findViewById(R.id.myname); // 내 프로필 이름
        mypicture = v.findViewById(R.id.mypicture);
        floatingActionButton2 = (FloatingActionButton)v.findViewById(R.id.floatingActionButton2);


        myRef = database.getReference("user").child(currentUser.getUid());
        Log.d(TAG, "onCreateView: getUID" + currentUser.getUid());
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                user = new User();
                user = dataSnapshot.getValue(User.class);

                Log.d(TAG, "user.uri" + user.uri);
                myprofile_name.setText(user.name);
                Log.d(TAG, "onDataChange: " + user.toString());

                if(TextUtils.isEmpty(user.uri)) {

                }
                else {
                    pbfriend.setVisibility(View.VISIBLE);
                    Picasso.get().load(user.uri).transform(new CircleTransform()).into(mypicture, new Callback() {
                        @Override
                        public void onSuccess() {
                            pbfriend.setVisibility(View.GONE);
                            Log.d(TAG, "onSuccess: picasso upload"); //로딩구현
                        }

                        @Override
                        public void onError(Exception e) {

                        }
                    });

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }

        });

        friends_bt_plus = v.findViewById(R.id.friends_bt_plus); //친구 검색 버튼
        friends_bt_plus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent intent = new Intent(getActivity(), Plus_FriendsActivity.class);
                    startActivityForResult(intent, REQUEST_FRIENDS);
                }

        });

            //친구 리싸이클러뷰
        mRecyclerView = v.findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        userInfoArrayList = new ArrayList<>();
        final FriendAdapter friendAdapter = new FriendAdapter(getActivity(), userInfoArrayList, this);
        Log.d(TAG, "onCreateView: 8");
        mRecyclerView.setAdapter(friendAdapter);
        //친구 리싸이클러뷰

        DatabaseReference FriendRef = database.getReference("user").child(currentUser.getUid()).child("friends");
        FriendRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                String value = dataSnapshot.getValue().toString();
                Log.d(TAG, "onChildAdded: Value" + value);
                friendUser = dataSnapshot.getValue(User.class);
                userInfoArrayList.add(friendUser);
                mRecyclerView.scrollToPosition(userInfoArrayList.size()-1);
                mRecyclerView.getAdapter().notifyItemInserted(userInfoArrayList.size()-1);



            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {


            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {


            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {



            }

        });

        floatingActionButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), SelectFriendActivity.class);
                startActivity(intent);
            }
        });




        card_myprofile = v.findViewById(R.id.card_myprofile); //내 프로필 카드뷰 아이디
        card_myprofile.setOnClickListener(new View.OnClickListener() { //내 프로필을 클릭한다면
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), ClickMyProfileActivity.class);
                    intent.putExtra("myname", myprofile_name.getText().toString()); //URL을 넘겨줄지 고민좀해보자
                    startActivityForResult(intent, REQUEST_CLICK_MYPROFILE);
                }
            });




        return v;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(REQUEST_CLICK_MYPROFILE == requestCode && data !=null) { //내 프로필 사진 변경할 때
            user.uri = data.getExtras().getString("st_uri");
            Uri image = Uri.parse(data.getExtras().getString("st_uri"));
            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), image);
                /*mypicture.setImageBitmap(bitmap);*/
            } catch (IOException e) {
                    e.printStackTrace();
                }

                finally{
                    FirebaseUser currentUser = mAuth.getCurrentUser();
                    StorageReference mountainsRef = mStorageRef.child("users").child(currentUser.getUid()+".jpg");
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    byte[] data2 = baos.toByteArray();
                    UploadTask uploadTask = mountainsRef.putBytes(data2);
                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Handle unsuccessful uploads

                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            final FirebaseUser currentUser = mAuth.getCurrentUser();

                            mStorageRef.child("users/"+currentUser.getUid()+".jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String result_uri = uri.toString();
                                    myRef.child("uri").setValue(result_uri);
                                    Picasso.get().load(uri).transform(new CircleTransform()).into(mypicture);
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

        else if(REQUEST_FRIENDS == requestCode && data != null && resultCode == RESULT_OK){
            /*user = (User) data.getExtras().getSerializable("user");*/
        }
    }


    @Override
    public void onClick(View view, int position, ArrayList<User> userInfoArrayList) { //아이템 클릭했을때
        User item_user;
        item_user = userInfoArrayList.get(position);
        Toast.makeText(getActivity(), "아이템을 클릭했습니다.", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(getActivity(), Click_FriendsActivity.class);
        intent.putExtra("item_user", item_user);
        intent.putExtra("myuser", user);

        startActivity(intent);

    }


    @Override
    public boolean onLongClick(View view, int position, ArrayList<User> userInfoArrayList) {
        dialog_check = false;
        show(userInfoArrayList, position, view);

        return true;
    }

    boolean show(ArrayList<User> userInfoArrayList, int position, View view) {
        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(getActivity());
        final int a = position;
        final View view2 = view;

        builder.setTitle("AlertDialog Title");
        builder.setMessage("친구를 삭제하시겠습니까?");
        builder.setNegativeButton("아니오",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getActivity(), "아니오를 선택했습니다.", Toast.LENGTH_LONG).show();

                        dialog_check = false;
                    }
                });
        builder.setPositiveButton("예",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(getActivity().CONNECTIVITY_SERVICE);
                        NetworkInfo networkInfo = cm.getActiveNetworkInfo();

                        sharedPreferences = getActivity().getSharedPreferences("shared", MODE_PRIVATE); //내 정보 저장
                        sharedPreferences.edit().remove("user_friends").commit();



                        Toast.makeText(getActivity(), "친구를 삭제하였습니다.", Toast.LENGTH_LONG).show();
                        dialog_check = true;
                        Log.d(TAG, "show(): 들어왔음");
                        mAuth = FirebaseAuth.getInstance();
                        FirebaseUser currentUser = mAuth.getCurrentUser();

                        final DatabaseReference removeRef = database.getReference("user")
                                .child(currentUser.getUid())
                                .child("friends")
                                .child(FriendFragment.this.userInfoArrayList.get(a).name);
                        removeRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                removeRef.removeValue(); //파이어베이스 데이터 삭제userInfoArrayList.remove(position);
                                FriendFragment.this.userInfoArrayList.remove(a);
                                mRecyclerView.getAdapter().notifyItemRemoved(a); //리싸이클러뷰 아이템 삭제

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                });

        builder.show();
        return dialog_check;
    }

    @Override
    public void onClick(String value) {

    }

    @Override
    public void onPause() {
        super.onPause();

    }

    public void uploadImage(){
        FirebaseUser currentUser = mAuth.getCurrentUser();
        StorageReference mountainsRef = mStorageRef.child("users").child(currentUser.getUid()+".jpg");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
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
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                /*downloadUri = taskSnapshot.getUploadSessionUri();*/

            }
        });
    }


}
