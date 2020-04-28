package com.clevertap.pushtemplates;

import android.app.Notification;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.widget.RemoteViews;


import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.NotificationTarget;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

class Utils {

    static boolean isPNFromCleverTap(Bundle extras){
        if(extras == null) return false;

        boolean fromCleverTap = extras.containsKey(Constants.NOTIF_TAG);
        boolean shouldRender = fromCleverTap && extras.containsKey("nm");
        return fromCleverTap && shouldRender;
    }



    static Bitmap getNotificationBitmap(String icoPath, boolean fallbackToAppIcon, final Context context)
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
        //noinspection ConstantConditions
        return (ic != null) ? ic : ((fallbackToAppIcon) ? getAppIcon(context) : null);
    }

    private static Bitmap getAppIcon(final Context context) throws NullPointerException {
        // Try to get the app logo first
        try {
            Drawable logo = context.getPackageManager().getApplicationLogo(context.getApplicationInfo());
            if (logo == null)
                throw new Exception("Logo is null");
            return drawableToBitmap(logo);
        } catch (Exception e) {
            // Try to get the app icon now
            // No error handling here - handle upstream
            return drawableToBitmap(context.getPackageManager().getApplicationIcon(context.getApplicationInfo()));
        }
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

    static void loadIntoGlide(Context context, int imageResource, String imageURL, RemoteViews remoteViews, Notification notification, int notificationId) {
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
                .centerCrop()
                .into(bigNotifTarget);
    }

    static void loadIntoGlide(Context context, int imageResource, int identifier, RemoteViews remoteViews, Notification notification, int notificationId) {
        NotificationTarget bigNotifTarget = new NotificationTarget(
                context,
                imageResource,
                remoteViews,
                notification,
                notificationId);
        Glide
                .with(context.getApplicationContext())
                .asBitmap()
                .load(identifier)
                .centerCrop()
                .into(bigNotifTarget);
    }

    static String getTimeStamp(Context context) {
        return DateUtils.formatDateTime(context, System.currentTimeMillis(), DateUtils.FORMAT_SHOW_TIME);
    }
}
