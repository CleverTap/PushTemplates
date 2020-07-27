[ ![Download](https://api.bintray.com/packages/clevertap/Maven/PushTemplates/images/download.svg) ](https://bintray.com/clevertap/Maven/PushTemplates/_latestVersion)
# Push Templates by CleverTap

Push Templates SDK helps you engage with your users using fancy push notification templates built specifically to work with [CleverTap](https://www.clevertap.com).

## NOTE
This library is in public beta, for any issues, queries and concerns please open a new issue [here](https://github.com/CleverTap/PushTemplates/issues)

# Table of contents

- [Installation](#installation)
- [Dashboard Usage](#dashboard-usage)
- [Template Types](#template-types)
- [Template Keys](#template-keys)
- [Sample App](#sample-app)
- [Contributing](#contributing)
- [License](#license)

# Installation

[(Back to top)](#table-of-contents)

### Out of the box

1. Add the dependencies to the `build.gradle`

```groovy
implementation 'com.clevertap.android:push-templates:0.0.2'
implementation 'com.clevertap.android:clevertap-android-sdk:3.8.0'
implementation 'com.github.bumptech.glide:glide:4.11.0'
```

2. Add the Service to your `AndroidManifest.xml`

```xml
<service
    android:name="com.clevertap.pushtemplates.PushTemplateMessagingService">
    <intent-filter>
        <action android:name="com.google.firebase.MESSAGING_EVENT"/>
    </intent-filter>
</service>
```

3. Add the Receiver to your `AndroidManifest.xml`

```xml
<receiver
    android:name="com.clevertap.pushtemplates.PushTemplateReceiver"
    android:exported="false"
    android:enabled="true">
</receiver>
```

### Custom Handling Push Notifications

1. Add the dependencies to the `build.gradle`

```groovy
implementation 'com.clevertap.android:push-templates:0.0.2'
implementation 'com.clevertap.android:clevertap-android-sdk:3.8.0'
implementation 'com.github.bumptech.glide:glide:4.11.0'
```

2. Add the Receiver to your `AndroidManifest.xml`

```xml
<receiver
    android:name="com.clevertap.pushtemplates.PushTemplateReceiver"
    android:exported="false"
    android:enabled="true">
</receiver>
```


3. Add the following code in your custom FirebaseMessageService class

```java
public class MyMessagingService extends FirebaseMessagingService {

    Context context;
    CleverTapAPI instance;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        try{
            context = getApplicationContext();
            if (remoteMessage.getData().size() > 0) {
                Bundle extras = new Bundle();
                for (Map.Entry<String, String> entry : remoteMessage.getData().entrySet()) {
                    extras.putString(entry.getKey(), entry.getValue());
                }

                instance = CleverTapAPI.getDefaultInstance(getApplicationContext());

                boolean processCleverTapPN = Utils.isPNFromCleverTap(extras);

                if(processCleverTapPN){
                    String pt_id = extras.getString(Constants.PT_ID);
                    if(pt_id == null || pt_id.isEmpty()){
                        CleverTapAPI.createNotification(context,extras);
                    }else{
                        TemplateRenderer.setDebugLevel(2); //-1 for OFF, 0, for INFO, 2 for DEBUG, 3 for VERBOSE (errors)
                        TemplateRenderer.createNotification(context,extras);
                    }
                }else{
                    //Other providers
                }
            }
        }catch (Throwable throwable){
            PTLog.verbose("Error parsing FCM payload",throwable);
        }
    }
}
```
# Dashboard Usage

[(Back to top)](#table-of-contents)

While creating a Push Notification campaign on CleverTap, just follow the steps below -

1. On the "WHAT" section pass any values in the "title" and "message" fields (NOTE: These will be ignored)

![Basic](https://github.com/darshanclevertap/PushTemplates/blob/readme-images/screens/basic.png)

2. Click on "Advanced" and then click on "Add pair" to add the [Template Keys](#template-keys)

![KVs](https://github.com/darshanclevertap/PushTemplates/blob/readme-images/screens/kv.png)

3. You can also add the above keys into one JSON object and use the `pt_json` key to fill in the values

![KVs in JSON](https://github.com/darshanclevertap/PushTemplates/blob/readme-images/screens/json.png)

4. Send a test push and schedule!


# Template Types

[(Back to top)](#table-of-contents)

## Basic Template

Basic Template is the basic push notification received on apps.
<br/>(Expanded and unexpanded example)<br/><br/>
![Basic with color](https://github.com/darshanclevertap/PushTemplates/blob/readme-images/screens/basic%20color.png)


## Auto Carousel Template

Auto carousel is an automatic revolving carousel push notification.
<br/>(Expanded and unexpanded example)<br/><br/>
![Auto Carousel](https://github.com/darshanclevertap/PushTemplates/blob/readme-images/screens/autocarousel.gif)

## Rating Template

Rating template lets your users give you feedback, this feedback is captures as an event in CleverTap with the rating as the event property so that it can later be actionable.<br/>(Expanded and unexpanded example)<br/>

![Rating](https://github.com/darshanclevertap/PushTemplates/blob/readme-images/screens/rating.gif)

## Product Catalog Template

Product catalog template lets you show case different images of a product (or a product catalog) before the user can decide to click on the "BUY NOW" option which can take them directly to the product via deep links. 
<br/>(Expanded and unexpanded example)

![Product Display](https://github.com/darshanclevertap/PushTemplates/blob/readme-images/screens/productdisplay.gif)


## Five Icons Template

Five icons template is a sticky push notification with no text, just 5 icons and a close button which can help your users go directly to the functionality of their choice with a button's click.

<img src="https://raw.githubusercontent.com/darshanclevertap/PushTemplates/readme-images/screens/fiveicon.png" width="412" height="100">

# Template Keys

[(Back to top)](#table-of-contents)

## Basic Template

 Basic Template Keys | Description 
 :---:|:---:|:---:| 
 pt_id | Required | Value - `pt_basic`
 pt_title | Required | Title 
 pt_msg* | Required | Message 
 pt_msg_summary* | Required | Message line when Notification is expanded
 pt_subtitle | Optional  | Subtitle
 pt_bg* | Required | Background Color in HEX
 pt_big_img | Optional | Image
 pt_ico | Optional | Large Icon 
 pt_dl1 | Optional | One Deep Link (minimum)
 pt_title_clr | Optional | Title Color in HEX
 pt_msg_clr | Optional | Message Color in HEX
 pt_small_icon_clr | Optional | Small Icon Color in HEX
 pt_json | Optional | Above keys in JSON format
 
## Auto Carousel Template
 
 Auto Carousel Template Keys | Description 
  :---:|:---:| 
  pt_id* | Value - `pt_carousel`
  pt_title* | Title 
  pt_msg* | Message
  pt_msg_summary | Message line when Notification is expanded
  pt_subtitle | Subtitle
  pt_dl1* | Deep Link (Max one) 
  pt_img1* | Image One
  pt_img2* | Image Two
  pt_img3* | Image Three
  pt_bg* | Background Color in HEX
  pt_ico | Large Icon
  pt_title_clr | Title Color in HEX
  pt_msg_clr | Message Color in HEX
  pt_small_icon_clr | Small Icon Color in HEX
  pt_json | Above keys in JSON format
  
## Manual Carousel Template
 
 Manual Carousel Template Keys | Description 
  :---:|:---:| 
  pt_id* | Value - `pt_manual_carousel`
  pt_title* | Title 
  pt_msg* | Message
  pt_msg_summary | Message line when Notification is expanded
  pt_subtitle | Subtitle
  pt_dl1* | Deep Link One
  pt_dl2 | Deep Link Two
  pt_dl3 | Deep Link Three 
  pt_img1* | Image One
  pt_img2* | Image Two
  pt_img3* | Image Three
  pt_bg* | Background Color in HEX
  pt_ico | Large Icon
  pt_title_clr | Title Color in HEX
  pt_msg_clr | Message Color in HEX
  pt_small_icon_clr | Small Icon Color in HEX
  pt_json | Above keys in JSON format
  
## Rating Template

 Rating Template Keys | Description 
 :---:|:---:| 
 pt_id* | Value - `pt_rating`
 pt_title* | Title 
 pt_msg* | Message 
 pt_msg_summary | Message line when Notification is expanded
 pt_subtitle | Subtitle
 pt_default_dl* | Default Deep Link for Push Notification
 pt_dl1* | Deep Link for first/all star(s)
 pt_dl2 | Deep Link for second star
 pt_dl3 | Deep Link for third star
 pt_dl4 | Deep Link for fourth star
 pt_dl5 | Deep Link for fifth star
 pt_bg* | Background Color in HEX
 pt_ico | Large Icon
 pt_title_clr | Title Color in HEX
 pt_msg_clr | Message Color in HEX
 pt_small_icon_clr | Small Icon Color in HEX
 pt_json | Above keys in JSON format
 
## Product Catalog Template

 Product Catalog Template Keys | Description 
 :---:|:---:| 
 pt_id* | Value - `pt_product_display`
 pt_title* | Title 
 pt_msg* | Message
 pt_subtitle | Subtitle
 pt_img1* | Image One
 pt_img2* | Image Two
 pt_img3* | Image Three
 pt_bt1* | Big text for first image
 pt_bt2* | Big text for second image
 pt_bt3* | Big text for third image
 pt_st1* | Small text for first image
 pt_st2* | Small text for second image
 pt_st3* | Small text for third image
 pt_dl1* | Deep Link for first image
 pt_dl2* | Deep Link for second image
 pt_dl3* | Deep Link for third image
 pt_bg* | Background Color in HEX
 pt_product_display_action* | Action Button Label Text
 pt_product_display_linear | Linear Layout Template ("true"/"false")
 pt_product_display_action_clr* | Action Button Background Color in HEX
 pt_title_clr | Title Color in HEX
 pt_msg_clr | Message Color in HEX
 pt_small_icon_clr | Small Icon Color in HEX
 pt_json | Above keys in JSON format
 
## Five Icons Template

 Five Icons Template Keys | Description 
  :---:|:---:| 
  pt_id* | Value - `pt_five_icons`
  pt_img1* | Icon One
  pt_img2* | Icon Two
  pt_img3* | Icon Three
  pt_img4* | Icon Four
  pt_img5* | Icon Five
  pt_dl1* | Deep Link for first icon
  pt_dl2* | Deep Link for second icon
  pt_dl3* | Deep Link for third icon
  pt_dl4* | Deep Link for fourth icon
  pt_dl5* | Deep Link for fifth icon
  pt_bg* | Background Color in HEX
  pt_small_icon_clr | Small Icon Color in HEX
  pt_json | Above keys in JSON format
  
## Timer Template
 
 Timer Template Keys | Description 
  :---:|:---:| 
  pt_id* | Value - `pt_timer`
  pt_title* | Title 
  pt_title_alt | Title to show after timer expires 
  pt_msg* | Message
  pt_msg_alt | Message to show after timer expires
  pt_msg_summary | Message line when Notification is expanded
  pt_subtitle | Subtitle
  pt_dl1 | Deep Link
  pt_big_img | Image
  pt_big_img_alt | Image to show when timer expires
  pt_bg* | Background Color in HEX
  pt_timer_threshold* | Timer duration in seconds (minimum 10)
  pt_timer_end* | Epoch Timestamp to countdown to (for example, $D_1595871380 or 1595871380). Not needed if pt_timer_threshold is specified. 
  pt_title_clr | Title Color in HEX
  pt_msg_clr | Message Color in HEX
  pt_small_icon_clr | Small Icon Color in HEX
  pt_json | Above keys in JSON format
  
## Videp Template
 
 Video Template Keys | Description 
  :---:|:---:| 
  pt_id* | Value - `pt_video`
  pt_title* | Title 
  pt_msg* | Message
  pt_msg_summary | Message line when Notification is expanded
  pt_subtitle | Subtitle
  pt_big_img* | Image
  pt_video_url* | Video URL (https only)
  pt_bg* | Background Color in HEX
  pt_dl1 | Deep Link
  pt_title_clr | Title Color in HEX
  pt_msg_clr | Message Color in HEX
  pt_small_icon_clr | Small Icon Color in HEX
  pt_ico | Large Icon
  pt_json | Above keys in JSON format
  
## Zero Bezel Template
 
 Zero Bezel Template Keys | Description 
  :---:|:---:| 
  pt_id* | Value - `pt_zero_bezel`
  pt_title* | Title 
  pt_msg* | Message
  pt_msg_summary | Message line when Notification is expanded
  pt_subtitle | Subtitle
  pt_big_img* | Image
  pt_small_view | Select text-only small view layout (`text_only`)
  pt_dl1 | Deep Link
  pt_title_clr | Title Color in HEX
  pt_msg_clr | Message Color in HEX
  pt_small_icon_clr | Small Icon Color in HEX
  pt_ico | Large Icon
  pt_json | Above keys in JSON format
  
## Input Box Template
 
 Input Box Template Keys | Description 
  :---:|:---:| 
  pt_id* | Value - `pt_input`
  pt_title* | Title 
  pt_msg* | Message
  pt_msg_summary | Message line when Notification is expanded
  pt_subtitle | Subtitle
  pt_big_img* | Image
  pt_big_img_alt | Image to be shown after feedback is collected
  wzrk_acts | Action Buttons; Sample `[{'l':'Yes','id':'2'},{'l':'No','id':'1'},{'l':'Remind Later','id':'remind'}]`
  pt_event_name | Name of Event to be raised
  pt_event_property_p1 | Value for event property p1  
  pt_event_property_p2 | Value for event property p2
  pt_input_label | Label text to be shown on the input
  pt_input_auto_open | Auto open the app after feedback
  pt_input_feedback | Feedback 
  pt_dl1 | Deep Link
  pt_title_clr | Title Color in HEX
  pt_msg_clr | Message Color in HEX
  pt_small_icon_clr | Small Icon Color in HEX
  pt_ico | Large Icon
  pt_dismiss_on_click | Dismiss notification on click
  pt_json | Above keys in JSON format
  
## Cancel Notifications 
 
 Cancel Notification Keys | Description 
  :---:|:---:|:---:| 
  pt_id | Required | Value - `pt_cancel`
  
  
  ### NOTE
  * (*) - Mandatory
  * `pt_title` and `pt_msg` in all the templates support HTML elements like bold `<b>`, italics `<i>` and underline `<u>`

# Sample App

[(Back to top)](#table-of-contents)

Check out the [Sample app](app) 

# Contributing

[(Back to top)](#table-of-contents)

Your contributions are always welcome! Please have a look at the [contribution guidelines](CONTRIBUTING.md) first. :tada:

# License

[(Back to top)](#table-of-contents)


The MIT License (MIT) 2020. Please have a look at the [LICENSE.md](LICENSE.md) for more details.
