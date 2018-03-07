package com.epsit.wifidemo;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;

import java.util.List;

/**
 * Created by Administrator on 2018/3/7/007.
 */

public class WifiHelper {
    private static WifiHelper instance;
    static Context mContext;
    static WifiManager mWifiManager;

    public WifiHelper(){

    }
    public static WifiHelper getInstance(Context context) {
        if(instance==null){
            synchronized (WifiHelper.class) {
                if(instance==null){
                    instance = new WifiHelper();
                }
            }
        }
        mContext = context;
        mWifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        return instance;
    }
    private static final int WIFICIPHER_NOPASS = 0;
    private static final int WIFICIPHER_WEP = 1;
    private static final int WIFICIPHER_WPA = 2;

    public WifiConfiguration createWifiConfig(String ssid, String password, int type) {
        WifiConfiguration config = new WifiConfiguration();
        config.allowedAuthAlgorithms.clear();
        config.allowedGroupCiphers.clear();
        config.allowedKeyManagement.clear();
        config.allowedPairwiseCiphers.clear();
        config.allowedProtocols.clear();
        config.SSID = "\"" + ssid + "\"";

        WifiConfiguration tempConfig = isExist(ssid);
        if (tempConfig != null) {
            mWifiManager.removeNetwork(tempConfig.networkId);
        }

        if (type == WIFICIPHER_NOPASS) {
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        } else if (type == WIFICIPHER_WEP) {
            config.hiddenSSID = true;
            config.wepKeys[0] = "\"" + password + "\"";
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
        } else if (type == WIFICIPHER_WPA) {
            config.preSharedKey = "\"" + password + "\"";
            config.hiddenSSID = true;
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            config.status = WifiConfiguration.Status.ENABLED;
        }

        return config;
    }

    public WifiConfiguration isExist(String ssid) {
        List<WifiConfiguration> configs = mWifiManager.getConfiguredNetworks();

        for (WifiConfiguration config : configs) {
            if (config.SSID.equals("\"" + ssid + "\"")) {
                return config;
            }
        }
        return null;
    }
}
