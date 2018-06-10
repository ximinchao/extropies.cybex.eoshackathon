package com.extropies.www.eoshackathon;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.extropies.www.eoshackathon.BleUtil.ExBlePermission;
import com.extropies.www.eoshackathon.Utils.JsonStringUtils;
import com.extropies.www.eoshackathon.http.HttpUtilsCallback;
import com.extropies.www.eoshackathon.http.HttpUtilsGet; 
import com.extropies.www.eoshackathon.http.HttpUtilsPost;
import com.journeyapps.barcodescanner.CaptureActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.ref.WeakReference;

import static com.extropies.www.eoshackathon.MainActivity.BLE_SIGN_TRANSATION_RSP;
import static com.extropies.www.eoshackathon.MainActivity.EosSymbolString;
import static com.extropies.www.eoshackathon.MainActivity.LOG_TEXT_APPEND;
import static com.extropies.www.eoshackathon.MainActivity.URL_ABI_JSON_TO_BIN;
import static com.extropies.www.eoshackathon.MainActivity.URL_GET_BLOCK;
import static com.extropies.www.eoshackathon.MainActivity.URL_GET_CURRENCY_BALANCE;
import static com.extropies.www.eoshackathon.MainActivity.URL_GET_INFO;
import static com.extropies.www.eoshackathon.MainActivity.URL_PUSH_TRANSACTION;
import static com.extropies.www.eoshackathon.MainActivity.URL_SIGN_TRANSACTION;
import static com.extropies.www.eoshackathon.MainActivity.URL_WALLET_UNLOCK;
import static com.extropies.www.eoshackathon.MainActivity.urlHead;
import static com.extropies.www.eoshackathon.MainActivity.urlWalletHead;

/**
 * Created by inst on 18-6-9.
 */

public class TransferActivity extends Activity {
    private String userName = "";
    private String userAccountAddress = "";
    private String userAccountBalance = "";
    private boolean coldWalletType = false;

    private TextView fromAccountNameTextview;
    private TextView fromAccountAddressTextview;
    private TextView fromAccountBalanceTextview;
    private TextView logTextView;
    private EditText toAccountNameTextview;
    private EditText toAccountQuantityTextview;

    private String headBlockTime="";
    private String lastIrreversibleBlockNum = "";
    private String chainId = "";
    private String blockNum="";
    private String blockPrefix="";
    private String binArgs = "";
    private String serializeBin = "";
    private String signaturesString = "";

    private String serializeStringPartOne = "";
    private String serializeStringPartTwo= "";
    private String serializeStringPartThree = "";
    private String serializeStringPartFour = "";

    private UrlHandler mUrlHandler;
    public HttpUtilsCallback httpRsp;

    BluetoothAction bluetoothAction;

