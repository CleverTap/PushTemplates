package com.clevertap.pushtemplates;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.widget.RemoteViews;
import android.widget.Toast;


import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.NotificationTarget;
import com.clevertap.android.sdk.CleverTapAPI;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static android.content.Context.NOTIFICATION_SERVICE;

@SuppressWarnings("WeakerAccess")
public class Utils {

    public static boolean isPNFromCleverTap(Bundle extras){
        if(extras == null) return false;

        boolean fromCleverTap = extras.containsKey(Constants.NOTIF_TAG);
        boolean shouldRender = fromCleverTap && extras.containsKey("nm");
        return fromCleverTap && shouldRender;
    }


    @SuppressWarnings("unused")
    static Bitmap getNotificationBitmap(String icoPath, boolean fallbackToAppIcon,
                                        final Context context)
            throws NullPointerException {
        // If the icon path is not specified
        if (icoPath == null || icoPath.equals("")) {
            return fallbackToAppIcon ? getAppIcon(context) : null;
        }
        // Simply stream the bitmap
        if (!icoPath.startsWith("http")) {
            icoPath = Constants.ICON_BASE_URL + "/" + icoPath;
        }
        Bitmap ic = getBitmapFromURL(icoPath);
        return (ic != null) ? ic : ((fallbackToAppIcon) ? getAppIcon(context) : null);
    }

    private static Bitmap getAppIcon(final Context context) throws NullPointerException {
        // Try to get the app logo first
        try {
            Drawable logo =
                    context.getPackageManager().getApplicationLogo(context.getApplicationInfo());
            if (logo == null)
                throw new Exception("Logo is null");
            return drawableToBitmap(logo);
        } catch (Exception e) {
            // Try to get the app icon now
            // No error handling here - handle upstream
            return drawableToBitmap(
                    context.getPackageManager().getApplicationIcon(context.getApplicationInfo()));
        }
    }

    static Bitmap drawableToBitmap(Drawable drawable)
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

