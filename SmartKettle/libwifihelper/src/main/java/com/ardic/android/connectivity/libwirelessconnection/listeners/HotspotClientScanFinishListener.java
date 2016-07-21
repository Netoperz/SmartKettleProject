package com.ardic.android.connectivity.libwirelessconnection.listeners;

import com.ardic.android.connectivity.libwirelessconnection.HotspotClientScanResult;

import java.util.ArrayList;

/**
 * Created by Yavuz on 6.06.2016.
 */
public interface HotspotClientScanFinishListener {
    public void onClientScanFinish(ArrayList<HotspotClientScanResult> clients);
}
