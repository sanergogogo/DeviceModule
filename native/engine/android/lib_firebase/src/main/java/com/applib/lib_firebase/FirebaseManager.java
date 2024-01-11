package com.applib.lib_firebase;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;

import com.applib.lib_common.ApiCallback;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONObject;

public class FirebaseManager {

    private static String TAG = "FirebaseManager";

    private static FirebaseAnalytics mFirebaseAnalytics;
    private static Context mContext;

    public static String notification_data = "";

    public  static void setMContext(Context context){
        FirebaseManager.mContext = context;
    }

    //初始化firebase
    public static void init(Context context) {
        mContext = context;
        if (checkPlayServices(context)) {
            mFirebaseAnalytics = FirebaseAnalytics.getInstance(context);
        }
    }

    public static boolean trackEvent(String data){
        if (checkPlayServices(mContext)) {
            try {
                Log.i(TAG, "logEvent: "+ data);
                JSONObject jsonObject = new JSONObject(data);
                String event_name = jsonObject.getString("event_name");
                int event_type = jsonObject.getInt("event_type");//1 代表 是购买 0 代表是普通事件
                Bundle bundle = new Bundle();
                if (event_type == 1) {
                    double value = jsonObject.getDouble("value");//购买的金额
                    String currency = jsonObject.getString("currency");//购买的国家货币类型
                    String transaction_id = jsonObject.getString("transaction_id");//订单号
                    bundle.putDouble(FirebaseAnalytics.Param.VALUE, value);
                    bundle.putString(FirebaseAnalytics.Param.CURRENCY, currency);
                    bundle.putString(FirebaseAnalytics.Param.TRANSACTION_ID, transaction_id);
                }
                mFirebaseAnalytics.logEvent(event_name, bundle);
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            return true;
        } else {
            return false;
        }
    }

    public static void getToken(final ApiCallback callback) {
        if (checkPlayServices(mContext)) {
            try {
                FirebaseMessaging.getInstance().getToken()
                        .addOnCompleteListener(new OnCompleteListener<String>() {
                            @Override
                            public void onComplete(@NonNull Task<String> task) {
                                if (!task.isSuccessful()) {
                                    Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                                    return;
                                }

                                //Get new FCM registration token
                                String token = task.getResult();
                                Log.i(TAG, "Fetching FCM registration token:" + token);
                                callback.onSuccess(token);
                            }
                        });
            } catch (Exception e) {
                //callback.onFail("fail");
            }
        }
    }

    //是否开启分析收集事件
    public static void setAnalyticsCollectionEnabled(boolean enabled){
        if (checkPlayServices(mContext)) {
            try {
                Log.i(TAG, "setAnalyticsCollectionEnabled: " + enabled);
                mFirebaseAnalytics.setAnalyticsCollectionEnabled(enabled);

            } catch (Exception e) {
                e.printStackTrace();

            }
        }
    }

    public static void subscribeToTopic(String topic) {
        if (checkPlayServices(mContext)) {
            try {
                FirebaseMessaging.getInstance().subscribeToTopic(topic)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                String msg = topic + " Subscribed";
                                if (!task.isSuccessful()) {
                                    msg = topic + " Subscribe failed";
                                }
                                Log.i(TAG, msg);
                            }
                        });
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
        }
    }

    public static void unsubscribeFromTopic(String topic) {
        if (checkPlayServices(mContext)){
            try {
                FirebaseMessaging.getInstance().unsubscribeFromTopic(topic)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                String msg = topic + " unsubscribed";
                                if (!task.isSuccessful()) {
                                    msg = topic + " unsubscribe failed";
                                }
                                Log.i(TAG, msg);
                            }
                        });
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
        }
    }

    //设置firebase 推送信息 赋值
    public static void setNotificationData(String message) {
        try {
            Log.d(TAG, "setNotificationData: " + message);
            FirebaseManager.notification_data = message;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //获取firebase 推送数据
    public static String getNotificationData() {
        String message = FirebaseManager.notification_data;
        FirebaseManager.notification_data = "";
        return message;
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If it
     * doesn't, display a dialog that allows users to download the APK from the
     * Google Play Store or enable it in the device's system settings.
     */
    public static boolean checkPlayServices(Context context) {
        int resultCode = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable
                (context);
        if (resultCode != ConnectionResult.SUCCESS) {
            Log.i(TAG, "This device does not support Google Play Services. " +
                    "Push notifications are not supported");
            return false;
        }
        return true;
    }

}
