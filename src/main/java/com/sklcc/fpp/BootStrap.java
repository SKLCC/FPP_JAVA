package com.sklcc.fpp;

import java.io.IOException;
import java.sql.SQLException;

import com.sklcc.fpp.nets.nodes.NodeConnector;
import com.sklcc.fpp.nets.pcs.PCConnector;
import com.sklcc.fpp.nets.phps.PHPConnector;
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
        
//        BufferedReader reader = new BufferedReader(new InputStreamReader(
//                System.in));
//        while (true) {
//            int i;
//            
//            try {
//                i = Integer.valueOf(reader.readLine());
//                switch (i) {
//                case 1:
//                    
//                    Message message = new Message(phpConnector);
//                    message.setTargetID(NodeConnector.class.getSimpleName());
//                    String setData = "#104407szdx00328";
//                    String crcStr = GenerateCrc.geneCRC(setData.substring(1));
//                    String sendData = "szdx001+"+setData+crcStr+"*$";
//                    System.out.println("the settingData is "+sendData);
//                    message.setMessageStr(sendData);
//                    phpConnector.sendMessage(message);
//                    break;
//                case 2:
//                    Message message2 = new Message(phpConnector);
//                    message2.setTargetID(NodeConnector.class.getSimpleName());
//                    String setString = "#10410310029";
//                    String crc = GenerateCrc.geneCRC(setString.substring(1));
//                    String sendData1 = "szdx001+"+setString+crc+"*$";
//                    message2.setMessageStr(sendData1);
//                    phpConnector.sendMessage(message2);
//                    break;
//                case 3:
//                    Message message3 = new Message(phpConnector);
//                    message3.setTargetID(NodeConnector.class.getSimpleName());
//                    String set = "#1047081:09:00,56";
//                    String crc1 = GenerateCrc.geneCRC(set.substring(1));
//                    System.out.println(crc1);
//                    String send = "szdx001+"+set+crc1+"*$";
//                    System.err.println(send);
//                    message3.setMessageStr(send);
//                    phpConnector.sendMessage(message3);
//                    break;
//                default:
//                    break;
//                }
//            } catch (NumberFormatException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            } catch (IOException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }
//
//           
//        }
    }
}