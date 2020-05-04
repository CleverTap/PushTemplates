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
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    Button sendBasicPush, sendCarouselPush, sendRatingPush, sendProductDisplayNotification, sendCTANotification;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        CleverTapAPI.setDebugLevel(CleverTapAPI.LogLevel.DEBUG);
        final CleverTapAPI cleverTapAPI = CleverTapAPI.getDefaultInstance(getApplicationContext());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            CleverTapAPI.createNotificationChannel(this,"Test","Push Template App Channel","Channel for Push Template App", NotificationManager.IMPORTANCE_HIGH,true);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            CleverTapAPI.createNotificationChannel(this,"PTTesting","Push Template App Channel","Channel for Push Template App", NotificationManager.IMPORTANCE_HIGH,true);
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
                            "    \"pt_img1\": \"https://images-na.ssl-images-amazon.com/images/I/41G1Vh5M9WL.jpg\",\n" +
                            "    \"pt_img2\": \"https://images-na.ssl-images-amazon.com/images/I/71KPrMzPSdL._UL1500_.jpg\",\n" +
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
                TemplateRenderer.createNotification(getApplicationContext(),extras);


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
                    JSONObject payload = new JSONObject("{\"wzrk_cid\":\"PTTesting\",\"pt_id\":\"pt_rating\",\"pt_msg\":\"Please take a moment to share your feedback.\", \"pt_msg_summary\":\"Please share your rating below.\",\"pt_title\":\"Your feedback is important.\",\"pt_ico\":\"https://www.searchpng.com/wp-content/uploads/2019/03/Swiggy-PNG-Logo-1024x1024.png\",\"pt_dl1\":\"https:///www.google.com\",\"pt_dl1\":\"https:///www.google.com\", \"pt_big_img\":\"https://images-na.ssl-images-amazon.com/images/I/61N--EbFDZL._UL1000_.jpg\"}");
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
                TemplateRenderer.createNotification(getApplicationContext(),extras);

            }
        });

        sendProductDisplayNotification = findViewById(R.id.productDisplay);
        sendProductDisplayNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*if (cleverTapAPI != null) {
                    cleverTapAPI.pushEvent("Send Product Display Notification");
                }*/

                Bundle extras = new Bundle();

                try {
                    JSONObject payload = new JSONObject("{\"wzrk_cid\":\"PTTesting\",\"pt_id\":\"pt_product_display\",\"pt_msg\": \"Size:11 UK Colour:Sky Blue\",\n" +
                            "    \"pt_title\": \"Adidas MEN'S SPORT INSPIRED GLENN M SHOES\",\n" +
                            "    \"pt_img1\": \"https://images-na.ssl-images-amazon.com/images/I/61N--EbFDZL._UL1000_.jpg\",\n" +
                            "    \"pt_img2\": \"https://images-na.ssl-images-amazon.com/images/I/611fGGFxNTL._UL1000_.jpg\",\n" +
                            "    \"pt_img3\": \"https://images-na.ssl-images-amazon.com/images/I/51jOvMLFq2L._UL1000_.jpg\",\n" +
                            "    \"pt_small_img\": \"https://img.icons8.com/bubbles/2x/car.png\",\n" +
                            "    \"pt_bt1\": \"Adidas MEN'S SPORT INSPIRED GLENN M SHOES\",\n" +
                            "    \"pt_bt2\": \"Adidas Women's SHOES\",\n" +
                            "    \"pt_bt3\": \"Adidas kids' SHOES\",\n" +
                            "    \"pt_st1\": \"Buy now to enter luckydraw.\",\n" +
                            "    \"pt_st2\": \"Cheapest option.\",\n" +
                            "    \"pt_st3\": \"Amazing shoes that offer maximum comfort.\",\n" +
                            "    \"pt_price1\": \"Rs. 5000\",\n" +
                            "    \"pt_price2\": \"Rs. 2000\",\n" +
                            "    \"pt_price3\": \"Rs. 7000\",\n" +
                            "    \"pt_dl1\": \"https:///www.google.com\",\n" +
                            "    \"pt_dl2\": \"https:///www.microsoft.com\",\n" +
                            "    \"pt_dl3\": \"https:///www.clevertap.com\"}");
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
                TemplateRenderer.createNotification(getApplicationContext(),extras);

            }
        });

        sendCTANotification = findViewById(R.id.cta);
        sendCTANotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               /* if (cleverTapAPI != null) {
                    cleverTapAPI.pushEvent("Send CTA Notification");
                }*/
                Bundle extras = new Bundle();

                try {
                    JSONObject payload = new JSONObject("{\n" +
                            "\"pt_id\":\"4\",\n"+
                            "\"wzrk_cid\":\"PTTesting\",\n"+
                            "    \"pt_type\": \"five icons\",\n" +
                            "    \"pt_img1\": \"https://img.icons8.com/bubbles/2x/car.png\",\n" +
                            "    \"pt_img2\": \"https://static.thenounproject.com/png/195178-84.png\",\n" +
                            "    \"pt_img3\": \"https://static.thenounproject.com/png/195175-84.png\",\n" +
                            "    \"pt_img4\": \"https://static.thenounproject.com/png/195171-84.png\",\n" +
                            "    \"pt_img5\": \"https://static.thenounproject.com/png/195168-84.png\",\n" +
                            "    \"pt_img6\": \"https://static.thenounproject.com/png/3165065-200.png\",\n" +
                            "    \"pt_dl1\": \"https://www.google.com\",\n" +
                            "    \"pt_dl2\": \"https://www.google.com\",\n" +
                            "    \"pt_dl3\": \"https://www.google.com\",\n" +
                            "    \"pt_dl4\": \"https://www.google.com\",\n" +
                            "    \"pt_dl5\": \"https://www.google.com\"\n" +
                            "  }");
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
                TemplateRenderer.createNotification(getApplicationContext(),extras);
            }
        });

    }
}