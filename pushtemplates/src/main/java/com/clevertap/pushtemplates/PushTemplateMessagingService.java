package com.clevertap.pushtemplates;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import com.clevertap.android.sdk.CleverTapAPI;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class PushTemplateMessagingService extends FirebaseMessagingService {

    Context context;

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        try {
            PTLog.debug("Inside Push Templates");
            context = getApplicationContext();
            if (remoteMessage.getData().size() > 0) {
                Bundle extras = new Bundle();
                for (Map.Entry<String, String> entry : remoteMessage.getData().entrySet()) {
                    extras.putString(entry.getKey(), entry.getValue());
                }

                boolean processCleverTapPN = Utils.isPNFromCleverTap(extras);

                if (processCleverTapPN) {
                    if (Utils.isForPushTemplates(extras)) {
                        TemplateRenderer.createNotification(context, extras);
                    } else {
                        CleverTapAPI.createNotification(context, extras);
                    }
                }
            }
        } catch (Throwable throwable) {
            PTLog.verbose("Error parsing FCM payload", throwable);
        }
    }

    @Override
    public void onNewToken(@NonNull final String s) {

    }
}
