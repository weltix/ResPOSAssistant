package com.respos.android.assistant.device.android;

import android.content.Context;
import android.os.Build;

import com.respos.android.assistant.device.AndroidDevice;

public abstract class AndroidDeviceAbstract implements AndroidDevice {
    public static final String ANDROID_DEVICE_NAME = AndroidDeviceAbstract.getDeviceName().toUpperCase();

    private static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.toLowerCase().startsWith(manufacturer.toLowerCase())) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }

    private static String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }

    Context context;
}