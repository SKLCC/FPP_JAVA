package com.sklcc.fpp.nets.nodes;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class NodeClientRunnable implements Runnable {
    private static Logger logger = LogManager
            .getLogger(NodeClientRunnable.class.getSimpleName());

    private NodeConnector nodeConnector = null;

    private Socket nodeSocket = null;
    private String nodeId = null;
    private InputStream reader = null;
    private BufferedWriter writer = null;
    private HashMap<String, Long> alarms = null;

    public NodeClientRunnable(Socket nodeClient, NodeConnector nodeConnector,
            HashMap<String, Long> alarms) {
        this.setnodeSocket(nodeClient);
        this.nodeConnector = nodeConnector;
        this.alarms = alarms;
    }

    public void setnodeSocket(Socket nodesSocket) {
        this.nodeSocket = nodesSocket;
        try {
            reader = nodesSocket.getInputStream();
            writer = new BufferedWriter(new OutputStreamWriter(
                    nodesSocket.getOutputStream()));
        } catch (IOException e) {
            logger.debug("setnodeSocket: " + e.getMessage());
        }

    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public void close() {
        logger.info("shut down the socket:" + nodeId);
        try {
            nodeSocket.close();
        } catch (Exception e) {
            logger.debug("the socket can't closed: " + e.getMessage());
        }
    }

    /**
     * send order to node
     * @param order
     * @throws IOException
     */
    public void sendOrder(String order) {
        try {
            writer.write(order + "\n");
            writer.flush();
        } catch (IOException e) {
            logger.debug("sendOrder failed: " + e.getMessage());
        }

    }

    /**
     * Revive the message from node;
     * 
     * @return string
     */
    long currentTime = -1;

    public void run() {
        byte tmp[] = new byte[1];
        char[] charArray = new char[100];
        String recdata = "";
        int i = 0;
        try {
            while ((reader.read(tmp)) != -1) {
                if (tmp[0] != 42) {
                    charArray[i] = (char) tmp[0];
                    i++;
                } else {
                    charArray[i] = '*';
                    recdata = String.valueOf(charArray);
                    if (!recdata.startsWith("#")) {
                        System.out.println("error failed");
                        charArray = new char[100];
                        i = 0;
                        continue;
                    }
                    
                    logger.debug("receive message :" + recdata);
                    int protrolType;
                    protrolType = Integer.valueOf(recdata.substring(2,4));
                    if(protrolType == 89) {    //设置信息的处理方法
                        MysqlManger.writeRecmsg2DB(recdata);
                        System.out.println("OK");
                        i = 0;
                    }else if(protrolType == 88 ) {
                        logger.debug("箱子参数信息: "+recdata);
                        MysqlManger.writeParam2DB(recdata);  //把参数信息写入数据库
                        MysqlManger.writeRecmsg2DB(recdata); 
                        //把ID和消息编号写入数据库,判断是是否成功
                        i = 0;
                    }
                    else {
//                    	ReceiveNodeMsg instance = new ReceiveNodeMsg(
//                                nodeConnector, alarms, nodeSocket);
                        ReceiveMessage instance = new ReceiveMessage(
                                nodeConnector, alarms, nodeSocket);
                    	NodeException myException = new NodeException(recdata);
                    	if (myException.judgeNodeInfor() == true) {
                            instance.dealData(recdata, currentTime);
                           
                        }else {
                            logger.error("wrong information : " + recdata);
                        }
                        i = 0;
                    }
                    
                    currentTime = 0;
                    charArray = new char[100];
                }
                
                if(i > 80) {
                    logger.debug("the node is blocked");
                    nodeSocket.close();
                    break;
                }
               
            }
        } catch (Exception e) {
            logger.debug("data formate error:" + e.getMessage());
        }
        logger.info("thread out");
    }

}
