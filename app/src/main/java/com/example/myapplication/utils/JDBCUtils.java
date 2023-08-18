package com.example.myapplication.utils;

import android.util.Log;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class JDBCUtils {
    private static final String TAG="my sql is tjtest";

    static {

        try {
            Class.forName("com.mysql.jdbc.Driver");
            Log.v(TAG,"加载JDBC驱动成功");
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "加载JDBC驱动失败");
            e.printStackTrace();
        }

    }

    public static Connection getConn() {
        Connection  conn = null;
        try {
            conn= DriverManager.getConnection("jdbc:mysql://10.25.3.154:3306/tjtest?serverTimezone=Asia/Shanghai&useTimezone=true&useSSL=false","root","root");
            Log.d(TAG, "数据库连接成功");
        }catch (Exception exception){
            Log.d(TAG, "数据库连接失败"+exception.toString());
            exception.printStackTrace();
        }
        return conn;
    }

    public static void close(Connection conn){
        try {
            conn.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

}
