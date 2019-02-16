package com.respos.android.assistant.device.android;

import android.content.Context;

public class AnotherAndroidDevice extends AndroidDeviceAbstractClass {

    private int indicatorLineLenght = 20;       //default value for any device

    private Context context;

    public AnotherAndroidDevice(Context context) {
        this.context = context;
    }

    @Override
    public void init() {
    }

    @Override
    public void finish() {
    }

    @Override
    public void sendDataToIndicator(String str) {
    }

    @Override
    public int getIndicatorLineLength() {
        return indicatorLineLenght;
    }
    
}
