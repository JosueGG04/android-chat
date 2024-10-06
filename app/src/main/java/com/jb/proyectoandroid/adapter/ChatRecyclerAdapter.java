package com.jb.proyectoandroid.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.jb.proyectoandroid.model.UserModel;
import com.jb.proyectoandroid.utils.FirebaseUtil;
import com.jb.proyectoandroid.R;
import com.jb.proyectoandroid.model.ChatMessageModel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ChatRecyclerAdapter extends FirestoreRecyclerAdapter<ChatMessageModel, ChatRecyclerAdapter.ChatModelViewHolder> {
    Context context;

    public ChatRecyclerAdapter(@NonNull FirestoreRecyclerOptions<ChatMessageModel> options, Context context) {
        super(options);
        this.context = context;
    }

    @Override
    protected void onBindViewHolder(@NonNull ChatModelViewHolder holder, int position, @NonNull ChatMessageModel model) {
        //email del usuario que envio
        FirebaseUtil.getUserReference(model.getSenderId()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot.exists()) {
                    UserModel sender = documentSnapshot.toObject(UserModel.class);
                    if(sender!=null){
                        holder.messageEmail.setText(sender.getEmail());
                    }
                }else{
                    Log.d("Firestore","sender doesnt exist");
                }
            }
        });
        //timestamp
        Date date = model.getTimestamp().toDate();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        String formattedTime = sdf.format(date);
        holder.messageTimestamp.setText(formattedTime);
        //message
        holder.messageText.setText(model.getMessage());
        //image
        if(model.getImagePath()!=null && !model.getImagePath().isEmpty()) {
            holder.imageVw.setVisibility(View.VISIBLE);
            Glide.with(context).load(model.getImagePath()).into(holder.imageVw);
        }
    }




    @NonNull
    @Override
    public ChatModelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.chat_message_recycler_row,parent,false);
        return new ChatModelViewHolder(view);
    }

    class ChatModelViewHolder extends RecyclerView.ViewHolder {
        TextView messageText,messageTimestamp,messageEmail;
        ImageView imageVw;

        public ChatModelViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.message_text);
            messageTimestamp = itemView.findViewById(R.id.message_timestamp);
            messageEmail = itemView.findViewById(R.id.message_email);
            imageVw = itemView.findViewById(R.id.imgVw_message);
        }
    }
}
