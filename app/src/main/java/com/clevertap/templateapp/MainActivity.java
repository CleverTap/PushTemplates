package com.clevertap.templateapp;

import android.app.NotificationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.clevertap.android.sdk.CleverTapAPI;
import com.clevertap.android.sdk.PushType;
import com.clevertap.pushtemplates.TemplateRenderer;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    Button sendBasicPush, sendCarouselPush, sendManualCarouselPush,sendRatingPush, sendProductDisplayNotification,
            sendCTANotification, sendZeroBezel, sendTimerNotification,sendInputBoxNotification,sendVideoNotification;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TemplateRenderer.setDebugLevel(3);
        final CleverTapAPI cleverTapAPI = CleverTapAPI.getDefaultInstance(getApplicationContext());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            CleverTapAPI.createNotificationChannel(this,"PTTesting","Push Template App Channel","Channel for Push Template App", NotificationManager.IMPORTANCE_HIGH,true);
        }
        HashMap<String, Object> profileUpdate = new HashMap<String, Object>();
        profileUpdate.put("Email", "test1@clevertap.com");
        if (cleverTapAPI != null) {
            cleverTapAPI.onUserLogin(profileUpdate);

        }

        RequestQueue queue = Volley.newRequestQueue(this);
        String fcmEndpoint = "https://fcm.googleapis.com/fcm/send";

        String token = cleverTapAPI.getDevicePushToken(PushType.FCM);

        //setting Basic payload
        JSONObject data = new JSONObject();
        try {
            data.put("nt","NM");
            data.put("nm","NT");
            data.put("wzrk_id","1584453563_20200606");
            data.put("wzrk_pn",true);
            data.put("wzrk_rnv",true);
            data.put("wzrk_cid","PTTesting");
            data.put("wzrk_dt","FIREBASE");
            data.put("pt_title","Welcome to Push Templates");
            data.put("pt_msg","This is the future");
            data.put("pt_msg_summary","This is where summary would come.");
            data.put("pt_big_img","https://i7.pngguru.com/preview/217/808/905/thor-printed-t-shirt-iron-man-avengers-png-transparent-image.jpg");
            data.put("pt_dl1","https://google.com");
            data.put("pt_msg_clr","#91B75F");
            data.put("pt_title_clr","#CD5748");
            data.put("pt_ico","https://i.pinimg.com/originals/49/3e/de/493ede620ab04894295105635d73f77d.png");
            data.put("pt_large_icon","https://i.pinimg.com/originals/49/3e/de/493ede620ab04894295105635d73f77d.png");
            data.put("pt_bg","#00ff00");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JSONObject payload = new JSONObject();

        sendBasicPush = findViewById(R.id.basicPush);
        sendBasicPush.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    data.put("wzrk_pid", System.currentTimeMillis()+"");
                    data.put("pt_id","pt_basic");

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                setPayload(payload, data, token);
                makeAPICall(fcmEndpoint, payload, queue);
            }
        });

        sendCarouselPush = findViewById(R.id.carouselPush);
        sendCarouselPush.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    data.put("wzrk_pid", System.currentTimeMillis()+"");
                    data.put("pt_id","pt_carousel");
                    data.put("pt_img1","https://images-na.ssl-images-amazon.com/images/I/61hKkRXXXxL._UL1200_.jpg");
                    data.put("pt_img2", "https://images-na.ssl-images-amazon.com/images/I/61uk4oR0ogL._UL1400_.jpg");
                    data.put("pt_img3","https://images-na.ssl-images-amazon.com/images/I/61T9lWYtVzL._UL1400_.jpg");
                    data.put("pt_small_img", "https://cdn.dribbble.com/users/619658/screenshots/2374888/untitled-1.jpg");

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                setPayload(payload, data, token);
                makeAPICall(fcmEndpoint, payload, queue);
            }
        });

        sendManualCarouselPush = findViewById(R.id.manualCarouselPush);
        sendManualCarouselPush.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    data.put("wzrk_pid", System.currentTimeMillis()+"");
                    data.put("pt_id","pt_manual_carousel");
                    data.put("pt_img1","https://images-na.ssl-images-amazon.com/images/I/61hKkRXXXxL._UL1200_.jpg");
                    data.put("pt_img2", "https://images-na.ssl-images-amazon.com/images/I/61uk4oR0ogL._UL1400_.jpg");
                    data.put("pt_img3","https://images-na.ssl-images-amazon.com/images/I/61T9lWYtVzL._UL1400_.jpg");
                    data.put("pt_small_img", "https://cdn.dribbble.com/users/619658/screenshots/2374888/untitled-1.jpg");

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                setPayload(payload, data, token);
                makeAPICall(fcmEndpoint, payload, queue);
            }
        });

        sendRatingPush = findViewById(R.id.ratingPush);
        sendRatingPush.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    data.put("wzrk_pid", System.currentTimeMillis()+"");
                    data.put("pt_id","pt_rating");
                    data.put("pt_dl1","https://c7.alamy.com/comp/MW0C9F/thanks-for-rating-message-on-smartphone-screen-in-male-hand-customer-service-survey-feedback-concept-MW0C9F.jpg");
                    data.put("pt_default_dl", "https://c7.alamy.com/comp/MW0C9F/thanks-for-rating-message-on-smartphone-screen-in-male-hand-customer-service-survey-feedback-concept-MW0C9F.jpg");
                    data.put("pt_big_img","https://i.ndtvimg.com/i/2017-11/margherita-pizza_620x330_71510224804.jpg");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                setPayload(payload, data, token);
                makeAPICall(fcmEndpoint, payload, queue);
            }
        });

        sendProductDisplayNotification = findViewById(R.id.productDisplay);
        sendProductDisplayNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    data.put("wzrk_pid", System.currentTimeMillis()+"");
                    data.put("pt_id","pt_product_display");
                    data.put("pt_img1","https://images-na.ssl-images-amazon.com/images/I/61hKkRXXXxL._UL1200_.jpg");
                    data.put("pt_img2", "https://images-na.ssl-images-amazon.com/images/I/61uk4oR0ogL._UL1400_.jpg");
                    data.put("pt_img3","https://images-na.ssl-images-amazon.com/images/I/61T9lWYtVzL._UL1400_.jpg");
                    data.put("pt_small_img", "https://cdn.dribbble.com/users/619658/screenshots/2374888/untitled-1.jpg");
                    data.put("pt_bt1","Shoeniverse Men's Shoes");
                    data.put("pt_bt2", "Shoeniverse Women's Shoes");
                    data.put("pt_bt3","Shoeniverse Kids's Shoes");
                    data.put("pt_st1", "Buy now to enter luckydraw.");
                    data.put("pt_st2","Cheapest option.");
                    data.put("pt_st3", "Amazing shoes that offer maximum comfort.");
                    data.put("pt_price1","Rs. 5000");
                    data.put("pt_price2", "Rs. 2000");
                    data.put("pt_price3","Rs. 1000");
                    data.put("pt_dl1", "https:///www.google.com");
                    data.put("pt_dl2","https:///www.microsoft.com");
                    data.put("pt_dl3", "https:///www.clevertap.com");
                    data.put("pt_product_display_action","Buy Me");
                    data.put("pt_product_display_action_clr", "#FFF000");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                setPayload(payload, data, token);
                makeAPICall(fcmEndpoint, payload, queue);
            }
        });

        sendCTANotification = findViewById(R.id.cta);
        sendCTANotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    data.put("wzrk_pid", System.currentTimeMillis()+"");
                    data.put("pt_id","pt_product_display");
                    data.put("pt_img1","https://i.imgur.com/NLkFNmq.png");
                    data.put("pt_img2", "https://i.imgur.com/8Nu1Mqf.png");
                    data.put("pt_img3","https://i.imgur.com/ziNiG6P.png");
                    data.put("pt_img4", "https://i.imgur.com/A6eeBDt.png");
                    data.put("pt_img5","https://i.imgur.com/SCW1rov.png");
                    data.put("pt_img6", "https://static.thenounproject.com/png/3165065-200.png");
                    data.put("pt_dl1","https://www.google.com");
                    data.put("pt_dl2", "https://www.google.com");
                    data.put("pt_dl3","https://www.google.com");
                    data.put("pt_dl4", "https://www.google.com");
                    data.put("pt_dl5","https://www.google.com");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                setPayload(payload, data, token);
                makeAPICall(fcmEndpoint, payload, queue);
            }
        });

        sendZeroBezel = findViewById(R.id.zero_bezel);
        sendZeroBezel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    data.put("wzrk_pid", System.currentTimeMillis()+"");
                    data.put("pt_id","pt_zero_bezel");
                    data.put("pt_big_img","https://images.financialexpress.com/2020/05/660_4.jpg");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                setPayload(payload, data, token);
                makeAPICall(fcmEndpoint, payload, queue);
            }
        });

        sendTimerNotification = findViewById(R.id.timer);
        sendTimerNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    data.put("wzrk_pid", System.currentTimeMillis()+"");
                    data.put("pt_id","pt_zero_bezel");
                    data.put("pt_timer_threshold","20");
                    data.put("pt_chrono_title_clr","#000000");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                setPayload(payload, data, token);
                makeAPICall(fcmEndpoint, payload, queue);
            }
        });

        sendInputBoxNotification = findViewById(R.id.inputBox);
        sendInputBoxNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long currentTS = System.currentTimeMillis();

                try {
                    data.put("wzrk_pid", currentTS+"");
                    data.put("pt_id","pt_input");
                    data.put("pt_input_label","Search");
                    data.put("pt_input_feedback","Redirecting to App");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                int type = mod(currentTS,3);

                switch (type){
                    case 0:
                        //CTA ONLY CASE - CTAS AUTO CLOSE ON CLICK
                        try {
                            data.put("wzrk_acts", "[{'l':'Yes','id':'1'},{'l':'No','id':'1'}]");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        break;
                    case 1:
                        //CTA ONLY CASE - Reply and auto close
                        try {
                            data.put("pt_event_name", "Feedback Submitted");
                            data.put("pt_event_property_Feedback", "pt_input_reply");
                            data.put("pt_event_property_platform", "Android");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        break;
                    case 2:
                        //CTA ONLY CASE - Reply and auto close
                        try {
                            data.put("pt_event_name", "Feedback Submitted");
                            data.put("pt_event_property_Feedback", "pt_input_reply");
                            data.put("pt_event_property_platform", "Android");
                            data.put("pt_input_auto_open", true);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        break;
                }

                setPayload(payload, data, token);
                makeAPICall(fcmEndpoint, payload, queue);

            }
        });

        sendVideoNotification = findViewById(R.id.videobutton);
        sendVideoNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    data.put("wzrk_pid", System.currentTimeMillis()+"");
                    data.put("pt_id","pt_video");
                    data.put("pt_video_url","https://static.videezy.com/system/resources/previews/000/035/451/original/18_022_05.mp4");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                setPayload(payload, data, token);
                makeAPICall(fcmEndpoint, payload, queue);
            }
        });
    }

    private void setPayload(JSONObject payload, JSONObject data, String token) {
        try {
            payload.put("data",data);
            payload.put("to",token);
            payload.put("collapse_key","type_a");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void makeAPICall(String fcmEndpoint, JSONObject payload, RequestQueue queue) {
        JsonObjectRequest objectRequest = new JsonObjectRequest(
                Request.Method.POST,
                fcmEndpoint,
                payload,
                response -> Log.v("API Response: ",response.toString()),
                error -> Log.v("API Error: ",error.toString())
        )
        {
            /** Passing some request headers* */
            @Override
            public Map getHeaders() throws AuthFailureError {
                HashMap headers = new HashMap();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", "key=AIzaSyAEqimoUQM5dofLm5jTTzxXsQaLex2yo1Q");
                return headers;
            }
        };

        queue.add(objectRequest);

    }

    private int mod(long x, int y)
    {
        int result = (int) (y % x);
        if (result < 0)
            result += y;
        return result;
    }
}