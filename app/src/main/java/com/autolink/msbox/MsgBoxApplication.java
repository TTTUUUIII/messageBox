package com.autolink.msbox;

import android.app.Application;

public class MsgBoxApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        DataRepository.initialize(this);
    }
}
