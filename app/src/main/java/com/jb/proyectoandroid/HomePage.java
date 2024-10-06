package com.jb.proyectoandroid;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.PopupMenu;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;

public class HomePage extends AppCompatActivity {

    ImageButton newChatButton;
    ImageButton menuButton;

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
                            //logout
                            auth.signOut();
                            startActivity(new Intent(HomePage.this,MainActivity.class));
                            return true;
                        }
                        return false;
                    }
                });
                // Mostrar el menú popup
                popup.show();
            }
        });
    }

}