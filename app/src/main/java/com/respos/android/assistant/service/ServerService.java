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
import com.respos.android.assistant.network.TCPIPPrintServer;
import com.respos.android.assistant.utils.AidlUtil;

import java.util.List;

import static com.respos.android.assistant.Constants.RESPOS_PACKAGE_NAME;

public class ServerService extends Service {

    public static final int NOTIFICATION_ID_1 = 001;
    public static final String NOTIFICATION_CHANNEL_ID = "com.respos.android.assistant";
    public static TCPIPPrintServer printServer;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            createNotificationChannel();
        startForeground(NOTIFICATION_ID_1, notificationBuilder().build());

        Toast.makeText(this, getString(R.string.notification_id_1_ticker), Toast.LENGTH_LONG).show();

        if (printServer == null) {
            AidlUtil.getInstance().connectPrinterService(this);
            printServer = new TCPIPPrintServer(this);
            printServer.runServer();
        }

        updateResPosAutoBootInfo();

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (printServer != null) {
            printServer.stopServer();
            printServer = null;
        }
        AidlUtil.getInstance().disconnectPrinterService(this);
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