package com.respos.android.assistant.device.android;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.respos.android.assistant.R;
import com.respos.android.assistant.network.TCPIPPrintServer;
import com.respos.android.assistant.utils.AidlUtil;
import com.respos.android.assistant.utils.ESCUtil;

public class SunmiT1MiniG extends AndroidDeviceAbstractClass {

    private static final int indicatorLineLenght = 16;
    public static final int SERVER_PORT = 9100;             // классика от Hewlett-Packard для сетевых принтеров
    public static final int clientSocketTimeOut = 500;
    public static final byte CHARSET_CP866 = (byte) 17;     // from printer's documentation
    public static TCPIPPrintServer printServer;

    private Bitmap logoLCD;

    public SunmiT1MiniG(Context context) {
        this.context = context;
        if (true)
            logoLCD = BitmapFactory.decodeResource(context.getResources(), R.drawable.logo_respos_market_128x40);
        else if (false)
            logoLCD = BitmapFactory.decodeResource(context.getResources(), R.drawable.logo_respos_restaurant_128x40);
    }

    @Override
    public void init() {
        AidlUtil.getInstance().connectPrinterService(context);
        printServer = new TCPIPPrintServer(context, SunmiT1MiniG.this, SERVER_PORT, clientSocketTimeOut);
        printServer.runServer();
        sendLCDBitmapWithDelay(logoLCD, 1000);       //show ResPOS logo on indicator
    }

    @Override
    public void sendDataToPrinter(byte[] byteArray) {
        AidlUtil.getInstance().sendRawData(ESCUtil.singleByte());
        AidlUtil.getInstance().sendRawData(ESCUtil.setCodeSystemSingle(CHARSET_CP866));
        AidlUtil.getInstance().sendRawData(byteArray);
        AidlUtil.getInstance().openDrawer();
//        Log.d(TCPIPPrintServer.TAG, "byteArray: " + BytesUtil.getHexStringFromBytes(outputByteArray));
    }

    @Override
    public void finish() {
        if (printServer != null) {
            printServer.stopServer();
            printServer = null;
        }
        AidlUtil.getInstance().sendLCDBitmap(logoLCD);
        AidlUtil.getInstance().sendLCDCommandSleep();
        AidlUtil.getInstance().disconnectPrinterService(context);
    }

    @Override
    public void sendDataToIndicator(String str) {
        int lineLength = str.length() / 2;
        String str1 = str.substring(0, lineLength);
        String str2 = str.substring(lineLength, lineLength * 2);
        AidlUtil.getInstance().sendLCDDoubleString(str1, str2);
        Log.d("python", "SunmiT1MiniG.sendDataToIndicator=\"" + str1 + "\"+\"" + str2 + "\" lineLength=" + lineLength);
    }

    @Override
    public int getIndicatorLineLength() {
        return indicatorLineLenght;
    }

    public void sendLCDBitmapWithDelay(Bitmap bitmap, int delayMs) {
        new Thread(new ShowBitmapWithDelay(bitmap, delayMs)).start();
    }

    // Class shows image with delay, cos it is need some time to connect to AIDL service
    class ShowBitmapWithDelay implements Runnable {
        private Bitmap bitmap;
        private int delayMs;

        public ShowBitmapWithDelay(Bitmap bitmap, int delayMs) {
            this.bitmap = bitmap;
            this.delayMs = delayMs;
        }

        public void run() {
            try {
                Thread.sleep(delayMs);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            AidlUtil.getInstance().sendLCDBitmap(bitmap);
            AidlUtil.getInstance().initPrinter();   // also init printer after AIDL service starting
        }
    }

}
