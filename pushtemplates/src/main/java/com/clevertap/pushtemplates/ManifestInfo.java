package com.clevertap.pushtemplates;


import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;

class ManifestInfo {
    private static String notificationIcon;
    private static ManifestInfo instance;
    private static String fcmSenderId;
    private static String intentServiceName;

    private static String _getManifestStringValueForKey(Bundle manifest, String name) {
        try {
            Object o = manifest.get(name);
            return (o != null) ? o.toString() : null;
        } catch (Throwable t) {
            return null;
        }
    }

    private ManifestInfo(Context context) {
        Bundle metaData = null;
        try {
            PackageManager pm = context.getPackageManager();
            ApplicationInfo ai = pm.getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            metaData = ai.metaData;
        } catch (Throwable t) {
            // no-op
        }
        if (metaData == null) {
            metaData = new Bundle();
        }

        notificationIcon = _getManifestStringValueForKey(metaData,Constants.LABEL_NOTIFICATION_ICON);
        if (fcmSenderId != null) {
            fcmSenderId = fcmSenderId.replace("id:", "");
        }
    }

    synchronized static ManifestInfo getInstance(Context context){
        if (instance == null) {
            instance = new ManifestInfo(context);
        }
        return instance;
    }

    String getIntentServiceName (){
        return intentServiceName;
    }

}