package com.cocos.game;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.util.Log;
import android.view.WindowManager;

import androidx.core.content.FileProvider;

import com.cocos.lib.CocosHelper;
import com.cocos.lib.CocosReflectionHelper;
import com.cocos.lib.JsbBridge;
import com.cocos.service.SDKWrapper;
import com.applib.lib_common.ApiCallback;
import com.applib.lib_sdkmgr.SdkManager;
import com.tbruyelle.rxpermissions2.RxPermissions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.lang.reflect.Method;
import java.util.List;

public class DeviceModule {

    private static final String TAG = "AndroidDevice";

    public static String ad_id = "";

    private static String uuid = "";

    public  static String cropOptions = null;

    private static RxPermissions rxPermissions;

    private static Context _getContext() {
        return SDKWrapper.shared().getActivity().getBaseContext();
    }

    private static RxPermissions getPermissionRequest() {
        if (rxPermissions == null) {
            rxPermissions = new RxPermissions(SDKWrapper.shared().getActivity());
        }
        return rxPermissions;
    }

    public static String getDeviceName() {
        return Build.BRAND + "|" + Build.MODEL;
    }

    // 必须先申请READ_EXTERNAL_STORAGE权限
    public static String getDeviceUuid() {
        if (uuid != "") {
            return uuid;
        }

        try {
            uuid = GuidUtil.createGUID(_getContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return uuid;
    }

    public static String getDeviceAdid() {
        do {
            if (!ad_id.isEmpty()) {
                break;
            }

            if (GlobalConfig.HasAppsFlyer) {
                ad_id = SdkManager.getAdidAppsFlyer();
                if (!ad_id.isEmpty()) {
                    break;
                }
            }

            if (GlobalConfig.HasAdjust) {
                ad_id = SdkManager.getAdidAdjust();
                if (!ad_id.isEmpty()) {
                    break;
                }
            }

        } while (false);

        return ad_id;
    }

    // 检测网络强度
    public static int getNetworkStrength() {
        // TODO 不同的机型有不同的问题 WIFI的比较稳定 暂时不获取了
        return 0;
    }

    public static void vibrate(float duration) {
        try {
            Vibrator sVibrateService = (Vibrator) _getContext().getSystemService(Context.VIBRATOR_SERVICE);
            if (sVibrateService != null && sVibrateService.hasVibrator()) {
                if (android.os.Build.VERSION.SDK_INT >= 26) {
                    Class<?> vibrationEffectClass = Class.forName("android.os.VibrationEffect");
                    if (vibrationEffectClass != null) {
                        final int DEFAULT_AMPLITUDE = CocosReflectionHelper.<Integer>getConstantValue(vibrationEffectClass,
                                "DEFAULT_AMPLITUDE");
                        //VibrationEffect.createOneShot(long milliseconds, int amplitude)
                        final Method method = vibrationEffectClass.getMethod("createOneShot",
                                new Class[]{Long.TYPE, Integer.TYPE});
                        Class<?> type = method.getReturnType();

                        Object effect = method.invoke(vibrationEffectClass,
                                new Object[]{(long) (duration * 1000), DEFAULT_AMPLITUDE});
                        //sVibrateService.vibrate(VibrationEffect effect);
                        CocosReflectionHelper.invokeInstanceMethod(sVibrateService, "vibrate",
                                new Class[]{type}, new Object[]{(effect)});
                    }
                } else {
                    sVibrateService.vibrate((long) (duration * 1000));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setKeepScreenOn(boolean keepScreenOn) {
        SDKWrapper.shared().getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (keepScreenOn) {
                    SDKWrapper.shared().getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                } else {
                    SDKWrapper.shared().getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                }
            }
        });
    }

    public static void shareText(String text, String packageName) {
        SDKWrapper.shared().getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_SEND);
                intent.putExtra(Intent.EXTRA_TEXT, text);
                intent.setType("text/plain");
                if (!packageName.isEmpty()) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setPackage(packageName);
                }
                SDKWrapper.shared().getActivity().startActivityForResult(Intent.createChooser(intent, ""), 1000);
            }
        });
    }

    public static void shareImage(String path, String packageName) {
        SDKWrapper.shared().getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    Intent intent = new Intent();
                    if (!packageName.isEmpty()) {
                        intent.setPackage(packageName);
                    }
                    intent.setAction(Intent.ACTION_SEND);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra(Intent.EXTRA_STREAM, getFileProvider(_getContext(), new File(path)));
                    intent.setType("image/*");   //分享文件
                    SDKWrapper.shared().getActivity().startActivityForResult(Intent.createChooser(intent, ""), 1000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public static void shareVideo(String path, String packageName) {
        SDKWrapper.shared().getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    Intent intent = new Intent();
                    if (!packageName.isEmpty()) {
                        intent.setPackage(packageName);
                    }
                    intent.setAction(Intent.ACTION_SEND);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra(Intent.EXTRA_STREAM, getFileProvider(_getContext(), new File(path)));
                    intent.setType("video/*");   //分享文件
                    SDKWrapper.shared().getActivity().startActivityForResult(Intent.createChooser(intent, ""), 1000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public static void shareFile(String path, String packageName) {
        SDKWrapper.shared().getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    Intent intent = new Intent();
                    if (!packageName.isEmpty()) {
                        intent.setPackage(packageName);
                    }
                    intent.setAction(Intent.ACTION_SEND);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra(Intent.EXTRA_STREAM, getFileProvider(_getContext(), new File(path)));
                    intent.setType("*/*");   //分享文件
                    SDKWrapper.shared().getActivity().startActivityForResult(Intent.createChooser(intent, ""), 1000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public static boolean checkAppInstalled(String packageName) {
        if (packageName.isEmpty()) {
            return false;
        }

        final PackageManager packageManager = _getContext().getPackageManager();// 获取packagemanager
        List<PackageInfo> packageInfos = packageManager.getInstalledPackages(0);// 获取所有已安装程序的包信息
        if (packageInfos != null) {
            for (int i = 0; i < packageInfos.size(); i++) {
                String pn = packageInfos.get(i).packageName;
                if (pn.equals(packageName)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 保存图片到相册
     * @param fullPathForFilename
     * @returns
     */
    public static boolean saveImageToAlbum(String fullPathForFilename) {
        SDKWrapper.shared().getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                getPermissionRequest().request(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE})
                        .subscribe(granted -> {
                            //授权再次进行定位
                            if (granted) {
                                AlbumUtils.saveImgFileToAlbum(_getContext(), fullPathForFilename);
                                //Toast.makeText(_getContext(), "保存成功！", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        return true;
    }

    public static boolean selectImageFromAlbum(String jsonStr) {
        SDKWrapper.shared().getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                getPermissionRequest().request(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE})
                        .subscribe(granted -> {
                            //授权再次进行定位
                            if (granted) {
                                cropOptions = jsonStr;
                                Intent intent = new Intent();
                                intent.setAction(Intent.ACTION_PICK);
                                intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                                SDKWrapper.shared().getActivity().startActivityForResult(intent, 100);
                            }
                        });
            }
        });

        return true;
    }

    public static boolean changeOrientation(int orientation) {
        if (orientation == 0) {
            SDKWrapper.shared().getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        } else {
            SDKWrapper.shared().getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        }

        return true;
    }

    public static boolean hasFacebook() {
        return GlobalConfig.HasFacebook;
    }

    public static boolean doFacebookLogin() {
        if (!GlobalConfig.HasFacebook) {
            return false;
        }

        try {
            SdkManager.doLoginFacebook(SDKWrapper.shared().getActivity(), new ApiCallback() {
                @Override
                public void onSuccess(final String code) {
                    CocosHelper.runOnGameThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                JSONObject jsonObject = new JSONObject();
                                jsonObject.put("success", true);
                                jsonObject.put("token", code);
                                JsbBridge.sendToScript("FacebookLogin", jsonObject.toString());
                            } catch (JSONException ex) {
                                // 键为null或使用json不支持的数字格式(NaN, infinities)
                                Log.e(TAG, "json object exception:" + ex.getMessage());
                            }

                        }
                    });
                }

                @Override
                public void onFail(String code) {
                    CocosHelper.runOnGameThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                JSONObject jsonObject = new JSONObject();
                                jsonObject.put("success", false);
                                jsonObject.put("msg", code);
                                JsbBridge.sendToScript("FacebookLogin", jsonObject.toString());
                            } catch (JSONException ex) {
                                // 键为null或使用json不支持的数字格式(NaN, infinities)
                                Log.e(TAG, "json object exception:" + ex.getMessage());
                            }

                        }
                    });
                }
            });
        } catch (Exception e) {
            return false;
        }

        return true;
    }

    public static boolean hasFirebase() {
        return GlobalConfig.HasFirebase;
    }

    public static boolean trackEventFirebase(String eventData) {
        if (!GlobalConfig.HasFirebase) {
            return false;
        }
        SDKWrapper.shared().getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    SdkManager.trackEventFirebase(eventData);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        return true;
    }

    public static boolean requestNotificationPermission() {
        // This is only necessary for API level >= 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            SDKWrapper.shared().getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    getPermissionRequest().request(new String[]{Manifest.permission.POST_NOTIFICATIONS})
                            .subscribe(granted -> {
                                //授权再次进行定位
                                if (granted) {
                                    //getTokenFirebase();
                                }
                            });
                }
            });
            return true;
        }
        return false;
    }

    public static boolean requestPermissions(String jsonArrayPermissions) {
        try {
            JSONArray jsonArray = new JSONArray(jsonArrayPermissions);
            String[] permissions = new String[jsonArray.length()];
            for (int i = 0; i < jsonArray.length(); i++) {
                permissions[i] = jsonArray.optString(i);
            }
            if (jsonArray.length() > 0) {
                SDKWrapper.shared().getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        getPermissionRequest().request(permissions)
                                .subscribe(granted -> {
                                    //授权再次进行定位
                                    if (granted) {
                                    }
                                });
                    }
                });
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return true;
    }

    public static boolean getTokenFirebase() {
        if (!GlobalConfig.HasFirebase) {
            return false;
        }
        SDKWrapper.shared().getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    SdkManager.getTokenFirebase(new ApiCallback() {
                        @Override
                        public void onSuccess(String code) {
                            CocosHelper.runOnGameThread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        JSONObject jsonObject = new JSONObject();
                                        jsonObject.put("token", code);
                                        JsbBridge.sendToScript("FirebaseToken", jsonObject.toString());
                                    } catch (JSONException ex) {
                                        // 键为null或使用json不支持的数字格式(NaN, infinities)
                                        Log.e(TAG, "json object exception:" + ex.getMessage());
                                    }
                                }
                            });
                        }

                        @Override
                        public void onFail(String code) {

                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        return true;
    }

    // 可能只使用推送功能
    public static void setAnalyticsCollectionEnabledFirebase(boolean enabled) {
        if (!GlobalConfig.HasFirebase) {
            return;
        }
        try {
            SdkManager.setAnalyticsCollectionEnabledFirebase(enabled);

        } catch (Exception e) {

        }
    }

    public static String getMessageFirebase() {
        if (!GlobalConfig.HasFirebase) {
            return "";
        }
        String message = "";
        try {
            message = SdkManager.getMessageFirebase();
        } catch (Exception e) {

        }
        return message;
    }

    public static boolean hasAdjust() {
        return GlobalConfig.HasAdjust;
    }

    public static boolean trackEventAdjust(String eventData) {
        if (!GlobalConfig.HasAdjust) {
            return false;
        }
        SDKWrapper.shared().getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    SdkManager.trackEventAdjust(eventData);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        return true;
    }

    // 将File 转化为 content://URI
    private static Uri getFileProvider(Context context, File file) {
        // ‘authority’要与`AndroidManifest.xml`中`provider`配置的`authorities`一致，假设你的应用包名为com.example.app
        //String authority = context.getPackageName() + "com.cocos.game.fileprovider";
        String authority = "com.cocos.game.fileProvider";
        Uri contentUri = FileProvider.getUriForFile(context, authority, file);
        return contentUri;
    }

}
