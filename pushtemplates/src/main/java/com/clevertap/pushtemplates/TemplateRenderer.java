package com.clevertap.pushtemplates;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.RemoteInput;
import androidx.core.content.ContextCompat;

import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.text.Html;
import android.view.View;
import android.widget.RemoteViews;

import com.clevertap.android.sdk.CTPushNotificationReceiver;
import com.clevertap.android.sdk.CleverTapAPI;

import org.json.JSONArray;
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
    private String pt_title_clr, pt_msg_clr, pt_chrono_title_clr;
    private ArrayList<String> imageList;
    private ArrayList<String> deepLinkList;
    private ArrayList<String> bigTextList;
    private ArrayList<String> smallTextList;
    private ArrayList<String> priceList;
    private String pt_product_display_action;
    private String pt_product_display_action_clr;
    private String pt_bg;
    private String pt_rating_default_dl;
    private String pt_small_view;
    private RemoteViews contentViewBig, contentViewSmall, contentViewCarousel, contentViewRating,
            contentFiveCTAs, contentViewTimer, contentViewTimerCollapsed, contentViewManualCarousel;
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
    private String pt_dismiss_on_click;
    private String pt_video_url;
    private int pt_timer_end;
    private String pt_title_alt;
    private String pt_msg_alt;
    private String pt_big_img_alt;




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
    public static void setDebugLevel(int level) {
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
        pt_small_view = extras.getString(Constants.PT_SMALL_VIEW);
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
        pt_dismiss_on_click = extras.getString(Constants.PT_DISMISS_ON_CLICK);
        pt_chrono_title_clr = extras.getString(Constants.PT_CHRONO_TITLE_COLOUR);
        pt_video_url = extras.getString(Constants.PT_VIDEO_URL);
        pt_product_display_action = extras.getString(Constants.PT_PRODUCT_DISPLAY_ACTION);
        pt_product_display_action_clr = extras.getString(Constants.PT_PRODUCT_DISPLAY_ACTION_COLOUR);
        pt_timer_end = Utils.getTimerEnd(extras);
        pt_big_img_alt = extras.getString(Constants.PT_BIG_IMG_ALT);
        pt_msg_alt = extras.getString(Constants.PT_MSG_ALT);
        pt_title_alt = extras.getString(Constants.PT_TITLE_ALT);
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
                        if (extras.getString(Constants.WZRK_PUSH_ID) != null) {
                            if (!extras.getString(Constants.WZRK_PUSH_ID).isEmpty()) {
                                String ptID = extras.getString(Constants.WZRK_PUSH_ID);
                                if (!dbHelper.isNotificationPresentInDB(ptID)) {
                                    _createNotification(context, extras, Constants.EMPTY_NOTIFICATION_ID);
                                    dbHelper.savePT(ptID, Utils.bundleToJSON(extras));
                                }
                                else {
                                    PTLog.debug("Notification already Rendered. skipping this payload");
                                }
                            }
                        } else {
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
            case MANUAL_CAROUSEL:
                if (hasAllManualCarouselNotifKeys())
                    renderManualCarouselNotification(context, extras, notificationId);
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
            case ZERO_BEZEL:
                if (hasAllBasicNotifKeys())
                    renderZeroBezelNotification(context, extras, notificationId);
            case TIMER:
                if (hasAllTimerKeys())
                    renderTimerNotification(context, extras, notificationId);
                break;
            case INPUT_BOX:
                if (hasAllInputBoxKeys())
                    renderInputBoxNotification(context, extras, notificationId);
                break;
            case VIDEO:
                if (hasAllVideoKeys())
                    renderVideoNotification(context, extras, notificationId);
                break;
        }
    }

    private boolean hasAllVideoKeys() {
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
        if (pt_video_url == null || pt_video_url.isEmpty()) {
            PTLog.verbose("Video URL is missing or empty. Not showing notification");
            result = false;
        }
        if (pt_bg == null || pt_bg.isEmpty()) {
            PTLog.verbose("Background colour is missing or empty. Not showing notification");
            result = false;
        }
        return result;
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
        if (pt_bg == null || pt_bg.isEmpty()) {
            PTLog.verbose("Background colour is missing or empty. Not showing notification");
            result = false;
        }
        return result;
    }

    private boolean hasAllManualCarouselNotifKeys() {
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
        if (pt_bg == null || pt_bg.isEmpty()) {
            PTLog.verbose("Background colour is missing or empty. Not showing notification");
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
        if (pt_bg == null || pt_bg.isEmpty()) {
            PTLog.verbose("Background colour is missing or empty. Not showing notification");
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
        if (pt_bg == null || pt_bg.isEmpty()) {
            PTLog.verbose("Background colour is missing or empty. Not showing notification");
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
        if (pt_bg == null || pt_bg.isEmpty()) {
            PTLog.verbose("Background colour is missing or empty. Not showing notification");
            result = false;
        }
        if (pt_product_display_action == null || pt_product_display_action.isEmpty()) {
            PTLog.verbose("Button label is missing or empty. Not showing notification");
            result = false;
        }
        if (pt_product_display_action_clr == null || pt_product_display_action_clr.isEmpty()) {
            PTLog.verbose("Button colour is missing or empty. Not showing notification");
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
        if (pt_timer_threshold == -1 && pt_timer_end == -1) {
            PTLog.verbose("Timer Threshold or End time not defined. Not showing notification");
            result = false;
        }
        if (pt_bg == null || pt_bg.isEmpty()) {
            PTLog.verbose("Background colour is missing or empty. Not showing notification");
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

            //Set the rating stars
            contentViewRating.setImageViewResource(R.id.star1, R.drawable.outline_star_1);
            contentViewRating.setImageViewResource(R.id.star2, R.drawable.outline_star_1);
            contentViewRating.setImageViewResource(R.id.star3, R.drawable.outline_star_1);
            contentViewRating.setImageViewResource(R.id.star4, R.drawable.outline_star_1);
            contentViewRating.setImageViewResource(R.id.star5, R.drawable.outline_star_1);

            notificationId = setNotificationId(notificationId);

            //Set Pending Intents for each star to listen to click

            Intent notificationIntent1 = new Intent(context, PushTemplateReceiver.class);
            notificationIntent1.putExtra("click1", true);
            notificationIntent1.putExtra(Constants.PT_NOTIF_ID, notificationId);
            notificationIntent1.putExtras(extras);
            PendingIntent contentIntent1 = PendingIntent.getBroadcast(context, new Random().nextInt(), notificationIntent1, 0);
            contentViewRating.setOnClickPendingIntent(R.id.star1, contentIntent1);

            Intent notificationIntent2 = new Intent(context, PushTemplateReceiver.class);
            notificationIntent2.putExtra("click2", true);
            notificationIntent2.putExtra(Constants.PT_NOTIF_ID, notificationId);
            notificationIntent2.putExtras(extras);
            PendingIntent contentIntent2 = PendingIntent.getBroadcast(context, new Random().nextInt(), notificationIntent2, 0);
            contentViewRating.setOnClickPendingIntent(R.id.star2, contentIntent2);

            Intent notificationIntent3 = new Intent(context, PushTemplateReceiver.class);
            notificationIntent3.putExtra("click3", true);
            notificationIntent3.putExtra(Constants.PT_NOTIF_ID, notificationId);
            notificationIntent3.putExtras(extras);
            PendingIntent contentIntent3 = PendingIntent.getBroadcast(context, new Random().nextInt(), notificationIntent3, 0);
            contentViewRating.setOnClickPendingIntent(R.id.star3, contentIntent3);

            Intent notificationIntent4 = new Intent(context, PushTemplateReceiver.class);
            notificationIntent4.putExtra("click4", true);
            notificationIntent4.putExtra(Constants.PT_NOTIF_ID, notificationId);
            notificationIntent4.putExtras(extras);
            PendingIntent contentIntent4 = PendingIntent.getBroadcast(context, new Random().nextInt(), notificationIntent4, 0);
            contentViewRating.setOnClickPendingIntent(R.id.star4, contentIntent4);

            Intent notificationIntent5 = new Intent(context, PushTemplateReceiver.class);
            notificationIntent5.putExtra("click5", true);
            notificationIntent5.putExtra(Constants.PT_NOTIF_ID, notificationId);
            notificationIntent5.putExtras(extras);
            PendingIntent contentIntent5 = PendingIntent.getBroadcast(context, new Random().nextInt(), notificationIntent5, 0);
            contentViewRating.setOnClickPendingIntent(R.id.star5, contentIntent5);

            Intent launchIntent = new Intent(context, PushTemplateReceiver.class);

            PendingIntent pIntent = setPendingIntent(context, notificationId, extras, launchIntent, pt_rating_default_dl);

            NotificationCompat.Builder notificationBuilder = setBuilderWithChannelIDCheck(requiresChannelId, channelId, context);

            setNotificationBuilderBasics(notificationBuilder, contentViewSmall, contentViewRating, pt_title, pIntent);

            Notification notification = notificationBuilder.build();
            notificationManager.notify(notificationId, notification);

            setCustomContentViewBigImage(contentViewRating, pt_big_img, context, notification, notificationId);

            setCustomContentViewLargeIcon(contentViewSmall, pt_large_icon, context, notification, notificationId);

            Utils.loadIntoGlide(context, R.id.small_icon, smallIcon, contentViewRating, notification, notificationId);
            Utils.loadIntoGlide(context, R.id.small_icon, smallIcon, contentViewSmall, notification, notificationId);

            raiseNotificationViewed(context, extras);

        } catch (Throwable t) {
            PTLog.verbose("Error creating rating notification ", t);
        }
    }

    private void renderAutoCarouselNotification(Context context, Bundle extras, int notificationId) {
        try {
            notificationId = setNotificationId(notificationId);

            contentViewCarousel = new RemoteViews(context.getPackageName(), R.layout.auto_carousel);
            setCustomContentViewBasicKeys(contentViewCarousel, context);

            contentViewSmall = new RemoteViews(context.getPackageName(), R.layout.content_view_small);

            setCustomContentViewBasicKeys(contentViewSmall, context);

            setCustomContentViewTitle(contentViewCarousel, pt_title);
            setCustomContentViewTitle(contentViewSmall, pt_title);

            setCustomContentViewMessage(contentViewCarousel, pt_msg);
            setCustomContentViewMessage(contentViewSmall, pt_msg);

            setCustomContentViewExpandedBackgroundColour(contentViewCarousel, pt_bg);
            setCustomContentViewCollapsedBackgroundColour(contentViewSmall, pt_bg);

            setCustomContentViewTitleColour(contentViewCarousel, pt_title_clr);
            setCustomContentViewTitleColour(contentViewSmall, pt_title_clr);

            setCustomContentViewMessageColour(contentViewCarousel, pt_msg_clr);
            setCustomContentViewMessageColour(contentViewSmall, pt_msg_clr);

            setCustomContentViewMessageSummary(contentViewCarousel, pt_msg_summary);

            contentViewCarousel.setInt(R.id.view_flipper, "setFlipInterval", 4000);

            Intent launchIntent = new Intent(context, CTPushNotificationReceiver.class);

            PendingIntent pIntent;

            if (deepLinkList != null) {
                pIntent = setPendingIntent(context, notificationId, extras, launchIntent, deepLinkList.get(0));
            } else {
                pIntent = setPendingIntent(context, notificationId, extras, launchIntent, null);
            }

            NotificationCompat.Builder notificationBuilder = setBuilderWithChannelIDCheck(requiresChannelId, channelId, context);

            setNotificationBuilderBasics(notificationBuilder, contentViewSmall, contentViewCarousel, pt_title, pIntent);

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

            setCustomContentViewLargeIcon(contentViewSmall, pt_large_icon, context, notification, notificationId);

            Utils.loadIntoGlide(context, R.id.small_icon, smallIcon, contentViewCarousel, notification, notificationId);
            Utils.loadIntoGlide(context, R.id.small_icon, smallIcon, contentViewSmall, notification, notificationId);


            raiseNotificationViewed(context, extras);
        } catch (Throwable t) {
            PTLog.verbose("Error creating auto carousel notification ", t);
        }
    }

    private void renderManualCarouselNotification(Context context, Bundle extras, int notificationId) {
        try {
            notificationId = setNotificationId(notificationId);

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

            int reqCodePos0 = new Random().nextInt();
            int reqCodePos1 = new Random().nextInt();
            int reqCodePos2 = new Random().nextInt();

            contentViewManualCarousel.setViewVisibility(R.id.leftArrowPos0, View.VISIBLE);
            contentViewManualCarousel.setViewVisibility(R.id.rightArrowPos0, View.VISIBLE);

            contentViewManualCarousel.setViewVisibility(R.id.leftArrowPos1, View.INVISIBLE);
            contentViewManualCarousel.setViewVisibility(R.id.rightArrowPos1, View.INVISIBLE);

            contentViewManualCarousel.setViewVisibility(R.id.leftArrowPos2, View.INVISIBLE);
            contentViewManualCarousel.setViewVisibility(R.id.rightArrowPos2, View.INVISIBLE);


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
            leftArrowPos0Intent.putExtra("notif_id", notificationId);
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

            setNotificationBuilderBasics(notificationBuilder, contentViewSmall, contentViewManualCarousel, pt_title, pIntent);


            Notification notification = notificationBuilder.build();
            notificationManager.notify(notificationId, notification);

            Utils.loadIntoGlide(context, R.id.small_icon, pt_large_icon, contentViewSmall, notification, notificationId);

            Utils.loadIntoGlide(context, R.id.carousel_image, imageList.get(0), contentViewManualCarousel, notification, notificationId);

            setCustomContentViewLargeIcon(contentViewSmall, pt_large_icon, context, notification, notificationId);

            Utils.loadIntoGlide(context, R.id.small_icon, smallIcon, contentViewManualCarousel, notification, notificationId);
            Utils.loadIntoGlide(context, R.id.small_icon, smallIcon, contentViewSmall, notification, notificationId);


            raiseNotificationViewed(context, extras);
        } catch (Throwable t) {
            PTLog.verbose("Error creating auto carousel notification ", t);
        }
    }

    private void renderBasicTemplateNotification(Context context, Bundle extras, int notificationId) {
        try {
            contentViewBig = new RemoteViews(context.getPackageName(), R.layout.image_only_big);
            setCustomContentViewBasicKeys(contentViewBig, context);

            contentViewSmall = new RemoteViews(context.getPackageName(), R.layout.content_view_small);

            setCustomContentViewBasicKeys(contentViewSmall, context);

            setCustomContentViewTitle(contentViewBig, pt_title);
            setCustomContentViewTitle(contentViewSmall, pt_title);

            setCustomContentViewMessage(contentViewBig, pt_msg);
            setCustomContentViewMessage(contentViewSmall, pt_msg);

            setCustomContentViewExpandedBackgroundColour(contentViewBig, pt_bg);
            setCustomContentViewCollapsedBackgroundColour(contentViewSmall, pt_bg);

            setCustomContentViewTitleColour(contentViewBig, pt_title_clr);
            setCustomContentViewTitleColour(contentViewSmall, pt_title_clr);

            setCustomContentViewMessageColour(contentViewBig, pt_msg_clr);
            setCustomContentViewMessageColour(contentViewSmall, pt_msg_clr);

            notificationId = setNotificationId(notificationId);

            Intent launchIntent = new Intent(context, CTPushNotificationReceiver.class);

            PendingIntent pIntent;

            if (deepLinkList != null) {
                pIntent = setPendingIntent(context, notificationId, extras, launchIntent, deepLinkList.get(0));
            } else {
                pIntent = setPendingIntent(context, notificationId, extras, launchIntent, null);
            }

            NotificationCompat.Builder notificationBuilder = setBuilderWithChannelIDCheck(requiresChannelId, channelId, context);

            setNotificationBuilderBasics(notificationBuilder, contentViewSmall, contentViewBig, pt_title, pIntent);

            Notification notification = notificationBuilder.build();
            notificationManager.notify(notificationId, notification);

            setCustomContentViewBigImage(contentViewBig, pt_big_img, context, notification, notificationId);

            setCustomContentViewLargeIcon(contentViewSmall, pt_large_icon, context, notification, notificationId);

            Utils.loadIntoGlide(context, R.id.small_icon, smallIcon, contentViewBig, notification, notificationId);
            Utils.loadIntoGlide(context, R.id.small_icon, smallIcon, contentViewSmall, notification, notificationId);

            raiseNotificationViewed(context, extras);
        } catch (Throwable t) {
            PTLog.verbose("Error creating image only notification", t);
        }
    }

    private void renderProductDisplayNotification(Context context, Bundle extras, int notificationId) {
        try {

            contentViewBig = new RemoteViews(context.getPackageName(), R.layout.product_display_template);

            setCustomContentViewBasicKeys(contentViewBig, context);

            contentViewSmall = new RemoteViews(context.getPackageName(), R.layout.content_view_small);

            setCustomContentViewBasicKeys(contentViewSmall, context);

            if (!bigTextList.isEmpty()) {
                contentViewBig.setTextViewText(R.id.product_name, bigTextList.get(0));

            }

            if (!smallTextList.isEmpty()) {
                contentViewBig.setTextViewText(R.id.product_description, smallTextList.get(0));

            }

            if (!priceList.isEmpty()) {
                contentViewBig.setTextViewText(R.id.product_price, priceList.get(0));

            }

            setCustomContentViewTitle(contentViewBig, pt_title);
            setCustomContentViewTitle(contentViewSmall, pt_title);

            setCustomContentViewMessage(contentViewBig, pt_msg);
            setCustomContentViewMessage(contentViewSmall, pt_msg);

            setCustomContentViewTitleColour(contentViewBig, pt_title_clr);
            setCustomContentViewTitleColour(contentViewSmall, pt_title_clr);

            setCustomContentViewMessageColour(contentViewBig, pt_msg_clr);
            setCustomContentViewMessageColour(contentViewSmall, pt_msg_clr);

            setCustomContentViewExpandedBackgroundColour(contentViewBig, pt_bg);
            setCustomContentViewCollapsedBackgroundColour(contentViewSmall, pt_bg);

            setCustomContentViewButtonLabel(contentViewBig, R.id.product_action, pt_product_display_action);
            setCustomContentViewButtonColour(contentViewSmall, R.id.product_action, pt_product_display_action_clr);

            notificationId = setNotificationId(notificationId);

            int requestCode1 = new Random().nextInt();
            int requestCode2 = new Random().nextInt();
            int requestCode3 = new Random().nextInt();

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
            notificationIntent4.putExtra(Constants.PT_BUY_NOW_DL, deepLinkList.get(0));
            notificationIntent4.putExtra("pt_reqcode1", requestCode1);
            notificationIntent4.putExtra("pt_reqcode2", requestCode2);
            notificationIntent4.putExtra("pt_reqcode3", requestCode3);
            notificationIntent4.putExtra("buynow", true);
            notificationIntent4.putExtras(extras);
            PendingIntent contentIntent4 = PendingIntent.getBroadcast(context, new Random().nextInt(), notificationIntent4, 0);
            contentViewBig.setOnClickPendingIntent(R.id.product_action, contentIntent4);


            Intent launchIntent = new Intent(context, CTPushNotificationReceiver.class);

            PendingIntent pIntent;

            if (deepLinkList != null) {
                pIntent = setPendingIntent(context, notificationId, extras, launchIntent, deepLinkList.get(0));
            } else {
                pIntent = setPendingIntent(context, notificationId, extras, launchIntent, null);
            }

            NotificationCompat.Builder notificationBuilder = setBuilderWithChannelIDCheck(requiresChannelId, channelId, context);

            setNotificationBuilderBasics(notificationBuilder, contentViewSmall, contentViewBig, pt_title, pIntent);

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
            raiseNotificationViewed(context, extras);
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

            setCustomContentViewExpandedBackgroundColour(contentFiveCTAs,pt_bg);


            notificationId = setNotificationId(notificationId);

            int reqCode1 = new Random().nextInt();
            int reqCode2 = new Random().nextInt();
            int reqCode3 = new Random().nextInt();
            int reqCode4 = new Random().nextInt();
            int reqCode5 = new Random().nextInt();
            int reqCode6 = new Random().nextInt();


            Intent notificationIntent1 = new Intent(context, PushTemplateReceiver.class);
            notificationIntent1.putExtra("cta1", true);
            notificationIntent1.putExtra(Constants.PT_NOTIF_ID, notificationId);
            notificationIntent1.putExtras(extras);
            PendingIntent contentIntent1 = PendingIntent.getBroadcast(context, reqCode1, notificationIntent1, 0);
            contentFiveCTAs.setOnClickPendingIntent(R.id.cta1, contentIntent1);

            Intent notificationIntent2 = new Intent(context, PushTemplateReceiver.class);
            notificationIntent2.putExtra("cta2", true);
            notificationIntent2.putExtra(Constants.PT_NOTIF_ID, notificationId);
            notificationIntent2.putExtras(extras);
            PendingIntent contentIntent2 = PendingIntent.getBroadcast(context, reqCode2, notificationIntent2, 0);
            contentFiveCTAs.setOnClickPendingIntent(R.id.cta2, contentIntent2);

            Intent notificationIntent3 = new Intent(context, PushTemplateReceiver.class);
            notificationIntent3.putExtra("cta3", true);
            notificationIntent3.putExtra(Constants.PT_NOTIF_ID, notificationId);
            notificationIntent3.putExtras(extras);
            PendingIntent contentIntent3 = PendingIntent.getBroadcast(context, reqCode3, notificationIntent3, 0);
            contentFiveCTAs.setOnClickPendingIntent(R.id.cta3, contentIntent3);

            Intent notificationIntent4 = new Intent(context, PushTemplateReceiver.class);
            notificationIntent4.putExtra("cta4", true);
            notificationIntent4.putExtra(Constants.PT_NOTIF_ID, notificationId);
            notificationIntent4.putExtras(extras);
            PendingIntent contentIntent4 = PendingIntent.getBroadcast(context, reqCode4, notificationIntent4, 0);
            contentFiveCTAs.setOnClickPendingIntent(R.id.cta4, contentIntent4);

            Intent notificationIntent5 = new Intent(context, PushTemplateReceiver.class);
            notificationIntent5.putExtra("cta5", true);
            notificationIntent5.putExtra(Constants.PT_NOTIF_ID, notificationId);
            notificationIntent5.putExtras(extras);
            PendingIntent contentIntent5 = PendingIntent.getBroadcast(context, reqCode5, notificationIntent5, 0);
            contentFiveCTAs.setOnClickPendingIntent(R.id.cta5, contentIntent5);

            Intent notificationIntent6 = new Intent(context, PushTemplateReceiver.class);
            notificationIntent6.putExtra("close", true);
            notificationIntent6.putExtra(Constants.PT_NOTIF_ID, notificationId);
            notificationIntent6.putExtras(extras);
            PendingIntent contentIntent6 = PendingIntent.getBroadcast(context, reqCode6, notificationIntent6, 0);
            contentFiveCTAs.setOnClickPendingIntent(R.id.close, contentIntent6);


            Intent launchIntent = new Intent(context, CTPushNotificationReceiver.class);

            PendingIntent pIntent = setPendingIntent(context, notificationId, extras, launchIntent, null);

            NotificationCompat.Builder notificationBuilder = setBuilderWithChannelIDCheck(requiresChannelId, channelId, context);

            setNotificationBuilderBasics(notificationBuilder, contentFiveCTAs, contentFiveCTAs, pt_title, pIntent);

            notificationBuilder.setOngoing(true);

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

            raiseNotificationViewed(context, extras);
        } catch (Throwable t) {
            PTLog.verbose("Error creating image only notification", t);
        }

    }

    private void renderZeroBezelNotification(Context context, Bundle extras, int notificationId) {
        try {
            contentViewBig = new RemoteViews(context.getPackageName(), R.layout.zero_bezel);
            setCustomContentViewBasicKeys(contentViewBig, context, R.color.white);

            Boolean text_only_small_view = pt_small_view != null && pt_small_view.equals("text_only");

            if (text_only_small_view) {
                contentViewSmall = new RemoteViews(context.getPackageName(), R.layout.cv_small_text_only);
                setCustomContentViewBasicKeys(contentViewSmall, context);
            } else {
                contentViewSmall = new RemoteViews(context.getPackageName(), R.layout.cv_small_zero_bezel);
                setCustomContentViewBasicKeys(contentViewSmall, context, R.color.white);
            }

            setCustomContentViewTitle(contentViewBig, pt_title);
            setCustomContentViewTitle(contentViewSmall, pt_title);

            setCustomContentViewMessage(contentViewBig, pt_msg);

            if (text_only_small_view) {
                contentViewSmall.setViewVisibility(R.id.msg, View.GONE);
            } else {
                setCustomContentViewMessage(contentViewSmall, pt_msg);
            }

            setCustomContentViewMessageSummary(contentViewBig, pt_msg_summary);

            setCustomContentViewTitleColour(contentViewBig, pt_title_clr);
            setCustomContentViewTitleColour(contentViewSmall, pt_title_clr);


            setCustomContentViewMessageColour(contentViewBig, pt_msg_clr);
            setCustomContentViewMessageColour(contentViewSmall, pt_msg_clr);

            notificationId = setNotificationId(notificationId);

            Intent launchIntent = new Intent(context, CTPushNotificationReceiver.class);

            PendingIntent pIntent;

            if (deepLinkList != null) {
                pIntent = setPendingIntent(context, notificationId, extras, launchIntent, deepLinkList.get(0));
            } else {
                pIntent = setPendingIntent(context, notificationId, extras, launchIntent, null);
            }

            NotificationCompat.Builder notificationBuilder = setBuilderWithChannelIDCheck(requiresChannelId, channelId, context);

            setNotificationBuilderBasics(notificationBuilder, contentViewSmall, contentViewBig, pt_title, pIntent);

            Notification notification = notificationBuilder.build();
            notificationManager.notify(notificationId, notification);


            setCustomContentViewBigImage(contentViewBig, pt_big_img, context, notification, notificationId);
            if (!text_only_small_view) {
                setCustomContentViewBigImage(contentViewSmall, pt_big_img, context, notification, notificationId);
            }

            if (text_only_small_view) {
                setCustomContentViewLargeIcon(contentViewSmall, pt_large_icon, context, notification, notificationId);
            }


            Utils.loadIntoGlide(context, R.id.small_icon, smallIcon, contentViewBig, notification, notificationId);

            if (!text_only_small_view) {
                Utils.loadIntoGlide(context, R.id.small_icon, smallIcon, contentViewSmall, notification, notificationId);
            }

            raiseNotificationViewed(context, extras);
        } catch (Throwable t) {
            PTLog.verbose("Error creating image only notification", t);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void renderTimerNotification(final Context context, Bundle extras, int notificationId) {
        try {

            contentViewTimer = new RemoteViews(context.getPackageName(), R.layout.timer);
            contentViewTimerCollapsed = new RemoteViews(context.getPackageName(), R.layout.timer_collapsed);

            int timer_end;

            if (pt_timer_threshold!=-1){
                timer_end = (pt_timer_threshold * 1000) + 1000;
            }else if (pt_timer_end >= Constants.PT_TIMER_MIN_THRESHOLD){
                timer_end = (pt_timer_end *1000) + 1000;
            } else  {
                PTLog.debug("Not rendering notification Timer End value lesser than threshold (10 seconds) from current time: " + Constants.PT_TIMER_END);
                return;
            }

            setCustomContentViewBasicKeys(contentViewTimer, context);
            setCustomContentViewBasicKeys(contentViewTimerCollapsed, context);

            setCustomContentViewTitle(contentViewTimer, pt_title);
            setCustomContentViewTitle(contentViewTimerCollapsed, pt_title);

            setCustomContentViewMessage(contentViewTimer, pt_msg);
            setCustomContentViewMessage(contentViewTimerCollapsed, pt_msg);

            setCustomContentViewExpandedBackgroundColour(contentViewTimer, pt_bg);
            setCustomContentViewCollapsedBackgroundColour(contentViewTimerCollapsed, pt_bg);

            setCustomContentViewChronometerBackgroundColour(contentViewTimer, pt_bg);
            setCustomContentViewChronometerBackgroundColour(contentViewTimerCollapsed, pt_bg);

            setCustomContentViewTitleColour(contentViewTimer, pt_title_clr);
            setCustomContentViewTitleColour(contentViewTimerCollapsed, pt_title_clr);

            setCustomContentViewChronometerTitleColour(contentViewTimer, pt_chrono_title_clr, pt_title_clr);
            setCustomContentViewChronometerTitleColour(contentViewTimerCollapsed, pt_chrono_title_clr, pt_title_clr);

            setCustomContentViewMessageColour(contentViewTimer, pt_msg_clr);
            setCustomContentViewMessageColour(contentViewTimerCollapsed, pt_msg_clr);

            setCustomContentViewMessageSummary(contentViewTimer,pt_msg_summary);

            contentViewTimer.setChronometer(R.id.chronometer, SystemClock.elapsedRealtime() + (timer_end), null, true);
            contentViewTimer.setChronometerCountDown(R.id.chronometer, true);

            contentViewTimerCollapsed.setChronometer(R.id.chronometer, SystemClock.elapsedRealtime() + (timer_end), null, true);
            contentViewTimerCollapsed.setChronometerCountDown(R.id.chronometer, true);

            notificationId = setNotificationId(notificationId);

            Intent launchIntent = new Intent(context, CTPushNotificationReceiver.class);

            PendingIntent pIntent;

            if (deepLinkList != null) {
                pIntent = setPendingIntent(context, notificationId, extras, launchIntent, deepLinkList.get(0));
            } else {
                pIntent = setPendingIntent(context, notificationId, extras, launchIntent, null);
            }

            NotificationCompat.Builder notificationBuilder = setBuilderWithChannelIDCheck(requiresChannelId, channelId, context);

            setNotificationBuilderBasics(notificationBuilder, contentViewTimerCollapsed, contentViewTimer, pt_title, pIntent);

            notificationBuilder.setTimeoutAfter(timer_end);

            Notification notification = notificationBuilder.build();
            notificationManager.notify(notificationId, notification);

            setCustomContentViewBigImage(contentViewTimer, pt_big_img, context, notification, notificationId);

            Utils.loadIntoGlide(context, R.id.small_icon, smallIcon, contentViewTimer, notification, notificationId);
            Utils.loadIntoGlide(context, R.id.small_icon, smallIcon, contentViewTimerCollapsed, notification, notificationId);

            raiseNotificationViewed(context, extras);

            timerRunner(context,extras, notificationId ,timer_end);

        } catch (Throwable t) {
            PTLog.verbose("Error creating Timer notification ", t);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT_WATCH)
    private void renderInputBoxNotification(final Context context, Bundle extras, int notificationId) {
        try {
            //Fetch Notif ID
            notificationId = setNotificationId(notificationId);

            //Set launchIntent to receiver
            Intent launchIntent = new Intent(context, CTPushNotificationReceiver.class);

            PendingIntent pIntent;

            if (deepLinkList != null) {
                pIntent = setPendingIntent(context, notificationId, extras, launchIntent, deepLinkList.get(0));
            } else {
                pIntent = setPendingIntent(context, notificationId, extras, launchIntent, null);
            }

            NotificationCompat.Builder notificationBuilder = setBuilderWithChannelIDCheck(requiresChannelId, channelId, context);

            notificationBuilder.setSmallIcon(smallIcon)
                    .setContentTitle(pt_title)
                    .setContentText(pt_msg)
                    .setContentIntent(pIntent)
                    .setVibrate(new long[]{0L})
                    .setWhen(System.currentTimeMillis())
                    .setAutoCancel(true);

            // Assign big picture notification
            setStandardViewBigImageStyle(pt_big_img, extras, context, notificationBuilder);

            //Initialise RemoteInput
            RemoteInput remoteInput = new RemoteInput.Builder(Constants.PT_INPUT_KEY)
                    .setLabel(pt_input_label)
                    .build();

            //Set launchIntent to receiver
            Intent replyIntent = new Intent(context, PushTemplateReceiver.class);
            replyIntent.putExtra(Constants.PT_INPUT_FEEDBACK, pt_input_feedback);
            replyIntent.putExtra(Constants.PT_INPUT_AUTO_OPEN, pt_input_auto_open);

            PendingIntent replyPendingIntent;
            if (deepLinkList != null) {
                replyPendingIntent = setPendingIntent(context, notificationId, extras, replyIntent, deepLinkList.get(0));
            } else {
                replyPendingIntent = setPendingIntent(context, notificationId, extras, replyIntent, null);
            }

            //Notification Action with RemoteInput instance added.
            NotificationCompat.Action replyAction = new NotificationCompat.Action.Builder(
                    android.R.drawable.sym_action_chat, pt_input_label, replyPendingIntent)
                    .addRemoteInput(remoteInput)
                    .setAllowGeneratedReplies(true)
                    .build();


            //Notification.Action instance added to Notification Builder.
            if (extras.getString(Constants.WZRK_ACTIONS) == null || extras.getString(Constants.WZRK_ACTIONS).isEmpty())
                notificationBuilder.addAction(replyAction);

            if (pt_dismiss_on_click != null)
                if (!pt_dismiss_on_click.isEmpty())
                    extras.putString(Constants.PT_DISMISS_ON_CLICK, pt_dismiss_on_click);

            setActionButtons(context, extras, notificationId, notificationBuilder);

            Notification notification = notificationBuilder.build();
            notificationManager.notify(notificationId, notification);

            raiseNotificationViewed(context, extras);

        } catch (Throwable t) {
            PTLog.verbose("Error creating Input Box notification ", t);
        }
    }
    private void renderVideoNotification(final Context context, Bundle extras, int notificationId) {
        try {
            contentViewBig = new RemoteViews(context.getPackageName(), R.layout.image_only_big);
            setCustomContentViewBasicKeys(contentViewBig, context);

            contentViewSmall = new RemoteViews(context.getPackageName(), R.layout.content_view_small);
            setCustomContentViewBasicKeys(contentViewSmall, context);

            setCustomContentViewTitle(contentViewBig,pt_title);
            setCustomContentViewTitle(contentViewSmall,pt_title);

            setCustomContentViewMessage(contentViewBig,pt_msg);
            setCustomContentViewMessage(contentViewSmall,pt_msg);

            setCustomContentViewExpandedBackgroundColour(contentViewBig,pt_bg);
            setCustomContentViewExpandedBackgroundColour(contentViewSmall,pt_bg);

            setCustomContentViewTitleColour(contentViewBig,pt_title_clr);
            setCustomContentViewTitleColour(contentViewSmall,pt_title_clr);

            setCustomContentViewMessageColour(contentViewBig,pt_msg_clr);
            setCustomContentViewMessageColour(contentViewSmall,pt_msg_clr);

            notificationId = setNotificationId(notificationId);

            Intent launchIntent = new Intent(context, VideoActivity.class);
            launchIntent.putExtras(extras);
            PendingIntent pIntent =null;
            if (pt_video_url != null ) {
                pIntent = PendingIntent.getActivity(context, 0, launchIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            }

            NotificationCompat.Builder notificationBuilder = setBuilderWithChannelIDCheck(requiresChannelId,channelId,context);

            setNotificationBuilderBasics(notificationBuilder,contentViewSmall, contentViewBig,pt_title, pIntent);

            Notification notification = notificationBuilder.build();
            notificationManager.notify(notificationId, notification);

            setCustomContentViewBigImage(contentViewBig,pt_big_img,context,notification,notificationId);

            setCustomContentViewLargeIcon(contentViewSmall,pt_large_icon,context,notification,notificationId);

            Utils.loadIntoGlide(context, R.id.small_icon, smallIcon, contentViewBig, notification, notificationId);
            Utils.loadIntoGlide(context, R.id.small_icon, smallIcon, contentViewSmall, notification, notificationId);

            raiseNotificationViewed(context,extras);
        } catch (Throwable t) {
            PTLog.verbose("Error creating image only notification", t);
        }
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
        PendingIntent pIntent = PendingIntent.getBroadcast(context, (int) System.currentTimeMillis(),
                launchIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        return pIntent;
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

    private void setStandardViewBigImageStyle(String pt_big_img, Bundle extras, Context context, NotificationCompat.Builder notificationBuilder) {
        NotificationCompat.Style bigPictureStyle;
        if (pt_big_img != null && pt_big_img.startsWith("http")) {
            try {
                Bitmap bpMap = Utils.getNotificationBitmap(pt_big_img, false, context);

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

    private void setCustomContentViewLargeIcon(RemoteViews contentView, String pt_large_icon, Context context, Notification notification, int notificationId) {
        if (pt_large_icon != null && !pt_large_icon.isEmpty()) {
            Utils.loadIntoGlide(context, R.id.large_icon, pt_large_icon, contentView, notification, notificationId);
        } else {
            contentView.setViewVisibility(R.id.large_icon, View.GONE);
        }
    }

    private void raiseNotificationViewed(Context context, Bundle extras) {
        CleverTapAPI instance = CleverTapAPI.getDefaultInstance(context);
        if (instance != null) {
            instance.pushNotificationViewedEvent(extras);
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

    private void setCustomContentViewBasicKeys(RemoteViews contentView, Context context, int color) {

        contentView.setTextViewText(R.id.app_name, Utils.getApplicationName(context));
        contentView.setTextViewText(R.id.timestamp, Utils.getTimeStamp(context));

        contentView.setTextColor(R.id.app_name, ContextCompat.getColor(context, color));
        contentView.setTextColor(R.id.timestamp, ContextCompat.getColor(context, color));

    }

    private void setCustomContentViewBigImage(RemoteViews contentView, String pt_big_img, Context context, Notification notification, int notificationId) {
        if (pt_big_img != null && !pt_big_img.isEmpty()) {
            Utils.loadIntoGlide(context, R.id.big_image, pt_big_img, contentView, notification, notificationId);
        } else {
            contentView.setViewVisibility(R.id.big_image, View.GONE);
        }
    }

    private int setNotificationId(int notificationId) {
        if (notificationId == Constants.EMPTY_NOTIFICATION_ID) {
            notificationId = (int) (Math.random() * 100);
        }
        return notificationId;
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

    private void setCustomContentViewMessageColour(RemoteViews contentView, int color) {
        contentView.setTextColor(R.id.msg, color);
    }

    private void setCustomContentViewTitleColour(RemoteViews contentView, String pt_title_clr) {
        if (pt_title_clr != null && !pt_title_clr.isEmpty()) {
            contentView.setTextColor(R.id.title, Color.parseColor(pt_title_clr));
        }
    }

    private void setCustomContentViewTitleColour(RemoteViews contentView, int color) {
        contentView.setTextColor(R.id.title, color);
    }

    private void setCustomContentViewChronometerTitleColour(RemoteViews contentView, String pt_chrono_title_clr, String pt_title_clr) {
        if (pt_chrono_title_clr != null && !pt_chrono_title_clr.isEmpty()) {
            contentView.setTextColor(R.id.chronometer, Color.parseColor(pt_chrono_title_clr));
        } else {
            if (pt_title_clr != null && !pt_title_clr.isEmpty()) {
                contentView.setTextColor(R.id.chronometer, Color.parseColor(pt_title_clr));
            }
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

    private void setCustomContentViewChronometerBackgroundColour(RemoteViews contentView, String pt_bg) {
        if (pt_bg != null && !pt_bg.isEmpty()) {
            contentView.setInt(R.id.chronometer, "setBackgroundColor", Color.parseColor(pt_bg));

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

    private void setActionButtons(Context context, Bundle extras, int notificationId, NotificationCompat.Builder nb) {
        JSONArray actions = null;

        String actionsString = extras.getString(Constants.WZRK_ACTIONS);
        if (actionsString != null) {
            try {
                actions = new JSONArray(actionsString);
            } catch (Throwable t) {
                PTLog.debug("error parsing notification actions: " + t.getLocalizedMessage());
            }
        }

        String intentServiceName = ManifestInfo.getInstance(context).getIntentServiceName();
        Class clazz = null;
        if (intentServiceName != null) {
            try {
                clazz = Class.forName(intentServiceName);
            } catch (ClassNotFoundException e) {
                try {
                    clazz = Class.forName("com.clevertap.pushtemplates.PTNotificationIntentService");
                } catch (ClassNotFoundException ex) {
                    PTLog.debug("No Intent Service found");
                }
            }
        } else {
            try {
                clazz = Class.forName("com.clevertap.pushtemplates.PTNotificationIntentService");
            } catch (ClassNotFoundException ex) {
                PTLog.debug("No Intent Service found");
            }
        }

        boolean isPTIntentServiceAvailable = isServiceAvailable(context, clazz);


        if (actions != null && actions.length() > 0) {
            for (int i = 0; i < actions.length(); i++) {
                try {
                    JSONObject action = actions.getJSONObject(i);
                    String label = action.optString("l");
                    String dl = action.optString("dl");
                    String ico = action.optString(Constants.PT_NOTIF_ICON);
                    String id = action.optString("id");
                    boolean autoCancel = action.optBoolean("ac", true);
                    if (label.isEmpty() || id.isEmpty()) {
                        PTLog.debug("not adding push notification action: action label or id missing");
                        continue;
                    }
                    int icon = 0;
                    if (!ico.isEmpty()) {
                        try {
                            icon = context.getResources().getIdentifier(ico, "drawable", context.getPackageName());
                        } catch (Throwable t) {
                            PTLog.debug("unable to add notification action icon: " + t.getLocalizedMessage());
                        }
                    }

                    boolean sendToPTIntentService = (autoCancel && isPTIntentServiceAvailable);

                    Intent actionLaunchIntent;
                    if (sendToPTIntentService) {
                        actionLaunchIntent = new Intent(PTNotificationIntentService.MAIN_ACTION);
                        actionLaunchIntent.setPackage(context.getPackageName());
                        actionLaunchIntent.putExtra("pt_type", PTNotificationIntentService.TYPE_BUTTON_CLICK);
                        if (!dl.isEmpty()) {
                            actionLaunchIntent.putExtra("dl", dl);
                        }
                    } else {
                        if (!dl.isEmpty()) {
                            actionLaunchIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(dl));
                        } else {
                            actionLaunchIntent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
                        }
                    }

                    if (actionLaunchIntent != null) {
                        actionLaunchIntent.putExtras(extras);
                        actionLaunchIntent.removeExtra(Constants.WZRK_ACTIONS);
                        actionLaunchIntent.putExtra("actionId", id);
                        actionLaunchIntent.putExtra("autoCancel", autoCancel);
                        actionLaunchIntent.putExtra("wzrk_c2a", id);
                        actionLaunchIntent.putExtra("notificationId", notificationId);
                        actionLaunchIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    }

                    PendingIntent actionIntent = null;
                    int requestCode = ((int) System.currentTimeMillis()) + i;
                    if (sendToPTIntentService) {
                        actionIntent = PendingIntent.getService(context, requestCode,
                                actionLaunchIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                    } else {
                        actionIntent = PendingIntent.getActivity(context, requestCode,
                                actionLaunchIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                    }

                    nb.addAction(icon, label, actionIntent);

                } catch (Throwable t) {
                    PTLog.debug("error adding notification action : " + t.getLocalizedMessage());
                }
            }
        }
    }

    @SuppressWarnings("SameParameterValue")
    private static boolean isServiceAvailable(Context context, Class clazz) {
        if (clazz == null) return false;

        PackageManager pm = context.getPackageManager();
        String packageName = context.getPackageName();

        PackageInfo packageInfo;
        try {
            packageInfo = pm.getPackageInfo(packageName, PackageManager.GET_SERVICES);
            ServiceInfo[] services = packageInfo.services;
            for (ServiceInfo serviceInfo : services) {
                if (serviceInfo.name.equals(clazz.getName())) {
                    PTLog.verbose("Service " + serviceInfo.name + " found");
                    return true;
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            PTLog.debug("Intent Service name not found exception - " + e.getLocalizedMessage());
        }
        return false;
    }

    private void timerRunner(final Context context, final Bundle extras, final int notificationId, final int delay){
        final Handler handler = new Handler(Looper.getMainLooper());

        extras.remove("wzrk_rnv");

        if (pt_big_img_alt != null || !pt_big_img_alt.isEmpty()) {
            extras.putString(Constants.PT_BIG_IMG, pt_big_img_alt);
            extras.putString(Constants.PT_TITLE, pt_msg_alt);
            extras.putString(Constants.PT_MSG, pt_title_alt);
            extras.putString(Constants.PT_ID, "pt_basic");
            handler.postDelayed(new Runnable() {
                @RequiresApi(api = Build.VERSION_CODES.O)
                @Override
                public void run() {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (Utils.isNotificationInTray(context, notificationId)) {
                            if(hasAllBasicNotifKeys()) {
                                renderBasicTemplateNotification(context, extras, Constants.EMPTY_NOTIFICATION_ID);
                            }
                        }
                    }

                }
            }, delay - 300);
        }
    }

}
