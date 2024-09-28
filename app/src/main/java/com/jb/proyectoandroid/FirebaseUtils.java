package com.jb.proyectoandroid;

import com.google.firebase.auth.FirebaseAuth;

public class FirebaseUtils {
    //returns the UID of the logged user
    public static String currentUserId(){
        return FirebaseAuth.getInstance().getUid();
    }
    //returns true is there is a logged user
    public static boolean isLoggedIn(){
        return currentUserId() != null;
    }

}
