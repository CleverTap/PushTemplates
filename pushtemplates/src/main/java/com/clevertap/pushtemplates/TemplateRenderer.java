package com.clevertap.pushtemplates;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import android.text.Html;
import android.widget.RemoteViews;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.NotificationTarget;
import com.bumptech.glide.request.transition.Transition;
import com.clevertap.android.sdk.CTPushNotificationReceiver;
import com.clevertap.android.sdk.CleverTapAPI;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

import static android.content.Context.NOTIFICATION_SERVICE;

class TemplateRenderer {

    private String pt_id,pt_json;
    private TemplateType templateType;
    private String pt_title;
    private String pt_msg;
    private String pt_img_small;
    private String pt_img_big;
    private String pt_title_clr,pt_msg_clr;
    private ArrayList<String> imageList = new ArrayList<>();
    private ArrayList<String> ctaList = new ArrayList<>();
    private ArrayList<String> deepLinkList = new ArrayList<>();
    private ArrayList<String> bigTextList = new ArrayList<>();
    private ArrayList<String> smallTextList = new ArrayList<>();
    private String pt_bg;
    private String pt_close;

    private RemoteViews contentViewBig, contentViewSmall, contentViewCarousel, contentViewRating, contentViewProductDisplay, contentFiveCTAs;
    private String channelId;
    private int smallIcon = 0;
    private boolean requiresChannelId;
    private NotificationManager notificationManager;

    private TemplateRenderer(Context context, Bundle extras) {
        pt_id = extras.getString(Constants.PT_ID);
        pt_json = extras.getString(Constants.PT_JSON);
        if (pt_id != null) {
            templateType = TemplateType.fromString(pt_id);
            Bundle newExtras = fromJson(fromTest(context, pt_json));
            extras.putAll(newExtras);
        }
        pt_msg = extras.getString(Constants.PT_MSG);
        pt_msg_clr = extras.getString(Constants.PT_MSG_COLOR);
        pt_title = extras.getString(Constants.PT_TITLE);
        pt_title_clr =extras.getString(Constants.PT_TITLE_COLOR);
        pt_bg = extras.getString(Constants.PT_BG);
        pt_img_big = extras.getString(Constants.PT_BIG_IMG);
        pt_img_small = extras.getString(Constants.PT_SMALL_IMG);
        imageList = Utils.getImageListFromExtras(extras);
        ctaList = Utils.getCTAListFromExtras(extras);
        deepLinkList = Utils.getDeepLinkListFromExtras(extras);
        bigTextList = Utils.getBigTextFromExtras(extras);
        smallTextList = Utils.getSmallTextFromExtras(extras);
        pt_close = extras.getString(Constants.PT_CLOSE);
    }

