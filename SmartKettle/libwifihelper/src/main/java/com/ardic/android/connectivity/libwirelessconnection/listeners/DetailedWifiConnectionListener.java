package com.ardic.android.connectivity.libwirelessconnection.listeners;

import android.net.NetworkInfo;

/**
 * Method name's is taken from android.net.NetworkInfo.State -> Detailed States.
 * For more info check here :
 * https://developer.android.com/reference/android/net/NetworkInfo.DetailedState.html
 *
 * Created by Yavuz Erzurumlu.
 */
public interface DetailedWifiConnectionListener {

    public void onAuthenticating(NetworkInfo info);
    public void onBlocked(NetworkInfo info);
    public void onCaptivePortalCheck(NetworkInfo info);
    public void onConnected(NetworkInfo info);
    public void onConnecting(NetworkInfo info);
    public void onDisconnecting(NetworkInfo info);
    public void onDisconnected(NetworkInfo info);
    public void onFailed(NetworkInfo info);
    public void onIdle(NetworkInfo info);
    public void onObtainingIpAddr(NetworkInfo info);
    public void onScanning(NetworkInfo info);
    public void onSuspended(NetworkInfo info);
    public void onVerifyingPoorLink(NetworkInfo info);
}
