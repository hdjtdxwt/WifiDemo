package com.epsit.wifidemo;

import android.net.wifi.ScanResult;
import android.support.annotation.NonNull;

/**
 * Created by Administrator on 2018/2/27/027.
 */

public class ItemBean implements Comparable{
    int level;
    ScanResult scanResult;

    public ItemBean(int level, ScanResult scanResult) {
        this.level = level;
        this.scanResult = scanResult;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public ScanResult getScanResult() {
        return scanResult;
    }

    public void setScanResult(ScanResult scanResult) {
        this.scanResult = scanResult;
    }

    @Override
    public int compareTo(@NonNull Object o) {

        if(o==null ||  ! (o instanceof ItemBean)){
            return -1;
        }
        return  this.level- ((ItemBean)o).level ;
    }
}
