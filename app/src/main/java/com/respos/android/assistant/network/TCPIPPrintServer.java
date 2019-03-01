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
import com.respos.android.assistant.device.Printer;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

import static com.respos.android.assistant.Constants.TAG;

public class TCPIPPrintServer {
    private Context context;
    private Printer printer;
    private ServerSocket serverSocket;
    private int serverSocketPort;
    private int clientSocketTimeout;
    private boolean running = false;                        // флаг для проверки, запущен ли сервер

    public TCPIPPrintServer(Context context, Printer printer, int serverSocketPort, int clientSocketTimeout) {
        this.context = context;
        this.serverSocketPort = serverSocketPort;
        this.clientSocketTimeout = clientSocketTimeout;
        this.printer = printer;
        createServerSocket();
    }

    private void createServerSocket() {
        try {
            serverSocket = new ServerSocket(serverSocketPort);
        } catch (IOException e) {
            Crashlytics.log("Try to create new ServerSocket on port " + serverSocketPort + ". " + e.toString());
            Crashlytics.logException(e);
            Log.d(TAG, e.toString());
            e.printStackTrace();
            callBindExceptionToast(context.getString(R.string.toast_bind_exception), 20);
        }
    }

    class Server implements Runnable {
        private Socket clientSocket;
        private BufferedWriter outputStream;
        private ByteArrayOutputStream byteArrayOutputStream;

        @Override
        public void run() {
            try {
                Log.d(TAG, String.format("listening on port = %d", serverSocketPort));
                while (running) {
                    Log.d(TAG, "waiting for client");
                    clientSocket = serverSocket.accept();
                    Log.d(TAG, String.format("client connected from: %s", clientSocket.getRemoteSocketAddress().toString()));
                    // close Socket anyway after some time if inputStream.read() doesn't occur
                    // to prevent ServerSocket hanging out in forever waiting
                    clientSocket.setSoTimeout(clientSocketTimeout);

                    InputStream inputStream = clientSocket.getInputStream();
                    outputStream = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
                    byteArrayOutputStream = new ByteArrayOutputStream();

                    int readBytesAmount;
                    byte[] inputByteArray = new byte[32768];  // хватит для 1024 строк по 32 символа (около 4м ленты)
                    while ((readBytesAmount = inputStream.read(inputByteArray, 0, inputByteArray.length)) != -1) {
                        byteArrayOutputStream.write(inputByteArray, 0, readBytesAmount);
//                        incomeDataProcessing();          // return true if any data was received, or false if input stream was empty
//                        byteArrayOutputStream.reset();   // clear byteArrayOutputStream after every iteration
                    }
                    incomeDataProcessing();   // other variant is to collect all bytes and then handle them here

                    try {
                        clientSocket.close();
                    } catch (Exception e) {
                        Crashlytics.logException(e);
                        e.printStackTrace();
                        Log.d(TAG, e.toString());
                    }
                }
            } catch (SocketTimeoutException e) {
                try {
                    if (!incomeDataProcessing()) {
                        outputStream.write("SocketTimeoutException (no data to receive)\n");
                        outputStream.flush();
                    }
                    clientSocket.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                Log.d(TAG, String.format("%s (%d ms)", e.toString(), clientSocketTimeout));
                e.printStackTrace();
                if (serverSocket == null || serverSocket.isClosed())        // though usually ServerSocket is still valid
                    createServerSocket();
                runServer();
            } catch (Exception e) {
                Crashlytics.log("Exception in TCPIPPrintServer.Server.run() " + e.toString());
                Crashlytics.log("TCPIPPrintServer.serverSocket.isClosed() = " + String.valueOf(serverSocket.isClosed()));
                Crashlytics.log("Port: " + serverSocketPort);
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
            if (byteArrayOutputStream.size() <= 0)
                return false;

            String printResult = printer.sendDataToPrinter(byteArrayOutputStream.toByteArray());
            try {
                outputStream.write(printResult);
                outputStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return true;
        }
    }

    public void runServer() {
        if (serverSocket != null) {
            running = true;
            try {
                new Thread(new Server()).start();
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

    private void callBindExceptionToast(final String message, int repeatTimes) {
        for (int i = 0; i < repeatTimes; i++) {
            new Handler(Looper.getMainLooper()).post(
                    () -> Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            );
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}