    private boolean showBalanceAfterPushTrans = false;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_transfer);

        Intent getIntent = getIntent();
        userName = getIntent.getStringExtra("userName");
        userAccountAddress = getIntent.getStringExtra("userAccountAddress");
        userAccountBalance = getIntent.getStringExtra("userAccountBalance");
        String walletType = getIntent.getStringExtra("walletType");
        if (walletType.equals("coldWallet")) {
            coldWalletType = true;
        } else {
            coldWalletType = false;
        }

        fromAccountNameTextview = findViewById(R.id.fromAccountName);
        fromAccountAddressTextview = findViewById(R.id.fromAccountAddress);
        fromAccountBalanceTextview = findViewById(R.id.fromAccountBalance);
        logTextView = findViewById(R.id.logTextView);
        toAccountNameTextview = findViewById(R.id.toAccountName);
        toAccountQuantityTextview = findViewById(R.id.toAccountQuantity);

        fromAccountNameTextview.setText(userName);
        fromAccountAddressTextview.setText(userAccountAddress);
        fromAccountBalanceTextview.setText(userAccountBalance);

        logTextView.append("From Account Name" + " : " + userName + "\n");
        logTextView.append("From Account Address" + " : " + userAccountAddress + "\n");
        logTextView.append("From Account Balance" + " : " + userAccountBalance + "\n");
        logTextView.setMovementMethod(ScrollingMovementMethod.getInstance());

        httpRsp = new HttpUtilsCallback() {
            @Override
            public void httpResponse(String url, String rsp) {
                getDataUrlRspString(url, rsp);
            }
        };

        toAccountQuantityTextview.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int start, int lengthBefore, int lengthAfter) {
                if (charSequence.toString().contains(".")) {
                    if (charSequence.length() - 1 - charSequence.toString().indexOf(".") > 4) {
                        charSequence = charSequence.toString().subSequence(0,
                                charSequence.toString().indexOf(".") + 5);
                        toAccountQuantityTextview.setText(charSequence);
                        toAccountQuantityTextview.setSelection(charSequence.length());
                    }
                }
                if (charSequence.toString().trim().substring(0).equals(".")) {
                    charSequence = "0" + charSequence;
                    toAccountQuantityTextview.setText(charSequence);
                    toAccountQuantityTextview.setSelection(2);
                }

                if (charSequence.toString().startsWith("0")
                        && charSequence.toString().trim().length() > 1) {
                    if (!charSequence.toString().substring(1, 2).equals(".")) {
                        toAccountQuantityTextview.setText(charSequence.subSequence(0, 1));
                        toAccountQuantityTextview.setSelection(1);
                        return;
                    }
                }
            }
            @Override
            public void afterTextChanged(Editable editable) {
            }

        });

        mUrlHandler = new UrlHandler(this.getMainLooper(), this);

        //ble
        if (coldWalletType) {
            bluetoothAction = new BluetoothAction();
            bluetoothAction.BluetoothInitialize(getApplication(),this,mUrlHandler);
        }
    }


    public void getInfoRspDone() {
        mUrlHandler.sendEmptyMessage(URL_GET_BLOCK);
        mUrlHandler.sendEmptyMessage(URL_GET_CURRENCY_BALANCE);
        mUrlHandler.sendEmptyMessage(URL_WALLET_UNLOCK);
        if (!headBlockTime.equals("")) {
            headBlockTime = JsonStringUtils.createTimeString(headBlockTime, 180);
        }
    }
    public void getBlockRspDone() {
        if (!headBlockTime.equals("") && !lastIrreversibleBlockNum.equals("") &&
                !chainId.equals("") && !blockNum.equals("") &&
                !blockPrefix.equals("") && !binArgs.equals("")
                ) {
            String serializeString = JsonStringUtils.createSignTransToBin(headBlockTime,Integer.valueOf(blockNum),blockPrefix,chainId,binArgs,userName,"eosio.token","transfer");
            serializeBin = JsonStringUtils.getSerializeBinString(serializeString);
            String signTransactionString = JsonStringUtils.createSignTransString(headBlockTime,Integer.valueOf(blockNum),blockPrefix,chainId,binArgs,userName,userAccountAddress,"eosio.token","transfer");

            serializeStringPartOne = JsonStringUtils.getSerializeBinPartString(serializeString, "part1");
            serializeStringPartTwo = JsonStringUtils.getSerializeBinPartString(serializeString, "part2");
            serializeStringPartThree = JsonStringUtils.getSerializeBinPartString(serializeString, "part3");
            serializeStringPartFour = JsonStringUtils.getSerializeBinPartString(serializeString, "part4");
            System.out.println("========================JsonStringUtils.getSerializeBinPartString part1: " + serializeStringPartOne);
            System.out.println("========================JsonStringUtils.getSerializeBinPartString part2: " + serializeStringPartTwo);
            System.out.println("========================JsonStringUtils.getSerializeBinPartString part3: " + serializeStringPartThree);
            System.out.println("========================JsonStringUtils.getSerializeBinPartString part4: " + serializeStringPartFour);

            if (coldWalletType) {
                bluetoothAction.setSerializeString(serializeStringPartOne, serializeStringPartTwo, serializeStringPartThree, serializeStringPartFour, chainId);
            } else {
                Message message = Message.obtain();
                message.obj = signTransactionString;
                message.what = URL_SIGN_TRANSACTION;
                mUrlHandler.sendMessage(message);
            }
        }
    }

    public void getCurrencyBalanceRspDone() {
        if (showBalanceAfterPushTrans) {
            showBalanceAfterPushTrans = false;
            Toast.makeText(this, "CurrencyBalance: " + userAccountBalance, Toast.LENGTH_LONG).show();
        }
    }
    public void walletUnlockRspDone() {

    }

    public void signTransactionRspDone() {
        System.out.println("===== signTransactionRspDone : "+ signaturesString + ":" + serializeBin );
        String pushTransString = JsonStringUtils.createPushTransactionString(signaturesString,serializeBin);
        System.out.println("===== pushTransString : "+ pushTransString );

        if (!pushTransString.equals("")) {
            Message message = Message.obtain();
            message.obj = pushTransString;
            message.what = URL_PUSH_TRANSACTION;
            mUrlHandler.sendMessage(message);
        }
    }
    public void pushTransRspDone() {
        mUrlHandler.sendEmptyMessage(URL_GET_CURRENCY_BALANCE);
        showBalanceAfterPushTrans = true;
    }

    public void resetVariables() {
        headBlockTime = "";
        lastIrreversibleBlockNum = "";
        chainId = "";
        blockNum="";
        blockPrefix="";
        binArgs = "";
        serializeBin = "";
        signaturesString = "";

        serializeStringPartOne = "";
        serializeStringPartTwo= "";
        serializeStringPartThree = "";
        serializeStringPartFour = "";

        showBalanceAfterPushTrans = false;
    }
    public void cancelBtnClick(View view) {
        this.finish();
    }
    public void confirmBtnClick(View view) {
        resetVariables();

        String toAccountNameString = toAccountNameTextview.getText().toString();
        String toAccountQuantityString = toAccountQuantityTextview.getText().toString();
        if (toAccountNameString.equals("") ) {
            toAccountNameTextview.setFocusable(true);
            toAccountNameTextview.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(toAccountNameTextview,InputMethodManager.SHOW_IMPLICIT);
            return;
        }
        if (toAccountQuantityString.equals("")) {
            toAccountQuantityTextview.setFocusable(true);
            toAccountQuantityTextview.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(toAccountQuantityTextview,InputMethodManager.SHOW_IMPLICIT);
            return;
        }

        if (!toAccountQuantityString.contains(".")) {
            toAccountQuantityString = toAccountQuantityString + ".0000";
            toAccountQuantityTextview.setText(toAccountQuantityString);
        } else {
            int offset = toAccountQuantityString.length() - 1 - toAccountQuantityString.indexOf(".");
            if (offset == 3) {
                toAccountQuantityString = toAccountQuantityString+'0';
            } else if (offset == 2) {
                toAccountQuantityString = toAccountQuantityString+"00";
            }else if (offset == 1) {
                toAccountQuantityString = toAccountQuantityString+"000";
            }
            toAccountQuantityTextview.setText(toAccountQuantityString);
        }

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
        }

        if (coldWalletType) {
            if (ExBlePermission.checkPermissions(this)) {
                bluetoothAction.startScan();
            } else {
                logTextAppend("Bluetooth permission denied");
            }
        } else {

        }

        mUrlHandler.sendEmptyMessage(URL_GET_INFO);

        String jsonObjString = JsonStringUtils.createAbiJsonToBinString(
                userName,toAccountNameString, toAccountQuantityString + " "+EosSymbolString,
                "memo","eosio.token","transfer");
        if (!jsonObjString.equals("")) {
            Message message = Message.obtain();
            message.obj = jsonObjString;
            message.what = URL_ABI_JSON_TO_BIN;
            mUrlHandler.sendMessage(message);
        }
    }

    private void logTextAppend(String input) {
        logTextView.append(input+"\n");
        int offset=logTextView.getLineCount()*logTextView.getLineHeight();
        if(offset>(logTextView.getHeight()-logTextView.getLineHeight()-20)){
            logTextView.scrollTo(0,offset-logTextView.getHeight()+logTextView.getLineHeight()+20);
        }
    }

    private class UrlHandler extends Handler {
        private final WeakReference<TransferActivity> reference;
        UrlHandler(Looper looper, TransferActivity activity) {
            super(looper);
            reference = new WeakReference<>(activity);
        }
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case URL_GET_INFO:
                    new HttpUtilsGet(urlHead + "/v1/chain/get_info",httpRsp).execute();
                    break;
                case URL_ABI_JSON_TO_BIN:
                    new HttpUtilsPost(urlHead + "/v1/chain/abi_json_to_bin",(String)msg.obj,httpRsp).execute();
                    break;
                case URL_SIGN_TRANSACTION:
                    new HttpUtilsPost(urlWalletHead + "/v1/wallet/sign_transaction",(String)msg.obj,httpRsp).execute();
                    break;
                case URL_GET_BLOCK:
                    try {
                        JSONObject jsonObj = new JSONObject();//
                        jsonObj.put("block_num_or_id", lastIrreversibleBlockNum);//
                        new HttpUtilsPost(urlHead + "/v1/chain/get_block",jsonObj.toString(),httpRsp).execute();
                    }catch (Exception e) {
                        Log.e("exception:","error");
                    }
                    break;
                case URL_GET_CURRENCY_BALANCE:
                    try {
                        JSONObject jsonObj = new JSONObject();
                        jsonObj.put("account", userName);
                        jsonObj.put("code","eosio.token");
                        jsonObj.put("symbol", EosSymbolString);
                        new HttpUtilsPost(urlHead + "/v1/chain/get_currency_balance",jsonObj.toString(),httpRsp).execute();
                    }catch (Exception e) {
                        Log.e("exception:","error");
                    }
                    break;
                case URL_WALLET_UNLOCK:
                    JSONArray jsonObj = new JSONArray();
                    jsonObj.put("ximc");
                    jsonObj.put("PW5JNcqr73uN2jzcp3cG6nXKErNY2bZrJexYUNCP1aRL4AqYGugQh");
                    String urlString = urlWalletHead + "/v1/wallet/unlock";
                    new HttpUtilsPost(urlString,jsonObj.toString(),httpRsp).execute();
                    break;
                case URL_PUSH_TRANSACTION:
                    logTextAppend("pushTransString" + " : " + (String)msg.obj);
                    new HttpUtilsPost(urlHead + "/v1/chain/push_transaction",(String)msg.obj,httpRsp).execute();
                    break;
                case BLE_SIGN_TRANSATION_RSP:
                    signaturesString = (String)msg.obj;
                    signTransactionRspDone();
                    break;
                case LOG_TEXT_APPEND:
                    logTextAppend((String)msg.obj);
                    break;
                default:
                    break;
            }
        };
    }

    public void getDataUrlRspString(String targeurl,String stringvalue) {
        if (targeurl.endsWith("get_currency_balance")) {
            String[] stringlist = stringvalue.split("\"");
            if (stringlist.length == 3) {
                userAccountBalance = stringlist[1];
                logTextAppend("currency_balance" + " : " + userAccountBalance);
                getCurrencyBalanceRspDone();
            }
        } else if (targeurl.endsWith("get_info")) {
            try {
                JSONObject jsonObject = new JSONObject(stringvalue);
                System.out.println("===== get_info rsp:"+ stringvalue);
                if (jsonObject.has("head_block_time")) {
                    headBlockTime = jsonObject.optString("head_block_time");
                    logTextAppend("head_block_time" + " : " + headBlockTime);
                } else {
                    logTextAppend("get head_block_time error");
                }
                if (jsonObject.has("last_irreversible_block_num")) {
                    lastIrreversibleBlockNum = jsonObject.optString("last_irreversible_block_num");
                    logTextAppend("last_irreversible_block_num" + " : " + lastIrreversibleBlockNum );
                } else {
                    logTextAppend("get last_irreversible_block_num error");
                }
                chainId = "";
                if (jsonObject.has("chain_id")) {
                    chainId = jsonObject.optString("chain_id");
                    logTextAppend("chain_id" + " : " + lastIrreversibleBlockNum);
                } else {
                    logTextAppend("get chain_id error");
                }
                if (!headBlockTime.equals("") && !lastIrreversibleBlockNum.equals("")) {
                    getInfoRspDone();

                }
            } catch (Exception e) {
                Log.e("exception:","error");
            }
        } else if (targeurl.endsWith("get_block")) {
            try {
                JSONObject jsonObject = new JSONObject(stringvalue);
                System.out.println("===== get_block rsp:"+ stringvalue);
                if (jsonObject.has("block_num")) {
                    blockNum=jsonObject.optString("block_num");
                    logTextAppend("block_num" + " : " + blockNum);
                } else {
                    logTextAppend("get block_num error");
                }
                if (jsonObject.has("ref_block_prefix")) {
                    blockPrefix=jsonObject.optString("ref_block_prefix");
                    logTextAppend("ref_block_prefix" + " : " + blockPrefix);
                    getBlockRspDone();
                } else {
                    logTextAppend("get ref_block_prefix error");
                }

            } catch (Exception e) {
                Log.e("exception:","error");
            }
        } else if (targeurl.endsWith("abi_json_to_bin")) {
            try {
                JSONObject jsonObject = new JSONObject(stringvalue);
                System.out.println("===== abi_json_to_bin rsp:"+ stringvalue);
                if (jsonObject.has("binargs")) {
                    binArgs=jsonObject.optString("binargs");
                    logTextAppend("binargs" + " : " + binArgs);
                } else {
                    logTextAppend("get abi_json_to_bin error");
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
                    logTextAppend("signatures" + " : " + signaturesString);
                    signTransactionRspDone();
                } else {
                    logTextAppend("get signatures error");
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
            if (coldWalletType) {
                bluetoothAction.disconnectBluetooth();
            }
            try {
                JSONObject jsonObject = new JSONObject(stringvalue);
                System.out.println("===== push_transaction rsp:"+ stringvalue);
                if (jsonObject.has("code") && jsonObject.getInt("code") == 500) {
                    logTextAppend("push transaction error code:500");
                } else {
                    pushTransRspDone();
                }
            } catch (Exception e) {
                Log.e("exception:","error");
            }
        }
    }
}
