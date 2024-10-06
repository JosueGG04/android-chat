package com.jb.proyectoandroid;

import static android.util.StatsLog.logEvent;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.jb.proyectoandroid.adapter.ChatRecyclerAdapter;
import com.jb.proyectoandroid.model.ChatMessageModel;
import com.jb.proyectoandroid.model.ChatroomModel;
import com.jb.proyectoandroid.model.UserModel;
import com.jb.proyectoandroid.utils.AndroidUtil;
import com.jb.proyectoandroid.utils.FirebaseUtil;

import java.util.Arrays;

public class ChatActivity extends AppCompatActivity {
    String chatroomId;
    UserModel otherUser;
    ChatroomModel chatroomModel;
    ImageButton sendMessageBtn;
    ImageButton backBtn;
    ImageButton imageBtn;
    TextView otherEmail;
    RecyclerView recyclerView;
    EditText messageInput;
    ActivityResultLauncher<Intent> getContent;
    ChatRecyclerAdapter adapter;
    FirebaseStorage storage;

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
        chatroomId = FirebaseUtil.getChatroomId(FirebaseUtil.currentUserId(),otherUser.getUserID());
        imageBtn = findViewById(R.id.message_image_btn);
        sendMessageBtn = findViewById(R.id.message_send_btn);
        backBtn = findViewById(R.id.back_btn);
        otherEmail = findViewById(R.id.other_user_email);
        recyclerView = findViewById(R.id.chat_recycler_view);
        messageInput = findViewById(R.id.chat_message_input);
        storage = FirebaseStorage.getInstance();

        backBtn.setOnClickListener((v) -> {
            getOnBackPressedDispatcher().onBackPressed();
        });
        otherEmail.setText(otherUser.getEmail());

        sendMessageBtn.setOnClickListener((v -> {
            String message = messageInput.getText().toString().trim();
            if(message.isEmpty())
                return;
            sendMessageToUser(message, 0);
        }));
        
        getOrCreateChatroomModel();
        setupChatRecyclerView();
        setupImagePicker();
    }

    void setupChatRecyclerView(){
        Query query = FirebaseUtil.getChatroomMessageReference(chatroomId).orderBy("timestamp", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<ChatMessageModel> options = new FirestoreRecyclerOptions.Builder<ChatMessageModel>()
                .setQuery(query, ChatMessageModel.class).build();
        adapter = new ChatRecyclerAdapter(options,getApplicationContext());
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setReverseLayout(true);
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(adapter);
        adapter.startListening();
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                recyclerView.smoothScrollToPosition(0);
            }
        });
    }

    private void setupImagePicker(){
        getContent = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult o) {
                        try {
                            Uri imageUri = o.getData().getData();
                            storage.getReference().child("images").child(imageUri.getLastPathSegment())
                                    .putFile(imageUri)
                                    .addOnCompleteListener(task -> {
                                        if(task.isSuccessful()){
                                            task.getResult().getStorage().getDownloadUrl().addOnCompleteListener(task1 -> {
                                                if(task1.isSuccessful()){
                                                    String imageUrl = task1.getResult().toString();
                                                    sendMessageToUser(imageUrl, 1);
                                                }
                                            });
                                        }
                                    });
                        }
                        catch (Exception e){
                            Toast.makeText(ChatActivity.this, "Ninguna imagen seleccionada", Toast.LENGTH_SHORT).show();
                    }
                }

    });
        imageBtn.setOnClickListener((v) -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            getContent.launch(intent);
        });
    }

    void sendMessageToUser(String message, Integer image){
        chatroomModel.setLastMessageTimestamp(Timestamp.now());
        chatroomModel.setLastMessageSenderId(FirebaseUtil.currentUserId());
        FirebaseUtil.getChatroomReference(chatroomId).set(chatroomModel);
        ChatMessageModel chatMessageModel;
        if (image == 0){
            chatMessageModel = new ChatMessageModel(message,"", FirebaseUtil.currentUserId(),Timestamp.now());
            messageInput.setText("");
        }
        else{
            chatMessageModel = new ChatMessageModel("",message, FirebaseUtil.currentUserId(),Timestamp.now());
        }

        FirebaseUtil.getChatroomMessageReference(chatroomId).add(chatMessageModel)
                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {
                    }
                });
    }

    void getOrCreateChatroomModel(){
        FirebaseUtil.getChatroomReference(chatroomId).get().addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                chatroomModel = task.getResult().toObject(ChatroomModel.class);
                if(chatroomModel==null){
                    //first time chat
                    chatroomModel = new ChatroomModel(
                            chatroomId,
                            Arrays.asList(FirebaseUtil.currentUserId(),otherUser.getUserID()),
                            Timestamp.now(),
                            "");
                }
                FirebaseUtil.getChatroomReference(chatroomId).set(chatroomModel);
            }
        });
    }
}