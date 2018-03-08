package com.epsit.wifidemo;

import android.Manifest;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;

import android.support.v7.app.AppCompatActivity;

import com.epsit.wifidemo.dialog.InputAndConnectDialog;
import com.epsit.wifidemo.dialog.SelectItemDialog;

/**
 * Created by Administrator on 2018/2/27/027.
 */

public class WifiActivity extends AppCompatActivity implements WifiReceiverActionListener, OnItemClickListener, InputAndConnectDialog.OnConnectionListener, OnItemLongClickListener, SelectItemDialog.OnSelectDialogListener {
    private WifiManager mWifiManager;
    private Handler mMainHandler;
    private boolean mHasPermission;
    String TAG = "WifiActivity";
    TextView mOpenWifiButton;
    Button mGetWifiInfoButton;
    WifiReceiver wifiReceiver;
    ListView listView;
    ResultListAdapter adapter;
    String connectSsid;
    List<ScanResult> list = new ArrayList<>();
    private static final int WIFICIPHER_NOPASS = 0;
    private static final int WIFICIPHER_WEP = 1;
    private static final int WIFICIPHER_WPA = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi);

        mWifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        mMainHandler = new Handler();

        findChildViews();

        configChildViews();

        mHasPermission = checkPermission();
        if (!mHasPermission) {
            requestPermission();
        }
        registerBroadcastReceiver();
    }


    private Runnable mMainRunnable = new Runnable() {
        @Override
        public void run() {
            if (mWifiManager.isWifiEnabled()) {
                mGetWifiInfoButton.setEnabled(true);
            } else {
                mMainHandler.postDelayed(mMainRunnable, 1000);
            }
        }
    };

    private List<ScanResult> mScanResultList;
    private void findChildViews() {
        mOpenWifiButton = (TextView) findViewById(R.id.open_wifi);
        mGetWifiInfoButton = (Button) findViewById(R.id.get_wifi_info);
        listView = (ListView) findViewById(R.id.wifi_info_detail);
    }
    private void configChildViews() {
        /*mOpenWifiButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mWifiManager.isWifiEnabled()) {
                    mWifiManager.setWifiEnabled(true);
                    mMainHandler.post(mMainRunnable);
                }
            }
        });*/

        mGetWifiInfoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e(TAG," 点击后查询到的状态："+mWifiManager.getWifiState());
                if(WifiManager.WIFI_STATE_ENABLED==mWifiManager.getWifiState()){ //wifi可用
                    //v.setBackgroundResource(R.drawable.wifi_gray_icon);
                    mWifiManager.setWifiEnabled(false);
                    Log.e(TAG,"当前的是可用状态，立马点击让他不可用");
                }else{
                    //v.setBackgroundResource(R.drawable.wifi_blue_icon);
                    mWifiManager.setWifiEnabled(true);
                    Log.e(TAG,"当前的是不可用状态，立马点击让他可用");
                }
                //reflushList();
            }
        });

        adapter = new ResultListAdapter(this, list);
        adapter.setListener(this);
        adapter.setLongClickListener(this);
        String connectedName = WifiHelper.getInstance(this).getConnectWifiSsid();
        adapter.setConnectedName(connectedName);
        listView.setAdapter(adapter);
    }
    public void reflushList(){
        if (mWifiManager.isWifiEnabled()) {
            mScanResultList = mWifiManager.getScanResults();//这里面的ScanResult的SSID不带有引号
            for(ScanResult result :mScanResultList){
                Log.e(TAG,result.SSID);
            }
            Log.e(TAG,"---刚进来：mScanResultList。size="+mScanResultList.size());
            //下面的两个for去除重复的wifi（网络的说法是同一个wifi双频段就会出现这样的情况，比如有2.4G的网络还有5G的网络），
            for(int i=0;i< mScanResultList.size();i++){
                ScanResult current = mScanResultList.get(i);//
                for(int j=i+1;j<mScanResultList.size();j++){
                    if(current.SSID.equals(mScanResultList.get(j).SSID)){
                        mScanResultList.remove(j);
                    }
                }
            }
            Log.e(TAG,"移除重名的wifi后："+mScanResultList.size());
            //到这里，mScanResultList中就没有重复的wifi了
            list.clear();

            //resultList里的第一个就是当前连接的wifi名称
            List<ScanResult>resultList = new ArrayList();
            String name = WifiHelper.getInstance(getApplicationContext()).getConnectWifiSsid();
            Log.e(TAG,"当前连接的wifi:"+name);
            if(!TextUtils.isEmpty(name)) {
                for (int i = 0; i < mScanResultList.size(); i++) {
                    if (name.equals("\""+mScanResultList.get(i).SSID+"\"")) {
                        ScanResult result = mScanResultList.get(i);
                        resultList.add(result);
                        mScanResultList.remove(result);
                        break;
                    }
                }
            }
            Log.e(TAG,"移除当前已经连接的那个wifi后：resultList.size="+resultList.size()+"  mScanResultList.size="+mScanResultList.size());
            List<WifiConfiguration> configs = mWifiManager.getConfiguredNetworks();//里面的WifiConfiguration的SSID属性带有引号
            if(configs!=null){
                Log.e(TAG,"configs.size="+configs.size());
                for (WifiConfiguration config : configs) {
                    Log.e(TAG,"config.SSID="+config.SSID);
                }
                for(int i=0;i<mScanResultList.size();i++){
                    ScanResult result = mScanResultList.get(i);
                    for(int j=0;j<configs.size();j++){
                        if(configs.get(j).SSID.equals("\""+result.SSID+"\"")){
                            resultList.add(result);
                            mScanResultList.remove(result);
                            break;
                        }
                    }
                }
                Log.e(TAG,"添加完保存了的wifi后：resultList.size="+resultList.size()+"  mScanResultList.size="+mScanResultList.size());
            }

            resultList.addAll(mScanResultList);
            list.addAll(resultList);
            //sortList(list);
            adapter.notifyDataSetChanged();
        }
    }

    //-------------------回调start
    @Override
    public void onWifiConnected(WifiInfo wifiInfo) {
        Log.e(TAG, "onWifiConnected-->" + wifiInfo.getSSID());
        String name = mWifiManager.getConnectionInfo().getSSID();
        Log.e(TAG, "name-->" + name);
        adapter.setConnectedName(name);
        reflushList();
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onWifiScanResultBack() {
        Log.e(TAG, "onWifiScanResultBack-->有搜索结果返回");
    }

    @Override
    public void onWifiOpened() {
        Log.e(TAG, "onWifiOpened-->wifi打开了");
        reflushList();
        mGetWifiInfoButton.setBackgroundResource(R.drawable.wifi_blue_icon);
    }

    @Override
    public void onWifiOpening() {
        Log.e(TAG, "onWifiOpened-->wifi正在打开");
    }

    @Override
    public void onWifiClosed() {
        Log.e(TAG, "onWifiOpened-->wifi关闭的");
        list.clear();
        //reflushList();
        adapter.notifyDataSetChanged();
        mGetWifiInfoButton.setBackgroundResource(R.drawable.wifi_gray_icon);
    }

    @Override
    public void onWifiClosing() {
        Log.e(TAG, "onWifiOpened-->wifi正在关闭");
    }
    //-------------------回调end

    /**
     * public void onClick(View v) {
     * <p>
     * }
     */
    private static final String[] NEEDED_PERMISSIONS = new String[]{
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    private boolean checkPermission() {
        for (String permission : NEEDED_PERMISSIONS) {
            if (ActivityCompat.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }

        return true;
    }

    private static final int PERMISSION_REQUEST_CODE = 0;

    private void requestPermission() {
        ActivityCompat.requestPermissions(this,
                NEEDED_PERMISSIONS, PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mWifiManager.isWifiEnabled() && mHasPermission) {
            mGetWifiInfoButton.setEnabled(true);
        } else {
            mGetWifiInfoButton.setEnabled(false);
            if (mScanResultList != null) {
                mScanResultList.clear();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        boolean hasAllPermission = true;

        if (requestCode == PERMISSION_REQUEST_CODE) {
            for (int i : grantResults) {
                if (i != PackageManager.PERMISSION_GRANTED) {
                    hasAllPermission = false;
                    break;
                }
            }

            if (hasAllPermission) {
                mHasPermission = true;
            } else {
                mHasPermission = false;
                Toast.makeText(
                        this, "Need More Permission",
                        Toast.LENGTH_LONG)
                        .show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (wifiReceiver != null) {
            unregisterReceiver(wifiReceiver);
            wifiReceiver = null;
        }
    }

    /**
     * 在这里注册广播接收者
     * 这里面注册广播的话,包括:
     * 1 wifi开启状态的监听(wifi关闭,wifi打开)
     * 2 wifi连接的广播
     * 3 wifi连接状态改变的广播
     * <p>
     */
    private void registerBroadcastReceiver() {

        IntentFilter filter = new IntentFilter();
        //设置意图过滤

        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);

        wifiReceiver = new WifiReceiver(this);
        registerReceiver(wifiReceiver, filter);

    }


    //dialog输入密码后点击连接
    @Override
    public void onConnectionListener(String password) {
        if (!TextUtils.isEmpty(connectSsid) && !TextUtils.isEmpty(password)) {

            Log.e(TAG, "马上连接：" + connectSsid + "  password=" + password);
            int netId = mWifiManager.addNetwork(WifiHelper.getInstance(getApplicationContext()).createWifiConfig(connectSsid, password, WIFICIPHER_WPA));
            boolean enable = mWifiManager.enableNetwork(netId, true);
            Log.e("ZJTest", "enable: " + enable);
            /*boolean reconnect = mWifiManager.reconnect();
            Log.e("ZJTest", "reconnect: " + reconnect);*/
        } else {
            Log.e("connectListener", !TextUtils.isEmpty(connectSsid) + "  " + (!TextUtils.isEmpty(password)));
        }
    }

    @Override
    public void onItemClick(View view, int postion) {
        Toast.makeText(this, "点击item:" + postion, Toast.LENGTH_SHORT).show();
        connectSsid = list.get(postion).SSID;
        Log.e(TAG, "点击item connectSsid=" + connectSsid);
        showDialog();
    }

    //显示输入密码的框框
    public void showDialog() {
        Dialog dialog = new InputAndConnectDialog.Builder(this).setListener(this).setName(connectSsid).create();
        dialog.show();
        android.view.WindowManager.LayoutParams p = dialog.getWindow().getAttributes();  //获取对话框当前的参数值
        p.height = 450;
        p.width = 600;    //宽度设置为屏幕的0.5
        dialog.getWindow().setAttributes(p);     //设置生效
    }

    //长按后显示一个dialog
    @Override
    public void onItemLongClick(View view, int postion) {
        connectSsid = list.get(postion).SSID;
        Log.e(TAG, "长按item connectSsid=" + connectSsid);
        SelectItemDialog.Builder builder = new SelectItemDialog.Builder(this);
        builder.setOnSelectDialogListener(this);
        builder.setShowText(connectSsid);
        Dialog dialog = builder.create();
        dialog.show();
        android.view.WindowManager.LayoutParams p = dialog.getWindow().getAttributes();  //获取对话框当前的参数值
        p.height = 240;
        p.width = 450;    //宽度设置为屏幕的0.5
        dialog.getWindow().setAttributes(p);     //设置生效
    }

    //点击了长按出现的dialog里的选择项
    @Override
    public void onClickSelectedListener(View view, int type) {
        switch (type) {
            case 1: {//连接
                Log.e(TAG,"弹框选择项的长条条点击了--》1");
                WifiInfo info = WifiHelper.getInstance(this).getConnectWifiInifo();
                //断开指定ID的网络
                mWifiManager.disableNetwork(info.getNetworkId());
                mWifiManager.disconnect();

                Log.e(TAG,  "connectSsid="+connectSsid );
                List<WifiConfiguration> configList = mWifiManager.getConfiguredNetworks();
                for (WifiConfiguration cfg : configList) {
                    if (cfg.SSID.equals("\""+ connectSsid+"\"")) {
                        Log.e(TAG,"点击了连接这个网络："+cfg.toString());
                        mWifiManager.enableNetwork(cfg.networkId, true);
                        break;
                    }
                }
            }break;
            case 2: {//取消保存
                Log.e(TAG,"弹框选择项的长条条点击了--》2");
                List<WifiConfiguration> configList = mWifiManager.getConfiguredNetworks();
                for (WifiConfiguration cfg : configList) {
                    if (cfg.SSID.equals("\""+ connectSsid+"\"")) {
                        mWifiManager.removeNetwork(cfg.networkId);
                        mWifiManager.saveConfiguration();
                        reflushList();
                        break;
                    }
                }
            }break;
            case 3://修改密码
                Log.e(TAG,"弹框选择项的长条条点击了--》3");
                break;
        }
    }
}