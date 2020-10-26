package com.clevertap.templateapp;

import android.app.NotificationManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.clevertap.android.sdk.CleverTapAPI;
import com.clevertap.pushtemplates.TemplateRenderer;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

public class MainActivity extends AppCompatActivity {

    Button sendBasicPush, sendCarouselPush,sendManualCarouselPush,
            sendRatingPush, sendProductDisplayNotification,
            sendLinearProductDisplayNotification,
            sendCTANotification, sendZeroBezel, sendTimerNotification,
            sendInputBoxNotification,sendVideoNotification;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TemplateRenderer.setDebugLevel(3);
        final CleverTapAPI cleverTapAPI = CleverTapAPI.getDefaultInstance(getApplicationContext());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            CleverTapAPI.createNotificationChannel(this,"Test","Push Template App Channel","Channel for Push Template App", NotificationManager.IMPORTANCE_HIGH,true);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            CleverTapAPI.createNotificationChannel(this,"PTTesting","Push Template App Channel","Channel for Push Template App", NotificationManager.IMPORTANCE_HIGH,true);
        }

        sendBasicPush = findViewById(R.id.basicPush);
        sendBasicPush.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cleverTapAPI != null) {
                    //cleverTapAPI.pushEvent("Send Basic Push");
                    String payload = "{\n" +
                            "        \"nm\": \"<Notification Body>\",\n" +
                            "        \"nt\": \"<Notification Title>\",\n" +
                            "        \"wzrk_id\": \"1584453563_20200606\",\n" +
                            "                \"wzrk_ck\": \"type_a\",\n" +
                            "        \"wzrk_pn\": true,\n" +
                            "        \"wzrk_rnv\": true,\n" +
                            "        \"wzrk_cid\": \"PTTesting\",\n" +
                            "        \"wzrk_ttl\": \"1593116116\",\n" +
                            "        \"wzrk_dt\": \"FIREBASE\",\n" +
                            "        \"pt_id\": \"pt_basic\",\n" +
                            "        \"pt_json\": {\n" +
                            "            \"pt_title\": \"Ende is c\", \n" +
                            "            \"pt_msg\": \"Get 50-80% off | 19-22 June \",\n" +
                            "            \"pt_msg_summary\": \"Get 50-80% off | <b style='color:green'>19-22 June</b> \",\n" +
                            "            \"pt_subtitle\": \"<b>test</b>\",\n" +
                            "            \"pt_big_im\": \"https://img.freepik.com/free-vector/abstract-dark-sales-background-concept_23-2148408570.jpg?size=626&ext=jpg\",\n" +
                            "            \"pt_dl1\": \"https://www.myntra.com/\",\n" +
                            "            \"pt_bg\": \"white\",\n" +
                            "            \"pt_title_clr\": \"black\",\n" +
                            "            \"pt_msg_clr\": \"black\",\n" +
                            "            \"wzrk_clr\":\"lightgrey\",\n" +
                            "            \"pt_small_icon_clr\": \"lightgrey\",\n" +
                            "            \"pt_ico\": \"https://png.pngtree.com/element_our/png_detail/20181008/b-logo-isolated-on-black-background-png_130986.jpg\",\n" +
                            "            \"pt_custom_font_url\": \"https://challenges.thefittestleague.in/Pangolin-Regular.ttf\",\n" +
                            "            \"pt_custom_cta1\":\"{'pt_custom_cta_bg_clr' : 'red','pt_custom_cta_text_clr' : 'white','pt_custom_cta_text' : 'works','pt_custom_cta_dl' : 'gmail.com'}\",\n" +
                            "            \"pt_custom_cta2\":\"{'pt_custom_cta_bg_clr' : 'black','pt_custom_cta_text_clr' : 'yellow','pt_custom_cta_text' : 'works2','pt_custom_cta_dl' : 'gmail.com'}\",\n" +
                            "            \"pt_custom_cta3\":\"{'pt_custom_cta_bg_clr' : 'blue','pt_custom_cta_text_clr' : 'white','pt_custom_cta_text' : 'works3','pt_custom_cta_dl' : 'gmail.com'}\"\n" +
                            "        }\n" +
                            "    }";

                    Bundle extras = jsonStringToBundle(payload);
                    TemplateRenderer.createNotification(getApplicationContext(), extras);
                }
            }
        });

        sendCarouselPush = findViewById(R.id.carouselPush);
        sendCarouselPush.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cleverTapAPI != null) {
                    cleverTapAPI.pushEvent("Send Auto Carousel Push");
                }
            }
        });

        sendCarouselPush = findViewById(R.id.manualCarouselPush);
        sendCarouselPush.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cleverTapAPI != null) {
//                    cleverTapAPI.pushEvent("Send Manual Carousel Push");
                    String payload = " {\n" +
                            "        \"nm\": \"<Notification Body>\",\n" +
                            "        \"nt\": \"<Notification Title>\",\n" +
                            "        \"wzrk_id\": \"1584453563_20200606\",\n" +
                            "        \"wzrk_pn\": true,\n" +
                            "        \"wzrk_rnv\": true,\n" +
                            "        \"wzrk_cid\": \"PTTesting\",\n" +
                            "        \"wzrk_ttl\": \"1593116016\",\n" +
                            "        \"wzrk_dt\": \"FIREBASE\",\n" +
                            "        \"pt_id\": \"pt_manual_carousel\",\n" +
                            "        \"pt_json\": {\n" +
                            "            \"wzrk_cid\": \"PTTesting\",\n" +
                            "            \"pt_id\": \"pt_manual_carousel\",\n" +
                            "            \"pt_msg\": \"Price $10\",\n" +
                            "            \"pt_msg_summary\": \"Get all these just for $10 each.\",\n" +
                            "            \"pt_title\": \"Slingbag for Women\",\n" +
                            "            \"pt_msg_clr\": \"#FF0000\",\n" +
                            "            \"pt_title_clr\": \"#00FF00\",\n" +
                            "            \"pt_bg\": \"#0000FF\",\n" +
                            "            \"pt_img1\": \"https://images-na.ssl-images-amazon.com/images/I/61hKkRXXXxL._UL1200_.jpg\",\n" +
                            "            \"pt_img2\": \"https://images-na.ssl-images-amazon.com/images/I/61uk4oR0ogL._UL1400_.jpg\",\n" +
                            "            \"pt_img3\": \"https://images-na.ssl-images-amazon.com/images/I/61T9lWYtVzL._UL1400_.jpg\",\n" +
                            "            \"pt_img4\": \"https://challenges.thefittestleague.in/wp-content/uploads/2020/06/iconfinder_money_1055022.png\",\n" +
                            "            \"pt_img5\": \"https://icon2.cleanpng.com/20180522/bga/kisspng-elon-musk-tesla-motors-investor-the-boring-company-5b03a350b15c73.9267333715269650727265.jpg\",\n" +
                            "            \"pt_img6\": \"https://www.vhv.rs/dpng/d/24-241018_elon-musk-meme-review-hd-png-download.png\",\n" +
                            "            \"pt_img7\": \"https://i.dlpng.com/static/png/6543004_preview.png\",\n" +
                            "            \"pt_img8\": \"https://www.vhv.rs/dpng/d/463-4633987_elon-musk-png-free-pic-transparent.png\",\n" +
                            "            \"pt_small_img\": \"https://cdn.dribbble.com/users/619658/screenshots/2374888/untitled-1.jpg\",\n" +
                            "            \"pt_dl1\": \"https://google.com\",\n" +
                            "            \"pt_dl2\": \"https://amazon.com\",\n" +
                            "            \"pt_dl3\": \"https://flipkart.com\",\n" +
                            "            \"pt_dl4\": \"https://google.com\",\n" +
                            "            \"pt_dl5\": \"https://amazon.com\",\n" +
                            "            \"pt_dl6\": \"https://flipkart.com\",\n" +
                            "            \"pt_dl7\": \"https://google.com\",\n" +
                            "            \"pt_dl8\": \"https://amazon.com\",\n" +
                            "                        \"wzrk_clr\": \"#FFFF00\",\n" +
                            "                           \"pt_ico\": \"https://www.searchpng.com/wp-content/uploads/2019/01/Myntra-logo-png-icon.png\"\n" +
                            "\n" +
                            "        }\n" +
                            "    }";

                    Bundle extras = jsonStringToBundle(payload);
                    TemplateRenderer.createNotification(getApplicationContext(), extras);
                }
            }
        });


        sendRatingPush = findViewById(R.id.ratingPush);
        sendRatingPush.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cleverTapAPI != null) {
                    cleverTapAPI.pushEvent("Send Rating Push");
                }
            }
        });

        sendProductDisplayNotification = findViewById(R.id.productDisplay);
        sendProductDisplayNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cleverTapAPI != null) {
//                    cleverTapAPI.pushEvent("Send Product Display Notification");
                    String payload = "{\n" +
                            "        \"nm\": \"<Notification Body>\",\n" +
                            "        \"nt\": \"<Notification Title>\",\n" +
                            "        \"wzrk_id\": \"1584453563_20200606\",\n" +
                            "        \"wzrk_pn\": true,\n" +
                            "        \"wzrk_rnv\": true,\n" +
                            "        \"wzrk_cid\": \"PTTesting\",\n" +
                            "        \"wzrk_ttl\": \"1593116016\",\n" +
                            "        \"wzrk_dt\": \"FIREBASE\",\n" +
                            "        \"pt_id\": \"pt_product_display\",\n" +
                            "        \"pt_json\": {\n" +
                            "            \"wzrk_cid\": \"PTTesting\",\n" +
                            "            \"pt_id\": \"pt_product_display\",\n" +
                            "            \"pt_msg\": \"Range for the entire family\",\n" +
                            "            \"pt_title\": \"Shoeniverse Shoes\",\n" +
                            "            \"pt_img1\": \"https://images-na.ssl-images-amazon.com/images/I/61N--EbFDZL._UL1000_.jpg\",\n" +
                            "            \"pt_img2\": \"https://images-na.ssl-images-amazon.com/images/I/81f1lvAwdqL._UL1500_.jpg\",\n" +
                            "            \"pt_img3\": \"https://images-na.ssl-images-amazon.com/images/I/71ecw31vOLL._UL1200_.jpg\",\n" +
                            "            \"pt_small_img\": \"https://img.icons8.com/bubbles/2x/car.png\",\n" +
                            "            \"pt_bt1\": \"Shoeniverse Men's Shoes\",\n" +
                            "            \"pt_bt2\": \"Shoeniverse Women's Shoes\",\n" +
                            "            \"pt_bt3\": \"Shoeniverse Kids's Shoes\",\n" +
                            "            \"pt_st1\": \"Buy now to enter luckydraw.\",\n" +
                            "            \"pt_st2\": \"Cheapest option.\",\n" +
                            "            \"pt_st3\": \"Amazing shoes that offer maximum comfort.\",\n" +
                            "            \"pt_price1\": \"Rs. 5000\",\n" +
                            "            \"pt_price2\": \"Rs. 2000\",\n" +
                            "            \"pt_price3\": \"Rs. 7000\",\n" +
                            "            \"pt_dl1\": \"https:///www.google.com\",\n" +
                            "            \"pt_dl2\": \"https:///www.microsoft.com\",\n" +
                            "            \"pt_dl3\": \"https:///www.clevertap.com\",\n" +
                            "            \"pt_product_display_action\":\"CLick Me\",\n" +
                            "            \"pt_product_display_action_clr\":\"#FF6600\",\n" +
                            "                        \"pt_small_icon_clr\": \"#FF0000\",\n" +
                            "            \"pt_bg\":\"#ffffff\",\n" +
                            "            \"pt_ico\":\"https://i.pinimg.com/originals/49/3e/de/493ede620ab04894295105635d73f77d.png\"\n" +
                            "        }\n" +
                            "    }";


                    Bundle extras = jsonStringToBundle(payload);
                    TemplateRenderer.createNotification(getApplicationContext(), extras);
                }
            }
        });

        sendLinearProductDisplayNotification = findViewById(R.id.linearProductDisplay);
        sendLinearProductDisplayNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cleverTapAPI != null) {
//                    cleverTapAPI.pushEvent("Send Linear Product Display Push");
                    String payload = "{\n" +
                            "        \"nm\": \"<Notification Body>\",\n" +
                            "        \"nt\": \"<Notification Title>\",\n" +
                            "        \"wzrk_id\": \"1584453563_20200606\",\n" +
                            "        \"wzrk_pn\": true,\n" +
                            "        \"wzrk_rnv\": true,\n" +
                            "        \"wzrk_cid\": \"PTTesting\",\n" +
                            "        \"wzrk_ttl\": \"1593116016\",\n" +
                            "        \"wzrk_dt\": \"FIREBASE\",\n" +
                            "        \"pt_id\": \"pt_product_display\",\n" +
                            "        \"pt_json\": {\n" +
                            "            \"wzrk_cid\": \"PTTesting\",\n" +
                            "                    \"wzrk_ck\": \"type_a\",\n" +
                            "\n" +
                            "            \"pt_id\": \"pt_product_display\",\n" +
                            "            \"pt_msg\": \"Range for the entire family\",\n" +
                            "            \"pt_title\": \"Glasses for all ocaasions!\",\n" +
                            "            \"pt_img1\": \"https://static1lenskart.com/media/desktop/img/14-June-20/eyeicon.jpg\",\n" +
                            "            \"pt_img2\": \"https://static1.lenskart.com/media/desktop/img/14-June-20/sunicon.jpg\",\n" +
                            "            \"pt_img3\": \"https://static1.lenskart.com/media/desktop/img/14-June-20/599icon.jpg\",\n" +
                            "            \"pt_bt1\": \"Eyeglasses\",\n" +
                            "            \"pt_bt2\": \"Sunglasses\",\n" +
                            "            \"pt_bt3\": \"Stylish Glasses\",\n" +
                            "            \"pt_st1\": \"Buy now to enter luckydraw.\",\n" +
                            "            \"pt_st2\": \"Cheapest option.\",\n" +
                            "            \"pt_st3\": \"Amazing glasses that offer maximum comfort.\",\n" +
                            "            \"pt_price1\": \"Rs. 5000\",\n" +
                            "            \"pt_price2\": \"Rs. 2000\",\n" +
                            "            \"pt_price3\": \"Rs. 7000\",\n" +
                            "            \"pt_dl1\": \"https:///www.google.com\",\n" +
                            "            \"pt_dl2\": \"https:///www.microsoft.com\",\n" +
                            "            \"pt_dl3\": \"https:///www.clevertap.com\",\n" +
                            "            \"pt_bg\": \"white\",\n" +
                            "            \"pt_product_display_action\": \"Buy\",\n" +
                            "            \"pt_product_display_action_clr\": \"#0F00F0\",\n" +
                            "            \"pt_title_clr\": \"#FFFFFF\",\n" +
                            "            \"pt_msg_clr\": \"#FFFFFF\",\n" +
                            "            \"wzrk_clr\": \"darkgrey\",\n" +
                            "\"pt_product_display_linear\":true,\n" +

                            "            \"pt_ico\": \"https://www.designmantic.com/logo-images/3439.png?company=Company+Name&slogan=&verify=1\"\n" +
                            "        }\n" +
                            "    }";

                    Bundle extras = jsonStringToBundle(payload);
                    TemplateRenderer.createNotification(getApplicationContext(), extras);
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
                if (cleverTapAPI != null) {
                    cleverTapAPI.pushEvent("Send Zero Bezel Notification");
                }
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

    public static Bundle jsonStringToBundle(String jsonString) {
        try {
            JSONObject jsonObject = toJsonObject(jsonString);
            return jsonToBundle(jsonObject);
        } catch (JSONException ignored) {

        }
        return null;
    }

    public static JSONObject toJsonObject(String jsonString) throws JSONException {
        return new JSONObject(jsonString);
    }

    public static Bundle jsonToBundle(JSONObject jsonObject) throws JSONException {
        Bundle bundle = new Bundle();
        Iterator iter = jsonObject.keys();
        while (iter.hasNext()) {
            String key = (String) iter.next();
            String value = jsonObject.getString(key);
            bundle.putString(key, value);
        }
        return bundle;
    }
}