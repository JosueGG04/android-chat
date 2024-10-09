package com.jb.proyectoandroid;

import static java.security.AccessController.getContext;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.PopupMenu;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.Query;
import com.jb.proyectoandroid.adapter.ChatRecyclerAdapter;
import com.jb.proyectoandroid.adapter.HomePageRecyclerAdapter;
import com.jb.proyectoandroid.model.ChatMessageModel;
import com.jb.proyectoandroid.model.ChatroomModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessaging;
import com.jb.proyectoandroid.utils.FirebaseUtil;

public class HomePage extends AppCompatActivity {

    ImageButton newChatButton;
    ImageButton menuButton;
    HomePageRecyclerAdapter adapter;
    RecyclerView recyclerView;
    FirebaseAuth auth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home_page);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        newChatButton = findViewById(R.id.new_chat_btn);

        newChatButton.setOnClickListener((v)->{
            startActivity(new Intent(HomePage.this,NewChatSearchActivity.class).setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY));
        });
        menuButton = findViewById(R.id.home_options_menu_btn);
        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Crear el menú popup
                PopupMenu popup = new PopupMenu(HomePage.this, v);
                // Inflar el menú popup desde XML
                popup.getMenuInflater().inflate(R.menu.home_options_menu, popup.getMenu());

                // Manejar la selección de ítems del menú
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if (item.getItemId() == R.id.menu_logout) {
                            FirebaseMessaging.getInstance().deleteToken().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        auth.signOut();
                                        Intent intent = new Intent(HomePage.this,LoadingActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                    }
                                }
                            });
                            return true;
                        }
                        return false;
                    }
                });
                // Mostrar el menú popup
                popup.show();
            }
        });
        recyclerView = findViewById(R.id.home_page_recycler);
        setupChatRecyclerView();
        getFirebaseInstanceToken();
    }

    void setupChatRecyclerView(){
        Query query = FirebaseUtil.allChatroomsCollectionReference().orderBy("lastMessageTimestamp", Query.Direction.DESCENDING).whereArrayContains("userIds", FirebaseUtil.currentUserId());

        FirestoreRecyclerOptions<ChatroomModel> options = new FirestoreRecyclerOptions.Builder<ChatroomModel>()
                .setQuery(query, ChatroomModel.class).build();
        adapter = new HomePageRecyclerAdapter(options,getApplicationContext());
        LinearLayoutManager manager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(adapter);
        adapter.startListening();
    }

    @Override
    protected void onStart() {//TODO check
        super.onStart();
        if(adapter!=null)
            adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(adapter!=null)
            adapter.stopListening();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(adapter!=null)
            adapter.notifyDataSetChanged();
    }

    void getFirebaseInstanceToken(){
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(token -> {
            FirebaseUtil.currentUserDetails().update("fcmToken",token);
        });
    }
}

