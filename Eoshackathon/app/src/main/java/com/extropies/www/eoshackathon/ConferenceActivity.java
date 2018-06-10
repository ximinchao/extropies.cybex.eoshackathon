package com.extropies.www.eoshackathon;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.extropies.www.eoshackathon.BleUtil.ExBlePermission;
import com.extropies.www.eoshackathon.Utils.HexUtil;
import com.extropies.www.eoshackathon.Utils.JsonStringUtils;
import com.extropies.www.eoshackathon.http.HttpUtilsCallback;
import com.extropies.www.eoshackathon.http.HttpUtilsGet;
import com.extropies.www.eoshackathon.http.HttpUtilsPost;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.ref.WeakReference;

import static com.extropies.www.eoshackathon.MainActivity.BLE_SIGN_TRANSATION_RSP;
import static com.extropies.www.eoshackathon.MainActivity.BLE_SIGN_TRANSATION_TIMEOUT;
import static com.extropies.www.eoshackathon.MainActivity.ConsaleSymbolString;
import static com.extropies.www.eoshackathon.MainActivity.LOG_TEXT_APPEND;
import static com.extropies.www.eoshackathon.MainActivity.URL_ABI_JSON_TO_BIN;
import static com.extropies.www.eoshackathon.MainActivity.URL_GET_BLOCK;
import static com.extropies.www.eoshackathon.MainActivity.URL_GET_INFO;
import static com.extropies.www.eoshackathon.MainActivity.URL_GET_TABLE_ROWS;
import static com.extropies.www.eoshackathon.MainActivity.URL_PUSH_TRANSACTION;
import static com.extropies.www.eoshackathon.MainActivity.URL_SIGN_TRANSACTION;
import static com.extropies.www.eoshackathon.MainActivity.URL_WALLET_UNLOCK;
import static com.extropies.www.eoshackathon.MainActivity.urlHead;
import static com.extropies.www.eoshackathon.MainActivity.urlWalletHead;

/**
 * Created by inst on 18-6-9.
 */

public class ConferenceActivity extends Activity {
    private String userName = "";
    private String userAddress = "";
    private String confId = "";
    private String confName = "";
    private String confFee = "";
    private String confOrgn = "";
    private boolean coldWalletType = false;

    private String headBlockTime="";
    private String lastIrreversibleBlockNum = "";
    private String chainId = "";
    private String blockNum="";
    private String blockPrefix="";
    private String binArgs = "";
    private String serializeBin = "";
    private String signaturesString = "";

    private UrlHandler mUrlHandler;
    public HttpUtilsCallback httpRsp;

    private Button buyBtn;
    private Button qcodeToCardBtn;
    private TextView attendeeName;

    private PopupWindow mPopWindow;

    private String operationTag;
    private boolean longClickShowWithdrawMenuTag;
    private boolean qcodeToCardFlag;

