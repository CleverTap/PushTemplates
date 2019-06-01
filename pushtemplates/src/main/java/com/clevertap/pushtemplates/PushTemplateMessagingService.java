package com.clevertap.pushtemplates;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.clevertap.android.sdk.CTPushNotificationReceiver;
import com.clevertap.android.sdk.CleverTapAPI;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class PushTemplateMessagingService extends FirebaseMessagingService {

    Context context;
    CleverTapAPI instance;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        try{
            PTLog.debug("Inside Push Templates");
            context = getApplicationContext();
            if (remoteMessage.getData().size() > 0) {
                Bundle extras = new Bundle();
                for (Map.Entry<String, String> entry : remoteMessage.getData().entrySet()) {
                    extras.putString(entry.getKey(), entry.getValue());
                }

                instance = CleverTapAPI.getDefaultInstance(getApplicationContext());

                boolean processCleverTapPN = Utils.isPNFromCleverTap(extras);

                if(processCleverTapPN){
                    String pt_id = extras.getString(Constants.PT_ID);
                    if(("0").equals(pt_id)){
                        CleverTapAPI.createNotification(context,extras);
                    }else{
                        TemplateRenderer.createNotification(context,extras);
                        if (instance != null) {
                            instance.pushNotificationViewedEvent(extras);
                        }
                    }
                }
            }
        }catch (Throwable throwable){
            PTLog.error("Error parsing FCM payload",throwable);
        }
    }
}
