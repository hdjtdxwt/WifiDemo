package com.epsit.wifidemo;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Administrator on 2018/3/8/008.
 */

public class ResultListAdapter extends AppBaseAdapter<ScanResult> {
    String connectedName;
    List<String>wifiConfigurationList;
    OnItemClickListener listener;
    OnItemLongClickListener longClickListener;

    public ResultListAdapter(Context context, List<ScanResult> list) {
        super(context, list);
    }

    public OnItemLongClickListener getLongClickListener() {
        return longClickListener;
    }

    public void setLongClickListener(OnItemLongClickListener longClickListener) {
        this.longClickListener = longClickListener;
    }

    public List<String> getWifiConfigurationList() {
        return wifiConfigurationList;
    }

    public void setWifiConfigurationList(List<String> wifiConfigurationList) {
        this.wifiConfigurationList = wifiConfigurationList;
    }

    public OnItemClickListener getListener() {
        return listener;
    }

    public void setListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public String getConnectedName() {
        return connectedName;
    }

    public void setConnectedName(String connectedName) {
        this.connectedName = connectedName;
    }

    @Override
    public View getItemView(final int position, View convertView, ViewGroup parent) {
        /*if(convertView==null){
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.list_item,null);
        }*/
        //这里为了点击事件不会乱，每一个convertView都是新的
        LayoutInflater inflater = LayoutInflater.from(context);
        convertView = inflater.inflate(R.layout.list_item,null);
        TextView levelNum = (TextView) convertView.findViewById(R.id.tv0);//信号值
        TextView name = (TextView) convertView.findViewById(R.id.tv1);//wifi名字
        TextView mac = (TextView) convertView.findViewById(R.id.tv2);//mac值
        TextView state = (TextView) convertView.findViewById(R.id.state);//状态
        ImageView imageView = (ImageView) convertView.findViewById(R.id.imageLevel);
        ScanResult result = list.get(position);
        name.setText(result.SSID);
        mac.setText(result.BSSID);
        if(!TextUtils.isEmpty(connectedName) && connectedName.equals("\""+result.SSID+"\"")){
            state.setText("已连接");
        }else{
            if(WifiHelper.getInstance(context).isExist(result.SSID)!=null){
                state.setText("已保存");
                convertView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        if(longClickListener!=null){
                            longClickListener.onItemLongClick(v,position);
                        }
                        return true;
                    }
                });
            }else{
                state.setText("");
                convertView.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v) {
                        if(listener!=null){
                            listener.onItemClick(v,position);
                        }
                    }
                });
            }
        }
        levelNum.setText(result.level+"");
        if (Math.abs(result.level) > 90) {
            imageView.setImageDrawable(context.getResources().getDrawable(R.drawable.wifi_level4));
        } else if (Math.abs(result.level) > 70) {
            imageView.setImageDrawable(context.getResources().getDrawable(R.drawable.wifi_level3));
        } else if (Math.abs(result.level) > 50) {
            imageView.setImageDrawable(context.getResources().getDrawable(R.drawable.wifi_level2));
        }else{
            imageView.setImageDrawable(context.getResources().getDrawable(R.drawable.wifi_level1));
        }
        /*convertView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(listener!=null){
                    listener.onItemClick(v,position);
                }
            }
        });*/

        return convertView;
    }
}
