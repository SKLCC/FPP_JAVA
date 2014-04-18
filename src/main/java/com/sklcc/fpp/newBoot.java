package com.sklcc.fpp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;

import com.sklcc.fpp.comps.messages.Message;
import com.sklcc.fpp.nets.nodes.NodeConnector;
import com.sklcc.fpp.nets.pcs.PCConnector;
import com.sklcc.fpp.nets.phps.PHPConnector;
import com.sklcc.fpp.utils.sqls.MySQLPool;

public class newBoot {

    public static void main(String[] args)  {
        MySQLPool.getInstance();
        try {
            MySQLPool.getInstance().getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        DefaultContext defaultContext = new DefaultContext();
        defaultContext.init();
        NodeConnector nodeConnector = new NodeConnector();
        nodeConnector.init();
        nodeConnector.setContext(defaultContext);
        nodeConnector.start();

        PCConnector pcConnector = new PCConnector();
        pcConnector.init();
        pcConnector.setContext(defaultContext);
        pcConnector.start();
        
        /*PHPConnector phpConnector = new PHPConnector();
        phpConnector.init();
        phpConnector.setContext(defaultContext);
        phpConnector.start();*/
        
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                System.in));
        while (true) {
            int i;
            try {
                i = Integer.valueOf(reader.readLine());
                switch (i) {
                case 1:
                    PHPConnector phpConnector = new PHPConnector();
                    phpConnector.init();
                    phpConnector.setContext(defaultContext);
                    Message message = new Message(phpConnector);
                    message.setTargetID(NodeConnector.class.getSimpleName());
                    message.setMessageStr("shit001+#104601465*$");
                    phpConnector.sendMessage(message);
                    break;

                default:
                    break;
                }
            } catch (NumberFormatException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

           
        }
    }
}
