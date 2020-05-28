package com.clevertap.pushtemplates;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.RemoteInput;
import androidx.core.content.ContextCompat;

import android.os.SystemClock;
import android.text.Html;
import android.view.View;
import android.widget.RemoteViews;

import com.clevertap.android.sdk.CTPushNotificationReceiver;
import com.clevertap.android.sdk.CleverTapAPI;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Random;

import static android.content.Context.NOTIFICATION_SERVICE;

@SuppressWarnings("FieldCanBeLocal")
public class TemplateRenderer {

    private static int debugLevel = TemplateRenderer.LogLevel.INFO.intValue();

    private String pt_id;
    private TemplateType templateType;
    private String pt_title;
    private String pt_msg;
    private String pt_msg_summary;
    private String pt_large_icon;
    private String pt_big_img;
    private String pt_title_clr, pt_msg_clr;
    private ArrayList<String> imageList;
    private ArrayList<String> deepLinkList;
    private ArrayList<String> bigTextList;
    private ArrayList<String> smallTextList;
    private ArrayList<String> priceList;
    private String pt_bg;
    private String pt_rating_default_dl;
    private RemoteViews contentViewBig, contentViewSmall, contentViewCarousel, contentViewRating,
             contentFiveCTAs, contentViewTimer, contentViewTimerCollapsed;
    private String channelId;
    private int smallIcon = 0;
    private boolean requiresChannelId;
    private NotificationManager notificationManager;
    private AsyncHelper asyncHelper;
    private DBHelper dbHelper;
    private int pt_timer_threshold;
    private String pt_input_label;
    private String pt_input_feedback;
    private String pt_input_auto_open;



    @SuppressWarnings({"unused"})
    public enum LogLevel {
        OFF(-1),
        INFO(0),
        DEBUG(2),
        VERBOSE(3);

        private final int value;

        LogLevel(final int newValue) {
            value = newValue;
        }

        public int intValue() {
            return value;
        }
    }

    /**
     * Enables or disables debugging. If enabled, see debug messages in Android's logcat utility.
     * Debug messages are tagged as PTLog.
     *
     * @param level Can be one of the following:  -1 (disables all debugging), 0 (default, shows minimal SDK integration related logging),
     *              2(shows debug output)
     */
    public static void setDebugLevel(int level){
        debugLevel = level;
    }

    /**
     * Returns the log level set for PushTemplates
     *
     * @return The int value
     */
    @SuppressWarnings("WeakerAccess")
    public static int getDebugLevel() {
        return debugLevel;
    }

