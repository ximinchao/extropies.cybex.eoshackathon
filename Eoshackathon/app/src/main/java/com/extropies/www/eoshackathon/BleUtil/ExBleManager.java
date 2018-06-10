package com.extropies.www.eoshackathon.BleUtil;

import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.extropies.www.eoshackathon.Utils.HexUtil;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID; 

/**
 * Created by inst on 18-6-9.
 */

public class ExBleManager {
    public static final int CANCEL_SCAN =8;
    public static final int STOP_SCAN =9;
    public static final int START_CONNECT =10;
    public static final int START_SCAN_SUCCESS =11;
    public static final int START_SCAN_FAIL = 12;
    public static final int GATT_CONNECT_START_CONNECT = 13;
    public static final int GATT_CONNECT_FAIL = 14;
    public static final int GATT_CONNECT_SUCCESS = 15;
    public static final int GATT_CHRCHANGE = 16;
    public static final int GATT_DISCONNECT = 17;
    public static final int GATT_SERVICES_DISCOVERED = 18;
    public static final int GATT_SERVICES_DISCOVERED_FAIL = 19;

    public static final int HEARTBEAT_RESPONE = 30;
    public static final int CMD_RESPONE = 31;
    public static final int CMD_WRITE_SUCCESS = 32;
    public static final int CMD_WRITE_FAIL = 33;

    private Application context;
    private BluetoothAdapter bluetoothAdapter;
    //    private HandlerThread mHandlerThread;
    private BleHandler mBleHandler;
    private String BleNameFilter;
    private String BleServiceUUID;
    private String BleReadUUID;
    private String BleWriteUUID;
    private List<ExBleDevice> mBleDeviceList = new ArrayList<>();

    private String BleHeartBeatString;
    private String BleDataHeadString;
    private String BleWriteHeadString;
    private String heartBeatString;

    private ExBleScanCallback mBleScanCallback;
    private ExBleGattCallback mBleGattCallback;
    private ExBleNotifyCallback mBleNotifyCallback;
    private ExBleWriteCallback mBleWriteCallback;

    public static ExBleManager getInstance() {
        return BleManagerHolder.sBleManager;
    }
    private static class BleManagerHolder {
        private static final ExBleManager sBleManager = new ExBleManager();
    }

    public void ExBleManagerInit(Application app) {
        if (context == null && app != null) {
            context = app;
            BluetoothManager bluetoothManager = (BluetoothManager) context
                    .getSystemService(Context.BLUETOOTH_SERVICE);
            if (bluetoothManager != null){
                bluetoothAdapter = bluetoothManager.getAdapter();
            }
            mBleHandler = new BleHandler(context.getMainLooper(), this);
            BleNameFilter = "";
            BleServiceUUID = "";
            BleReadUUID = "";
            BleWriteUUID = "";
            heartBeatString = "";
        }

    }
    public Context getContext() {
        return context;
    }
    public BluetoothAdapter getBluetoothAdapter() {
        return bluetoothAdapter;
    }

    public boolean isBlueEnable() {
        return bluetoothAdapter != null && bluetoothAdapter.isEnabled();
    }
    public void enableBluetooth() {
        if (bluetoothAdapter != null) {
            bluetoothAdapter.enable();
        }
    }
    public void disableBluetooth() {
        if (bluetoothAdapter != null) {
            if (bluetoothAdapter.isEnabled())
                bluetoothAdapter.disable();
        }
    }
    public void setBleNameFilter(String filter){
        this.BleNameFilter = filter;
    }
    public void setBleReadWriteUUID(String bleServiceUUID,String bleWriteUUID,String bleReadUUID){
        this.BleServiceUUID = bleServiceUUID;
        this.BleWriteUUID = bleWriteUUID;
        this.BleReadUUID = bleReadUUID;
    }
    public void setSelfBleProtocol(String heartbeatString,String readHeadString,String writeString){
        this.BleHeartBeatString = heartbeatString;
        this.BleDataHeadString = readHeadString;
        this.BleWriteHeadString = writeString;
    }

