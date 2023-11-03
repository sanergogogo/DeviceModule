package com.applib.lib_sdkmgr;

// 所有sdk配置
// 这里添加的所有类都能混淆，如果混淆通过反射会找不到类
// 比如 -keep class com.applib.lib_facebook.** { *; }
public class SdkConfig {

    // facebook
    public static final String Facebook = "com.applib.lib_facebook.FacebookLoginManager";

    // firebase
    public static final String Firebase = "com.applib.lib_firebase.FirebaseManager";

    // appsflyer
    public static final String AppsFlyer = "com.applib.lib_appsflyer.AppsFlyerManager";

    // adjust
    public static final String Adjust = "com.applib.lib_adjust.AdjustManager";

    public static final String GooglePay = "com.applib.lib_googlepay.GooglePayManager";

}
