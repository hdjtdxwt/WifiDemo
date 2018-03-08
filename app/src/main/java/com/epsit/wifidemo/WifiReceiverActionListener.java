package com.epsit.wifidemo;

import android.net.wifi.WifiInfo;

/**
 * Created by Administrator on 2018/3/8/008.
 */

public interface WifiReceiverActionListener {
    void onWifiConnected(WifiInfo wifiInfo);
    void onWifiScanResultBack();
    void onWifiOpened();
    void onWifiOpening();
    void onWifiClosed();
    void onWifiClosing();
}
