package com.applib.lib_firebase;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.applib.lib_firebase.R;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static String TAG = "MyFirebaseMessagingService";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Log.d(TAG, "From: " + remoteMessage.getFrom());
        Intent intent = remoteMessage.toIntent();
        if (intent == null) {
            return;
        }

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());

            if (/* Check if data needs to be processed by long running job */ true) {
                scheduleJob();
            } else {
                handleNow();
            }
        }

        Log.d(TAG, "Message data payload: " + remoteMessage.getNotification());
        // Check if message contains a notification payload.
        if (remoteMessage.getData() != null) {
            try {
                JSONObject object = new JSONObject(remoteMessage.getData());
                String title = remoteMessage.getNotification().getTitle();
                String body = remoteMessage.getNotification().getBody();
                String image = "";
                String bg = "";
                int jump_type = 0;
                String push_id = "";

                if (object.has("image")) {
                    image = object.getString("image");
                }
                if (object.has("bg")) {
                    bg = object.getString("bg");
                }
                if (object.has("jump_type")) {
                    jump_type = object.getInt("jump_type");
                }
                if (object.has("push_id")) {
                    push_id = object.getString("push_id");
                }

                Bundle bundle = new Bundle();
                bundle.putString("message",object.toString());
                bundle.putInt("firebase",1);
                Log.d(TAG, "Message data payload: " + body + ", image=" + image + ", title=" + title + ", bg=" + bg + ", jump_type="+jump_type + ", push_id="+push_id + "");
                sendNotification(title, body, image, bg, intent,bundle);

                FirebaseManager.jump_type = jump_type;
                FirebaseManager.push_id = push_id;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
//        if (remoteMessage.getNotification()!=null) {
//            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody() + ",title=" + remoteMessage.getNotification().getTitle());
//            sendNotification(remoteMessage);
//        }
    }

    @Override
    public void onNewToken(String token) {
        Log.d(TAG, "Refershed token: " + token);
        sendRegistrationToServer(token);
    }

    private void scheduleJob() {

    }

    private void handleNow() {
        Log.d(TAG, "Short lived task is done.");
    }

    private void sendRegistrationToServer(String token) {
        // TODO: Implement this method to send token to your app server
    }

    private void sendNotification(String titleStr, String bodyStr, String image, String bg, Intent intent,Bundle bundle) {

        //通知点击跳转
//        if (TextUtils.isEmpty(notification.getClickAction())) {
            Intent launchIntent = getPackageManager().getLaunchIntentForPackage(getPackageName());
            if (launchIntent == null) {
                return;
            } else {
                launchIntent.putExtras(intent);
                intent = launchIntent;
            }
//        } else {
//            intent.setAction(notification.getClickAction());
//            intent.addCategory(Intent.CATEGORY_DEFAULT);
//        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtras(bundle);
        final PendingIntent pendingIntent = PendingIntent.getActivity(MyFirebaseMessagingService.this, 0 /* Request code */, intent, PendingIntent.FLAG_IMMUTABLE);

        //注册通知渠道
        final String channelId = getString(R.string.channel_id);
        final Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);  //通知声音
        final String title = titleStr;//标题
        final String body = bodyStr;//内容
        //加载网络图片
        if (bg.length() > 0) { // 有背景图片
            final Uri imageUrl = Uri.parse(bg);      //网络图片
            final RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.notification_layout);
            Glide.with(MyFirebaseMessagingService.this)
                    .asBitmap()
                    .load(imageUrl)
                    .addListener(new RequestListener<Bitmap>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                            showNotification(remoteViews, channelId, title, body, null, defaultSoundUri, pendingIntent);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                            showNotification(remoteViews, channelId, title, body, resource, defaultSoundUri, pendingIntent);
                            return false;
                        }
                    }).submit();
        } else {
            if (image.length() > 0) {
                final Uri imageUrl = Uri.parse(image);      //网络图片
                Glide.with(MyFirebaseMessagingService.this)
                        .asBitmap()
                        .load(imageUrl)
                        .addListener(new RequestListener<Bitmap>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                                showNotification2(channelId, title, body, defaultSoundUri, null, pendingIntent);
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                                showNotification2(channelId, title, body, defaultSoundUri, resource, pendingIntent);
                                return false;
                            }
                        }).submit();
            } else {
                showNotification2(channelId, title, body, defaultSoundUri, null, pendingIntent);
            }
        }
    }

    private void showNotification2(String channelId, String messageTitle, String messageBody, Uri defaultSoundUri, Bitmap image, PendingIntent pendingIntent) {
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.drawable.firebase_icon)
                        .setContentTitle( Html.fromHtml(messageTitle))
                        .setContentText(Html.fromHtml(messageBody))
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);
        if (image != null) {
            builder.setLargeIcon(image);
        }

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(channelId, getString(R.string.app_name), NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.enableVibration(true);
            notificationChannel.setVibrationPattern(new long[]{500});
            manager.createNotificationChannel(notificationChannel);
        }
        manager.notify((int) System.currentTimeMillis() /* ID of notification */, builder.build());

    }

    private void showNotification(RemoteViews remoteViews, String channelId, String title, String msg, Bitmap image, Uri defaultSoundUri, PendingIntent pendingIntent) {
        if (image != null){
            remoteViews.setImageViewBitmap(R.id.push_bg, image);
        }
        remoteViews.setTextViewText(R.id.push_title, Html.fromHtml(title));
        remoteViews.setTextViewText(R.id.push_content, Html.fromHtml(msg));
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(MyFirebaseMessagingService.this, channelId)
                        .setSmallIcon(R.drawable.firebase_icon)
                        .setContentTitle(title)
                        .setContentText(msg)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContent(remoteViews)
                        .setContentIntent(pendingIntent);

//        if (image == null) {
//            builder.setStyle(new NotificationCompat.BigTextStyle()
//                    .bigText(msg));
//        } else {
//            //设置大图标，这样通知收起时大图标就可以用来显示图片
//            builder.setLargeIcon(image)
//                    .setStyle(//设置为大图样式
//                            new NotificationCompat.BigPictureStyle()
//                                    .setBigContentTitle(title)//这里如果设置通知标题，在华为荣耀8上会直接覆盖通知的标题设置，而三星5.0手机则在通知展开时替换为这个文本，收起时恢复通知标题，好在这里不需要有两个标题。。。
//                                    .setSummaryText(msg)//三星5.0手机如果不设置，内容文本会变成空的。。。
//                                    .bigPicture(image)
//                                    .bigLargeIcon(null)//显示大图时，通知的大图标为空(三星5.0手机不起作用)
//                    );
//        }
        NotificationManager manager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(channelId, getString(R.string.app_name), NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.enableVibration(true);
            notificationChannel.setVibrationPattern(new long[]{500});
            manager.createNotificationChannel(notificationChannel);
        }
        manager.notify((int) System.currentTimeMillis() /* ID of notification */, builder.build());

    }
}
