/*
 * Copyright (c) RESONANCE JSC, Bludov Dmitriy, 14.04.2019
 */

package com.respos.android.assistant.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.respos.android.assistant.Constants;
import com.respos.android.assistant.R;
import com.respos.android.assistant.activity.MainActivity;
import com.respos.android.assistant.device.AndroidDevice;
import com.respos.android.assistant.device.Indicator;
import com.respos.android.assistant.device.android.AnotherAndroidDevice;
import com.respos.android.assistant.device.android.CitaqH14;
import com.respos.android.assistant.device.android.SunmiT1MiniG;

import java.util.List;

import static com.respos.android.assistant.Constants.RESPOS_PACKAGE_NAME;
import static com.respos.android.assistant.device.AndroidDevice.CITAQ_H14;
import static com.respos.android.assistant.device.AndroidDevice.SUNMI_T1MINI_G;
import static com.respos.android.assistant.device.android.AndroidDeviceAbstract.ANDROID_DEVICE_NAME;

public class ResPOSAssistantService extends Service {
    private static final int NOTIFICATION_ID_1 = 001;
    private static final String NOTIFICATION_CHANNEL_ID = "com.respos.android.assistant";
    private AndroidDevice androidDevice = null;
    private Indicator indicator = null;
    public static String resposPackageName = null;

    private final Messenger serviceMessenger = new Messenger(new IncomingHandler());
    private Messenger clientMessenger = null;
    private static final int MSG_RESPOS_MODE = 0;
    private static final int MSG_INDICATOR_LINE_LENGTH = 1;
    private static final int MSG_DATA_TO_INDICATOR = 2;

    private class IncomingHandler extends Handler {
        public void handleMessage(Message msg) {
            Bundle bundle;
            switch (msg.what) {
                case MSG_RESPOS_MODE:
                    bundle = msg.getData();
                    resposPackageName = bundle.getString("respos_mode", "ekka.com.ua.respos_market");
                    androidDevice.init();   // basically initInnerDevices to show logo on LCD Indicator
                    updateResPosAutoBootInfo();
                    break;
                case MSG_INDICATOR_LINE_LENGTH:
                    clientMessenger = msg.replyTo;
                    try {
                        if (indicator != null)
                            clientMessenger.send(Message.obtain(null, MSG_INDICATOR_LINE_LENGTH, indicator.getIndicatorLineLength(), 0));
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    break;
                case MSG_DATA_TO_INDICATOR:
                    bundle = msg.getData();
                    String indicatorData = bundle.getString("data_to_indicator", "");
                    if (indicator != null)
                        indicator.sendDataToIndicator(indicatorData);
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return serviceMessenger.getBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        switch (ANDROID_DEVICE_NAME) {
            case SUNMI_T1MINI_G:
                androidDevice = new SunmiT1MiniG(this);
                break;
            case CITAQ_H14:
                androidDevice = new CitaqH14(this);
                break;
            default:
                androidDevice = new AnotherAndroidDevice(this);
                break;
        }
        androidDevice.init();

        if (androidDevice instanceof Indicator)
            indicator = (Indicator) androidDevice;      // for access to indicator methods in AndroidDevice

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            createNotificationChannel();
        startForeground(NOTIFICATION_ID_1, notificationBuilder().build());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, getString(R.string.notification_id_1_ticker), Toast.LENGTH_LONG).show();

        updateResPosAutoBootInfo();

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (androidDevice != null)
            androidDevice.finish();
    }

    private NotificationCompat.Builder notificationBuilder() {
        Intent intentMainActivity = new Intent(this, MainActivity.class);
        PendingIntent pendingIntentMainActivity =
                PendingIntent.getActivity(this, 0, intentMainActivity, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        builder
                .setContentTitle(this.getString(R.string.app_name))
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(this.getString(R.string.notification_id_1_subtitle)))
                .setTicker(this.getString(R.string.notification_id_1_ticker))
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                .setSmallIcon(R.drawable.ic_notification_id_1)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setContentIntent(pendingIntentMainActivity)
                .setContentText(this.getString(R.string.notification_id_1_subtitle));       // в одну строку
//                .setSubText("")           // мелкий текст под основным текстом уведомления

        return builder;
    }

    //workaround for Android API>=26
    @RequiresApi(Build.VERSION_CODES.O)
    private void createNotificationChannel() {
        String channelName = "My Background Service";
        NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_DEFAULT);
//        chan.setLightColor(Color.BLUE);
//        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null)
            manager.createNotificationChannel(chan);
    }

    private void updateResPosAutoBootInfo() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String savedResposPackageName = sharedPref.getString(Constants.RESPOS_PACKAGE_NAME, null);
        if (savedResposPackageName == null && resposPackageName == null) {
            final PackageManager pm = getPackageManager();
            List<ApplicationInfo> apps = pm.getInstalledApplications(PackageManager.GET_META_DATA);
            for (ApplicationInfo appInfo : apps) {
                if (appInfo.packageName.contains("respos")) {
                    resposPackageName = appInfo.packageName;
                    break;
                }
            }
        }
        if (resposPackageName != null) {
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString(RESPOS_PACKAGE_NAME, resposPackageName);
            editor.commit();
        }
    }
}