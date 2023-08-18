package com.example.myapplication.utils;

import android.util.Log;

import java.io.IOException;
import java.net.Socket;

public class RemoteConn {

    private static final String TAG="远程连接";
    //与指定的远程服务器建连接，返回一个Socket类型
    public static Socket getConn(String ip,int port){
        Socket s=null;
        //建立连接
        try {
            s = new Socket(ip, port);  //连接对应的ip和端口
            //System.out.println("连接成功！！！！");
            Log.i(TAG,"------连接成功-----");
            Log.i(TAG,"远程连接地址ip："+ip+"端口："+port);
        }catch (IOException e) {
            Log.e(TAG,"连接失败!!!");
            e.printStackTrace();
        }
        return s;
    }
    //关闭远程连接
    public static void closeConn(Socket s){
        try {
            s.close();
            Log.i(TAG,"------传输结束，释放连接-----");
        } catch (IOException e) {
            Log.e(TAG,"close error");
            e.printStackTrace();
        }
    }
}
