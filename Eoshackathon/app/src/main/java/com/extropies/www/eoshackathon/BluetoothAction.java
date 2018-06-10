package com.extropies.www.eoshackathon;

import android.app.Activity;
import android.app.Application;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothGatt;
import android.os.Handler;
import android.os.Looper;
import android.os.Message; 

import com.extropies.www.eoshackathon.BleUtil.BleDataPackage;
import com.extropies.www.eoshackathon.BleUtil.ExBleDevice;
import com.extropies.www.eoshackathon.BleUtil.ExBleException;
import com.extropies.www.eoshackathon.BleUtil.ExBleGattCallback;
import com.extropies.www.eoshackathon.BleUtil.ExBleManager;
import com.extropies.www.eoshackathon.BleUtil.ExBleNotifyCallback;
import com.extropies.www.eoshackathon.BleUtil.ExBlePermission;
import com.extropies.www.eoshackathon.BleUtil.ExBleScanCallback;
import com.extropies.www.eoshackathon.BleUtil.ExBleWriteCallback;
import com.extropies.www.eoshackathon.Utils.HexUtil;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import static com.extropies.www.eoshackathon.MainActivity.BLE_SIGN_TRANSATION_RSP;
import static com.extropies.www.eoshackathon.MainActivity.BLE_SIGN_TRANSATION_TIMEOUT;
import static com.extropies.www.eoshackathon.MainActivity.BLE_START_SCAN_AGAIN;
import static com.extropies.www.eoshackathon.MainActivity.LOG_TEXT_APPEND;

/**
 * Created by inst on 18-6-9.
 */

public class BluetoothAction {

    public static ExBleDevice mExBleDevice = null;
    public static BluetoothGatt mBleGatt;
    private static final String Customer_Ble_Device_Filter = "PixelauthCard";
    ProgressDialog mProgressDialog;
    public boolean startScanAgainFlag = false;
    public boolean bleReady = false;
    public int fingerprintTryNum = 0;

    private static final int CMD_TIMEOUT = 30;
    private static final int CMD_RESPONE =  31;
    public static final int CMD_GET_FP_LIST = 32;
    public static final int CMD_DEL_FP = 33;
    public static final int CMD_SET_SIGN_INFO = 34;
    public static final int CMD_SET_SIGN_INFO_TWO = 35;
    public static final int CMD_SET_SIGN_INFO_THREE = 36;
    public static final int CMD_SET_SIGN_INFO_FOUR = 37;
    public static final int CMD_GEY_SIGN_RESULT = 38;
    public static final int CMD_SHOW_QRCODE  = 39;
    public static final int BLE_TAG_DEVICE_DISCONNECT = 50;
    public static final int BLE_TAG_HEARTBEAT = 51;

    private BluetoothCMDHandler mBleHandler;
    private Activity bleRelayActivity;

    /////////////////////////////////////////////////
    private String serializeStringPartOne = "";
    private String serializeStringPartTwo= "";
    private String serializeStringPartThree = "";
    private String serializeStringPartFour = "";
    private String chainId = "";
    private String qrCodeString = "";
    private boolean disconnectAfterRsp;
    private boolean showQRcodeAfterBleReady = false;
    private Handler guiHandler;

    public String BluetoothInitialize(Application application, Activity activity, Handler handler) {
        mBleHandler = new BluetoothCMDHandler(activity.getMainLooper(), activity);
        bleRelayActivity = activity;
        guiHandler = handler;
        startScanAgainFlag = true;

        if (mExBleDevice == null || !mExBleDevice.getIsConnected()) {
            mExBleDevice = null;
            ExBleManager.getInstance().ExBleManagerInit(application);
            ExBleManager.getInstance().setBleNameFilter(Customer_Ble_Device_Filter);
            ExBleManager.getInstance().setBleReadWriteUUID(
                    "6e4000f1-b5a3-f393-e0a9-e50e24dcca9e",
                    "6e4000f2-b5a3-f393-e0a9-e50e24dcca9e",
                    "6e4000f3-b5a3-f393-e0a9-e50e24dcca9e");

            if (ExBlePermission.checkPermissions(activity)) {
                return "OK";
            } else {
                logTextAppend("Bluetooth permission denied");
                return "permissionDenied";
            }
        } else {
            ExBleManager.getInstance().disconnectByDevice(mExBleDevice,mBleGatt);
            mBleGatt = null;
            return "disconnectBle";
        }
    }

