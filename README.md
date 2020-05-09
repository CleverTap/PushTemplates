# Push Templates SDK by CleverTap

The Push Templates SDK helps you engage with your users using fancy Push Notification templates built specifically to work with CleverTap

# Table of contents

- [Installation](#installation)
- [Template Types](#template-types)
- [Template Keys](#template-keys)
- [Usage](#usage)
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

### Basic Template

Basic Template is the basic push notification received on apps.

//TODO @vijay add Basic Template Image

### Auto Carousel Template

Auto Carousel is an automatic revolving carousel push notification.

//TODO @vijay add Auto Carousel GIF if possible

### Rating Template

Rating template lets your users give you feedback, this feedback is captures as an event in CleverTap with the rating as the event property so that it can later be actionable.

//TODO @vijay add Rating template image

### Product Catalog Template

Product Catalog Template lets you show case different images of a product before the user can decide to click on the "BUY NOW" option which can take them directly to the product via deep links.

//TODO @vijay add Product Catalog Template GIF if possible

### Five CTA Template

Five CTA Template is a push notification with no text, just 5 icons and a close button which can help your users go directly to the functionality of their choice with a button's click.

//TODO @vijay add Five CTA Template Image if possible

# Template Keys

[(Back to top)](#table-of-contents)

 Basic Template Keys | Description 
 :---:|:---:| 
 pt_title* | Title 
 pt_msg* | Message 
 pt_big_img* | Image
 pt_ico* | Large Icon 
 pt_bg | Background Color in HEX
 pt_title_clr | Title Color in HEX
 pt_msg_clr | Message Color in HEX
 pt_dl1 | One Deep Link (minimum)
 
 Auto Carousel Template Keys | Description 
  :---:|:---:| 
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
  
 
 Rating Template Keys | Description 
   :---:|:---:| 
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
 
 
 Product Catalog Template Keys | Description 
    :---:|:---:| 
    pt_title* | Title 
    pt_msg* | Message
    pt_bg | Background Color in HEX
    pt_title_clr | Title Color in HEX
    pt_msg_clr | Message Color in HEX
    pt_msg_summary | Message line when Notification is expanded