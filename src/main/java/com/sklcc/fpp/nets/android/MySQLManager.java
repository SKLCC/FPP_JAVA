package com.sklcc.fpp.nets.android;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import com.alibaba.druid.pool.DruidPooledConnection;
import com.sklcc.fpp.utils.sqls.MySQLPool;

public class MySQLManager {
    private  MySQLPool mySQLPool = MySQLPool.getInstance();
    private static Logger logger = LogManager.getLogger(MySQLManager.class
            .getSimpleName());
    private static final int DELTA_UPDATE_TIME = 1000 * 60;     //间隔时间为一分钟
    private static MySQLManager mySQLManager = null;
    private  LinkedList<String> accessUsers = new LinkedList<String>();
    DruidPooledConnection connection = null;
    
    private MySQLManager() throws SQLException {
        connection = mySQLPool.getConnection();
    }
    
    public static MySQLManager getInstance() throws SQLException {
        if(mySQLManager == null) {
            mySQLManager = new MySQLManager();
        }
        return mySQLManager;
    }
  //-------------------------------------------------------------------用来控制更新计时器的控件
    private boolean isTask = false;
    private Timer updateTimer = new Timer();
    private class updateClientsTask extends TimerTask {        
        @Override
        public void run() {
            String sqlOrder = "select * from fpp_client";
            LinkedList<String> tmpList = new LinkedList<String>();
            try {
                PreparedStatement statement = connection.prepareStatement(sqlOrder);
                ResultSet rs = statement.executeQuery(sqlOrder);
                while (rs.next()) {
                    if(rs.getString("type").equals("1")){
                        tmpList.add(rs.getString("name"));
                        logger.debug("新进入："+rs.getString("name"));
                    }                         
                }                                            
                statement.close();
                statement = null;
                accessUsers.clear();        
                logger.debug("检查数据库");
                for (String string : tmpList) {
                    logger.debug(string);
                }
                accessUsers.addAll(tmpList);
                tmpList.clear();
                rs.close();
                rs = null;
                System.gc();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    };
                 
    /**
     * @return 返回是否成功开始任务
     */
    public  boolean startUpdate(){
        try {
            if(isTask) return false;
            else {
                updateTimer.schedule(new updateClientsTask(), 0, DELTA_UPDATE_TIME);
                isTask = true;
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * @return 返回是否成功停止任务
     */
    public boolean stopUpdate(){
        try {
            if (isTask) {
                updateTimer.cancel();
                isTask = false;
                return true;
            }else return false;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * @return 一个包含了所有有权限的手机的链表
     */
    public LinkedList<String> getUserList() {
        return new LinkedList<String>(accessUsers);
    }
}
