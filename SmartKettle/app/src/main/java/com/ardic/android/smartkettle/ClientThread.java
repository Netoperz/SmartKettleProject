package com.ardic.android.smartkettle;

import android.util.Log;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Created by root on 26.04.2016.
 */
public class ClientThread extends Thread {

    private static final String TAG = ClientThread.class.getSimpleName();
    private Socket socket;
    private InputStream in;
    private String ip;
    private int port;
    private String str;
    private PacketListener myPacketListener;

    public ClientThread(String ip, int port, String str) {
        this.ip = ip;
        this.port = port;
        this.str = str;

    }

    @Override
    public void run() {

        try {
            Log.i(TAG, "ip :" + ip + " port " + port);
            socket = new Socket(ip, port);

            if ("read".equals(str)) {
                if (BuildConfig.DEBUG) {
                    Log.i(TAG, "Reading Data Function Starting ...:");
                }
                readIncomingData();
            }

        } catch (UnknownHostException e) {

            Log.i(TAG, "UnknownHostException : " + e);
        } catch (IOException e) {

            Log.i(TAG, "IOException :" + e);
        }

    }

    public void sendWirelessString(String data) {
        String local = "";

        if (data == null)
            local = str;
        else
            local = data;
        try {

            PrintWriter out = new PrintWriter(new BufferedWriter(
                    new OutputStreamWriter(socket.getOutputStream())), true);
            if (socket != null && socket.isConnected()) {
                Log.i(TAG, "Message :" + local);
                out.println(local);
            }
        } catch (UnknownHostException e) {

            Log.i(TAG, "UnknownHostException :" + e);

        } catch (IOException e) {

            Log.i(TAG, "IOException :" + e);

        } catch (Exception e) {

            Log.i(TAG, "Exception :" + e);

        }

    }

    public void readIncomingData() {

        InputStream tmpIn = null;
        byte[] buffer = new byte[1024];
        int bytes;

        try {
            // Create I/O streams for connection
            tmpIn = socket.getInputStream();

            if (BuildConfig.DEBUG) {
                Log.d(TAG, "tmpIn: " + tmpIn);
            }
            in = tmpIn;
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "In: " + in);
            }
            while (true) {
                try {
                    bytes = in.read(buffer); // read bytes from input buffer
                    if (BuildConfig.DEBUG) {
                        Log.d(TAG, "Bytes: " + bytes);
                    }
                    String readMessage;
                    if (bytes != -1) {
                        readMessage = new String(buffer, 0, bytes);
                        sendWirelessPacketEvent(readMessage);
                    } else {

                        readMessage = "null";
                        try {
                            Thread.sleep(3000);
                        } catch (InterruptedException e) {
                            Log.i(TAG, "Interrupted Exception on ClientThread:" + e);
                        }
                    }
                    if (BuildConfig.DEBUG) {
                        Log.d(TAG, "ReadMessage: " + readMessage);
                    }
                } catch (IOException e) {
                    if (BuildConfig.DEBUG) {
                        Log.d(TAG, "IOException: " + e);
                    }
                    break;
                }
            }

        } catch (IOException e) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "getInputStream error: " + e);
            }
        }

    }

    public void setOnPacketListener(PacketListener myPacketListener) {
        this.myPacketListener = myPacketListener;
    }

    public void sendWirelessPacketEvent(String packet) {
        if (myPacketListener != null) {
            if (BuildConfig.DEBUG) {
                Log.i(TAG, "myPacketListener is not null.");
            }
            myPacketListener.onPacketReceived(packet);
        } else {
            if (BuildConfig.DEBUG) {
                Log.i(TAG, "myPacketListener is null.");
            }
        }
    }

    public void closeSocket() {

        try {
            if(socket!=null) {
                socket.close();
            }
            socket = null;
        } catch (IOException e) {
            Log.i(TAG, "IOException:" + e);
        }
    }
}

