package com.sklcc.fpp;

import java.io.IOException;
import java.sql.SQLException;

import com.sklcc.fpp.nets.android.AndroidConnector;
import com.sklcc.fpp.nets.nodes.NodeConnector;
//import com.sklcc.fpp.nets.nettynodes.NodeConnector;
import com.sklcc.fpp.nets.pcs.PCConnector;
import com.sklcc.fpp.nets.phps.PHPConnector;
import com.sklcc.fpp.nets.settings.ReadSetting;
import com.sklcc.fpp.utils.sqls.MySQLPool;

public class BootStrap {

    public static void main(String[] args) throws NumberFormatException,
            IOException {
        MySQLPool.getInstance();
        try {
            MySQLPool.getInstance().getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        DefaultContext defaultContext = new DefaultContext();
        defaultContext.init();
        
        //初始化配置文件
        ReadSetting readSetting = new ReadSetting();
        readSetting.init();
        
        PCConnector pcConnector = new PCConnector();
        pcConnector.init();
        pcConnector.setContext(defaultContext);
        pcConnector.start();

        NodeConnector nodeConnector = new NodeConnector();
        nodeConnector.init();
        nodeConnector.setContext(defaultContext);
        nodeConnector.start();

        PHPConnector phpConnector = new PHPConnector();
        phpConnector.init();
        phpConnector.setContext(defaultContext);
        phpConnector.start();
        
        AndroidConnector androidConnector = new AndroidConnector();
        androidConnector.init();
        androidConnector.setContext(defaultContext);
        androidConnector.start();
    }
}