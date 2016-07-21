package com.ardic.android.connectivity.libwirelessconnection;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import com.ardic.android.connectivity.libwirelessconnection.listeners.DetailedWifiConnectionListener;
import com.ardic.android.connectivity.libwirelessconnection.listeners.SimpleWifiConnectionListener;

/**
 * Wifi state receiver for changing connection events.
 * Use detailed listener for all info or use simple listener for connection info.
 * <p/>
 * Created by Yavuz Erzurumlu
 */
public class WifiStateReceiver extends BroadcastReceiver {
    private DetailedWifiConnectionListener mDetailedWifiConnectionListener;
    private SimpleWifiConnectionListener mSimpleWifiConnectionListener;

    public WifiStateReceiver() {
    }

    public void setDetailedWifiConnectionListener(DetailedWifiConnectionListener listener) {
        if (listener != null) {
            this.mDetailedWifiConnectionListener = listener;
        }
    }

    public void setSimpleWifiConnectionListener(SimpleWifiConnectionListener listener) {
        if (listener != null) {
            this.mSimpleWifiConnectionListener = listener;
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
        if (info != null && info.getType() == ConnectivityManager.TYPE_WIFI) {
            NetworkInfo.State state = info.getState();
            checkStateAndTriggerSimpleConnectionListener(info, state);

            NetworkInfo.DetailedState detailedState = info.getDetailedState();
            checkDetailedStateAndTriggerDetailedConnectionListener(info, detailedState);
        }
    }

    private void checkStateAndTriggerSimpleConnectionListener(NetworkInfo info, NetworkInfo.State mState) {
        if (mSimpleWifiConnectionListener != null && info != null) {

            if (NetworkInfo.State.CONNECTED.equals(mState)) {
                // trigger onConnected.
                mSimpleWifiConnectionListener.onConnected(info);
            } else if (NetworkInfo.State.CONNECTING.equals(mState)) {
                // trigger onConnecting
                mSimpleWifiConnectionListener.onConnecting(info);
            } else if (NetworkInfo.State.DISCONNECTED.equals(mState)) {
                // trigger onDisconnected
                mSimpleWifiConnectionListener.onDisconnected(info);
            } else if (NetworkInfo.State.DISCONNECTING.equals(mState)) {
                // trigger onDisconnecting
                mSimpleWifiConnectionListener.onDisconnecting(info);
            } else if (NetworkInfo.State.SUSPENDED.equals(mState)) {
                // trigger onSuspended
                mSimpleWifiConnectionListener.onSuspended(info);
            } else if (NetworkInfo.State.UNKNOWN.equals(mState)) {
                // trigger onUnknown
                mSimpleWifiConnectionListener.onUnknown(info);
            }
        }
    }

    private void checkDetailedStateAndTriggerDetailedConnectionListener(NetworkInfo info, NetworkInfo.DetailedState mState) {

        if (mDetailedWifiConnectionListener != null && info != null) {

            if (NetworkInfo.DetailedState.AUTHENTICATING.equals(mState)) {
                // trigger onAuthenticating.
                mDetailedWifiConnectionListener.onAuthenticating(info);
            } else if (NetworkInfo.DetailedState.BLOCKED.equals(mState)) {
                // trigger onBlocked.
                mDetailedWifiConnectionListener.onBlocked(info);
            } else if (NetworkInfo.DetailedState.CAPTIVE_PORTAL_CHECK.equals(mState)) {
                // trigger onCaptivePortalCheck.
                mDetailedWifiConnectionListener.onCaptivePortalCheck(info);
            } else if (NetworkInfo.DetailedState.CONNECTED.equals(mState)) {
                // trigger onConnected.
                mDetailedWifiConnectionListener.onConnected(info);
            } else if (NetworkInfo.DetailedState.CONNECTING.equals(mState)) {
                // trigger onConnecting.
                mDetailedWifiConnectionListener.onConnecting(info);
            } else if (NetworkInfo.DetailedState.DISCONNECTED.equals(mState)) {
                // trigger onDisconnected.
                mDetailedWifiConnectionListener.onDisconnected(info);
            } else if (NetworkInfo.DetailedState.DISCONNECTING.equals(mState)) {
                // trigger onDisconnecting.
                mDetailedWifiConnectionListener.onDisconnecting(info);
            } else if (NetworkInfo.DetailedState.FAILED.equals(mState)) {
                // trigger onFailed.
                mDetailedWifiConnectionListener.onFailed(info);
            } else if (NetworkInfo.DetailedState.IDLE.equals(mState)) {
                // trigger onIdle.
                mDetailedWifiConnectionListener.onIdle(info);
            } else if (NetworkInfo.DetailedState.OBTAINING_IPADDR.equals(mState)) {
                // trigger onObtainingIpAdrr.
                mDetailedWifiConnectionListener.onObtainingIpAddr(info);
            } else if (NetworkInfo.DetailedState.SCANNING.equals(mState)) {
                // trigger onScanning
                mDetailedWifiConnectionListener.onScanning(info);
            } else if (NetworkInfo.DetailedState.SUSPENDED.equals(mState)) {
                // trigger onSuspended
                mDetailedWifiConnectionListener.onSuspended(info);
            } else if (NetworkInfo.DetailedState.VERIFYING_POOR_LINK.equals(mState)) {
                // trigger onVerifyingPoorLink
                mDetailedWifiConnectionListener.onVerifyingPoorLink(info);
            }
        }
    }

}
