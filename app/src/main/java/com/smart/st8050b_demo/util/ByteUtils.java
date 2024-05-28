/*
 * Copyright (c) 2011-2015. ShenZhen trendit Information Technology Co.,Ltd.
 *
 * All right reserved.
 *
 * This software is the confidential and proprietary
 * information of trendit Company of China.
 * ("Confidential Information"). You shall not disclose
 * such Confidential Information and shall use it only
 * in accordance with the terms of the contract agreement
 * you entered into with trendit inc.
 */

package com.smart.st8050b_demo.util;

import android.text.TextUtils;

import java.io.UnsupportedEncodingException;

public class ByteUtils {

    /**
     * 字符串转换为ASCII码表示的Byte数组
     * 方法名：string2ASCIIByteArray
     * �? 述：
     *
     * @param str
     * @return byte[] �? 期：2014�?10�?13�? by lijinniu
     */
    public static byte[] string2ASCIIByteArray(String str) {
        if (TextUtils.isEmpty(str)) {
            return null;
        }
        byte[] data = null;
        try {
            data = str.getBytes("US-ASCII");
        } catch (UnsupportedEncodingException e) {

            android.util.Log.e("xgd", "字符串转换为ASCII码Byte数组错误");
            e.printStackTrace();
        }
        return data;
    }

    /**
     * ASCII码byte数组转为字符�?
     * 方法名：byteArray2String
     * �? 述：aaa
     *
     * @param data
     * @return String �? 期：2014�?10�?13�? by lijinniu
     */
    public static String asciiByteArray2String(byte[] data) {
        if (data == null) return "";
        StringBuffer tStringBuf = new StringBuffer();
        char[] tChars = new char[data.length];

        int end = 0;
        for (int i = 0; i < data.length; i++) {
            /*if (data[i] == 0x20) {
                end = i;
                break; // 卡号�?后可能含有空格，去除空格
            } else {
                end = data.length;
            }*/
            end = data.length;
            tChars[i] = (char) data[i];
        }

        tStringBuf.append(tChars, 0, end);

        return tStringBuf.toString().trim();
    }


    /**
     * hex string convert to byte[]
     */
    public static byte[] hexString2ByteArray(String hexStr) {
        if (hexStr == null) {
            return null;
        }

        if (hexStr.length() % 2 != 0) {
            return null;
        }
        byte[] data = new byte[hexStr.length() / 2];
        for (int i = 0; i < hexStr.length() / 2; i++) {
            char hc = hexStr.charAt(2 * i);
            char lc = hexStr.charAt(2 * i + 1);
            byte hb = hexChar2Byte(hc);
            byte lb = hexChar2Byte(lc);

            if (hb < 0 || lb < 0) {
                return null;
            }

            int n = hb << 4;
            data[i] = (byte) (n + lb);
        }

        return data;
    }



    public static byte hexChar2Byte(char c) {
        if (c >= '0' && c <= '9') {
            return (byte) (c - '0');
        } else if (c >= 'a' && c <= 'f') {
            return (byte) (c - 'a' + 10);
        } else if (c >= 'A' && c <= 'F') {
            return (byte) (c - 'A' + 10);
        } else {
            return -1;
        }
    }


    /**
     * byte[] convert to hex string
     */
    public static String byteArray2HexString(byte[] arr) {
        if (arr == null || arr.length == 0) {
            return null;
        }

        StringBuilder sbd = new StringBuilder();
        for (byte b : arr) {
            String tmp = Integer.toHexString(0xFF & b);
            if (tmp.length() < 2) {
                tmp = "0" + tmp;
            }

            sbd.append(tmp);
        }

        return sbd.toString();
    }



}
