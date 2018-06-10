package com.extropies.www.eoshackathon.BleUtil;

import java.util.List;

/**
 * Created by inst on 18-6-9.
 */

public abstract class ExBleScanCallback {
    public abstract void onScanStarted(boolean success);
    public abstract void onScanning(ExBleDevice bleDevice);
    public abstract void onScanFinished(List<ExBleDevice> scanResultList);
    public void onLeScan(ExBleDevice bleDevice){}
}
