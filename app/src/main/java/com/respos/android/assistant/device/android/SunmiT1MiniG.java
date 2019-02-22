package com.respos.android.assistant.device.android;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.respos.android.assistant.R;
import com.respos.android.assistant.device.Indicator;
import com.respos.android.assistant.device.Printer;
import com.respos.android.assistant.network.TCPIPPrintServer;
import com.respos.android.assistant.service.ResPOSAssistantService;
import com.respos.android.assistant.utils.AidlUtil;
import com.respos.android.assistant.utils.ESCUtil;

public class SunmiT1MiniG extends AndroidDeviceAbstract implements Indicator, Printer {
    private static final int INDICATOR_LINE_LENGTH = 16;

    private static final int SERVER_SOCKET_PORT = 9100;             // arbitrary port for inner printer
    private static final int CLIENT_SOCKET_TIMEOUT = 500;
    private static TCPIPPrintServer printServer = null;      // data to print we get from TCP/IP Server
    private static final byte PRINTER_CHARSET_CP866 = (byte) 17;     // from printer's documentation

    private Bitmap logoLCD;

    public SunmiT1MiniG(Context context) {
        this.context = context;
    }

    @Override
    public void init() {
        AidlUtil.getInstance().connectPrinterService(context);

        if (ResPOSAssistantService.resposPackageName != null) {
            int resID;
            if (ResPOSAssistantService.resposPackageName.contains("market"))
                resID = R.drawable.logo_respos_market_128x40;
            else
                resID = R.drawable.logo_respos_restaurant_128x40;
            logoLCD = BitmapFactory.decodeResource(context.getResources(), resID);
        }

        if (printServer == null) {
            printServer = new TCPIPPrintServer(context, this, SERVER_SOCKET_PORT, CLIENT_SOCKET_TIMEOUT);
            printServer.runServer();
        }
        initInnerDevices(1000);       //show ResPOS logo on indicator
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
    public String sendDataToPrinter(byte[] byteArray) {
        AidlUtil.getInstance().sendRawData(ESCUtil.singleByte());
        AidlUtil.getInstance().sendRawData(ESCUtil.setCodeSystemSingle(PRINTER_CHARSET_CP866));
        AidlUtil.getInstance().sendRawData(byteArray);
        AidlUtil.getInstance().openDrawer();
//        Log.d(TAG, "SunmiT1MiniG.sendDataToPrinter byteArray: " + BytesUtil.getHexStringFromBytes(byteArray));
        return "OK\n";
    }

    @Override
    public void sendDataToIndicator(String str) {
        int lineLength = str.length() / 2;
        String str1 = str.substring(0, lineLength);
        String str2 = str.substring(lineLength, lineLength * 2);
        AidlUtil.getInstance().sendLCDDoubleString(str1, str2);
//        Log.d(TAG, "SunmiT1MiniG.sendDataToIndicator=\"" + str1 + "\"+\"" + str2 + "\" lineLength=" + lineLength);
    }

    @Override
    public int getIndicatorLineLength() {
        return INDICATOR_LINE_LENGTH;
    }

    // it is need some time to connect to AIDL service before init
    public void initInnerDevices(int delayMs) {
        new Thread(() -> {
            try {
                Thread.sleep(delayMs);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            AidlUtil.getInstance().initPrinter();
            AidlUtil.getInstance().initLCD();
            AidlUtil.getInstance().sendLCDBitmap(logoLCD);
        }).start();
    }
}
