package com.jb.proyectoandroid.model;

public class UserModel {
    private String email;
    private String userID;
    private String fcmToken;

    public UserModel(){

    }

    public UserModel(String email, String userID) {
        this.email = email;
        this.userID = userID;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUserID() {
        return userID;
    }

    public String getFcmToken() {
        return fcmToken;
    }

    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }


}
