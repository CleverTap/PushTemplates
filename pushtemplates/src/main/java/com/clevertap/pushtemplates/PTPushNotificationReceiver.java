package com.clevertap.pushtemplates;

import android.content.Context;
import android.content.Intent;

import com.clevertap.android.sdk.CTPushNotificationReceiver;

public class PTPushNotificationReceiver extends CTPushNotificationReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        Utils.deleteSilentNotificationChannel(context);
    }
}
