package com.applib.lib_sdkmgr;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.applib.lib_common.ApiCallback;

public class SdkManager {

    public static void initFacebook(Activity context){
        try {
            Object managerObject = ReFlectUtils.processMethod(
                    SdkConfig.Facebook, "getInstance",
                    new Class[] { Activity.class }, new Object[] { context });
            ReFlectUtils.processMethod(managerObject, "init",
                    new Class[] {},
                    new Object[] {});
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void doLoginFacebook(Activity context, ApiCallback callback) {
        try {
            Object managerObject = ReFlectUtils.processMethod(
                    SdkConfig.Facebook, "getInstance",
                    new Class[] { Activity.class }, new Object[] { context });
            ReFlectUtils.processMethod(managerObject, "doLogin",
                    new Class[] { ApiCallback.class },
                    new Object[] { callback });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void onActivityResultFacebook(Activity context, int requestCode, int resultCode, Intent data){
        try {
            Object managerObject = ReFlectUtils.processMethod(
                    SdkConfig.Facebook, "getInstance",
                    new Class[] { Activity.class },
                    new Object[] { context });
            ReFlectUtils.processMethod(managerObject, "onActivityResult",
                    new Class[] {Integer.class, Integer.class, Intent.class},
                    new Object[] {requestCode, resultCode, data});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void initFirebase(Context context) {
        try {
            ReFlectUtils.processMethod(
                    SdkConfig.Firebase, "init",
                    new Class[] { Context.class },
                    new Object[] { context });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void trackEventFirebase(String eventData) {
        try {
            ReFlectUtils.processMethod(
                    SdkConfig.Firebase, "trackEvent",
                    new Class[] { String.class },
                    new Object[] { eventData });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void getTokenFirebase(ApiCallback callback) {
        try {
            ReFlectUtils.processMethod(
                    SdkConfig.Firebase, "getToken",
                    new Class[] { ApiCallback.class },
                    new Object[] { callback });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setMContextFirebase(Context context) {
        try {
            ReFlectUtils.processMethod(
                    SdkConfig.Firebase, "setMContext",
                    new Class[] { Context.class },
                    new Object[] { context });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setAnalyticsCollectionEnabledFirebase(Boolean enabled) {
        try {
            ReFlectUtils.processMethod(
                    SdkConfig.Firebase, "setAnalyticsCollectionEnabled",
                    new Class[] { Boolean.class },
                    new Object[] { enabled });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void subscribeToTopicFirebase(String topic) {
        try {
            ReFlectUtils.processMethod(
                    SdkConfig.Firebase, "subscribeToTopic",
                    new Class[] { String.class },
                    new Object[] { topic });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void unsubscribeFromTopicFirebase(String topic) {
        try {
            ReFlectUtils.processMethod(
                    SdkConfig.Firebase, "unsubscribeFromTopic",
                    new Class[] { String.class },
                    new Object[] { topic });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setNotificationDataFirebase(String message) {
        try {
            ReFlectUtils.processMethod(
                    SdkConfig.Firebase, "setNotificationData",
                    new Class[] { String.class },
                    new Object[] { message });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getNotificationDataFirebase(){
        String result = "";
        try {
            result = (String) ReFlectUtils.processMethod(
                    SdkConfig.Firebase, "getNotificationData",
                    new Class[] { },
                    new Object[] { });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static void initAppsFlyer(Context context, String key, String channelId) {
        try {
            ReFlectUtils.processMethod(
                    SdkConfig.AppsFlyer, "init",
                    new Class[] { Context.class, String.class, String.class },
                    new Object[] { context, key, channelId });
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public static String getAdidAppsFlyer(){
        String result = "";
        try {
            result = (String) ReFlectUtils.processMethod(
                    SdkConfig.AppsFlyer, "getAdid",
                    new Class[] { },
                    new Object[] { });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static void trackEventAppsFlyer(String eventData) {
        try {
            ReFlectUtils.processMethod(
                    SdkConfig.AppsFlyer, "trackEvent",
                    new Class[] { String.class },
                    new Object[] { eventData });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void initAdjust(Context context, String key, String channelId) {
        try {
            ReFlectUtils.processMethod(
                    SdkConfig.Adjust, "init",
                    new Class[] { Context.class, String.class, String.class },
                    new Object[] { context, key, channelId });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getAdidAdjust() {
        String result = "";
        try {
            result = (String) ReFlectUtils.processMethod(
                    SdkConfig.Adjust, "getAdid",
                    new Class[] { },
                    new Object[] { });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static void trackEventAdjust(String eventData) {
        try {
            ReFlectUtils.processMethod(
                    SdkConfig.Adjust, "trackEvent",
                    new Class[] { String.class },
                    new Object[] { eventData });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void initGoogleService(Activity context){
        try {
            Object managerObject = ReFlectUtils.processMethod(
                    SdkConfig.GoogleService, "getInstance",
                    new Class[] { Activity.class }, new Object[] { context });
            ReFlectUtils.processMethod(managerObject, "init",
                    new Class[] {},
                    new Object[] {});
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void doGooglePay(Activity context, String productId, String orderId, String productType, ApiCallback callback) {
        try {
            Object managerObject = ReFlectUtils.processMethod(
                    SdkConfig.GoogleService, "getInstance",
                    new Class[] { Activity.class }, new Object[] { context });
            ReFlectUtils.processMethod(managerObject, "pay",
                    new Class[] { String.class, String.class, String.class, ApiCallback.class },
                    new Object[] { productId, orderId, productType, callback });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void doGoogleSignIn(Activity context, ApiCallback callback) {
        try {
            Object managerObject = ReFlectUtils.processMethod(
                    SdkConfig.GoogleService, "getInstance",
                    new Class[] { Activity.class }, new Object[] { context });
            ReFlectUtils.processMethod(managerObject, "signIn",
                    new Class[] { ApiCallback.class },
                    new Object[] { callback });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void doGoogleSignOut(Activity context, ApiCallback callback) {
        try {
            Object managerObject = ReFlectUtils.processMethod(
                    SdkConfig.GoogleService, "getInstance",
                    new Class[] { Activity.class }, new Object[] { context });
            ReFlectUtils.processMethod(managerObject, "signOut",
                    new Class[] { ApiCallback.class },
                    new Object[] { callback });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getGoogleAdid(Activity context) {
        String result = "";
        try {
            Object managerObject = ReFlectUtils.processMethod(
                    SdkConfig.GoogleService, "getInstance",
                    new Class[] { Activity.class }, new Object[] { context });
            result = (String) ReFlectUtils.processMethod(managerObject, "getAdid",
                    new Class[] {  },
                    new Object[] { });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static String getInstallReferrer(Activity context) {
        String result = "";
        try {
            Object managerObject = ReFlectUtils.processMethod(
                    SdkConfig.GoogleService, "getInstance",
                    new Class[] { Activity.class }, new Object[] { context });
            result = (String) ReFlectUtils.processMethod(managerObject, "getInstallReferrer",
                    new Class[] {  },
                    new Object[] { });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static boolean isGooglePlayServicesAvailable(Context context) {
        Boolean result = false;
        try {
            result = (Boolean) ReFlectUtils.processMethod(
                    SdkConfig.GoogleService, "checkPlayServices",
                    new Class[] { Context.class },
                    new Object[] { context });
            if (result == null)
                result = false;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    public static void onActivityResultGoogle(Activity context, int requestCode, int resultCode, Intent data){
        try {
            Object managerObject = ReFlectUtils.processMethod(
                    SdkConfig.GoogleService, "getInstance",
                    new Class[] { Activity.class },
                    new Object[] { context });
            ReFlectUtils.processMethod(managerObject, "onActivityResult",
                    new Class[] {Integer.class, Integer.class, Intent.class},
                    new Object[] {requestCode, resultCode, data});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
