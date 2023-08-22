package com.applib.lib_adjust;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.adjust.sdk.Adjust;
import com.adjust.sdk.AdjustConfig;
import com.adjust.sdk.AdjustEvent;
import com.adjust.sdk.LogLevel;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class AdjustManager {

    // 预定义的事件
    private static final Map<String, String> eventMap = new HashMap<String, String>();
    static {
        eventMap.put("purchase", "mfmtqc");
        eventMap.put("normal_event", "wxyb47");
    }

    public static void init(Context context, String key, String channelId) {
        try {
            String environment = AdjustConfig.ENVIRONMENT_SANDBOX ; // TODO AdjustConfig.ENVIRONMENT_PRODUCTION;
            AdjustConfig config = new AdjustConfig(context, key, environment);
            config.setLogLevel(LogLevel.SUPRESS);
            Adjust.onCreate(config);
            Application app = (Application)context;
            app.registerActivityLifecycleCallbacks(new AdjustLifecycleCallbacks());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static String getAdid(){
        String result = "";
        try {
            result = Adjust.getAdid();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static void trackEvent(String eventData) {
        try {
            JSONObject jsonObject = new JSONObject(eventData);
            String event_name = jsonObject.getString("event_name");
            AdjustEvent adjustEvent = new AdjustEvent(eventMap.get(event_name));
            int type = jsonObject.getInt("event_type");   // 1 代表 是购买 0 代表是普通事件
            if (type == 1) {
                double value = jsonObject.getDouble("value");   // 购买的金额
                String currency = jsonObject.getString("currency"); // 购买的国家货币类型
                String transaction_id = jsonObject.getString("transaction_id");// 订单号
                String product_id = jsonObject.getString("product_id");// 产品号
                //String channel_id = jsonObject.getString("channel_id");// 渠道号
                //String player_id = jsonObject.getString("player_id");// 玩家id
                adjustEvent.setRevenue(value, currency);
                adjustEvent.setOrderId(transaction_id);
                adjustEvent.setProductId(product_id);
                //adjustEvent.addPartnerParameter("channel_id",channel_id);
                //adjustEvent.addPartnerParameter("player_id",player_id);
            }
            Adjust.trackEvent(adjustEvent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private static final class AdjustLifecycleCallbacks implements Application.ActivityLifecycleCallbacks {

        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

        }

        @Override
        public void onActivityStarted(Activity activity) {

        }

        @Override
        public void onActivityResumed(Activity activity) {
            Log.i("AdjustManager", "onActivityResumed");
            Adjust.onResume();
        }

        @Override
        public void onActivityPaused(Activity activity) {
            Log.i("AdjustManager", "onActivityPaused");
            Adjust.onPause();
        }

        @Override
        public void onActivityStopped(Activity activity) {

        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

        }

        @Override
        public void onActivityDestroyed(Activity activity) {

        }
    }

}
