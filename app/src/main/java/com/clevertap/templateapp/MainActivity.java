package com.clevertap.templateapp;

import android.app.NotificationManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.clevertap.android.sdk.CleverTapAPI;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    Button sendBasicPush, sendCarouselPush, sendRatingPush, sendProductDisplayNotification, sendCTANotification, sendTimerNotification;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final CleverTapAPI cleverTapAPI = CleverTapAPI.getDefaultInstance(getApplicationContext());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            CleverTapAPI.createNotificationChannel(this,"Test","Push Template App Channel","Channel for Push Template App", NotificationManager.IMPORTANCE_HIGH,true);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            CleverTapAPI.createNotificationChannel(this,"PTTesting","Push Template App Channel","Channel for Push Template App", NotificationManager.IMPORTANCE_HIGH,true);
        }
        HashMap<String, Object> profileUpdate = new HashMap<String, Object>();
        profileUpdate.put("Email", "test1@clevertap.com");
        if (cleverTapAPI != null) {
            cleverTapAPI.onUserLogin(profileUpdate);
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

        sendProductDisplayNotification = findViewById(R.id.productDisplay);
        sendProductDisplayNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cleverTapAPI != null) {
                    cleverTapAPI.pushEvent("Send Product Display Notification");
                }
            }
        });

        sendCTANotification = findViewById(R.id.cta);
        sendCTANotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cleverTapAPI != null) {
                    cleverTapAPI.pushEvent("Send CTA Notification");
                }
            }
        });

        sendTimerNotification = findViewById(R.id.timer);
        sendTimerNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cleverTapAPI != null) {
                    cleverTapAPI.pushEvent("Send Timer Notification");
                }
            }
        });
    }
}