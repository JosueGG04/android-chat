package com.jb.proyectoandroid.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.jb.proyectoandroid.ChatActivity;
import com.jb.proyectoandroid.R;
import com.jb.proyectoandroid.model.ChatroomModel;
import com.jb.proyectoandroid.model.UserModel;
import com.jb.proyectoandroid.utils.AndroidUtil;
import com.jb.proyectoandroid.utils.FirebaseUtil;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HomePageRecyclerAdapter extends FirestoreRecyclerAdapter<ChatroomModel, HomePageRecyclerAdapter.ChatroomModelViewHolder> {
    Context context;

    public HomePageRecyclerAdapter(@NonNull FirestoreRecyclerOptions<ChatroomModel> options, Context context) {
        super(options);
        this.context = context;
    }

    @Override
    protected void onBindViewHolder(@NonNull ChatroomModelViewHolder holder, int position, @NonNull ChatroomModel model) {
        //name
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
                        String name = user.getEmail();
                        name += name.equals(currentUserId) ? " (Yo)" : "";
                        holder.chatNameView.setText(name);
                        holder.itemView.setOnClickListener(v -> {
                            //go to chat activity
                            Intent intent = new Intent(context, ChatActivity.class);
                            AndroidUtil.passUserModelAsIntent(intent,user);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            context.startActivity(intent);
                        });
                    }
                }else{
                    Log.d("Firestore","user doesnt exist");
                }
            }
        });
        //lastmessage
        holder.chatLastMessage.setText(model.getLastMessage());
        //timeStamp
        Timestamp lastMessageTime = model.getLastMessageTimestamp();

        Calendar lastMessageCalendar = Calendar.getInstance();
        lastMessageCalendar.setTimeInMillis(lastMessageTime.getNanoseconds());

        Calendar yesterday = Calendar.getInstance();
        yesterday.add(Calendar.DAY_OF_YEAR, -1);
        Date date = lastMessageTime.toDate();

        String formattedTime;
        if (lastMessageCalendar.get(Calendar.YEAR) == yesterday.get(Calendar.YEAR) &&
                lastMessageCalendar.get(Calendar.DAY_OF_YEAR) == yesterday.get(Calendar.DAY_OF_YEAR)) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            formattedTime = sdf.format(date);
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
            formattedTime = sdf.format(date);
        }
        holder.chatTimeStamp.setText(formattedTime);
    }

    @NonNull
    @Override
    public ChatroomModelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.chatroom_recycler_row,parent,false);
        return new ChatroomModelViewHolder(view);
    }

    class ChatroomModelViewHolder extends RecyclerView.ViewHolder {
        TextView chatNameView,chatTimeStamp,chatLastMessage;
        ImageView chatImage;

        public ChatroomModelViewHolder(@NonNull View itemView) {
            super(itemView);
            chatNameView = itemView.findViewById(R.id.chatroom_email);
            chatTimeStamp = itemView.findViewById(R.id.chatroom_timestamp);
            chatLastMessage = itemView.findViewById(R.id.chat_last_message_text);
            chatImage = itemView.findViewById(R.id.chat_pic_image_view);

        }
    }
}
