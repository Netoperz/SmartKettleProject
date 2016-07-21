package com.ardic.android.connectivity.libwirelessconnection;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;

import com.ardic.android.connectivity.libwirelessconnection.listeners.DetailedWifiConnectionListener;
import com.ardic.android.connectivity.libwirelessconnection.listeners.SimpleWifiConnectionListener;

import java.util.List;

/**
 * Created by Yavuz Erzurumlu.
 */
public class WifiHelper {

    private Context appContext;
    private WifiManager mWifiManager;
    private WifiStateReceiver mWifiStateReceiver;
    private DetailedWifiConnectionListener mDetailedWifiConnectionListener;
    private SimpleWifiConnectionListener mSimpleWifiConnectionListener;

    private WifiHelper(Context appContext, SimpleWifiConnectionListener listener) {
        mWifiManager = (WifiManager) appContext.getSystemService(Context.WIFI_SERVICE);
        mWifiStateReceiver = new WifiStateReceiver();
        if (listener != null) {
            mWifiStateReceiver.setSimpleWifiConnectionListener(listener);
        }
    }

    private WifiHelper(Context appContext, DetailedWifiConnectionListener listener) {
        mWifiManager = (WifiManager) appContext.getSystemService(Context.WIFI_SERVICE);
        mWifiStateReceiver = new WifiStateReceiver();
        if (listener != null) {
            mWifiStateReceiver.setDetailedWifiConnectionListener(listener);
        }
    }

    public WifiManager getWifiManager() {
        return this.mWifiManager;
    }

    public DetailedWifiConnectionListener getDetailedWifiConnectionListener() {
        return this.mDetailedWifiConnectionListener;
    }

    public void setDetailedWifiConnectionListener(DetailedWifiConnectionListener listener) {
        if (listener != null) {
            this.mDetailedWifiConnectionListener = listener;
        }
    }

    public SimpleWifiConnectionListener getSimpleWifiConnectionListener() {
        return this.mSimpleWifiConnectionListener;
    }

    public void setSimpleWifiConnectionListener(SimpleWifiConnectionListener listener) {
        if (listener != null) {
            this.mSimpleWifiConnectionListener = listener;
        }
    }

    public boolean isWifiEnabled() {
        return mWifiManager.isWifiEnabled();
    }

    public boolean setWifiEnabled(boolean enabled) {
        return mWifiManager.setWifiEnabled(enabled);
    }

    public boolean connectToWEPNetwork(String ssid, String password, boolean isHidden) {

        WifiConfiguration mWifiConf = new WifiConfiguration();
        mWifiConf.hiddenSSID = isHidden;
        mWifiConf.SSID = ssid;
        mWifiConf.wepKeys[0] = "\"" + password + "\"";
        mWifiConf.wepTxKeyIndex = 0;
        mWifiConf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        mWifiConf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);

        mWifiManager.addNetwork(mWifiConf);
        return connectToConfiguredSSID(ssid);
    }

    public boolean connectToWPANetwork(String ssid, String password, boolean isHidden) {
        WifiConfiguration mWifiConf = new WifiConfiguration();
        mWifiConf.hiddenSSID = isHidden;
        mWifiConf.SSID = ssid;
        mWifiConf.preSharedKey = "\"" + password + "\"";

        mWifiManager.addNetwork(mWifiConf);
        return connectToConfiguredSSID(ssid);
    }

    public boolean connectToOpenNetwork(String ssid, String password, boolean isHidden) {
        WifiConfiguration mWifiConf = new WifiConfiguration();
        mWifiConf.hiddenSSID = isHidden;
        mWifiConf.SSID = ssid;
        mWifiConf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        mWifiManager.addNetwork(mWifiConf);
        return connectToConfiguredSSID(ssid);
    }

    private boolean connectToConfiguredSSID(String ssid) {
        List<WifiConfiguration> list = mWifiManager.getConfiguredNetworks();
        for (WifiConfiguration i : list) {
            if (i.SSID != null && i.SSID.equals("\"" + ssid + "\"")) {
                mWifiManager.disconnect();
                mWifiManager.enableNetwork(i.networkId, true);
                return mWifiManager.reconnect();
            }
        }
        return false;
    }

    public boolean disconnect() {
        return mWifiManager.disconnect();
    }

    /**
     * Factory class for create instance for WifiHelper
     */
    public static class WifiHelperFactory {

        public static WifiHelper create(Context appContext, SimpleWifiConnectionListener listener) throws IllegalArgumentException {
            if (appContext != null) {
                return new WifiHelper(appContext, listener);
            } else {
                throw new IllegalArgumentException("Application Context is NULL.");
            }
        }

        ;

        public static WifiHelper create(Context appContext, DetailedWifiConnectionListener listener) throws IllegalArgumentException {
            if (appContext != null) {
                return new WifiHelper(appContext, listener);
            } else {
                throw new IllegalArgumentException("Application Context is NULL.");
            }
        }

        ;
    }
}
