package com.extropies.www.eoshackathon.BleUtil;

/**
 * Created by inst on 18-6-9.
 */

public abstract class ExBleNotifyCallback {
    public abstract void heartBeatNotify(ExBleDevice device);
    public abstract void cmdRespone(ExBleDevice device);
}
