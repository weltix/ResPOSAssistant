package com.respos.android.assistant.device;

public interface AndroidDevice extends Device {
    String SUNMI_T1MINI_G = "SUNMI T1MINI-G";
    String CITAQ_H14 = "CITAQ H14";

    String[] getCOMPortsList();
}