    private TemplateRenderer(Context context, Bundle extras) {
        pt_id = extras.getString(Constants.PT_ID);
        String pt_json = extras.getString(Constants.PT_JSON);
        if (pt_id != null) {
            templateType = TemplateType.fromString(pt_id);
            Bundle newExtras = null;
            try {
                if (pt_json != null && !pt_json.isEmpty()) {
                    newExtras = Utils.fromJson(new JSONObject(pt_json));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (newExtras != null) extras.putAll(newExtras);
        }
        pt_msg = extras.getString(Constants.PT_MSG);
        pt_msg_summary = extras.getString(Constants.PT_MSG_SUMMARY);
        pt_msg_clr = extras.getString(Constants.PT_MSG_COLOR);
        pt_title = extras.getString(Constants.PT_TITLE);
        pt_title_clr = extras.getString(Constants.PT_TITLE_COLOR);
        pt_bg = extras.getString(Constants.PT_BG);
        pt_big_img = extras.getString(Constants.PT_BIG_IMG);
        pt_large_icon = extras.getString(Constants.PT_NOTIF_ICON);
        imageList = Utils.getImageListFromExtras(extras);
        deepLinkList = Utils.getDeepLinkListFromExtras(extras);
        bigTextList = Utils.getBigTextFromExtras(extras);
        smallTextList = Utils.getSmallTextFromExtras(extras);
        priceList = Utils.getPriceFromExtras(extras);
        pt_rating_default_dl = extras.getString(Constants.PT_DEFAULT_DL);
        asyncHelper = AsyncHelper.getInstance();
        dbHelper = new DBHelper(context);
        pt_timer_threshold = Utils.getTimerThreshold(extras);
        pt_input_label = extras.getString(Constants.PT_INPUT_LABEL);
        pt_input_feedback = extras.getString(Constants.PT_INPUT_FEEDBACK);
        pt_input_auto_open = extras.getString(Constants.PT_INPUT_AUTO_OPEN);
    }

    @SuppressWarnings("WeakerAccess")
    @SuppressLint("NewApi")
    public static void createNotification(Context context, Bundle extras) {
        PTLog.verbose("Creating notification...");
        TemplateRenderer templateRenderer = new TemplateRenderer(context, extras);
        templateRenderer.dupeCheck(context, extras, Constants.EMPTY_NOTIFICATION_ID);
    }

    private synchronized void dupeCheck(final Context context, final Bundle extras, int id) {
        try {
            asyncHelper.postAsyncSafely("TemplateRenderer#_createNotification", new Runnable() {
                @RequiresApi(api = Build.VERSION_CODES.O)
                @Override
                public void run() {
                    try {
                        if(extras.getString(Constants.WZRK_PUSH_ID) != null || !extras.getString(Constants.WZRK_PUSH_ID).isEmpty()) {
                            String ptID = extras.getString(Constants.WZRK_PUSH_ID);
                            if(!dbHelper.isNotificationPresentInDB(ptID)){
                                _createNotification(context, extras, Constants.EMPTY_NOTIFICATION_ID);
                                dbHelper.savePT(ptID, Utils.bundleToJSON(extras));
                            }
                        }else {
                            _createNotification(context, extras, Constants.EMPTY_NOTIFICATION_ID);
                        }

                    } catch (Throwable t) {
                        PTLog.verbose("Couldn't render notification: " + t.getLocalizedMessage());
                    }
                }
            });
        } catch (Throwable t) {
            PTLog.verbose("Failed to process push notification: " + t.getLocalizedMessage());
        }
    }

    @SuppressWarnings("SameParameterValue")
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void _createNotification(Context context, Bundle extras, int notificationId) {
        if (pt_id == null) {
            PTLog.verbose("Template ID not provided. Cannot create the notification");
            return;
        }

        notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        channelId = extras.getString(Constants.WZRK_CHANNEL_ID, "");
        requiresChannelId = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O;

        if (requiresChannelId) {
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

        switch (templateType) {
            case BASIC:
                if (hasAllBasicNotifKeys())
                    renderBasicTemplateNotification(context, extras, notificationId);
                break;
            case AUTO_CAROUSEL:
                if (hasAllCarouselNotifKeys())
                    renderAutoCarouselNotification(context, extras, notificationId);
                break;
            case RATING:
                if (hasAllRatingNotifKeys())
                    renderRatingNotification(context, extras, notificationId);
                break;
            case FIVE_ICONS:
                if (hasAll5IconNotifKeys())
                    renderFiveIconNotification(context, extras, notificationId);
                break;
            case PRODUCT_DISPLAY:
                if (hasAllProdDispNotifKeys())
                    renderProductDisplayNotification(context, extras, notificationId);
                break;
            case TIMER:
                if (hasAllTimerKeys())
                    renderTimerNotification(context, extras, notificationId);
                break;
            case INPUT_BOX:
                if (hasAllInputBoxKeys())
                    renderInputBoxNotification(context, extras, notificationId);
                break;
        }
    }

    private boolean hasAllBasicNotifKeys() {
        boolean result = true;
        if (pt_title == null || pt_title.isEmpty()) {
            PTLog.verbose("Title is missing or empty. Not showing notification");
            result = false;
        }
        if (pt_msg == null || pt_msg.isEmpty()) {
            PTLog.verbose("Message is missing or empty. Not showing notification");
            result = false;
        }

        if (pt_big_img == null || pt_big_img.isEmpty()) {
            PTLog.verbose("Display Image is missing or empty. Not showing notification");
            result = false;
        }
        if (pt_large_icon == null || pt_large_icon.isEmpty()) {
            PTLog.verbose("Icon Image is missing or empty. Not showing notification");
            result = false;
        }

        return result;
    }

    private boolean hasAllCarouselNotifKeys() {
        boolean result = true;
        if (pt_title == null || pt_title.isEmpty()) {
            PTLog.verbose("Title is missing or empty. Not showing notification");
            result = false;
        }
        if (pt_msg == null || pt_msg.isEmpty()) {
            PTLog.verbose("Message is missing or empty. Not showing notification");
            result = false;
        }
        if (deepLinkList == null || deepLinkList.size() == 0) {
            PTLog.verbose("Deeplink is missing or empty. Not showing notification");
            result = false;
        }
        if (imageList == null || imageList.size() < 3) {
            PTLog.verbose("Three required images not present. Not showing notification");
            result = false;
        }
        return result;
    }

    private boolean hasAllRatingNotifKeys() {
        boolean result = true;
        if (pt_title == null || pt_title.isEmpty()) {
            PTLog.verbose("Title is missing or empty. Not showing notification");
            result = false;
        }
        if (pt_msg == null || pt_msg.isEmpty()) {
            PTLog.verbose("Message is missing or empty. Not showing notification");
            result = false;
        }
        if (pt_rating_default_dl == null || pt_rating_default_dl.isEmpty()) {
            PTLog.verbose("Default deeplink is missing or empty. Not showing notification");
            result = false;
        }

        if (deepLinkList == null || deepLinkList.size() == 0) {
            PTLog.verbose("At least one deeplink is required. Not showing notification");
            result = false;
        }
        return result;
    }

    private boolean hasAll5IconNotifKeys() {
        boolean result = true;
        if (deepLinkList == null || deepLinkList.size() < 5) {
            PTLog.verbose("Five required deeplinks not present. Not showing notification");
            result = false;
        }
        if (imageList == null || imageList.size() < 5) {
            PTLog.verbose("Five required images not present. Not showing notification");
            result = false;
        }
        return result;
    }

    private boolean hasAllProdDispNotifKeys() {
        boolean result = true;
        if (pt_title == null || pt_title.isEmpty()) {
            PTLog.verbose("Title is missing or empty. Not showing notification");
            result = false;
        }
        if (pt_msg == null || pt_msg.isEmpty()) {
            PTLog.verbose("Message is missing or empty. Not showing notification");
            result = false;
        }
        if (bigTextList == null || bigTextList.size() < 3) {
            PTLog.verbose("Three required product titles not present. Not showing notification");
            result = false;
        }
        if (smallTextList == null || smallTextList.size() < 3) {
            PTLog.verbose("Three required product descriptions not present. Not showing notification");
            result = false;
        }
        if (deepLinkList == null || deepLinkList.size() < 3) {
            PTLog.verbose("Three required deeplinks not present. Not showing notification");
            result = false;
        }
        if (imageList == null || imageList.size() < 3) {
            PTLog.verbose("Three required images not present. Not showing notification");
            result = false;
        }
        return result;
    }

    private boolean hasAllTimerKeys() {
        boolean result = true;
        if (deepLinkList == null || deepLinkList.size() == 0) {
            PTLog.verbose("Deeplink not present. Not showing notification");
            result = false;
        }
        if (pt_title == null || pt_title.isEmpty()) {
            PTLog.verbose("Title is missing or empty. Not showing notification");
            result = false;
        }
        if (pt_msg == null || pt_msg.isEmpty()) {
            PTLog.verbose("Message is missing or empty. Not showing notification");
            result = false;
        }
        if (pt_timer_threshold == -1) {
            PTLog.verbose("Timer Threshold not defined. Not showing notification");
            result = false;
        }
        return result;
    }

    private boolean hasAllInputBoxKeys() {
        boolean result = true;
        if (deepLinkList == null || deepLinkList.size() == 0) {
            PTLog.verbose("Deeplink is not present. Not showing notification");
            result = false;
        }
        if (pt_title == null || pt_title.isEmpty()) {
            PTLog.verbose("Title is missing or empty. Not showing notification");
            result = false;
        }
        if (pt_msg == null || pt_msg.isEmpty()) {
            PTLog.verbose("Message is missing or empty. Not showing notification");
            result = false;
        }
        if (pt_msg_summary == null || pt_msg_summary.isEmpty()) {
            PTLog.verbose("Message is missing or empty. Not showing notification");
            result = false;
        }
        if (pt_input_label == null || pt_input_label.isEmpty()) {
            PTLog.verbose("Input Label is missing or empty. Not showing notification");
            result = false;
        }
        if (pt_input_feedback == null || pt_input_feedback.isEmpty()) {
            PTLog.verbose("Feedback Text is missing or empty. Not showing notification");
            result = false;
        }
        return result;
    }

    private void renderRatingNotification(Context context, Bundle extras, int notificationId) {
        try {
            contentViewRating = new RemoteViews(context.getPackageName(), R.layout.rating);
            contentViewRating.setTextViewText(R.id.app_name, Utils.getApplicationName(context));
            contentViewRating.setTextViewText(R.id.timestamp, Utils.getTimeStamp(context));

            contentViewSmall = new RemoteViews(context.getPackageName(), R.layout.content_view_small);
            contentViewSmall.setTextViewText(R.id.app_name, Utils.getApplicationName(context));
            contentViewSmall.setTextViewText(R.id.timestamp, Utils.getTimeStamp(context));

            contentViewRating.setTextColor(R.id.app_name, ContextCompat.getColor(context,R.color.gray));
            contentViewSmall.setTextColor(R.id.app_name, ContextCompat.getColor(context,R.color.gray));
            contentViewRating.setTextColor(R.id.timestamp, ContextCompat.getColor(context,R.color.gray));
            contentViewSmall.setTextColor(R.id.timestamp, ContextCompat.getColor(context,R.color.gray));

            if (pt_title != null && !pt_title.isEmpty()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    contentViewRating.setTextViewText(R.id.title, Html.fromHtml(pt_title, Html.FROM_HTML_MODE_LEGACY));
                    contentViewSmall.setTextViewText(R.id.title, Html.fromHtml(pt_title, Html.FROM_HTML_MODE_LEGACY));
                } else {
                    contentViewRating.setTextViewText(R.id.title, Html.fromHtml(pt_title));
                    contentViewSmall.setTextViewText(R.id.title, Html.fromHtml(pt_title));
                }
            }

            if (pt_msg != null && !pt_msg.isEmpty()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    contentViewSmall.setTextViewText(R.id.msg, Html.fromHtml(pt_msg, Html.FROM_HTML_MODE_LEGACY));
                    contentViewRating.setTextViewText(R.id.msg, Html.fromHtml(pt_msg, Html.FROM_HTML_MODE_LEGACY));
                } else {
                    contentViewSmall.setTextViewText(R.id.msg, Html.fromHtml(pt_msg));
                    contentViewRating.setTextViewText(R.id.msg, Html.fromHtml(pt_msg));
                }
            }
            if (pt_msg_summary != null && !pt_msg_summary.isEmpty()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    contentViewRating.setTextViewText(R.id.msg, Html.fromHtml(pt_msg_summary, Html.FROM_HTML_MODE_LEGACY));
                } else {
                    contentViewRating.setTextViewText(R.id.msg, Html.fromHtml(pt_msg_summary));
                }
            }

            if (pt_title_clr != null && !pt_title_clr.isEmpty()) {
                contentViewRating.setTextColor(R.id.title, Color.parseColor(pt_title_clr));
                contentViewSmall.setTextColor(R.id.title, Color.parseColor(pt_title_clr));
            }

            if (pt_msg_clr != null && !pt_msg_clr.isEmpty()) {
                contentViewRating.setTextColor(R.id.msg, Color.parseColor(pt_msg_clr));
                contentViewSmall.setTextColor(R.id.msg, Color.parseColor(pt_msg_clr));
            }

            //Set the rating stars
            contentViewRating.setImageViewResource(R.id.star1, R.drawable.outline_star_1);
            contentViewRating.setImageViewResource(R.id.star2, R.drawable.outline_star_1);
            contentViewRating.setImageViewResource(R.id.star3, R.drawable.outline_star_1);
            contentViewRating.setImageViewResource(R.id.star4, R.drawable.outline_star_1);
            contentViewRating.setImageViewResource(R.id.star5, R.drawable.outline_star_1);

            notificationId = new Random().nextInt();

            //Set Pending Intents for each star to listen to click

            Intent notificationIntent1 = new Intent(context, PushTemplateReceiver.class);
            notificationIntent1.putExtra("click1", true);
            notificationIntent1.putExtra("notif_id", notificationId);
            notificationIntent1.putExtras(extras);
            PendingIntent contentIntent1 = PendingIntent.getBroadcast(context, new Random().nextInt(), notificationIntent1, 0);
            contentViewRating.setOnClickPendingIntent(R.id.star1, contentIntent1);

            Intent notificationIntent2 = new Intent(context, PushTemplateReceiver.class);
            notificationIntent2.putExtra("click2", true);
            notificationIntent2.putExtra("notif_id", notificationId);
            notificationIntent2.putExtras(extras);
            PendingIntent contentIntent2 = PendingIntent.getBroadcast(context, new Random().nextInt(), notificationIntent2, 0);
            contentViewRating.setOnClickPendingIntent(R.id.star2, contentIntent2);

            Intent notificationIntent3 = new Intent(context, PushTemplateReceiver.class);
            notificationIntent3.putExtra("click3", true);
            notificationIntent3.putExtra("notif_id", notificationId);
            notificationIntent3.putExtras(extras);
            PendingIntent contentIntent3 = PendingIntent.getBroadcast(context, new Random().nextInt(), notificationIntent3, 0);
            contentViewRating.setOnClickPendingIntent(R.id.star3, contentIntent3);

            Intent notificationIntent4 = new Intent(context, PushTemplateReceiver.class);
            notificationIntent4.putExtra("click4", true);
            notificationIntent4.putExtra("notif_id", notificationId);
            notificationIntent4.putExtras(extras);
            PendingIntent contentIntent4 = PendingIntent.getBroadcast(context, new Random().nextInt(), notificationIntent4, 0);
            contentViewRating.setOnClickPendingIntent(R.id.star4, contentIntent4);

            Intent notificationIntent5 = new Intent(context, PushTemplateReceiver.class);
            notificationIntent5.putExtra("click5", true);
            notificationIntent5.putExtra("notif_id", notificationId);
            notificationIntent5.putExtras(extras);
            PendingIntent contentIntent5 = PendingIntent.getBroadcast(context, new Random().nextInt(), notificationIntent5, 0);
            contentViewRating.setOnClickPendingIntent(R.id.star5, contentIntent5);

            Intent launchIntent = new Intent(context, PushTemplateReceiver.class);
            launchIntent.putExtras(extras);
            launchIntent.putExtra("notif_id", notificationId);
            launchIntent.putExtra("default_dl",true);
            launchIntent.putExtra(Constants.WZRK_DL, pt_rating_default_dl);
            launchIntent.removeExtra(Constants.WZRK_ACTIONS);
            launchIntent.putExtra(Constants.WZRK_FROM_KEY, Constants.WZRK_FROM);
            launchIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            PendingIntent pIntent = PendingIntent.getBroadcast(context, (int) System.currentTimeMillis(),
                    launchIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            NotificationCompat.Builder notificationBuilder;
            if (requiresChannelId) {
                notificationBuilder = new NotificationCompat.Builder(context, channelId);
            } else {
                notificationBuilder = new NotificationCompat.Builder(context);
            }

            notificationBuilder.setSmallIcon(smallIcon)
                    .setCustomContentView(contentViewSmall)
                    .setCustomBigContentView(contentViewRating)
                    .setContentTitle(pt_title)
                    .setContentIntent(pIntent)
                    .setAutoCancel(true);

            Notification notification = notificationBuilder.build();
            notificationManager.notify(notificationId, notification);

            if (pt_big_img != null && !pt_big_img.isEmpty()) {
                Utils.loadIntoGlide(context, R.id.big_image, pt_big_img, contentViewRating, notification, notificationId);
            } else {
                contentViewRating.setViewVisibility(R.id.big_image, View.GONE);
            }

            if (pt_large_icon != null && !pt_large_icon.isEmpty()) {
                Utils.loadIntoGlide(context, R.id.large_icon, pt_large_icon, contentViewSmall, notification, notificationId);
            } else {
                contentViewSmall.setViewVisibility(R.id.large_icon, View.GONE);
            }

            Utils.loadIntoGlide(context, R.id.small_icon, smallIcon, contentViewRating, notification, notificationId);
            Utils.loadIntoGlide(context, R.id.small_icon, smallIcon, contentViewSmall, notification, notificationId);
            //Utils.loadIntoGlide(context, R.id.big_image_app, pt_large_icon, contentViewSmall, notification, notificationId);

            raiseNotificationViewed(context,extras);

        } catch (Throwable t) {
            PTLog.verbose("Error creating rating notification ", t);
        }
    }

    private void renderAutoCarouselNotification(Context context, Bundle extras, int notificationId) {
        try {
            contentViewCarousel = new RemoteViews(context.getPackageName(), R.layout.auto_carousel);
            contentViewCarousel.setTextViewText(R.id.app_name, Utils.getApplicationName(context));
            contentViewCarousel.setTextViewText(R.id.timestamp, Utils.getTimeStamp(context));

            contentViewSmall = new RemoteViews(context.getPackageName(), R.layout.content_view_small);
            contentViewSmall.setTextViewText(R.id.app_name, Utils.getApplicationName(context));
            contentViewSmall.setTextViewText(R.id.timestamp, Utils.getTimeStamp(context));

            contentViewCarousel.setTextColor(R.id.app_name, ContextCompat.getColor(context,R.color.gray));
            contentViewSmall.setTextColor(R.id.app_name, ContextCompat.getColor(context,R.color.gray));
            contentViewCarousel.setTextColor(R.id.timestamp, ContextCompat.getColor(context,R.color.gray));
            contentViewSmall.setTextColor(R.id.timestamp, ContextCompat.getColor(context,R.color.gray));

            if (pt_title != null && !pt_title.isEmpty()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    contentViewCarousel.setTextViewText(R.id.title, Html.fromHtml(pt_title, Html.FROM_HTML_MODE_LEGACY));
                    contentViewSmall.setTextViewText(R.id.title, Html.fromHtml(pt_title, Html.FROM_HTML_MODE_LEGACY));
                } else {
                    contentViewCarousel.setTextViewText(R.id.title, Html.fromHtml(pt_title));
                    contentViewSmall.setTextViewText(R.id.title, Html.fromHtml(pt_title));
                }
            }

            if (pt_msg != null && !pt_msg.isEmpty()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    contentViewCarousel.setTextViewText(R.id.msg, Html.fromHtml(pt_msg, Html.FROM_HTML_MODE_LEGACY));
                    contentViewSmall.setTextViewText(R.id.msg, Html.fromHtml(pt_msg, Html.FROM_HTML_MODE_LEGACY));
                } else {
                    contentViewCarousel.setTextViewText(R.id.msg, Html.fromHtml(pt_msg));
                    contentViewSmall.setTextViewText(R.id.msg, Html.fromHtml(pt_msg));
                }
            }

            if (pt_msg_summary != null && !pt_msg_summary.isEmpty()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    contentViewCarousel.setTextViewText(R.id.msg, Html.fromHtml(pt_msg_summary, Html.FROM_HTML_MODE_LEGACY));
                } else {
                    contentViewCarousel.setTextViewText(R.id.msg, Html.fromHtml(pt_msg_summary));
                }
            }

