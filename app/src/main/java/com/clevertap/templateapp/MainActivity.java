package com.clevertap.templateapp;

import android.app.NotificationManager;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.clevertap.android.sdk.CleverTapAPI;

public class MainActivity extends AppCompatActivity {

    Button sendBasicPush, sendCarouselPush, sendRatingPush, sendProductDisplayNotificaiton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        CleverTapAPI.setDebugLevel(3);
        final CleverTapAPI cleverTapAPI = CleverTapAPI.getDefaultInstance(getApplicationContext());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            CleverTapAPI.createNotificationChannel(this,"PTTesting","Push Template App Channel","Channel for Push Template App", NotificationManager.IMPORTANCE_HIGH,true);
        }

        sendBasicPush = findViewById(R.id.basicPush);
        sendBasicPush.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cleverTapAPI != null) {
                    cleverTapAPI.pushEvent("Send Basic Push");
                }
            }
        });

        sendCarouselPush = findViewById(R.id.carouselPush);
        sendCarouselPush.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cleverTapAPI != null) {
                    cleverTapAPI.pushEvent("Send Carousel Push");
                }
            }
        });

        sendRatingPush = findViewById(R.id.ratingPush);
        sendRatingPush.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cleverTapAPI != null) {
                    cleverTapAPI.pushEvent("Send Rating Push");
                }
            }
        });

        sendProductDisplayNotificaiton = findViewById(R.id.productDisplay);
        sendProductDisplayNotificaiton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cleverTapAPI != null) {
                    cleverTapAPI.pushEvent("Send Product Display Notification");
                }
            }
        });

    }
}
