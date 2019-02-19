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
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.respos.android.assistant.R;
import com.respos.android.assistant.activity.MainActivity;
import com.respos.android.assistant.device.android.AndroidDeviceAbstractClass;
import com.respos.android.assistant.device.android.AnotherAndroidDevice;
import com.respos.android.assistant.device.android.CitaqH14;
import com.respos.android.assistant.device.android.SunmiT1MiniG;

import java.util.List;

import static com.respos.android.assistant.Constants.RESPOS_PACKAGE_NAME;
import static com.respos.android.assistant.device.android.AndroidDeviceAbstractClass.ANDROID_DEVICE_NAME;
import static com.respos.android.assistant.device.android.AndroidDeviceAbstractClass.CITAQ_H14;
import static com.respos.android.assistant.device.android.AndroidDeviceAbstractClass.SUNMI_T1MINI_G;

public class ResPOSAssistantService extends Service {
    private static final int NOTIFICATION_ID_1 = 001;
    private static final String NOTIFICATION_CHANNEL_ID = "com.respos.android.assistant";
    private static AndroidDeviceAbstractClass androidDevice = null;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            createNotificationChannel();
        startForeground(NOTIFICATION_ID_1, notificationBuilder().build());

        if (androidDevice == null) {
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
        }

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
        SharedPreferences.Editor editor = sharedPref.edit();

        final PackageManager pm = getPackageManager();
        List<ApplicationInfo> apps = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        for (ApplicationInfo appInfo : apps) {
            if (appInfo.packageName.contains("respos")) {
                editor.putString(RESPOS_PACKAGE_NAME, appInfo.packageName);
                editor.commit();
                break;
            }
        }
    }
}