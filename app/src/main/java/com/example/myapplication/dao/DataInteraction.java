package com.example.myapplication.dao;

import android.util.Log;

import com.example.myapplication.utils.RemoteConn;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;


public class DataInteraction {
    private static final String TAG="数据交互";
    private static final String sKey="1111111111111111";
    private static final String ivParameter="9999999999999999";
    public static String Sendprotocol(String protocol, String ip, int port) throws IOException {
        //先获取一个Socket连接
        Socket s= RemoteConn.getConn(ip, port);
        //定义Socket写入流
        OutputStream os = null;
        //定义写入大小
        byte[] buffer = new byte[1024];
        //定义输入流，使用socket的inputStream对数据包进行读取*/
        InputStream is = null;
        //获取socket的OutputStream，以便向其中写入协议
        os = s.getOutputStream();
        //接收字符串
        String result=null;
        //写入最终文件的关键步骤
        try {
            //prorocol包含文件名和大小
            //发送协议
            String newprotocol=EncAndDec.AESencrypt(protocol.getBytes(),sKey,ivParameter);
            os.write(newprotocol.getBytes());
            os.write("finish".getBytes());
            //刷新一下
            os.flush();
            Log.i(TAG,"发送协议数据为："+protocol);
            //接收数据
            is = s.getInputStream();
            //读取出ok
            int nums=is.read(buffer);
            if(nums>0){
                result=new String(buffer,0,nums);
                Log.i(TAG,"读取协议回应数据为："+result);
            }else {
                Log.i(TAG,"读取协议回应失败");
                result="error";
            }
        } catch (FileNotFoundException e) {
            Log.e(TAG,"客户端读取文件出错");
            e.printStackTrace();
            return "error";
        } catch (IOException e) {
            Log.e(TAG,"客户端输出文件出错");
            e.printStackTrace();
            return "error";
        } catch (Exception e) {
            Log.e(TAG,"加密错误");
            e.printStackTrace();
            return "error";
        } finally {
            if(os !=null)
                os.close();
            if(is!=null)
                is.close();
            RemoteConn.closeConn(s);
        }
        //返回接收结果，如果为ok就发送成功，失败就为error
        return  result;
    }
    public static String SendData(FileInputStream fis, String ip, int port) throws IOException {

        //先获取一个Socket连接
        Socket s= RemoteConn.getConn(ip, port);
        //定义Socket写入流
        OutputStream os = null;
        //定义写入大小
        byte[] buffer = new byte[1024];
        //定义输入流，使用socket的inputStream对数据包进行读取*/
        InputStream is = null;
        //定义接收字符串
        String result=null;
        //先发送文件信息，再发送文件数据
        //获取socket的OutputStream，以便向其中写入数据包
        os = s.getOutputStream();
        //写入最终文件的关键步骤
        try {
            //读取文件
            //size 用来记录每次读取文件的大小
            int size = 0;
            //用count记录发送多少数据
            int count=0;
            //使用while循环读取文件，直到文件读取结束
            while((size = fis.read(buffer)) != -1){
                //写入socket流中
                os.write(buffer, 0, size);
                //刷新一下
                os.flush();
                count+=size;
            }
            Log.i(TAG,"发送文件大小为："+count);
            //接收数据
            is = s.getInputStream();
            //读取出ok
            int nums=is.read(buffer);
            if(nums>0){
                result=new String(buffer,0,nums);
                Log.i(TAG,"读取文件回应数据为："+result);
            }else {
                result="error";
            }
        } catch (FileNotFoundException e) {
            Log.e(TAG,"客户端读取文件出错");
            e.printStackTrace();
            return "error";
        } catch (IOException e) {
            Log.e(TAG,"客户端输出文件出错");
            e.printStackTrace();
            return "error";
        }finally {
            //关闭占用的资源
            if(fis!=null)
               fis.close();
            if(os !=null)
               os.close();
            if(is!=null)
                is.close();
            RemoteConn.closeConn(s);
        }
        return  result;
    }

