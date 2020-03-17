package com.clevertap.templateapp;

import android.app.Application;

import com.clevertap.android.sdk.ActivityLifecycleCallback;
import com.clevertap.android.sdk.CleverTapAPI;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        CleverTapAPI.setDebugLevel(3);
        ActivityLifecycleCallback.register(this);
        super.onCreate();
    }
}
