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
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class PushTemplateMessagingService extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        try{
            PTLog.debug("Inside Push Templates");
            Context context = getApplicationContext();
            if (remoteMessage.getData().size() > 0) {
                Bundle extras = new Bundle();
                for (Map.Entry<String, String> entry : remoteMessage.getData().entrySet()) {
                    extras.putString(entry.getKey(), entry.getValue());
                }

                boolean processCleverTapPN = Utils.isPNFromCleverTap(extras);

                if(processCleverTapPN){
                    //Sample code to render NT and NM for now. We build on top of this.
                    NotificationManager notificationManager =
                            (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
                    String channelId = extras.getString(Constants.WZRK_CHANNEL_ID, "");
                    String notifMessage = extras.getString(Constants.NOTIF_MSG);
                    String notifTitle = extras.getString(Constants.NOTIF_TITLE);
                    boolean requiresChannelId = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O;

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        String channelIdError = null;
                        if (channelId.isEmpty()) {
                            channelIdError = "Unable to render notification, channelId is required but not provided in the notification payload: " + extras.toString();
                        } else if (notificationManager != null && notificationManager.getNotificationChannel(channelId) == null) {
                            channelIdError = "Unable to render notification, channelId: " + channelId + " not registered by the app.";
                        }
                        if (channelIdError != null) {
                            PTLog.debug(channelIdError);
                            return;
                        }
                    }

                    Intent launchIntent = new Intent(context, CTPushNotificationReceiver.class);
                    launchIntent.putExtras(extras);
                    launchIntent.removeExtra(Constants.WZRK_ACTIONS);
                    launchIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    PendingIntent pIntent = PendingIntent.getBroadcast(context, (int) System.currentTimeMillis(),
                            launchIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                    NotificationCompat.Style style;
                    String bigPictureUrl = extras.getString(Constants.WZRK_BIG_PICTURE);
                    if (bigPictureUrl != null && bigPictureUrl.startsWith("http")) {
                        try {
                            Bitmap bpMap = Utils.getNotificationBitmap(bigPictureUrl, false, context);

                            if (bpMap == null)
                                throw new Exception("Failed to fetch big picture!");

                            if(extras.containsKey(Constants.WZRK_MSG_SUMMARY)){
                                String summaryText = extras.getString(Constants.WZRK_MSG_SUMMARY);
                                style = new NotificationCompat.BigPictureStyle()
                                        .setSummaryText(summaryText)
                                        .bigPicture(bpMap);
                            }else {
                                style = new NotificationCompat.BigPictureStyle()
                                        .setSummaryText(notifMessage)
                                        .bigPicture(bpMap);
                            }
                        } catch (Throwable t) {
                            style = new NotificationCompat.BigTextStyle()
                                    .bigText(notifMessage);
                            PTLog.verbose( "Falling back to big text notification, couldn't fetch big picture", t);
                        }
                    } else {
                        style = new NotificationCompat.BigTextStyle()
                                .bigText(notifMessage);
                    }

                    int smallIcon = 0;
                    try {
                        Bundle metaData = null;
                        try {
                            PackageManager pm = context.getPackageManager();
                            ApplicationInfo ai = pm.getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
                            metaData = ai.metaData;
                        } catch (Throwable t) {
                            // no-op
                        }
                        String x = Utils._getManifestStringValueForKey(metaData,Constants.LABEL_NOTIFICATION_ICON);
                        if (x == null) throw new IllegalArgumentException();
                        smallIcon = context.getResources().getIdentifier(x, "drawable", context.getPackageName());
                        if (smallIcon == 0) throw new IllegalArgumentException();
                    } catch (Throwable t) {
                        PTLog.error("Small Icon not found in AndroidManifest.xml!!");
                        smallIcon = Utils.getAppIconAsIntId(context);
                    }

                    int priorityInt = NotificationCompat.PRIORITY_DEFAULT;
                    String priority = extras.getString(Constants.NOTIF_PRIORITY);
                    if (priority != null) {
                        if (priority.equals(Constants.PRIORITY_HIGH)) {
                            priorityInt = NotificationCompat.PRIORITY_HIGH;
                        }
                        if (priority.equals(Constants.PRIORITY_MAX)) {
                            priorityInt = NotificationCompat.PRIORITY_MAX;
                        }
                    }

                    NotificationCompat.Builder nb;

                    if (requiresChannelId) {
                        nb = new NotificationCompat.Builder(context, channelId);

                        // choices here are Notification.BADGE_ICON_NONE = 0, Notification.BADGE_ICON_SMALL = 1, Notification.BADGE_ICON_LARGE = 2.  Default is  Notification.BADGE_ICON_LARGE
                        String badgeIconParam = extras.getString(Constants.WZRK_BADGE_ICON, null);
                        if (badgeIconParam != null) {
                            try {
                                int badgeIconType = Integer.parseInt(badgeIconParam);
                                if (badgeIconType >=0) {
                                    nb.setBadgeIconType(badgeIconType);
                                }
                            } catch (Throwable t) {
                                // no-op
                            }
                        }

                        String badgeCountParam = extras.getString(Constants.WZRK_BADGE_COUNT, null);
                        if (badgeCountParam != null) {
                            try {
                                int badgeCount = Integer.parseInt(badgeCountParam);
                                if (badgeCount >= 0) {
                                    nb.setNumber(badgeCount);
                                }
                            } catch (Throwable t) {
                                // no-op
                            }
                        }
                        if(extras.containsKey(Constants.WZRK_SUBTITLE)){
                            nb.setSubText(extras.getString(Constants.WZRK_SUBTITLE));
                        }
                    } else {
                        nb = new NotificationCompat.Builder(context);
                    }

                    int notificationId = (int) (Math.random() * 100);

                    nb.setContentTitle(notifTitle)
                            .setContentText(notifMessage)
                            .setContentIntent(pIntent)
                            .setAutoCancel(true)
                            .setStyle(style)
                            .setSmallIcon(smallIcon)
                            .setPriority(priorityInt);
                    Notification n = nb.build();
                    if (notificationManager != null) {
                        notificationManager.notify(notificationId, n);
                    }

                }
            }
        }catch (Throwable throwable){
            PTLog.error("Error parsing FCM payload",throwable);
        }
    }
}
