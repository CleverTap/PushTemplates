package com.clevertap.pushtemplates;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.widget.RelativeLayout;
import android.widget.RemoteViews;

import com.clevertap.android.sdk.CTPushNotificationReceiver;
import com.clevertap.android.sdk.CleverTapAPI;

import java.net.URL;
import java.util.ArrayList;

import static android.content.Context.NOTIFICATION_SERVICE;

class TemplateRenderer {

    private String pt_id;
    private TemplateType templateType;
    private String pt_title;
    private String pt_msg;
    private String pt_img_small;
    private String pt_img_big;
    private ArrayList<String> imageList = new ArrayList<>();
    private ArrayList<String> ctaList = new ArrayList<>();
    private ArrayList<String> deepLinkList = new ArrayList<>();
    private String pt_bg;

    private RemoteViews contentViewBig, contentViewSmall;
    private String channelId;
    private int smallIcon = 0;
    private boolean requiresChannelId;
    private NotificationManager notificationManager;

    private TemplateRenderer(Bundle extras) {
        pt_id = extras.getString(Constants.PT_ID);
        if (pt_id != null) {
            templateType = TemplateType.fromString(pt_id);
        }
        pt_msg = extras.getString(Constants.PT_MSG);
        pt_title = extras.getString(Constants.PT_TITLE);
        pt_bg = extras.getString(Constants.PT_BG);
        pt_img_big = extras.getString(Constants.PT_IMG_BIG);
        pt_img_small = extras.getString(Constants.PT_IMG_SMALL);
        imageList = Utils.getImageListFromExtras(extras);
        ctaList = Utils.getCTAListFromExtras(extras);
        deepLinkList = Utils.getDeepLinkListFromExtras(extras);
    }

    static void createNotification(Context context, Bundle extras){
        TemplateRenderer templateRenderer = new TemplateRenderer(extras);
        templateRenderer._createNotification(context,extras,Constants.EMPTY_NOTIFICATION_ID);
    }

    private void _createNotification(Context context, Bundle extras, int notificationId){
        if(pt_id == null){
            PTLog.error("Template ID not provided. Cannot create the notification");
            return;
        }

        notificationManager = (NotificationManager)context.getSystemService(NOTIFICATION_SERVICE);
        channelId = extras.getString(Constants.WZRK_CHANNEL_ID, "");
        requiresChannelId = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelIdError = null;
            if (channelId.isEmpty()) {
                channelIdError = "Unable to render notification, channelId is required but not provided in the notification payload: " + extras.toString();
            } else if (notificationManager != null && notificationManager.getNotificationChannel(channelId) == null) {
                channelIdError = "Unable to render notification, channelId: " + channelId + " not registered by the app.";
            }
            if (channelIdError != null) {
                PTLog.error(channelIdError);
                return;
            }
        }

        Bundle metaData;
        try {
            PackageManager pm = context.getPackageManager();
            ApplicationInfo ai = pm.getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            metaData = ai.metaData;
            String x = Utils._getManifestStringValueForKey(metaData,Constants.LABEL_NOTIFICATION_ICON);
            if (x == null) throw new IllegalArgumentException();
            smallIcon = context.getResources().getIdentifier(x, "drawable", context.getPackageName());
            if (smallIcon == 0) throw new IllegalArgumentException();
        } catch (Throwable t) {
            smallIcon = Utils.getAppIconAsIntId(context);
        }

        switch (templateType){
            case IMAGE_ONLY:
                renderImageOnlyNotification(context, extras, notificationId);
                break;
            case AUTO_CAROUSEL:
                break;
            case RATING:
                break;
            case FIVE_ICONS:
                break;
        }
    }

    private void renderImageOnlyNotification(Context context, Bundle extras, int notificationId){
        try{
            contentViewBig = new RemoteViews(context.getPackageName(), R.layout.image_only_big);
            contentViewSmall = new RemoteViews(context.getPackageName(),R.layout.image_only_small);
            if(pt_img_big!=null && !pt_img_big.isEmpty()) {
                URL bigImgUrl = new URL(pt_img_big);
                contentViewBig.setImageViewBitmap(R.id.image_pic, BitmapFactory.decodeStream(bigImgUrl.openConnection().getInputStream()));

            }

            if(pt_img_small!=null && !pt_img_small.isEmpty()) {
                URL smallImgUrl = new URL(pt_img_small);
                contentViewSmall.setImageViewBitmap(R.id.small_image_app, BitmapFactory.decodeStream(smallImgUrl.openConnection().getInputStream()));
                contentViewBig.setImageViewBitmap(R.id.big_image_app, BitmapFactory.decodeStream(smallImgUrl.openConnection().getInputStream()));
            }

            if(pt_title!=null && !pt_title.isEmpty()) {
                contentViewBig.setTextViewText(R.id.title, pt_title);
                contentViewSmall.setTextViewText(R.id.title, pt_title);
            }

            if(pt_msg!=null && !pt_msg.isEmpty()) {
                contentViewBig.setTextViewText(R.id.msg, pt_msg);
                contentViewSmall.setTextViewText(R.id.msg, pt_msg);
            }

            if(pt_bg!=null && !pt_bg.isEmpty()){
                contentViewBig.setInt(R.id.image_only_big_relative_layout,"setBackgroundColor", Color.parseColor(pt_bg));
                contentViewSmall.setInt(R.id.image_only_small_relative_layout,"setBackgroundColor", Color.parseColor(pt_bg));
            }

            if (notificationId == Constants.EMPTY_NOTIFICATION_ID) {
                notificationId = (int) (Math.random() * 100);
            }

            Intent launchIntent = new Intent(context, CTPushNotificationReceiver.class);
            launchIntent.putExtras(extras);
            launchIntent.removeExtra(Constants.WZRK_ACTIONS);
            launchIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent pIntent = PendingIntent.getBroadcast(context, (int) System.currentTimeMillis(),
                    launchIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            NotificationCompat.Builder notificationBuilder;
            if(requiresChannelId) {
                notificationBuilder = new NotificationCompat.Builder(context, channelId);
            }else{
                notificationBuilder = new NotificationCompat.Builder(context);
            }

            notificationBuilder.setSmallIcon(smallIcon)
                    .setCustomContentView(contentViewSmall)
                    .setCustomBigContentView(contentViewBig)
                    .setContentTitle("Custom Notification")
                    .setContentIntent(pIntent)
                    .setAutoCancel(true);

            notificationManager.notify(notificationId, notificationBuilder.build());
            CleverTapAPI instance = CleverTapAPI.getDefaultInstance(context);
            if (instance != null) {
                instance.pushNotificationViewedEvent(extras);
            }
        }catch (Throwable t){
            PTLog.error("Error creating image only notification", t);
        }
    }

}
