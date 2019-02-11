package com.example.lhw.myapplication3;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class SignInActivity extends AppCompatActivity {

    final int REQUEST_ACT_REGISTER = 33;

    Bitmap bitmap;
    Uri external_uri;
    String st_uri;
    private static final String TAG = "SignInActivity";

    private StorageReference mStorageRef;
    private FirebaseAuth mAuth;
    FirebaseDatabase database;
    DatabaseReference myRef;
    Uri downloadUri;

    String st_email;
    String st_password;
    String st_name;
    String st_phone;

    String email;

    int name_check = 0;

    String getuid;

    ArrayList<User> user2_list;

    private boolean check = false;

    private User user;

    TextView regist_email;
    TextView regist_password;
    TextView regist_name;
    TextView regist_phone;
    ImageView regist_picture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        mAuth = FirebaseAuth.getInstance(); //파이어베이스 인스턴스 초기화


        regist_email = findViewById(R.id.regist_email);
        regist_password = findViewById(R.id.regist_password);
        regist_name = findViewById(R.id.regist_name);
        regist_phone = findViewById(R.id.regist_phone);
        regist_picture = findViewById(R.id.regist_picture);

        user2_list = new ArrayList<User>();

        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("user");

        mStorageRef = FirebaseStorage.getInstance().getReference();

        Button bt_id_check = findViewById(R.id.id_check);
        /*download_uri();*/
        bt_id_check.setOnClickListener(new View.OnClickListener() { //이메일 중복체크
            @Override
            public void onClick(View v) { //이메일 중복체크
                regist_email = findViewById(R.id.regist_email);
                String tmp_email = regist_email.getText().toString();
                Search_email();
                int a = 0;
                for(int i = 0; i<user2_list.size(); i++) {
                    if(user2_list.get(i).email.equals(tmp_email)){
                        a++;
                    }
                }
                if(a == 0) {
                    Toast.makeText(SignInActivity.this, "사용가능", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(SignInActivity.this, "사용불가능", Toast.LENGTH_SHORT).show();
                }
            }
        });

        regist_picture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, REQUEST_ACT_REGISTER);
            }
        });

        Button bt_name_check = findViewById(R.id.name_check);
        bt_name_check.setOnClickListener(new View.OnClickListener() { //닉네임 중복체크
            @Override
            public void onClick(View v) { // 닉네임 중복체크

                if(regist_name.getText().toString() != null){
                    myRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for(DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()){
                                User user = dataSnapshot1.getValue(User.class);

                                if(user.name.equals(regist_name.getText().toString()) == true){
                                    name_check++;
                                }

                            }

                            if(name_check>0){
                                Toast.makeText(SignInActivity.this, "사용불가능", Toast.LENGTH_SHORT).show();
                                name_check = 0;
                            }

                            else {
                                Toast.makeText(SignInActivity.this, "사용가능", Toast.LENGTH_SHORT).show();
                                name_check = 0;
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }

            }
        });



        Button bt_regist = findViewById(R.id.bt_regist); //회원가입
        bt_regist.setOnClickListener(new View.OnClickListener() { //레지스터 버튼 눌렀을 때
            @Override
            public void onClick(View v) {
                st_name = regist_name.getText().toString();
                st_email = regist_email.getText().toString();
                st_password = regist_password.getText().toString();
                st_phone = regist_phone.getText().toString();

                Log.d(TAG, "st_email" + st_email);
                Log.d(TAG, "st_password" + st_password);


                if(check){
                    Log.d(TAG, "check st_uri: " + st_uri);
                    /*user = new User(st_email, st_name, st_password, st_phone, st_uri);
                    myRef.child(st_name).setValue(user);*/
                    register(st_email, st_password);
                }

                else {
                    Toast.makeText(SignInActivity.this, "사진을 업로드 해주세요.", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

    public void register(String email, String password){
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();

                            final FirebaseUser currentUser = mAuth.getCurrentUser();


                            getuid = currentUser.getUid().toString();

                            st_name = regist_name.getText().toString();

                            //유저프로필 업로드
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(st_name)
                                    .build();

                            user.updateProfile(profileUpdates)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Log.d(TAG, "User profile updated.");
                                                Intent intent = new Intent(SignInActivity.this, LoginActivity.class);
                                                uploadImage();
                                                Log.d(TAG, "currentUser.getDisplayName(): " + currentUser.getDisplayName());
                                                startActivity(intent);


                                                Toast.makeText(SignInActivity.this, "회원가입 완료", Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    });

                        } else {
                            // If sign in fails, display a message to the user.
                            //updateUI(null);
                        }

                    }
                });
    }

    public void download_uri(){
        StorageReference islandRef = mStorageRef.child("users/basicprofile.png");

        islandRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Log.d(TAG, "onSuccess: ");
                external_uri = uri;
                st_uri = external_uri.toString();
                Log.d(TAG, "onSuccess: " + external_uri.toString());

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.d(TAG, "Fail: ");
                Log.d(TAG, "Failue: ");
            }
        });
    }

    public void Search_email(){ //이메일 체크
        myRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                user = dataSnapshot.getValue(User.class);
                user2_list.add(user);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public void Search_name(){

        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                String tmp = regist_name.getText().toString();
                if(dataSnapshot.hasChild(tmp)) {
                    Toast.makeText(SignInActivity.this, "닉네임 중복", Toast.LENGTH_SHORT).show();
                }

                else {
                    Toast.makeText(SignInActivity.this, "사용가능", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)  {
        super.onActivityResult(requestCode, resultCode, data);

        if(REQUEST_ACT_REGISTER == requestCode){
            Uri image = data.getData();
            st_uri = image.toString();
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), image);
                regist_picture.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }

            finally {
                check = true;
                Log.d(TAG, "onActivityResult: finally");
            }
        }

    }

    public void uploadImage(){
        st_email = regist_email.getText().toString();
        st_name = regist_name.getText().toString();
        st_password = regist_password.getText().toString();
        st_phone = regist_phone.getText().toString();
        Log.d(TAG, "uploadImage: " + st_email);

        StorageReference mountainsRef = mStorageRef.child("users").child(getuid+".jpg");

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

                mStorageRef.child("users/"+getuid+".jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        // Got the download URL for 'users/me/profile.png'
                        Log.d(TAG, "mStorageRef onSuccess: ");
                        Log.d(TAG, "mStorageRef onSuccess: " + uri.toString());
                        st_uri = uri.toString();
                        Log.d(TAG, "onSuccess: st_uri " + st_uri);
                        user = new User(st_email, st_name, st_password, st_phone, st_uri);
                        myRef.child(getuid).setValue(user);

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

    @Override
    public void onStart() {
        super.onStart();
    }
}
