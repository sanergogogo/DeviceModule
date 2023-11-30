package com.applib.lib_google;
import android.content.Context;
import android.os.RemoteException;
import android.util.Log;

import com.android.installreferrer.api.InstallReferrerClient;
import com.android.installreferrer.api.InstallReferrerClient.InstallReferrerResponse;
import com.android.installreferrer.api.InstallReferrerStateListener;
import com.android.installreferrer.api.ReferrerDetails;

public class GoogleReferrerHelper {
    private static GoogleReferrerHelper instance = null;

    public static GoogleReferrerHelper getInstance() {
        if (instance == null) {
            instance = new GoogleReferrerHelper();
        }
        return instance;
    }

    private static final String TAG = "GoogleReferrerHelper";
    private InstallReferrerClient mReferrerClient;
    private Context mContext;

    private String mInstallReferrer = "";

    public void start(Context context) {
        Log.d(TAG, "start");
        mContext = context;
        if (mReferrerClient != null) {
            end();
        }
        mReferrerClient = InstallReferrerClient.newBuilder(context).build();
        mReferrerClient.startConnection(new InstallReferrerStateListener() {
            @Override
            public void onInstallReferrerSetupFinished(int responseCode) {
                Log.d(TAG, String.format("onInstallReferrerSetupFinished, responseCode: %d", responseCode));
                switch (responseCode) {
                    case InstallReferrerResponse.OK:
                        // Connection established.
                        getArgs();
                        break;
                    case InstallReferrerResponse.FEATURE_NOT_SUPPORTED:
                        // API not available on the current Play Store app.
                        Log.d(TAG, "InstallReferrerResponse.FEATURE_NOT_SUPPORTED");
                        break;
                    case InstallReferrerResponse.SERVICE_UNAVAILABLE:
                        // Connection couldn't be established.
                        Log.d(TAG, "InstallReferrerResponse.SERVICE_UNAVAILABLE");
                        break;
                }
            }

            @Override
            public void onInstallReferrerServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
                Log.d(TAG, "onInstallReferrerServiceDisconnected");
            }
        });
    }

    public String getInstallReferrer() {
        return mInstallReferrer;
    }

    private void getArgs() {
        try {
            ReferrerDetails response = mReferrerClient.getInstallReferrer();
            String referrerUrl = response.getInstallReferrer();
            long referrerClickTime = response.getReferrerClickTimestampSeconds();
            long appInstallTime = response.getInstallBeginTimestampSeconds();
            //boolean instantExperienceLaunched = response.getGooglePlayInstantParam();

            mInstallReferrer = String.format("%s&referrerClickTime=%d&appInstallTime=%d", referrerUrl, referrerClickTime, appInstallTime);
            Log.d(TAG, String.format("InstallReferrer: %s", mInstallReferrer));
            SPUtil.putString(mContext, "InstallReferrer", mInstallReferrer);
            end();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void end() {
        if (mReferrerClient != null) {
            mReferrerClient.endConnection();
            mReferrerClient = null;
        }
    }
}
