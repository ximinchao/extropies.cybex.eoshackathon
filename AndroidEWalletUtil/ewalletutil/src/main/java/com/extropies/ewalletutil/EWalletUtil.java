package com.extropies.ewalletutil;

public class EWalletUtil {
    static {
        System.loadLibrary("EWalletUtil");
    }

    public static native int add(int a, int b);

    public static native int PAEW_EOS_TX_Serialize(String jsonString, byte[] binData, int[] binDataLen);

    public final static int EOS_SER_PART_ALL = 0;
    public final static int EOS_SER_PART_1 = 1;
    public final static int EOS_SER_PART_2 = 2;
    public final static int EOS_SER_PART_3 = 3;
    public final static int EOS_SER_PART_4 = 4;
    public static native int PAEW_EOS_TX_Serialize_part(String jsonString, int nPart, byte[] binData, int[] binDataLen);
}
