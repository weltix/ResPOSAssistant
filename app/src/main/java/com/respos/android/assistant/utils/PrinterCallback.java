/*
 * Copyright (c) RESONANCE JSC, Bludov Dmitriy, 28.34.2019
 */

package com.respos.android.assistant.utils;

/**
 * Created by Administrator on 2017/6/12.
 */

public interface PrinterCallback {
    String getResult();

    void onReturnString(String result);
}
