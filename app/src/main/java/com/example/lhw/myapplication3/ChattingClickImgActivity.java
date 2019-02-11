package com.example.lhw.myapplication3;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class ChattingClickImgActivity extends AppCompatActivity {

    final int REQUEST_ACT_CONFIRM_GALLERY = 567;
    final String TAG = "ChattingClickImgAc";

    Button bt_download;
    ImageView bmImage;
    Bitmap bitmap;

    ChatModel.Comment comment;

    Bitmap mSaveBm;
    String imageUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatting_click_img);


        comment = (ChatModel.Comment) getIntent().getExtras().getSerializable("comments");

        bt_download = findViewById(R.id.bt_download);
        bmImage = findViewById(R.id.img);
        Picasso.get().load(Uri.parse(comment.photo_uri)).centerInside().fit().into(bmImage);
        imageUrl  = comment.photo_uri;
        BitmapFactory.Options bmOptions;
        bmOptions = new BitmapFactory.Options();
        bmOptions.inSampleSize = 1;

        OpenHttpConnection opHttpCon = new OpenHttpConnection();
        opHttpCon.execute(bmImage, imageUrl);

        bt_download.setOnClickListener(new View.OnClickListener() { //다운로드 시작
            @Override
            public void onClick(View v) {
                checkVerify();
                download();

            }
        });

    }

    void download(){

        Calendar calendar2 = Calendar.getInstance();
        SimpleDateFormat transFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String simpledate = transFormat.format(calendar2.getTime());

        OutputStream outStream = null;
        String extStorageDirectory = Environment.getExternalStorageDirectory().toString();
        File file = new File(extStorageDirectory, simpledate+".PNG");
        if(file.exists()){
            file.mkdirs();
        }

        try {
            outStream = new FileOutputStream(file);
            mSaveBm.compress(
                    Bitmap.CompressFormat.PNG, 50, outStream);
            outStream.flush();
            outStream.close();

            Toast.makeText(getApplication(), "다운로드 완료", Toast.LENGTH_LONG).show();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(getApplication(), e.toString(), Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getApplication(), e.toString(), Toast.LENGTH_LONG).show();
        }
        finally {
            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE), String.valueOf(Uri.parse("file://" + file.getPath())));
        }


/*        OutputStream outStream = null;
        String extStorageDirectory = Environment.getExternalStorageDirectory().toString();
        File file = new File(extStorageDirectory, "downimage.PNG");
        try {
            outStream = new FileOutputStream(file);
            mSaveBm.compress(
                    Bitmap.CompressFormat.PNG, 50, outStream);
            outStream.flush();
            outStream.close();

            Toast.makeText(getApplication(), "다운로드 완료", Toast.LENGTH_LONG).show();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(getApplication(), e.toString(), Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getApplication(), e.toString(), Toast.LENGTH_LONG).show();
        }
        finally {
            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE), String.valueOf(Uri.parse("file://" + file.getPath())));
        }*/
    }

    @TargetApi(Build.VERSION_CODES.M)
    public void checkVerify() {
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // Should we show an explanation?
            if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                // ...
            }
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
                    1);
        }
        else {
            /*download();*/
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {
            if (grantResults.length > 0) {
                for (int i=0; i<grantResults.length; ++i) {
                    if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                        // 하나라도 거부한다면.
                        new AlertDialog.Builder(this).setTitle("알림").setMessage("권한을 허용해주셔야 앱을 이용할 수 있습니다.")
                                .setPositiveButton("종료", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                        finish();
                                    }
                                }).setNegativeButton("권한 설정", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                        .setData(Uri.parse("package:" + getApplicationContext().getPackageName()));
                                getApplicationContext().startActivity(intent);
                            }
                        }).setCancelable(false).show();

                        return;
                    }
                }
                /*download();*/
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ACT_CONFIRM_GALLERY && data != null) {

        } else if (requestCode == REQUEST_ACT_CONFIRM_GALLERY && data == null) {

        }
    }

    class OpenHttpConnection extends AsyncTask<Object, Void, Bitmap> {

        private ImageView bmImage;


        @Override
        protected Bitmap doInBackground(Object... params) {
            Bitmap mBitmap = null;
            bmImage = (ImageView) params[0];
            String url = (String) params[1];
            InputStream in = null;
            try {
                in = new java.net.URL(url).openStream();
                mBitmap = BitmapFactory.decodeStream(in);
                in.close();

            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return mBitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bm) {
            super.onPostExecute(bm);
            mSaveBm = bm;
            bmImage.setImageBitmap(bm);
        }
    }
}