    private static Bitmap getBitmapFromURL(String srcUrl) {
        // Safe bet, won't have more than three /s
        srcUrl = srcUrl.replace("///", "/");
        srcUrl = srcUrl.replace("//", "/");
        srcUrl = srcUrl.replace("http:/", "http://");
        srcUrl = srcUrl.replace("https:/", "https://");
        HttpURLConnection connection = null;
        try {
            URL url = new URL(srcUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            return BitmapFactory.decodeStream(input);
        } catch (IOException e) {

            PTLog.verbose("Couldn't download the notification icon. URL was: " + srcUrl);
            return null;
        } finally {
            try {
                if (connection != null) {
                    connection.disconnect();
                }
            } catch (Throwable t) {
                PTLog.verbose("Couldn't close connection!", t);
            }
        }
    }

    @SuppressWarnings("SameParameterValue")
    static String _getManifestStringValueForKey(Bundle manifest, String name) {
        try {
            Object o = manifest.get(name);
            return (o != null) ? o.toString() : null;
        } catch (Throwable t) {
            return null;
        }
    }

    static int getAppIconAsIntId(final Context context) {
        ApplicationInfo ai = context.getApplicationInfo();
        return ai.icon;
    }

    static ArrayList<String> getImageListFromExtras(Bundle extras){
        ArrayList<String> imageList = new ArrayList<>();
        for(String key : extras.keySet()){
            if(key.contains("pt_img")){
                imageList.add(extras.getString(key));
            }
        }
        return imageList;
    }

    @SuppressWarnings("unused")
    static ArrayList<String> getCTAListFromExtras(Bundle extras){
        ArrayList<String> ctaList = new ArrayList<>();
        for(String key : extras.keySet()){
            if(key.contains("pt_cta")){
                ctaList.add(extras.getString(key));
            }
        }
        return ctaList;
    }

    static ArrayList<String> getDeepLinkListFromExtras(Bundle extras){
        ArrayList<String> dlList = new ArrayList<>();
        for(String key : extras.keySet()){
            if(key.contains("pt_dl")){
                dlList.add(extras.getString(key));
            }
        }
        return dlList;
    }

    static ArrayList<String> getBigTextFromExtras(Bundle extras){
        ArrayList<String> btList = new ArrayList<>();
        for(String key : extras.keySet()){
            if(key.contains("pt_bt")){
                btList.add(extras.getString(key));
            }
        }
        return btList;
    }
    static ArrayList<String> getSmallTextFromExtras(Bundle extras){
        ArrayList<String> stList = new ArrayList<>();
        for(String key : extras.keySet()){
            if(key.contains("pt_st")){
                stList.add(extras.getString(key));
            }
        }
        return stList;
    }

    static ArrayList<String> getPriceFromExtras(Bundle extras){
        ArrayList<String> stList = new ArrayList<>();
        for(String key : extras.keySet()){
            if(key.contains("pt_price")){
                stList.add(extras.getString(key));
            }
        }
        return stList;
    }

    static void loadIntoGlide(Context context,
                              int imageResource,
                              String imageURL,
                              RemoteViews remoteViews,
                              Notification notification,
                              int notificationId) {
        Glide
                .with(context.getApplicationContext())
                .asBitmap()
                .load(imageURL)
                .centerCrop()
                .into(buildNotificationTarget(context,imageResource,remoteViews,notification,
                        notificationId));
    }

    static void loadIntoGlide(Context context, int imageResource, int identifier,
                              RemoteViews remoteViews, Notification notification,
                              int notificationId) {
        Glide
                .with(context.getApplicationContext())
                .asBitmap()
                .load(identifier)
                .centerCrop()
                .into(buildNotificationTarget(context,imageResource,remoteViews,notification,
                        notificationId));
    }

    static void loadIntoGlide(Context context, int imageResource, Bitmap image,
                              RemoteViews remoteViews, Notification notification,
                              int notificationId) {
        Glide
                .with(context.getApplicationContext())
                .asBitmap()
                .load(image)
                .centerCrop()
                .into(buildNotificationTarget(context,imageResource,remoteViews,notification,
                        notificationId));
    }

    static NotificationTarget buildNotificationTarget(Context context, int imageResource,
                                                      RemoteViews remoteViews,
                                                      Notification notification,
                                                      int notificationId ){
        return new NotificationTarget(
                context,
                imageResource,
                remoteViews,
                notification,
                notificationId);
    }

    static String getTimeStamp(Context context) {
        return DateUtils.formatDateTime(context, System.currentTimeMillis(),
                DateUtils.FORMAT_SHOW_TIME);
    }

    static String getApplicationName(Context context) {
        ApplicationInfo applicationInfo = context.getApplicationInfo();
        int stringId = applicationInfo.labelRes;
        return stringId == 0 ? applicationInfo.nonLocalizedLabel.toString()
                : context.getString(stringId);
    }

    @SuppressWarnings("ConstantConditions")
    static Bundle fromJson(JSONObject s) {
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
            } else if (str != null)
                bundle.putString(key, str);

            else
                System.err.println("unable to transform json to bundle " + key);
        }

