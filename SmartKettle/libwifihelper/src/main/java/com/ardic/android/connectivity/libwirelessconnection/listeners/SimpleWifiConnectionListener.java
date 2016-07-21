package com.ardic.android.connectivity.libwirelessconnection.listeners;

import android.net.NetworkInfo;

/**
 * Method name's is taken from android.net.NetworkInfo.State -> Detailed States.
 * For more info check here :
 * https://developer.android.com/reference/android/net/NetworkInfo.State.html
 *
 * Created by Yavuz Erzurumlu.
 */
public interface SimpleWifiConnectionListener {

    public void onConnecting(NetworkInfo info);
    public void onConnected(NetworkInfo info);
    public void onDisconnected(NetworkInfo info);
    public void onDisconnecting(NetworkInfo info);
    public void onSuspended(NetworkInfo info);
    public void onUnknown(NetworkInfo info);
}
