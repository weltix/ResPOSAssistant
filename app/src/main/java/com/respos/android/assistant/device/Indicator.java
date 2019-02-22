package com.respos.android.assistant.device;

public interface Indicator extends Device{
    void sendDataToIndicator(String string);

    int getIndicatorLineLength();
}