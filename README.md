# Push Templates SDK by CleverTap

The Push Templates SDK helps you engage with your users using fancy Push Notification templates built specifically to work with CleverTap

# Table of contents

- [Installation](#installation)
- [Template Types](#template-types)
- [Template Keys](#template-keys)
- [Usage](#usage)
- [Sample App](#sample-app)
- [Contributing](#contributing)
- [License](#license)

# Installation

[(Back to top)](#table-of-contents)

### Out of the box

1. Add the dependencies to the `build.gradle`
//TODO add code

2. Add the Service to your `AndroidManifest.xml`
//TODO add code

3. Add the Receiver to your `AndroidManifest.xml`
//TODO add code

### Custom Handling

1. Add the dependencies to the `build.gradle`
//TODO add code

2. Add the Receiver to your `AndroidManifest.xml`
//TODO add code

3. Add the following code in your custom FirebaseMessageService class
//TODO add code

# Template Types

[(Back to top)](#table-of-contents)

## Basic Template

Basic Template is the basic push notification received on apps.

![Basic Unexpanded](https://github.com/darshanclevertap/PushTemplates/blob/readme-images/screens/basic%20unexpanded.png)

<br/>(When Expanded)<br/><br/>
![Basic Expanded](https://github.com/darshanclevertap/PushTemplates/blob/readme-images/screens/basic%20expanded.png)


## Auto Carousel Template

Auto carousel is an automatic revolving carousel push notification.

//TODO @vijay add Auto Carousel GIF if possible

## Rating Template

Rating template lets your users give you feedback, this feedback is captures as an event in CleverTap with the rating as the event property so that it can later be actionable.

//TODO @vijay add Rating template image

## Product Catalog Template

Product catalog template lets you show case different images of a product before the user can decide to click on the "BUY NOW" option which can take them directly to the product via deep links.

//TODO @vijay add Product Catalog Template GIF if possible

## Five Icons Template

Five icons template is a push notification with no text, just 5 icons and a close button which can help your users go directly to the functionality of their choice with a button's click.

//TODO @vijay add Five CTA Template Image if possible

# Template Keys

[(Back to top)](#table-of-contents)

## Basic Template

 Basic Template Keys | Description 
 :---:|:---:| 
 pt_id | Value - `pt_basic`
 pt_title* | Title 
 pt_msg* | Message 
 pt_big_img* | Image
 pt_ico* | Large Icon 
 pt_bg | Background Color in HEX
 pt_title_clr | Title Color in HEX
 pt_msg_clr | Message Color in HEX
 pt_dl1 | One Deep Link (minimum)
 pt_json | Above keys in JSON format
 
## Auto Carousel Template
 
 Auto Carousel Template Keys | Description 
  :---:|:---:| 
  pt_id | Value - `pt_carousel`
  pt_title* | Title 
  pt_msg* | Message
  pt_dl1* | One Deep Link (minimum)
  pt_img1* | Image One
  pt_img2* | Image Two
  pt_img3* | Image Three
  pt_img4 | Image Four
  pt_img5 | Image Five 
  pt_bg | Background Color in HEX
  pt_title_clr | Title Color in HEX
  pt_msg_clr | Message Color in HEX
  pt_msg_summary | Message line when Notification is expanded
  pt_json | Above keys in JSON format
  
## Rating Template

 Rating Template Keys | Description 
 :---:|:---:| 
 pt_id | Value - `pt_rating`
 pt_title* | Title 
 pt_msg* | Message 
 pt_default_dl* | Default Deep Link for Push Notification
 pt_dl1* | Deep Link for first/all star(s)
 pt_dl2 | Deep Link for second star
 pt_dl3 | Deep Link for third star
 pt_dl4 | Deep Link for fourth star
 pt_dl5 | Deep Link for fifth star
 pt_bg | Background Color in HEX
 pt_title_clr | Title Color in HEX
 pt_msg_clr | Message Color in HEX
 pt_msg_summary | Message line when Notification is expanded
 pt_json | Above keys in JSON format
 
## Product Catalog Template

 Product Catalog Template Keys | Description 
 :---:|:---:| 
 pt_id | Value - `pt_product_display`
 pt_title* | Title 
 pt_msg* | Message
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
 pt_bg | Background Color in HEX
 pt_title_clr | Title Color in HEX
 pt_msg_clr | Message Color in HEX
 pt_json | Above keys in JSON format
 
## Five Icons Template

 Five Icons Template Keys | Description 
  :---:|:---:| 
  pt_id | Value - `pt_five_icons`
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
  pt_json | Above keys in JSON format
  
  ### NOTE
  (*) - Mandatory
  
# Usage

[(Back to top)](#table-of-contents)

Using the above mentioned keys is very simple. While creating a Push Notification campaign on CleverTap, just follow the steps below -

1. On the "WHAT" section pass some staple values in the "title" and "message" fields (NOTE: These will be ignored)

![Basic](https://github.com/darshanclevertap/PushTemplates/blob/readme-images/screens/basic.png)

2. Click on "Advanced" and then click on "Add pair" to add the above keys

![KVs](https://github.com/darshanclevertap/PushTemplates/blob/readme-images/screens/kv.png)

3. You can also add the above keys into one JSON object and use the `pt_json` key to fill in the values

//TODO @vijay Add custom key value pair screenshot using pt_json
![KVs in JSON](https://github.com/darshanclevertap/PushTemplates/blob/readme-images/screens/json.png)

4. Send a test push and schedule!

# Sample App

[(Back to top)](#table-of-contents)

Check out the [Sample app](app) 

# Contributing

[(Back to top)](#table-of-contents)

Your contributions are always welcome! Please have a look at the [contribution guidelines](CONTRIBUTING.md) first. :tada:

# License

[(Back to top)](#table-of-contents)


The MIT License (MIT) 2020. Please have a look at the [LICENSE.md](LICENSE.md) for more details.
