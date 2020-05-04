package com.example.mosis;

import android.app.Application;

public class UserAuth extends Application {

    private UserModel user = null;
    public UserModel getUser() {return user;};
    public void setUser(UserModel user) {this.user = user;}
}
