package com.example.lhw.myapplication3;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    final String TAG = "LoginActivity";

    private FirebaseAuth mAuth;

    Button bt_login;
    Button bt_signin;
    Button bt_search_id;
    Button bt_search_password;

    TextView view_email;
    TextView view_password;

    String email;
    String password;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        view_email = findViewById(R.id.email);
        view_password = findViewById(R.id.pass);




        bt_search_password = findViewById(R.id.bt_search_password);


        bt_login = findViewById(R.id.bt_login); //로그인버튼 눌렀을때
        bt_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String st_email = view_email.getText().toString();
                String st_password = view_password.getText().toString();

                login_check(st_email, st_password);
            }
        });

        bt_signin = findViewById(R.id.sign_bt); //회원가입버튼 눌렀을때
        bt_signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, SignInActivity.class);
                startActivity(intent);

            }
        });

        bt_search_id = findViewById(R.id.search_id); //아이디찾기 버튼 눌렀을때
        bt_search_id.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        bt_search_password = findViewById(R.id.bt_search_password); //비밀번호 찾기 버튼눌렀을때
        bt_search_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    public void login_check(String st_email, String st_password) {
        mAuth.signInWithEmailAndPassword(st_email, st_password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }

                        // ...
                    }
                });
    }
}
