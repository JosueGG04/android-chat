package com.jb.proyectoandroid.utils;

import android.content.Intent;

import com.jb.proyectoandroid.model.UserModel;

public class AndroidUtil {
    public static void passUserModelAsIntent(Intent intent, UserModel model){
        intent.putExtra("email",model.getEmail());
        intent.putExtra("userId",model.getUserID());
    }
}
