package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.dao.DataInteraction;
import com.example.myapplication.utils.FileReadload;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public class UserRegister extends AppCompatActivity implements View.OnClickListener {

    TextView showuser=null;
    private static final String TAG="主界面";
    private static final String TAG2="接收线程";
    String username=null;
    String[] otheruser=null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_register);
        SharedPreferences sp=getSharedPreferences("newusers",MODE_PRIVATE);
        username=sp.getString("newuser","error");
        showuser=findViewById(R.id.showusernames);
        //显示用户名字
        showuser.setText(username);

        //button事件申请
        Button button1 = findViewById(R.id.uploadown);
        button1.setOnClickListener(this);

        Button button2 = findViewById(R.id.personal);
        button2.setOnClickListener(this);

    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.uploadown:

                final Intent intent1 = new Intent(UserRegister.this, Servicelogic.class);
                //从一个界面跳到另一个界面
                startActivity(intent1);
                break;

            case R.id.personal:
                final Intent intent2 = new Intent(UserRegister.this, UserRegister.class);
                //启动活动（参数）
                startActivity(intent2);
                break;
        }

    }
    //获取私密钥
    @SuppressLint("SetTextI18n")
    public void getprivatekey(View view) throws IOException {
        //获得显示组件，以显示生成的信息
        EditText showprikey=findViewById(R.id.showprivatekey);
        //直接是文件名即可
        String privatekeyfile="privatekey.txt";
        //连接安全多方计算私密钥  这个是罗震宇那边的ip和端口
        String ip="10.25.2.62";
        int port=8001;
        // 用自己的id
        SharedPreferences sp=getSharedPreferences("userid",MODE_PRIVATE);
        String ids=sp.getString("id","error");
        FileOutputStream finalFosprifile = openFileOutput(privatekeyfile,Context.MODE_PRIVATE);
        new Thread(() -> {
            try {
                //获取私钥文件
                Log.i(TAG2,"启动！！！！");
                byte[] getprivatestr=DataInteraction.getprivate(ids,ip,port);
                if (getprivatestr.length !=5){
                    Log.i(TAG2,"得到私密钥成功！！！");
                    boolean  privateresult=FileReadload.safefilestr(finalFosprifile,getprivatestr);
                    if(privateresult){
                        Log.i(TAG2,"密钥文件存储成功，位置为："+privatekeyfile);
                    }else {
                        Log.e(TAG2,"存储失败");
                    }
                }else {
                    Log.e(TAG2,"读取私钥失败！！");
                }
            }catch (Exception e){
                Log.e(TAG2,"连接失败");
                e.printStackTrace();
            }
        }).start();
        try {
            Thread.sleep(20);
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }
        showprikey.setText("私钥获取成功！！！，存储位置为："+privatekeyfile);
        Toast.makeText(getApplicationContext(),"私密钥生成成功",Toast.LENGTH_LONG).show();
    }
    //记录选择的ip地址
    public void Selected(View view) {
        Spinner choosefile=findViewById(R.id.showconnect);
        //获取当前选中的ip地址
        String whichip=choosefile.getSelectedItem().toString();
        //将想要传递的数据存放在SharedPreferences中进行持久化存储
        //申请变量，获取的容器名字为getip
        final SharedPreferences sp=getSharedPreferences("getip",MODE_PRIVATE);
        final SharedPreferences.Editor editor=sp.edit();
        editor.clear();  //先清一下数据，避免重复写入值
        editor.putString("whichIP",whichip);  //存入数据
        editor.apply();  //提交数据
        Toast.makeText(getApplicationContext(),"选择成功",Toast.LENGTH_LONG).show();
    }
    // 实现从管理节点获取当前可供连接的ip地址
    public void ShowConnection(View view) {
        Spinner setip=findViewById(R.id.showconnect);
        //连接管理节点的ip和端口  这个是你本机的ip和端口
        String ip="10.25.2.177";
        int port=8900;
        //查看当前ip地址的指令
        SharedPreferences sp=getSharedPreferences("userid",MODE_PRIVATE);
        String ids=sp.getString("id","error"); //提取出自己的id
        String get_conn="1#get_connection#"+ids;
        AtomicReference<String> get_result = new AtomicReference<>();
        new Thread(() -> {
            try {
                //获取当前可供连接的ip
                Log.i(TAG2,"启动！！！！");
                get_result.set(DataInteraction.InteractingManagement(get_conn, ip, port));
                if (!Objects.equals(get_result.get(), "error")){
                    Log.i(TAG2,"ip获取成功");
                }else {
                    Log.e(TAG2,"ip获取失败！！");
                }
            }catch (Exception e){
                Log.e(TAG2,"连接失败");
                e.printStackTrace();
            }
        }).start();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }
        // 回应为形式：ip#ip#ip...形式
        String[] getip=get_result.get().split("#");
        // 申请一个列表用于保存添加的item
        ArrayList<String> items = new ArrayList<>(Arrays.asList(getip));
        // 将item添加到spinner组件中
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        setip.setAdapter(adapter);
        Toast.makeText(getApplicationContext(),"获取成功",Toast.LENGTH_LONG).show();
    }
    // 注册信息的操作
    @SuppressLint("SetTextI18n")
    public void registeruser(View view) {
        TextView showuser=findViewById(R.id.shownewuser);
        //连接管理节点的ip和端口
        String ip="10.25.2.177";
        int port=8900;
        // 获取当前手机的版本号
        String model=android.os.Build.VERSION.RELEASE;
        //形成注册语句
        String registercode="1#register#"+username+"#Android"+model;
        Log.i(TAG,"发送的数据为："+registercode);
        AtomicReference<String> get_result = new AtomicReference<>();
        //启动线程进行注册操作
        new Thread(() -> {
            try {
                //注册信息
                Log.i(TAG2,"启动！！！！");
                //将注册信息发送给管理节点
                get_result.set(DataInteraction.InteractingManagement(registercode, ip, port));
                if (!Objects.equals(get_result.get(), "error")){
                    Log.i(TAG2,"注册成功");
                }else {
                    Log.e(TAG2,"注册失败！！");
                }
            }catch (Exception e){
                Log.e(TAG2,"连接失败");
                e.printStackTrace();
            }
        }).start();
        //睡眠一秒保证上面线程执行结束  后期可以使用线程池更加节约时间
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }
        // 解析获得的注册信息格式为：权限#id
        String[] getid=get_result.get().split("#");
        Log.i(TAG,"获得的回应为："+getid[0]+getid[1]);
        //将id信息保存起来
        //将想要传递的数据存放在SharedPreferences中进行持久化存储
        //申请变量，获取的容器名字为userid
        final SharedPreferences sp=getSharedPreferences("userid",MODE_PRIVATE);
        final SharedPreferences.Editor editor=sp.edit();
        editor.clear();  //先清一下数据，避免重复写入值
        editor.putString("id",getid[1]);  //存入数据
        editor.putString("power",getid[0]);  //存入数据
        editor.apply();  //提交数据
        // 展示得到的注册信息
        showuser.setText("注册成功;  "+get_result.get());
        Toast.makeText(getApplicationContext(),"注册成功",Toast.LENGTH_LONG).show();
    }
    // 选中当前的用户
    public void selectuser(View view) {
        Spinner showother=findViewById(R.id.showother);
        //获取当前选中的ip地址
        String whichuser=showother.getSelectedItem().toString();
        int index=0;
        for (int i = 0; i < otheruser.length; i++) {
            if (otheruser[i].equals(whichuser)) {
                index=i;
                break;
            }
        }
        //将想要传递的数据存放在SharedPreferences中进行持久化存储
        //申请变量，获取的容器名字为getother
        final SharedPreferences sp=getSharedPreferences("getother",MODE_PRIVATE);
        final SharedPreferences.Editor editor=sp.edit();
        editor.clear();  //先清一下数据，避免重复写入值
        editor.putString("otherid",otheruser[index-1]);  //存入数据
        editor.putString("otherip",otheruser[index+1]);  //存入数据
        editor.apply();  //提交数据
        Toast.makeText(getApplicationContext(),"选择成功",Toast.LENGTH_LONG).show();

    }
    //展示其他用户的情况
    public void getotheruser(View view) {
        Spinner setother=findViewById(R.id.showother);
        //连接管理节点的ip和端口
        String ip="10.25.2.177";
        int port=8900;
        //查看当前其他用户的指令
        // 获取当前的id
        SharedPreferences sp=getSharedPreferences("userid",MODE_PRIVATE);
        String ids=sp.getString("id","error");
        //形成查看语句
        String get_other="1#get_other_user#"+ids;
        AtomicReference<String> get_result = new AtomicReference<>();
        new Thread(() -> {
            try {
                //获取当前可供发送的其他用户
                Log.i(TAG2,"启动！！！！");
                get_result.set(DataInteraction.InteractingManagement(get_other, ip, port));
                if (!Objects.equals(get_result.get(), "error")){
                    Log.i(TAG2,"用户获取获取成功");
                }else {
                    Log.e(TAG2,"用户信息获取失败！！");
                }
            }catch (Exception e){
                Log.e(TAG2,"连接失败");
                e.printStackTrace();
            }
        }).start();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }
        // 解析接收的信息，格式：用户id#名称#ip#用户id#名称#ip
        otheruser=get_result.get().split("#");
        Log.i(TAG,"获得的回应长度为："+otheruser.length);
        String[] othername=new String[otheruser.length/3];
        // 只取出名字
        int i=1;
        int j=0;
        while(i<otheruser.length){
            othername[j]=otheruser[i];
            i+=3;
            j+=1;
        }
        // 申请一个列表用于保存添加的item
        ArrayList<String> items = new ArrayList<>(Arrays.asList(othername));
        // 将item添加到spinner组件中
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        setother.setAdapter(adapter);
        Toast.makeText(getApplicationContext(),"获取成功",Toast.LENGTH_LONG).show();


    }
}