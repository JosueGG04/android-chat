package com.jb.proyectoandroid.utils;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
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

    public static CollectionReference allUserCollectionReference(){
        return FirebaseFirestore.getInstance().collection("users");
    }
}
