/*
 * Copyright (c) RESONANCE JSC, Bludov Dmitriy, 18.18.2019
 */

package com.respos.android.assistant;

public final class Constants {
    // don't allow the class to be instantiated
    private Constants() {
    }

    public static final String RESPOS_PACKAGE_NAME = "ResPOS package name";
    public static final String TAG = "respos_assistant";
    // next sequence of bytes ResPOS sends to open Cash Drawer. Sequence contains two slightly different commands.
    public static final byte[] OPEN_DRAWER_TWO_TIMES = {0x10, 0x14, 0x01, 0x00, 0x05, 0x10, 0x14, 0x01, 0x01, 0x05};
}