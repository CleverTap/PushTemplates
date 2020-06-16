package com.clevertap.pushtemplates;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.core.app.RemoteInput;

import android.text.Html;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.clevertap.android.sdk.CTPushNotificationReceiver;
import com.clevertap.android.sdk.CleverTapAPI;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import static android.content.Context.NOTIFICATION_SERVICE;

public class PushTemplateReceiver extends BroadcastReceiver {
    boolean clicked1 = true, clicked2 = true, clicked3 = true, clicked4 = true, clicked5 = true, img1 = false, img2 = false, img3 = false, buynow = true, bigimage = true, cta1 = true, cta2 = true, cta3 = true, cta4 = true, cta5 = true, close = true;
    private RemoteViews contentViewBig, contentViewSmall, contentViewRating, contentViewManualCarousel;
    private String pt_id;
    private TemplateType templateType;
    private String pt_title;
    private String pt_msg;
    private String pt_msg_summary;
    private String pt_img_small;
    private String pt_large_icon;
    private String pt_rating_default_dl;
    private String pt_title_clr, pt_msg_clr;
    private ArrayList<String> imageList = new ArrayList<>();
    private ArrayList<String> deepLinkList = new ArrayList<>();
    private ArrayList<String> bigTextList = new ArrayList<>();
    private ArrayList<String> smallTextList = new ArrayList<>();
    private ArrayList<String> priceList = new ArrayList<>();
    private String pt_bg;
    private String channelId;
    private int smallIcon = 0;
    private boolean requiresChannelId;
    private NotificationManager notificationManager;
    private CleverTapAPI cleverTapAPI;
    private String pt_product_display_action;
    private String pt_product_display_action_clr;
    private String pt_product_display_linear;
    private String pt_big_img_alt;
    private Bitmap pt_small_icon;
    private String pt_small_icon_clr;
    private String pt_product_display_action_text_clr;


    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getExtras() != null) {
            Bundle extras = intent.getExtras();
            pt_id = intent.getStringExtra(Constants.PT_ID);
            pt_msg = extras.getString(Constants.PT_MSG);
            pt_msg_summary = extras.getString(Constants.PT_MSG_SUMMARY);
            pt_msg_clr = extras.getString(Constants.PT_MSG_COLOR);
            pt_title = extras.getString(Constants.PT_TITLE);
            pt_title_clr = extras.getString(Constants.PT_TITLE_COLOR);
            pt_img_small = extras.getString(Constants.PT_SMALL_IMG);
            pt_large_icon = extras.getString(Constants.PT_NOTIF_ICON);
            pt_bg = extras.getString(Constants.PT_BG);
            pt_rating_default_dl = extras.getString(Constants.PT_DEFAULT_DL);
            imageList = Utils.getImageListFromExtras(extras);
            deepLinkList = Utils.getDeepLinkListFromExtras(extras);
            bigTextList = Utils.getBigTextFromExtras(extras);
            smallTextList = Utils.getSmallTextFromExtras(extras);
            priceList = Utils.getPriceFromExtras(extras);
            pt_product_display_action = extras.getString(Constants.PT_PRODUCT_DISPLAY_ACTION);
            pt_product_display_action_clr = extras.getString(Constants.PT_PRODUCT_DISPLAY_ACTION_COLOUR);
            pt_product_display_linear = extras.getString(Constants.PT_PRODUCT_DISPLAY_LINEAR);
            notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
            cleverTapAPI = CleverTapAPI.getDefaultInstance(context);
            channelId = extras.getString(Constants.WZRK_CHANNEL_ID, "");
            pt_big_img_alt = extras.getString(Constants.PT_BIG_IMG_ALT);
            pt_small_icon_clr = extras.getString(Constants.PT_SMALL_ICON_COLOUR);
            pt_product_display_action_text_clr = extras.getString(Constants.PT_PRODUCT_DISPLAY_ACTION_TEXT_COLOUR);

            requiresChannelId = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                String channelIdError = null;
                if (channelId.isEmpty()) {
                    channelIdError = "Unable to render notification, channelId is required but not provided in the notification payload: " + extras.toString();
                } else if (notificationManager != null && notificationManager.getNotificationChannel(channelId) == null) {
                    channelIdError = "Unable to render notification, channelId: " + channelId + " not registered by the app.";
                }
                if (channelIdError != null) {
                    PTLog.verbose(channelIdError);
                    return;
                }
            }

            if (pt_id != null) {
                templateType = TemplateType.fromString(pt_id);
            }
            if (templateType != null) {
                switch (templateType) {
                    case RATING:
                        handleRatingNotification(context, extras);
                        break;
                    case FIVE_ICONS:
                        handleFiveCTANotification(context, extras);
                        break;
                    case PRODUCT_DISPLAY:
                        handleProductDisplayNotification(context, extras);
                        break;
                    case INPUT_BOX:
                        handleInputBoxNotification(context, extras, intent);
                        break;
                    case MANUAL_CAROUSEL:
                        handleManualCarouselNotification(context, extras);
                        break;
                }
            }
        }
    }

    private void handleManualCarouselNotification(Context context, Bundle extras) {
            try {
                contentViewManualCarousel = new RemoteViews(context.getPackageName(), R.layout.manual_carousel);
                setCustomContentViewBasicKeys(contentViewManualCarousel, context);

                contentViewSmall = new RemoteViews(context.getPackageName(), R.layout.content_view_small);
                setCustomContentViewBasicKeys(contentViewSmall, context);


                setCustomContentViewTitle(contentViewManualCarousel, pt_title);
                setCustomContentViewTitle(contentViewSmall, pt_title);

                setCustomContentViewMessage(contentViewManualCarousel, pt_msg);
                setCustomContentViewMessage(contentViewSmall, pt_msg);

                setCustomContentViewExpandedBackgroundColour(contentViewManualCarousel, pt_bg);
                setCustomContentViewCollapsedBackgroundColour(contentViewSmall, pt_bg);

                setCustomContentViewTitleColour(contentViewManualCarousel, pt_title_clr);
                setCustomContentViewTitleColour(contentViewSmall, pt_title_clr);

                setCustomContentViewMessageColour(contentViewManualCarousel, pt_msg_clr);
                setCustomContentViewMessageColour(contentViewSmall, pt_msg_clr);

                setCustomContentViewMessageSummary(contentViewManualCarousel, pt_msg_summary);

                int notificationId = extras.getInt(Constants.PT_NOTIF_ID);

                int positionFrom = extras.getInt("manual_carousel_from");

                final boolean rightSwipe = extras.getBoolean("right_swipe");
                int currPosition;
                if(rightSwipe) {
                    currPosition = (positionFrom + 1) % 3;
                } else {
                    currPosition = (positionFrom - 1) % 3;
                }

                int reqCodePos0 = extras.getInt("pt_reqcode0");
                int reqCodePos1 = extras.getInt("pt_reqcode1");
                int reqCodePos2 = extras.getInt("pt_reqcode2");
                if(currPosition == 0) {
                    contentViewManualCarousel.setViewVisibility(R.id.leftArrowPos0, View.VISIBLE);
                    contentViewManualCarousel.setViewVisibility(R.id.rightArrowPos0, View.VISIBLE);

                    contentViewManualCarousel.setViewVisibility(R.id.leftArrowPos1, View.INVISIBLE);
                    contentViewManualCarousel.setViewVisibility(R.id.rightArrowPos1, View.INVISIBLE);

                    contentViewManualCarousel.setViewVisibility(R.id.leftArrowPos2, View.INVISIBLE);
                    contentViewManualCarousel.setViewVisibility(R.id.rightArrowPos2, View.INVISIBLE);
                } else if(currPosition == 1) {
                    contentViewManualCarousel.setViewVisibility(R.id.leftArrowPos0, View.INVISIBLE);
                    contentViewManualCarousel.setViewVisibility(R.id.rightArrowPos0, View.INVISIBLE);

                    contentViewManualCarousel.setViewVisibility(R.id.leftArrowPos1, View.VISIBLE);
                    contentViewManualCarousel.setViewVisibility(R.id.rightArrowPos1, View.VISIBLE);

                    contentViewManualCarousel.setViewVisibility(R.id.leftArrowPos2, View.INVISIBLE);
                    contentViewManualCarousel.setViewVisibility(R.id.rightArrowPos2, View.INVISIBLE);
                } else if(currPosition == 2) {
                    contentViewManualCarousel.setViewVisibility(R.id.leftArrowPos0, View.INVISIBLE);
                    contentViewManualCarousel.setViewVisibility(R.id.rightArrowPos0, View.INVISIBLE);

                    contentViewManualCarousel.setViewVisibility(R.id.leftArrowPos1, View.INVISIBLE);
                    contentViewManualCarousel.setViewVisibility(R.id.rightArrowPos1, View.INVISIBLE);

                    contentViewManualCarousel.setViewVisibility(R.id.leftArrowPos2, View.VISIBLE);
                    contentViewManualCarousel.setViewVisibility(R.id.rightArrowPos2, View.VISIBLE);
                }

                Intent rightArrowPos0Intent = new Intent(context, PushTemplateReceiver.class);
                rightArrowPos0Intent.putExtra("right_swipe", true);
                rightArrowPos0Intent.putExtra("manual_carousel_from", 0);
                rightArrowPos0Intent.putExtra("pt_reqcode0", reqCodePos0);
                rightArrowPos0Intent.putExtra("pt_reqcode1", reqCodePos1);
                rightArrowPos0Intent.putExtra("pt_reqcode2", reqCodePos2);
                rightArrowPos0Intent.putExtra(Constants.PT_NOTIF_ID, notificationId);
                rightArrowPos0Intent.putExtras(extras);
                PendingIntent contentRightPos0Intent = PendingIntent.getBroadcast(context, reqCodePos1, rightArrowPos0Intent, 0);
                contentViewManualCarousel.setOnClickPendingIntent(R.id.rightArrowPos0, contentRightPos0Intent);

                Intent rightArrowPos1Intent = new Intent(context, PushTemplateReceiver.class);
                rightArrowPos1Intent.putExtra("right_swipe", true);
                rightArrowPos1Intent.putExtra("manual_carousel_from", 1);
                rightArrowPos1Intent.putExtra("pt_reqcode0", reqCodePos0);
                rightArrowPos1Intent.putExtra("pt_reqcode1", reqCodePos1);
                rightArrowPos1Intent.putExtra("pt_reqcode2", reqCodePos2);
                rightArrowPos1Intent.putExtra(Constants.PT_NOTIF_ID, notificationId);
                rightArrowPos1Intent.putExtras(extras);
                PendingIntent contentRightPos1Intent = PendingIntent.getBroadcast(context, reqCodePos2, rightArrowPos1Intent, 0);
                contentViewManualCarousel.setOnClickPendingIntent(R.id.rightArrowPos1, contentRightPos1Intent);

                Intent rightArrowPos2Intent = new Intent(context, PushTemplateReceiver.class);
                rightArrowPos2Intent.putExtra("right_swipe", true);
                rightArrowPos2Intent.putExtra("manual_carousel_from", 2);
                rightArrowPos2Intent.putExtra("pt_reqcode0", reqCodePos0);
                rightArrowPos2Intent.putExtra("pt_reqcode1", reqCodePos1);
                rightArrowPos2Intent.putExtra("pt_reqcode2", reqCodePos2);
                rightArrowPos2Intent.putExtra(Constants.PT_NOTIF_ID, notificationId);
                rightArrowPos2Intent.putExtras(extras);
                PendingIntent contentRightPos2Intent = PendingIntent.getBroadcast(context, reqCodePos0, rightArrowPos2Intent, 0);
                contentViewManualCarousel.setOnClickPendingIntent(R.id.rightArrowPos2, contentRightPos2Intent);

                Intent leftArrowPos0Intent = new Intent(context, PushTemplateReceiver.class);
                leftArrowPos0Intent.putExtra("right_swipe", false);
                leftArrowPos0Intent.putExtra("manual_carousel_from", 0);
                leftArrowPos0Intent.putExtra("pt_reqcode0", reqCodePos0);
                leftArrowPos0Intent.putExtra("pt_reqcode1", reqCodePos1);
                leftArrowPos0Intent.putExtra("pt_reqcode2", reqCodePos2);
                leftArrowPos0Intent.putExtra(Constants.PT_NOTIF_ID, notificationId);
                leftArrowPos0Intent.putExtras(extras);
                PendingIntent contentLeftPos0Intent = PendingIntent.getBroadcast(context, reqCodePos2, leftArrowPos0Intent, 0);
                contentViewManualCarousel.setOnClickPendingIntent(R.id.leftArrowPos0, contentLeftPos0Intent);

                Intent leftArrowPos1Intent = new Intent(context, PushTemplateReceiver.class);
                leftArrowPos1Intent.putExtra("right_swipe", false);
                leftArrowPos1Intent.putExtra("manual_carousel_from", 1);
                leftArrowPos1Intent.putExtra("pt_reqcode0", reqCodePos0);
                leftArrowPos1Intent.putExtra("pt_reqcode1", reqCodePos1);
                leftArrowPos1Intent.putExtra("pt_reqcode2", reqCodePos2);
                leftArrowPos1Intent.putExtra(Constants.PT_NOTIF_ID, notificationId);
                leftArrowPos1Intent.putExtras(extras);
                PendingIntent contentLeftPos1Intent = PendingIntent.getBroadcast(context, reqCodePos0, leftArrowPos1Intent, 0);
                contentViewManualCarousel.setOnClickPendingIntent(R.id.leftArrowPos1, contentLeftPos1Intent);

                Intent leftArrowPos2Intent = new Intent(context, PushTemplateReceiver.class);
                leftArrowPos2Intent.putExtra("right_swipe", false);
                leftArrowPos2Intent.putExtra("manual_carousel_from", 2);
                leftArrowPos2Intent.putExtra("pt_reqcode0", reqCodePos0);
                leftArrowPos2Intent.putExtra("pt_reqcode1", reqCodePos1);
                leftArrowPos2Intent.putExtra("pt_reqcode2", reqCodePos2);
                leftArrowPos2Intent.putExtra(Constants.PT_NOTIF_ID, notificationId);
                leftArrowPos2Intent.putExtras(extras);
                PendingIntent contentLeftPos2Intent = PendingIntent.getBroadcast(context, reqCodePos1, leftArrowPos2Intent, 0);
                contentViewManualCarousel.setOnClickPendingIntent(R.id.leftArrowPos2, contentLeftPos2Intent);


                Intent launchIntent = new Intent(context, CTPushNotificationReceiver.class);
                PendingIntent pIntent;

                if (deepLinkList != null) {
                    pIntent = setPendingIntent(context, notificationId, extras, launchIntent, deepLinkList.get(0));
                } else {
                    pIntent = setPendingIntent(context, notificationId, extras, launchIntent, null);
                }

                NotificationCompat.Builder notificationBuilder = setBuilderWithChannelIDCheck(requiresChannelId, channelId, context);

                setSmallIcon(context);

                setNotificationBuilderBasics(notificationBuilder, contentViewSmall, contentViewManualCarousel, pt_title, pIntent);

                Notification notification = notificationBuilder.build();
                notificationManager.notify(notificationId, notification);


                if(currPosition >=0 && currPosition < imageList.size()) {
                    Utils.loadIntoGlide(context, R.id.carousel_image, imageList.get(currPosition), contentViewManualCarousel, notification, notificationId);
                }

                Utils.loadIntoGlide(context, R.id.small_icon, pt_large_icon, contentViewSmall, notification, notificationId);

                setCustomContentViewLargeIcon(contentViewSmall, pt_large_icon, context, notification, notificationId);

                setCustomContentViewSmallIcon(context,contentViewManualCarousel,notification,notificationId);
                setCustomContentViewSmallIcon(context,contentViewSmall,notification,notificationId);


            } catch (Throwable t) {
                PTLog.verbose("Error creating auto carousel notification ", t);
            }
        }

    private void handleInputBoxNotification(Context context, Bundle extras, Intent intent){

        //Fetch Remote Input
        Bundle remoteInput = RemoteInput.getResultsFromIntent(intent);

        if (remoteInput != null) {
            //Fetch Reply
            CharSequence reply = remoteInput.getCharSequence(
                    Constants.PT_INPUT_KEY);

            int notificationId = extras.getInt(Constants.PT_NOTIF_ID);

            if (reply != null) {

                PTLog.verbose("Processing Input from Input Template");

                extras.putString(Constants.PT_INPUT_KEY,reply.toString());

                Utils.raiseCleverTapEvent(cleverTapAPI, extras, Constants.PT_INPUT_KEY);

                //Update the notification to show that the reply was received.
                NotificationCompat.Builder repliedNotification;
                if (requiresChannelId) {
                    repliedNotification = new NotificationCompat.Builder(context, channelId);
                } else {
                    repliedNotification = new NotificationCompat.Builder(context);
                }

                setSmallIcon(context);

                repliedNotification.setSmallIcon(smallIcon)
                        .setContentTitle(pt_title)
                        .setContentText(extras.getString(Constants.PT_INPUT_FEEDBACK))
                        .setVibrate(new long[]{0L})
                        .setTimeoutAfter(Constants.PT_INPUT_TIMEOUT)
                        .setWhen(System.currentTimeMillis())
                        .setAutoCancel(true);

                setStandardViewBigImageStyle(pt_big_img_alt,extras,context,repliedNotification);

                Notification notification = repliedNotification.build();
                notificationManager.notify(notificationId, notification);

                /* Check if Auto Open key is present and not empty, if not present then show feedback and
                auto kill in 3 secs. If present, then launch the App with Dl or Launcher activity.
                The launcher activity will get the reply in extras under the key "pt_reply" */
                if(extras.getString(Constants.PT_INPUT_AUTO_OPEN) != null || extras.getBoolean(Constants.PT_INPUT_AUTO_OPEN)) {
                    //adding delay for launcher
                    try {
                        Thread.sleep(Constants.PT_INPUT_TIMEOUT);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    Intent launchIntent;

                    if (extras.containsKey(Constants.WZRK_DL)) {
                        launchIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(intent.getStringExtra(Constants.WZRK_DL)));
                        Utils.setPackageNameFromResolveInfoList(context, launchIntent);
                    } else {
                        launchIntent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
                        if (launchIntent == null) {
                            return;
                        }
                    }

                    launchIntent.putExtras(extras);

                    //adding reply to extra
                    launchIntent.putExtra("pt_reply", reply);

                    launchIntent.removeExtra(Constants.WZRK_ACTIONS);
                    launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

                    context.startActivity(launchIntent);
                }

            } else {
                PTLog.verbose("PushTemplateReceiver: Input is Empty");
            }
        }

    }


    private void handleRatingNotification(Context context, Bundle extras) {

        try {
            int notificationId = extras.getInt(Constants.PT_NOTIF_ID);
            if (extras.getBoolean("default_dl", false)) {
                notificationManager.cancel(notificationId);
                Intent launchIntent;
                Class clazz = null;
                try {
                    clazz = Class.forName("com.clevertap.pushtemplates.PTNotificationIntentService");
                } catch (ClassNotFoundException ex) {
                    PTLog.debug("No Intent Service found");
                }

                boolean isPTIntentServiceAvailable = Utils.isServiceAvailable(context, clazz);
                if(isPTIntentServiceAvailable) {
                    launchIntent = new Intent(PTNotificationIntentService.MAIN_ACTION);
                    launchIntent.setPackage(context.getPackageName());
                    launchIntent.putExtra("pt_type", PTNotificationIntentService.TYPE_BUTTON_CLICK);
                    launchIntent.putExtras(extras);
                    launchIntent.putExtra("dl", pt_rating_default_dl);
                    context.startService(launchIntent);
                } else {
                    launchIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(pt_rating_default_dl));
                    launchIntent.removeExtra(Constants.WZRK_ACTIONS);
                    launchIntent.putExtra(Constants.WZRK_FROM_KEY, Constants.WZRK_FROM);
                    launchIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    Utils.raiseNotificationClicked(context,extras);
                    launchIntent.putExtras(extras);
                    launchIntent.putExtra(Constants.WZRK_DL, pt_rating_default_dl);
                    context.startActivity(launchIntent);
                }

                return;
            }
            //Set RemoteViews again
            contentViewRating = new RemoteViews(context.getPackageName(), R.layout.rating);
            setCustomContentViewBasicKeys(contentViewRating, context);

            contentViewSmall = new RemoteViews(context.getPackageName(), R.layout.content_view_small);

            setCustomContentViewBasicKeys(contentViewSmall, context);

            setCustomContentViewTitle(contentViewRating, pt_title);
            setCustomContentViewTitle(contentViewSmall, pt_title);

            setCustomContentViewMessage(contentViewRating, pt_msg);
            setCustomContentViewMessage(contentViewSmall, pt_msg);

            setCustomContentViewMessageSummary(contentViewRating, pt_msg_summary);

            setCustomContentViewTitleColour(contentViewRating, pt_title_clr);
            setCustomContentViewTitleColour(contentViewSmall, pt_title_clr);

            setCustomContentViewMessageColour(contentViewRating, pt_msg_clr);
            setCustomContentViewMessageColour(contentViewSmall, pt_msg_clr);

            setCustomContentViewExpandedBackgroundColour(contentViewRating, pt_bg);
            setCustomContentViewCollapsedBackgroundColour(contentViewSmall, pt_bg);

            String pt_dl_clicked = deepLinkList.get(0);

            HashMap<String, Object> map = new HashMap<String, Object>();
            if (clicked1 == extras.getBoolean("click1", false)) {
                contentViewRating.setImageViewResource(R.id.star1, R.drawable.pt_star_filled);
                map.put("Campaign", extras.getString("wzrk_id"));
                map.put("Rating", 1);
                cleverTapAPI.pushEvent("Rated", map);
                clicked1 = false;

                if (deepLinkList.size() > 0) {
                    pt_dl_clicked = deepLinkList.get(0);
                }
            } else {
                contentViewRating.setImageViewResource(R.id.star1, R.drawable.pt_star_outline);
            }
            if (clicked2 == extras.getBoolean("click2", false)) {
                contentViewRating.setImageViewResource(R.id.star1, R.drawable.pt_star_filled);
                contentViewRating.setImageViewResource(R.id.star2, R.drawable.pt_star_filled);
                map.put("Campaign", extras.getString("wzrk_id"));
                map.put("Rating", 2);
                cleverTapAPI.pushEvent("Rated", map);
                clicked2 = false;
                if (deepLinkList.size() > 1) {
                    pt_dl_clicked = deepLinkList.get(1);
                }else{
                    pt_dl_clicked = deepLinkList.get(0);
                }
            } else {
                contentViewRating.setImageViewResource(R.id.star2, R.drawable.pt_star_outline);
            }
            if (clicked3 == extras.getBoolean("click3", false)) {
                contentViewRating.setImageViewResource(R.id.star1, R.drawable.pt_star_filled);
                contentViewRating.setImageViewResource(R.id.star2, R.drawable.pt_star_filled);
                contentViewRating.setImageViewResource(R.id.star3, R.drawable.pt_star_filled);
                map.put("Campaign", extras.getString("wzrk_id"));
                map.put("Rating", 3);
                cleverTapAPI.pushEvent("Rated", map);
                clicked3 = false;
                if (deepLinkList.size() > 2) {
                    pt_dl_clicked = deepLinkList.get(2);
                }else{
                    pt_dl_clicked = deepLinkList.get(0);
                }
            } else {
                contentViewRating.setImageViewResource(R.id.star3, R.drawable.pt_star_outline);
            }
            if (clicked4 == extras.getBoolean("click4", false)) {
                contentViewRating.setImageViewResource(R.id.star1, R.drawable.pt_star_filled);
                contentViewRating.setImageViewResource(R.id.star2, R.drawable.pt_star_filled);
                contentViewRating.setImageViewResource(R.id.star3, R.drawable.pt_star_filled);
                contentViewRating.setImageViewResource(R.id.star4, R.drawable.pt_star_filled);
                map.put("Campaign", extras.getString("wzrk_id"));
                map.put("Rating", 4);
                cleverTapAPI.pushEvent("Rated", map);
                clicked4 = false;
                if (deepLinkList.size() > 3) {
                    pt_dl_clicked = deepLinkList.get(3);
                }else{
                    pt_dl_clicked = deepLinkList.get(0);
                }
            } else {
                contentViewRating.setImageViewResource(R.id.star4, R.drawable.pt_star_outline);
            }
            if (clicked5 == extras.getBoolean("click5", false)) {
                contentViewRating.setImageViewResource(R.id.star1, R.drawable.pt_star_filled);
                contentViewRating.setImageViewResource(R.id.star2, R.drawable.pt_star_filled);
                contentViewRating.setImageViewResource(R.id.star3, R.drawable.pt_star_filled);
                contentViewRating.setImageViewResource(R.id.star4, R.drawable.pt_star_filled);
                contentViewRating.setImageViewResource(R.id.star5, R.drawable.pt_star_filled);
                map.put("Campaign", extras.getString("wzrk_id"));
                map.put("Rating", 5);
                cleverTapAPI.pushEvent("Rated", map);
                clicked5 = false;
                if (deepLinkList.size() > 4) {
                    pt_dl_clicked = deepLinkList.get(4);
                }else{
                    pt_dl_clicked = deepLinkList.get(0);
                }
            } else {
                contentViewRating.setImageViewResource(R.id.star5, R.drawable.pt_star_outline);
            }

            setSmallIcon(context);

            NotificationCompat.Builder notificationBuilder;
            if (requiresChannelId) {
                notificationBuilder = new NotificationCompat.Builder(context, channelId);
            } else {
                notificationBuilder = new NotificationCompat.Builder(context);
            }

            if (notificationManager != null) {
                //Use the Builder to build notification
                notificationBuilder.setSmallIcon(smallIcon)
                        .setCustomContentView(contentViewSmall)
                        .setCustomBigContentView(contentViewRating)
                        .setContentTitle("Custom Notification")
                        .setAutoCancel(true);

                Notification notification = notificationBuilder.build();
                notificationManager.notify(notificationId, notification);
                setCustomContentViewSmallIcon(context,contentViewSmall,notification,notificationId);
                setCustomContentViewSmallIcon(context,contentViewRating,notification,notificationId);
                Thread.sleep(1000);
                notificationManager.cancel(notificationId);
                Toast.makeText(context, "Thank you for your feedback", Toast.LENGTH_SHORT).show();

                Intent it = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
                context.sendBroadcast(it);

                Intent launchIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(pt_dl_clicked));
                launchIntent.putExtras(extras);
                launchIntent.putExtra(Constants.WZRK_DL, pt_dl_clicked);
                launchIntent.removeExtra(Constants.WZRK_ACTIONS);
                launchIntent.putExtra(Constants.WZRK_FROM_KEY, Constants.WZRK_FROM);
                launchIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(launchIntent);
            }

        } catch (Throwable t) {
            PTLog.verbose("Error creating rating notification ", t);
        }
    }


    private void handleProductDisplayNotification(Context context, Bundle extras) {
        try {
            int notificationId = extras.getInt(Constants.PT_NOTIF_ID);
            if (buynow == extras.getBoolean("buynow", false)) {
                notificationManager.cancel(notificationId);
                String dl = extras.getString(Constants.PT_BUY_NOW_DL, deepLinkList.get(0));
                notificationManager.cancel(notificationId);
                Intent launchIntent;

                Class clazz = null;
                try {
                    clazz = Class.forName("com.clevertap.pushtemplates.PTNotificationIntentService");
                } catch (ClassNotFoundException ex) {
                    PTLog.debug("No Intent Service found");
                }

                boolean isPTIntentServiceAvailable = Utils.isServiceAvailable(context, clazz);
                if(isPTIntentServiceAvailable) {
                    launchIntent = new Intent(PTNotificationIntentService.MAIN_ACTION);
                    launchIntent.putExtras(extras);
                    launchIntent.putExtra("dl", dl);
                    launchIntent.setPackage(context.getPackageName());
                    launchIntent.putExtra("pt_type", PTNotificationIntentService.TYPE_BUTTON_CLICK);
                    context.startService(launchIntent);
                } else {
                    launchIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(dl));
                    launchIntent.putExtras(extras);
                    launchIntent.putExtra(Constants.WZRK_DL, dl);
                    launchIntent.removeExtra(Constants.WZRK_ACTIONS);
                    launchIntent.putExtra(Constants.WZRK_FROM_KEY, Constants.WZRK_FROM);
                    launchIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    Utils.raiseNotificationClicked(context,extras);
                    context.startActivity(launchIntent);
                }
                return;
            }

            boolean isLinear = false;
            if (pt_product_display_linear == null || pt_product_display_linear.isEmpty()) {
                contentViewBig = new RemoteViews(context.getPackageName(), R.layout.product_display_template);
                contentViewSmall = new RemoteViews(context.getPackageName(), R.layout.content_view_small);
            }else{
                isLinear = true;
                contentViewBig = new RemoteViews(context.getPackageName(), R.layout.product_display_linear_expanded);
                contentViewSmall = new RemoteViews(context.getPackageName(), R.layout.product_display_linear_collapsed);
            }

            setCustomContentViewBasicKeys(contentViewBig, context);
            if(!isLinear) {
                setCustomContentViewBasicKeys(contentViewSmall, context);
            }

            if (!bigTextList.isEmpty()) {
                contentViewBig.setTextViewText(R.id.product_name, bigTextList.get(0));

            }

            if(!isLinear) {
                if (!smallTextList.isEmpty()) {
                    contentViewBig.setTextViewText(R.id.product_description, smallTextList.get(0));
                }
            }

            if (!priceList.isEmpty()) {
                contentViewBig.setTextViewText(R.id.product_price, priceList.get(0));

            }

            if(!isLinear) {
                setCustomContentViewTitle(contentViewBig, pt_title);
                setCustomContentViewTitle(contentViewSmall, pt_title);
                setCustomContentViewMessage(contentViewBig, pt_msg);
                setCustomContentViewMessageColour(contentViewBig, pt_msg_clr);
                setCustomContentViewTitleColour(contentViewBig, pt_title_clr);
                setCustomContentViewTitleColour(contentViewSmall, pt_title_clr);
            }

            if(isLinear){
                Intent notificationSmallIntent1 = new Intent(context, CTPushNotificationReceiver.class);
                PendingIntent contentSmallIntent1 = setPendingIntent(context,notificationId,extras,notificationSmallIntent1,deepLinkList.get(0));
                contentViewSmall.setOnClickPendingIntent(R.id.small_image1_collapsed, contentSmallIntent1);

                Intent notificationSmallIntent2 = new Intent(context, CTPushNotificationReceiver.class);
                PendingIntent contentSmallIntent2 = setPendingIntent(context,notificationId,extras,notificationSmallIntent2,deepLinkList.get(1));
                contentViewSmall.setOnClickPendingIntent(R.id.small_image2_collapsed, contentSmallIntent2);

                Intent notificationSmallIntent3 = new Intent(context, CTPushNotificationReceiver.class);
                PendingIntent contentSmallIntent3 = setPendingIntent(context,notificationId,extras,notificationSmallIntent3,deepLinkList.get(2));
                contentViewSmall.setOnClickPendingIntent(R.id.small_image3_collapsed, contentSmallIntent3);

            }

            setCustomContentViewMessage(contentViewSmall, pt_msg);
            setCustomContentViewMessageColour(contentViewSmall, pt_msg_clr);


            setCustomContentViewExpandedBackgroundColour(contentViewBig, pt_bg);
            setCustomContentViewCollapsedBackgroundColour(contentViewSmall, pt_bg);

            setCustomContentViewButtonLabel(contentViewBig, R.id.product_action, pt_product_display_action);
            setCustomContentViewButtonColour(contentViewBig, R.id.product_action, pt_product_display_action_clr);
            setCustomContentViewButtonText(contentViewBig, R.id.product_action, pt_product_display_action_text_clr);

            String imageUrl = "", dl = "";
            if (img1 != extras.getBoolean("img1", false)) {
                imageUrl = imageList.get(0);
                if (!bigTextList.isEmpty()) {
                    contentViewBig.setTextViewText(R.id.product_name, bigTextList.get(0));
                    contentViewBig.setTextViewText(R.id.product_description, smallTextList.get(0));
                    contentViewBig.setTextViewText(R.id.product_price, priceList.get(0));
                }
                img1 = false;
                dl = deepLinkList.get(0);
            }
            if (img2 != extras.getBoolean("img2", false)) {
                imageUrl = imageList.get(1);
                if (!bigTextList.isEmpty()) {
                    contentViewBig.setTextViewText(R.id.product_name, bigTextList.get(1));
                    contentViewBig.setTextViewText(R.id.product_description, smallTextList.get(1));
                    contentViewBig.setTextViewText(R.id.product_price, priceList.get(1));
                }
                img2 = false;
                dl = deepLinkList.get(1);
            }
            if (img3 != extras.getBoolean("img3", false)) {
                imageUrl = imageList.get(2);
                if (!bigTextList.isEmpty()) {
                    contentViewBig.setTextViewText(R.id.product_name, bigTextList.get(2));
                    contentViewBig.setTextViewText(R.id.product_description, smallTextList.get(2));
                    contentViewBig.setTextViewText(R.id.product_price, priceList.get(2));
                }
                img3 = false;
                dl = deepLinkList.get(2);
            }

            Intent launchIntent = new Intent(context, CTPushNotificationReceiver.class);

            PendingIntent pIntent;

            if (bigimage == extras.getBoolean("bigimage", false)) {
                bigimage = false;
            }


            int requestCode1 = extras.getInt("pt_reqcode1");
            int requestCode2 = extras.getInt("pt_reqcode2");
            int requestCode3 = extras.getInt("pt_reqcode3");

            Intent notificationIntent1 = new Intent(context, PushTemplateReceiver.class);
            notificationIntent1.putExtra("img1", true);
            notificationIntent1.putExtra(Constants.PT_NOTIF_ID, notificationId);
            notificationIntent1.putExtra(Constants.PT_BUY_NOW_DL, deepLinkList.get(0));
            notificationIntent1.putExtra("pt_reqcode1", requestCode1);
            notificationIntent1.putExtra("pt_reqcode2", requestCode2);
            notificationIntent1.putExtra("pt_reqcode3", requestCode3);
            notificationIntent1.putExtras(extras);
            PendingIntent contentIntent1 = PendingIntent.getBroadcast(context, requestCode1, notificationIntent1, 0);
            contentViewBig.setOnClickPendingIntent(R.id.small_image1, contentIntent1);

            Intent notificationIntent2 = new Intent(context, PushTemplateReceiver.class);
            notificationIntent2.putExtra("img2", true);
            notificationIntent2.putExtra(Constants.PT_NOTIF_ID, notificationId);
            notificationIntent2.putExtra(Constants.PT_BUY_NOW_DL, deepLinkList.get(1));
            notificationIntent2.putExtra("pt_reqcode1", requestCode1);
            notificationIntent2.putExtra("pt_reqcode2", requestCode2);
            notificationIntent2.putExtra("pt_reqcode3", requestCode3);
            notificationIntent2.putExtras(extras);
            PendingIntent contentIntent2 = PendingIntent.getBroadcast(context, requestCode2, notificationIntent2, 0);
            contentViewBig.setOnClickPendingIntent(R.id.small_image2, contentIntent2);

            Intent notificationIntent3 = new Intent(context, PushTemplateReceiver.class);
            notificationIntent3.putExtra("img3", true);
            notificationIntent3.putExtra(Constants.PT_NOTIF_ID, notificationId);
            notificationIntent3.putExtra(Constants.PT_BUY_NOW_DL, deepLinkList.get(2));
            notificationIntent3.putExtra("pt_reqcode1", requestCode1);
            notificationIntent3.putExtra("pt_reqcode2", requestCode2);
            notificationIntent3.putExtra("pt_reqcode3", requestCode3);
            notificationIntent3.putExtras(extras);
            PendingIntent contentIntent3 = PendingIntent.getBroadcast(context, requestCode3, notificationIntent3, 0);
            contentViewBig.setOnClickPendingIntent(R.id.small_image3, contentIntent3);

            Intent notificationIntent4 = new Intent(context, PushTemplateReceiver.class);
            notificationIntent4.putExtra("img1", true);
            notificationIntent4.putExtra(Constants.PT_NOTIF_ID, notificationId);
            notificationIntent4.putExtra(Constants.PT_BUY_NOW_DL, dl);
            notificationIntent4.putExtra("pt_reqcode1", requestCode1);
            notificationIntent4.putExtra("pt_reqcode2", requestCode2);
            notificationIntent4.putExtra("pt_reqcode3", requestCode3);
            notificationIntent4.putExtra("buynow", true);
            notificationIntent4.putExtras(extras);
            PendingIntent contentIntent4 = PendingIntent.getBroadcast(context, new Random().nextInt(), notificationIntent4, 0);
            contentViewBig.setOnClickPendingIntent(R.id.product_action, contentIntent4);

            setSmallIcon(context);

            NotificationCompat.Builder notificationBuilder = setBuilderWithChannelIDCheck(requiresChannelId, channelId, context);

            if (deepLinkList != null) {
                pIntent = setPendingIntent(context, notificationId, extras, launchIntent, deepLinkList.get(0));
            } else {
                pIntent = setPendingIntent(context, notificationId, extras, launchIntent, null);
            }


            if (notificationManager != null) {
                //Use the Builder to build notification
                setNotificationBuilderBasics(notificationBuilder, contentViewSmall, contentViewBig, pt_title, pIntent);

                Notification notification = notificationBuilder.build();
                notificationManager.notify(notificationId, notification);

                setCustomContentViewSmallIcon(context,contentViewSmall,notification,notificationId);
                if(!isLinear) {
                    setCustomContentViewSmallIcon(context,contentViewSmall,notification,notificationId);

                }
                for (int index = 0; index < imageList.size(); index++) {
                    if (index == 0) {
                        Utils.loadIntoGlide(context, R.id.small_image1, imageList.get(0), contentViewBig, notification, notificationId);
                        if(isLinear) {
                            Utils.loadIntoGlide(context, R.id.small_image1, imageList.get(0), contentViewSmall, notification, notificationId);
                        }
                    } else if (index == 1) {
                        Utils.loadIntoGlide(context, R.id.small_image2, imageList.get(1), contentViewBig, notification, notificationId);
                        if(isLinear) {
                            Utils.loadIntoGlide(context, R.id.small_image2, imageList.get(1), contentViewSmall, notification, notificationId);
                        }
                    } else if (index == 2) {
                        Utils.loadIntoGlide(context, R.id.small_image3, imageList.get(2), contentViewBig, notification, notificationId);
                        if(isLinear) {
                            Utils.loadIntoGlide(context, R.id.small_image3, imageList.get(2), contentViewSmall, notification, notificationId);
                        }
                    }
                }
                setCustomContentViewSmallIcon(context,contentViewBig,notification,notificationId);

                Utils.loadIntoGlide(context, R.id.big_image, imageUrl, contentViewBig, notification, notificationId);
            }

        } catch (Throwable t) {
            PTLog.verbose("Error creating rating notification ", t);
        }
    }

    private void handleFiveCTANotification(Context context, Bundle extras) {
        String dl = null;

        int notificationId = extras.getInt(Constants.PT_NOTIF_ID);
        if (cta1 == extras.getBoolean("cta1")) {
            dl = deepLinkList.get(0);
        }
        if (cta2 == extras.getBoolean("cta2")) {
            dl = deepLinkList.get(1);
        }
        if (cta3 == extras.getBoolean("cta3")) {
            dl = deepLinkList.get(2);
        }
        if (cta4 == extras.getBoolean("cta4")) {
            dl = deepLinkList.get(3);
        }
        if (cta5 == extras.getBoolean("cta5")) {
            dl = deepLinkList.get(4);
        }
        if (close == extras.getBoolean("close")) {
            notificationManager.cancel(notificationId);
            context.sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
            return;

        }

        Intent launchIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(dl));
        launchIntent.putExtras(extras);
        launchIntent.putExtra(Constants.WZRK_DL, dl);
        launchIntent.removeExtra(Constants.WZRK_ACTIONS);
        launchIntent.putExtra(Constants.WZRK_FROM_KEY, Constants.WZRK_FROM);
        launchIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(launchIntent);
        context.sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
    }

    private PendingIntent setPendingIntent(Context context, int notificationId, Bundle extras, Intent launchIntent, String dl) {
        launchIntent.putExtras(extras);
        launchIntent.putExtra(Constants.PT_NOTIF_ID, notificationId);
        if (dl != null) {
            launchIntent.putExtra("default_dl", true);
            launchIntent.putExtra(Constants.WZRK_DL, dl);
        }
        launchIntent.removeExtra(Constants.WZRK_ACTIONS);
        launchIntent.putExtra(Constants.WZRK_FROM_KEY, Constants.WZRK_FROM);
        launchIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        return PendingIntent.getBroadcast(context, (int) System.currentTimeMillis(),
                launchIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private void setNotificationBuilderBasics(NotificationCompat.Builder notificationBuilder, RemoteViews contentViewSmall, RemoteViews contentViewBig, String pt_title, PendingIntent pIntent) {
        notificationBuilder.setSmallIcon(smallIcon)
                .setCustomContentView(contentViewSmall)
                .setCustomBigContentView(contentViewBig)
                .setContentTitle(pt_title)
                .setContentIntent(pIntent)
                .setVibrate(new long[]{0L})
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(true);
    }

    private void setCustomContentViewLargeIcon(RemoteViews contentView, String pt_large_icon, Context context, Notification notification, int notificationId) {
        if (pt_large_icon != null && !pt_large_icon.isEmpty()) {
            Utils.loadIntoGlide(context, R.id.large_icon, pt_large_icon, contentView, notification, notificationId);
        } else {
            contentView.setViewVisibility(R.id.large_icon, View.GONE);
        }
    }

    private NotificationCompat.Builder setBuilderWithChannelIDCheck(boolean requiresChannelId, String channelId, Context context) {
        if (requiresChannelId) {
            return new NotificationCompat.Builder(context, channelId);
        } else {
            return new NotificationCompat.Builder(context);
        }
    }

    private void setCustomContentViewBasicKeys(RemoteViews contentView, Context context) {
        contentView.setTextViewText(R.id.app_name, Utils.getApplicationName(context));
        contentView.setTextViewText(R.id.timestamp, Utils.getTimeStamp(context));
    }

    private void setCustomContentViewButtonColour(RemoteViews contentView, int resourceID, String pt_product_display_action_clr) {
        if (pt_product_display_action_clr != null && !pt_product_display_action_clr.isEmpty()) {
            contentView.setInt(resourceID, "setBackgroundColor", Color.parseColor(pt_product_display_action_clr));
        }
    }

    private void setCustomContentViewButtonLabel(RemoteViews contentView, int resourceID, String pt_product_display_action) {
        if (pt_product_display_action != null && !pt_product_display_action.isEmpty()) {
            contentView.setTextViewText(resourceID, pt_product_display_action);
        }
    }


    private void setCustomContentViewMessageSummary(RemoteViews contentView, String pt_msg_summary) {
        if (pt_msg_summary != null && !pt_msg_summary.isEmpty()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                contentView.setTextViewText(R.id.msg, Html.fromHtml(pt_msg_summary, Html.FROM_HTML_MODE_LEGACY));
            } else {
                contentView.setTextViewText(R.id.msg, Html.fromHtml(pt_msg_summary));
            }
        }
    }

    private void setCustomContentViewMessageColour(RemoteViews contentView, String pt_msg_clr) {
        if (pt_msg_clr != null && !pt_msg_clr.isEmpty()) {
            contentView.setTextColor(R.id.msg, Color.parseColor(pt_msg_clr));
        }
    }

    private void setCustomContentViewTitleColour(RemoteViews contentView, String pt_title_clr) {
        if (pt_title_clr != null && !pt_title_clr.isEmpty()) {
            contentView.setTextColor(R.id.title, Color.parseColor(pt_title_clr));
        }
    }

    private void setCustomContentViewExpandedBackgroundColour(RemoteViews contentView, String pt_bg) {
        if (pt_bg != null && !pt_bg.isEmpty()) {
            contentView.setInt(R.id.content_view_big, "setBackgroundColor", Color.parseColor(pt_bg));
        }
    }

    private void setCustomContentViewCollapsedBackgroundColour(RemoteViews contentView, String pt_bg) {
        if (pt_bg != null && !pt_bg.isEmpty()) {
            contentView.setInt(R.id.content_view_small, "setBackgroundColor", Color.parseColor(pt_bg));
        }
    }

    private void setCustomContentViewMessage(RemoteViews contentView, String pt_msg) {
        if (pt_msg != null && !pt_msg.isEmpty()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                contentView.setTextViewText(R.id.msg, Html.fromHtml(pt_msg, Html.FROM_HTML_MODE_LEGACY));
            } else {
                contentView.setTextViewText(R.id.msg, Html.fromHtml(pt_msg));
            }
        }
    }

    private void setCustomContentViewTitle(RemoteViews contentView, String pt_title) {
        if (pt_title != null && !pt_title.isEmpty()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                contentView.setTextViewText(R.id.title, Html.fromHtml(pt_title, Html.FROM_HTML_MODE_LEGACY));
            } else {
                contentView.setTextViewText(R.id.title, Html.fromHtml(pt_title));
            }
        }
    }


    private void setStandardViewBigImageStyle(String imgUrl, Bundle extras, Context context, NotificationCompat.Builder notificationBuilder) {
        NotificationCompat.Style bigPictureStyle;
        if (imgUrl != null && imgUrl.startsWith("http")) {
            try {
                Bitmap bpMap = Utils.getNotificationBitmap(imgUrl, false, context);

                if (bpMap == null)
                    throw new Exception("Failed to fetch big picture!");

                if (extras.containsKey(Constants.PT_MSG_SUMMARY)) {
                    String summaryText = pt_msg_summary;
                    bigPictureStyle = new NotificationCompat.BigPictureStyle()
                            .setSummaryText(summaryText)
                            .bigPicture(bpMap);
                } else {
                    bigPictureStyle = new NotificationCompat.BigPictureStyle()
                            .setSummaryText(pt_msg)
                            .bigPicture(bpMap);
                }
            } catch (Throwable t) {
                bigPictureStyle = new NotificationCompat.BigTextStyle()
                        .bigText(pt_msg);
                PTLog.verbose("Falling back to big text notification, couldn't fetch big picture", t);
            }
        } else {
            bigPictureStyle = new NotificationCompat.BigTextStyle()
                    .bigText(pt_msg);
        }

        notificationBuilder.setStyle(bigPictureStyle);

    }

    private void setCustomContentViewSmallIcon(Context context, RemoteViews contentView, Notification notification, int notificationId) {
        if (pt_small_icon != null){
            Utils.loadIntoGlide(context, R.id.small_icon, pt_small_icon, contentView, notification, notificationId);
        }else{
            Utils.loadIntoGlide(context, R.id.small_icon, smallIcon, contentView, notification, notificationId);
        }
    }

    private void setSmallIcon(Context context) {
        Bundle metaData;
        try {
            PackageManager pm = context.getPackageManager();
            ApplicationInfo ai = pm.getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            metaData = ai.metaData;
            String x = Utils._getManifestStringValueForKey(metaData, Constants.LABEL_NOTIFICATION_ICON);
            if (x == null) throw new IllegalArgumentException();
            smallIcon = context.getResources().getIdentifier(x, "drawable", context.getPackageName());
            if (smallIcon == 0) throw new IllegalArgumentException();
        } catch (Throwable t) {
            smallIcon = Utils.getAppIconAsIntId(context);
        }
        pt_small_icon = setSmallIconColour(context,smallIcon,pt_small_icon_clr);

    }

    private Bitmap setSmallIconColour(Context context, int resourceID, String clr) {
        if (clr != null && !clr.isEmpty()) {
            int color = Color.parseColor(clr);

            Drawable mDrawable = ContextCompat.getDrawable(context, resourceID).mutate();
            mDrawable.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN));
            Bitmap nBP = drawableToBitmap(mDrawable);
            return nBP;
        }
        return null;
    }

    private static Bitmap drawableToBitmap(Drawable drawable)
            throws NullPointerException {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    private void setCustomContentViewButtonText(RemoteViews contentView, int resourceID, String pt_product_display_action_text_clr) {
        if (pt_product_display_action_text_clr != null && !pt_product_display_action_text_clr.isEmpty()) {
            contentView.setTextColor(resourceID, Color.parseColor(pt_product_display_action_text_clr));
        }
    }

}
