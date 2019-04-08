package com.respos.android.assistant.device.android;

import android.content.Context;

import com.respos.android.assistant.device.Indicator;
import com.respos.android.assistant.device.Printer;

// Citaq H14 is handled in ekka.com.ua.respos_*
@Deprecated
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

    @Override
    public String[] getCOMPortsList() {
        return new String[0];
    }
}
