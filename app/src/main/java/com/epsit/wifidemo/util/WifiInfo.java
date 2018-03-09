package com.epsit.wifidemo.util;


import android.os.Parcel;
import android.os.Parcelable;

public class WifiInfo implements Parcelable{
    public String Ssid="";
    public String Password="";
    public String mgmt="NONE";
    public WifiInfo(){}
    protected WifiInfo(Parcel in) {
        Ssid = in.readString();
        Password = in.readString();
        mgmt = in.readString();
    }

    public static final Creator<WifiInfo> CREATOR = new Creator<WifiInfo>() {
        @Override
        public WifiInfo createFromParcel(Parcel in) {
            return new WifiInfo(in);
        }

        @Override
        public WifiInfo[] newArray(int size) {
            return new WifiInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(Ssid);
        dest.writeString(Password);
        dest.writeString(mgmt);
    }
}