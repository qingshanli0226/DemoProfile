package com.example.bottombar.demoprofile;

import android.app.Application;

public class ProfileApplication extends Application {
    public static ProfileApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }
}