    static void createNotification(Context context, Bundle extras){
        TemplateRenderer templateRenderer = new TemplateRenderer(context, extras);
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
            case BASIC:
                renderBasicTemplateNotification(context, extras, notificationId);
                break;
            case AUTO_CAROUSEL:
                renderAutoCarouselNotification(context, extras, notificationId);
                break;
            case RATING:
                renderRatingCarouselNotification(context,extras,notificationId);
                break;
            case FIVE_ICONS:
                renderFiveIconNotification(context,extras,notificationId);
                break;
            case PRODUCT_DISPLAY:
                renderProductDisplayNotification(context, extras, notificationId);
                break;
        }
    }



    private void renderRatingCarouselNotification(Context context, Bundle extras, int notificationId){
        try{
            contentViewRating = new RemoteViews(context.getPackageName(),R.layout.rating);
            contentViewSmall = new RemoteViews(context.getPackageName(),R.layout.image_only_small);

            if(pt_title!=null && !pt_title.isEmpty()) {
                contentViewRating.setTextViewText(R.id.title, pt_title);
                contentViewSmall.setTextViewText(R.id.title, pt_title);
            }

            if(pt_msg!=null && !pt_msg.isEmpty()) {
                contentViewRating.setTextViewText(R.id.msg, pt_msg);
                contentViewSmall.setTextViewText(R.id.msg, pt_msg);
            }

            if(pt_title_clr != null && !pt_title_clr.isEmpty()){
                contentViewRating.setTextColor(R.id.title,Color.parseColor(pt_title_clr));
                contentViewSmall.setTextColor(R.id.title,Color.parseColor(pt_title_clr));
            }

            if(pt_msg_clr != null && !pt_msg_clr.isEmpty()){
                contentViewRating.setTextColor(R.id.msg,Color.parseColor(pt_msg_clr));
                contentViewSmall.setTextColor(R.id.msg,Color.parseColor(pt_msg_clr));
            }

            if(pt_img_small!=null && !pt_img_small.isEmpty()) {
                URL smallImgUrl = new URL(pt_img_small);
                contentViewSmall.setImageViewBitmap(R.id.small_image_app, BitmapFactory.decodeStream(smallImgUrl.openConnection().getInputStream()));
                contentViewRating.setImageViewBitmap(R.id.big_image_app, BitmapFactory.decodeStream(smallImgUrl.openConnection().getInputStream()));
            }

            //Set the rating stars
            contentViewRating.setImageViewResource(R.id.star1,R.drawable.outline_star_1);
            contentViewRating.setImageViewResource(R.id.star2,R.drawable.outline_star_1);
            contentViewRating.setImageViewResource(R.id.star3,R.drawable.outline_star_1);
            contentViewRating.setImageViewResource(R.id.star4,R.drawable.outline_star_1);
            contentViewRating.setImageViewResource(R.id.star5,R.drawable.outline_star_1);

            notificationId = new Random().nextInt();

            //Set Pending Intents for each star to listen to click

            Intent notificationIntent1 = new Intent(context, PushTemplateReceiver.class);
            notificationIntent1.putExtra("click1",true);
            notificationIntent1.putExtra("notif_id",notificationId);
            notificationIntent1.putExtras(extras);
            PendingIntent contentIntent1 = PendingIntent.getBroadcast(context, new Random().nextInt(), notificationIntent1, 0);
            contentViewRating.setOnClickPendingIntent(R.id.star1, contentIntent1);

            Intent notificationIntent2 = new Intent(context, PushTemplateReceiver.class);
            notificationIntent2.putExtra("click2",true);
            notificationIntent2.putExtra("notif_id",notificationId);
            notificationIntent2.putExtras(extras);
            PendingIntent contentIntent2 = PendingIntent.getBroadcast(context, new Random().nextInt(), notificationIntent2, 0);
            contentViewRating.setOnClickPendingIntent(R.id.star2, contentIntent2);

            Intent notificationIntent3 = new Intent(context, PushTemplateReceiver.class);
            notificationIntent3.putExtra("click3",true);
            notificationIntent3.putExtra("notif_id",notificationId);
            notificationIntent3.putExtras(extras);
            PendingIntent contentIntent3 = PendingIntent.getBroadcast(context, new Random().nextInt(), notificationIntent3, 0);
            contentViewRating.setOnClickPendingIntent(R.id.star3, contentIntent3);

            Intent notificationIntent4 = new Intent(context, PushTemplateReceiver.class);
            notificationIntent4.putExtra("click4",true);
            notificationIntent4.putExtra("notif_id",notificationId);
            notificationIntent4.putExtras(extras);
            PendingIntent contentIntent4 = PendingIntent.getBroadcast(context, new Random().nextInt(), notificationIntent4, 0);
            contentViewRating.setOnClickPendingIntent(R.id.star4, contentIntent4);

            Intent notificationIntent5 = new Intent(context, PushTemplateReceiver.class);
            notificationIntent5.putExtra("click5",true);
            notificationIntent5.putExtra("notif_id",notificationId);
            notificationIntent5.putExtras(extras);
            PendingIntent contentIntent5 = PendingIntent.getBroadcast(context, new Random().nextInt(), notificationIntent5, 0);
            contentViewRating.setOnClickPendingIntent(R.id.star5, contentIntent5);

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
                    .setCustomBigContentView(contentViewRating)
                    .setContentTitle("Custom Notification")
                    .setContentIntent(pIntent)
                    .setAutoCancel(true);

            Notification notification = notificationBuilder.build();
            notificationManager.notify(notificationId, notification);
            loadIntoGlide(context, R.id.small_image_app, imageList.get(0), contentViewSmall, notification, notificationId);
            loadIntoGlide(context, R.id.big_image_app, imageList.get(0), contentViewRating, notification, notificationId);

            CleverTapAPI instance = CleverTapAPI.getDefaultInstance(context);
            if (instance != null) {
                instance.pushNotificationViewedEvent(extras);
            }

        }catch (Throwable t){
            PTLog.error("Error creating rating notification ",t);
        }
    }

    private void renderAutoCarouselNotification(Context context, Bundle extras, int notificationId){
        try{
            contentViewCarousel = new RemoteViews(context.getPackageName(),R.layout.auto_carousel);
            contentViewSmall = new RemoteViews(context.getPackageName(),R.layout.image_only_small);

            if(pt_title!=null && !pt_title.isEmpty()) {
                contentViewCarousel.setTextViewText(R.id.title, pt_title);
                contentViewSmall.setTextViewText(R.id.title, pt_title);
            }

            if(pt_msg!=null && !pt_msg.isEmpty()) {
                contentViewCarousel.setTextViewText(R.id.msg, pt_msg);
                contentViewSmall.setTextViewText(R.id.msg, pt_msg);
            }

            if(pt_title_clr != null && !pt_title_clr.isEmpty()){
                contentViewCarousel.setTextColor(R.id.title,Color.parseColor(pt_title_clr));
                contentViewSmall.setTextColor(R.id.title,Color.parseColor(pt_title_clr));
            }

            if(pt_msg_clr != null && !pt_msg_clr.isEmpty()){
                contentViewCarousel.setTextColor(R.id.msg,Color.parseColor(pt_msg_clr));
                contentViewSmall.setTextColor(R.id.msg,Color.parseColor(pt_msg_clr));
            }

            if(pt_bg!=null && !pt_bg.isEmpty()){
                contentViewCarousel.setInt(R.id.carousel_relative_layout,"setBackgroundColor", Color.parseColor(pt_bg));
                contentViewSmall.setInt(R.id.image_only_small_relative_layout,"setBackgroundColor", Color.parseColor(pt_bg));
            }

            if(pt_img_small!=null && !pt_img_small.isEmpty()) {
                //URL smallImgUrl = new URL(pt_img_small);
                //contentViewSmall.setImageViewBitmap(R.id.small_image_app, BitmapFactory.decodeStream(smallImgUrl.openConnection().getInputStream()));
                //contentViewCarousel.setImageViewBitmap(R.id.big_image_app, BitmapFactory.decodeStream(smallImgUrl.openConnection().getInputStream()));
            }

            contentViewCarousel.setInt(R.id.view_flipper,"setFlipInterval",4000);

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
                    .setCustomBigContentView(contentViewCarousel)
                    .setContentTitle("Custom Notification")
                    .setContentIntent(pIntent)
                    .setAutoCancel(true);

            Notification notification = notificationBuilder.build();
            notificationManager.notify(notificationId, notification);

            NotificationTarget smallNotifTarget = new NotificationTarget(
                    context,
                    R.id.small_image_app,
                    contentViewSmall,
                    notification,
                    notificationId);
            Glide
                    .with(context.getApplicationContext())
                    .asBitmap()
                    .load(pt_img_small)
                    .into(smallNotifTarget);
            NotificationTarget bigNotifTargetIcon = new NotificationTarget(
                    context,
                    R.id.big_image_app,
                    contentViewCarousel,
                    notification,
                    notificationId);
            Glide
                    .with(context.getApplicationContext())
                    .asBitmap()
                    .load(pt_img_small)
                    .into(bigNotifTargetIcon);


            ArrayList<Integer> layoutIds = new ArrayList<>();
            layoutIds.add(0, R.id.flipper_img1);
            layoutIds.add(1, R.id.flipper_img2);
            layoutIds.add(2, R.id.flipper_img3);


            for(int index = 0; index < imageList.size(); index++){
                //URL imageURL = new URL(image);
                //RemoteViews imageView =  new RemoteViews(context.getPackageName(),R.layout.carousel_image);
                //contentViewCarousel.addView(R.id.view_flipper,imageView);
                //imageView.setImageViewBitmap(R.id.flipper_img, BitmapFactory.decodeStream(imageURL.openConnection().getInputStream()));

                loadIntoGlide(context, layoutIds.get(index),imageList.get(index),contentViewCarousel, notification, notificationId);
                /*NotificationTarget bigNotifCarouselTarget = new NotificationTarget(
                        context,
                        layoutIds.get(index),
                        contentViewCarousel,
                        notification,
                        notificationId);
                Glide
                        .with(context.getApplicationContext())
                        .asBitmap()
                        .load(imageList.get(index))
                        .into(bigNotifCarouselTarget);*/
            }

            CleverTapAPI instance = CleverTapAPI.getDefaultInstance(context);
            if (instance != null) {
                instance.pushNotificationViewedEvent(extras);
            }
        }catch (Throwable t){
            PTLog.error("Error creating auto carousel notification ",t);
        }
    }

    private void renderBasicTemplateNotification(Context context, Bundle extras, int notificationId){
        try{
            contentViewBig = new RemoteViews(context.getPackageName(), R.layout.image_only_big);
            contentViewSmall = new RemoteViews(context.getPackageName(),R.layout.image_only_small);
            if(pt_img_big!=null && !pt_img_big.isEmpty()) {
                //URL bigImgUrl = new URL(pt_img_big);
                //contentViewBig.setImageViewBitmap(R.id.image_pic, BitmapFactory.decodeStream(bigImgUrl.openConnection().getInputStream()));

            }

            if(pt_img_small!=null && !pt_img_small.isEmpty()) {
                //URL smallImgUrl = new URL(pt_img_small);
                //contentViewSmall.setImageViewBitmap(R.id.small_image_app, BitmapFactory.decodeStream(smallImgUrl.openConnection().getInputStream()));
                //contentViewBig.setImageViewBitmap(R.id.big_image_app, BitmapFactory.decodeStream(smallImgUrl.openConnection().getInputStream()));
            }

            if(pt_title!=null && !pt_title.isEmpty()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    contentViewBig.setTextViewText(R.id.title, Html.fromHtml(pt_title,Html.FROM_HTML_MODE_LEGACY));
                    contentViewSmall.setTextViewText(R.id.title, Html.fromHtml(pt_title,Html.FROM_HTML_MODE_LEGACY));
                } else {
                    contentViewBig.setTextViewText(R.id.title, Html.fromHtml(pt_title));
                    contentViewSmall.setTextViewText(R.id.title, Html.fromHtml(pt_title));
                }

            }

            if(pt_msg!=null && !pt_msg.isEmpty()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    contentViewBig.setTextViewText(R.id.msg, Html.fromHtml(pt_msg,Html.FROM_HTML_MODE_LEGACY));
                    contentViewSmall.setTextViewText(R.id.msg, Html.fromHtml(pt_msg,Html.FROM_HTML_MODE_LEGACY));
                } else {
                    contentViewBig.setTextViewText(R.id.msg, Html.fromHtml(pt_msg));
                    contentViewSmall.setTextViewText(R.id.msg, Html.fromHtml(pt_msg));
                }
            }

            if(pt_bg!=null && !pt_bg.isEmpty()){
                contentViewBig.setInt(R.id.image_only_big_relative_layout,"setBackgroundColor", Color.parseColor(pt_bg));
                contentViewSmall.setInt(R.id.image_only_small_relative_layout,"setBackgroundColor", Color.parseColor(pt_bg));
            }

            if(pt_title_clr != null && !pt_title_clr.isEmpty()){
                contentViewBig.setTextColor(R.id.title,Color.parseColor(pt_title_clr));
                contentViewSmall.setTextColor(R.id.title,Color.parseColor(pt_title_clr));
            }

            if(pt_msg_clr != null && !pt_msg_clr.isEmpty()){
                contentViewBig.setTextColor(R.id.msg,Color.parseColor(pt_msg_clr));
                contentViewSmall.setTextColor(R.id.msg,Color.parseColor(pt_msg_clr));
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

            Notification notification = notificationBuilder.build();
            notificationManager.notify(notificationId, notification);

            NotificationTarget bigNotifTarget = new NotificationTarget(
                    context,
                    R.id.image_pic,
                    contentViewBig,
                    notification,
                    notificationId);
            Glide
                    .with(context.getApplicationContext())
                    .asBitmap()
                    .load(pt_img_big)
                    .into(bigNotifTarget);

            NotificationTarget smallNotifTarget = new NotificationTarget(
                    context,
                    R.id.small_image_app,
                    contentViewSmall,
                    notification,
                    notificationId);
            Glide
                    .with(context.getApplicationContext())
                    .asBitmap()
                    .load(pt_img_small)
                    .into(smallNotifTarget);
            NotificationTarget bigNotifTargetIcon = new NotificationTarget(
                    context,
                    R.id.big_image_app,
                    contentViewBig,
                    notification,
                    notificationId);
            Glide
                    .with(context.getApplicationContext())
                    .asBitmap()
                    .load(pt_img_small)
                    .into(bigNotifTargetIcon);


            CleverTapAPI instance = CleverTapAPI.getDefaultInstance(context);
            if (instance != null) {
                instance.pushNotificationViewedEvent(extras);
            }
        }catch (Throwable t){
            PTLog.error("Error creating image only notification", t);
        }
    }

    private void renderProductDisplayNotification(Context context, Bundle extras, int notificationId){
        try{

            contentViewBig = new RemoteViews(context.getPackageName(),R.layout.product_display_template);
            contentViewSmall = new RemoteViews(context.getPackageName(),R.layout.image_only_small);


            /*for(int index = 0; index < imageList.size(); index++){
                final int finalIndex = index;
                Glide.with(context.getApplicationContext())
                        .asBitmap()
                        .load(imageList.get(index))
                        .into(new CustomTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                if (finalIndex == 0){
                                    contentViewBig.setImageViewBitmap(R.id.small_image1, resource);
                                }
                                else if(finalIndex == 1){
                                    contentViewBig.setImageViewBitmap(R.id.small_image2, resource);
                                }
                                else if(finalIndex == 2){
                                    contentViewBig.setImageViewBitmap(R.id.small_image3, resource);
                                }
                            }

                            @Override
                            public void onLoadCleared(@Nullable Drawable placeholder) {
                            }
                        });

            }*/

            if(!bigTextList.isEmpty()) {
                contentViewBig.setTextViewText(R.id.big_text, bigTextList.get(0));

            }

            if(!smallTextList.isEmpty()) {
                contentViewBig.setTextViewText(R.id.small_text, smallTextList.get(0));

            }


            if(pt_title!=null && !pt_title.isEmpty()) {
                contentViewBig.setTextViewText(R.id.title, pt_title);
                contentViewSmall.setTextViewText(R.id.title, pt_title);
            }

            if(pt_msg!=null && !pt_msg.isEmpty()) {
                contentViewBig.setTextViewText(R.id.msg, pt_msg);
                contentViewSmall.setTextViewText(R.id.msg, pt_msg);
            }

            if(pt_title_clr != null && !pt_title_clr.isEmpty()){
                contentViewBig.setTextColor(R.id.title,Color.parseColor(pt_title_clr));
                contentViewSmall.setTextColor(R.id.title,Color.parseColor(pt_title_clr));
            }

            if(pt_msg_clr != null && !pt_msg_clr.isEmpty()){
                contentViewBig.setTextColor(R.id.msg,Color.parseColor(pt_msg_clr));
                contentViewSmall.setTextColor(R.id.msg,Color.parseColor(pt_msg_clr));
            }

            /*Glide.with(context.getApplicationContext())
                    .asBitmap()
                    .load(imageList.get(0))
                    .into(new CustomTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            contentViewBig.setImageViewBitmap(R.id.big_image, resource);
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {
                        }
                    });*/


            notificationId = new Random().nextInt();

            int requestCode1 = new Random().nextInt();
            int requestCode2 = new Random().nextInt();
            int requestCode3 = new Random().nextInt();

            Intent notificationIntent1 = new Intent(context, PushTemplateReceiver.class);
            notificationIntent1.putExtra("img1",true);
            notificationIntent1.putExtra("notif_id",notificationId);
            notificationIntent1.putExtra("pt_dl",deepLinkList.get(0));
            notificationIntent1.putExtra("pt_reqcode1",requestCode1);
            notificationIntent1.putExtra("pt_reqcode2",requestCode2);
            notificationIntent1.putExtra("pt_reqcode3",requestCode3);
            notificationIntent1.putExtras(extras);
            PendingIntent contentIntent1 = PendingIntent.getBroadcast(context, requestCode1, notificationIntent1, 0);
            contentViewBig.setOnClickPendingIntent(R.id.small_image1, contentIntent1);

            Intent notificationIntent2 = new Intent(context, PushTemplateReceiver.class);
            notificationIntent2.putExtra("img2",true);
            notificationIntent2.putExtra("notif_id",notificationId);
            notificationIntent2.putExtra("pt_dl",deepLinkList.get(1));
            notificationIntent2.putExtra("pt_reqcode1",requestCode1);
            notificationIntent2.putExtra("pt_reqcode2",requestCode2);
            notificationIntent2.putExtra("pt_reqcode3",requestCode3);
            notificationIntent2.putExtras(extras);
            PendingIntent contentIntent2 = PendingIntent.getBroadcast(context, requestCode2, notificationIntent2, 0);
            contentViewBig.setOnClickPendingIntent(R.id.small_image2, contentIntent2);

            Intent notificationIntent3 = new Intent(context, PushTemplateReceiver.class);
            notificationIntent3.putExtra("img3",true);
            notificationIntent3.putExtra("notif_id",notificationId);
            notificationIntent3.putExtra("pt_dl",deepLinkList.get(2));
            notificationIntent3.putExtra("pt_reqcode1",requestCode1);
            notificationIntent3.putExtra("pt_reqcode2",requestCode2);
            notificationIntent3.putExtra("pt_reqcode3",requestCode3);
            notificationIntent3.putExtras(extras);
            PendingIntent contentIntent3 = PendingIntent.getBroadcast(context, requestCode3, notificationIntent3, 0);
            contentViewBig.setOnClickPendingIntent(R.id.small_image3, contentIntent3);


            Intent launchIntent = new Intent(context, CTPushNotificationReceiver.class);
            launchIntent.putExtras(extras);
            launchIntent.putExtra("wzrk_dl", deepLinkList.get(0));
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

            Notification notification = notificationBuilder.build();
            notificationManager.notify(notificationId, notification);
            loadIntoGlide(context, R.id.small_image_app, pt_img_small, contentViewSmall, notification, notificationId);
            for(int index = 0; index < imageList.size(); index++){
                if (index == 0){
                    loadIntoGlide(context, R.id.small_image1, imageList.get(0), contentViewBig, notification, notificationId);
                }
                else if(index == 1){
                    loadIntoGlide(context, R.id.small_image2, imageList.get(1), contentViewBig, notification, notificationId);
                }
                else if(index == 2){
                    loadIntoGlide(context, R.id.small_image3, imageList.get(2), contentViewBig, notification, notificationId);
                }
            }
            loadIntoGlide(context, R.id.big_image, imageList.get(0), contentViewBig, notification, notificationId);
            CleverTapAPI instance = CleverTapAPI.getDefaultInstance(context);
            if (instance != null) {
                instance.pushNotificationViewedEvent(extras);
            }
        }catch (Throwable t){
            PTLog.error("Error creating Product Display Notification ",t);
        }
    }

    private void renderFiveIconNotification(Context context, Bundle extras, int notificationId) {
        try{
            contentFiveCTAs = new RemoteViews(context.getPackageName(), R.layout.five_cta);

            notificationId = new Random().nextInt();

            Intent notificationIntent1 = new Intent(context, PushTemplateReceiver.class);
            notificationIntent1.putExtra("cta1",true);
            notificationIntent1.putExtras(extras);
            PendingIntent contentIntent1 = PendingIntent.getBroadcast(context, ((int) System.currentTimeMillis()) + 1, notificationIntent1, 0);
            contentFiveCTAs.setOnClickPendingIntent(R.id.cta1, contentIntent1);

            Intent notificationIntent2 = new Intent(context, PushTemplateReceiver.class);
            notificationIntent2.putExtra("cta2",true);
            notificationIntent2.putExtras(extras);
            PendingIntent contentIntent2 = PendingIntent.getBroadcast(context, ((int) System.currentTimeMillis()) + 2, notificationIntent2, 0);
            contentFiveCTAs.setOnClickPendingIntent(R.id.cta2, contentIntent2);

            Intent notificationIntent3 = new Intent(context, PushTemplateReceiver.class);
            notificationIntent3.putExtra("cta3",true);
            notificationIntent3.putExtras(extras);
            PendingIntent contentIntent3 = PendingIntent.getBroadcast(context, ((int) System.currentTimeMillis()) + 3, notificationIntent3, 0);
            contentFiveCTAs.setOnClickPendingIntent(R.id.cta3, contentIntent3);

            Intent notificationIntent4 = new Intent(context, PushTemplateReceiver.class);
            notificationIntent4.putExtra("cta4",true);
            notificationIntent4.putExtras(extras);
            PendingIntent contentIntent4 = PendingIntent.getBroadcast(context, ((int) System.currentTimeMillis()) + 4, notificationIntent4, 0);
            contentFiveCTAs.setOnClickPendingIntent(R.id.cta4, contentIntent4);

            Intent notificationIntent5 = new Intent(context, PushTemplateReceiver.class);
            notificationIntent5.putExtra("cta5",true);
            notificationIntent5.putExtras(extras);
            PendingIntent contentIntent5 = PendingIntent.getBroadcast(context, ((int) System.currentTimeMillis()) + 5, notificationIntent5, 0);
            contentFiveCTAs.setOnClickPendingIntent(R.id.cta5, contentIntent5);

            Intent notificationIntent6 = new Intent(context, PushTemplateReceiver.class);
            notificationIntent6.putExtra("close",true);
            notificationIntent6.putExtras(extras);
            PendingIntent contentIntent6 = PendingIntent.getBroadcast(context, ((int) System.currentTimeMillis()) + 6, notificationIntent6, 0);
            contentFiveCTAs.setOnClickPendingIntent(R.id.close, contentIntent6);

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
                    .setCustomContentView(contentFiveCTAs)
                    .setCustomBigContentView(contentFiveCTAs)
                    .setContentTitle("Custom Notification")
                    .setContentIntent(pIntent)
                    .setOngoing(true)
                    .setAutoCancel(true);

            Notification notification = notificationBuilder.build();
            notificationManager.notify(notificationId, notification);



            for(int imageKey = 0; imageKey < imageList.size(); imageKey ++){
                if (imageKey == 0){
                    loadIntoGlide(context, R.id.cta1, imageList.get(imageKey), contentFiveCTAs, notification, notificationId);
                }
                else if(imageKey == 1){
                    loadIntoGlide(context, R.id.cta2, imageList.get(imageKey), contentFiveCTAs, notification, notificationId);
                }
                else if(imageKey == 2){
                    loadIntoGlide(context, R.id.cta3, imageList.get(imageKey), contentFiveCTAs, notification, notificationId);
                }
                else if(imageKey == 3){
                    loadIntoGlide(context, R.id.cta4, imageList.get(imageKey), contentFiveCTAs, notification, notificationId);
                }
                else if(imageKey == 4) {
                    loadIntoGlide(context, R.id.cta5, imageList.get(imageKey), contentFiveCTAs, notification, notificationId);
                }

            }
            loadIntoGlide(context, R.id.close, imageList.get(5), contentFiveCTAs, notification, notificationId);

            CleverTapAPI instance = CleverTapAPI.getDefaultInstance(context);
            if (instance != null) {
                instance.pushNotificationViewedEvent(extras);
            }
        }catch (Throwable t){
            PTLog.error("Error creating image only notification", t);
        }

    }

    private Bundle fromJson(JSONObject s) {
        Bundle bundle = new Bundle();

        for (Iterator<String> it = s.keys(); it.hasNext(); ) {
            String key = it.next();
            JSONArray arr = s.optJSONArray(key);
            String str = s.optString(key);

            if (arr != null && arr.length() <= 0)
                bundle.putStringArray(key, new String[]{});

            else if (arr != null && arr.optString(0) != null) {
                String[] newarr = new String[arr.length()];
                for (int i = 0; i < arr.length(); i++)
                    newarr[i] = arr.optString(i);
                bundle.putStringArray(key, newarr);
            }

            else if (str != null)
                bundle.putString(key, str);

            else
                System.err.println("unable to transform json to bundle " + key);
        }

        return bundle;
    }

    private JSONObject fromTest(Context context, String jsonString) {
        JSONObject jsonObject = null;
        if(jsonString == null) {
            jsonString = readRawTextFile(context, R.raw.test);
        }
        try {
            jsonObject = new JSONObject(jsonString);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    private String readRawTextFile(Context ctx, int resId)
    {
        InputStream inputStream = ctx.getResources().openRawResource(resId);

        InputStreamReader inputreader = new InputStreamReader(inputStream);
        BufferedReader buffreader = new BufferedReader(inputreader);
        String line;
        StringBuilder text = new StringBuilder();

        try {
            while (( line = buffreader.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }
        } catch (IOException e) {
            return null;
        }
        return text.toString();
    }

    private void loadIntoGlide(Context context, int imageResource, String imageURL, RemoteViews remoteViews, Notification notification, int notificationId) {
        NotificationTarget bigNotifTarget = new NotificationTarget(
                context,
                imageResource,
                remoteViews,
                notification,
                notificationId);
        Glide
                .with(context.getApplicationContext())
                .asBitmap()
                .load(imageURL)
                .into(bigNotifTarget);
    }
}
