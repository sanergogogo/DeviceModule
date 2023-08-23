package com.applib.lib_facebook;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import com.applib.lib_common.ApiCallback;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;

import java.util.Arrays;

public class FacebookLoginManager {

    private static  FacebookLoginManager _instance = null;
    protected static Activity mActivity = null;
    private static CallbackManager callbackManager = null;
    protected static ApiCallback loginCallback = null;

    private static final String TAG = "Facebook";

    private FacebookLoginManager() {

    }

    public static FacebookLoginManager getInstance(Activity activity){
        if (_instance == null){
            _instance = new FacebookLoginManager();
        }
        mActivity = activity;
        return _instance;
    }

    public void init() {
        callbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                String token = loginResult.getAccessToken().getToken();
                loginCallback.onSuccess(token);
            }

            @Override
            public void onCancel() {
                loginCallback.onFail("cancel");
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG, error.toString());
                loginCallback.onFail(error.toString());
            }
        });
    }

    public void doLogin(ApiCallback callback) {
        loginCallback = callback;

        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        boolean isLoggedIn = accessToken != null && !accessToken.isExpired();
        if (isLoggedIn) {
            loginCallback.onSuccess(accessToken.getToken());
            return;
        }
        LoginManager.getInstance().logInWithReadPermissions(mActivity, Arrays.asList("public_profile"));
    }

    public void clearToken() {
        try {
            Log.e(TAG, "clearToken: ");
            LoginManager.getInstance().logOut();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onActivityResult(Integer requestCode, Integer resultCode, Intent data){
        Log.d(TAG, "onActivityResult: "+"resultCode:"+resultCode+"   requestCode:" +resultCode+ "  data:"+(data != null ? data.getDataString() : ""));
        if (callbackManager != null){
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }
}
