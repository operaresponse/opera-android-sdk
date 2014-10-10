Opera Android SDK
=================

Updated: October 10, 2014

Version: 0.1d

About
=====

The Opera Android SDK works with MoPub SDK. Integration process is very simple. Download the sdk jar file into your libs folder. Setup your Ad Units on MoPub dashboard. And that's all. No other confguration or code is required.

Get Started
===========

1. Download the jar file from Library folder or download the entire project with the [**Download ZIP**](https://github.com/operaresponse/opera-android-sdk/archive/master.zip) button on this page.

2. Copy the `Library/opera-sdk.jar` into your libs folder. Be sure that the libs folder is on your build classpath. 

3. Add following libs to your classpath from the Library folder:
```
- retrofit-1.6.1.jar
- okhttp-2.0.0.jar
- okhttp-urlconnection-2.0.0.jar
```
or use gradle dependencies

```
    compile 'com.squareup.retrofit:retrofit:1.6.1'
    compile 'com.squareup.okhttp:okhttp-urlconnection:2.0.0'
    compile 'com.squareup.okhttp:okhttp:2.0.0'
```

4. Setup your Ad Units in MoPub dashboard with either Line Items or a Network and plugin following settings:

**Banner Setup**
- Custom class name for Banner Ads: com.operamediaworks.android.OperaEventBanner
- Custom data for Banner Ads: 

```
    {
        "sig": <opera publisher sig>, 
        "width": 320, 
        "height": 50
    }
```

**Interstitial Setup**
- Custom class name for Banner Ads: com.operamediaworks.android.OperaInterstitial
- Custom data for Interstitial Ads: 

```
    {
        "sig": <opera publisher sig>, 
        "width": 320, 
        "height": 480
    }
```

Checkout the [Wiki](https://github.com/operaresponse/opera-android-sdk/wiki) for more details on optimizing your revenue with the Opera Android SDK.

Issues
======

Please report any issues to <support email>. 
