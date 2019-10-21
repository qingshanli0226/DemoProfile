package com.example.bottombar.demoprofile;

import android.content.Context;
import android.content.SharedPreferences;

public class AccountManager {
    private static AccountManager instance;
    private final String TOKEN = "token";

    private AccountManager() {

    }
    public static AccountManager getInstance(){
        if (instance == null) {
            instance = new AccountManager();
        }

        return instance;
    }

    public void saveToken(String token) {
        SharedPreferences sp = ProfileApplication.instance.getSharedPreferences(TOKEN, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();

        editor.putString(TOKEN, token).commit();

    }
}