        return bundle;
    }

    static String bundleToJSON(Bundle extras) {
        JSONObject json = new JSONObject();
        Set<String> keys = extras.keySet();
        for (String key : keys) {
            try {
                json.put(key, extras.get(key));
            } catch(JSONException e) {
                //Handle exception here
            }
        }
        return json.toString();
    }

    static void cancelNotification(Context ctx, int notifyId) {
        String ns = NOTIFICATION_SERVICE;
        NotificationManager nMgr = (NotificationManager) ctx.getSystemService(ns);
        nMgr.cancel(notifyId);
    }

    static int getTimerThreshold(Bundle extras){
        String val = "-1";
        for(String key : extras.keySet()){
            if(key.contains(Constants.PT_TIMER_THRESHOLD)){
                val =  extras.getString(key);
            }
        }
        return Integer.parseInt(val);
    }

    static void setPackageNameFromResolveInfoList(Context context, Intent launchIntent){
        List<ResolveInfo> resolveInfoList = context.getPackageManager().queryIntentActivities(launchIntent,0);
        if(resolveInfoList != null){
            String appPackageName = context.getPackageName();
            for(ResolveInfo resolveInfo : resolveInfoList){
                if(appPackageName.equals(resolveInfo.activityInfo.packageName)){
                    launchIntent.setPackage(appPackageName);
                    break;
                }
            }
        }
    }

    static void raiseCleverTapEvent(CleverTapAPI cleverTapAPI, Bundle extras) {

        HashMap<String, Object> eProps;
        eProps = getEventPropertiesFromExtras(extras);

        String eName = getEventNameFromExtras(extras);

        if (eName != null || !eName.isEmpty()){
            if (eProps != null)
                cleverTapAPI.pushEvent(eName, eProps);
            else
                cleverTapAPI.pushEvent(eName);
        }

    }

    static void raiseCleverTapEvent(CleverTapAPI cleverTapAPI, Bundle extras,String key) {

        HashMap<String, Object> eProps;
        String value = extras.getString(key);

        eProps = getEventPropertiesFromExtras(extras,key,value);

        String eName = getEventNameFromExtras(extras);

        if (eName != null || !eName.isEmpty()){
            if (eProps != null)
                cleverTapAPI.pushEvent(eName, eProps);
            else
                cleverTapAPI.pushEvent(eName);
        }

    }

    static String getEventNameFromExtras(Bundle extras) {
        String eName = null;
        for(String key : extras.keySet()){
            if(key.contains(Constants.PT_EVENT_NAME_KEY)){
                eName = extras.getString(key);
            }
        }
        return eName;
    }

    static HashMap<String, Object> getEventPropertiesFromExtras(Bundle extras,String pkey, String value) {
        HashMap<String, Object> eProps = new HashMap<>();

        String[] eProp;
        for(String key : extras.keySet()){
            if(key.contains(Constants.PT_EVENT_PROPERTY_KEY)){
                if (extras.getString(key) != null || !extras.getString(key).isEmpty()) {
                    if(key.contains(Constants.PT_EVENT_PROPERTY_SEPERATOR)){
                        eProp = key.split(Constants.PT_EVENT_PROPERTY_SEPERATOR);
                        if(extras.getString(key).equalsIgnoreCase(pkey)) {
                            eProps.put(eProp[1],value);
                            continue;
                        }
                        eProps.put(eProp[1],extras.getString(key));
                    }else{
                        PTLog.verbose("Property " + key + " does not have the separator");
                    }

                }
                else{
                    PTLog.verbose("Property Key is Empty. Skipping Property: " + key);
                }

            }
        }
        return eProps;
    }


    static HashMap<String, Object> getEventPropertiesFromExtras(Bundle extras) {
        HashMap<String, Object> eProps = new HashMap<>();

        String[] eProp;
        for(String key : extras.keySet()){
            if(key.contains(Constants.PT_EVENT_PROPERTY_KEY)){
                if (extras.getString(key) != null || !extras.getString(key).isEmpty()) {
                    if(key.contains(Constants.PT_EVENT_PROPERTY_SEPERATOR)){
                        eProp = key.split(Constants.PT_EVENT_PROPERTY_SEPERATOR);
                        eProps.put(eProp[1],extras.getString(key));
                    }else{
                        PTLog.verbose("Property " + key + " does not have the separator");
                    }

                }
                else{
                    PTLog.verbose("Property Key is Empty. Skipping Property: " + key);
                }

            }
        }
        return eProps;
    }

    public static int getTimerEnd(Bundle extras) {
        String val = "-1";
        for(String key : extras.keySet()){
            if(key.contains(Constants.PT_TIMER_END)){
                val =  extras.getString(key);
            }
        }
        if (val.contains("$D_")){
            String[] temp =  val.split(Constants.PT_TIMER_SPLIT);
            val = temp[1];
        }
        long currentts =  System.currentTimeMillis();
        int diff = (int) (Long.parseLong(val) - (currentts/1000));
        return diff;
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    public static boolean isNotificationInTray(Context context, int notificationId) {
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        StatusBarNotification[] notifications = mNotificationManager.getActiveNotifications();
        for (StatusBarNotification notification : notifications) {
            if (notification.getId() == notificationId) {
                return true;
            }
        }
        return false;
    }


    public static ArrayList<Integer> getNotificationIds(Context context) {
        ArrayList<Integer> ids = new ArrayList<Integer>();
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        StatusBarNotification[] notifications = new StatusBarNotification[0];
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            notifications = mNotificationManager.getActiveNotifications();
            for (StatusBarNotification notification : notifications) {
                if(notification.getPackageName().equalsIgnoreCase(context.getPackageName())){
                    ids.add(notification.getId());
                }
            }
        }
        return ids;
    }

    @SuppressWarnings("SameParameterValue")
    static boolean isServiceAvailable(Context context, Class clazz) {
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

    static void raiseNotificationClicked(Context context, Bundle extras) {
        CleverTapAPI instance = CleverTapAPI.getDefaultInstance(context);
        if (instance != null) {
            instance.pushNotificationClickedEvent(extras);
        }
    }

    static JSONArray getActionKeys(Bundle extras){
        JSONArray actions = null;

        String actionsString = extras.getString(Constants.WZRK_ACTIONS);
        if (actionsString != null) {
            try {
                actions = new JSONArray(actionsString);
            } catch (Throwable t) {
                PTLog.debug("error parsing notification actions: " + t.getLocalizedMessage());
            }
        }
        return actions;
    }

    static void showToast(final Context context, final String message, final int duration) {
        AsyncHelper.getMainThreadHandler().post(new Runnable() {

            Toast toast;

            @Override
            public void run() {
                if (!TextUtils.isEmpty(message)) {
                    if (toast != null) {
                        toast.cancel(); //dismiss current toast if visible
                        toast.setText(message);
                    } else {
                        toast = Toast.makeText(context, message, duration);
                    }
                    toast.show();
                }
            }
        });
    }

    static void createSilentNotificationChannel(Context context){
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        if (notificationManager == null) return;
        NotificationChannel notificationChannel = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notificationChannel = new NotificationChannel(Constants.PT_SILENT_CHANNEL_ID, Constants.PT_SILENT_CHANNEL_NAME, NotificationManager.IMPORTANCE_NONE);
            notificationChannel.setDescription(Constants.PT_SILENT_CHANNEL_DESC);
            notificationChannel.setShowBadge(false);
            notificationManager.createNotificationChannel(notificationChannel);
        }

    }

    static void deleteSilentNotificationChannel(Context context){
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        if (notificationManager == null) return;
        NotificationChannel notificationChannel = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notificationManager.deleteNotificationChannel(Constants.PT_SILENT_CHANNEL_ID);
        }

    }

    static Bitmap setBitMapColour(Context context, int resourceID, String clr)
            throws NullPointerException {
        if (clr != null && !clr.isEmpty()) {
            int color = getColour(clr, Constants.PT_COLOUR_GREY);

            Drawable mDrawable = Objects.requireNonNull(ContextCompat.getDrawable(context, resourceID)).mutate();
            mDrawable.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN));
            return Utils.drawableToBitmap(mDrawable);
        }
        return null;
    }

    static int getColour(String clr, String default_clr){
        try{
            return Color.parseColor(clr);
        }
        catch (Exception e){
            return Color.parseColor(default_clr);
        }
    }
}
