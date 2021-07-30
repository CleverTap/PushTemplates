package com.clevertap.templateapp;

import android.app.NotificationManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.clevertap.android.sdk.CleverTapAPI;
import com.clevertap.pushtemplates.TemplateRenderer;
import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    Button sendBasicPush, sendCarouselPush,sendManualCarouselPush,sendFilmCarouselPush,
            sendRatingPush, sendProductDisplayNotification,
            sendLinearProductDisplayNotification,
            sendCTANotification, sendZeroBezel, sendTimerNotification,
            sendInputBoxNotification;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TemplateRenderer.setDebugLevel(3);
        final CleverTapAPI cleverTapAPI = CleverTapAPI.getDefaultInstance(getApplicationContext());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            CleverTapAPI.createNotificationChannel(this,"Test","Push Template App Channel","Channel for Push Template App", NotificationManager.IMPORTANCE_HIGH,true);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            CleverTapAPI.createNotificationChannel(this,"PTTesting","Push Template App Channel","Channel for Push Template App", NotificationManager.IMPORTANCE_HIGH,true);
        }

        sendBasicPush = findViewById(R.id.basicPush);
        sendBasicPush.setOnClickListener(v -> {
            if (cleverTapAPI != null) {
                cleverTapAPI.pushEvent("Send Basic Push");
                cleverTapAPI.pushChargedEvent(new HashMap<>(),new ArrayList<>());
            }
        });

        sendCarouselPush = findViewById(R.id.carouselPush);
        sendCarouselPush.setOnClickListener(v -> {
            if (cleverTapAPI != null) {
                cleverTapAPI.pushEvent("Send Auto Carousel Push");
            }
        });

        sendManualCarouselPush = findViewById(R.id.manualCarouselPush);
        sendManualCarouselPush.setOnClickListener(v -> {
            if (cleverTapAPI != null) {
                cleverTapAPI.pushEvent("Send Manual Carousel Push");

            }
        });

        sendFilmCarouselPush = findViewById(R.id.filmCarouselPush);
        sendFilmCarouselPush.setOnClickListener(v -> {
            if (cleverTapAPI != null) {
                cleverTapAPI.pushEvent("Send Filmstrip Carousel Push");
            }
        });


        sendRatingPush = findViewById(R.id.ratingPush);
        sendRatingPush.setOnClickListener(v -> {
            if (cleverTapAPI != null) {
                cleverTapAPI.pushEvent("Send Rating Push");
            }
        });

        sendProductDisplayNotification = findViewById(R.id.productDisplay);
        sendProductDisplayNotification.setOnClickListener(v -> {
            if (cleverTapAPI != null) {
                cleverTapAPI.pushEvent("Send Product Display Notification");

            }
        });

        sendLinearProductDisplayNotification = findViewById(R.id.linearProductDisplay);
        sendLinearProductDisplayNotification.setOnClickListener(v -> {
            if (cleverTapAPI != null) {
                cleverTapAPI.pushEvent("Send Linear Product Display Push");
            }
        });

        sendCTANotification = findViewById(R.id.cta);
        sendCTANotification.setOnClickListener(v -> {
            if (cleverTapAPI != null) {
                cleverTapAPI.pushEvent("Send CTA Notification");
            }
        });

        sendZeroBezel = findViewById(R.id.zero_bezel);
        sendZeroBezel.setOnClickListener(v -> {
            if (cleverTapAPI != null) {
                cleverTapAPI.pushEvent("Send Zero Bezel Notification");
            }
        });

        sendTimerNotification = findViewById(R.id.timer);
        sendTimerNotification.setOnClickListener(v -> {
            if (cleverTapAPI != null) {
                cleverTapAPI.pushEvent("Send Timer Notification");
            }
        });

        sendInputBoxNotification = findViewById(R.id.inputBox);
        sendInputBoxNotification.setOnClickListener(v -> {
            if (cleverTapAPI != null) {
                cleverTapAPI.pushEvent("Send Input Box Notification");
            }
        });
    }
}