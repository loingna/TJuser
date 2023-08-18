package com.example.myapplication.dao;

import com.example.myapplication.entity.User;
import com.example.myapplication.utils.JDBCUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;

public class UserDao {

    //登录判断
    public boolean login(String name,String password){

        String sql = "select * from user where name = ? and password = ?";

        Connection  con = JDBCUtils.getConn();

        try {
            //准备初始化sql语句
            PreparedStatement pst=con.prepareStatement(sql);
            //为sql语句赋值
            pst.setString(1,name);
            pst.setString(2,password);
            //sql语句执行 如果账号密码正确则返回true
            if(pst.executeQuery().next()){
                return true;
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }finally {
            JDBCUtils.close(con);
        }

        return false;
    }
    //用户注册
    public boolean register(User user){

        int userid=new Random().nextInt(100);
        String sql = "insert into user(Uid,username,password) values (userid,?,?)";

        Connection  con = JDBCUtils.getConn();

        try {
            PreparedStatement pst=con.prepareStatement(sql);

            pst.setString(1,user.getUsername());
            pst.setString(2,user.getPassword());
            //判断是否插入成功
            int value = pst.executeUpdate();
            if(value>0){
                return true;
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }finally {
            JDBCUtils.close(con);
        }
        return false;
    }

    //查询用户信息
    public User findUser(String name){

        String sql = "select * from user where name = ?";
        Connection  con = JDBCUtils.getConn();
        User user = null;
        try {

            PreparedStatement pst=con.prepareStatement(sql);
            pst.setString(1,name);
            //获得查询结果
            ResultSet rs = pst.executeQuery();
            //判断是否有对应的用户名
            while (rs.next()){ //rs.next是数组形式 数据库里面的列表属性是从0开始排序的

                int id = rs.getInt(0);
                String username = rs.getString(1);
                String password  = rs.getString(2);
                user = new User(id,username,password);
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }finally {
            JDBCUtils.close(con);
        }
        //将查询到的user用户的数据返回
        return user;
    }


}
