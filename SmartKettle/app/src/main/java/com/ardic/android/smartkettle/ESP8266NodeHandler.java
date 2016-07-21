package com.ardic.android.smartkettle;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;

import com.ardic.android.iotignite.callbacks.ConnectionCallback;
import com.ardic.android.iotignite.enumerations.NodeType;
import com.ardic.android.iotignite.enumerations.ThingCategory;
import com.ardic.android.iotignite.enumerations.ThingDataType;
import com.ardic.android.iotignite.listeners.ThingListener;
import com.ardic.android.iotignite.nodes.IotIgniteManager;
import com.ardic.android.iotignite.nodes.Node;
import com.ardic.android.iotignite.things.Thing;
import com.ardic.android.iotignite.things.ThingActionData;
import com.ardic.android.iotignite.things.ThingData;
import com.ardic.android.iotignite.things.ThingType;

import org.w3c.dom.Text;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by root on 29.04.2016.
 */
public class ESP8266NodeHandler  implements ConnectionCallback,PacketListener {

    private static final String TAG ="TestApp";
    private IotIgniteManager mIotIgniteManager;
    private Node myNode;
    private Thing mLDRThing;
    private ThingData mLDRData;
    private final String TEST_APP_KEY = "c4d1bf09d50b4e0a8fb158a9bbab845d";
    private String NODE = "ESP-8266-DEV";
    private boolean igniteConnected = false;
    private boolean nodeUniquelyModified = false;
    private boolean isFirstRun = true;
    private boolean isRunning=false;
    private ClientThread myReaderThread;

    private ThingListener LDRThingListener = new ThingListener() {
        @Override
        public void onConfigurationReceived(Thing thing) {
            Log.i(TAG, "Config arrived for " + thing.getThingID());
            // TEST //
            if(myReaderThread!=null) {
                myReaderThread.sendWirelessString("ReadingFreq:"+thing.getThingConfiguration().getDataReadingFrequency());
            }
        }

        @Override
        public void onActionReceived(String s, String s1, ThingActionData thingActionData) {


        }
    };


    private static final long SOCKET_TIMER_DELAY = 1000L;

    private static final long SOCKET_TIMER_PERIOD = 30000L;

    private static final long IGNITE_TIMER_PERIOD = 45000L;

    private static Timer sockeTimer = new Timer();

    private static Timer igniteTimer = new Timer();

    private String IP;

    private int PORT;

    private Context applicatonContext;

    private TextView txt;

    private Activity mActivity;

    private class SocketWatchDog extends TimerTask {
        @Override
        public void run() {
            Log.i(TAG,"Timeout Reached! Trying to reconnect...");
            reconnect();
        }
    }

    private IgniteWatchDog igniteWatchDog = new IgniteWatchDog();
    private SocketWatchDog socketWatchDog = new SocketWatchDog();

    private class IgniteWatchDog extends TimerTask {
        @Override
        public void run() {
            Log.i(TAG,"Rebuild Ignite...");
            rebuild();

        }
    }


    public ESP8266NodeHandler(String ip, int port, Context appContext,Activity mActivity){
        this.IP = ip;
        this.PORT = port;
        this.applicatonContext = appContext;
        this.mActivity = mActivity;

        this.txt = (TextView) mActivity.findViewById(R.id.stateText);
    }

    public void start(){



        mIotIgniteManager = new IotIgniteManager.Builder()
                .setContext(applicatonContext)
                .setAppKey(TEST_APP_KEY)
                .setConnectionListener(this)
                .build();
        cancelAndScheduleIgniteTimer();

    }

    public  void stop(){
        if(myReaderThread!=null){
            myReaderThread.closeSocket();
        }

        if(igniteConnected){
            mLDRThing.setConnected(false,"Application Destroyed");
            myNode.setConnected(false,"ApplicationDestroyed");
        }
    }

    @Override
    public void onConnected() {

        Log.i(TAG, "Ignite Connected!");
        igniteConnected=true;
        // Ignite Connected Register Node and Things..


        if(NODE.length()>12) {
            initIgniteVariables();
        }
        sockeTimer.cancel();
        socketWatchDog.cancel();
        socketWatchDog = new SocketWatchDog();
        sockeTimer = new Timer();
        sockeTimer.schedule(socketWatchDog,SOCKET_TIMER_DELAY);

        cancelAndScheduleIgniteTimer();
    }

    @Override
    public void onDisconnected() {

        igniteConnected=false;
        Log.i(TAG,"Ignite Disconnected!");
        cancelAndScheduleTimer();
        cancelAndScheduleIgniteTimer();

    }

