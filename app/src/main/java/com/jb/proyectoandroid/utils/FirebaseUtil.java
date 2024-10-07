package com.jb.proyectoandroid.utils;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class FirebaseUtil {
    //returns the UID of the logged user
    public static String currentUserId(){
        return FirebaseAuth.getInstance().getUid();
    }
    //returns true is there is a logged user
    public static boolean isLoggedIn(){
        return currentUserId() != null;
    }

    public static DocumentReference getUserReference(String userId){
        return FirebaseFirestore.getInstance().collection("users").document(userId);
    }
    public static DocumentReference currentUserDetails(){
        return FirebaseFirestore.getInstance().collection("users").document(currentUserId());
    }

    public static CollectionReference allUserCollectionReference(){
        return FirebaseFirestore.getInstance().collection("users");
    }

    public static DocumentReference getChatroomReference(String chatroomId){
        return FirebaseFirestore.getInstance().collection("chatrooms").document(chatroomId);
    }

    public static CollectionReference getChatroomMessageReference(String chatroomId){
        return getChatroomReference(chatroomId).collection("chats");
    }

    public static String getChatroomId(String userId1, String userId2){//TODO this needs to change for group implementation
        if(userId1.hashCode()<userId2.hashCode()){
            return userId1+"_"+userId2;
        } else{
            return userId2+"_"+userId1;
        }
    }
}
