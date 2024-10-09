package com.jb.proyectoandroid;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
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

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.auth.oauth2.GoogleCredentials;
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
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;

import java.io.IOException;
import java.io.InputStream;
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
            sendMessage(message, false);
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
                                                    sendMessage(imageUrl, true);
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

    void sendMessage(String message, boolean isImage){
        chatroomModel.setLastMessageTimestamp(Timestamp.now());
        chatroomModel.setLastMessageSenderId(FirebaseUtil.currentUserId());
        ChatMessageModel chatMessageModel;
        if (!isImage){
            chatMessageModel = new ChatMessageModel(message,"", FirebaseUtil.currentUserId(),Timestamp.now());
            messageInput.setText("");
            chatroomModel.setLastMessage(message);
        }
        else{
            chatMessageModel = new ChatMessageModel("",message, FirebaseUtil.currentUserId(),Timestamp.now());
            chatroomModel.setLastMessage("imagen");
        }
        FirebaseUtil.getChatroomReference(chatroomId).set(chatroomModel);

        FirebaseUtil.getChatroomMessageReference(chatroomId).add(chatMessageModel)
                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {
                        if(task.isSuccessful()) {
                            messageInput.setText("");
                            sendNotification(message, isImage);
                        }
                    }
                });
    }

    void sendNotification(String message, boolean isImage){
        String receiverToken = otherUser.getFcmToken();
        getJsonString(message,receiverToken,isImage);
    }

    private void getJsonString(String message, String receiverToken, boolean isImage) {
        FirebaseUtil.currentUserDetails().get().addOnSuccessListener(documentSnapshot -> {
            UserModel currentUser = documentSnapshot.toObject(UserModel.class);
            if (currentUser != null) {
                String json = "{\n" +
                        "  \"message\": {\n" +
                        "    \"token\": \"" + receiverToken + "\",\n" +
                        "    \"notification\": {\n" +
                        "      \"title\": \"" + currentUser.getEmail() + "\",\n" +
                        (isImage ?
                        "      \"image\": \"" + message + "\"\n" :
                                "      \"body\": \"" + message + "\"\n") +
                        "    },\n" +
                        "    \"data\": {\n" +
                        "      \"chatroom\": \"" + chatroomId + "\"\n" +
                        "    }\n" +
                        "  }\n" +
                        "}";

                callApi(json);
            }
        });
    }

    private void callApi(String json) {
        Thread thread = new Thread(() -> {
            MediaType JSON = MediaType.parse("application/json; charset=utf-8");
            OkHttpClient client = new OkHttpClient();
            String url = "https://fcm.googleapis.com/v1/projects/android-chat-b6e3f/messages:send";
            RequestBody body = RequestBody.create(JSON, json);
            Request req;
            try {
                req = new Request.Builder()
                        .url(url)
                        .post(body)
                        .addHeader("Authorization", "Bearer " + getAccessToken())
                        .addHeader("Content-Type", "application/json; UTF-8")
                        .build();
                client.newCall(req).execute();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        thread.start();
    }

    public String getAccessToken() throws IOException {
        final String[] SCOPES = { "https://www.googleapis.com/auth/firebase.messaging" };
        InputStream serviceAccountStream = ChatActivity.class.getClassLoader().getResourceAsStream("android-chat-service-account.json");
        GoogleCredentials googleCredentials = GoogleCredentials
                .fromStream(serviceAccountStream)
                .createScoped(Arrays.asList(SCOPES));
        googleCredentials.refreshIfExpired();
        return googleCredentials.getAccessToken().getTokenValue();
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
                            "",
                            "");
                }
                FirebaseUtil.getChatroomReference(chatroomId).set(chatroomModel);
            }
        });
    }
}