    public void disconnectBluetooth() {
        mBleHandler.sendEmptyMessage(BLE_TAG_DEVICE_DISCONNECT);
    }

    public boolean getConnectStatus() {
        if (mExBleDevice != null && mExBleDevice.getIsConnected()) {
            return true;
        } else {
            return false;
        }
    }
    public void logTextAppend(String input) {
        Message message = Message.obtain();
        message.obj = input;
        message.what = LOG_TEXT_APPEND;
        guiHandler.sendMessage(message);
    }
    public void startScan() {
        ExBleManager.getInstance().scan(new ExBleScanCallback() {
            @Override
            public void onScanStarted(boolean success) {
                System.out.println("========================onScanStarted===========================");
                if (success) {
                    bleReady = false;
                    serializeStringPartFour = "";
                    serializeStringPartOne = "";
                    serializeStringPartThree = "";
                    serializeStringPartTwo = "";
                    chainId = "";
                    //显示等待对话框
                    mProgressDialog = new ProgressDialog(bleRelayActivity);
                    mProgressDialog.setProgress(ProgressDialog.STYLE_HORIZONTAL);
                    mProgressDialog.setIndeterminate(false);
                    mProgressDialog.setCancelable(false);
                    mProgressDialog.setMessage("Connecting……");
                    mProgressDialog.show();

                    logTextAppend("Bluetooth startScan");
                }else {
                    System.out.println("========================onScanStarted failed ===========================");
                    if (startScanAgainFlag) {
                        startScanAgainFlag = false;
                        mBleHandler.sendEmptyMessageDelayed(BLE_START_SCAN_AGAIN,1000);
                    }
                }
            }
            @Override
            public void onScanning(ExBleDevice bleDevice) {
                System.out.println("========================onScanning===========================");
                if (bleDevice.getName() != null && bleDevice.getName().contains(Customer_Ble_Device_Filter)) {
                    ExBleManager.getInstance().cancelScan();
                }
            }
            @Override
            public void onLeScan(ExBleDevice bleDevice) {
                System.out.println("========================onLeScan===========================");
                super.onLeScan(bleDevice);
            }
            @Override
            public void onScanFinished(List<ExBleDevice> scanResultList) {
                System.out.println("========================onScanFinished===========================");
                if (scanResultList.size()!=0){
                    mExBleDevice = scanResultList.get(0);
                    startConnect(mExBleDevice);
                }else {
                    mBleGatt = null;
                    mProgressDialog.dismiss();
                }
            }
        });
    }

