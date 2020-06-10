package com.clevertap.templateapp;

import android.app.NotificationManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.clevertap.android.sdk.CleverTapAPI;
import com.clevertap.pushtemplates.TemplateRenderer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    Button sendBasicPush, sendCarouselPush, sendRatingPush, sendProductDisplayNotification,
            sendCTANotification, sendZeroBezel, sendTimerNotification, sendInputBoxNotification, sendVideoNotification;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TemplateRenderer.setDebugLevel(3);
        final CleverTapAPI cleverTapAPI = CleverTapAPI.getDefaultInstance(getApplicationContext());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            CleverTapAPI.createNotificationChannel(this, "Test", "Push Template App Channel", "Channel for Push Template App", NotificationManager.IMPORTANCE_HIGH, true);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            CleverTapAPI.createNotificationChannel(this, "PTTesting", "Push Template App Channel", "Channel for Push Template App", NotificationManager.IMPORTANCE_HIGH, true);
        }
        HashMap<String, Object> profileUpdate = new HashMap<String, Object>();
        profileUpdate.put("Email", "test1@clevertap.com");
        if (cleverTapAPI != null) {
            cleverTapAPI.onUserLogin(profileUpdate);
        }

        sendBasicPush = findViewById(R.id.basicPush);
        sendBasicPush.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cleverTapAPI != null) {
                    cleverTapAPI.pushEvent("Send Basic Push");
                }
            }
        });

        sendCarouselPush = findViewById(R.id.carouselPush);
        sendCarouselPush.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              /* if (cleverTapAPI != null) {
                    cleverTapAPI.pushEvent("Send Carousel Push");
                }*/

                Bundle extras = new Bundle();

                try {
                    JSONObject payload = new JSONObject("{\"wzrk_cid\":\"PTTesting\",\"pt_id\":\"pt_carousel\",\"pt_msg\": \"Price $10\",\"pt_msg_summary\": \"Get all these just for $10 each.\",\n" +
                            "    \"pt_title\": \"Scarf Decorated Slingbag\",\n" +
                            "    \"pt_msg_clr\": \"#000000\",\n" +
                            "    \"pt_title_clr\": \"#000000\",\n" +
                            "    \"pt_img1\": \"https://www.bmmagazine.co.uk/wp-content/uploads/2020/03/marvel-avengers-not-ended-disney-ceo-teases-mcu-fans.jpg\",\n" +
                            "    \"pt_img2\": \"https://cdn.pixabay.com/photo/2016/12/13/05/15/puppy-1903313_1280.jpg\",\n" +
                            "    \"pt_img3\": \"https://images-na.ssl-images-amazon.com/images/I/51jOvMLFq2L._UL1000_.jpg\",\n" +
                            "    \"pt_small_img\": \"https://cdn.dribbble.com/users/619658/screenshots/2374888/untitled-1.jpg\"," +
                            "    \"pt_dl1\": \"https://google.com\"}");
                    //JSONObject payload = new JSONObject("{\"wzrk_cid\":\"PTTesting\",\"pt_id\":\"3\",\"pt_msg\":\"Please share your rating below.\",\"pt_title\":\"Your feedback is important.\",\"pt_small_img\":\"https://d35fo82fjcw0y8.cloudfront.net/2019/12/02015204/Manan-Bajoria.png\",\"pt_dl1\":\"https:///www.google.com\"}");

                    JSONArray keys = payload.names();
                    for (int i = 0; i < keys.length(); ++i) {
                        String key = keys.getString(i);
                        String value = payload.getString(key);
                        extras.putString(key, value);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                TemplateRenderer.createNotification(getApplicationContext(), extras);

            }
        });

        sendRatingPush = findViewById(R.id.ratingPush);
        sendRatingPush.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               /*if (cleverTapAPI != null) {
                    cleverTapAPI.pushEvent("Send Rating Push");
                }*/
                Bundle extras = new Bundle();

                try {
                    JSONObject payload = new JSONObject("{\"wzrk_cid\":\"PTTesting\",\"pt_id\":\"pt_rating\",\"pt_msg\":\"Please take a moment to share your feedback.\", \"pt_msg_summary\":\"Please share your rating below.\",\"pt_title\":\"Your feedback is important.\",\"pt_ico\":\"https://www.searchpng.com/wp-content/uploads/2019/03/Swiggy-PNG-Logo-1024x1024.png\",\"pt_default_dl\":\"https://amazon.com\",\"pt_dl1\":\"https:///www.google.com\", \"pt_big_img\":\"https://images-na.ssl-images-amazon.com/images/I/61N--EbFDZL._UL1000_.jpg\"}");
                    //JSONObject payload = new JSONObject("{\"wzrk_cid\":\"PTTesting\",\"pt_id\":\"3\",\"pt_msg\":\"Please share your rating below.\",\"pt_title\":\"Your feedback is important.\",\"pt_small_img\":\"https://d35fo82fjcw0y8.cloudfront.net/2019/12/02015204/Manan-Bajoria.png\",\"pt_dl1\":\"https:///www.google.com\"}");

                    JSONArray keys = payload.names();
                    for (int i = 0; i < keys.length(); ++i) {
                        String key = keys.getString(i);
                        String value = payload.getString(key);
                        extras.putString(key, value);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                TemplateRenderer.createNotification(getApplicationContext(), extras);

            }
        });

        sendProductDisplayNotification = findViewById(R.id.productDisplay);
        sendProductDisplayNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cleverTapAPI != null) {
                    cleverTapAPI.pushEvent("Send Product Display Notification");
                }
            }
        });

        sendCTANotification = findViewById(R.id.cta);
        sendCTANotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cleverTapAPI != null) {
                    cleverTapAPI.pushEvent("Send CTA Notification");
                }
            }
        });

        sendZeroBezel = findViewById(R.id.zero_bezel);
        sendZeroBezel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*if (cleverTapAPI != null) {
                    cleverTapAPI.pushEvent("Send Rating Push");
                }*/
                Bundle extras = new Bundle();

                try {
                    JSONObject payload = new JSONObject("{\"wzrk_cid\":\"PTTesting\",\"pt_id\":\"pt_zero_bezel\",\"pt_small_view\":\"text_only\",\"pt_msg\":\"Now Streaming!\",\"pt_msg_summary\":\"Enjoy this action packed adventure from the comfort of your home!\",\"pt_title\":\"Catch The Avengers on the OTT app!\",\"pt_big_img\":\"https://www.bmmagazine.co.uk/wp-content/uploads/2020/03/marvel-avengers-not-ended-disney-ceo-teases-mcu-fans.jpg\",\"pt_ico\":\"https://i.pinimg.com/originals/49/3e/de/493ede620ab04894295105635d73f77d.png\",\"pt_dl1\":\"https://google.com\"}");

                    JSONArray keys = payload.names();
                    for (int i = 0; i < keys.length(); ++i) {
                        String key = keys.getString(i);
                        String value = payload.getString(key);
                        extras.putString(key, value);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                TemplateRenderer.createNotification(getApplicationContext(), extras);
            }
        });

        sendTimerNotification = findViewById(R.id.timer);
        sendTimerNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cleverTapAPI != null) {
                    cleverTapAPI.pushEvent("Send Timer Notification");
                }
            }
        });

        sendInputBoxNotification = findViewById(R.id.inputBox);
        sendInputBoxNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cleverTapAPI != null) {
                    cleverTapAPI.pushEvent("Send Input Box Notification");
                }
            }
        });
        sendVideoNotification = findViewById(R.id.videobutton);
        sendVideoNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cleverTapAPI != null) {
                    cleverTapAPI.pushEvent("Send Video Notification");
                }
            }
        });
    }
}