package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.myapplication.dao.UserDao;
public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //启动配置，显示对应界面
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        //button事件申请
        Button button1 = findViewById(R.id.bt_lg);
        button1.setOnClickListener(this);

        Button button2 = findViewById(R.id.bt_reg);
        button2.setOnClickListener(this);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    //抽象接口的内部方法实现
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.bt_lg:

                final Intent intent1 = new Intent(LoginActivity.this, LoginActivity.class);
                //从一个界面跳到另一个界面
                startActivity(intent1);
                break;

            case R.id.bt_reg:
                final Intent intent2 = new Intent(LoginActivity.this, RegisterActivity.class);
                //启动活动（参数）
                startActivity(intent2);
                break;
        }
    }

    public void login(View view){

        //获取前端的输入
        EditText EditTextname = (EditText)findViewById(R.id.username);
        EditText EditTextpassword = (EditText)findViewById(R.id.pwd);
        //获取SharedPreferences中传递的数据
        SharedPreferences sp=getSharedPreferences("newusers",MODE_PRIVATE);
        String newuser=sp.getString("newuser",null);
        String newpwd= sp.getString("newpwd",null);
        //如果输入的和注册的账号密码一致则登录成功并将用户信息发送出去
        if (EditTextname.getText().toString().equals(newuser) && EditTextpassword.getText().toString().equals(newpwd)){
            //登录成功提示
            Toast.makeText(getApplicationContext(),"登录成功",Toast.LENGTH_LONG).show();
            //登录成功后跳转到主显示界面
            Intent intent4=new Intent(getApplicationContext(),Servicelogic.class);
            startActivity(intent4);
        }else{
            Toast.makeText(getApplicationContext(),"登录失败，账号密码错误！",Toast.LENGTH_LONG).show();
        }

//        new Thread(){
//            @Override
//            public void run() {
//
//                UserDao userDao = new UserDao();
//                //判断用户名和密码是否正确
//                boolean aa = userDao.login(EditTextname.getText().toString(),EditTextpassword.getText().toString());
//                if(aa){
//                    //登录成功提示
//                    Toast.makeText(getApplicationContext(),"登录成功",Toast.LENGTH_LONG).show();
//                    //登录成功后跳转到主显示界面
//                    Intent intent3=new Intent(getApplicationContext(),Servicelogic.class);
//                    //传递用户名的信息
//                    intent3.putExtra("usernames",EditTextname.getText().toString());
//                    startActivity(intent3);
//                }else {
//                    Toast.makeText(getApplicationContext(),"登录失败,用户名或者密码错误",Toast.LENGTH_LONG).show();
//                }
//            }
//        }.start();
    }


}