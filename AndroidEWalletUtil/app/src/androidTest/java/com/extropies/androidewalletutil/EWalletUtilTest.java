package com.extropies.androidewalletutil;

import android.support.test.runner.AndroidJUnit4;

import com.extropies.ewalletutil.EWalletUtil;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class EWalletUtilTest {
    @Test
    public void serializeTest() {
        int     iRtn = -1;
        String  strJson = "{ \"expiration\": \"2018-05-24T11:34:03\", \"ref_block_num\": 47172, \"ref_block_prefix\": \"3655466833\", \"max_net_usage_words\": 0, \"max_cpu_usage_ms\": 0, \"delay_sec\": 0, \"context_free_actions\": [], \"actions\": [ { \"account\": \"consale\", \"name\": \"getlist\", \"authorization\": [ { \"actor\": \"alice\", \"permission\": \"active\" } ], \"data\": \"\" } ], \"transaction_extensions\": []}";
        byte[]  destBinData = {(byte)0x2b, (byte)0xa3, (byte)0x06, (byte)0x5b, (byte)0x44, (byte)0xb8, (byte)0x51, (byte)0xff, (byte)0xe1, (byte)0xd9, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x01, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x40, (byte)0x45, (byte)0x83, (byte)0x27, (byte)0x45, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x20, (byte)0x63, (byte)0x17, (byte)0xb3, (byte)0x62, (byte)0x01, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x85, (byte)0x5c, (byte)0x34, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0xa8, (byte)0xed, (byte)0x32, (byte)0x32, (byte)0x00, (byte)0x00};
        byte[]  destBinData1 = {(byte)0x2b, (byte)0xa3, (byte)0x06, (byte)0x5b, (byte)0x44, (byte)0xb8, (byte)0x51, (byte)0xff, (byte)0xe1, (byte)0xd9, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00};
        byte[]  destBinData2 = {(byte)0x01};
        byte[]  destBinData3 = {(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x40, (byte)0x45, (byte)0x83, (byte)0x27, (byte)0x45, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x20, (byte)0x63, (byte)0x17, (byte)0xb3, (byte)0x62, (byte)0x01, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x85, (byte)0x5c, (byte)0x34, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0xa8, (byte)0xed, (byte)0x32, (byte)0x32, (byte)0x00};
        byte[]  destBinData4 = {(byte)0x00};
        byte[]  outBinData = null;
        int[]   outBinDataLen = new int[1];

        assertEquals(EWalletUtil.add(1, 2), 3);

        iRtn = EWalletUtil.PAEW_EOS_TX_Serialize(strJson, null, outBinDataLen);
        assertEquals(iRtn, 0);
        outBinData = new byte[outBinDataLen[0]];
        iRtn = EWalletUtil.PAEW_EOS_TX_Serialize(strJson, outBinData, outBinDataLen);
        assertEquals(iRtn, 0);
        assertArrayEquals(destBinData, outBinData);

        iRtn = EWalletUtil.PAEW_EOS_TX_Serialize_part(strJson, EWalletUtil.EOS_SER_PART_ALL, null, outBinDataLen);
        assertEquals(iRtn, 0);
        outBinData = new byte[outBinDataLen[0]];
        iRtn = EWalletUtil.PAEW_EOS_TX_Serialize_part(strJson, EWalletUtil.EOS_SER_PART_ALL, outBinData, outBinDataLen);
        assertEquals(iRtn, 0);
        assertArrayEquals(destBinData, outBinData);

        iRtn = EWalletUtil.PAEW_EOS_TX_Serialize_part(strJson, EWalletUtil.EOS_SER_PART_1, null, outBinDataLen);
        assertEquals(iRtn, 0);
        outBinData = new byte[outBinDataLen[0]];
        iRtn = EWalletUtil.PAEW_EOS_TX_Serialize_part(strJson, EWalletUtil.EOS_SER_PART_1, outBinData, outBinDataLen);
        assertEquals(iRtn, 0);
        assertArrayEquals(destBinData1, outBinData);

        iRtn = EWalletUtil.PAEW_EOS_TX_Serialize_part(strJson, EWalletUtil.EOS_SER_PART_2, null, outBinDataLen);
        assertEquals(iRtn, 0);
        outBinData = new byte[outBinDataLen[0]];
        iRtn = EWalletUtil.PAEW_EOS_TX_Serialize_part(strJson, EWalletUtil.EOS_SER_PART_2, outBinData, outBinDataLen);
        assertEquals(iRtn, 0);
        assertArrayEquals(destBinData2, outBinData);

        iRtn = EWalletUtil.PAEW_EOS_TX_Serialize_part(strJson, EWalletUtil.EOS_SER_PART_3, null, outBinDataLen);
        assertEquals(iRtn, 0);
        outBinData = new byte[outBinDataLen[0]];
        iRtn = EWalletUtil.PAEW_EOS_TX_Serialize_part(strJson, EWalletUtil.EOS_SER_PART_3, outBinData, outBinDataLen);
        assertEquals(iRtn, 0);
        assertArrayEquals(destBinData3, outBinData);

        iRtn = EWalletUtil.PAEW_EOS_TX_Serialize_part(strJson, EWalletUtil.EOS_SER_PART_4, null, outBinDataLen);
        assertEquals(iRtn, 0);
        outBinData = new byte[outBinDataLen[0]];
        iRtn = EWalletUtil.PAEW_EOS_TX_Serialize_part(strJson, EWalletUtil.EOS_SER_PART_4, outBinData, outBinDataLen);
        assertEquals(iRtn, 0);
        assertArrayEquals(destBinData4, outBinData);
    }
}