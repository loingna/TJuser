package com.example.myapplication.utils;

import java.io.FileOutputStream;
import java.util.Random;


public class CreateKey {
    private static final int BLOCK_SIZE = 16; // 分块大小为16字节


    //生成对称密钥SM4
    public static String generateKey(FileOutputStream fos) throws Exception {
        // 随机生成生成16字节的密钥（128位）
        byte[] key = new byte[16];
        Random random = new Random();
        random.nextBytes(key);
        //生成初始填充块
        byte[] iv = new byte[BLOCK_SIZE];  //初始块
        Random randoms = new Random();  //随机生成
        randoms.nextBytes(iv); //填充随机块
        //将生成的会话密码进行保存
        fos.write(key);
        fos.write(iv);
        fos.flush();
        fos.close();
        return "success";
    }

}