            if (pt_title_clr != null && !pt_title_clr.isEmpty()) {
                contentViewCarousel.setTextColor(R.id.title, Color.parseColor(pt_title_clr));
                contentViewSmall.setTextColor(R.id.title, Color.parseColor(pt_title_clr));
            }

            if (pt_msg_clr != null && !pt_msg_clr.isEmpty()) {
                contentViewCarousel.setTextColor(R.id.msg, Color.parseColor(pt_msg_clr));
                contentViewSmall.setTextColor(R.id.msg, Color.parseColor(pt_msg_clr));
            }

            if (pt_bg != null && !pt_bg.isEmpty()) {
                contentViewCarousel.setInt(R.id.carousel_relative_layout, "setBackgroundColor", Color.parseColor(pt_bg));
                contentViewSmall.setInt(R.id.content_view_small, "setBackgroundColor", Color.parseColor(pt_bg));
            }

            contentViewCarousel.setInt(R.id.view_flipper, "setFlipInterval", 4000);

            if (notificationId == Constants.EMPTY_NOTIFICATION_ID) {
                notificationId = (int) (Math.random() * 100);
            }

            Intent launchIntent = new Intent(context, CTPushNotificationReceiver.class);
            launchIntent.putExtras(extras);
            if (deepLinkList != null) {
                launchIntent.putExtra(Constants.WZRK_DL, deepLinkList.get(0));
            }
            launchIntent.removeExtra(Constants.WZRK_ACTIONS);
            launchIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent pIntent = PendingIntent.getBroadcast(context, (int) System.currentTimeMillis(),
                    launchIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            NotificationCompat.Builder notificationBuilder;
            if (requiresChannelId) {
                notificationBuilder = new NotificationCompat.Builder(context, channelId);
            } else {
                notificationBuilder = new NotificationCompat.Builder(context);
            }

            notificationBuilder.setSmallIcon(smallIcon)
                    .setCustomContentView(contentViewSmall)
                    .setCustomBigContentView(contentViewCarousel)
                    .setContentTitle(pt_title)
                    .setContentIntent(pIntent)
                    .setAutoCancel(true);

            Notification notification = notificationBuilder.build();
            notificationManager.notify(notificationId, notification);

            Utils.loadIntoGlide(context, R.id.small_icon, pt_large_icon, contentViewSmall, notification, notificationId);

            ArrayList<Integer> layoutIds = new ArrayList<>();
            layoutIds.add(0, R.id.flipper_img1);
            layoutIds.add(1, R.id.flipper_img2);
            layoutIds.add(2, R.id.flipper_img3);


            for (int index = 0; index < imageList.size(); index++) {
                Utils.loadIntoGlide(context, layoutIds.get(index), imageList.get(index), contentViewCarousel, notification, notificationId);
            }

            if (pt_large_icon != null && !pt_large_icon.isEmpty()) {
                Utils.loadIntoGlide(context, R.id.large_icon, pt_large_icon, contentViewSmall, notification, notificationId);
            } else {
                contentViewSmall.setViewVisibility(R.id.large_icon, View.GONE);
            }

            Utils.loadIntoGlide(context, R.id.small_icon, smallIcon, contentViewCarousel, notification, notificationId);
            Utils.loadIntoGlide(context, R.id.small_icon, smallIcon, contentViewSmall, notification, notificationId);


            raiseNotificationViewed(context,extras);
        } catch (Throwable t) {
            PTLog.verbose("Error creating auto carousel notification ", t);
        }
    }

    private void renderBasicTemplateNotification(Context context, Bundle extras, int notificationId) {
        try {
            contentViewBig = new RemoteViews(context.getPackageName(), R.layout.image_only_big);
            contentViewBig.setTextViewText(R.id.app_name, Utils.getApplicationName(context));
            contentViewBig.setTextViewText(R.id.timestamp, Utils.getTimeStamp(context));

            contentViewSmall = new RemoteViews(context.getPackageName(), R.layout.content_view_small);
            contentViewSmall.setTextViewText(R.id.app_name, Utils.getApplicationName(context));
            contentViewSmall.setTextViewText(R.id.timestamp, Utils.getTimeStamp(context));

            contentViewBig.setTextColor(R.id.app_name, ContextCompat.getColor(context,R.color.gray));
            contentViewSmall.setTextColor(R.id.app_name, ContextCompat.getColor(context,R.color.gray));
            contentViewBig.setTextColor(R.id.timestamp, ContextCompat.getColor(context,R.color.gray));
            contentViewSmall.setTextColor(R.id.timestamp, ContextCompat.getColor(context,R.color.gray));

            if (pt_title != null && !pt_title.isEmpty()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    contentViewBig.setTextViewText(R.id.title, Html.fromHtml(pt_title, Html.FROM_HTML_MODE_LEGACY));
                    contentViewSmall.setTextViewText(R.id.title, Html.fromHtml(pt_title, Html.FROM_HTML_MODE_LEGACY));
                } else {
                    contentViewBig.setTextViewText(R.id.title, Html.fromHtml(pt_title));
                    contentViewSmall.setTextViewText(R.id.title, Html.fromHtml(pt_title));
                }
            }

            if (pt_msg != null && !pt_msg.isEmpty()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    contentViewBig.setTextViewText(R.id.msg, Html.fromHtml(pt_msg, Html.FROM_HTML_MODE_LEGACY));
                    contentViewSmall.setTextViewText(R.id.msg, Html.fromHtml(pt_msg, Html.FROM_HTML_MODE_LEGACY));
                } else {
                    contentViewBig.setTextViewText(R.id.msg, Html.fromHtml(pt_msg));
                    contentViewSmall.setTextViewText(R.id.msg, Html.fromHtml(pt_msg));
                }
            }

            if (pt_bg != null && !pt_bg.isEmpty()) {
                contentViewBig.setInt(R.id.image_only_big_linear_layout, "setBackgroundColor", Color.parseColor(pt_bg));
                contentViewSmall.setInt(R.id.content_view_small, "setBackgroundColor", Color.parseColor(pt_bg));
            }

            if (pt_title_clr != null && !pt_title_clr.isEmpty()) {
                contentViewBig.setTextColor(R.id.title, Color.parseColor(pt_title_clr));
                contentViewSmall.setTextColor(R.id.title, Color.parseColor(pt_title_clr));
            }

            if (pt_msg_clr != null && !pt_msg_clr.isEmpty()) {
                contentViewBig.setTextColor(R.id.msg, Color.parseColor(pt_msg_clr));
                contentViewSmall.setTextColor(R.id.msg, Color.parseColor(pt_msg_clr));
            }

            if (notificationId == Constants.EMPTY_NOTIFICATION_ID) {
                notificationId = (int) (Math.random() * 100);
            }

            Intent launchIntent = new Intent(context, CTPushNotificationReceiver.class);
            launchIntent.putExtras(extras);
            if (deepLinkList != null && deepLinkList.size()>0) {
                launchIntent.putExtra(Constants.WZRK_DL, deepLinkList.get(0));
            }
            launchIntent.removeExtra(Constants.WZRK_ACTIONS);
            launchIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent pIntent = PendingIntent.getBroadcast(context, (int) System.currentTimeMillis(),
                    launchIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            NotificationCompat.Builder notificationBuilder;
            if (requiresChannelId) {
                notificationBuilder = new NotificationCompat.Builder(context, channelId);
            } else {
                notificationBuilder = new NotificationCompat.Builder(context);
            }

            notificationBuilder.setSmallIcon(smallIcon)
                    .setCustomContentView(contentViewSmall)
                    .setCustomBigContentView(contentViewBig)
                    .setContentTitle(pt_title)
                    .setContentIntent(pIntent)
                    .setVibrate(new long[]{0L})
                    .setAutoCancel(true);

            Notification notification = notificationBuilder.build();
            notificationManager.notify(notificationId, notification);
            if (pt_big_img != null && !pt_big_img.isEmpty()) {
                Utils.loadIntoGlide(context, R.id.big_image, pt_big_img, contentViewBig, notification, notificationId);
            } else {
                contentViewBig.setViewVisibility(R.id.big_image, View.GONE);
            }

            if (pt_large_icon != null && !pt_large_icon.isEmpty()) {
                Utils.loadIntoGlide(context, R.id.large_icon, pt_large_icon, contentViewSmall, notification, notificationId);
            } else {
                contentViewSmall.setViewVisibility(R.id.large_icon, View.GONE);
            }

            Utils.loadIntoGlide(context, R.id.small_icon, smallIcon, contentViewBig, notification, notificationId);
            Utils.loadIntoGlide(context, R.id.small_icon, smallIcon, contentViewSmall, notification, notificationId);

            raiseNotificationViewed(context,extras);
        } catch (Throwable t) {
            PTLog.verbose("Error creating image only notification", t);
        }
    }

    private void renderProductDisplayNotification(Context context, Bundle extras, int notificationId) {
        try {

            contentViewBig = new RemoteViews(context.getPackageName(), R.layout.product_display_template);
            contentViewBig.setTextViewText(R.id.app_name, Utils.getApplicationName(context));
            contentViewBig.setTextViewText(R.id.timestamp, Utils.getTimeStamp(context));

            contentViewSmall = new RemoteViews(context.getPackageName(), R.layout.content_view_small);
            contentViewSmall.setTextViewText(R.id.app_name, Utils.getApplicationName(context));
            contentViewSmall.setTextViewText(R.id.timestamp, Utils.getTimeStamp(context));

            contentViewBig.setTextColor(R.id.app_name, ContextCompat.getColor(context,R.color.gray));
            contentViewSmall.setTextColor(R.id.app_name, ContextCompat.getColor(context,R.color.gray));
            contentViewBig.setTextColor(R.id.timestamp, ContextCompat.getColor(context,R.color.gray));
            contentViewSmall.setTextColor(R.id.timestamp, ContextCompat.getColor(context,R.color.gray));

            if (!bigTextList.isEmpty()) {
                contentViewBig.setTextViewText(R.id.product_name, bigTextList.get(0));

            }

            if (!smallTextList.isEmpty()) {
                contentViewBig.setTextViewText(R.id.product_description, smallTextList.get(0));

            }

            if (!priceList.isEmpty()) {
                contentViewBig.setTextViewText(R.id.product_price, priceList.get(0));

            }

            if (pt_title != null && !pt_title.isEmpty()) {
                contentViewBig.setTextViewText(R.id.title, pt_title);
                contentViewSmall.setTextViewText(R.id.title, pt_title);
            }

            if (pt_msg != null && !pt_msg.isEmpty()) {
                contentViewBig.setTextViewText(R.id.msg, pt_msg);
                contentViewSmall.setTextViewText(R.id.msg, pt_msg);
            }

            if (pt_title_clr != null && !pt_title_clr.isEmpty()) {
                contentViewBig.setTextColor(R.id.title, Color.parseColor(pt_title_clr));
                contentViewSmall.setTextColor(R.id.title, Color.parseColor(pt_title_clr));
            }

            if (pt_msg_clr != null && !pt_msg_clr.isEmpty()) {
                contentViewBig.setTextColor(R.id.msg, Color.parseColor(pt_msg_clr));
                contentViewSmall.setTextColor(R.id.msg, Color.parseColor(pt_msg_clr));
            }

            notificationId = new Random().nextInt();

            int requestCode1 = new Random().nextInt();
            int requestCode2 = new Random().nextInt();
            int requestCode3 = new Random().nextInt();

            Intent notificationIntent1 = new Intent(context, PushTemplateReceiver.class);
            notificationIntent1.putExtra("img1", true);
            notificationIntent1.putExtra("notif_id", notificationId);
            notificationIntent1.putExtra(Constants.PT_BUY_NOW_DL, deepLinkList.get(0));
            notificationIntent1.putExtra("pt_reqcode1", requestCode1);
            notificationIntent1.putExtra("pt_reqcode2", requestCode2);
            notificationIntent1.putExtra("pt_reqcode3", requestCode3);
            notificationIntent1.putExtras(extras);
            PendingIntent contentIntent1 = PendingIntent.getBroadcast(context, requestCode1, notificationIntent1, 0);
            contentViewBig.setOnClickPendingIntent(R.id.small_image1, contentIntent1);

            Intent notificationIntent2 = new Intent(context, PushTemplateReceiver.class);
            notificationIntent2.putExtra("img2", true);
            notificationIntent2.putExtra("notif_id", notificationId);
            notificationIntent2.putExtra(Constants.PT_BUY_NOW_DL, deepLinkList.get(1));
            notificationIntent2.putExtra("pt_reqcode1", requestCode1);
            notificationIntent2.putExtra("pt_reqcode2", requestCode2);
            notificationIntent2.putExtra("pt_reqcode3", requestCode3);
            notificationIntent2.putExtras(extras);
            PendingIntent contentIntent2 = PendingIntent.getBroadcast(context, requestCode2, notificationIntent2, 0);
            contentViewBig.setOnClickPendingIntent(R.id.small_image2, contentIntent2);

            Intent notificationIntent3 = new Intent(context, PushTemplateReceiver.class);
            notificationIntent3.putExtra("img3", true);
            notificationIntent3.putExtra("notif_id", notificationId);
            notificationIntent3.putExtra(Constants.PT_BUY_NOW_DL, deepLinkList.get(2));
            notificationIntent3.putExtra("pt_reqcode1", requestCode1);
            notificationIntent3.putExtra("pt_reqcode2", requestCode2);
            notificationIntent3.putExtra("pt_reqcode3", requestCode3);
            notificationIntent3.putExtras(extras);
            PendingIntent contentIntent3 = PendingIntent.getBroadcast(context, requestCode3, notificationIntent3, 0);
            contentViewBig.setOnClickPendingIntent(R.id.small_image3, contentIntent3);

            Intent notificationIntent4 = new Intent(context, PushTemplateReceiver.class);
            notificationIntent1.putExtra("img1", true);
            notificationIntent4.putExtra("notif_id", notificationId);
            notificationIntent4.putExtra(Constants.PT_BUY_NOW_DL, deepLinkList.get(0));
            notificationIntent4.putExtra("pt_reqcode1", requestCode1);
            notificationIntent4.putExtra("pt_reqcode2", requestCode2);
            notificationIntent4.putExtra("pt_reqcode3", requestCode3);
            notificationIntent4.putExtra("buynow", true);
            notificationIntent4.putExtras(extras);
            PendingIntent contentIntent4 = PendingIntent.getBroadcast(context, new Random().nextInt(), notificationIntent4, 0);
            contentViewBig.setOnClickPendingIntent(R.id.action_button, contentIntent4);


            Intent launchIntent = new Intent(context, CTPushNotificationReceiver.class);
            launchIntent.putExtras(extras);
            launchIntent.putExtra(Constants.WZRK_DL, deepLinkList.get(0));
            launchIntent.removeExtra(Constants.WZRK_ACTIONS);
            launchIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent pIntent = PendingIntent.getBroadcast(context, (int) System.currentTimeMillis(),
                    launchIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            NotificationCompat.Builder notificationBuilder;
            if (requiresChannelId) {
                notificationBuilder = new NotificationCompat.Builder(context, channelId);
            } else {
                notificationBuilder = new NotificationCompat.Builder(context);
            }

            notificationBuilder.setSmallIcon(smallIcon)
                    .setCustomContentView(contentViewSmall)
                    .setCustomBigContentView(contentViewBig)
                    .setContentTitle(pt_title)
                    .setContentIntent(pIntent)
                    .setVibrate(new long[]{0L})
                    .setAutoCancel(true);

            Notification notification = notificationBuilder.build();
            notificationManager.notify(notificationId, notification);
            Utils.loadIntoGlide(context, R.id.small_icon, pt_large_icon, contentViewSmall, notification, notificationId);
            for (int index = 0; index < imageList.size(); index++) {
                if (index == 0) {
                    Utils.loadIntoGlide(context, R.id.small_image1, imageList.get(0), contentViewBig, notification, notificationId);
                } else if (index == 1) {
                    Utils.loadIntoGlide(context, R.id.small_image2, imageList.get(1), contentViewBig, notification, notificationId);
                } else if (index == 2) {
                    Utils.loadIntoGlide(context, R.id.small_image3, imageList.get(2), contentViewBig, notification, notificationId);
                }
            }

            Utils.loadIntoGlide(context, R.id.small_icon, smallIcon, contentViewBig, notification, notificationId);
            Utils.loadIntoGlide(context, R.id.small_icon, smallIcon, contentViewSmall, notification, notificationId);
            Utils.loadIntoGlide(context, R.id.big_image, imageList.get(0), contentViewBig, notification, notificationId);
            raiseNotificationViewed(context,extras);
        } catch (Throwable t) {
            PTLog.verbose("Error creating Product Display Notification ", t);
        }
    }

    private void renderFiveIconNotification(Context context, Bundle extras, int notificationId) {
        try {
            if (pt_title == null || pt_title.isEmpty()) {
                pt_title = Utils.getApplicationName(context);
            }
            contentFiveCTAs = new RemoteViews(context.getPackageName(), R.layout.five_cta);

            notificationId = new Random().nextInt();

            int reqCode1 = new Random().nextInt();
            int reqCode2 = new Random().nextInt();
            int reqCode3 = new Random().nextInt();
            int reqCode4 = new Random().nextInt();
            int reqCode5 = new Random().nextInt();
            int reqCode6 = new Random().nextInt();


            Intent notificationIntent1 = new Intent(context, PushTemplateReceiver.class);
            notificationIntent1.putExtra("cta1", true);
            notificationIntent1.putExtra("notif_id",notificationId);
            notificationIntent1.putExtras(extras);
            PendingIntent contentIntent1 = PendingIntent.getBroadcast(context, reqCode1, notificationIntent1, 0);
            contentFiveCTAs.setOnClickPendingIntent(R.id.cta1, contentIntent1);

            Intent notificationIntent2 = new Intent(context, PushTemplateReceiver.class);
            notificationIntent2.putExtra("cta2", true);
            notificationIntent2.putExtra("notif_id",notificationId);
            notificationIntent2.putExtras(extras);
            PendingIntent contentIntent2 = PendingIntent.getBroadcast(context, reqCode2, notificationIntent2, 0);
            contentFiveCTAs.setOnClickPendingIntent(R.id.cta2, contentIntent2);

            Intent notificationIntent3 = new Intent(context, PushTemplateReceiver.class);
            notificationIntent3.putExtra("cta3", true);
            notificationIntent3.putExtra("notif_id",notificationId);
            notificationIntent3.putExtras(extras);
            PendingIntent contentIntent3 = PendingIntent.getBroadcast(context, reqCode3, notificationIntent3, 0);
            contentFiveCTAs.setOnClickPendingIntent(R.id.cta3, contentIntent3);

            Intent notificationIntent4 = new Intent(context, PushTemplateReceiver.class);
            notificationIntent4.putExtra("cta4", true);
            notificationIntent4.putExtra("notif_id",notificationId);
            notificationIntent4.putExtras(extras);
            PendingIntent contentIntent4 = PendingIntent.getBroadcast(context,  reqCode4, notificationIntent4, 0);
            contentFiveCTAs.setOnClickPendingIntent(R.id.cta4, contentIntent4);

            Intent notificationIntent5 = new Intent(context, PushTemplateReceiver.class);
            notificationIntent5.putExtra("cta5", true);
            notificationIntent5.putExtra("notif_id",notificationId);
            notificationIntent5.putExtras(extras);
            PendingIntent contentIntent5 = PendingIntent.getBroadcast(context, reqCode5, notificationIntent5, 0);
            contentFiveCTAs.setOnClickPendingIntent(R.id.cta5, contentIntent5);

            Intent notificationIntent6 = new Intent(context, PushTemplateReceiver.class);
            notificationIntent6.putExtra("close", true);
            notificationIntent6.putExtra("notif_id",notificationId);
            notificationIntent6.putExtras(extras);
            PendingIntent contentIntent6 = PendingIntent.getBroadcast(context, reqCode6, notificationIntent6, 0);
            contentFiveCTAs.setOnClickPendingIntent(R.id.close, contentIntent6);

            Intent launchIntent = new Intent(context, CTPushNotificationReceiver.class);
            launchIntent.putExtras(extras);
            launchIntent.removeExtra(Constants.WZRK_ACTIONS);
            launchIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent pIntent = PendingIntent.getBroadcast(context, (int) System.currentTimeMillis(),
                    launchIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            NotificationCompat.Builder notificationBuilder;
            if (requiresChannelId) {
                notificationBuilder = new NotificationCompat.Builder(context, channelId);
            } else {
                notificationBuilder = new NotificationCompat.Builder(context);
            }


            notificationBuilder.setSmallIcon(smallIcon)
                    .setCustomContentView(contentFiveCTAs)
                    .setCustomBigContentView(contentFiveCTAs)
                    .setContentTitle(pt_title)
                    .setContentIntent(pIntent)
                    .setOngoing(true)
                    .setVibrate(new long[]{0L})
                    .setAutoCancel(true);

            Notification notification = notificationBuilder.build();
            notificationManager.notify(notificationId, notification);


            for (int imageKey = 0; imageKey < imageList.size(); imageKey++) {
                if (imageKey == 0) {
                    Utils.loadIntoGlide(context, R.id.cta1, imageList.get(imageKey), contentFiveCTAs, notification, notificationId);
                } else if (imageKey == 1) {
                    Utils.loadIntoGlide(context, R.id.cta2, imageList.get(imageKey), contentFiveCTAs, notification, notificationId);
                } else if (imageKey == 2) {
                    Utils.loadIntoGlide(context, R.id.cta3, imageList.get(imageKey), contentFiveCTAs, notification, notificationId);
                } else if (imageKey == 3) {
                    Utils.loadIntoGlide(context, R.id.cta4, imageList.get(imageKey), contentFiveCTAs, notification, notificationId);
                } else if (imageKey == 4) {
                    Utils.loadIntoGlide(context, R.id.cta5, imageList.get(imageKey), contentFiveCTAs, notification, notificationId);
                }

            }
            Utils.loadIntoGlide(context, R.id.close, R.drawable.pt_close, contentFiveCTAs, notification, notificationId);

            raiseNotificationViewed(context,extras);
        } catch (Throwable t) {
            PTLog.verbose("Error creating image only notification", t);
        }

    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    private void renderTimerNotification(final Context context, Bundle extras, int notificationId) {
        try {

            contentViewTimer = new RemoteViews(context.getPackageName(), R.layout.timer);
            contentViewTimerCollapsed = new RemoteViews(context.getPackageName(), R.layout.timer_collapsed);
            contentViewTimer.setTextViewText(R.id.app_name, Utils.getApplicationName(context));
            contentViewTimer.setTextViewText(R.id.timestamp, Utils.getTimeStamp(context));

            contentViewTimer.setTextColor(R.id.app_name, ContextCompat.getColor(context,R.color.gray));
            contentViewTimer.setTextColor(R.id.timestamp, ContextCompat.getColor(context,R.color.gray));
            contentViewTimerCollapsed.setTextViewText(R.id.app_name, Utils.getApplicationName(context));
            contentViewTimerCollapsed.setTextViewText(R.id.timestamp, Utils.getTimeStamp(context));

            contentViewTimerCollapsed.setTextColor(R.id.app_name, ContextCompat.getColor(context,R.color.gray));
            contentViewTimerCollapsed.setTextColor(R.id.timestamp, ContextCompat.getColor(context,R.color.gray));

            if (pt_title != null && !pt_title.isEmpty()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    contentViewTimer.setTextViewText(R.id.title, Html.fromHtml(pt_title, Html.FROM_HTML_MODE_LEGACY));
                    contentViewTimerCollapsed.setTextViewText(R.id.title, Html.fromHtml(pt_title, Html.FROM_HTML_MODE_LEGACY));
                } else {
                    contentViewTimer.setTextViewText(R.id.title, Html.fromHtml(pt_title));
                    contentViewTimerCollapsed.setTextViewText(R.id.title, Html.fromHtml(pt_title));
                }
            }

            if (pt_msg != null && !pt_msg.isEmpty()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    contentViewTimer.setTextViewText(R.id.msg, Html.fromHtml(pt_msg, Html.FROM_HTML_MODE_LEGACY));
                    contentViewTimerCollapsed.setTextViewText(R.id.msg, Html.fromHtml(pt_msg, Html.FROM_HTML_MODE_LEGACY));
                } else {
                    contentViewTimer.setTextViewText(R.id.msg, Html.fromHtml(pt_msg));
                    contentViewTimerCollapsed.setTextViewText(R.id.msg, Html.fromHtml(pt_msg));
                }
            }

            if (pt_bg != null && !pt_bg.isEmpty()) {
                contentViewTimer.setInt(R.id.image_only_big_linear_layout, "setBackgroundColor", Color.parseColor(pt_bg));
                contentViewTimerCollapsed.setInt(R.id.content_view_small, "setBackgroundColor", Color.parseColor(pt_bg));
            }

            if (pt_title_clr != null && !pt_title_clr.isEmpty()) {
                contentViewTimer.setTextColor(R.id.title, Color.parseColor(pt_title_clr));
                contentViewTimerCollapsed.setTextColor(R.id.title, Color.parseColor(pt_title_clr));
            }

            if (pt_msg_clr != null && !pt_msg_clr.isEmpty()) {
                contentViewTimer.setTextColor(R.id.msg, Color.parseColor(pt_msg_clr));
                contentViewTimerCollapsed.setTextColor(R.id.msg, Color.parseColor(pt_msg_clr));

            }

            contentViewTimer.setChronometer(R.id.chronometer, SystemClock.elapsedRealtime() + (pt_timer_threshold*1000),null,true);
            contentViewTimer.setChronometerCountDown(R.id.chronometer, true);

            contentViewTimerCollapsed.setChronometer(R.id.chronometer, SystemClock.elapsedRealtime() + (pt_timer_threshold*1000),null,true);
            contentViewTimerCollapsed.setChronometerCountDown(R.id.chronometer, true);


            if (notificationId == Constants.EMPTY_NOTIFICATION_ID) {
                notificationId = (int) (Math.random() * 100);
            }

            Intent launchIntent = new Intent(context, CTPushNotificationReceiver.class);
            launchIntent.putExtras(extras);
            if (deepLinkList != null && deepLinkList.size()>0) {
                launchIntent.putExtra(Constants.WZRK_DL, deepLinkList.get(0));
            }
            launchIntent.removeExtra(Constants.WZRK_ACTIONS);
            launchIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent pIntent = PendingIntent.getBroadcast(context, (int) System.currentTimeMillis(),
                    launchIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            NotificationCompat.Builder notificationBuilder;
            if (requiresChannelId) {
                notificationBuilder = new NotificationCompat.Builder(context, channelId);
            } else {
                notificationBuilder = new NotificationCompat.Builder(context);
            }

            notificationBuilder.setSmallIcon(smallIcon)
                    .setCustomContentView(contentViewTimerCollapsed)
                    .setCustomBigContentView(contentViewTimer)
                    .setContentTitle(pt_title)
                    .setContentIntent(pIntent)
                    .setVibrate(new long[]{0L})
                    .setTimeoutAfter(pt_timer_threshold*1000)
                    .setAutoCancel(true);

            Notification notification = notificationBuilder.build();
            notificationManager.notify(notificationId, notification);

            if (pt_big_img != null && !pt_big_img.isEmpty()) {
                Utils.loadIntoGlide(context, R.id.big_image, pt_big_img, contentViewTimer, notification, notificationId);
            } else {
                contentViewTimer.setViewVisibility(R.id.big_image, View.GONE);
            }

            Utils.loadIntoGlide(context, R.id.small_icon, smallIcon, contentViewTimer, notification, notificationId);

            raiseNotificationViewed(context,extras);

        } catch (Throwable t) {
            PTLog.verbose("Error creating Timer notification ", t);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT_WATCH)
    private void renderInputBoxNotification(final Context context, Bundle extras, int notificationId) {
        try {
            //Fetch Notif ID
            if (notificationId == Constants.EMPTY_NOTIFICATION_ID) {
                notificationId = (int) (Math.random() * 100);
            }
            //Set launchIntent to reciever
            Intent launchIntent = new Intent(context, PushTemplateReceiver.class);
            launchIntent.putExtras(extras);
            launchIntent.putExtra(Constants.PT_NOTIF_ID, notificationId);
            launchIntent.putExtra(Constants.PT_INPUT_FEEDBACK, pt_input_feedback);
            launchIntent.putExtra(Constants.PT_INPUT_AUTO_OPEN, pt_input_auto_open);


            if (deepLinkList != null && deepLinkList.size()>0) {
                launchIntent.putExtra(Constants.WZRK_DL, deepLinkList.get(0));
            }
            launchIntent.removeExtra(Constants.WZRK_ACTIONS);
            launchIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent pIntent = PendingIntent.getBroadcast(context, (int) System.currentTimeMillis(),
                    launchIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            NotificationCompat.Builder notificationBuilder;
            if (requiresChannelId) {
                notificationBuilder = new NotificationCompat.Builder(context, channelId);
            } else {
                notificationBuilder = new NotificationCompat.Builder(context);
            }

            notificationBuilder.setSmallIcon(smallIcon)
                    .setContentTitle(pt_title)
                    .setContentText(pt_msg)
                    .setContentIntent(pIntent)
                    .setVibrate(new long[]{0L})
                    .setWhen(System.currentTimeMillis())
                    .setAutoCancel(true);



            //Initialise RemoteInput
            RemoteInput remoteInput = new RemoteInput.Builder(Constants.PT_INPUT_KEY)
                    .setLabel(pt_input_label)
                    .build();


            //Notification Action with RemoteInput instance added.
            NotificationCompat.Action replyAction = new NotificationCompat.Action.Builder(
                    android.R.drawable.sym_action_chat, pt_input_label, pIntent)
                    .addRemoteInput(remoteInput)
                    .setAllowGeneratedReplies(true)
                    .build();


            //Notification.Action instance added to Notification Builder.
            notificationBuilder.addAction(replyAction);

            Notification notification = notificationBuilder.build();
            notificationManager.notify(notificationId, notification);

            raiseNotificationViewed(context,extras);

        } catch (Throwable t) {
            PTLog.verbose("Error creating Input Box notification ", t);
        }
    }


    private void raiseNotificationViewed(Context context, Bundle extras){
        CleverTapAPI instance = CleverTapAPI.getDefaultInstance(context);
        if (instance != null) {
            instance.pushNotificationViewedEvent(extras);
        }
    }

}