    BluetoothAction bluetoothAction;
    private String serializeStringPartOne = "";
    private String serializeStringPartTwo= "";
    private String serializeStringPartThree = "";
    private String serializeStringPartFour = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);// 设置全屏
        setContentView(R.layout.activity_conference);

        buyBtn = findViewById(R.id.confBuy);
        qcodeToCardBtn = findViewById(R.id.loadQRcodeToCard);
        attendeeName = findViewById(R.id.attendee);
        View view = findViewById(R.id.confTag);

        Intent getIntent = getIntent();
        userName = getIntent.getStringExtra("userName");
        confId = getIntent.getStringExtra("confID");
        confName = getIntent.getStringExtra("confName");
        confFee = getIntent.getStringExtra("confFee");
        confOrgn = getIntent.getStringExtra("confOrgn");
        userAddress = getIntent.getStringExtra("userAddress");
        String walletType = getIntent.getStringExtra("walletType");
        if (walletType.equals("coldWallet")) {
            coldWalletType = true;
        } else {
            coldWalletType = false;
        }

        TextView tempTextview = findViewById(R.id.buyConfName);
        tempTextview.setText(confName);
        tempTextview = findViewById(R.id.buyConfId);
        tempTextview.setText("id:" + confId);
        tempTextview = findViewById(R.id.buyConfOrganizer);
        tempTextview.setText(confOrgn);
        tempTextview = findViewById(R.id.buyConfFee);
        tempTextview.setText(confFee);

        attendeeName.setText(userName);

        httpRsp = new HttpUtilsCallback() {
            @Override
            public void httpResponse(String url, String rsp) {
                getDataUrlRspString(url, rsp);
            }
        };
        mUrlHandler = new UrlHandler(this.getMainLooper(), this);
        mUrlHandler.sendEmptyMessage(URL_GET_TABLE_ROWS);

        view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                System.out.println("===== onLongClick : " );
                if (qcodeToCardBtn.getVisibility() == View.VISIBLE) {
                    mUrlHandler.sendEmptyMessage(URL_GET_TABLE_ROWS);
                    longClickShowWithdrawMenuTag = true;
                }
                return true;
            }
        });

        //ble
        bluetoothAction = new BluetoothAction();
        bluetoothAction.BluetoothInitialize(getApplication(),this,mUrlHandler);

        qcodeToCardFlag = false;
    }

    public void buyBtnClick(View view) {
        buyBtn.setEnabled(false);
        operationTag = "buy";
        mUrlHandler.sendEmptyMessage(URL_GET_INFO);
        String jsonObjString = JsonStringUtils.confCreateAbiJsonToBinString(
                confName,confOrgn, userName,
                userAddress,ConsaleSymbolString,"regist");//"consale"
        Message message = Message.obtain();
        message.obj = jsonObjString;
        message.what = URL_ABI_JSON_TO_BIN;
        mUrlHandler.sendMessage(message);

        if (ExBlePermission.checkPermissions(this)){
            bluetoothAction.startScan();
        }else {
            buyBtn.setEnabled(true);
            Toast.makeText(this, "Bluetooth permission denied: ", Toast.LENGTH_SHORT).show();
            System.out.println("Bluetooth permission denied" );
        }
    }

    public void loadQRcodeToCardBtnClick(View view) {
        qcodeToCardFlag = false;
        if (bluetoothAction.getConnectStatus()) {
            if (binArgs.equals("")) {
                String jsonObjString = JsonStringUtils.confCreateAbiJsonToBinString(
                        confName, confOrgn, userName,
                        userAddress, ConsaleSymbolString, "regist");//"consale"
                Message message = Message.obtain();
                message.obj = jsonObjString;
                message.what = URL_ABI_JSON_TO_BIN;
                mUrlHandler.sendMessage(message);
                qcodeToCardFlag = true;
            } else {
                downloadQRcodeToCard(binArgs);
            }
        } else {
            String jsonObjString = JsonStringUtils.confCreateAbiJsonToBinString(
                    confName,confOrgn, userName,
                    userAddress,ConsaleSymbolString,"regist");//"consale"
            Message message = Message.obtain();
            message.obj = jsonObjString;
            message.what = URL_ABI_JSON_TO_BIN;
            mUrlHandler.sendMessage(message);
            qcodeToCardFlag = true;

            if (ExBlePermission.checkPermissions(this)){
                bluetoothAction.startScan();
            }else {
                Toast.makeText(this, "Bluetooth permission denied: ", Toast.LENGTH_SHORT).show();
                System.out.println("Bluetooth permission denied" );
            }
        }
    }
    public void jsonToBinRspDone() {
        if (qcodeToCardFlag) {
            qcodeToCardFlag = false;
            downloadQRcodeToCard(binArgs);
        }
    }
    public void downloadQRcodeToCard(String input) {
        int length = HexUtil.hexStringToAlgorism(input.substring(0, 2));
        length = length + 1 + 16;
        String qrcodeString = input.substring(0, length * 2);
        if (!qrcodeString.equals("")) {
            bluetoothAction.showQrcode(qrcodeString,true);
        }
    }


    public void getTableRowsRspDone(JSONArray object) {
        if (object.length() != 0) {
            JSONObject attendeeObj;
            String attendeeNameLoop = "";
            int attendeeIdLoop = 0;
            boolean flag = false;
            boolean fee_locked = false;
            for (int loop=0;loop<object.length();loop++) {
                try {
                    attendeeObj = (JSONObject)object.get(loop);
                    attendeeNameLoop = attendeeObj.getString("attendee");
                    attendeeIdLoop = attendeeObj.getInt("conf_id");
                    if (attendeeNameLoop.equals(userName) && Integer.toString(attendeeIdLoop).equals(confId)) {
                        flag = true;
                        int feeLockedInt = attendeeObj.getInt("fee_locked");
                        if (feeLockedInt == 1) {
                            fee_locked = true;
                        }
                        break;
                    }
                }catch (Exception e) {
                    Log.e("exception:","error");
                }

            }
            if (flag) {
                qcodeToCardBtn.setVisibility(View.VISIBLE);
                buyBtn.setVisibility(View.GONE);
                if (fee_locked) {
                    qcodeToCardBtn.setEnabled(false);
                } else {
                    qcodeToCardBtn.setEnabled(true);
                    if (longClickShowWithdrawMenuTag) {
                        longClickShowWithdrawMenuTag = false;
                        showPopupWindow();
                    }
                }
            } else {
                buyBtn.setVisibility(View.VISIBLE);
                qcodeToCardBtn.setVisibility(View.GONE);
            }
        } else {
            buyBtn.setVisibility(View.VISIBLE);
            qcodeToCardBtn.setVisibility(View.GONE);
        }
    }
    public void getInfoRspDone() {
        mUrlHandler.sendEmptyMessage(URL_GET_BLOCK);
        mUrlHandler.sendEmptyMessage(URL_WALLET_UNLOCK);
        if (!headBlockTime.equals("")) {
            headBlockTime = JsonStringUtils.createTimeString(headBlockTime, 120);
        }
    }
    public void getBlockRspDone() {
        if (operationTag.equals("buy")) {
            if (!headBlockTime.equals("") && !lastIrreversibleBlockNum.equals("") &&
                    !chainId.equals("") && !blockNum.equals("") &&
                    !blockPrefix.equals("") && !binArgs.equals("")
                    ) {
                String serializeString = JsonStringUtils.createSignTransToBin(headBlockTime,Integer.valueOf(blockNum),blockPrefix,chainId,binArgs,userName,ConsaleSymbolString,"regist");//"consale"
                serializeBin = JsonStringUtils.getSerializeBinString(serializeString);
                String signTransactionString = JsonStringUtils.createSignTransString(headBlockTime,Integer.valueOf(blockNum),blockPrefix,chainId,binArgs,userName,userAddress,ConsaleSymbolString,"regist");//"consale"

                serializeStringPartOne = JsonStringUtils.getSerializeBinPartString(serializeString, "part1");
                serializeStringPartTwo = JsonStringUtils.getSerializeBinPartString(serializeString, "part2");
                serializeStringPartThree = JsonStringUtils.getSerializeBinPartString(serializeString, "part3");
                serializeStringPartFour = JsonStringUtils.getSerializeBinPartString(serializeString, "part4");
                System.out.println("========================JsonStringUtils.getSerializeBinPartString part1: " + serializeStringPartOne);
                System.out.println("========================JsonStringUtils.getSerializeBinPartString part2: " + serializeStringPartTwo);
                System.out.println("========================JsonStringUtils.getSerializeBinPartString part3: " + serializeStringPartThree);
                System.out.println("========================JsonStringUtils.getSerializeBinPartString part4: " + serializeStringPartFour);
                bluetoothAction.setSerializeString(serializeStringPartOne,serializeStringPartTwo,serializeStringPartThree,serializeStringPartFour,chainId);

            }
        } else if (operationTag.equals("withdraw")) {
            if (!headBlockTime.equals("") && !lastIrreversibleBlockNum.equals("") &&
                    !chainId.equals("") && !blockNum.equals("") &&
                    !blockPrefix.equals("") && !binArgs.equals("")
                    ) {
                String serializeString = JsonStringUtils.createSignTransToBin(headBlockTime,Integer.valueOf(blockNum),blockPrefix,chainId,binArgs,userName,ConsaleSymbolString,"withdraw");//"consale"
                serializeBin = JsonStringUtils.getSerializeBinString(serializeString);
                String signTransactionString = JsonStringUtils.createSignTransString(headBlockTime,Integer.valueOf(blockNum),blockPrefix,chainId,binArgs,userName,userAddress,ConsaleSymbolString,"withdraw");//"consale"
                Message message = Message.obtain();
                message.obj = signTransactionString;
                message.what = URL_SIGN_TRANSACTION;
                mUrlHandler.sendMessage(message);
            }
        }

    }
    public void walletUnlockRspDone() {

    }

    public void signTransactionRspDone() {
        String pushTransString = JsonStringUtils.createPushTransactionString(signaturesString,serializeBin);
        System.out.println("===== pushTransString : "+ pushTransString );

        if (!pushTransString.equals("")) {
            Message message = Message.obtain();
            message.obj = pushTransString;
            message.what = URL_PUSH_TRANSACTION;
            mUrlHandler.sendMessage(message);
        }
    }
    public void pushTransRspDone(JSONObject jsonObject) {
        try {
            if (jsonObject.has("processed")) {
                JSONObject receiptObj = jsonObject.getJSONObject("processed").getJSONObject("receipt");
                String statusString = receiptObj.getString("status");
                if (statusString.equals("executed")) {
                    if (operationTag.equals("buy")) {
                        buyBtn.setEnabled(true);
                        qcodeToCardBtn.setVisibility(View.VISIBLE);
                        buyBtn.setVisibility(View.GONE);
                        downloadQRcodeToCard(binArgs);
                    } else if (operationTag.equals("withdraw")) {
                        buyBtn.setEnabled(true);
                        qcodeToCardBtn.setVisibility(View.GONE);
                        buyBtn.setVisibility(View.VISIBLE);
                    }
                }
            } else if (jsonObject.has("code")) {
                int codeInt = jsonObject.getInt("code");
                if (codeInt == 500) {
                    Toast.makeText(this, "Push Transaction rsp: 500", Toast.LENGTH_SHORT).show();
                    if (operationTag.equals("buy")) {
                        buyBtn.setEnabled(true);
                    }
                }
                bluetoothAction.disconnectBluetooth();
            } else {
                bluetoothAction.disconnectBluetooth();
            }
        }catch (Exception e) {
            Log.e("exception:","error");
        }
    }


    private class UrlHandler extends Handler {
        private final WeakReference<ConferenceActivity> reference;
        UrlHandler(Looper looper, ConferenceActivity activity) {
            super(looper);
            reference = new WeakReference<>(activity);
        }
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case URL_GET_INFO:
                    System.out.println("===== HttpUtilsGet URL_GET_INFO" );
                    new HttpUtilsGet(urlHead + "/v1/chain/get_info",httpRsp).execute();
                    break;
                case URL_GET_BLOCK:
                    try {
                        JSONObject jsonObj = new JSONObject();//创建一个json对象
                        jsonObj.put("block_num_or_id", lastIrreversibleBlockNum);//把name值放入json对象中
                        System.out.println("===== HttpUtilsGet URL_GET_BLOCK" );
                        new HttpUtilsPost(urlHead + "/v1/chain/get_block",jsonObj.toString(),httpRsp).execute();
                    }catch (Exception e) {
                        Log.e("exception:","error");
                    }

                    break;
                case URL_ABI_JSON_TO_BIN:
                    System.out.println("===== HttpUtilsGet URL_ABI_JSON_TO_BIN" + (String)msg.obj);
                    new HttpUtilsPost(urlHead + "/v1/chain/abi_json_to_bin",(String)msg.obj,httpRsp).execute();
                    break;
                case URL_SIGN_TRANSACTION:
                    System.out.println("===== HttpUtilsGet URL_SIGN_TRANSACTION" + (String)msg.obj);
                    new HttpUtilsPost(urlWalletHead + "/v1/wallet/sign_transaction",(String)msg.obj,httpRsp).execute();
                    break;
                case URL_WALLET_UNLOCK:
                    JSONArray jsonObj = new JSONArray();
                    jsonObj.put("ximc");
                    jsonObj.put("PW5JNcqr73uN2jzcp3cG6nXKErNY2bZrJexYUNCP1aRL4AqYGugQh");
                    String urlString = urlWalletHead + "/v1/wallet/unlock";
                    System.out.println("===== HttpUtilsGet URL_WALLET_UNLOCK" );
                    new HttpUtilsPost(urlString,jsonObj.toString(),httpRsp).execute();
                    break;
                case URL_PUSH_TRANSACTION:
                    System.out.println("===== HttpUtilsGet URL_PUSH_TRANSACTION:" +  (String)msg.obj);
                    new HttpUtilsPost(urlHead + "/v1/chain/push_transaction",(String)msg.obj,httpRsp).execute();
                    break;
                case URL_GET_TABLE_ROWS:
                    try {
                        JSONObject getTableJsonObj = new JSONObject();
                        getTableJsonObj.put("scope", ConsaleSymbolString);//"consale"
                        getTableJsonObj.put("code",ConsaleSymbolString);//"consale"
                        getTableJsonObj.put("table", "attable");
                        getTableJsonObj.put("lower_bound", "0");
                        getTableJsonObj.put("limit", "100");
                        getTableJsonObj.put("json", true);
                        System.out.println("===== HttpUtilsGet URL_GET_TABLE_ROWS" );
                        new HttpUtilsPost(urlHead + "/v1/chain/get_table_rows",getTableJsonObj.toString(),httpRsp).execute();
                    }catch (Exception e) {
                        Log.e("exception:","error");
                    }
                    break;
                case BLE_SIGN_TRANSATION_RSP:
                    signaturesString = (String)msg.obj;
                    signTransactionRspDone();
                    break;
                case BLE_SIGN_TRANSATION_TIMEOUT:
                    if (operationTag.equals("buy")) {
                        buyBtn.setEnabled(true);
                        bluetoothAction.disconnectBluetooth();
                    }
                    break;
                case LOG_TEXT_APPEND:
                    break;
                default:
                    break;
            }
        };
    }

    public void getDataUrlRspString(String targeurl,String stringvalue) {
        if (targeurl.endsWith("get_table_rows")) {
            try {
                JSONObject jsonObject = new JSONObject(stringvalue);
                System.out.println("===== get_table_rows rsp:"+ stringvalue);
                if (jsonObject.has("rows")) {
                    JSONArray confArray =jsonObject.getJSONArray("rows");
                    getTableRowsRspDone(confArray);
                } else {
                }
            } catch (Exception e) {
                Log.e("exception:","error");
            }
        }
        else if (targeurl.endsWith("get_info")) {
            try {
                JSONObject jsonObject = new JSONObject(stringvalue);
                System.out.println("===== get_info rsp:"+ stringvalue);
                if (jsonObject.has("head_block_time")) {
                    headBlockTime = jsonObject.optString("head_block_time");
                } else {
                }
                if (jsonObject.has("last_irreversible_block_num")) {
                    lastIrreversibleBlockNum = jsonObject.optString("last_irreversible_block_num");
                } else {
                }
                chainId = "";
                if (jsonObject.has("chain_id")) {
                    chainId = jsonObject.optString("chain_id");
                } else {
                }
                if (!headBlockTime.equals("") && !lastIrreversibleBlockNum.equals("")) {
                    getInfoRspDone();
                }

            } catch (Exception e) {
                Log.e("exception:","error");
            }
        }
        else if (targeurl.endsWith("get_block")) {
            try {
                JSONObject jsonObject = new JSONObject(stringvalue);
                System.out.println("===== get_block rsp:"+ stringvalue);
                if (jsonObject.has("block_num")) {
                    blockNum=jsonObject.optString("block_num");
                } else {
                }
                if (jsonObject.has("ref_block_prefix")) {
                    blockPrefix=jsonObject.optString("ref_block_prefix");
                    getBlockRspDone();
                } else {
                }

            } catch (Exception e) {
                Log.e("exception:","error");
            }
        }else if (targeurl.endsWith("abi_json_to_bin")) {
            try {
                JSONObject jsonObject = new JSONObject(stringvalue);
                System.out.println("===== abi_json_to_bin rsp:"+ stringvalue);

                if (jsonObject.has("binargs")) {
                    binArgs=jsonObject.optString("binargs");
                    jsonToBinRspDone();
                } else {
                }

            } catch (Exception e) {
                Log.e("exception:","error");
            }
        } else if (targeurl.endsWith("sign_transaction")) {
            try {
                JSONObject jsonObject = new JSONObject(stringvalue);
                System.out.println("===== sign_transaction rsp:"+ stringvalue);

                if (jsonObject.has("signatures")) {
                    JSONArray signaturesArray =jsonObject.getJSONArray("signatures");
                    signaturesString  = (String)signaturesArray.get(0);
                    signTransactionRspDone();
                } else {
                }

            } catch (Exception e) {
                Log.e("exception:","error");
            }
        }else if (targeurl.endsWith("unlock")) {
            try {
                JSONObject jsonObject = new JSONObject(stringvalue);
                System.out.println("===== unlock rsp:"+ stringvalue);
                walletUnlockRspDone();
            } catch (Exception e) {
                Log.e("exception:","error");
            }
        } else if (targeurl.endsWith("push_transaction")) {
            try {
                JSONObject jsonObject = new JSONObject(stringvalue);
                System.out.println("===== push_transaction rsp:"+ stringvalue);
                pushTransRspDone(jsonObject);
            } catch (Exception e) {
                Log.e("exception:","error");
            }
        }

    }

    private void showPopupWindow() {
        View contentView = this.getLayoutInflater().inflate(R.layout.layout_withdraw_popupwindows, null);
        popupWindowBtnSet(contentView);
        mPopWindow = new PopupWindow(contentView, ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT, true);
        mPopWindow.setFocusable(true);
        mPopWindow.setBackgroundDrawable(new BitmapDrawable());//
        mPopWindow.setAnimationStyle(R.style.Popupwindow);
        mPopWindow.showAtLocation(this.getWindow().getDecorView(), Gravity.LEFT | Gravity.BOTTOM, 0, 0);
        mPopWindow.setOnDismissListener(new PopupWindow.OnDismissListener(){
            @Override
            public void onDismiss() {
                backgroundAlpha(1.0f);
            }
        });
        backgroundAlpha(0.5f);

    }
    public void backgroundAlpha(float bgAlpha)
    {
        WindowManager.LayoutParams lp = this.getWindow().getAttributes();
        lp.alpha = bgAlpha; //0.0-1.0
        this.getWindow().setAttributes(lp);
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
    }

    private void popupWindowBtnSet(View view) {
        Button yesBtn = (Button) view.findViewById(R.id.yesBtn);
        Button noBtn = (Button) view.findViewById(R.id.noBtn);

        yesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mPopWindow != null && mPopWindow.isShowing()) {
                    withdrawTicket();
                    mPopWindow.dismiss();
                }
            }
        });
        noBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mPopWindow != null && mPopWindow.isShowing()) {
                    mPopWindow.dismiss();
                }
            }
        });
    }

    private void withdrawTicket() {
        operationTag = "withdraw";
        mUrlHandler.sendEmptyMessage(URL_GET_INFO);
        String jsonObjString = JsonStringUtils.confWithdrawAbiJsonToBinString(
                confName,confOrgn, userName,
                ConsaleSymbolString,"withdraw");//"consale"
        Message message = Message.obtain();
        message.obj = jsonObjString;
        message.what = URL_ABI_JSON_TO_BIN;
        mUrlHandler.sendMessage(message);
    }
}
