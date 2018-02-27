package com.epsit.wifidemo;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Administrator on 2018/2/26.
 */

public class ScanResultAdapter extends BaseAdapter {
    List<ItemBean> mList;
    Context mContext;
    String name;
    public ScanResultAdapter(Context context, List<ItemBean> list,String selectedName) {
        mList = list;
        mContext = context;
        name = selectedName;
    }

    @Override
    public int getCount() {
        return mList != null ? mList.size() : 0;//数目
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view;
        ItemBean scanResult = mList.get(position);
        if (convertView == null) {
            //因为getView()返回的对象，adapter会自动赋给ListView
            view = inflater.inflate(R.layout.list_item, null);
        } else {
            view = convertView;
            Log.i("info", "adapter有缓存，不需要重新生成" + position);
        }
        TextView tv0 = (TextView) view.findViewById(R.id.tv0);//找到Textviewname
        tv0.setText(Math.abs(scanResult.level)+"");//设置参数

        TextView tv1 = (TextView) view.findViewById(R.id.tv1);//找到Textviewname
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(scanResult.getScanResult().SSID);
        if(name!=null && name.equals(scanResult.getScanResult().SSID)){
            stringBuffer.append("(已连接)");
        }else{
        }
        tv1.setText(stringBuffer);//设置参数

        TextView tv2 = (TextView) view.findViewById(R.id.tv2);//找到Textviewage
        tv2.setText(scanResult.getScanResult().BSSID);//设置参数

        /*TextView state = view.findViewById(R.id.state);
        if(name!=null && name.equals(scanResult.getScanResult().SSID)){
            state.setText("已连接");
        }else{
            state.setText("");
        }*/
        ImageView imageView = (ImageView) view.findViewById(R.id.imageLevel);
        //判断信号强度，显示对应的指示图标
        /*if (Math.abs(scanResult.level) > 100) {
            imageView.setImageDrawable(mContext.getResources().getDrawable(R.drawable.stat_sys_wifi_signal_0));
        } else if (Math.abs(scanResult.level) > 80) {
            imageView.setImageDrawable(mContext.getResources().getDrawable(R.drawable.stat_sys_wifi_signal_1));
        } else if (Math.abs(scanResult.level) > 70) {
            imageView.setImageDrawable(mContext.getResources().getDrawable(R.drawable.stat_sys_wifi_signal_2));
        } else if (Math.abs(scanResult.level) > 60) {
            imageView.setImageDrawable(mContext.getResources().getDrawable(R.drawable.stat_sys_wifi_signal_3));
        } else if (Math.abs(scanResult.level) > 50) {
            imageView.setImageDrawable(mContext.getResources().getDrawable(R.drawable.stat_sys_wifi_signal_4));
        } else {
            imageView.setImageDrawable(mContext.getResources().getDrawable(R.drawable.stat_sys_wifi_signal_5));
        }*/
        return view;
    }

    @Override
    public long getItemId(int position) {//取在列表中与指定索引对应的行id
        return 0;
    }

    @Override
    public ItemBean getItem(int position) {//获取数据集中与指定索引对应的数据项
        return mList.get(position);
    }
}
