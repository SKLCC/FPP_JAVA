package com.sklcc.fpp.utils.crc16;

public class GenerateCrc {
    public static String geneCRC(String temp) {
        byte[] strByte = new byte[temp.length() + 1];
        strByte = temp.getBytes();
        char ch = new CRC16().caluCRC(strByte);
        char []a = new char[2];
        String crcStr = Integer.toHexString((int) ch);
        switch (crcStr.length()) {
        case 1:
            crcStr = "000" + crcStr;
            a[0] = (char) (Integer.valueOf(crcStr.substring(0, 2), 16) + 0);
            a[1] = (char) (Integer.valueOf(crcStr.substring(2, 4), 16) + 0);
            crcStr = new String(a);
            break;
        case 2:
            crcStr = "00" + crcStr;
            a[0] = (char) (Integer.valueOf(crcStr.substring(0, 2), 16) + 0);
            a[1] = (char) (Integer.valueOf(crcStr.substring(2, 4), 16) + 0);
            crcStr = new String(a);
            break;
        case 3:
            crcStr = "0" + crcStr;
            a[0] = (char) (Integer.valueOf(crcStr.substring(0, 2), 16) + 0);
            a[1] = (char) (Integer.valueOf(crcStr.substring(2, 4), 16) + 0);
            crcStr = new String(a);
            break;
        case 4:
            a[0] = (char) (Integer.valueOf(crcStr.substring(0, 2), 16) + 0);
            a[1] = (char) (Integer.valueOf(crcStr.substring(2, 4), 16) + 0);
            crcStr = new String(a);
            break;
        }
        return crcStr;
    }
}
