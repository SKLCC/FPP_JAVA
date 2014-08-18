package com.sklcc.fpp.nets.nodes;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.sklcc.fpp.comps.messages.Message;
import com.sklcc.fpp.nets.pcs.PCConnector;
import com.sklcc.fpp.utils.crc16.GenerateCrc;

public class ReceiveMessage {
    private static Logger logger = LogManager
            .getLogger(NodeClientRunnable.class.getSimpleName());
    private static NodeConnector nodeConnector;
    private static Socket client = null;
    private HashMap<String, Long> alarms = null;

    public ReceiveMessage(NodeConnector nodeConnector,
            HashMap<String, Long> alams, Socket client) {
        ReceiveMessage.nodeConnector = nodeConnector;
        this.alarms = alams;
        ReceiveMessage.client = client;
    }

    public void dealData(String recdata, long currentTime) {
        NodeDataType type = defineType(recdata);
        String nodeAddres = String.valueOf(client.getInetAddress());
        switch (type) {
        case ALIVE:
            try {
                if (checkCRC(recdata)) {
                    MysqlManger.writeNodeData2DB(recdata, type, nodeAddres);
                } else {
                    logger.debug("the alive msg is wrong");
                }
            } catch (Exception e) {
                logger.debug("the alive message is wrong " + e.getMessage());
            }

            break;
        case PROBLEM:
            try {
                int length = Integer.valueOf(recdata.substring(4, 5));
                String messageType = recdata.substring(7 + length, 9 + length); // 消息编号
                String crc = GenerateCrc.geneCRC("99" + messageType);
                if (checkCRC(recdata)) {
                    ReceiveMessage.sendOrder("#99" + messageType + crc + "*");
                    logger.debug("the problem has sent to box");
                    MysqlManger.writeNodeData2DB(recdata, type, nodeAddres);
                } else {
                    logger.debug("the problem msg is wrong for crc");
                }
            } catch (Exception e) {
                logger.debug("the problem message is wrong " + e.getMessage());
            }

            break;
        case ALARM:
            try {
                Message message = new Message(nodeConnector);
                message.setTargetID(PCConnector.class.getSimpleName());
                int length1 = Integer.valueOf(recdata.substring(4, 5));
                // ID编号的长度
                String ID = recdata.substring(5, 5 + length1); // ID编号
                String sid = recdata.substring(5 + length1, 6 + length1);
                // 设备编号
                String messageType1 = recdata.substring(6 + length1,
                        8 + length1);
                System.out.println(alarms);
                String crc1 = GenerateCrc.geneCRC("99" + messageType1);
                logger.debug("crc  " + crc1);
                ReceiveMessage.sendOrder("#99" + messageType1 + crc1 + "*"); // 回复箱子
                if (currentTime == -1) {
                    currentTime = System.currentTimeMillis();
                    alarms.put(sid, currentTime);
                    message.setMessageStr("#" + ID + "," + sid + "*");
                    logger.debug(message.getMessageStr());
                    MysqlManger.writeNodeData2DB(recdata, type, nodeAddres);
                    if (nodeConnector.sendMessage(message)) {
                        logger.debug("send Message success");
                    } else {
                        logger.debug("send Message failed");
                    }
                } else if (alarms.containsKey(sid)) {
                    currentTime = System.currentTimeMillis();
                    System.out.println(currentTime - alarms.get(sid));
                    if ((currentTime - alarms.get(sid)) > 6 * 60 * 1000) {
                        alarms.remove(sid);
                        alarms.put(sid, currentTime);
                        message.setMessageStr("#" + ID + "," + sid + "*");
                        logger.debug(message.getMessageStr());
                        MysqlManger.writeNodeData2DB(recdata, type, nodeAddres);
                        if (nodeConnector.sendMessage(message)) {
                            logger.debug("send Message success");
                        } else {
                            logger.debug("send Message failed");
                        }
                    }
                } else {
                    currentTime = System.currentTimeMillis();
                    alarms.put(sid, currentTime);
                    message.setMessageStr("#" + ID + "," + sid + "*");
                    logger.debug(message.getMessageStr());
                    MysqlManger.writeNodeData2DB(recdata, type, nodeAddres);
                    if (nodeConnector.sendMessage(message)) {
                        logger.debug("send Message success");
                    } else {
                        logger.debug("send Message failed");
                    }
                }
            } catch (Exception e) {
                logger.debug("the alarm message is wrong "+e.getMessage());
            }

            break;
        default:
            try {
                client.close(); // 关闭套接字
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            logger.debug("Error !node is bad");
            break;
        }

    }

    private static NodeDataType defineType(String recdata) {
        int type = 0;
        type = Integer.parseInt(recdata.substring(3, 4));
        switch (type) {
        case 1:
            return NodeDataType.ALIVE;
        case 2:
            return NodeDataType.PROBLEM;
        case 3:
            return NodeDataType.ALARM;

        }
        return null;

    }

    private static boolean checkCRC(String str) {
        return true;
    }

    private static void sendOrder(String order) {
        try {
            BufferedWriter bufferedWriter = new BufferedWriter(
                    new OutputStreamWriter(client.getOutputStream()));
            bufferedWriter.write(order);
            bufferedWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
