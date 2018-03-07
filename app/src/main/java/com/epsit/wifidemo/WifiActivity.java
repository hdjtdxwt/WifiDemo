package com.epsit.wifidemo;

import android.Manifest;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.TreeMap;

import android.support.v7.app.AppCompatActivity;

/**
 * Created by Administrator on 2018/2/27/027.
 */

public class WifiActivity extends AppCompatActivity {
    private WifiManager mWifiManager;
    private Handler mMainHandler;
    private boolean mHasPermission;
    String TAG ="WifiActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi);

        //registerBroadcastReceiver();

        mWifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        mMainHandler = new Handler();

        findChildViews();

        configChildViews();

        mHasPermission = checkPermission();
        if (!mHasPermission) {
            requestPermission();
        }
    }

    Button mOpenWifiButton;
    Button mGetWifiInfoButton;
    RecyclerView mWifiInfoRecyclerView;

    private void findChildViews() {
        mOpenWifiButton = (Button) findViewById(R.id.open_wifi);
        mGetWifiInfoButton = (Button) findViewById(R.id.get_wifi_info);
        mWifiInfoRecyclerView = (RecyclerView) findViewById(R.id.wifi_info_detail);
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

    private void configChildViews() {
        mOpenWifiButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mWifiManager.isWifiEnabled()) {
                    mWifiManager.setWifiEnabled(true);
                    mMainHandler.post(mMainRunnable);
                }
            }
        });

        mGetWifiInfoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mWifiManager.isWifiEnabled()) {
                    mScanResultList = mWifiManager.getScanResults();
                    sortList(mScanResultList);
                    mWifiInfoRecyclerView.getAdapter().notifyDataSetChanged();
                }
            }
        });

        mWifiInfoRecyclerView.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mWifiInfoRecyclerView.setAdapter(new ScanResultAdapter());
    }

    private void sortList(List<ScanResult> list) {
        TreeMap<String, ScanResult> map = new TreeMap<>();
        for (ScanResult scanResult : list) {
            map.put(scanResult.SSID, scanResult);
        }
        list.clear();
        list.addAll(map.values());
    }

    private class ScanResultViewHolder extends RecyclerView.ViewHolder {
        private View mView;
        private TextView mWifiName;
        private TextView mWifiLevel;

        ScanResultViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
            mWifiName = (TextView) itemView.findViewById(R.id.ssid);
            mWifiLevel = (TextView) itemView.findViewById(R.id.level);
        }

        void bindScanResult(final ScanResult scanResult) {
            mWifiName.setText(
                    getString(R.string.scan_wifi_name, "" + scanResult.SSID));
            mWifiLevel.setText(
                    getString(R.string.scan_wifi_level, "" + scanResult.level));

            mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int netId = mWifiManager.addNetwork(createWifiConfig(scanResult.SSID, "21123073", WIFICIPHER_WPA));
                    boolean enable = mWifiManager.enableNetwork(netId, true);
                    Log.d("ZJTest", "enable: " + enable);
                    boolean reconnect = mWifiManager.reconnect();
                    Log.d("ZJTest", "reconnect: " + reconnect);
                }
            });
        }
    }

    private static final int WIFICIPHER_NOPASS = 0;
    private static final int WIFICIPHER_WEP = 1;
    private static final int WIFICIPHER_WPA = 2;

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

    private WifiConfiguration isExist(String ssid) {
        List<WifiConfiguration> configs = mWifiManager.getConfiguredNetworks();

        for (WifiConfiguration config : configs) {
            if (config.SSID.equals("\"" + ssid + "\"")) {
                return config;
            }
        }
        return null;
    }

    private class ScanResultAdapter extends RecyclerView.Adapter<ScanResultViewHolder> {
        @Override
        public ScanResultViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(getApplicationContext())
                    .inflate(R.layout.item_scan_result, parent, false);

            return new ScanResultViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ScanResultViewHolder holder, int position) {
            if (mScanResultList != null) {
                holder.bindScanResult(mScanResultList.get(position));
            }
        }

        @Override
        public int getItemCount() {
            if (mScanResultList == null) {
                return 0;
            } else {
                return mScanResultList.size();
            }
        }
    }

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
                mWifiInfoRecyclerView.getAdapter().notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
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
        //unregisterBroadcastReceiver();
    }

    private BroadcastReceiver mBroadcastReceiver;

    private void registerBroadcastReceiver() {
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int state = intent.getIntExtra("wifi_state", 11);
                Log.d("ZJTest", "AP state: " + state);
            }
        };

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.wifi.WIFI_AP_STATE_CHANGED");
        this.registerReceiver(mBroadcastReceiver, intentFilter);
    }

    private void registerNetworkConnectChangeReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        filter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
        filter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        networkConnectChangedReceiver = new NetworkConnectChangedReceiver();
        registerReceiver(networkConnectChangedReceiver, filter);
    }
    NetworkConnectChangedReceiver networkConnectChangedReceiver = new NetworkConnectChangedReceiver();
    class NetworkConnectChangedReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Bundle extras = intent.getExtras();
            Log.e(TAG,"actioin:"+action);
            Log.e(TAG,"==>"+printBundle(extras));

            if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)) {//这个监听wifi的打开与关闭，与wifi的连接无关
                int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, -1);
                Log.e(TAG,"WIFI状态 wifiState:" + wifiState);
                switch (wifiState) {
                    case WifiManager.WIFI_STATE_DISABLED:
                        Log.e(TAG,"WIFI状态 wifiState:WIFI_STATE_DISABLED");
                        break;
                    case WifiManager.WIFI_STATE_DISABLING:
                        Log.e(TAG,"WIFI状态 wifiState:WIFI_STATE_DISABLING");
                        break;
                    case WifiManager.WIFI_STATE_ENABLED:
                        Log.e(TAG,"WIFI状态 wifiState:WIFI_STATE_ENABLED");
                        break;
                    case WifiManager.WIFI_STATE_ENABLING:
                        Log.e(TAG,"WIFI状态 wifiState:WIFI_STATE_ENABLING");
                        break;
                    case WifiManager.WIFI_STATE_UNKNOWN:
                        Log.e(TAG,"WIFI状态 wifiState:WIFI_STATE_UNKNOWN");
                        break;
                    //
                }
            }
            // 这个监听wifi的连接状态即是否连上了一个有效无线路由，当上边广播的状态是WifiManager.WIFI_STATE_DISABLING，和WIFI_STATE_DISABLED的时候，根本不会接到这个广播。
            // 在上边广播接到广播是WifiManager.WIFI_STATE_ENABLED状态的同时也会接到这个广播，当然刚打开wifi肯定还没有连接到有效的无线
            if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action)) {
                Parcelable parcelableExtra = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                WifiInfo wifiInfo = intent.getParcelableExtra(WifiManager.EXTRA_WIFI_INFO);
                String bssid = intent.getStringExtra(WifiManager.EXTRA_BSSID);
                if (null != parcelableExtra && !isJoinTimeOut) {
                    NetworkInfo networkInfo = (NetworkInfo) parcelableExtra;
                    NetworkInfo.State state = networkInfo.getState();
                    Log.e(TAG,"NetWork Sate Change:"+state+" connectedBssid:"+connectedBssid);
                    if(state==NetworkInfo.State.DISCONNECTED){
                        if(connectedBssid.equals(bssid)){
                            boolean findCfg=false;
                            Log.e(TAG,"连接耗时:" + ((System.currentTimeMillis() - start) / 1000.0) + "s");
                            /*if (progressWheelDialog != null) {
                                progressWheelDialog.setTextMsg("灯被成功加入ssid=" + mScanResult.SSID + "的WIFI网络,系统开始切换到指定网络...");
                            }*/
                            List<WifiConfiguration> configuredNetworks = mWifiManager.getConfiguredNetworks();
                            for(WifiConfiguration configuration:configuredNetworks){

                                Log.e(TAG,configuration.SSID+"========>"+mScanResult.SSID);
                                String addSSID=mScanResult.SSID;
                                if(!(mScanResult.SSID.startsWith("\"") && mScanResult.SSID.endsWith("\""))){
                                    addSSID="\""+addSSID+"\"";
                                }
                                if(configuration.SSID.equals(addSSID)){
                                    findCfg=true;
                                    Log.e(TAG,"找到连接wifi的Configuration..尝试切换wifi");
                                    mWifiManager.enableNetwork(configuration.networkId,true);
                                    break;
                                }
                            }
                            if(!findCfg){
                                Log.e(TAG,"未找到连接wifi的Configuration..尝试创建连接...");
                                int type=3;
                                if(TextUtils.isEmpty(mPass)){
                                    type=0;
                                }
                                WifiConfiguration configuration=   WifiHelper.getInstance(context).createWifiConfig(mScanResult.SSID,mPass,type);
                                Log.e(TAG,"新的wifi配置:"+configuration);
                                int newNetId = mWifiManager.addNetwork(configuration);
                                Log.e(TAG,"新的netId="+newNetId);
                                mWifiManager.enableNetwork(newNetId,true);
                            }

                        }
                    }else if(state== NetworkInfo.State.CONNECTED){
                        if(mScanResult!=null) {
                            String ssid = wifiInfo.getSSID();
                            String addSSID=mScanResult.SSID;
                            if(!(mScanResult.SSID.startsWith("\"") && mScanResult.SSID.endsWith("\""))){
                                addSSID="\""+addSSID+"\"";
                            }
                            Log.e(TAG,ssid + "***>" + mScanResult.SSID);
                            Log.e(TAG,"总共耗时:"+((System.currentTimeMillis()-start)/1000.0));
                            if (ssid.equals(addSSID)) {
                                handler.removeCallbacks(watchDog);
//                                dismissLoadDialog(TYPE_LOAD_DIALOG);
                                if(progressWheelDialog!=null) {
                                    progressWheelDialog.dismiss();
                                }
                                Toast.makeText(getApplicationContext(), "操作成功！",Toast.LENGTH_SHORT).show();
                                Intent intent1 = new Intent(WifiActivity.this, MainActivity.class);
                                intent1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent1);
                            }
                        }
                    }
                }
            }
        }

        private  String printBundle(Bundle bundle) {
            StringBuilder sb = new StringBuilder();
            for (String key : bundle.keySet()) {
                if (key.equals(WifiManager.EXTRA_WIFI_STATE)) {
                    sb.append("\nkey:" + key + ", value:" + bundle.getInt(key));
                } else {
                    sb.append("\nkey:" + key + ", value:" + bundle.get(key));
                }
            }
//        Log.e(TAG,"bundle:"+bundle);
            return sb.toString();
        }

    }
    private void unregisterBroadcastReceiver() {
        this.unregisterReceiver(mBroadcastReceiver);
    }
}