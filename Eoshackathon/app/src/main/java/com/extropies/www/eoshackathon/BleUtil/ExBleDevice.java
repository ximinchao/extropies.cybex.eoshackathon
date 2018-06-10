package com.extropies.www.eoshackathon.BleUtil;

import android.bluetooth.BluetoothDevice;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by inst on 18-6-9.
 */ 

public class ExBleDevice implements Parcelable {
    private BluetoothDevice mDevice;
    private byte[] mScanRecord;
    private int mRssi;
    private long mTimestampNanos;
    private int isConnected;
    private int isServDisced;
    private String deviceRespontString;
    private String deviceHeartBeatString;
    private String deviceCmdString;

    public ExBleDevice(BluetoothDevice device) {
        mDevice = device;
    }

    public ExBleDevice(BluetoothDevice device, int rssi, byte[] scanRecord, long timestampNanos) {
        mDevice = device;
        mScanRecord = scanRecord;
        mRssi = rssi;
        mTimestampNanos = timestampNanos;
    }

    protected ExBleDevice(Parcel in) {
        mDevice = in.readParcelable(BluetoothDevice.class.getClassLoader());
        mScanRecord = in.createByteArray();
        mRssi = in.readInt();
        mTimestampNanos = in.readLong();
        isConnected = in.readInt();
        isServDisced = in.readInt();
        deviceRespontString = in.readString();
        deviceHeartBeatString = in.readString();
        deviceCmdString = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(mDevice, flags);
        dest.writeByteArray(mScanRecord);
        dest.writeInt(mRssi);
        dest.writeLong(mTimestampNanos);
        dest.writeInt(isConnected);
        dest.writeInt(isServDisced);
        dest.writeString(deviceRespontString);
        dest.writeString(deviceHeartBeatString);
        dest.writeString(deviceCmdString);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ExBleDevice> CREATOR = new Creator<ExBleDevice>() {
        @Override
        public ExBleDevice createFromParcel(Parcel in) {
            return new ExBleDevice(in);
        }

        @Override
        public ExBleDevice[] newArray(int size) {
            return new ExBleDevice[size];
        }
    };

    public boolean getIsConnected(){
        return isConnected!=0;
    }
    public void setIsConnected(boolean flag){
        if (flag){
            isConnected = 1;
        }else {
            isConnected = 0;
        }
    }
    public boolean getIsServDisced(){
        return isServDisced!=0;
    }
    public void setIsServDisced(boolean flag){
        if (flag){
            isServDisced = 1;
        }else {
            isServDisced = 0;
        }
    }
    public String getName() {
        if (mDevice != null)
            return mDevice.getName();
        return null;
    }

    public BluetoothDevice getDevice() {
        return mDevice;
    }

    public void setDevice(BluetoothDevice device) {
        this.mDevice = device;
    }

    public void setRespontString(String deviceRespontString) {
        this.deviceRespontString = deviceRespontString;
    }
    public String getDeviceRespontString(){
        return deviceRespontString;
    }

    public void setDeviceHeartBeatString(String heartBeatString) {
        this.deviceHeartBeatString = heartBeatString;
    }
    public void setDeviceCmdString(String deviceCmdString){
        this.deviceCmdString = deviceCmdString;
    }

    public String getDeviceCmdString() {
        return deviceCmdString;
    }
    public String getDeviceHeartBeatString() {
        return deviceHeartBeatString;
    }
}
