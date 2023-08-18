package com.example.myapplication.dao;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.Security;

import org.bouncycastle.crypto.BlockCipher;
import org.bouncycastle.crypto.BufferedBlockCipher;
import org.bouncycastle.crypto.engines.SM4Engine;
import org.bouncycastle.crypto.modes.CBCBlockCipher;
import org.bouncycastle.crypto.paddings.PKCS7Padding;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class EncAndDec {
    private static final int KEY_SIZE = 16; // 密钥长度16字节
    private static final int BLOCK_SIZE = 16; // 分块大小为16字节

    static {
        Security.addProvider(new BouncyCastleProvider());
    }
    public static String encrypt(FileInputStream fis, FileOutputStream fos, byte[] key,byte[] iv)  {
        processFile(fis, fos, key,iv, true);
        return "success";
    }

    public static String decrypt(FileInputStream fis, FileOutputStream fos, byte[] key,byte[] iv) {
        processFile(fis, fos, key,iv, false);
        return "success";
    }
    //使用SM4进行加密和解密操作
    private static void processFile(FileInputStream fis, FileOutputStream fos, byte[] key,byte[] iv, boolean encrypt) {
        if (key.length != KEY_SIZE) {
            System.out.println("Invalid key size");
            throw new IllegalArgumentException("Invalid key size");
        }
        BufferedBlockCipher cipher = createCipher(key, iv, encrypt);
        try  {
            int bufferSize = BLOCK_SIZE * 1024;
            byte[] buffer = new byte[bufferSize];
            int bytesRead=0;
            while ((bytesRead = fis.read(buffer, 0, bufferSize)) != -1) {
                int outputSize = cipher.getOutputSize(bytesRead);
                byte[] outputBuffer = new byte[outputSize];
                //读取文件的内容进行加密或解密
                int processedBytes = cipher.processBytes(buffer, 0, bytesRead, outputBuffer, 0);
                fos.write(outputBuffer, 0, processedBytes);
            }
            byte[] outputBuffer = new byte[cipher.getOutputSize(0)];
            int processedBytes = cipher.doFinal(outputBuffer, 0);
            fos.write(outputBuffer, 0, processedBytes);
        }
        catch (Exception e) {
            // TODO: handle exception
            System.out.println("error");
            e.printStackTrace();
        }
    }
    //创建生成SM4块加密
    private static BufferedBlockCipher createCipher(byte[] key, byte[] iv, boolean encrypt) {
        BlockCipher engine = new SM4Engine();
        BlockCipher cbcEngine = new CBCBlockCipher(engine);
        BufferedBlockCipher cipher = new PaddedBufferedBlockCipher(cbcEngine, new PKCS7Padding());
        KeyParameter keyParam = new KeyParameter(key);
        ParametersWithIV params = new ParametersWithIV(keyParam, iv);
        cipher.init(encrypt, params);
        return cipher;
    }

    // 加密
    public static String AESencrypt(byte[] sSrc, String sKey, String ivParameter) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        byte[] raw = sKey.getBytes();
        SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
        IvParameterSpec iv = new IvParameterSpec(ivParameter.getBytes());//使用CBC模式，需要一个向量iv，可增加加密算法的强度
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);
        byte[] encrypted = cipher.doFinal(sSrc);
        String reslult= null;//此处使用BASE64做转码。
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            reslult = Base64.getEncoder().encodeToString(encrypted);
        }
        return reslult;
    }


    // 解密
    public static byte[] AESdecrypt(String sSrc, String encodingFormat, String sKey, String ivParameter) throws Exception {
        try {
            byte[] raw = sKey.getBytes(StandardCharsets.US_ASCII);
            SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            IvParameterSpec iv = new IvParameterSpec(ivParameter.getBytes());
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
            byte[] encrypted1 = new byte[0];//先用base64解密
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                encrypted1 = Base64.getDecoder().decode(sSrc);
            }
            return cipher.doFinal(encrypted1);
        } catch (Exception ex) {
            return null;
        }
    }

}
