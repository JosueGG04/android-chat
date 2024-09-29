package com.jb.proyectoandroid;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.Query;
import com.jb.proyectoandroid.adapter.SearchUserRecyclerAdapter;
import com.jb.proyectoandroid.model.UserModel;

public class NewChatSearchActivity extends AppCompatActivity {

    EditText searchInput;
    ImageButton searchButton;
    ImageButton backButton;
    RecyclerView recyclerView;

    SearchUserRecyclerAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_new_chat_search);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        searchInput = findViewById(R.id.search_email_input);
        searchButton = findViewById(R.id.search_email_btn);
        backButton = findViewById(R.id.back_btn);
        recyclerView = findViewById(R.id.search_email_recycler_view);

        searchInput.requestFocus();

        backButton.setOnClickListener(v ->{
            getOnBackPressedDispatcher().onBackPressed();
        });

        searchButton.setOnClickListener(v ->{
            String searchTerm = searchInput.getText().toString();
            if(searchTerm.isEmpty()){
                searchInput.setError("Email invalido");
                return;
            }
            setupSearchRecyclerView(searchTerm);
        });
    }

    private void setupSearchRecyclerView(String searchTerm) {

        Query query = FirebaseUtils.allUserCollectionReference()
                .whereGreaterThanOrEqualTo("email",searchTerm).whereLessThanOrEqualTo("email", searchTerm + "\uf8ff");

        FirestoreRecyclerOptions<UserModel> options = new FirestoreRecyclerOptions.Builder<UserModel>()
                .setQuery(query, UserModel.class).build();
        adapter = new SearchUserRecyclerAdapter(options,getApplicationContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        adapter.startListening();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(adapter!=null)
            adapter.startListening();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(adapter!=null)
            adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(adapter!=null)
            adapter.stopListening();
    }
}