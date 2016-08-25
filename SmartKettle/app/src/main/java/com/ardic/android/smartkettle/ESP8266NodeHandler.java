package com.ardic.android.smartkettle;

import android.app.Activity;
import android.content.Context;
import android.text.Layout;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Button;


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


import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by root on 29.04.2016.
 */
public class ESP8266NodeHandler  implements ConnectionCallback,PacketListener {

    private static final String TAG ="TestApp";
    private IotIgniteManager mIotIgniteManager;
    private Node myNode;           // Node tanımlıyoruz.
    private Thing mLDRThing;       // Sensörlere ait Thing tanımlaması yapıyoruz.
    private Thing mWEIGHTThing;
    private Thing mWATERThing;
    private Thing mTEAThing;
    private Thing mRELAYThing;
    private ThingData mRELAYData;
    private ThingData mLDRData;        // Thinglere bağlı sensörlerin verilerini depolamak için değişken tanımlıyoruz.
    private ThingData mWEIGHTData;
    private ThingData mWATERData;
    private ThingData mTEAData;
    private final String TEST_APP_KEY = "YOUR APP KEY";    // Her kullanıcı için farklı olan koddur.
    private String NODE;
    private boolean igniteConnected = false;
    private boolean nodeUniquelyModified = false;
    private boolean isFirstRun = true;
    private boolean isRunning=false;
    private ClientThread myReaderThread;


    private ThingListener LDRThingListener = new ThingListener() {
        @Override
        public void onConfigurationReceived(Thing thing) {
            Log.i(TAG, "LDR Config arrived for " + thing.getThingID());
            // TEST //
            if(myReaderThread!=null) {
                myReaderThread.sendWirelessString("LeadingFreq:"+thing.getThingConfiguration().getDataReadingFrequency());

                Log.i(TAG,"LDR Config ="+thing.getThingConfiguration().getDataReadingFrequency());
            }
        }

        @Override
        public void onActionReceived(String s, String s1, ThingActionData thingActionData) {


        }
    };

    private ThingListener WEIGHTTHINGListener = new ThingListener() {
        @Override
        public void onConfigurationReceived(Thing thing) {
            Log.i(TAG, "WEIGHT Config arrived for " + thing.getThingID());
            // TEST //
            if(myReaderThread!=null) {
                myReaderThread.sendWirelessString("WeadingFreq:"+thing.getThingConfiguration().getDataReadingFrequency());

                Log.i(TAG,"WEIGHT Config ="+thing.getThingConfiguration().getDataReadingFrequency());
            }
        }

        @Override
        public void onActionReceived(String s, String s1, ThingActionData thingActionData) {


        }
    };

    private ThingListener WATERTHINGListener = new ThingListener() {
        @Override
        public void onConfigurationReceived(Thing thing) {
            Log.i(TAG, "WATER Config arrived for " + thing.getThingID());
            // TEST //
            if(myReaderThread!=null) {
                myReaderThread.sendWirelessString("AeadingFreq:"+thing.getThingConfiguration().getDataReadingFrequency());
            }
        }

        @Override
        public void onActionReceived(String s, String s1, ThingActionData thingActionData) {


        }
    };

    private ThingListener TEATHINGListener = new ThingListener() {
        @Override
        public void onConfigurationReceived(Thing thing) {
            Log.i(TAG, "TEA Config arrived for " + thing.getThingID());
            // TEST //
            if(myReaderThread!=null) {
                myReaderThread.sendWirelessString("TeadingFreq:"+thing.getThingConfiguration().getDataReadingFrequency());
            }
        }

        @Override
        public void onActionReceived(String s, String s1, ThingActionData thingActionData) {


        }
    };

    private ThingListener RELAYTHINGListener = new ThingListener() {
        @Override
        public void onConfigurationReceived(Thing thing) {
            Log.i(TAG, "RELAY Config arrived for " + thing.getThingID());
            // TEST //
            if(myReaderThread!=null) {
                myReaderThread.sendWirelessString(btn.getText().toString());
            }
        }

        @Override
        public void onActionReceived(String s, String s1, ThingActionData thingActionData) {


        }
    };





