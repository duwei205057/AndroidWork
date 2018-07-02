package com.database;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by duwei on 18-5-14.
 */

public class CipherUtils {

    private static PublicKey mPublicKey;
    private static PrivateKey mPrivateKey;
    private static String password = "9588028899797979";
    static{
        try {
            KeyPair keyPair = genKeyPair(1024);
            mPublicKey = keyPair.getPublic();
            mPrivateKey = keyPair.getPrivate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 1:RSA 2:DES 3:AES
     */
    private static int MODE = 3;

    //生成密钥对
    private static KeyPair genKeyPair(int keyLength) throws Exception{
        KeyPairGenerator keyPairGenerator= KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(keyLength);
        return keyPairGenerator.generateKeyPair();
    }

    //公钥加密
    private static byte[] encryptByRSA(byte[] content) throws Exception{
        Cipher cipher=Cipher.getInstance("RSA");//java默认"RSA"="RSA/ECB/PKCS1Padding"
        cipher.init(Cipher.ENCRYPT_MODE, mPublicKey);
        return cipher.doFinal(content);
    }

    //私钥解密
    private static byte[] decryptByRSA(byte[] content) throws Exception{
        Cipher cipher=Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, mPrivateKey);
        return cipher.doFinal(content);
    }

    private static  byte[] encryptByDES(byte[] datasource) {
        try{
            SecureRandom random = new SecureRandom();
            DESKeySpec desKey = new DESKeySpec(password.getBytes());
            //创建一个密匙工厂，然后用它把DESKeySpec转换成
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
            SecretKey securekey = keyFactory.generateSecret(desKey);
            //Cipher对象实际完成加密操作
            Cipher cipher = Cipher.getInstance("DES");
            //用密匙初始化Cipher对象,ENCRYPT_MODE用于将 Cipher 初始化为加密模式的常量
            cipher.init(Cipher.ENCRYPT_MODE, securekey, random);
            //现在，获取数据并加密
            //正式执行加密操作
            return cipher.doFinal(datasource); //按单部分操作加密或解密数据，或者结束一个多部分操作
        }catch(Throwable e){
            e.printStackTrace();
        }
        return null;
    }

    private static byte[] decryptByDES(byte[] src) throws Exception {
        // DES算法要求有一个可信任的随机数源
        SecureRandom random = new SecureRandom();
        // 创建一个DESKeySpec对象
        DESKeySpec desKey = new DESKeySpec(password.getBytes());
        // 创建一个密匙工厂
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");//返回实现指定转换的 Cipher 对象
        // 将DESKeySpec对象转换成SecretKey对象
        SecretKey securekey = keyFactory.generateSecret(desKey);
        // Cipher对象实际完成解密操作
        Cipher cipher = Cipher.getInstance("DES");
        // 用密匙初始化Cipher对象
        cipher.init(Cipher.DECRYPT_MODE, securekey, random);
        // 真正开始解密操作
        return cipher.doFinal(src);
    }

    private static byte[] encryptByAES(byte[] content) throws Exception {
        SecretKeySpec key = generateKey(password);
        /**
         * 方案2,不用种子生成秘钥，直接将password作为秘钥
         * SecretKeySpec key = new SecretKeySpec(password.getBytes(), "AES/CBC/PKCS5PADDING");
         */
        Cipher cipher = Cipher.getInstance("AES/ECB/ZeroBytePadding");// 创建密码器
        cipher.init(Cipher.ENCRYPT_MODE, key);// 初始化
        byte[] result = cipher.doFinal(content);
        return result; // 加密
    }

    private static byte[] decryptByAES(byte[] content) throws Exception {
        SecretKeySpec key = generateKey(password);
        Cipher cipher = Cipher.getInstance("AES/ECB/ZeroBytePadding");// 创建密码器
        cipher.init(Cipher.DECRYPT_MODE, key);// 初始化
        byte[] result = cipher.doFinal(content);
        return result; // 加密
    }

    private static SecretKeySpec generateKey(String seed) throws Exception {
        // 获取秘钥生成器
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        // 通过种子初始化
        /**
         * 每次执行生成的秘钥都是不一样的。也就是说，加密时的秘钥如果没有保存到本地，解密的时候再次调用上述方法生成一个秘钥，那么将无法解密
         * SecureRandom secureRandom = new SecureRandom();
         */
//        SecureRandom secureRandom = new SecureRandom();
        /**
         * 方案1,使用Crypto,但是Android N（7.0）以后将不再支持，移除了Crypto
         */
        /*SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG", "Crypto");
        secureRandom.setSeed(seed.getBytes("UTF-8"));
        keyGenerator.init(128, secureRandom);
        // 生成秘钥并返回
        SecretKey secretKey = keyGenerator.generateKey();
        byte[] enCodeFormat = secretKey.getEncoded();
        SecretKeySpec key = new SecretKeySpec(enCodeFormat, "AES");*/
        /**
         * 方案2,不用种子生成秘钥，直接将password作为秘钥
         * SecretKeySpec key = new SecretKeySpec(password.getBytes(), "AES/CBC/PKCS5PADDING");
         * 必须要注意的是，这里的password的长度，必须为128或192或256bits.也就是16或24或32byte。否则会报出错误
         */
        SecretKeySpec key = new SecretKeySpec(password.getBytes(), "AES/CBC/PKCS5PADDING");
        return key;
    }

    public static byte[] encrypt(byte[] content) throws Exception {
        byte[] result;
        switch(MODE){
            case 1:
                result = encryptByRSA(content);
                break;
            case 2:
                result = encryptByDES(content);
                break;
            case 3:
            default:
                result = encryptByAES(content);
                break;
        }
        return result;
    }

    public static byte[] decrypt(byte[] content) throws Exception {
        byte[] result;
        switch(MODE){
            case 1:
                result = decryptByRSA(content);
                break;
            case 2:
                result = decryptByDES(content);
                break;
            case 3:
            default:
                result = decryptByAES(content);
                break;
        }
        return result;
    }
}
