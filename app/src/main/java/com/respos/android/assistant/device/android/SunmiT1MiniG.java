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
import com.respos.android.assistant.utils.BitmapMaker;
import com.respos.android.assistant.utils.ESCUtil;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import static android.graphics.Bitmap.Config.ARGB_8888;
import static com.respos.android.assistant.Constants.OPEN_DRAWER_TWO_TIMES;

public class SunmiT1MiniG extends AndroidDeviceAbstract implements Indicator, Printer {
    private static final int INDICATOR_LINE_LENGTH = 16;    // the best arbitrary number of chars in one line
    private static final int INDICATOR_CHAR_WIDTH = 8;      // the best arbitrary width of one char (pixels)
    private static final int INDICATOR_WIDTH = 128;  // hardware physical dimension (pixels)
    private static final int INDICATOR_HEIGHT = 40;  // hardware physical dimension (pixels)
    private static final boolean SHRINKABLE_IMAGE = false;    // image size for indicator is always stable and must be not less

    private static final int SERVER_SOCKET_PORT = 9100;             // arbitrary port for inner printer
    private static final int CLIENT_SOCKET_TIMEOUT = 500;           // arbitrary value sufficient for given case
    private static TCPIPPrintServer printServer = null;             // data to print we get from this TCP/IP Server
    private static final byte PRINTER_CHARSET_CP866 = (byte) 17;    // from printer's documentation

    private Bitmap logoLCD;
    private BitmapMaker indicatorBitmapMaker;

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

        Bitmap charactersCP866Bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.characters_cp866);
        indicatorBitmapMaker = new BitmapMaker(charactersCP866Bitmap, INDICATOR_CHAR_WIDTH);

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

        if (Arrays.equals(byteArray, OPEN_DRAWER_TWO_TIMES))
            AidlUtil.getInstance().openDrawer();
        else {
            AidlUtil.getInstance().sendRawData(byteArray);
        }
//        Log.d(TAG, "SunmiT1MiniG.sendDataToPrinter byteArray: " + BytesUtil.getHexStringFromBytes(byteArray));
        return "OK\n";
    }

    @Override
    public void sendDataToIndicator(String str) {
        byte[] bytesData = new byte[str.length()];
        try {
            bytesData = str.getBytes("CP866");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        // TODO: 04.03.2019 Картинку должна быть возможность в будущем только уменьшить, не нарушив при этом минимум
        // TODO: 04.03.2019 Если надо запас по высоте, то сразу посылать с запасом отсюда
        Bitmap initialBitmap = Bitmap.createBitmap(INDICATOR_WIDTH, INDICATOR_HEIGHT, ARGB_8888);
        Bitmap outputBitmap = indicatorBitmapMaker.getOutputBitmap(initialBitmap, bytesData, SHRINKABLE_IMAGE);

        AidlUtil.getInstance().sendLCDBitmap(outputBitmap);
/*        int lineLength = str.length() / 2;
        String str1 = str.substring(0, lineLength);
        String str2 = str.substring(lineLength, lineLength * 2);
        Log.d(TAG, "SunmiT1MiniG.sendDataToIndicator=\"" + str1 + "\"+\"" + str2 + "\" lineLength=" + lineLength);
        AidlUtil.getInstance().sendLCDDoubleString(str1, str2); */
    }


    @Override
    public int getIndicatorLineLength() {
        return INDICATOR_WIDTH / INDICATOR_CHAR_WIDTH;  // must be equal to INDICATOR_LINE_LENGTH
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