    private static final long SOCKET_TIMER_DELAY = 1000L;                  //Timer süreleri ve periyotları belirleniyor.

    private static final long SOCKET_TIMER_PERIOD = 30000L;

    private static final long IGNITE_TIMER_PERIOD = 45000L;

    private static Timer sockeTimer = new Timer();

    private static Timer igniteTimer = new Timer();

    private String IP;                                       //Esp cihazımıza ait local İp adresi

    private int PORT;                                 //Esp cihazımızın bağlı olduğu port

    private Context applicatonContext;

    private TextView txt,txt2,txt3,txt4,txt5;

    private Button btn;

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


    public ESP8266NodeHandler(String ip, int port, Context appContext,Activity mActivity){       //Esp ip ve port bilgileri kaydediliyor.
        this.IP = ip;
        this.PORT = port;
        this.applicatonContext = appContext;
        this.mActivity = mActivity;


        this.txt = (TextView) mActivity.findViewById(R.id.stateText);
        this.txt2 = (TextView) mActivity.findViewById(R.id.weightText);
        this.txt3 = (TextView) mActivity.findViewById(R.id.waterText);
        this.txt4 = (TextView) mActivity.findViewById(R.id.teaText);
        this.txt5 = (TextView) mActivity.findViewById(R.id.kettlestateText);
        this.btn = (Button) mActivity.findViewById(R.id.relayButton);
        btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                if(!TextUtils.isEmpty(btn.getText().toString())) {
                    myReaderThread.sendWirelessString(btn.getText().toString());
                }

            }
        });
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
            mWEIGHTThing.setConnected(false,"Application Destroyed");
            mWATERThing.setConnected(false,"Application Destroyed");
            mTEAThing.setConnected(false,"Application Destroyed");
            mRELAYThing.setConnected(false,"Application Destroyed");
            myNode.setConnected(false,"Application Destroyed");
        }
    }

    @Override
    public void onConnected() {

        Log.i(TAG, "Ignite Connected!");
        igniteConnected=true;
        // Ignite Connected Register Node and Things..

            if(!TextUtils.isEmpty(NODE)) {
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
        ThingType mLDRThingType = new ThingType("LDR Sensor", "LDR", ThingDataType.STRING);           //Sensör datalarının biçimleri belirleniyor.
        ThingType mWEIGHTThingType =  new ThingType("WEIGHT Sensor", "WEIGHT", ThingDataType.FLOAT);
        ThingType mWATERThingType = new ThingType("WATER Sensor", "WATER", ThingDataType.FLOAT);
        ThingType mTEAThingType = new ThingType("TEA Sensor", "TEA", ThingDataType.FLOAT);
        ThingType mRELAYThingType = new ThingType("Relay Sensor", "RELAY", ThingDataType.STRING);

        Log.i(TAG,"Creating Node : " + NODE);
        myNode = IotIgniteManager.NodeFactory.createNode(NODE,NODE, NodeType.GENERIC);              //Node oluşturuluyor

        // register node if not registered and set connection.
        if(!myNode.isRegistered()){ // if koşulunda bir tane daha koşul vardı onu sildikten sonra clouda iki tane kayıt etme sorunu çözüldü
            myNode.setConnected(true,NODE + " is online");
            Log.i(TAG, myNode.getNodeID() + " is successfully registered!");


        }
        else{
            myNode.setConnected(true,NODE + " is online");
            Log.i(TAG, myNode.getNodeID() + " is already registered!");
        }
        if(myNode.isRegistered()){

            String LDR_THING = "LDR Sensor";
            String WEIGHT_THING = "WEIGHT Sensor";
            String WATER_THING = "WATER Sensor";
            String TEA_THING = "TEA Sensor";
            String RELAY_THING = "RELAY Sensor";
            mLDRThing = myNode.createThing(LDR_THING, mLDRThingType, ThingCategory.EXTERNAL,false,LDRThingListener);
            registerThingIfNoRegistered(mLDRThing);
            mWEIGHTThing = myNode.createThing(WEIGHT_THING,mWEIGHTThingType,ThingCategory.EXTERNAL,false,WEIGHTTHINGListener);
            registerThingIfNoRegistered(mWEIGHTThing);
            mWATERThing = myNode.createThing(WATER_THING,mWATERThingType,ThingCategory.EXTERNAL,false,WATERTHINGListener);
            registerThingIfNoRegistered(mWATERThing);
            mTEAThing = myNode.createThing(TEA_THING,mTEAThingType,ThingCategory.EXTERNAL,false,TEATHINGListener);
            registerThingIfNoRegistered(mTEAThing);
            mRELAYThing = myNode.createThing(RELAY_THING,mRELAYThingType,ThingCategory.EXTERNAL,false,RELAYTHINGListener);
            registerThingIfNoRegistered(mRELAYThing);

        }
        // register things...


    }

    private void registerThingIfNoRegistered(Thing t){         //Kayıtlı olmayan Thing kaydediliyor.
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


            // values[0] -> LDR
            if(values[0]!=null) {
                Log.i(TAG, "LDR Data Sending : " + values[0]);
                mLDRData = new ThingData();
                mLDRData.addData(values[0]);
                mLDRData.setDataAccuracy(100);
                mLDRThing.sendData(mLDRData);
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        txt.setText("State = " + values[0]);


                    }
                });
            }

            // values[1] -> WEIGHT
            if(values[1]!=null) {
                Log.i(TAG, "WEIGHT Data Sending : " + values[1]);
                mWEIGHTData = new ThingData();
                mWEIGHTData.addData(values[1]);
                mWEIGHTData.setDataAccuracy(100);
                mWEIGHTThing.sendData(mWEIGHTData);
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        txt2.setText("Weight = " + values[1]);

                    }
                });
            }

            if(values[2]!=null) {
                // values[2] -> WATER
                Log.i(TAG, "WATER Data Sending : " + values[2]);
                mWATERData = new ThingData();
                mWATERData.addData(values[2]);
                mWATERData.setDataAccuracy(100);
                mWATERThing.sendData(mWATERData);
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {


                            txt3.setText("Water = " + values[2]+ "L");
                            Log.i(TAG, "DATA writing TXT");

                    }
                });
            }
            // values[3] -> TEA
            if(values[3]!=null) {
                Log.i(TAG, "TEA Data Sending : " + values[3]);
                mTEAData = new ThingData();
                mTEAData.addData(values[3]);
                mTEAData.setDataAccuracy(100);
                mTEAThing.sendData(mTEAData);
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {


                        txt4.setText("Tea = " + values[3] + "L");
                    }
                });

            }

            // values[4] -> RELAY
            if(values[4]!=null) {
                Log.i(TAG, "RELAY Data Sending : " + values[4]);
                mRELAYData = new ThingData();
                mRELAYData.addData(values[4]);
                mRELAYData.setDataAccuracy(100);
                mRELAYThing.sendData(mRELAYData);
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        txt5.setText(values[4]);

                        String state = txt5.getText().toString();

                        System.out.println("ilker : " + state );


                        if ("open".equals(state))
                        {
                            btn.setText("close");
                        }
                        if ("close".equals(state))
                        {
                            btn.setText("open");
                        }

                    }
                });

            }


        }else if(packet.charAt(0)=='{'){

            if(!nodeUniquelyModified) {

                NODE = "ESP-8266-DEV  "+ packet.substring(1, packet.length()-1);

            }
            Log.i(TAG,"Node ID Arrived :  "  + NODE );
            if(igniteConnected) {
                cancelAndScheduleIgniteTimer();
                isRunning = true;
                initIgniteVariables();
                if(myReaderThread!=null && igniteConnected && isFirstRun) {
                    myReaderThread.sendWirelessString("LeadingFreq:"+mLDRThing.getThingConfiguration().getDataReadingFrequency());
                    myReaderThread.sendWirelessString("WeadingFreq:"+mWEIGHTThing.getThingConfiguration().getDataReadingFrequency());
                    myReaderThread.sendWirelessString("AeadingFreq:"+mWATERThing.getThingConfiguration().getDataReadingFrequency());
                    myReaderThread.sendWirelessString("TeadingFreq:"+mTEAThing.getThingConfiguration().getDataReadingFrequency());





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
