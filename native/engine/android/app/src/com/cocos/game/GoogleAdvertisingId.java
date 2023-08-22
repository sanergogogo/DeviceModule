package com.cocos.game;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;

import java.io.IOException;

public class GoogleAdvertisingId {

    //获取 GAID
    public static String getGAID(Context context){
        String gaid= "";
        AdvertisingIdClient.Info adInfo = null ;
        try {
            adInfo = AdvertisingIdClient.getAdvertisingIdInfo(context);
        } catch (IOException e) {
            // Unrecoverable error connecting to Google Play services (e.g.,
            // the old version of the service doesn't support getting AdvertisingId).
            Log.e("getGAID", "IOException");
        } catch (GooglePlayServicesNotAvailableException e) {
            // Google Play services is not available entirely.
            Log.e("getGAID", "GooglePlayServicesNotAvailableException");
        } catch (Exception e) {
            Log.e("getGAID", "Exception:"+e.toString());
            // Encountered a recoverable error connecting to Google Play services.
        }
        if (adInfo!= null){
            gaid= adInfo.getId();
            //Log.w("getGAID", "gaid:"+gaid);
        }
        return gaid;
    }

}
