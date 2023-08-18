package com.example.myapplication.utils;

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class FileReadload {

    //一次读取1024字节大小的文件--fileoutputStream
    public static String readFilestr(FileInputStream fis) throws IOException {
        //一次最多读1024B的文件
        byte[] data = new byte[1024];
        int len = fis.read(data);
        //将一次读取的数据转为String类型
        fis.close();
        return new String(data,0,len);
    }
    public static String readWinFile(FileInputStream file) throws IOException {

        StringBuilder test= new StringBuilder();
        byte[] b =new byte[1024];
        int len=-1;
        try {
            while ((len = file.read(b))!= -1){
                test.append(new String(b, 0, len));
            }
        }catch (IOException e){
            Log.e("ReadingFile","IOException");
        }
        //将一次读取的数据转为String类型
        return test.toString();
    }
    //读取大文件
    public static byte[][] readFile(String fileName) throws IOException {
        File file = new File(fileName);
        long fileSize = file.length();
        int bytesize=(int)Math.ceil((double)fileSize/1024);
        int lastsize=bytesize*1024-(int)fileSize;
        if (fileSize > 1024 * 1024 * 10) {
            throw new IOException("文件大小超过10M");
        }
        byte[][] data = new byte[bytesize][];
        FileInputStream fis = new FileInputStream(file);
        for (int i = 0; i < data.length; i++) {
            if(i==data.length-1) {
                data[i] = new byte[lastsize];
            }else {
                data[i] = new byte[1024];
            }
            int read = fis.read(data[i]);
            if (read == -1) {
                break;
            }
        }
        fis.close();
        return data;
    }
    //读出最多1024字节
    public static byte[] readFilebyte(FileInputStream fis) throws IOException {
        //一次最多读1024B的文件
        byte[] data = new byte[1024];
        int len = fis.read(data);
        fis.close();
        //将一次读取的数据返回
        return Arrays.copyOfRange(data,0,len);
    }
    //读取文件的头文件信息
    public static String readFileinfo(FileInputStream fis,String filename,String filetype,String id1,String id2) throws IOException {
        String result=null;
        try {
            String[] aa=filename.split("\\.");
            result="up#"+aa[0]+"#"+fis.available()+"#"+filetype+"#"+id1+"#"+id2;
        }catch (IOException e){
            Log.i("文件读取","读取失败");
            e.printStackTrace();
        }
        //fis.close();
        //返回文件的大小和文件名
        return result;
    }

    //写入文件---fileoutputStream
    public static boolean safefilestr(FileOutputStream fos,byte[] data){
        //写入文件
        try {
            fos.write(data);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
    //存储获得会话密钥文件
    public static boolean safesessionkey(FileOutputStream fos, byte[] key) throws IOException {
        // 将 DES 密钥保存在一个文件中
        fos.write(key);
        fos.flush();
        fos.close();
        return true;

    }



}
