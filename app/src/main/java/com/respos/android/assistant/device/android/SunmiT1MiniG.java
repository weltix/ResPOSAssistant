package com.respos.android.assistant.device.android;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.respos.android.assistant.utils.AidlUtil;

public class SunmiT1MiniG extends AndroidDeviceAbstractClass {

    public static final byte CHARSET_CP866 = (byte) 17;     // from printer's documentation
    private static final int indicatorLineLenght = 16;

    private Context context;

    private Bitmap logoLCD;

    public SunmiT1MiniG(Context context) {
        this.context = context;
     //   logoLCD = getResources.getBitmap("logo_respos_market_128x40", "drawable");
	//    logoLCD = getBitmap("logo_respos_restaurant_128x40", "drawable");
    }

    @Override
    public void init() {
        AidlUtil.getInstance().connectPrinterService(context);
        sendLCDBitmapWithDelay(logoLCD, 1000);       //show ResPOS logo on indicator
	Log.d("python", "SunmiT1MiniG.connectPrinterService (AIDL)");
    }

    @Override
    public void finish() {
        AidlUtil.getInstance().sendLCDBitmap(logoLCD);
        AidlUtil.getInstance().sendLCDCommandSleep();
        AidlUtil.getInstance().disconnectPrinterService(context);
	Log.d("python", "SunmiT1MiniG.disconnectPrinterService (AIDL)");
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
        }
    }

}
