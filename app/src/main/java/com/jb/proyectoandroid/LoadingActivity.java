package com.jb.proyectoandroid;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.jb.proyectoandroid.model.ChatroomModel;
import com.jb.proyectoandroid.model.UserModel;
import com.jb.proyectoandroid.utils.AndroidUtil;
import com.jb.proyectoandroid.utils.FirebaseUtil;

import java.util.List;

public class LoadingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);
        if(getIntent().getExtras() != null) {
            // Desde la notificación
            String chatroom = getIntent().getExtras().getString("chatroom");
            assert chatroom != null;
            FirebaseUtil.allChatroomsCollectionReference().document(chatroom).get()
                    .addOnCompleteListener(task -> {
                        if(task.isSuccessful()){
                            ChatroomModel model = task.getResult().toObject(ChatroomModel.class);
                            String currentUserId = FirebaseUtil.currentUserId();
                            String otherUserID = "";
                            //TODO if for future implementation of groupchats
                            List<String> chatUserId = model.getUserIds();
                            for (String id:
                                    chatUserId) {
                                if(!id.equals(currentUserId)){
                                    otherUserID = id;
                                }
                            }
                            String id = otherUserID.isEmpty() ? currentUserId : otherUserID;

                            FirebaseUtil.getUserReference(id).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    if(documentSnapshot.exists()) {
                                        UserModel user = documentSnapshot.toObject(UserModel.class);
                                        if(user!=null){
                                            Intent mainIntent = new Intent(getApplicationContext(), HomePage.class);
                                            mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                            startActivity(mainIntent);

                                            Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
                                            AndroidUtil.passUserModelAsIntent(intent, user);
                                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                            startActivity(intent);
                                            finish();

                                        }
                                    }else{
                                        Log.d("Firestore","user doesnt exist");
                                    }
                                }
                            });
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