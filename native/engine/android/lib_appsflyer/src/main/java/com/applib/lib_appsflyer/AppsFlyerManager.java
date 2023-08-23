package com.applib.lib_appsflyer;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.appsflyer.AppsFlyerLib;
import com.appsflyer.AppsFlyerConversionListener;
import com.appsflyer.attribution.AppsFlyerRequestListener;
import com.appsflyer.AFInAppEventType; // Predefined event names
import com.appsflyer.AFInAppEventParameterName; // Predefined parameter names

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class AppsFlyerManager {

    private static final String TAG = "AppsFlyerManager";

    private static Context mContext;

    public static void init(Context context, String afKey, String channelId){
        mContext = context;
        try {
            AppsFlyerConversionListener conversionListener = new AppsFlyerConversionListener() {
                public void onConversionDataSuccess(Map<String, Object> var1) {
                    for (String attrName : var1.keySet()) {
                        Log.d(TAG, "attribute: " + attrName + " = " + var1.get(attrName));
                    }
                }

                public void onConversionDataFail(String var1) {
                    Log.d(TAG, "error getting conversion data: " + var1);
                }

                public void onAppOpenAttribution(Map<String, String> var1) {
                    for (String attrName : var1.keySet()) {
                        Log.d(TAG, "attribute: " + attrName + " = " + var1.get(attrName));
                    }
                }

                public void onAttributionFailure(String var1) {
                    Log.d(TAG, "error onAttributionFailure : " + var1);
                }
            };
            // 手动设置oaid
            //AppsFlyerLib.getInstance().setOaidData("");

            AppsFlyerLib.getInstance().init(afKey, conversionListener, context);
            AppsFlyerLib.getInstance().waitForCustomerUserId(true);
            AppsFlyerLib.getInstance().start(context, afKey, new AppsFlyerRequestListener() {
                @Override
                public void onSuccess() {
                    Log.d(TAG, "Launch sent successfully, got 200 response code from server");
                }

                @Override
                public void onError(int i, @NonNull String s) {
                    Log.d(TAG, "Launch failed to be sent:\n" +
                            "Error code: " + i + "\n"
                            + "Error description: " + s);
                }
            });
            AppsFlyerLib.getInstance().setCustomerIdAndLogSession(channelId, context);

            // 调试日志
            AppsFlyerLib.getInstance().setDebugLog(true);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public static String getAdid(Context context){
        String result = "";
        try{
            result = AppsFlyerLib.getInstance().getAppsFlyerUID(context);
        }catch (Exception e){
            e.printStackTrace();
        }
        return result;
    }

    public static void trackEvent(String eventData) {
        try {
            JSONObject jsonObject = new JSONObject(eventData);
            String event_name = jsonObject.getString("event_name");
            Map<String, Object> eventValues = new HashMap<String, Object>();
            int type = jsonObject.getInt("event_type");   // 1 代表 是购买 0 代表是普通事件
            if (type == 1) {
                double value = jsonObject.getDouble("value");   // 购买的金额
                String currency = jsonObject.getString("currency"); // 购买的国家货币类型
                String transaction_id = jsonObject.getString("transaction_id");// 订单号
                String product_id = jsonObject.getString("product_id");// 产品号
                //String channel_id = jsonObject.getString("channel_id");// 渠道号
                //String player_id = jsonObject.getString("player_id");// 玩家id
                eventValues.put(AFInAppEventParameterName.REVENUE, value);
                eventValues.put(AFInAppEventParameterName.CURRENCY, currency);
                eventValues.put(AFInAppEventParameterName.ORDER_ID, transaction_id);
                eventValues.put(AFInAppEventParameterName.CONTENT_ID, product_id);
            }

            AppsFlyerLib.getInstance().logEvent(mContext,
                    "af_" + event_name, eventValues);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
