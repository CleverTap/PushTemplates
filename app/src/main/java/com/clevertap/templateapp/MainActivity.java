package com.clevertap.templateapp;

import android.app.NotificationManager;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.clevertap.android.sdk.CleverTapAPI;

public class MainActivity extends AppCompatActivity {

    Button sendPush;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        CleverTapAPI.setDebugLevel(3);
        final CleverTapAPI cleverTapAPI = CleverTapAPI.getDefaultInstance(getApplicationContext());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            CleverTapAPI.createNotificationChannel(this,"PTTesting","Push Template App Channel","Channel for Push Template App", NotificationManager.IMPORTANCE_HIGH,true);
        }
        sendPush = findViewById(R.id.button);
        sendPush.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cleverTapAPI != null) {
                    cleverTapAPI.pushEvent("Send Push");
                }
            }
        });
    }
}
