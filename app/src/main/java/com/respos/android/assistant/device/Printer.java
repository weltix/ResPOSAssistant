package com.respos.android.assistant.device;

public interface Printer extends Device{
    String sendDataToPrinter(byte[] byteArray);
}