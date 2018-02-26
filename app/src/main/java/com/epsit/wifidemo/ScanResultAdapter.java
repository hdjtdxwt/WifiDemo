package com.epsit.wifidemo;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Administrator on 2018/2/26.
 */

public class ScanResultAdapter extends BaseAdapter {
    List<ScanResult> mList;
    Context mContext;
    public ScanResultAdapter(Context context , List<ScanResult> list ) {
        mList = list;
        mContext = context;
    }
    @Override
    public int getCount() {
        return mList!=null ? mList.size():0;//数目
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view;
        if (convertView == null) {
            //因为getView()返回的对象，adapter会自动赋给ListView
            view = inflater.inflate(R.layout.list_item, null);
        } else {
            view = convertView;
            Log.i("info", "有缓存，不需要重新生成" + position);
        }
        TextView tv1 = (TextView) view.findViewById(R.id.tv1);//找到Textviewname
        tv1.setText(mList.get(position).SSID);//设置参数

        TextView tv2 = (TextView) view.findViewById(R.id.tv2);//找到Textviewage
        tv2.setText(mList.get(position).capabilities);//设置参数
        return view;
    }
    @Override
    public long getItemId(int position) {//取在列表中与指定索引对应的行id
        return 0;
    }
    @Override
    public ScanResult getItem(int position) {//获取数据集中与指定索引对应的数据项
        return mList.get(position);
    }
}
