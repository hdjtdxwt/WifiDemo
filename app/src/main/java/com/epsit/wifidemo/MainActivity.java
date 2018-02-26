package com.epsit.wifidemo;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    Button scan,open;
    ListView listView;
    WifiManager wifiManager ;
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
                wifiManager.setWifiEnabled(true);
                break;

        }
    }

    //显示所有的列表
    public void showWifiList(){
        wifiManager.startScan();
        List<ScanResult> list = wifiManager.getScanResults();
        Log.e("wifiList",list==null?"0":""+list.size());
        ScanResultAdapter adapter = new ScanResultAdapter(getApplicationContext(), list);
        listView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }
}
