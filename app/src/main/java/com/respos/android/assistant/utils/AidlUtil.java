/*
 *  Changed RESONANCE JSC, Bludov Dmitriy
 *  private void callToastPrinterUnavailable();    13.08.2019
 *  public void openDrawer();    21.01.2019
 */

package com.respos.android.assistant.utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import com.respos.android.assistant.R;
import com.respos.android.assistant.bean.TableItem;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import woyou.aidlservice.jiuiv5.ICallback;
import woyou.aidlservice.jiuiv5.IWoyouService;


public class AidlUtil {
    private static final String SERVICE＿PACKAGE = "woyou.aidlservice.jiuiv5";
    private static final String SERVICE＿ACTION = "woyou.aidlservice.jiuiv5.IWoyouService";

    private IWoyouService woyouService;
    private static AidlUtil mAidlUtil = new AidlUtil();
    private Context context;

    private AidlUtil() {
    }

    public static AidlUtil getInstance() {
        return mAidlUtil;
    }

    /**
     * Connection service
     *
     * @param context context
     */
    public void connectPrinterService(Context context) {
        this.context = context.getApplicationContext();
        Intent intent = new Intent();
        intent.setPackage(SERVICE＿PACKAGE);
        intent.setAction(SERVICE＿ACTION);
        context.getApplicationContext().startService(intent);
        context.getApplicationContext().bindService(intent, connService, Context.BIND_AUTO_CREATE);
    }

    /**
     * Disconnect service
     *
     * @param context context
     */
    public void disconnectPrinterService(Context context) {
        if (woyouService != null) {
            context.getApplicationContext().unbindService(connService);
            woyouService = null;
        }
    }

    public boolean isConnect() {
        return woyouService != null;
    }

