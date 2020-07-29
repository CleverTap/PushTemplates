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
implementation 'com.clevertap.android:push-templates:0.0.3'
implementation 'com.clevertap.android:clevertap-android-sdk:3.8.2'
implementation 'com.github.bumptech.glide:glide:4.11.0'
implementation 'com.google.android.exoplayer:exoplayer:2.10.2' // required only if you plan on using the Video Template
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
    android:name="com.clevertap.pushtemplates.PTPushNotificationReceiver"
    android:exported="false"
    android:enabled="true">
</receiver>

<receiver
    android:name="com.clevertap.pushtemplates.PushTemplateReceiver"
    android:exported="false"
    android:enabled="true">
</receiver>
```

### Custom Handling Push Notifications

1. Add the dependencies to the `build.gradle`

```groovy
implementation 'com.clevertap.android:push-templates:0.0.3'
implementation 'com.clevertap.android:clevertap-android-sdk:3.8.2'
implementation 'com.github.bumptech.glide:glide:4.11.0'
implementation 'com.google.android.exoplayer:exoplayer:2.10.2' // required only if you plan on using the Video Template
```

2. Add the Receiver to your `AndroidManifest.xml`

```xml
    <receiver
        android:name="com.clevertap.pushtemplates.PTPushNotificationReceiver"
        android:exported="false"
        android:enabled="true">
    </receiver>
    
    <receiver
        android:name="com.clevertap.pushtemplates.PushTemplateReceiver"
        android:exported="false"
        android:enabled="true">
    </receiver>
```


3. Add the following code in your custom FirebaseMessageService class

```java
public class PushTemplateMessagingService extends FirebaseMessagingService {

