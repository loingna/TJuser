package com.example.myapplication;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.os.Handler;
import android.os.Message;
import android.widget.EditText;
import android.widget.Toast;

//import com.example.myapplication.dao.UserDao;
//import com.example.myapplication.entity.User;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {
    private EditText username = null;
    private EditText password = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        Button button1 = findViewById(R.id.bt_lg);
        button1.setOnClickListener(this);

        Button button2 = findViewById(R.id.bt_reg);
        button2.setOnClickListener(this);

        //获取对应的值
        username=findViewById(R.id.new_username);
        password=findViewById(R.id.new_pwd);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.bt_lg:

                final Intent intent1 = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent1);
                break;

            case R.id.bt_reg:
                final Intent intent2 = new Intent(RegisterActivity.this, RegisterActivity.class);
                //启动活动（参数）
                startActivity(intent2);
                break;
        }
    }

    public void register(View view){
        //将前端的数据转为string类型
        String cusername = username.getText().toString();
        String cpassword = password.getText().toString();

        //判断是否符合输入要求
        if(cusername.length() < 2 || cpassword.length() < 2 ){
            Toast.makeText(getApplicationContext(),"输入信息不符合要求请重新输入",Toast.LENGTH_LONG).show();
            return;
        }
        //显示注册成功
        Toast.makeText(getApplicationContext(),"注册成功",Toast.LENGTH_LONG).show();
        //跳转到登录界面，并将注册的信息发送到登录界面
        Intent intent3 = new Intent(getApplication(),LoginActivity.class);
        //将想要传递的数据存放在SharedPreferences中进行持久化存储，传递给登录界面
        //申请变量，获取的容器名字为newuser
        final SharedPreferences sp=getSharedPreferences("newusers",MODE_PRIVATE);
        final SharedPreferences.Editor editor=sp.edit();
        editor.clear();  //先清一下数据，避免重复写入值
        editor.putString("newuser",cusername);  //存入数据
        editor.putString("newpwd",cpassword);
        editor.apply();  //提交数据
        startActivity(intent3);
    }
}