package com.askey.dvr.cdr7010.dashcam.util;

import android.util.Base64;
import android.util.Log;

import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Cipher;

/**
 * keystore加密解密工具类
 * <p>
 * Created by Navas.li on 2018/7/27
 */
public class KeyStoreUtils {

    private static final String TAG = "KeyStoreUtils";
    private static KeyStoreUtils encryUtilsInstance;
    private Map<String, Object> stringObjectMap;

    public static KeyStoreUtils getInstance() throws Exception {
        synchronized (KeyStoreUtils.class) {
            if (null == encryUtilsInstance) {
                encryUtilsInstance = new KeyStoreUtils();
            }
        }
        return encryUtilsInstance;
    }

    private KeyStoreUtils() throws Exception {
        stringObjectMap = initKey();
    }

    private static final String KEY_ALGORITHM = "RSA";

    private static final String PUBLIC_KEY = "RSAPublicKey";
    private static final String PRIVATE_KEY = "RSAPrivateKey";

    private byte[] decryptBASE64(String key) {
        return Base64.decode(key, Base64.DEFAULT);
    }

    private String encryptBASE64(byte[] key) {
        return Base64.encodeToString(key, Base64.DEFAULT);
    }

    /**
     * 解密<br>
     * 用私钥解密
     */
    public String decryptByPrivateKey(String data)
            throws Exception {
        String key = getPrivateKey(stringObjectMap);
        Logg.d(TAG, "key==" + key);
        // 对密钥解密
        byte[] keyBytes = decryptBASE64(key);

        // 取得私钥
        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        Key privateKey = keyFactory.generatePrivate(pkcs8KeySpec);

        // 对数据解密
        Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] decode = Base64.decode(data, Base64.NO_WRAP);
        byte[] bytes = cipher.doFinal(decode);
        return new String(bytes);
    }

    /**
     * 加密<br>
     * 用公钥加密
     */
    public String encryptByPublicKey(String data)
            throws Exception {
        String key = getPublicKey(stringObjectMap);
        Logg.d(TAG, "key==" + key);
        // 对公钥解密
        byte[] keyBytes = decryptBASE64(key);

        // 取得公钥
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        Key publicKey = keyFactory.generatePublic(x509KeySpec);

        // 对数据加密
        Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] bytes = cipher.doFinal(data.getBytes());
        return Base64.encodeToString(bytes, Base64.NO_WRAP);
    }

    /**
     * 取得私钥
     */
    private String getPrivateKey(Map<String, Object> keyMap) {
        Key key = (Key) keyMap.get(PRIVATE_KEY);
        byte[] encoded = key.getEncoded();
        Log.d(TAG, "key.getEncoded()..." + encoded.length);
        return encryptBASE64(encoded);
    }

    /**
     * 取得公钥
     */
    private String getPublicKey(Map<String, Object> keyMap) {
        Key key = (Key) keyMap.get(PUBLIC_KEY);
        byte[] encoded = key.getEncoded();
        Log.d(TAG, "key.getEncoded()..." + encoded.length);
        return encryptBASE64(encoded);
    }

    /**
     * 初始化密钥
     */
    private Map<String, Object> initKey() throws Exception {
        KeyPairGenerator keyPairGen = KeyPairGenerator
                .getInstance(KEY_ALGORITHM);
        keyPairGen.initialize(1024);

        KeyPair keyPair = keyPairGen.generateKeyPair();

        // 公钥
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();

        // 私钥
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();

        Map<String, Object> keyMap = new HashMap<>(2);

        keyMap.put(PUBLIC_KEY, publicKey);
        keyMap.put(PRIVATE_KEY, privateKey);
        return keyMap;
    }
}
