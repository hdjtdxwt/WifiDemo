package com.epsit.wifidemo;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.epsit.wifidemo.dialog.InputAndConnectDialog;
import com.epsit.wifidemo.dialog.SelectItemDialog;
import com.epsit.wifidemo.util.WifiInfoManage;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2018/2/27/027.
 */

public class WifiActivity extends AppCompatActivity implements WifiReceiverActionListener, OnItemClickListener, InputAndConnectDialog.OnConnectionListener, OnItemLongClickListener, SelectItemDialog.OnSelectDialogListener {
    private WifiManager mWifiManager;
    private Handler mMainHandler;
    private boolean mHasPermission;
    String TAG = "WifiActivity";
    TextView mOpenWifiButton;
    Switch mGetWifiInfoButton;
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
        mGetWifiInfoButton = (Switch) findViewById(R.id.get_wifi_info);
        listView = (ListView) findViewById(R.id.wifi_info_detail);
        mGetWifiInfoButton.setChecked(WifiManager.WIFI_STATE_ENABLED==mWifiManager.getWifiState());
    }
    private void configChildViews() {

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
                String willConnect = null;
                for (WifiConfiguration cfg : configList) {
                    if (cfg.SSID.equals("\""+ connectSsid+"\"")) {
                        Log.e(TAG,"点击了连接这个网络："+cfg.toString());
                        willConnect = cfg.SSID;
                        mWifiManager.enableNetwork(cfg.networkId, true);
                        break;
                    }
                }
                Log.e(TAG,"-------------->1--start");
                try {
                    List<com.epsit.wifidemo.util.WifiInfo>wifiInfos = new WifiInfoManage().Read();
                    com.epsit.wifidemo.util.WifiInfo targetWifi=null;
                    if(wifiInfos!=null && willConnect!=null){
                        for(com.epsit.wifidemo.util.WifiInfo wifiInfo :wifiInfos){
                            if(willConnect.equals("\""+wifiInfo.Ssid+"\"")){
                                targetWifi = wifiInfo;
                            }
                            Log.e(TAG,"已经保存的wifi的信息：ssid="+wifiInfo.Ssid+"  password="+wifiInfo.Password+"  加密方式mgmt:"+wifiInfo.mgmt);
                        }
                    }
                    Log.e(TAG,"------targetWifi判断是为空");
                    if(targetWifi!=null){
                        Log.e(TAG,"要连接的目标wifi:ssid="+targetWifi.Ssid+"  password="+targetWifi.Password+"  加密方式mgmt:"+targetWifi.mgmt);
                        Log.e(TAG,targetWifi.Ssid+"_"+targetWifi.Password+"_"+targetWifi.mgmt);
                    }else{
                        Log.e(TAG,"没有获取到当前的要连接的wifi的信息（密码）");
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
                Log.e(TAG,"-------------->1--end");
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
    public void test(){
        {

            Intent intent1 =  new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivity(intent1);


            Intent intent2 =  new Intent(Settings.ACTION_ADD_ACCOUNT);
            startActivity(intent2);


            Intent intent3 =  new Intent(Settings.ACTION_AIRPLANE_MODE_SETTINGS);
            startActivity(intent3);



            Intent intent4 =  new Intent(Settings.ACTION_WIFI_SETTINGS);
            startActivity(intent4);


            Intent intent5 =  new Intent(Settings.ACTION_APN_SETTINGS);
            startActivity(intent5);




        Intent intent6 =  new Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS);
            startActivity(intent6);


            Intent intent7 =  new Intent(Settings.ACTION_APPLICATION_SETTINGS);
            startActivity(intent7);


            Intent intent8 =  new Intent(Settings.ACTION_MANAGE_ALL_APPLICATIONS_SETTINGS);
            startActivity(intent8);

            /*ACTION_MANAGE_APPLICATIONS_SETTINGS  ：//  跳转 应用程序列表界面【已安装的】

            Intent intent =  new Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS);
            startActivity(intent);



            8.    ACTION_BLUETOOTH_SETTINGS  ：      // 跳转系统的蓝牙设置界面

            Intent intent =  new Intent(Settings.ACTION_BLUETOOTH_SETTINGS);
            startActivity(intent);

            9.    ACTION_DATA_ROAMING_SETTINGS ：   //  跳转到移动网络设置界面

            Intent intent =  new Intent(Settings.ACTION_DATA_ROAMING_SETTINGS);
            startActivity(intent);

            10.    ACTION_DATE_SETTINGS ：           //  跳转日期时间设置界面

            Intent intent =  new Intent(Settings.ACTION_DATA_ROAMING_SETTINGS);
            startActivity(intent);

            11.    ACTION_DEVICE_INFO_SETTINGS  ：    // 跳转手机状态界面

            Intent intent =  new Intent(Settings.ACTION_DEVICE_INFO_SETTINGS);
            startActivity(intent);

            12.    ACTION_DISPLAY_SETTINGS  ：        // 跳转手机显示界面

            Intent intent =  new Intent(Settings.ACTION_DISPLAY_SETTINGS);
            startActivity(intent);

            13.    ACTION_DREAM_SETTINGS     【API 18及以上 没测试】

            Intent intent =  new Intent(Settings.ACTION_DREAM_SETTINGS);
            startActivity(intent);


            14.    ACTION_INPUT_METHOD_SETTINGS ：    // 跳转语言和输入设备

            Intent intent =  new Intent(Settings.ACTION_INPUT_METHOD_SETTINGS);
            startActivity(intent);

            15.    ACTION_INPUT_METHOD_SUBTYPE_SETTINGS  【API 11及以上】  //  跳转 语言选择界面 【多国语言选择】

            Intent intent =  new Intent(Settings.ACTION_INPUT_METHOD_SUBTYPE_SETTINGS);
            startActivity(intent);

            16.    ACTION_INTERNAL_STORAGE_SETTINGS         // 跳转存储设置界面【内部存储】

            Intent intent =  new Intent(Settings.ACTION_INTERNAL_STORAGE_SETTINGS);
            startActivity(intent);

            或者：

            ACTION_MEMORY_CARD_SETTINGS    ：   // 跳转 存储设置 【记忆卡存储】

            Intent intent =  new Intent(Settings.ACTION_MEMORY_CARD_SETTINGS);
            startActivity(intent);


            17.    ACTION_LOCALE_SETTINGS  ：         // 跳转语言选择界面【仅有English 和 中文两种选择】

            Intent intent =  new Intent(Settings.ACTION_LOCALE_SETTINGS);
            startActivity(intent);


            18.     ACTION_LOCATION_SOURCE_SETTINGS :    //  跳转位置服务界面【管理已安装的应用程序。】

        Intent intent =  new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);

            19.    ACTION_NETWORK_OPERATOR_SETTINGS ： // 跳转到 显示设置选择网络运营商。

            Intent intent =  new Intent(Settings.ACTION_NETWORK_OPERATOR_SETTINGS);
            startActivity(intent);

            20.    ACTION_NFCSHARING_SETTINGS  ：       // 显示NFC共享设置。 【API 14及以上】

            Intent intent =  new Intent(Settings.ACTION_NFCSHARING_SETTINGS);
            startActivity(intent);

            21.    ACTION_NFC_SETTINGS  ：           // 显示NFC设置。这显示了用户界面,允许NFC打开或关闭。  【API 16及以上】

            Intent intent =  new Intent(Settings.ACTION_NFC_SETTINGS);
            startActivity(intent);

            22.    ACTION_PRIVACY_SETTINGS ：       //  跳转到备份和重置界面

            Intent intent =  new Intent(Settings.ACTION_PRIVACY_SETTINGS);
            startActivity(intent);

            23.    ACTION_QUICK_LAUNCH_SETTINGS  ： // 跳转快速启动设置界面

            Intent intent =  new Intent(Settings.ACTION_QUICK_LAUNCH_SETTINGS);
            startActivity(intent);

            24.    ACTION_SEARCH_SETTINGS    ：    // 跳转到 搜索设置界面

            Intent intent =  new Intent(Settings.ACTION_SEARCH_SETTINGS);
            startActivity(intent);

            25.    ACTION_SECURITY_SETTINGS  ：     // 跳转到安全设置界面

            Intent intent =  new Intent(Settings.ACTION_SECURITY_SETTINGS);
            startActivity(intent);

            26.    ACTION_SETTINGS   ：                // 跳转到设置界面

            Intent intent =  new Intent(Settings.ACTION_SETTINGS);
            startActivity(intent);

            27.   ACTION_SOUND_SETTINGS                // 跳转到声音设置界面

            Intent intent =  new Intent(Settings.ACTION_SOUND_SETTINGS);
            startActivity(intent);

            28.   ACTION_SYNC_SETTINGS ：             // 跳转账户同步界面

            Intent intent =  new Intent(Settings.ACTION_SYNC_SETTINGS);
            startActivity(intent);

            29.     ACTION_USER_DICTIONARY_SETTINGS ：  //  跳转用户字典界面

            Intent intent =  new Intent(Settings.ACTION_USER_DICTIONARY_SETTINGS);
            startActivity(intent);

            30.     ACTION_WIFI_IP_SETTINGS  ：         // 跳转到IP设定界面  */

            Intent intent =  new Intent(Settings.ACTION_WIFI_IP_SETTINGS);
            startActivity(intent);


            Intent intent31 =  new Intent(Settings.ACTION_WIFI_SETTINGS );//wifi设置
            startActivity(intent31);
        }
    }
}