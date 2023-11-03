/****************************************************************************
Copyright (c) 2015-2016 Chukong Technologies Inc.
Copyright (c) 2017-2018 Xiamen Yaji Software Co., Ltd.

http://www.cocos2d-x.org

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
****************************************************************************/
package com.cocos.game;

import android.app.Activity;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.content.Intent;
import android.content.res.Configuration;
import android.text.TextUtils;
import android.util.Log;

import com.cocos.lib.CocosHelper;
import com.cocos.lib.JsbBridge;
import com.cocos.service.SDKWrapper;
import com.cocos.lib.CocosActivity;
import com.applib.lib_sdkmgr.SdkManager;
import com.yalantis.ucrop.UCrop;
import com.yalantis.ucrop.util.FileUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.concurrent.Executors;

public class AppActivity extends CocosActivity {

    private static final String TAG = "AppActivity";

    private static Activity sActivity = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // DO OTHER INITIALIZATION BELOW
        SDKWrapper.shared().init(this);

        sActivity = this;

        checkGoogleAdId();

        // 安装归因
        if (GlobalConfig.HasInstallReferrer && TextUtils.isEmpty(SPUtil.getString(this, "InstallReferrer", ""))) {
            GoogleReferrerHelper.getInstance().start(this);
        }

        if (GlobalConfig.HasFacebook) {
            SdkManager.initFacebook(this);
        }

        if (GlobalConfig.HasFirebase) {
            initFireBasePush();
            SdkManager.initFirebase(this);
        }

        if (GlobalConfig.HasAdjust) {
            SdkManager.initAdjust(this.getApplicationContext(), GlobalConfig.AdjustKey, GlobalConfig.ChannelId);
        }

        if (GlobalConfig.HasAppsFlyer) {
            SdkManager.initAppsFlyer(this, GlobalConfig.AppsFlyerKey, GlobalConfig.ChannelId);
        }

