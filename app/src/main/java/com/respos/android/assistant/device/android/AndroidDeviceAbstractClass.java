package com.respos.android.assistant.device.android;

import android.os.Build;

public abstract class AndroidDeviceAbstractClass {

    public static final String SUNMI_T1MINI_G = "SUNMI T1MINI-G";

    public static final String ANDROID_DEVICE_NAME = AndroidDeviceAbstractClass.getDeviceName().toUpperCase();
    public static final String ANDROID_DEVICE_MANUFACTURER = AndroidDeviceAbstractClass.getDeviceManufacturer().toUpperCase();
    public static final String ANDROID_DEVICE_MODEL = AndroidDeviceAbstractClass.getDeviceModel().toUpperCase();

    public abstract void init();

    public abstract void finish();

    public abstract void sendDataToIndicator(String str);

    public abstract int getIndicatorLineLength();

    private static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.toLowerCase().startsWith(manufacturer.toLowerCase())) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }

    private static String getDeviceManufacturer() {
        String manufacturer = Build.MANUFACTURER;
        return capitalize(manufacturer);
    }

    private static String getDeviceModel() {
        String model = Build.MODEL;
        return capitalize(model);
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
}
