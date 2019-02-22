package com.respos.android.assistant.device.android;

import android.content.Context;

import com.respos.android.assistant.device.Indicator;
import com.respos.android.assistant.device.Printer;

public class CitaqH14 extends AndroidDeviceAbstract implements Indicator, Printer {
    private static final int INDICATOR_LINE_LENGTH = 30;

    public CitaqH14(Context context) {
        this.context = context;
    }

    @Override
    public void init() {
    }

    @Override
    public void finish() {
    }

    @Override
    public String sendDataToPrinter(byte[] byteArray) {
        return "\n";
    }

    @Override
    public void sendDataToIndicator(String str) {
    }

    @Override
    public int getIndicatorLineLength() {
        return INDICATOR_LINE_LENGTH;
    }

}
