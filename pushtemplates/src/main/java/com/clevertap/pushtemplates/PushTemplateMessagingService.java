package com.clevertap.pushtemplates;

import android.content.Context;
import android.os.Bundle;
import com.clevertap.android.sdk.CleverTapAPI;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONObject;

import java.util.Map;

public class PushTemplateMessagingService extends FirebaseMessagingService {

    Context context;
    CleverTapAPI instance;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        try{
            PTLog.debug("Inside Push Templates");
            context = getApplicationContext();
            if (remoteMessage.getData().size() > 0) {
                Bundle extras = new Bundle();
                for (Map.Entry<String, String> entry : remoteMessage.getData().entrySet()) {
                    extras.putString(entry.getKey(), entry.getValue());
                }

                instance = CleverTapAPI.getDefaultInstance(getApplicationContext());

                boolean processCleverTapPN = Utils.isPNFromCleverTap(extras);

                if(processCleverTapPN){
                    String pt_json = extras.getString(Constants.PT_JSON);
                    if(pt_json != null && !pt_json.isEmpty()) {
                        JSONObject json = new JSONObject(pt_json);
                        extras = Utils.fromJson(json);
                    }

                    String pt_id = extras.getString(Constants.PT_ID);

                    if(("0").equals(pt_id) || pt_id == null || pt_id.isEmpty()){
                        CleverTapAPI.createNotification(context,extras);
                    }else{
                        TemplateRenderer.createNotification(context,extras);
                        if (instance != null) {
                            instance.pushNotificationViewedEvent(extras);
                        }
                    }
                }
            }
        }catch (Throwable throwable){
            PTLog.error("Error parsing FCM payload",throwable);
        }
    }
}
