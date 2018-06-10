package com.extropies.www.eoshackathon.BleUtil;

import android.bluetooth.BluetoothGatt;

/**
 * Created by inst on 18-6-9.
 */

public abstract class ExBleGattCallback {

    public abstract void onStartConnect();

    public abstract void onConnectFail(ExBleException exception);

    public abstract void onConnectSuccess(BluetoothGatt gatt, int status);

    public abstract void onServicesDiscovered(BluetoothGatt gatt, int status);

    public abstract void onDisConnected(BluetoothGatt gatt, int status);

}