    public static String GetData(FileOutputStream fos,byte[] datainfo,String ip, int port) throws IOException {
        //获取连接，接收来自服务器的文件
        Socket s= RemoteConn.getConn(ip, port);
        //定义读取流，使用socket的inputStream对数据包进行读取*/
        InputStream is = null;
        //定义写入流，向socket中进行写入
        OutputStream os=null;
        //定义byte数组来作为数据包的存储数据包*/
        byte[] buffer = new byte[2048];
        //先发送元数据文件
        try {
            //获取写入流
            os=s.getOutputStream();
            //发送元数据
            String infos="down#"+new String(datainfo);
            String fininfo=EncAndDec.AESencrypt(infos.getBytes(StandardCharsets.UTF_8),sKey,ivParameter);
            os.write(fininfo.getBytes());
            os.write("finish".getBytes());
            os.flush();
            Log.i(TAG,"元数据发送成功,发送长度为："+datainfo.length);
            //获取读取流，读取下载的文件
            is=s.getInputStream();
            //休眠0.02秒保证数据稳定发送
            try {
                Thread.sleep(20);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
            //size 用来记录每次读取文件的大小
            int size = 0;
            //用count记录发送多少数据
            int count=0;
            while((size=is.read(buffer))!=-1){
                //将读取的文件写到文件中
                boolean ok = new String(buffer, 0, 2).equals("ok");
                // 判断是否先接受到ok
                if (ok){
                    if(size>2&&size<2048){
                        fos.write(buffer,2,size);
                        fos.flush();
                        count+=size-2;
                    }else{
                        Log.i(TAG,"接收标识为："+new String(buffer, size - 2, 2));
                    }
                }else if(size<2048&&size>6){
                    // 判断是否接受到finish结束符
                    boolean finishString=new String(buffer, size-6, 6).equals("finish");
                    if(finishString) {
                        fos.write(buffer,0,size-6);
                        count+=size-6;
                        break;
                    }else {
                        fos.write(buffer,0,size);
                        count+=size;
                    }
                    fos.flush();
                }else {
                    // 接受刚好2048就直接写入进去
                    // 判断是否接受到finish结束符
                    boolean finishString= new String(buffer, size-6, 6).equals("finish");
                    if(finishString) {
                        fos.write(buffer,0,size-6);
                        count+=size-6;
                        break;
                    }else {
                        fos.write(buffer,0,size);
                        count+=size;
                    }
                    fos.flush();
                }
            }
            Log.i(TAG,"接收文件大小为: "+count);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG,"连接出错！！！");
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG,"加密失败");
            return null;
        } finally {
            //关闭占用的资源
            if(fos != null)
                fos.close();
            if(is !=null)
                is.close();
            if(os!=null)
                os.close();
            RemoteConn.closeConn(s);
        }
        //接收成功就返回保存的文件夹位置
        return "success";
    }
    //获取私秘钥
    public static byte[] getprivate(String id, String ip, int port) throws IOException {
        //先获取一个Socket连接
        Socket s= RemoteConn.getConn(ip, port);
        //定义Socket写入流
        OutputStream os = null;
        //定义写入大小
        byte[] buffer = new byte[1024];
        //定义输入流，使用socket的inputStream对数据包进行读取*/
        InputStream is = null;
        //获取socket的OutputStream，以便向其中写入协议
        os = s.getOutputStream();
        //初始化字符串指令
        String inis="1#"+id;
        //接收字符串
        byte[] result=null;
        //写入最终文件的关键步骤
        try {
            //发送协议
            os.write(inis.getBytes());
            os.write("finish".getBytes());
            //刷新一下
            os.flush();
            Log.i(TAG,"发送协议数据为："+inis);
            //接收数据
            is = s.getInputStream();
            //读取出私秘钥
            int nums=is.read(buffer);
            //安全多方那边是一次写入不需要加循环
            if(nums>0){
                result=Arrays.copyOfRange(buffer,0,nums);
                Log.i(TAG,"读取字节流长度为："+nums);
            }else {
                Log.i(TAG,"读取失败");
                result="error".getBytes();
            }
        } catch (FileNotFoundException e) {
            Log.e(TAG,"客户端读取文件出错");
            e.printStackTrace();
            return "error".getBytes();
        } catch (IOException e) {
            Log.e(TAG,"客户端输出文件出错");
            e.printStackTrace();
            return "error".getBytes();
        }finally {
            if(os !=null)
                os.close();
            if(is!=null)
                is.close();
            RemoteConn.closeConn(s);
        }
        //返回接收结果，如果为ok就发送成功，失败就为error
        return  result;
    }
    //加密对称密钥形成密钥信封
    public static String encrpysession(String userid,byte[] sessionkey,FileOutputStream savadatafile, String ip, int port) throws IOException {
        //先获取一个Socket连接
        Socket s= RemoteConn.getConn(ip, port);
        //定义Socket写入流
        OutputStream os = null;
        //定义写入大小
        byte[] buffer = new byte[1024];
        //定义输入流，使用socket的inputStream对数据包进行读取*/
        InputStream is = null;
        //获取socket的OutputStream，以便向其中写入协议
        os = s.getOutputStream();
        //发送的头数据
        String inis="2#"+userid+"#";
        //写入最终文件的关键步骤
        try {
            //发送数据
            os.write(inis.getBytes()); //先头协议
            os.write(sessionkey);  //再发送数据
            os.write("finish".getBytes()); //发送结尾符号
            os.flush();
            Log.i(TAG,"发送文件结束");
            //接收数据
            is = s.getInputStream();
            int nums=0;
            int getcount=0;
            nums = is.read(buffer);  //同样是一次发过来
            savadatafile.write(buffer,0,nums);
            savadatafile.flush();
//            while((nums = is.read(buffer)) != -1){
//                savadatafile.write(buffer,0,nums);
//                Log.i(TAG,"一次接收文件大小为："+nums);
//                savadatafile.flush();
//                getcount+=nums;
//            }
            Log.i(TAG,"接收文件大小为："+nums);
        } catch (FileNotFoundException e) {
            Log.e(TAG,"客户端读取文件出错");
            e.printStackTrace();
            return "error";
        } catch (IOException e) {
            Log.e(TAG,"客户端输出文件出错");
            e.printStackTrace();
            return "error";
        }finally {
            if(os !=null)
                os.close();
            if(is!=null)
                is.close();
            if(savadatafile !=null)
                savadatafile.close();
            RemoteConn.closeConn(s);
        }
        //返回接收结果
        return  "success";
    }
    //解密密钥信封
    public static byte[] decrpysession(FileInputStream fis, String ip, int port) throws IOException {
        //先获取一个Socket连接
        Socket s= RemoteConn.getConn(ip, port);
        //定义Socket写入流
        OutputStream os = null;
        //定义写入大小
        byte[] buffer = new byte[2048];
        //定义输入流，使用socket的inputStream对数据包进行读取*/
        InputStream is = null;
        //获取socket的OutputStream，以便向其中写入协议
        os = s.getOutputStream();
        //接受会话密钥字符串
        byte[] sessionkey=null;
        //发送的头数据
        //写入最终文件的关键步骤
        try {
            //发送数据
            //size 用来记录每次读取文件的大小
            int size = 0;
            //用count记录发送多少数据
            int count=0;
            //使用while循环读取文件，直到文件读取结束
            String inis="3#";  //先发送标识
            os.write(inis.getBytes());
            os.flush();
            while((size = fis.read(buffer)) != -1){
                //写入socket流中
                os.write(buffer, 0, size);
                Log.i(TAG,"一次读取的大小为："+size);
                //刷新一下
                os.flush();
                count+=size;
            }
            os.write("finish".getBytes());
            Log.i(TAG,"发送文件大小为："+count);
            //接收数据,接收解密之后的会话密钥
            is = s.getInputStream();
            int nums=0;
            nums=is.read(buffer); //同样是一次获取
            Log.i(TAG,"接收文件大小为："+nums);
            sessionkey= Arrays.copyOfRange(buffer,0,nums);
            Log.i(TAG,"接收文件成功");
        } catch (FileNotFoundException e) {
            Log.e(TAG,"客户端读取文件出错");
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            Log.e(TAG,"客户端输出文件出错");
            e.printStackTrace();
            return null;
        }finally {
            if(os !=null)
                os.close();
            if(is!=null)
                is.close();
            if(fis !=null)
                fis.close();
            RemoteConn.closeConn(s);
        }
        //返回接收结果
        return  sessionkey;
    }
    //用于链接指定的ip与port发送指定的语句
    public static String InteractingManagement(String codedata, String ip, int port)throws IOException{
        //先获取一个Socket连接
        Socket s= RemoteConn.getConn(ip, port);
        //定义Socket写入流
        OutputStream os = null;
        //定义输入流，使用socket的inputStream对数据包进行读取*/
        InputStream is = null;
        //定义写入大小
        byte[] buffer = new byte[1024];
        //获取socket的OutputStream，以便向其中写入数据包
        os = s.getOutputStream();
        //创建一个stringbuffer用于接收字符串
        StringBuilder resultBuffer=new StringBuilder();
        //写入最终文件的关键步骤
        try {
            os.write(codedata.getBytes());
            //刷新一下
            os.flush();
            //接收数据
            //休眠0.02秒保证数据稳定发送
            try {
                Thread.sleep(20);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
            is = s.getInputStream();
            //size 用来记录每次读取文件的大小
            int size = 0;
            //用count记录发送多少数据
            int count=0;
            //读取出数据
            while((size=is.read(buffer))!=-1){
                //判断后六位是否为finish，是就截断并跳出
                boolean teString=new String(buffer, size-6, 6).equals("finish");
                if(teString) {
                    resultBuffer.append(new String(buffer,0,size-6));
                    count+=size-6;
                    break;
                }else {
                    resultBuffer.append(new String(buffer,0,size));
                    count+=size;
                }
            }
            Log.i(TAG,"接收总长度为："+count);
        } catch (IOException e) {
            e.printStackTrace();
            return "error";
        } finally {
            //关闭占用的资源
            if(os !=null)
                os.close();
            if(is!=null)
                is.close();
            RemoteConn.closeConn(s);
        }
        return resultBuffer.toString();
    }
    public static String InteractingManagement1(String codedata, String ip, int port)throws IOException{
        //先获取一个Socket连接
        Socket s= RemoteConn.getConn(ip, port);
        //定义Socket写入流
        OutputStream os = null;
        //定义输入流，使用socket的inputStream对数据包进行读取*/
        InputStream is = null;
        //定义写入大小
        byte[] buffer = new byte[1024];
        //获取socket的OutputStream，以便向其中写入数据包
        os = s.getOutputStream();
        is = s.getInputStream();
        //创建一个stringbuffer用于接收字符串
        StringBuilder resultBuffer=new StringBuilder();
        //写入最终文件的关键步骤
        try {
            os.write("21".getBytes());
            os.flush();
            int num=is.read(buffer);
            if (num==2){
                os.write("ok".getBytes());
                os.write(codedata.getBytes());
                //刷新一下
                os.flush();
                Log.i(TAG,"发送协议成功！！");
            }
            //接收数据
            //休眠0.02秒保证数据稳定发送
            try {
                Thread.sleep(20);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
            //size 用来记录每次读取文件的大小
            int size = 0;
            //用count记录发送多少数据
            int count=0;
            //读取出数据
            while((size=is.read(buffer))!=-1){
                //判断后六位是否为finish，是就截断并跳出
                boolean teString=new String(buffer, size-6, 6).equals("finish");
                if(teString) {
                    resultBuffer.append(new String(buffer,0,size-6));
                    count+=size-6;
                    break;
                }else {
                    resultBuffer.append(new String(buffer,0,size));
                    count+=size;
                }
            }
            Log.i(TAG,"接收总长度为："+count);
        } catch (IOException e) {
            e.printStackTrace();
            return "error";
        } finally {
            //关闭占用的资源
            if(os !=null)
                os.close();
            if(is!=null)
                is.close();
            RemoteConn.closeConn(s);
        }
        return resultBuffer.toString();
    }
}