    @Override
    public void onPacketReceived(String packet) {

        cancelAndScheduleTimer();
        cancelAndScheduleIgniteTimer();
        if(igniteConnected && !TextUtils.isEmpty(packet)) {
            onPacketHandler(packet);
            Log.i(TAG, "Packet Sending... : " + packet);
        }

    }


    private void initIgniteVariables(){
        ThingType mLDRThingType = new ThingType("LDR Sensor", "LDR", ThingDataType.STRING);


        myNode = IotIgniteManager.NodeFactory.createNode(NODE, NODE, NodeType.GENERIC);

        // register node if not registered and set connection.
        if(!myNode.isRegistered() && myNode.register()){
            myNode.setConnected(true,NODE + " is online");
            Log.i(TAG, myNode.getNodeID() + " is successfully registered!");


        }
        else{
            myNode.setConnected(true,NODE + " is online");
            Log.i(TAG, myNode.getNodeID() + " is already registered!");
        }
        if(myNode.isRegistered()){

            String LDR_THING = "LDR Sensor";
            mLDRThing = myNode.createThing(LDR_THING, mLDRThingType, ThingCategory.EXTERNAL,false,LDRThingListener);
            registerThingIfNoRegistered(mLDRThing);
        }
        // register things...


    }

    private void registerThingIfNoRegistered(Thing t){
        if(!t.isRegistered() && t.register()){
            t.setConnected(true,t.getThingID() + "connected");
            Log.i(TAG, t.getThingID() +" is successfully registered!");
        }else{
            t.setConnected(true,t.getThingID() + "connected");
            Log.i(TAG, t.getThingID() +" is already registered!");
        }
    }

    private void reconnect(){
        isRunning = false;
        if(myReaderThread!=null){
            myReaderThread.closeSocket();
        }

        myReaderThread = new ClientThread(IP,PORT, "read");
        myReaderThread.setOnPacketListener(this);

        if (myReaderThread != null) {
            if (BuildConfig.DEBUG) {
                Log.i(TAG, "Device Ip : " + IP + " Device Port : " + PORT);
            }
            myReaderThread.start();
        } else {
            Log.i(TAG, "myReader Thread is NULL");
        }


        cancelAndScheduleTimer();
        cancelAndScheduleIgniteTimer();


    }

    private void rebuild(){
        isRunning = false;
        mIotIgniteManager = new IotIgniteManager.Builder()
                .setContext(applicatonContext)
                .setAppKey(TEST_APP_KEY)
                .setConnectionListener(this)
                .build();
        cancelAndScheduleIgniteTimer();

    }

    private void cancelAndScheduleTimer(){
        sockeTimer.cancel();
        socketWatchDog.cancel();
        socketWatchDog = new SocketWatchDog();
        sockeTimer = new Timer();
        sockeTimer.schedule(socketWatchDog,SOCKET_TIMER_PERIOD);
    }
    private void cancelAndScheduleIgniteTimer(){
        igniteTimer.cancel();
        igniteWatchDog.cancel();
        igniteWatchDog = new IgniteWatchDog();
        igniteTimer = new Timer();
        igniteTimer.schedule(igniteWatchDog,IGNITE_TIMER_PERIOD);
    }
    private void onPacketHandler(String packet) {

        // handle sensor values
        if (packet.charAt(0) == '#') {
            final String[] values =
                    PacketUtils.packetHandler(packet);


            // values[1] -> LDR
            Log.i(TAG,"Data Sending : " + values[0]);
            mLDRData = new ThingData();
            mLDRData.addData(values[0]);
            mLDRData.setDataAccuracy(100);
            Log.i(TAG,"Data Sending : " + mLDRData.getDataList().get(0));
            mLDRThing.sendData(mLDRData);

            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    txt.setText("State : " + values[0]);
                }
            });



        }else if(packet.charAt(0)=='~'){

            if(!nodeUniquelyModified) {
                NODE += "  "+ packet.substring(1, packet.length() - 1);
                nodeUniquelyModified = true;
            }
            Log.i(TAG,"Node ID Arrived :  "  + NODE );
            if(igniteConnected) {
                cancelAndScheduleIgniteTimer();
                isRunning = true;
                initIgniteVariables();
                if(myReaderThread!=null && igniteConnected && isFirstRun) {
                    myReaderThread.sendWirelessString("ReadingFreq:"+mLDRThing.getThingConfiguration().getDataReadingFrequency());
                    isFirstRun = false;
                }
            }

        }
    }

    public String getIpAndPort(){
        return this.IP +" : " + this.PORT;
    }
    public boolean isRunning(){
        return this.isRunning;
    }
}
