package com.extropies.www.eoshackathon.BleUtil;

import android.bluetooth.BluetoothGatt;

/**
 * Created by inst on 18-6-9.
 */

public abstract class ExBleWriteCallback {
    public abstract void onWriteSuccess(BluetoothGatt mGatt);
    public abstract void onWriteFailure(BluetoothGatt mGatt);
}
