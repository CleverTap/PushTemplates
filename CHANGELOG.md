## CHANGE LOG

### Version 0.0.6 (January 20, 2021)
* Supports CleverTap Android SDK v4.0.2
* Removes support for Video Push Notifications.

Video Push Notifications required an `implementation` dependency of Exoplayer by the app.
Based on feedback, not all developers were comfortable with adding the Exoplayer dependency.
This version removes Video Push Notifications and we will re-introduce them as a separate module soon.
Video Push notifications can still be used with `v0.0.5` of the Push Templates SDK.

### Version 0.0.5 (October 29, 2020)
* Added support for [CleverTap Android SDK v4.0.0](https://github.com/CleverTap/clevertap-android-sdk/blob/master/docs/CTV4CHANGES.md)
* Added `filmstrip` type to Manual Carousel Template. See [README](https://github.com/CleverTap/PushTemplates/blob/master/README.md) for details.
* ExoPlayer is now an `implementation` dependency for PushTemplates SDK 

### Version 0.0.4 (August 19, 2020)
* Removed `Rated` event. 
* The library will now never raise extra events apart from CleverTap System Events and events given to the library by KV pairs.
* CTAs on Video, Rating and 5 CTA Template can now be tracked in the event Notification Clicked with in the property `wzrk_c2a`.
* Added support for collapse key.
* Performance enhancements

### Version 0.0.3 (August 3, 2020)
* Added 5 more templates - Video, Manual Carousel, Timer, Zero Bezel & Input Templates
* Added support for multiple instances of CleverTap
* Performance enhancements

### Version 0.0.2 (May 21, 2020)
* Added Duplication check
* Added support to enable/disable logs
* Performance enhancements

### Version 0.0.1 (May 12, 2020)
* First release :tada:
* Supports 5 templates - Basic, Auto Carousel, Rating, Product Catalog and Five Icons
* Compatible with CleverTap Android SDK v3.8.0