    private ExBleDevice getDeviceFromGatt(BluetoothGatt gatt) {
        for (ExBleDevice device:mBleDeviceList) {
            if (gatt.getDevice().getAddress().equals(device.getDevice().getAddress())) {
                return device;
            }
        }
        return null;
    }

    public void cancelScan() {
        if (mBleHandler.hasMessages(STOP_SCAN)){
            mBleHandler.removeMessages(STOP_SCAN);
        }
        mBleHandler.sendEmptyMessage(CANCEL_SCAN);
    }
    public void scan(ExBleScanCallback callback) {
        if (callback == null) {
            throw new IllegalArgumentException("BleScanCallback can not be Null!");
        }
        mBleScanCallback = callback;
        if (!isBlueEnable()) {
            mBleHandler.sendEmptyMessage(START_SCAN_FAIL);
            return;
        }
        boolean startScanRes = bluetoothAdapter.startLeScan(mLeScanCallback);
        if (!startScanRes){
            mBleHandler.sendEmptyMessage(START_SCAN_FAIL);
            return;
        }else {
            mBleHandler.sendEmptyMessage(START_SCAN_SUCCESS);
        }
        mBleHandler.sendEmptyMessageDelayed(STOP_SCAN, 10000);
    }
    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice bluetoothDevice, int i, byte[] bytes) {
            ExBleDevice scanDevice = new ExBleDevice(bluetoothDevice);
            mBleScanCallback.onLeScan(scanDevice);
            if (BleNameFilter.equals("")){
                mBleDeviceList.add(scanDevice);
                mBleScanCallback.onScanning(scanDevice);
                return;
            }
            if(bluetoothDevice.getName() != null && bluetoothDevice.getName().contains(BleNameFilter)){
                mBleDeviceList.add(scanDevice);
                mBleScanCallback.onScanning(scanDevice);
            }
        }
    };

    public BluetoothGatt connect(ExBleDevice bleDevice, ExBleGattCallback bleGattCallback) {
        if (bleGattCallback == null) {
            throw new IllegalArgumentException("ExBleGattCallback can not be Null!");
        }
        if (bleDevice.getDevice() == null){
            throw new IllegalArgumentException("ExBleDevice Device can not be Null!");
        }
        if (!isBlueEnable()) {
            bleGattCallback.onConnectFail(new OtherException("Bluetooth not enable!"));
            return null;
        }

        if (Looper.myLooper() == null || Looper.myLooper() != Looper.getMainLooper()) {
            throw new IllegalArgumentException("Be careful: currentThread is not MainThread");
        }

        mBleGattCallback = bleGattCallback;
        mBleHandler.sendEmptyMessage(GATT_CONNECT_START_CONNECT);
        return bleDevice.getDevice().connectGatt(context,false,mGattCallback);
    }
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback(){
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Message message = Message.obtain();
                message.obj = gatt;
                message.what = GATT_DISCONNECT;
                mBleHandler.sendMessage(message);
            }else if (newState == BluetoothProfile.STATE_CONNECTED){
                gatt.discoverServices();
                Message message = Message.obtain();
                message.obj = gatt;
                message.what = GATT_CONNECT_SUCCESS;
                mBleHandler.sendMessage(message);
            }else{
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            if (status == BluetoothGatt.GATT_SUCCESS){
                Message message = Message.obtain();
                message.obj = gatt;
                message.what = GATT_SERVICES_DISCOVERED;
                mBleHandler.sendMessage(message);
            }else {
                Message message = Message.obtain();
                message.obj = gatt;
                message.what = GATT_SERVICES_DISCOVERED_FAIL;
                mBleHandler.sendMessage(message);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            byte[] data = characteristic.getValue();
            String bletempdatastring = HexUtil.byte2hex(data);
            if (bletempdatastring.equals(BleHeartBeatString)) {
                ExBleDevice device = getDeviceFromGatt(gatt);
                if (device != null) {
                    device.setDeviceHeartBeatString(device.getDeviceHeartBeatString()+BleHeartBeatString);
                    System.out.println("========================onCharacteristicChanged HeartBeat in:"+bletempdatastring + "    all data:"+device.getDeviceHeartBeatString());
                    Message message = Message.obtain();
                    message.obj = device;
                    message.what = HEARTBEAT_RESPONE;
                    mBleHandler.sendMessage(message);
                }
            }else{
                ExBleDevice device = getDeviceFromGatt(gatt);
                if (device != null) {
                    device.setRespontString(device.getDeviceRespontString()+bletempdatastring);
                    device.setDeviceHeartBeatString(device.getDeviceHeartBeatString()+BleHeartBeatString);
                    System.out.println("========================onCharacteristicChanged data in:"+bletempdatastring + "    all data:"+device.getDeviceRespontString());
                    bleDataPackageCheck(device,BleDataHeadString);
                }
            }
        }

    };

    public void bleDataPackageCheck(ExBleDevice device,String bleDataHeadString){
        if (BleDataPackage.checkRspDataComplete(device.getDeviceRespontString(),bleDataHeadString)){
            Message message = Message.obtain();
            message.obj = device;
            message.what = CMD_RESPONE;
            mBleHandler.sendMessage(message);
        }
    }
    public boolean setReadChrNotificationByGatt(BluetoothGatt mGatt){
        System.out.println("========================setReadChrNotificationByGatt=====");
        if (mGatt == null) {
            return false;
        }
        BluetoothGattCharacteristic readCharacteristic = getCharcteristicByGatt(mGatt,BleServiceUUID,BleReadUUID);  //这个UUID都是根据协议号的UUID
        if (readCharacteristic == null) {
            return false;
        }
        // Check characteristic property
        final int properties = readCharacteristic.getProperties();
        if ((properties & BluetoothGattCharacteristic.PROPERTY_NOTIFY) == 0) {
            return false;
        }

        System.out.println("========================gatt.setCharacteristicNotification:  " + readCharacteristic.getUuid());//Logger.d(mLogSession, "gatt.setCharacteristicNotification(" + characteristic.getUuid() + ", true)");
        boolean res = mGatt.setCharacteristicNotification(readCharacteristic, true);
        List<BluetoothGattDescriptor> descriptors=readCharacteristic.getDescriptors();
        for(BluetoothGattDescriptor dp:descriptors){
            dp.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            mGatt.writeDescriptor(dp);
        }
        return res;
    }
    private BluetoothGattCharacteristic getCharcteristicByGatt(BluetoothGatt mGatt,String serviceUUID, String characteristicUUID) {
        BluetoothGattService service = getServiceByGatt(mGatt, UUID.fromString(serviceUUID));
        if (service == null) {
            return null;
        }
        final BluetoothGattCharacteristic gattCharacteristic = service.getCharacteristic(UUID.fromString(characteristicUUID));
        if (gattCharacteristic != null) {
            return gattCharacteristic;
        } else {
            return null;
        }
    }
    public BluetoothGattService getServiceByGatt(BluetoothGatt mGatt,UUID uuid){
        if (mGatt == null) {
            return null;
        }
        return mGatt.getService(uuid);
    }

    public void enableReadWriteChr(BluetoothGatt gatt,ExBleNotifyCallback notifyCallback,ExBleWriteCallback writeCallback) {
        boolean res = setReadChrNotificationByGatt(gatt);
        this.mBleNotifyCallback = notifyCallback;
        this.mBleWriteCallback = writeCallback;
        ExBleDevice device = getDeviceFromGatt(gatt);
        if (device != null) {
            device.setRespontString("");
            device.setDeviceHeartBeatString("");
        }
    }

    public void writeByGatt(BluetoothGatt mGatt,byte[] data) {
        BluetoothGattCharacteristic writeCharacteristic = getCharcteristicByGatt(mGatt,BleServiceUUID,BleWriteUUID);  //这个UUID都是根据协议号的UUID
        if (writeCharacteristic == null || mGatt == null) {
            return;
        }
        String writeString = new String(data);
        boolean writeRes = false;
        System.out.println("========================writeByGatt====" );//+ writeString
        int dataRemainLength = data.length;
        int dataOffset = 0;
        byte[] dataToSend = new byte[20];
        while (dataRemainLength>20){
            System.arraycopy(data, dataOffset, dataToSend,0, 20);
            writeCharacteristic.setValue(dataToSend);
            writeRes = mGatt.writeCharacteristic(writeCharacteristic);
            if (!writeRes) {
                Message message = Message.obtain();
                message.obj = mGatt;
                message.what = CMD_WRITE_FAIL;
                mBleHandler.sendMessage(message);
                return;
            }
            dataOffset +=20;
            dataRemainLength -=20;
            try
            {
                Thread.currentThread().sleep(10);
            }
            catch(Exception e){}
        }
        if (dataRemainLength!=0){
            byte[] dataToSendRemain = new byte[dataRemainLength];
            System.arraycopy(data, dataOffset, dataToSendRemain,0, dataRemainLength);
            writeCharacteristic.setValue(dataToSendRemain);
            writeRes = mGatt.writeCharacteristic(writeCharacteristic);
            if (!writeRes) {
                Message message = Message.obtain();
                message.obj = mGatt;
                message.what = CMD_WRITE_FAIL;
                mBleHandler.sendMessage(message);
                return;
            }
        }

        Message message = Message.obtain();
        message.obj = mGatt;
        message.what = CMD_WRITE_SUCCESS;
        mBleHandler.sendMessage(message);
        return;
    }

    private void disconnectByGatt(BluetoothGatt gatt){
        if (gatt!=null){
            gatt.disconnect();
            gatt.close();
        }
    }
    public void disconnectByDevice(ExBleDevice device,BluetoothGatt gatt){
        disconnectByGatt(gatt);
        device.setIsConnected(false);
        device.setIsServDisced(false);
    }

    private class BleHandler extends Handler {
        private final WeakReference<ExBleManager> reference;
        BleHandler(Looper looper, ExBleManager exBleManager) {
            super(looper);
            reference = new WeakReference<>(exBleManager);
        }
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case START_SCAN_FAIL:
                    mBleScanCallback.onScanStarted(false);
                    break;
                case START_SCAN_SUCCESS:
                    mBleScanCallback.onScanStarted(true);
                    break;

                case CANCEL_SCAN:
                    bluetoothAdapter.stopLeScan(mLeScanCallback);
                    mBleScanCallback.onScanFinished(mBleDeviceList);
                    break;
                case STOP_SCAN:
                    bluetoothAdapter.stopLeScan(mLeScanCallback);
                    mBleScanCallback.onScanFinished(mBleDeviceList);
                    break;
                case START_CONNECT:
//                    connect();  //
                    break;
                case GATT_CONNECT_START_CONNECT:
                    mBleGattCallback.onStartConnect();
                    break;
                case GATT_CONNECT_FAIL:
                    break;
                case GATT_CONNECT_SUCCESS:
                    mBleGattCallback.onConnectSuccess((BluetoothGatt)msg.obj,GATT_CONNECT_SUCCESS);
                    break;
                case GATT_SERVICES_DISCOVERED:
                    mBleGattCallback.onServicesDiscovered((BluetoothGatt)msg.obj,GATT_SERVICES_DISCOVERED);
                    break;
                case GATT_SERVICES_DISCOVERED_FAIL:
                    mBleGattCallback.onServicesDiscovered((BluetoothGatt)msg.obj,GATT_SERVICES_DISCOVERED_FAIL);
                    break;
                case GATT_DISCONNECT:
                    mBleGattCallback.onDisConnected((BluetoothGatt)msg.obj,GATT_DISCONNECT);
                    break;
                case GATT_CHRCHANGE:
                    break;
                case HEARTBEAT_RESPONE:
                    mBleNotifyCallback.heartBeatNotify((ExBleDevice)msg.obj);
                    break;
                case CMD_RESPONE:
                    mBleNotifyCallback.cmdRespone((ExBleDevice)msg.obj);
                    break;
                case CMD_WRITE_FAIL:
                    mBleWriteCallback.onWriteFailure((BluetoothGatt)msg.obj);
                    break;
                case CMD_WRITE_SUCCESS:
                    mBleWriteCallback.onWriteSuccess((BluetoothGatt)msg.obj);
                    break;
                default:
                    break;
            }
        };
    }
}
