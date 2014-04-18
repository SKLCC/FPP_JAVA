package com.sklcc.fpp.example;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.alibaba.druid.pool.DruidPooledConnection;
import com.sklcc.fpp.utils.sqls.MySQLPool;

public class MySQLPoolExample {

    /*
     * Before run this example, you should run this SQL first.
     *      > create database fpp;
     *      > use fpp;
     *      > create table mike (
     *      >   name char(20)
     *      > );
     * and make sure you have the user fpp with password fpp
     */
    public static void main(String[] args) {
        MySQLPool mySQLPool = MySQLPool.getInstance();

        try {
            DruidPooledConnection connection = mySQLPool.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement("show tables;");
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                System.out.println(resultSet.getString(1));
            }

            connection.recycle(); // IMPORTANT
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }
}
