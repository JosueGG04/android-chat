package com.jb.proyectoandroid;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

public class LoadingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);
        //if there is a logged user go to home activity else go to the login activity
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(FirebaseUtils.isLoggedIn()){
                    startActivity(new Intent(LoadingActivity.this,HomePage.class));
                } else{
                    startActivity(new Intent(LoadingActivity.this,MainActivity.class));
                }
            }
        }, 1000);
    }
}