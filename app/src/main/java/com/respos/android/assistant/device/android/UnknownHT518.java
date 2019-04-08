/*
 * Copyright (c) RESONANCE JSC, Bludov Dmitriy, 05.04.2019
 */

package com.respos.android.assistant.device.android;

import android.content.Context;

// Unknown HT518 is handled in ekka.com.ua.respos_*
@Deprecated
public class UnknownHT518 extends AndroidDeviceAbstract {

    public UnknownHT518(Context context) {
        this.context = context;
    }

    @Override
    public void init() {
    }

    @Override
    public void finish() {
    }

    @Override
    public String[] getCOMPortsList() {
        return new String[0];
    }
}
