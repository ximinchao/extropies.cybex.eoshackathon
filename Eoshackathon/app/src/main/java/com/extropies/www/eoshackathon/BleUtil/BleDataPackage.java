package com.extropies.www.eoshackathon.BleUtil;

import com.extropies.www.eoshackathon.Utils.HexUtil;

/** 
 * Created by inst on 18-6-9.
 */

public class BleDataPackage {
    public static boolean checkRspDataComplete(String rspString,String readHeadString){
        if (rspString.length() > 6 && rspString.substring(0, 2).equals(readHeadString)) {
            int length = HexUtil.hexStringToAlgorism(rspString.substring(2, 4))*0x100 + HexUtil.hexStringToAlgorism(rspString.substring(4, 6));
            if (length*2==rspString.length()){
                return true;
            }else {
                return false;
            }
        }
        return false;
    }

    public static byte[] BleDataWithCRC(String datastring) {
        int length = datastring.length()/2;
        byte[] databyte = HexUtil.hexStringToUnsignByte(datastring);
        int finalLength;
        finalLength = length + 1 + 2 + 2;
        byte[] databytefinal = new byte[finalLength];
        System.arraycopy(databyte,0,databytefinal,3,length);

        databytefinal[0] = (byte)0x81;
        databytefinal[1] = (byte)((finalLength>>8) & 0xff);
        databytefinal[2] = (byte)(finalLength & 0xff);
        int crc = crcData(databytefinal,finalLength-2);
        databytefinal[finalLength-2] = (byte)((crc>>8) & 0xff);
        databytefinal[finalLength-1] = (byte)(crc & 0xff);

        return databytefinal;
    }
    public static int crcData(byte[] databyte,int length){
        int y,yy;
        int x;
        int i,j;
        y = 0xffff;
        for (i=0;i<length;i++){
            x = databyte[i];
            for (j= 0;j<8;j++){
                if (((y^x) & 1) !=0) yy= 0x8408;
                else yy=0;
                x>>=1;
                y>>=1;
                y^=yy;
            }
        }
        return (~y & 0xffff);
    }
    public static int crcDataExtropies(byte[] databyte,int length){
        int j;
        int crcreg;
        int crcval;
        int i;

        crcreg = 0x0000;
        for (i=0;i<length;i++){
            crcval = databyte[i];
            for (j = 0; j < 8; j++){
                if ( ((crcreg ^ crcval) & 0x0001) !=0){
                    crcreg = (crcreg >> 1) ^ 0x8408;
                }else{
                    crcreg >>= 1;
                }
                crcval >>= 1;
            }
        }
        return crcreg;
    }
    public static int crcCalculate(String datastring){
        int length = datastring.length()/2;
        byte[] databyte = HexUtil.hexStringToUnsignByte(datastring);
        return crcDataExtropies(databyte,length);
    }
}
