/*
 * Copyright (c) RESONANCE JSC, Bludov Dmitriy, 14.04.2019
 */

package com.respos.android.assistant.network;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.respos.android.assistant.R;
import com.respos.android.assistant.service.ResPOSAssistantService;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class TCPIPPrintServer {
    private static final int SERVER_PORT;
    private static final int clientSocketTimeOut;
    public static final String TAG = "PrintServer";
    private boolean running = false;                        // флаг для проверки, запущен ли сервер
    private static Context context;
    private static ServerSocket serverSocket;
    private static Thread workingThread;
    private static Server server;

    public TCPIPPrintServer(Context context, ) {
        this.context = context;
        createServerSocket();
    }

    private static void createServerSocket() {
        try {
            serverSocket = new ServerSocket(TCPIPPrintServer.SERVER_PORT);
        } catch (IOException e) {
            Crashlytics.log("Try to create new ServerSocket. " + e.toString());
            Crashlytics.logException(e);
            Log.d(TAG, e.toString());
            e.printStackTrace();
            callBindExceptionToast(context.getString(R.string.toast_bind_exception), 20);
        }
    }

    class Server implements Runnable {
        private Socket client;
        private BufferedWriter out;
        private ByteArrayOutputStream byteArrayOutputStream;

        @Override
        public void run() {
            try {
                Log.d(TAG, String.format("listening on port = %d", SERVER_PORT));
                while (running) {
                    Log.d(TAG, "waiting for client");
                    client = serverSocket.accept();
                    Log.d(TAG, String.format("client connected from: %s", client.getRemoteSocketAddress().toString()));
                    // close Socket anyway after some time if inputStream.read() doesn't occur
                    // to prevent ServerSocket hanging out in forever waiting
                    client.setSoTimeout(clientSocketTimeOut);

                    InputStream inputStream = client.getInputStream();
                    out = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
                    byteArrayOutputStream = new ByteArrayOutputStream();

                    int readBytesAmount;
                    byte[] inputByteArray = new byte[32768];  // хватит для 1024 строк по 32 символа (около 4м ленты)
                    while ((readBytesAmount = inputStream.read(inputByteArray, 0, inputByteArray.length)) != -1) {
                        byteArrayOutputStream.write(inputByteArray, 0, readBytesAmount);
                    }

                    incomeDataProcessing();     //return true if any data was received, or false if input stream was empty

                    try {
                        client.close();
                    } catch (Exception e) {
                        Crashlytics.logException(e);
                        e.printStackTrace();
                        Log.d(TAG, e.toString());
                    }
                }
            } catch (SocketTimeoutException e) {
                try {
                    if (!incomeDataProcessing()) {
                        out.write("SocketTimeoutException (no data to receive)");
                        out.flush();
                    }
                    client.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                Log.d(TAG, String.format("%s (%d ms)", e.toString(), clientSocketTimeOut));
                e.printStackTrace();
                if (serverSocket == null || serverSocket.isClosed())        // though usually ServerSocket is still valid
                    createServerSocket();
                runServer();
            } catch (Exception e) {
                Crashlytics.log("Exception in TCPIPPrintServer.Server.run() " + e.toString());
                Crashlytics.log("TCPIPPrintServer.serverSocket.isClosed() = " + String.valueOf(serverSocket.isClosed()));
                Crashlytics.logException(e);
                Log.d(TAG, e.toString());
                e.printStackTrace();
                if (running) {
                    if (serverSocket == null || serverSocket.isClosed())
                        createServerSocket();
                    runServer();
                }
            }
        }

        private boolean incomeDataProcessing() {
            if (byteArrayOutputStream.size() > 0) {
                ResPOSAssistantService.androidDevice.sendDataToPrinter(byteArrayOutputStream.toByteArray());
                try {
                    out.write("OK");
                    out.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return true;
            }
            return false;
        }
    }

    public void runServer() {
        if (serverSocket != null) {
            running = true;
            try {
                server = new Server();
                workingThread = new Thread(server);
                workingThread.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void stopServer() {
        running = false;
        Crashlytics.log("TCPIPPrintServer.stopServer() called");
        try {
            serverSocket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void callBindExceptionToast(final String message, int repeatTimes) {
        for (int i = 0; i < repeatTimes; i++) {

            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                }
            });

            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}