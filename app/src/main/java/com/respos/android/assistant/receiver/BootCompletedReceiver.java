/*
 * Copyright (c) RESONANCE JSC, Bludov Dmitriy, 14.01.2019
 */

package com.respos.android.assistant.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.respos.android.assistant.Constants;
import com.respos.android.assistant.service.ResPOSAssistantService;

public class BootCompletedReceiver extends BroadcastReceiver {

    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Intent pushIntent = new Intent(context, ResPOSAssistantService.class);
            context.startService(pushIntent);

            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
            String resposPackageName = sharedPref.getString(Constants.RESPOS_PACKAGE_NAME, "ekka.com.ua.respos_market");
            Intent launchIntent = context
                    .getPackageManager()
                    .getLaunchIntentForPackage(resposPackageName);
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(launchIntent);
            }
        }
    }
}