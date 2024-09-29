package com.jb.proyectoandroid.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.jb.proyectoandroid.ChatActivity;
import com.jb.proyectoandroid.utils.AndroidUtil;
import com.jb.proyectoandroid.utils.FirebaseUtil;
import com.jb.proyectoandroid.R;
import com.jb.proyectoandroid.model.UserModel;

public class SearchUserRecyclerAdapter extends FirestoreRecyclerAdapter<UserModel, SearchUserRecyclerAdapter.UserModelViewHolder> {
    Context context;

    public SearchUserRecyclerAdapter(@NonNull FirestoreRecyclerOptions<UserModel> options, Context context) {
        super(options);
        this.context = context;
    }

    @Override
    protected void onBindViewHolder(@NonNull UserModelViewHolder holder, int position, @NonNull UserModel model) {
        holder.emailText.setText(model.getEmail());
        if(model.getUserID().equals(FirebaseUtil.currentUserId())){
            holder.emailText.setText(model.getEmail() + " (Yo)");
        }

        holder.itemView.setOnClickListener(v -> {
            //go to chat activity
            Intent intent = new Intent(context, ChatActivity.class);
            AndroidUtil.passUserModelAsIntent(intent,model);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        });
    }

    @NonNull
    @Override
    public UserModelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.search_email_recycler_row,parent,false);
        return new UserModelViewHolder(view);
    }

    class UserModelViewHolder extends RecyclerView.ViewHolder {
        TextView emailText;

        public UserModelViewHolder(@NonNull View itemView) {
            super(itemView);
            emailText = itemView.findViewById(R.id.user_email_text);
        }
    }
}
