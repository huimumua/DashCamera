package com.askey.dvr.cdr7010.dashcam.util;

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;

/**
 * 获取文件的Hash值
 * <p>
 * Created by Navas.li on 2018/7/23
 */
public class HashUtil {
    private static final char[] hexDigits = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            'a', 'b', 'c', 'd', 'e', 'f'};

    public static String getHash(File file, String algorithm) {
        FileInputStream fis = null;
        try {
            MessageDigest md = MessageDigest.getInstance(algorithm);
            fis = new FileInputStream(file);
            byte[] buffer = new byte[8192];
            int length = -1;
            while ((length = fis.read(buffer)) != -1) {
                md.update(buffer, 0, length);
            }
            return byteToHexString(md.digest());
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        } finally {
            try {
                if (fis != null) {
                    fis.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * 计算文件的MD5码
     */
    public static String getMD5(File file) {
        return getHash(file, "MD5");
    }

    /**
     * 得到文件的SHA码,用于校验
     */
    public static String getSHA256(File file) {
        return getHash(file, "SHA256");
    }

    /**
     * 把byte[]数组转换成十六进制字符串表示形式
     *
     * @param tmp 要转换的byte[]
     * @return 十六进制字符串表示形式
     */
    private static String byteToHexString(byte[] tmp) {
        Log.d("byteToHexString", "length" + tmp.length);
        String s;
        // 用字节表示就是 16 个字节
        char str[] = new char[tmp.length * 2]; // 每个字节用 16 进制表示的话，使用两个字符，
        // 所以表示成 16 进制需要 32 个字符
        int k = 0; // 表示转换结果中对应的字符位置
        for (int i = 0; i < tmp.length; i++) { // 从第一个字节开始，对 MD5 的每一个字节
            // 转换成 16 进制字符的转换
            byte byte0 = tmp[i]; // 取第 i 个字节
            str[k++] = hexDigits[byte0 >>> 4 & 0xf]; // 取字节中高 4 位的数字转换,
            // >>> 为逻辑右移，将符号位一起右移
            str[k++] = hexDigits[byte0 & 0xf]; // 取字节中低 4 位的数字转换
        }
        s = new String(str); // 换后的结果转换为字符串
        return s;
    }
}
