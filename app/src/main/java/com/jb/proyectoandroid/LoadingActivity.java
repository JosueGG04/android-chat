package com.jb.proyectoandroid;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.jb.proyectoandroid.model.UserModel;
import com.jb.proyectoandroid.utils.AndroidUtil;
import com.jb.proyectoandroid.utils.FirebaseUtil;

public class LoadingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);
        if(getIntent().getExtras() != null) {
            // Desde la notificación
            String userId = getIntent().getExtras().getString("userId");
            assert userId != null;
            FirebaseUtil.allUserCollectionReference().document(userId).get()
                    .addOnCompleteListener(task -> {
                        if(task.isSuccessful()){
                            UserModel model = task.getResult().toObject(UserModel.class);
                            Intent mainIntent = new Intent(this, HomePage.class);
                            mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(mainIntent);

                            Intent intent = new Intent(this, ChatActivity.class);
                            assert model != null;
                            AndroidUtil.passUserModelAsIntent(intent, model);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                        }
                    });
        } else{
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if(FirebaseUtil.isLoggedIn()){
                        startActivity(new Intent(LoadingActivity.this,HomePage.class));
                    } else{
                        startActivity(new Intent(LoadingActivity.this,MainActivity.class));
                    }
                }
            }, 1000);
        }



    }
}