        if (GlobalConfig.HasGooglePay) {
            SdkManager.initGooglePay(this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        SDKWrapper.shared().onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        SDKWrapper.shared().onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Workaround in https://stackoverflow.com/questions/16283079/re-launch-of-activity-on-home-button-but-only-the-first-time/16447508
        if (!isTaskRoot()) {
            return;
        }
        SDKWrapper.shared().onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        SDKWrapper.shared().onActivityResult(requestCode, resultCode, data);

        if (GlobalConfig.HasFacebook) {
            SdkManager.onActivityResultFacebook(this, requestCode, resultCode, data);
        }

        Log.i(TAG, "AppActivity onActivityResult requestCode:" + requestCode + " resultCode:" + resultCode);

        switch (requestCode){
            case 1000:
                // android不能确定是否真的分享了，都当作成功处理
                CocosHelper.runOnGameThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put("success", true);
                            JsbBridge.sendToScript("share", jsonObject.toString());
                        } catch (JSONException ex) {
                            // 键为null或使用json不支持的数字格式(NaN, infinities)
                            Log.e(TAG, "json object exception:" + ex.getMessage());
                        }
                    }
                });
                break;
            case 100:
                if (resultCode != RESULT_OK) {
                    break;
                }
                Uri uri = data.getData();

                if (DeviceModule.cropOptions != null) {
                    JSONObject jsonObj = null;
                    boolean crop = false;
                    String filename = "crop.jpg";
                    float aspectRatioX = 1.0f;
                    float aspectRatioY = 1.0f;
                    int maxWidth = 500;
                    int maxHeight = 500;
                    try {
                        jsonObj = new JSONObject(DeviceModule.cropOptions);
                        crop = jsonObj.getInt("type") != 0 ? true : false;
                        filename = jsonObj.getString("filename");
                        aspectRatioX = (float)jsonObj.getDouble("aspectRatioX");
                        aspectRatioY = (float)jsonObj.getDouble("aspectRatioY");
                        maxWidth = jsonObj.getInt("maxWidth");
                        maxHeight = jsonObj.getInt("maxHeight");
                    } catch (JSONException ex) {
                        // 键为null或使用json不支持的数字格式(NaN, infinities)
                        Log.e(TAG, "json object exception:" + ex.getMessage());
                    }
                    if (crop) {
                        UCrop.Options options = new UCrop.Options();
                        options.setCompressionFormat(Bitmap.CompressFormat.JPEG);
                        options.setCompressionQuality(100);
                        options.withAspectRatio(aspectRatioX, aspectRatioY);
                        options.withMaxResultSize(maxWidth, maxHeight);
                        //options.setCircleDimmedLayer(true);
                        //options.setFreeStyleCropEnabled(false);
                        //options.setShowCropGrid(showCropGuidelines);
                        //options.setShowCropFrame(showCropFrame);
                        //options.setHideBottomControls(true);
                        UCrop.of(uri, Uri.fromFile(new File(this.getCacheDir() + "/" + filename)))
                                .withOptions(options)
                                .start(this);
                        break;
                    }
                }

                CocosHelper.runOnGameThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put("filepath", FileUtils.getPath(sActivity, uri));
                            JsbBridge.sendToScript("selectImageFromAlbum", jsonObject.toString());
                        } catch (JSONException ex) {
                            // 键为null或使用json不支持的数字格式(NaN, infinities)
                            Log.e(TAG, "json object exception:" + ex.getMessage());
                        }

                    }
                });
                break;
            case UCrop.REQUEST_CROP:
                if (resultCode == RESULT_OK) {
                    final Uri resultUri = UCrop.getOutput(data);
                    CocosHelper.runOnGameThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                JSONObject jsonObject = new JSONObject();
                                jsonObject.put("filepath", FileUtils.getPath(sActivity, resultUri));
                                JsbBridge.sendToScript("selectImageFromAlbum", jsonObject.toString());
                            } catch (JSONException ex) {
                                // 键为null或使用json不支持的数字格式(NaN, infinities)
                                Log.e(TAG, "json object exception:" + ex.getMessage());
                            }

                        }
                    });
                } else if (resultCode == UCrop.RESULT_ERROR) {
                    final Throwable cropError = UCrop.getError(data);
                    if (cropError != null) {
                        Log.e(TAG, "handleCropError: ", cropError);
                    } else {
                        Log.e(TAG, "unexpected error: ");
                    }
                }
                break;
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        SDKWrapper.shared().onNewIntent(intent);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        SDKWrapper.shared().onRestart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        SDKWrapper.shared().onStop();
    }

    @Override
    public void onBackPressed() {
        SDKWrapper.shared().onBackPressed();
        super.onBackPressed();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        SDKWrapper.shared().onConfigurationChanged(newConfig);
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        SDKWrapper.shared().onRestoreInstanceState(savedInstanceState);
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        SDKWrapper.shared().onSaveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onStart() {
        SDKWrapper.shared().onStart();
        super.onStart();
    }

    @Override
    public void onLowMemory() {
        SDKWrapper.shared().onLowMemory();
        super.onLowMemory();
    }

    protected void checkGoogleAdId() {
        try {
            Executors.newSingleThreadExecutor().execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        String id = GoogleAdvertisingId.getGAID(getApplicationContext());
                        Log.i(TAG, "google ad id is" + id);
                        DeviceModule.ad_id = id;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 初始化firebase推送
    protected void initFireBasePush() {
        String channelId = GlobalConfig.ChannelId;
        String topic = channelId;
        Log.d("AppActivity：topic", topic);
        SdkManager.setMContextFirebase(this);
        SdkManager.subscribeToTopicFirebase(topic);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            int firebase = bundle.getInt("firebase");
            String message = bundle.getString("message");
            SdkManager.setMessageFirebase(message);
            Log.e(TAG, "onResume:FireBasePushData: " + "firebase:" + firebase + " ----message:" + message);
        }

    }
}
