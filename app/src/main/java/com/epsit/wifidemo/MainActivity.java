package com.epsit.wifidemo;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import com.epsit.wifidemo.util.WifiInfoManage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener {
    private static final String TAG = "MainActivity";
    Button scan,open;
    ListView listView;
    WifiManager wifiManager ;

    private static final int WIFICIPHER_NOPASS = 0;
    private static final int WIFICIPHER_WEP = 1;
    private static final int WIFICIPHER_WPA = 2;

    List<ItemBean> resultList = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        scan = (Button) findViewById(R.id.scan);
        open = (Button) findViewById(R.id.open);
        listView = (ListView) findViewById(R.id.listView);

        scan.setOnClickListener(this);
        open.setOnClickListener(this);

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        listView.setOnItemClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.scan: {
                int wifiState = wifiManager.getWifiState();
                switch (wifiState) {
                    case WifiManager.WIFI_STATE_ENABLING: //WiFi正要开启的状态, 是 Enabled 和 Disabled 的临界状态;
                    case WifiManager.WIFI_STATE_ENABLED: //wifi可用
                        showWifiList();
                        break;
                    case WifiManager.WIFI_STATE_DISABLING:
                    case WifiManager.WIFI_STATE_DISABLED:
                        break;
                }
            }
                break;
            case R.id.open:
                //wifiManager.setWifiEnabled(true);
                getInfo();
                break;

        }
    }
    public void getInfo(){
        try {
            List<com.epsit.wifidemo.util.WifiInfo>list = new WifiInfoManage().Read();
            for(com.epsit.wifidemo.util.WifiInfo info:list){
                Log.e(TAG,info.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public String getConnectedName(){
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();//当前连接的网络
        if(wifiInfo==null){
            return null;
        }
        return wifiInfo.getSSID();
    }

    //显示所有的列表
    public void showWifiList(){
        wifiManager.startScan();
        List<ScanResult> list = wifiManager.getScanResults();
        Log.e("wifiList",list==null?"0":""+list.size());
        resultList.clear();
        for(ScanResult result:list){
            Log.e(TAG,"level=>"+result.level+"  BSSID=>"+result.BSSID+"  SSID=>"+result.SSID);
            if(!containsWifi(resultList,result.SSID)){
                resultList.add(new ItemBean(result.level, result));
            }
        }
        Collections.sort(resultList);//排序
        Log.e("wifiList","过滤后：size="+resultList.size());
        String name = getConnectedName();
        Log.e(TAG,"ConnectedName="+name);
        if(name.contains("\"")){
            name = name.replace("\"","");
        }
        ScanResultAdapter adapter = new ScanResultAdapter(getApplicationContext(), resultList, name);
        listView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }
    public boolean containsWifi(List<ItemBean> list ,String name){
        if(list==null || TextUtils.isEmpty(name) || list.size()==0){
            return false;
        }
        boolean flag = false;
        for(ItemBean result:list){
            if(result.getScanResult().SSID.equals(name)){
                flag = true;
                return flag;
            }
        }
        return flag;
    }
    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            showWifiList();
        }
    };
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ItemBean itemBean = resultList.get(position);
        ScanResult result = itemBean.getScanResult();
        int type = 0;
        if(result.capabilities.contains("wpa")||result.capabilities.contains("WPA")){
            type = WIFICIPHER_WPA;
        }else if(result.capabilities.contains("wep")||result.capabilities.contains("WEP")){
            type = WIFICIPHER_WEP;
        }else{
            type = WIFICIPHER_NOPASS;
        }
        int netId = wifiManager.addNetwork(createWifiConfig(result.SSID, "21123073", type));
        boolean enable = wifiManager.enableNetwork(netId, true);
        Log.d("ZJTest", "enable: " + enable);
        boolean reconnect = wifiManager.reconnect();
        Log.d("ZJTest", "reconnect: " + reconnect);

        //如果用的是listView显示的控件，需要等一会重连其他的网络之后再重新刷新一遍listView,不然可能出现点击会连接不上的情况，RecycleView不会
        handler.sendEmptyMessageDelayed(1,3000);
    }


    private WifiConfiguration createWifiConfig(String ssid, String password, int type) {
        WifiConfiguration config = new WifiConfiguration();
        config.allowedAuthAlgorithms.clear();
        config.allowedGroupCiphers.clear();
        config.allowedKeyManagement.clear();
        config.allowedPairwiseCiphers.clear();
        config.allowedProtocols.clear();
        config.SSID = "\"" + ssid + "\"";

        WifiConfiguration tempConfig = isExist(ssid);
        if (tempConfig != null) {
            wifiManager.removeNetwork(tempConfig.networkId);
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

    private WifiConfiguration isExist(String ssid) {
        List<WifiConfiguration> configs = wifiManager.getConfiguredNetworks();

        for (WifiConfiguration config : configs) {
            if (config.SSID.equals("\"" + ssid + "\"")) {
                return config;
            }
        }
        return null;
    }
}