    private void startConnect(final ExBleDevice bleDevice){
        ExBleManager.getInstance().connect(bleDevice, new ExBleGattCallback() {
                    @Override
                    public void onStartConnect() {
                        System.out.println("========================onStartConnect======="+bleDevice.getName());
                    }
                    @Override
                    public void onConnectFail(ExBleException exception) {
                        System.out.println("========================onConnectFail===========================");
                        mBleGatt = null;
                        mProgressDialog.dismiss();
                    }
                    @Override
                    public void onConnectSuccess(BluetoothGatt gatt, int status) {
                        System.out.println("========================onConnectSuccess===========================");
                        mExBleDevice.setIsConnected(true);
                    }
                    @Override
                    public void onDisConnected(BluetoothGatt gatt, int status) {
                        System.out.println("========================onDisConnected===========================");
                        if (mExBleDevice!=null || mExBleDevice.getIsConnected()) {
                            mExBleDevice.setIsServDisced(false);
                            mExBleDevice.setIsConnected(false);
                        }
                        if (mProgressDialog.isShowing()) {
                            mProgressDialog.dismiss();
                        }
                        mBleGatt = null;
                    }
                    @Override
                    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                        System.out.println("========================onServicesDiscovered===========================");
                        mBleGatt = gatt;
                        enableDataTransfer(mBleGatt);
                        mExBleDevice.setIsServDisced(true);
                        mProgressDialog.dismiss();
                        System.out.println("========================bleReady = true===========================");
                        logTextAppend("Bluetooth connected");
                        bleReady = true;
                        fingerprintTryNum = 0;
                        if (showQRcodeAfterBleReady) {
                            Message message = Message.obtain();
                            message.obj = mBleGatt;
                            message.what = CMD_SHOW_QRCODE;
                            mBleHandler.sendMessageDelayed(message,500);
                        } else {
                            waitingSignTransaction();
                        }
                    }
                }
        );
    }

    private void enableDataTransfer(BluetoothGatt gatt){
        ExBleManager.getInstance().setSelfBleProtocol("91","18","81");

        ExBleManager.getInstance().enableReadWriteChr(gatt,
                new ExBleNotifyCallback() {
                    @Override
                    public void heartBeatNotify(ExBleDevice device) {
                        //                        device.setDeviceHeartBeatString("");
                    }

                    @Override
                    public void cmdRespone(ExBleDevice device) {
                        System.out.println("========================cmdRespone===========================");
                        Message message = Message.obtain();
                        message.obj = device.getDeviceRespontString();
                        message.what = CMD_RESPONE;
                        mBleHandler.sendMessage(message);
                    }
                },
                new ExBleWriteCallback() {
                    @Override
                    public void onWriteSuccess(BluetoothGatt mGatt) {
                        System.out.println("========================onWriteSuccess===========================");
                    }

                    @Override
                    public void onWriteFailure(BluetoothGatt mGatt) {
                        System.out.println("========================onWriteFailure===========================");
                    }
                });
    }

    private class BluetoothCMDHandler extends Handler {
        private final WeakReference<Activity> reference;
        BluetoothCMDHandler(Looper looper, Activity activity) {
            super(looper);
            reference = new WeakReference<>(activity);
        }
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case BLE_START_SCAN_AGAIN:
                    startScan();
                    break;
                case CMD_GET_FP_LIST:
                    getFPList((BluetoothGatt) msg.obj);
                    mBleHandler.sendEmptyMessageDelayed(BLE_TAG_HEARTBEAT,6000);//
                    break;
                case BLE_TAG_DEVICE_DISCONNECT:
                    if (mBleHandler.hasMessages(BLE_TAG_HEARTBEAT)){
                        mBleHandler.removeMessages(BLE_TAG_HEARTBEAT);
                    }
                    ExBleManager.getInstance().disconnectByDevice(mExBleDevice,mBleGatt);
                    mBleGatt = null;
                    mExBleDevice=null;
                    bleReady = false;
                    logTextAppend("Bluetooth disconnect");
                    break;
                case BLE_TAG_HEARTBEAT:
                    if (mExBleDevice==null){
                        break;
                    }
                    if (mBleHandler.hasMessages(BLE_TAG_HEARTBEAT)){
                        mBleHandler.removeMessages(BLE_TAG_HEARTBEAT);
                    }
                    String heartBeatString = mExBleDevice.getDeviceHeartBeatString();
                    mExBleDevice.setDeviceHeartBeatString("");
                    if (heartBeatString.length()==0){
                        System.out.println("heartBeat none BLE DEATH===============");
                        mBleHandler.sendEmptyMessage(BLE_TAG_DEVICE_DISCONNECT);

                    }else{
                        System.out.println("heartBeat found BLE ALIVE===============");
                        mBleHandler.sendEmptyMessageDelayed(BLE_TAG_HEARTBEAT,6000);
                    }
                    break;
                case CMD_TIMEOUT:
                    System.out.println("========================CMD_TIMEOUT===========================");
                    break;
                case CMD_RESPONE:
                    if (mBleHandler.hasMessages(CMD_TIMEOUT)){
                        mBleHandler.removeMessages(CMD_TIMEOUT);
                    }
                    checkRsp((String)msg.obj);
                    String cmd = mExBleDevice.getDeviceCmdString();
                    mExBleDevice.setRespontString("");
                    break;
                case CMD_SET_SIGN_INFO:
                    setSignInfo((BluetoothGatt) msg.obj);
                    break;
                case CMD_SET_SIGN_INFO_TWO:
                    setSignInfoTwo((BluetoothGatt) msg.obj);
                    break;
                case CMD_SET_SIGN_INFO_THREE:
                    setSignInfoThree((BluetoothGatt) msg.obj);
                    break;
                case CMD_SET_SIGN_INFO_FOUR:
                    setSignInfoFour((BluetoothGatt) msg.obj);
                    break;
                case CMD_GEY_SIGN_RESULT:
                    getSignResult((BluetoothGatt) msg.obj);
                    break;
                case CMD_SHOW_QRCODE:
                    handleShowQRcode((BluetoothGatt) msg.obj);
                    break;
                default:
                    break;
            }
        };
    }

    public void setSerializeString(String part1, String part2, String part3, String part4,String chainId) {
        serializeStringPartOne = part1;
        serializeStringPartTwo = part2;
        serializeStringPartThree = part3;
        serializeStringPartFour = part4;
        this.chainId = chainId;
        waitingSignTransaction();
    }

    public void showQrcode(String showString,boolean isDisconnect) {
        disconnectAfterRsp = isDisconnect;
        qrCodeString = showString;
        showQRcodeAfterBleReady = false;
        if (bleReady) {
            Message message = Message.obtain();
            message.obj = mBleGatt;
            message.what = CMD_SHOW_QRCODE;
            mBleHandler.sendMessage(message);
        } else {
            showQRcodeAfterBleReady = true;
        }
    }


    public void showQrcodeRspDone() {
        if (disconnectAfterRsp) {
            disconnectBluetooth();
        }
    }
    public void setSignInfo(BluetoothGatt gatt){
        String part1 = createCmdString(chainId + serializeStringPartOne,1);
        String part2 = createCmdString(serializeStringPartTwo,2);
        String part3 = createCmdString(serializeStringPartThree,3);
        String part4 = createCmdString(serializeStringPartFour + "0000000000000000000000000000000000000000000000000000000000000000",4);
        System.out.println("========================setSignInfo part1: " + part1);
//        System.out.println("========================setSignInfo part2: " + part2);
//        System.out.println("========================setSignInfo part3: " + part3);
//        System.out.println("========================setSignInfo part4: " + part4);
        serializeStringPartOne = part1;
        serializeStringPartTwo = part2;
        serializeStringPartThree = part3;
        serializeStringPartFour = part4;
        List<String> resultList = new ArrayList<>();
        resultList = bleCmdSend(gatt,serializeStringPartOne);
        mExBleDevice.setDeviceCmdString("CMD_SET_SIGN_INFO");
        logTextAppend("sending CMD_SET_SIGN_INFO Part1");
//        mUrlHandler.sendEmptyMessageDelayed(CMD_TIMEOUT,6000);
    }

    public void setSignInfoTwo(BluetoothGatt gatt) {
        List<String> resultList = new ArrayList<>();
        resultList = bleCmdSend(gatt,serializeStringPartTwo);
        System.out.println("========================setSignInfo part2: " + serializeStringPartTwo);
        mExBleDevice.setDeviceCmdString("CMD_SET_SIGN_INFO_TWO");
//        mUrlHandler.sendEmptyMessageDelayed(CMD_TIMEOUT,6000);
    }
    public void setSignInfoThree(BluetoothGatt gatt) {
        List<String> resultList = new ArrayList<>();
        resultList = bleCmdSend(gatt,serializeStringPartThree);
        System.out.println("========================setSignInfo part3: " + serializeStringPartThree);
        mExBleDevice.setDeviceCmdString("CMD_SET_SIGN_INFO_THREE");
//        mUrlHandler.sendEmptyMessageDelayed(CMD_TIMEOUT,6000);
    }
    public void setSignInfoFour(BluetoothGatt gatt) {
        List<String> resultList = new ArrayList<>();
        resultList = bleCmdSend(gatt,serializeStringPartFour);
        System.out.println("========================setSignInfo part4: " + serializeStringPartFour);
        mExBleDevice.setDeviceCmdString("CMD_SET_SIGN_INFO_FOUR");
//        mUrlHandler.sendEmptyMessageDelayed(CMD_TIMEOUT,6000);
    }

    public void getSignResult(BluetoothGatt gatt) {
        String getSignResultString = createCmdString("",5);
        List<String> resultList = new ArrayList<>();
        resultList = bleCmdSend(gatt,getSignResultString);
        System.out.println("========================getSignResult: " + getSignResultString);
        mExBleDevice.setDeviceCmdString("CMD_GEY_SIGN_RESULT");
//        mUrlHandler.sendEmptyMessageDelayed(CMD_TIMEOUT,6000);
    }

    public void handleShowQRcode(BluetoothGatt gatt) {
        String showQrcodeString = createCmdString(qrCodeString,6);
        List<String> resultList = new ArrayList<>();
        resultList = bleCmdSend(gatt,showQrcodeString);
        System.out.println("========================show QRcode: " + showQrcodeString);
        mExBleDevice.setDeviceCmdString("CMD_SHOW_QRCODE");
    }
    public String createCmdString(String input,int part) {
        String hexLength = HexUtil.intTo1BytesHexString(input.length()/2);
        String returnString = "80";//A00000" + hexLength + input;
        if (part == 1) {
            returnString = returnString + "A00000" + hexLength + input;
        } else if (part == 2) {
            returnString = returnString + "A00001" + hexLength + input;
        } else if (part == 3) {
            returnString = returnString + "A00002" + hexLength + input;
        } else if (part == 4) {
            returnString = returnString + "A00003" + hexLength + input;
        } else if (part == 5) {
            returnString = returnString + "AE000000";
        } else if (part == 6) {
            returnString = returnString + "A10000" + hexLength + input;
        } else {
            return "";
        }
        hexLength = HexUtil.intTo2BytesHexString(returnString.length()/2);
        returnString = "55AA00000000" + hexLength + returnString;
        String cmdDataCrc = HexUtil.intTo2BytesHexString(BleDataPackage.crcCalculate(returnString));
        returnString = returnString + cmdDataCrc;
        returnString = returnString.toUpperCase();
        return returnString;
    }

    public void waitingSignTransaction() {
        if (bleReady &&
                !serializeStringPartFour.equals("") &&
                !serializeStringPartThree.equals("") &&
                !serializeStringPartTwo.equals("") &&
                !serializeStringPartOne.equals("") ) {

            Message message = Message.obtain();
            message.obj = mBleGatt;
            message.what = CMD_SET_SIGN_INFO;
//            mUrlHandler.sendMessage(message);
            mBleHandler.sendMessageDelayed(message, 500);
        }
    }

    public void getFPList(BluetoothGatt gatt){
        List<String> resultList = new ArrayList<>();
        resultList = bleCmdSend(gatt,"55AA0005000000008375");
        System.out.println("========================getFPList 55AA0005000000008375 ===========================");
        mExBleDevice.setDeviceCmdString("CMD_GET_FP_LIST");
        mBleHandler.sendEmptyMessageDelayed(CMD_TIMEOUT,6000);
    }
    public List<String> bleCmdSend(BluetoothGatt gatt,String cmdString){
        List<String> returnList = new ArrayList<>();
        byte editTextDataCrc[] = BleDataPackage.BleDataWithCRC(cmdString);
        ExBleManager.getInstance().writeByGatt(gatt,editTextDataCrc);
        returnList.add("bleError");
        returnList.add("");
        return returnList;
    }
    public void checkRsp(String rsp){
        String cmd = mExBleDevice.getDeviceCmdString();
        Message message;
        switch (cmd){
            case "CMD_GET_FP_LIST":
                logTextAppend("checkRsp:" + rsp);
                if (rsp.equals("18000F55AA00850000000009200000")){//
                }else if (rsp.equals("18001055AA0085000000010038D30000")) {//
                }else if (rsp.equals("18001255AA008500000003000102CB240000")){//
                }else {
                    System.out.println("getFPList error =====" + rsp);
                }
                break;
            case "CMD_SET_SIGN_INFO":
                message = Message.obtain();
                message.obj = mBleGatt;
                message.what = CMD_SET_SIGN_INFO_TWO;
                mBleHandler.sendMessage(message);
                logTextAppend("sending CMD_SET_SIGN_INFO Part2");
                break;
            case "CMD_SET_SIGN_INFO_TWO":
                message = Message.obtain();
                message.obj = mBleGatt;
                message.what = CMD_SET_SIGN_INFO_THREE;
                mBleHandler.sendMessage(message);
                logTextAppend("sending CMD_SET_SIGN_INFO Part3");
                break;
            case "CMD_SET_SIGN_INFO_THREE":
                message = Message.obtain();
                message.obj = mBleGatt;
                message.what = CMD_SET_SIGN_INFO_FOUR;
                mBleHandler.sendMessage(message);
                logTextAppend("sending CMD_SET_SIGN_INFO Part4");
                break;
            case "CMD_SET_SIGN_INFO_FOUR":
                message = Message.obtain();
                message.obj = mBleGatt;
                message.what = CMD_GEY_SIGN_RESULT;
//                mUrlHandler.sendMessageDelayed(message,4000);
                mBleHandler.sendMessage(message);
                logTextAppend("waiting for card response...");
                serializeStringPartOne = "";
                serializeStringPartTwo = "";
                serializeStringPartThree = "";
                serializeStringPartFour = "";
                break;
            case "CMD_GEY_SIGN_RESULT":
                System.out.println("========================CMD_GEY_SIGN_RESULT: " + rsp);
                if (rsp.length() == 238) {
                    rsp = rsp.substring(22, rsp.length() - 14);
                    String bleSignRsp = HexUtil.hexStringToString(rsp, 2);
                    System.out.println("========================CMD_GEY_SIGN_RESULT String: " + bleSignRsp);
                    logTextAppend("Card signatures:" + bleSignRsp);
                    message = Message.obtain();
                    message.obj = bleSignRsp;
                    message.what = BLE_SIGN_TRANSATION_RSP;
                    guiHandler.sendMessage(message);
                } else if (rsp.equals("18001155AA0080000000026201BC850000")) {
                    fingerprintTryNum +=1;
                    if (fingerprintTryNum > 2) {
                        logTextAppend("fingerprint dismatch");
                        mBleHandler.sendEmptyMessage(BLE_TAG_DEVICE_DISCONNECT);
                    } else {
                        message = Message.obtain();
                        message.obj = mBleGatt;
                        message.what = CMD_GEY_SIGN_RESULT;
                        mBleHandler.sendMessageDelayed(message,100);
                        logTextAppend("fingerprint dismatch... try again");
                        logTextAppend("waiting for card response...");
                    }
                } else if (rsp.equals("18001155AA008000000002620F55FB0000")) {//time out
                    message = Message.obtain();
                    message.obj = rsp;
                    message.what = BLE_SIGN_TRANSATION_TIMEOUT;
                    guiHandler.sendMessage(message);
                } else {
                    mBleHandler.sendEmptyMessage(BLE_TAG_DEVICE_DISCONNECT);

                }
                break;
            case "CMD_SHOW_QRCODE":
                System.out.println("========================CMD_SHOW_QRCODE: " + rsp);
                showQrcodeRspDone();
                break;
            default:
                break;
        }
    }
}
