package com.extropies.www.eoshackathon.Utils;

/**
 * Created by inst on 18-6-9.
 */

public class HexUtil { 

    // String "a" ===> String "61"
    public static String StringToAsciiString(String content) {
        String result = "";
        int max = content.length();
        for (int i = 0; i < max; i++) {
            char c = content.charAt(i);
            String b = Integer.toHexString(c);
            result = result + b;
        }
        return result;
    }
    public static String intTo2BytesHexString(int content){
        String returnString = "";
        String temp = Integer.toHexString(content);
        if (temp.length() == 1){
            returnString = "000" + temp;
        }else if (temp.length() == 2){
            returnString = "00" + temp;
        }else if (temp.length() == 3){
            returnString = "0" + temp;
        }else if (temp.length() == 4){
            returnString +=temp;
        }
        return returnString.toUpperCase();
    }
    public static String intTo1BytesHexString(int content){
        String returnString = "";
        String temp = Integer.toHexString(content);
        if (temp.length() == 1){
            returnString = "0" + temp;
        }else if (temp.length() == 2){
            returnString +=temp;
        }else {
            returnString = returnString.substring(temp.length()-2);
        }
        return returnString.toUpperCase();
    }
    //
    public static String byte2hex(byte b[]) {
        if (b == null) {
            throw new IllegalArgumentException(
                    "Argument b ( byte array ) is null! ");
        }
        String hs = "";
        String stmp = "";
        for (int n = 0; n < b.length; n++) {
            stmp = Integer.toHexString(b[n] & 0xff);
            if (stmp.length() == 1) {
                hs = hs + "0" + stmp;
            } else {
                hs = hs + stmp;
            }
        }
        return hs.toUpperCase();
    }
    //  String "aa" ===> int 170
    public static int hexStringToAlgorism(String hex) {
        hex = hex.toUpperCase();
        int max = hex.length();
        int result = 0;
        for (int i = max; i > 0; i--) {
            char c = hex.charAt(i - 1);
            int algorism = 0;
            if (c >= '0' && c <= '9') {
                algorism = c - '0';
            } else {
                algorism = c - 55;
            }
            result += Math.pow(16, max - i) * algorism;
        }
        return result;
    }
    public static byte[] hexStringToUnsignByte(String hex) {
        int max = hex.length() / 2;
        byte[] bytes = new byte[max];
        for (int i = 0; i < max; i++) {
            bytes[i] = (byte) HexUtil.hexStringToAlgorism(hex.substring(i*2,i*2+2));
        }
        return bytes;
    }
    // String "6131" ===> String "a1"
    //encodeType :  4 -> Unicode   2 -> normal
    public static String hexStringToString(String hexString, int encodeType) {
        String result = "";
        int max = hexString.length() / encodeType;
        for (int i = 0; i < max; i++) {
            char c = (char) HexUtil.hexStringToAlgorism(hexString
                    .substring(i * encodeType, (i + 1) * encodeType));
            result += c;
        }
        return result;
    }
}
