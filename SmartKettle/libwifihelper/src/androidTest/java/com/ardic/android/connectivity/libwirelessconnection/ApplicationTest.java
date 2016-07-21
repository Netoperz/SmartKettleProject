package com.ardic.android.connectivity.libwirelessconnection;

import android.app.Application;
import android.app.Instrumentation;
import android.net.NetworkInfo;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.ApplicationTestCase;
import android.test.InstrumentationTestCase;
import android.test.suitebuilder.annotation.SmallTest;

import com.ardic.android.connectivity.libwirelessconnection.listeners.DetailedWifiConnectionListener;
import com.ardic.android.connectivity.libwirelessconnection.listeners.SimpleWifiConnectionListener;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import dalvik.annotation.TestTargetClass;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
@RunWith(AndroidJUnit4.class)
@SmallTest
public class ApplicationTest extends InstrumentationTestCase{

    private WifiHelper mWifiHelperSimple;
    private WifiHelper mWifiHelperDetailed;

    private SimpleWifiConnectionListener listener = new SimpleWifiConnectionListener() {
        @Override
        public void onConnecting(NetworkInfo info) {

        }

        @Override
        public void onConnected(NetworkInfo info) {

        }

        @Override
        public void onDisconnected(NetworkInfo info) {

        }

        @Override
        public void onDisconnecting(NetworkInfo info) {

        }

        @Override
        public void onSuspended(NetworkInfo info) {

        }

        @Override
        public void onUnknown(NetworkInfo info) {

        }
    };

    private DetailedWifiConnectionListener detailedListener = new DetailedWifiConnectionListener() {
        @Override
        public void onAuthenticating(NetworkInfo info) {

        }

        @Override
        public void onBlocked(NetworkInfo info) {

        }

        @Override
        public void onCaptivePortalCheck(NetworkInfo info) {

        }

        @Override
        public void onConnected(NetworkInfo info) {

        }

        @Override
        public void onConnecting(NetworkInfo info) {

        }

        @Override
        public void onDisconnecting(NetworkInfo info) {

        }

        @Override
        public void onDisconnected(NetworkInfo info) {

        }

        @Override
        public void onFailed(NetworkInfo info) {

        }

        @Override
        public void onIdle(NetworkInfo info) {

        }

        @Override
        public void onObtainingIpAddr(NetworkInfo info) {

        }

        @Override
        public void onScanning(NetworkInfo info) {

        }

        @Override
        public void onSuspended(NetworkInfo info) {

        }

        @Override
        public void onVerifyingPoorLink(NetworkInfo info) {

        }
    };

    @Before
    public void createWifiHelper() {
       mWifiHelperSimple = WifiHelper.WifiHelperFactory.create(InstrumentationRegistry.getContext(),listener);
        mWifiHelperDetailed = WifiHelper.WifiHelperFactory.create(InstrumentationRegistry.getContext(),detailedListener);
    }
    @Test
    public void WifiHelperSimpleListenerTest(){

        // simple
        assertNotNull(mWifiHelperSimple);
        assertNotNull(mWifiHelperSimple.getWifiManager());
        assertNull(mWifiHelperSimple.getDetailedWifiConnectionListener());
    }

    @Test
    public void WifiHelperDetailedListenerTest(){

        //detailed
        assertNotNull(mWifiHelperDetailed);
        assertNotNull(mWifiHelperDetailed.getWifiManager());
        assertNull(mWifiHelperSimple.getSimpleWifiConnectionListener());
    }

}