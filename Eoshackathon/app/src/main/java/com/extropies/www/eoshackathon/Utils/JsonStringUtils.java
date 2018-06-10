package com.extropies.www.eoshackathon.Utils;

import android.util.Log;

import com.extropies.ewalletutil.EWalletUtil; 

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static junit.framework.Assert.assertEquals;

/**
 * Created by inst on 18-6-9.
 */

public class JsonStringUtils {

    public static String createTimeString(String currentTime, int plusSecond) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        Date date;
        try {
            date = sdf.parse(currentTime);
            long timeLong = date.getTime();
            timeLong += plusSecond*1000;
            return sdf.format(timeLong);
        }catch (ParseException e) {
            return "";
        }

    }
    public static String createAbiJsonToBinString(String from, String to, String quantity, String memo, String code, String action) {
        try {
            JSONObject jsonObj = new JSONObject();
            JSONObject jsonArgs = new JSONObject();
            jsonArgs.put("from", from);
            jsonArgs.put("to", to);
            jsonArgs.put("quantity", quantity);
            jsonArgs.put("memo", memo);
            jsonObj.put("code", code);
            jsonObj.put("action",action);
            jsonObj.put("args", jsonArgs);
            return jsonObj.toString();
        }catch (Exception e) {
            Log.e("exception:","error");
            return "";
        }
    }
    public static String confCreateAbiJsonToBinString(String arg1, String arg2, String arg3, String arg4, String code, String action) {
        try {
            JSONObject jsonObj = new JSONObject();
            JSONArray jsonArgs = new JSONArray();
            jsonArgs.put(arg1);
            jsonArgs.put(arg2);
            jsonArgs.put(arg3);
            jsonArgs.put(arg4);
            jsonObj.put("code", code);
            jsonObj.put("action",action);
            jsonObj.put("args", jsonArgs);
            return jsonObj.toString();
        }catch (Exception e) {
            Log.e("exception:","error");
            return "";
        }
    }
    public static String confWithdrawAbiJsonToBinString(String arg1, String arg2, String arg3, String code, String action) {
        try {
            JSONObject jsonObj = new JSONObject();
            JSONArray jsonArgs = new JSONArray();
            jsonArgs.put(arg1);
            jsonArgs.put(arg2);
            jsonArgs.put(arg3);
            jsonObj.put("code", code);
            jsonObj.put("action",action);
            jsonObj.put("args", jsonArgs);
            return jsonObj.toString();
        }catch (Exception e) {
            Log.e("exception:","error");
            return "";
        }
    }

    public static String createSignTransToBin(
            String expiration,
            int ref_block_num,
            String ref_block_prefix,
            String chainId,
            String  abiJsonToBin,
            String from,
            String accountName,
            String actionName) {
        try {
            JSONObject jsonObj = new JSONObject();
            jsonObj.put("expiration", expiration);
            jsonObj.put("ref_block_num", ref_block_num);//jsonObj.put("ref_block_num", 179);
            jsonObj.put("ref_block_prefix", ref_block_prefix);//jsonObj.put("ref_block_prefix", 1823777533);
            jsonObj.put("max_net_usage_words", 0);
            jsonObj.put("max_cpu_usage_ms", 0);
            jsonObj.put("delay_sec", 0);
            jsonObj.put("context_free_actions", new JSONArray());
            JSONObject jsonAuthorization = new JSONObject();
            jsonAuthorization.put("actor",from);
            jsonAuthorization.put("permission","active");
            JSONArray jsonAuthorizationArray = new JSONArray();
            jsonAuthorizationArray.put(jsonAuthorization);
            JSONObject jsonActions = new JSONObject();
            jsonActions.put("account",accountName);
            jsonActions.put("name",actionName);
            jsonActions.put("authorization",jsonAuthorizationArray);
            jsonActions.put("data",abiJsonToBin);
            JSONArray jsonActionsArray = new JSONArray();
            jsonActionsArray.put(jsonActions);
            jsonObj.put("actions", jsonActionsArray);
            jsonObj.put("transaction_extensions", new JSONArray());
            jsonObj.put("signatures", new JSONArray());
            jsonObj.put("context_free_data", new JSONArray());

            return jsonObj.toString();
        }catch (Exception e) {
            Log.e("exception:","error");
            return "";
        }
    }


    public static String createSignTransString(
            String expiration,
            int ref_block_num,
            String ref_block_prefix,
            String chainId,
            String  abiJsonToBin,
            String from,
            String fromAddress,
            String accountName,
            String actionName) {
        try {
            JSONObject jsonObj = new JSONObject();
            jsonObj.put("expiration", expiration);
            jsonObj.put("ref_block_num", ref_block_num);
            jsonObj.put("ref_block_prefix", ref_block_prefix);
            jsonObj.put("max_net_usage_words", 0);
            jsonObj.put("max_cpu_usage_ms", 0);
            jsonObj.put("delay_sec", 0);
            jsonObj.put("context_free_actions", new JSONArray());
            JSONObject jsonAuthorization = new JSONObject();
            jsonAuthorization.put("actor",from);
            jsonAuthorization.put("permission","active");
            JSONArray jsonAuthorizationArray = new JSONArray();
            jsonAuthorizationArray.put(jsonAuthorization);
            JSONObject jsonActions = new JSONObject();
            jsonActions.put("account",accountName);//"consale"
            jsonActions.put("name",actionName);
            jsonActions.put("authorization",jsonAuthorizationArray);
            jsonActions.put("data",abiJsonToBin);
            JSONArray jsonActionsArray = new JSONArray();
            jsonActionsArray.put(jsonActions);
            jsonObj.put("actions", jsonActionsArray);
            jsonObj.put("transaction_extensions", new JSONArray());
            jsonObj.put("signatures", new JSONArray());
            jsonObj.put("context_free_data", new JSONArray());

            JSONArray jsonObj2 = new JSONArray();
            jsonObj2.put(fromAddress);

            JSONArray finalArray = new JSONArray();
            finalArray.put(jsonObj);
            finalArray.put(jsonObj2);
            finalArray.put(chainId);
            return finalArray.toString();
        }catch (Exception e) {
            Log.e("exception:","error");
            return "";
        }
    }
    public static String createPushTransactionString(String signaturesString,String binArgs) {
        try {
            JSONObject jsonSignaturesObj = new JSONObject();
            JSONArray jsonSignaturesArray = new JSONArray();
            jsonSignaturesArray.put(signaturesString);
            jsonSignaturesObj.put("signatures", jsonSignaturesArray);
            jsonSignaturesObj.put("compression", "none");
            jsonSignaturesObj.put("packed_context_free_data", "");
            jsonSignaturesObj.put("packed_trx", binArgs);
            return jsonSignaturesObj.toString();
        }catch (Exception e) {
            Log.e("exception:","error");
            return "";
        }
    }

    public static String getSerializeBinString(String input) {
        int     iRtn = -1;
        byte[]  outBinData = null;
        int[]   outBinDataLen = new int[1];

        assertEquals(EWalletUtil.add(1, 2), 3);
        iRtn = EWalletUtil.PAEW_EOS_TX_Serialize(input, outBinData, outBinDataLen);
        assertEquals(iRtn, 0);
        outBinData = new byte[outBinDataLen[0]];
        iRtn = EWalletUtil.PAEW_EOS_TX_Serialize(input, outBinData, outBinDataLen);
        assertEquals(iRtn, 0);
        return HexUtil.byte2hex(outBinData);

    }
    public static String getSerializeBinPartString(String input,String partIndex) {
        int     iRtn = -1;
        byte[]  outBinData = null;
        int[]   outBinDataLen = new int[1];
        int part;
        if (partIndex.equals("part1")) {
            part = EWalletUtil.EOS_SER_PART_1;
        } else if (partIndex.equals("part2")) {
            part = EWalletUtil.EOS_SER_PART_2;
        } else if (partIndex.equals("part3")) {
            part = EWalletUtil.EOS_SER_PART_3;
        } else if (partIndex.equals("part4")) {
            part = EWalletUtil.EOS_SER_PART_4;
        } else {
            part = EWalletUtil.EOS_SER_PART_ALL;
        }

        assertEquals(EWalletUtil.add(1, 2), 3);
        iRtn = EWalletUtil.PAEW_EOS_TX_Serialize_part(input, part, outBinData, outBinDataLen);

        assertEquals(iRtn, 0);
        outBinData = new byte[outBinDataLen[0]];
        iRtn = EWalletUtil.PAEW_EOS_TX_Serialize_part(input, part, outBinData, outBinDataLen);
        assertEquals(iRtn, 0);
        return HexUtil.byte2hex(outBinData);
    }
}