    private ServiceConnection connService = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            woyouService = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            woyouService = IWoyouService.Stub.asInterface(service);
        }
    };

    public ICallback generateCB(final PrinterCallback printerCallback) {
        return new ICallback.Stub() {

            @Override
            public void onRunResult(boolean isSuccess) throws RemoteException {

            }

            @Override
            public void onReturnString(String result) throws RemoteException {

            }

            @Override
            public void onRaiseException(int code, String msg) throws RemoteException {

            }

            @Override
            public void onPrintResult(int code, String msg) throws RemoteException {

            }
        };
    }

    /**
     * Set the print density
     */
    private int[] darkness = new int[]{0x0600, 0x0500, 0x0400, 0x0300, 0x0200, 0x0100, 0,
            0xffff, 0xfeff, 0xfdff, 0xfcff, 0xfbff, 0xfaff};

    public void setDarkness(int index) {
        if (woyouService == null) {
            callToastPrinterUnavailable();
            return;
        }

        int k = darkness[index];
        try {
            woyouService.sendRAWData(ESCUtil.setPrinterDarkness(k), null);
            woyouService.printerSelfChecking(null);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get printer system information, put it in the list
     *
     * @return list
     */
    public List<String> getPrinterInfo() {
        if (woyouService == null) {
            callToastPrinterUnavailable();
            return null;
        }

        List<String> info = new ArrayList<>();
        try {
            info.add(woyouService.getPrinterSerialNo());
            info.add(woyouService.getPrinterModal());
            info.add(woyouService.getPrinterVersion());
            info.add(woyouService.getPrintedLength() + "");
            info.add("");
            //info.add(woyouService.getServiceVersion());
            PackageManager packageManager = context.getPackageManager();
            try {
                PackageInfo packageInfo = packageManager.getPackageInfo(SERVICE＿PACKAGE, 0);
                if (packageInfo != null) {
                    info.add(packageInfo.versionName);
                    info.add(packageInfo.versionCode + "");
                } else {
                    info.add("");
                    info.add("");
                }
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }

        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return info;
    }

    /**
     * Initialize the printer
     */
    public void initPrinter() {
        if (woyouService == null) {
            callToastPrinterUnavailable();
            return;
        }

        try {
            woyouService.printerInit(null);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * Print QR code
     */
    public void printQr(String data, int modulesize, int errorlevel) {
        if (woyouService == null) {
            callToastPrinterUnavailable();
            return;
        }


        try {
            woyouService.setAlignment(1, null);
            woyouService.printQRCode(data, modulesize, errorlevel, null);
            woyouService.lineWrap(3, null);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * Print barcode
     */
    public void printBarCode(String data, int symbology, int height, int width, int textposition) {
        if (woyouService == null) {
            callToastPrinterUnavailable();
            return;
        }


        try {
            woyouService.printBarCode(data, symbology, height, width, textposition, null);
            woyouService.lineWrap(3, null);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * Print text
     */
    public void printText(String content, float size, boolean isBold, boolean isUnderLine) {
        if (woyouService == null) {
            callToastPrinterUnavailable();
            return;
        }

        try {
            if (isBold) {
                woyouService.sendRAWData(ESCUtil.boldOn(), null);
            } else {
                woyouService.sendRAWData(ESCUtil.boldOff(), null);
            }

            if (isUnderLine) {
                woyouService.sendRAWData(ESCUtil.underlineWithOneDotWidthOn(), null);
            } else {
                woyouService.sendRAWData(ESCUtil.underlineOff(), null);
            }

            woyouService.printTextWithFont(content, null, size, null);
            woyouService.lineWrap(3, null);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }

    /*
     * Print picture
     */
    public void printBitmap(Bitmap bitmap) {
        if (woyouService == null) {
            callToastPrinterUnavailable();
            return;
        }

        try {
            woyouService.setAlignment(1, null);
            woyouService.printBitmap(bitmap, null);
            woyouService.lineWrap(3, null);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * Print pictures and text in the specified order
     */
    public void printBitmap(Bitmap bitmap, int orientation) {
        if (woyouService == null) {
            callToastPrinterUnavailable();
            return;
        }

        try {
            if (orientation == 0) {
                woyouService.printBitmap(bitmap, null);
                woyouService.printText("Horizontal arrangement\n", null);
                woyouService.printBitmap(bitmap, null);
                woyouService.printText("Horizontal arrangement\n", null);
            } else {
                woyouService.printBitmap(bitmap, null);
                woyouService.printText("\nVertical arrangement\n", null);
                woyouService.printBitmap(bitmap, null);
                woyouService.printText("\nVertical arrangement\n", null);
            }
            woyouService.lineWrap(3, null);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }


    /**
     * Print form
     */
    public void printTable(LinkedList<TableItem> list) {
        if (woyouService == null) {
            callToastPrinterUnavailable();
            return;
        }

        try {
            for (TableItem tableItem : list) {
                Log.i("kaltin", "printTable: " + tableItem.getText()[0] + tableItem.getText()[1] + tableItem.getText()[2]);
                woyouService.printColumnsText(tableItem.getText(), tableItem.getWidth(), tableItem.getAlign(), null);
            }
            woyouService.lineWrap(3, null);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /*
     * Free three lines!
     */
    public void print3Line() {
        if (woyouService == null) {
            callToastPrinterUnavailable();
            return;
        }

        try {
            woyouService.lineWrap(3, null);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void sendRawData(byte[] data) {
        if (woyouService == null) {
            callToastPrinterUnavailable();
            return;
        }

        try {
            woyouService.sendRAWData(data, null);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    //Get the current print mode
    public int getPrintMode() {
        if (woyouService == null) {
            callToastPrinterUnavailable();
            return -1;
        }

        int res;
        try {
            res = woyouService.getPrinterMode();
        } catch (RemoteException e) {
            e.printStackTrace();
            res = -1;
        }
        return res;
    }

    /*
     * Open Cash Drawer
     */
    public void openDrawer() {
        if (woyouService == null) {
            callToastPrinterUnavailable();
            return;
        }

        try {
            woyouService.openDrawer(null);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }


    public void initLCD() {
        if (woyouService == null) {
            callToastIndicatorUnavailable();
            return;
        }

        try {
            woyouService.sendLCDCommand(1);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void sendLCDCommandClear() {
        if (woyouService == null) {
            callToastIndicatorUnavailable();
            return;
        }

        try {
            woyouService.sendLCDCommand(4);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void sendLCDCommandSleep() {
        if (woyouService == null) {
            callToastIndicatorUnavailable();
            return;
        }

        try {
            woyouService.sendLCDCommand(3);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void sendLCDDoubleString(String str1, String str2) {
        if (woyouService == null) {
            callToastIndicatorUnavailable();
            return;
        }

        try {
            woyouService.sendLCDDoubleString(str1, str2, null);
            woyouService.sendLCDCommand(2);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void sendLCDBitmap(Bitmap bmp) {
        if (woyouService == null) {
            callToastIndicatorUnavailable();
            return;
        }

        try {
            if (bmp != null) {
                woyouService.sendLCDBitmap(bmp, null);
                woyouService.sendLCDCommand(2);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }


    private void callToastPrinterUnavailable() {
        new Handler(Looper.getMainLooper()).post(
                () -> Toast.makeText(context, context.getString(R.string.toast_sunmi_printer_unavailable), Toast.LENGTH_LONG).show()
        );
    }

    private void callToastIndicatorUnavailable() {
        new Handler(Looper.getMainLooper()).post(
                () -> Toast.makeText(context, context.getString(R.string.toast_sunmi_indicator_unavailable), Toast.LENGTH_LONG).show()
        );
    }
}
