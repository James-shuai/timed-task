package com.qujie.timedtask.common.utils;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import java.security.SecureRandom;

/**
 * 3DES 加密
 */
public class DES {
    public static void main(String[] args) {
        String key = "DFD66E389DBF7CE532076285290D048C";
        String data =  "62148321309554661234567891000000";
        System.out.println(data.length());
        String des = encryptECB3Des(key,data);
        System.out.println(des);
    }

    public static String encryptECB3Des(String key, String src) {
        if (src.length()%16!=0){
            int num = 16-src.length()%16;
            src=src+String.format("%1$0"+(num)+"d",0);//长度不够右补0
        }
        System.out.println("encryptECB3Des->" + "key:" + key);
        System.out.println("encryptECB3Des->" + "src:" + src);
        int len = key.length();
        if (key == null || src == null) {
            return null;
        }
        if (src.length() % 16 != 0) {
            return null;
        }
        if (len == 32) {
            String outData = "";
            String str = "";
            for (int i = 0; i < src.length() / 16; i++) {
                str = src.substring(i * 16, (i + 1) * 16);
                outData += encECB3Des(key, str);
            }
            return outData.toUpperCase();
        }
        return null;
    }

    public static String encECB3Des(String key, String src) {
        byte[] temp = null;
        byte[] temp1 = null;
        temp1 = encryptDes(hexStringToBytes(key.substring(0, 16)), hexStringToBytes(src));
        temp = decryptDes(hexStringToBytes(key.substring(16, 32)), temp1);
        temp1 = encryptDes(hexStringToBytes(key.substring(0, 16)), temp);
        return bytesToHexString(temp1);
    }

    public static String decECB3Des(String key, String src) {
        byte[] temp2 = decryptDes(hexStringToBytes(key.substring(0, 16)), hexStringToBytes(src));
        byte[] temp1 = encryptDes(hexStringToBytes(key.substring(16, 32)), temp2);
        byte[] dest = decryptDes(hexStringToBytes(key.substring(0, 16)), temp1);
        return bytesToHexString(dest);
    }

    public static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }


    public static byte[] hexStringToBytes(String hexString) {
        if (hexString == null || hexString.equals("")) {
            return null;
        }
        hexString = hexString.toUpperCase();
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
        }
        return d;
    }
    private static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }


    /**
     * 3DES(双倍长) 解密
     *
     * @param src
     * @return
     */
    public static String decryptECB3Des(String key, String src) {
        if (key == null || src == null) {
            return null;
        }
        if (src.length() % 16 != 0) {
            return null;
        }
        if (key.length() == 32) {
            String outData = "";
            String str = "";
            for (int i = 0; i < src.length() / 16; i++) {
                str = src.substring(i * 16, (i + 1) * 16);
                outData += decECB3Des(key, str);
            }
            return outData;
        }
        return null;
    }

    /**
     * DES加密
     *
     */
    public static byte[] encryptDes(byte[] key, byte[] src) {
        try {
            // 创建一个DESKeySpec对象
            DESKeySpec desKey = new DESKeySpec(key);
            // 创建一个密匙工厂
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
            // 将DESKeySpec对象转换成SecretKey对象
            SecretKey secretKey = keyFactory.generateSecret(desKey);
            // Cipher对象实际完成解密操作
            Cipher cipher = Cipher.getInstance("DES/ECB/NoPadding");
            // 用密匙初始化Cipher对象
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            // 现在，获取数据并加密
            // 正式执行加密操作
            return cipher.doFinal(src);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * des解密
     *
     * @param key
     * @param src
     * @return
     */
    public static byte[] decryptDes(byte[] key, byte[] src) {
        try {
            // DES算法要求有一个可信任的随机数源
            SecureRandom random = new SecureRandom();
            // 创建一个DESKeySpec对象
            DESKeySpec desKey = new DESKeySpec(key);
            // 创建一个密匙工厂
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
            // 将DESKeySpec对象转换成SecretKey对象
            SecretKey secretKey = keyFactory.generateSecret(desKey);
            // Cipher对象实际完成解密操作
            Cipher cipher = Cipher.getInstance("DES/ECB/NoPadding");
            // 用密匙初始化Cipher对象
            cipher.init(Cipher.DECRYPT_MODE, secretKey, random);
            // 现在，获取数据并加密
            // 正式执行加密操作
            return cipher.doFinal(src);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}