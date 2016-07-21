package com.ardic.android.smartkettle;

import android.app.Activity;
import android.content.Context;
import android.net.NetworkInfo;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.ardic.android.connectivity.libwirelessconnection.WifiHelper;
import com.ardic.android.connectivity.libwirelessconnection.listeners.SimpleWifiConnectionListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class ControlDashboard extends AppCompatActivity implements NsdManager.DiscoveryListener {

    private static final String TAG ="TestApp";
    // Ignite Variables //
    private NsdManager mNsdManager;
    private static final String SERVICE_TYPE="_esp8266._tcp.";
    private static List<String> espServiceList = new ArrayList<String>();
    private static List<ESP8266NodeHandler> espInstances = new ArrayList<ESP8266NodeHandler>();
    private static final long ESP_HANDLER_PERIOD = 30000L;
    private static final long ESP_HANDLER_DELAY = 40000L;

    private WifiHelper mWifiHelper;

    private TextView stateText;

    private Activity mActivity;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control_dashboard);
        Log.i(TAG,"Application started...");
        mActivity = this;
        mNsdManager = (NsdManager) getSystemService(Context.NSD_SERVICE);
        mNsdManager.discoverServices(
                SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, this);
        stateText = (TextView) findViewById(R.id.stateText);

        mWifiHelper = WifiHelper.WifiHelperFactory.create(getApplicationContext(), new SimpleWifiConnectionListener() {
            @Override
            public void onConnecting(NetworkInfo info) {
                Log.i(TAG,"Connecting...");
            }

            @Override
            public void onConnected(NetworkInfo info) {
                Log.i(TAG,"Connected.");
            }

            @Override
            public void onDisconnected(NetworkInfo info) {
                Log.i(TAG,"Disconnected");
            }

            @Override
            public void onDisconnecting(NetworkInfo info) {
                Log.i(TAG,"disconnecting...");
            }

            @Override
            public void onSuspended(NetworkInfo info) {
                Log.i(TAG,"Suspended...");
            }

            @Override
            public void onUnknown(NetworkInfo info) {
                Log.i(TAG,"Unknown...");
            }
        });

        Log.i(TAG ,"Wifi Enabled :"  + mWifiHelper.isWifiEnabled());



        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {

                for(ESP8266NodeHandler esp : espInstances){
                    if(!esp.isRunning()){
                        esp.start();
                        Log.i(TAG,"ESP : " + esp.getIpAndPort() + " is started again.");
                    }
                }
            }
        },ESP_HANDLER_DELAY,ESP_HANDLER_PERIOD);

    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        mNsdManager.stopServiceDiscovery(this);


        for(ESP8266NodeHandler esp : espInstances){
            esp.stop();
        }

    }


    @Override
    public void onStartDiscoveryFailed(String serviceType, int errorCode) {

    }

    @Override
    public void onStopDiscoveryFailed(String serviceType, int errorCode) {

    }

    @Override
    public void onDiscoveryStarted(String serviceType) {

        Log.i(TAG,"Service discovery started...");
    }

    @Override
    public void onDiscoveryStopped(String serviceType) {
        Log.i(TAG,"Service discovery stopped...");
    }

    @Override
    public void onServiceFound(NsdServiceInfo serviceInfo) {
        Log.i(TAG,"New service found!");


        mNsdManager.resolveService(serviceInfo, new NsdManager.ResolveListener() {
            @Override
            public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
                Log.i(TAG,"New service resolve failed!");
            }

            @Override
            public void onServiceResolved(NsdServiceInfo serviceInfo) {

                Log.i(TAG,"New service resolved!");
                Log.i(TAG,"Raw  :  " + serviceInfo.toString());
                String espIP = serviceInfo.getHost().toString().substring(1,serviceInfo.getHost().toString().length());
                int port = serviceInfo.getPort();

                Log.i(TAG,"IP&PORT  :  " + espIP + ":"+port);
                if(!espServiceList.contains(espIP)) {
                    final ESP8266NodeHandler mEspHandler = new ESP8266NodeHandler(espIP, port, getApplicationContext(),mActivity);
                    espInstances.add(mEspHandler);
                    espServiceList.add(espIP);
                    Log.i(TAG,"Total founded esp8266 : " +espServiceList.size() );
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {

                            Log.i(TAG,"Instance Size : " + espInstances.size());
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    mEspHandler.start();
                                    Log.i(TAG,"STARTING ESP " + mEspHandler.getIpAndPort());
                                }
                            },(5000*espInstances.indexOf(mEspHandler)*2));


                        }
                    },1000);
                }else{
                    int loc = 0;

                    for(ESP8266NodeHandler e : espInstances){
                        if(e.getIpAndPort().equals(espIP+" : "+port)){
                            loc = espInstances.indexOf(e);
                            break;
                        }
                    }
                    final ESP8266NodeHandler esp = espInstances.get(loc);

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {

                            Log.i(TAG,"Instance Size : " + espInstances.size());
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    esp.start();
                                    Log.i(TAG,"STARTING ESP " + esp.getIpAndPort());
                                }
                            },(5000*espInstances.indexOf(esp)*2));


                        }
                    },1000);
                }
            }
        });
    }

    @Override
    public void onServiceLost(NsdServiceInfo serviceInfo) {

    }
}
