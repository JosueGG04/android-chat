package com.jb.proyectoandroid;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.jb.proyectoandroid.model.UserModel;
import com.jb.proyectoandroid.utils.AndroidUtil;

public class ChatActivity extends AppCompatActivity {
    UserModel otherUser;
    ImageButton sendMessageBtn;
    ImageButton backBtn;
    TextView otherEmail;
    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chat);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        otherUser = AndroidUtil.getUserModelFromIntent(getIntent());
        sendMessageBtn = findViewById(R.id.message_send_btn);
        backBtn = findViewById(R.id.back_btn);
        otherEmail = findViewById(R.id.other_user_email);
        recyclerView = findViewById(R.id.chat_recycler_view);

        backBtn.setOnClickListener((v) -> {
            getOnBackPressedDispatcher().onBackPressed();
        });
        otherEmail.setText(otherUser.getEmail());
    }
}