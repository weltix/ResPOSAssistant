package com.respos.android.assistant.device;

public interface Indicator {
    void sendDataToIndicator(String string);

    int getIndicatorLineLength();
}