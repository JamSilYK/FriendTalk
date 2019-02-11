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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class SNSWriteActivity extends AppCompatActivity {


    //태그//
    final String TAG = "SNSWriteActivity";
    //태그//
    //***********************************************************************************//
    final int REQUEST_IMAGE_CAPTURE = 99; //
    final int REQUEST_SELECT_GALLERY = 77;
    //***********************************************************************************//

    //*******************************엑티비티 아이디*************************************//
    ImageView sns_myprofile; //내 프로필 사진
    ImageView border; //사진이나 동영상 올렸을때 보여지는 영역
    TextView sns_myname; //내 이름
    TextView contents_text;//텍스트내용
    Button bt_picture;//사진 버튼
    Button bt_gallery; //갤러리
    Button bt_send;//올리기 버튼
    //*******************************엑티비티 아이디*************************************//

    //*******************************데이터베이스****************************************//
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference myRef;
    private DatabaseReference SNSRef;
    private StorageReference mStorageRef;

    Bitmap bitmap;

    //*******************************데이터베이스****************************************//

    User user; //내 정보

    ////////////////////////////
    Uri uri; //갤러리에서 얻어온 사진 정보
    ///////////////////////////

    ///////////유저 데이터 저장 변수////////////
    String contents_uri;
    String text;
    String name; // 내 이름
    String email; //내 이메일
    String myuri; // 내 프로필 uri
    String date; //오전 오후 몇시 몇분
    String formattedDate; //yyyymmmmdddd....

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_snswrite);

        //************************************아이디 세팅 *************************//
        sns_myprofile = findViewById(R.id.sns_myprofile);
        sns_myname = findViewById(R.id.sns_myname);
        contents_text = findViewById(R.id.text_contents);
        border = findViewById(R.id.border);

        bt_picture = findViewById(R.id.bt_picture);
        bt_send = findViewById(R.id.bt_send);
        bt_gallery = findViewById(R.id.bt_gallery);
        //************************************아이디 세팅 *************************//


        final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        myRef = database.getReference("user").child(currentUser.getUid()); //내 아이디 정보 세팅
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                user = dataSnapshot.getValue(User.class);
                Log.d(TAG, "onDataChange: user " + user.toString());
                Picasso.get().load(user.uri).transform(new CircleTransform()).into(sns_myprofile); //내 프로필 세팅
                sns_myname.setText(user.name); // 내 이름 세팅
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        bt_picture.setOnClickListener(new View.OnClickListener() { //사진 버튼 사진 촬영
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
                }
            }
        });

        bt_gallery.setOnClickListener(new View.OnClickListener() { //갤러리에 있는 사진을 선택할 때
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, REQUEST_SELECT_GALLERY);
            }
        });

        bt_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final Intent intent = new Intent(getApplicationContext(), MainActivity.class);

                text = contents_text.getText().toString(); //내가 적은 내용
                name = user.name; // 내 이름
                email = user.email; //내 이메일
                myuri = user.uri; // 내 프로필 uri

                Calendar c = Calendar.getInstance();
                SimpleDateFormat df2 = new SimpleDateFormat("hh:mm a");
                date = df2.format(c.getTime());

                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                formattedDate = df.format(c.getTime());



                SNSRef = database.getReference("SNS").child(formattedDate);





                mStorageRef = FirebaseStorage.getInstance().getReference();

                if(bitmap!=null){
                    StorageReference mountainsRef = mStorageRef.child("SNS").child(formattedDate+".jpg");
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

                            mStorageRef.child("SNS/"+formattedDate+".jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri2) {
                                    Toast.makeText(SNSWriteActivity.this, "정상적으로 업로드 되었습니다.", Toast.LENGTH_LONG).show();
                                    Log.d(TAG, "mStorageRef onSuccess: " + uri2.toString());
                                    contents_uri = uri2.toString();
                                    SNSVo snsVo = new SNSVo(name, email, myuri, text, contents_uri, date, formattedDate);
                                    SNSRef.setValue(snsVo);
                                    intent.putExtra("check", "SNSWriteActivity");
                                    startActivity(intent);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception exception) {
                                    // Handle any errors
                                    Log.d(TAG, "mStorageRef onFailure: ");
                                }
                            });
                        }
                    });
                }

                else {
                    SNSVo snsVo = new SNSVo(name, email, myuri, text, date, formattedDate);
                    Log.d(TAG, "snsVo.toString(): " + snsVo.toString());
                    Log.d(TAG, "snsVo.toString() " + snsVo.name + "," + snsVo.contents_uri + "," + snsVo.date + "," + snsVo.checkd_date + "," + snsVo.email);
                    SNSRef.setValue(snsVo);

                    intent.putExtra("check", "SNSWriteActivity");
                    startActivity(intent);
                }


            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(data != null && requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK){ //사진 찍기
            Bundle extras = data.getExtras();
            bitmap = (Bitmap) extras.get("data");
            ((ImageView)findViewById(R.id.border)).setImageBitmap(bitmap);
        }

        else if(data != null && requestCode == REQUEST_SELECT_GALLERY && resultCode == RESULT_OK){ //사진 갤러리 선택
            uri = data.getData();
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), uri);
                border.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