    Context context;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        try {
            PTLog.debug("Inside Push Templates");
            context = getApplicationContext();
            if (remoteMessage.getData().size() > 0) {
                Bundle extras = new Bundle();
                for (Map.Entry<String, String> entry : remoteMessage.getData().entrySet()) {
                    extras.putString(entry.getKey(), entry.getValue());
                }

                boolean processCleverTapPN = Utils.isPNFromCleverTap(extras);

                if (processCleverTapPN) {
                    if (Utils.isForPushTemplates(extras)) {
                        TemplateRenderer.createNotification(context, extras);
                    } else {
                        CleverTapAPI.createNotification(context, extras);
                    }
                }
            }
        } catch (Throwable throwable) {
            PTLog.verbose("Error parsing FCM payload", throwable);
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
![Basic with color](https://github.com/CleverTap/PushTemplates/blob/readme-images/screens/basic%20color.png)


## Auto Carousel Template

Auto carousel is an automatic revolving carousel push notification.
<br/>(Expanded and unexpanded example)<br/><br/>
![Auto Carousel](https://github.com/CleverTap/PushTemplates/blob/Readme_changes/screens/autocarouselv0.0.3.gif)


## Manual Carousel Template

This is the manual version of the carousel. The user can navigate to the next image by clicking on the arrows.
<br/>(Expanded and unexpanded example)<br/><br/>
![Auto Carousel](https://github.com/CleverTap/PushTemplates/blob/Readme_changes/screens/manual.gif)

If only one image can be downloaded, this template falls back to the Basic Template

## Rating Template

Rating template lets your users give you feedback, this feedback is captures as an event in CleverTap with the rating as the event property so that it can later be actionable.<br/>(Expanded and unexpanded example)<br/>

![Rating](https://github.com/CleverTap/PushTemplates/blob/Readme_changes/screens/rating.gif)

## Product Catalog Template

Product catalog template lets you show case different images of a product (or a product catalog) before the user can decide to click on the "BUY NOW" option which can take them directly to the product via deep links. This template has two variants. 

### Vertical View (Expanded and unexpanded example)

![Product Display](https://github.com/CleverTap/PushTemplates/blob/Readme_changes/screens/productdisplay.gif)

### Linear View

Use the following keys to enable linear view variant of this template.
Template Key | Required | Value
---:|:---:|:---
pt_product_display_linear | Required | `true`

![Product Display](https://github.com/CleverTap/PushTemplates/blob/Readme_changes/screens/productdisplay.gif)


## Five Icons Template

Five icons template is a sticky push notification with no text, just 5 icons and a close button which can help your users go directly to the functionality of their choice with a button's click.
<br/> If at least 3 icons are not retrieved, the library falls back to the Basic Template.

<img src="https://raw.githubusercontent.com/CleverTap/PushTemplates/Readme_changes/screens/fiveicon.png" width="412" height="100">

## Timer Template

This template features a live countdown timer. You can even choose to show different title, message, and background image after the timer expires.  
<br/> Timer notification is only supported for Android N (7) and above. For OS versions below N, the library falls back to the Basic Template.

![Timer](https://github.com/CleverTap/PushTemplates/blob/Readme_changes/screens/timer.gif)

## Video Template

The Video template plays a video when the user clicks on the notification. 
<br/> If your app does not include the Exo Player library, the library falls back to the Basic Template.

![Video](https://github.com/CleverTap/PushTemplates/blob/Readme_changes/screens/video.gif)

## Zero Bezel Template

The Zero Bezel template ensures that the background image covers the entire available surface area of the push notification. All the text is overlayed on the image.
<br/> The library will fallback to the Basic Template if the image can't be downloaded.

![Zero Bezel](https://github.com/CleverTap/PushTemplates/blob/Readme_changes/screens/zerobezel.gif)

## Input Box Template

The Input Box Template lets you collect any kind of input including feedback from your users. It has four variants.

### With CTAs

The CTA variant of the Input Box Template use action buttons on the notification to collect input from the user. 
<br/> To set the CTAs use the Advanced Options when setting up the campaign on the dashboard.

<img src="https://raw.githubusercontent.com/darshanclevertap/PushTemplates/Readme_changes/screens/fiveicon.png" width="412" height="100">

Template Key | Required | Value
---:|:---:|:---
pt_dismiss_on_click | Optional | Dismisses the notification without opening the app

### CTAs with Remind Later option

This variant of the Input Box Template is particularly useful if the user wants to be reminded of the notification after sometime. Clicking on the remind later button raises an event to the user profiles, with a custom user property p2 whose value is a future time stamp. You can have a campaign running on the dashboard that will send a reminded notification at the timestamp in the event property.
<br/> To set one of the CTAs as a Remind Later button set the action id to `remind` from the dashboard.

Template Key | Required | Value
---:|:---:|:---
pt_event_name | Required | for e.g. `Remind Later`,
pt_event_property_<property_name_1> | Optional | for e.g. `<property_value>`,
pt_event_property_<property_name_2> | Required | future epoch timestamp. For e.g., `$D_1592503813`
pt_dismiss_on_click | Optional | Dismisses the notification without opening the app

<img src="https://raw.githubusercontent.com/darshanclevertap/PushTemplates/Readme_changes/screens/fiveicon.png" width="412" height="100">

### Reply as an Event

This variant raises an event capturing the user's input as an event property. The app is not opened after the user sends the reply. 
<br/> To use this variant, use the following values for the keys.

Template Key | Required | Value
---:|:---:|:---
pt_input_label | Required | for e.g., `Search`
pt_input_feedback | Required | for e.g., `Thanks for your feedback`
pt_event_name | Required | for e.g. `Searched`,
pt_event_property_<property_name_1> | Optional | for e.g. `<property_value>`,
pt_event_property_<property_name_2> | Required to capture input | fixed value - `pt_input_reply`
            
<img src="https://raw.githubusercontent.com/darshanclevertap/PushTemplates/Readme_changes/screens/fiveicon.png" width="412" height="100">

### Reply as an Intent

This variant passes the reply to the app as an Intent. The app can then process the reply and take appropriate actions. 

<br/> To use this variant, use the following values for the keys.

Template Key | Required | Value
---:|:---:|:---
pt_input_label | Required | for e.g., `Search`
pt_input_feedback | Required | for e.g., `Thanks for your feedback`
pt_input_auto_open | Required | fixed value - `true`

<br/> To capture the input, the app can get the `pt_input_reply` key from the Intent extras.

<img src="https://raw.githubusercontent.com/darshanclevertap/PushTemplates/Readme_changes/screens/fiveicon.png" width="412" height="100">

## Cancel Notifications

This template removes all the currently displayed notifications associated with your app. This template can be used to prevent the user from seeing or clicking on a wrongly sent notification.

# Template Keys

[(Back to top)](#table-of-contents)

### Basic Template

 Basic Template Keys | Required | Description 
 ---:|:---:|:---| 
 pt_id | Required | Value - `pt_basic`
 pt_title | Required | Title 
 pt_msg | Required | Message 
 pt_msg_summary | Required | Message line when Notification is expanded
 pt_subtitle | Optional  | Subtitle
 pt_bg | Required | Background Color in HEX
 pt_big_img | Optional | Image
 pt_ico | Optional | Large Icon 
 pt_dl1 | Optional | One Deep Link (minimum)
 pt_title_clr | Optional | Title Color in HEX
 pt_msg_clr | Optional | Message Color in HEX
 pt_small_icon_clr | Optional | Small Icon Color in HEX
 pt_json | Optional | Above keys in JSON format
 
### Auto Carousel Template
 
 Auto Carousel Template Keys | Required | Description 
  ---:|:---:|:--- 
  pt_id | Required | Value - `pt_carousel`
  pt_title | Required | Title 
  pt_msg | Required | Message
  pt_msg_summary | Optional | Message line when Notification is expanded
  pt_subtitle | Optional | Subtitle
  pt_dl1 | Required | Deep Link (Max one) 
  pt_img1 | Required | Image One
  pt_img2 | Required | Image Two
  pt_img`n` | Optional | Image `N`
  pt_bg | Required | Background Color in HEX
  pt_ico | Optional | Large Icon
  pt_title_clr | Optional | Title Color in HEX
  pt_msg_clr | Optional | Message Color in HEX
  pt_small_icon_clr | Optional | Small Icon Color in HEX
  pt_json | Optional | Above keys in JSON format
  
### Manual Carousel Template
 
 Manual Carousel Template Keys | Required | Description 
  ---:|:---:|:--- 
  pt_id | Required | Value - `pt_manual_carousel`
  pt_title | Required | Title 
  pt_msg | Required | Message
  pt_msg_summary | Optional | Message line when Notification is expanded
  pt_subtitle | Optional | Subtitle
  pt_dl1 | Required | Deep Link One
  pt_dl2 | Optional | Deep Link Two
  pt_dl`n` | Optional | Deep Link for the nth image 
  pt_img1 | Required | Image One
  pt_img2 | Required | Image Two
  pt_img`n` | Optional | Image `N`
  pt_bg | Required | Background Color in HEX
  pt_ico | Optional | Large Icon
  pt_title_clr | Optional | Title Color in HEX
  pt_msg_clr | Optional | Message Color in HEX
  pt_small_icon_clr | Optional | Small Icon Color in HEX
  pt_json | Optional | Above keys in JSON format
  
### Rating Template

 Rating Template Keys | Required | Description 
 ---:|:---:|:--- 
 pt_id | Required  | Value - `pt_rating`
 pt_title | Required  | Title 
 pt_msg | Required  | Message 
 pt_msg_summary | Optional | Message line when Notification is expanded
 pt_subtitle | Optional | Subtitle
 pt_default_dl | Required  | Default Deep Link for Push Notification
 pt_dl1 | Required  | Deep Link for first/all star(s)
 pt_dl2 | Optional | Deep Link for second star
 pt_dl3 | Optional | Deep Link for third star
 pt_dl4 | Optional | Deep Link for fourth star
 pt_dl5 | Optional | Deep Link for fifth star
 pt_bg | Required  | Background Color in HEX
 pt_ico | Optional | Large Icon
 pt_title_clr | Optional | Title Color in HEX
 pt_msg_clr | Optional | Message Color in HEX
 pt_small_icon_clr | Optional | Small Icon Color in HEX
 pt_json | Optional | Above keys in JSON format
 
### Product Catalog Template

 Product Catalog Template Keys | Required | Description 
 ---:|:---:|:--- 
 pt_id | Required  | Value - `pt_product_display`
 pt_title | Required  | Title 
 pt_msg | Required  | Message
 pt_subtitle | Optional  | Subtitle
 pt_img1 | Required  | Image One
 pt_img2 | Required  | Image Two
 pt_img3 | Optional  | Image Three
 pt_bt1 | Required  | Big text for first image
 pt_bt2 | Required  | Big text for second image
 pt_bt3 | Required  | Big text for third image
 pt_st1 | Required  | Small text for first image
 pt_st2 | Required  | Small text for second image
 pt_st3 | Required  | Small text for third image
 pt_dl1 | Required  | Deep Link for first image
 pt_dl2 | Required  | Deep Link for second image
 pt_dl3 | Required  | Deep Link for third image
 pt_bg | Required  | Background Color in HEX
 pt_product_display_action | Required  | Action Button Label Text
 pt_product_display_linear | Optional  | Linear Layout Template ("true"/"false")
 pt_product_display_action_clr | Required  | Action Button Background Color in HEX
 pt_title_clr | Optional  | Title Color in HEX
 pt_msg_clr | Optional  | Message Color in HEX
 pt_small_icon_clr | Optional  | Small Icon Color in HEX
 pt_json | Optional  | Above keys in JSON format
 
### Five Icons Template

 Five Icons Template Keys | Required | Description 
  ---:|:---:|:--- 
  pt_id | Required  | Value - `pt_five_icons`
  pt_img1 | Required  | Icon One
  pt_img2 | Required  | Icon Two
  pt_img3 | Required  | Icon Three
  pt_img4 | Required  | Icon Four
  pt_img5 | Required  | Icon Five
  pt_dl1 | Required  | Deep Link for first icon
  pt_dl2 | Required  | Deep Link for second icon
  pt_dl3 | Required  | Deep Link for third icon
  pt_dl4 | Required  | Deep Link for fourth icon
  pt_dl5 | Required  | Deep Link for fifth icon
  pt_bg | Required  | Background Color in HEX
  pt_small_icon_clr | Optional | Small Icon Color in HEX
  pt_json | Optional | Above keys in JSON format
  
### Timer Template
 
 Timer Template Keys | Required | Description 
  ---:|:---:|:--- 
  pt_id | Required | Value - `pt_timer`
  pt_title | Required | Title 
  pt_title_alt | Optional | Title to show after timer expires 
  pt_msg | Required | Message
  pt_msg_alt | Optional | Message to show after timer expires
  pt_msg_summary | Optional | Message line when Notification is expanded
  pt_subtitle | Optional | Subtitle
  pt_dl1 | Optional | Deep Link
  pt_big_img | Optional | Image
  pt_big_img_alt | Optional | Image to show when timer expires
  pt_bg | Required | Background Color in HEX
  pt_timer_threshold | Required | Timer duration in seconds (minimum 10)
  pt_timer_end | Required | Epoch Timestamp to countdown to (for example, $D_1595871380 or 1595871380). Not needed if pt_timer_threshold is specified. 
  pt_title_clr | Optional | Title Color in HEX
  pt_msg_clr | Optional | Message Color in HEX
  pt_small_icon_clr | Optional | Small Icon Color in HEX
  pt_json | Optional | Above keys in JSON format
  
### Video Template
 
 Video Template Keys | Required | Description 
  ---:|:---:|:--- 
  pt_id | Required | Value - `pt_video`
  pt_title | Required | Title 
  pt_msg | Required | Message
  pt_msg_summary | Optional | Message line when Notification is expanded
  pt_subtitle | Optional | Subtitle
  pt_big_img | Optional | Required | Image
  pt_video_url | Optional | Required | Video URL (https only)
  pt_bg | Required | Background Color in HEX
  pt_dl1 | Optional | Deep Link
  pt_title_clr | Optional | Title Color in HEX
  pt_msg_clr | Optional | Message Color in HEX
  pt_small_icon_clr | Optional | Small Icon Color in HEX
  pt_ico | Optional | Large Icon
  pt_json | Optional | Above keys in JSON format
  
### Zero Bezel Template
 
 Zero Bezel Template Keys | Required | Description 
  ---:|:---:|:--- 
  pt_id | Required | Value - `pt_zero_bezel`
  pt_title | Required | Title 
  pt_msg | Required | Message
  pt_msg_summary | Optional | Message line when Notification is expanded
  pt_subtitle | Optional | Subtitle
  pt_big_img | Required | Image
  pt_small_view | Optional | Select text-only small view layout (`text_only`)
  pt_dl1 | Optional | Deep Link
  pt_title_clr | Optional | Title Color in HEX
  pt_msg_clr | Optional | Message Color in HEX
  pt_small_icon_clr | Optional | Small Icon Color in HEX
  pt_ico | Optional | Large Icon
  pt_json | Optional | Above keys in JSON format
  
### Input Box Template
 
 Input Box Template Keys | Required | Description 
  ---:|:---:|:--- 
  pt_id | Required | Value - `pt_input`
  pt_title | Required | Title 
  pt_msg | Required | Message
  pt_msg_summary | Optional | Message line when Notification is expanded
  pt_subtitle | Optional | Subtitle
  pt_big_img | Required | Image
  pt_big_img_alt | Optional | Image to be shown after feedback is collected
  pt_event_name | Optional | Name of Event to be raised
  pt_event_property_<property_name_1> | Optional | Value for event property <property_name_1>  
  pt_event_property_<property_name_2> | Optional | Value for event property <property_name_2>
  pt_event_property_<property_name_n> | Optional | Value for event property <property_name_n>
  pt_input_label | Optional | Label text to be shown on the input
  pt_input_auto_open | Optional | Auto open the app after feedback
  pt_input_feedback | Optional | Feedback 
  pt_dl1 | Optional | Deep Link
  pt_title_clr | Optional | Title Color in HEX
  pt_msg_clr | Optional | Message Color in HEX
  pt_small_icon_clr | Optional | Small Icon Color in HEX
  pt_ico | Optional | Large Icon
  pt_dismiss_on_click | Optional | Dismiss notification on click
  pt_json | Optional | Above keys in JSON format
  
  
  ### NOTE
  * `pt_title` and `pt_msg` in all the templates support HTML elements like bold `<b>`, italics `<i>` and underline `<u>`
  
  ### DEVELOPER NOTE
  * This library uses local file system to download images for better performance. /data/data/<yourapp>/app_data/\*pt_dir\*. 
  * These images are stored at a notification id level.
  * These images are deleted whenever the notification is dismissed or clicked.
  * A silent notification channel with importance: low is created every time on an interaction with the Rating, Manual Carousel, and Product Catalog templates. This prevents the notification sound from playing when the notification is rerendered.
  * The silent notification channel is deleted whenever the notification is dismissed or clicked.   
 
 
# Sample App

[(Back to top)](#table-of-contents)

Check out the [Sample app](app) 

# Contributing

[(Back to top)](#table-of-contents)

Your contributions are always welcome! Please have a look at the [contribution guidelines](CONTRIBUTING.md) first. :tada:

# License

[(Back to top)](#table-of-contents)


The MIT License (MIT) 2020. Please have a look at the [LICENSE.md](LICENSE.md) for more details.
