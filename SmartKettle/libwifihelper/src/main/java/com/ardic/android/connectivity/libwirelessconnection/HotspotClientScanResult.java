package com.ardic.android.connectivity.libwirelessconnection;

/**
 * Created by Yavuz on 6.06.2016.
 */
public class HotspotClientScanResult {

    private String IpAddr;
    private String HWAddr;
    private String Device;
    private boolean isReachable;

    public HotspotClientScanResult(String ipAddr, String hWAddr, String device, boolean isReachable) {
        super();
        this.IpAddr = ipAddr;
        this.HWAddr = hWAddr;
        this.Device = device;
        this.isReachable = isReachable;
    }

    public String getIpAddr() {
        return IpAddr;
    }
    public void setIpAddr(String ipAddr) {
        IpAddr = ipAddr;
    }


    public String getHWAddr() {
        return HWAddr;
    }
    public void setHWAddr(String hWAddr) {
        HWAddr = hWAddr;
    }


    public String getDevice() {
        return Device;
    }
    public void setDevice(String device) {
        Device = device;
    }


    public boolean isReachable() {
        return isReachable;
    }
    public void setReachable(boolean isReachable) {
        this.isReachable = isReachable;
    }

}
