package com.clevertap.pushtemplates;

import android.content.Context;
import android.os.Bundle;
import com.clevertap.android.sdk.interfaces.NotificationHandler;

public class PushTemplateMessagingService implements NotificationHandler {

    @Override
    public boolean onMessageReceived(final Context applicationContext, final Bundle message, final String pushType) {
        try {
            PTLog.debug("Inside Push Templates");
            TemplateRenderer.createNotification(applicationContext, message);

        } catch (Throwable throwable) {
            PTLog.verbose("Error parsing FCM payload", throwable);
        }
        return true;
    }

    @Override
    public boolean onNewToken(final Context applicationContext, final String token, final String pushType) {
        return true;
    }

}
