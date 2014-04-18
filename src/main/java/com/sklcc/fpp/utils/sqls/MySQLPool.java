package com.sklcc.fpp.utils.sqls;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Properties;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidDataSourceFactory;
import com.alibaba.druid.pool.DruidPooledConnection;

public class MySQLPool {
    private static MySQLPool       databasePool = null;
    private static DruidDataSource dataSource   = null;     //开源数据库连接池
    private static final String    MYSQLCONFIG  = "mysql.properties";
    //mysql的配置信息

    static {
        Properties properties = loadPropertyFile(MYSQLCONFIG);    //加载mysql配置信息，见下面的方法
        try {
            dataSource = (DruidDataSource) DruidDataSourceFactory.createDataSource(properties);
            //通过datasource接口创建数据库连接池
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private MySQLPool() {}
    
    //单列模式
    public static synchronized MySQLPool getInstance() {
        if (null == databasePool) {
            databasePool = new MySQLPool();
        }
        return databasePool;
    }
    
    //连接数据库
    public DruidPooledConnection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public static Properties loadPropertyFile(String fullFile) {
        if (null == fullFile || fullFile.equals(""))
            throw new IllegalArgumentException("Properties file path can not be null : " + fullFile);
        InputStream inputStream = null;
        Properties properties = null;
        try {
            inputStream = MySQLPool.class.getClassLoader().getResourceAsStream(fullFile);
            properties = new Properties();
            properties.load(inputStream);
        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException("Properties file not found: " + fullFile);
        } catch (IOException e) {
            throw new IllegalArgumentException("Properties file can not be loading: " + fullFile);
        } finally {
            try {
                if (inputStream != null)
                    inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return properties;
    }
}
