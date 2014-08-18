package com.sklcc.fpp.nets.nodes;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.sklcc.fpp.comps.AbstractConnector;
import com.sklcc.fpp.comps.messages.Message;
import com.sklcc.fpp.nets.phps.PHPConnector;
import com.sklcc.fpp.nets.settings.Settings;
import com.sklcc.fpp.utils.crc16.GenerateCrc;
import com.sklcc.fpp.utils.threads.ThreadPoolExecutor;
import com.sklcc.fpp.utils.threads.ThreadsPool;

public class NodeConnector extends AbstractConnector {
    private static Logger logger = LogManager.getLogger(NodeConnector.class
            .getSimpleName());

    private ServerSocket serverSocket = null;
    private HashMap<String, NodeClientRunnable> areas = null;
    private ThreadPoolExecutor threadsPool = null;

    /**
     * receive the message from the PHPConnector
     * 
     * @return
     */
    public void receiveMessage(Message message) {
        String phpconnector = PHPConnector.class.getSimpleName();
        if (!message.getSourceID().equals(phpconnector)) {
            logger.info("message from a wrong connector ,refuse to send: "
                    + message.getSourceID());
            return;
        }
        int count = 1;
        String content = message.getMessageStr();
        String receiveMessage[] = content.split("\\$");
        for (int i = 0; i < receiveMessage.length; i++) {
            logger.debug("receiveMessage[]: " + receiveMessage[i]);
            String data[] = receiveMessage[i].split("\\+");
            NodeClientRunnable nodeClientRunnable = findClient(data[0]);
            if(nodeClientRunnable == null) {
                logger.error("the node is offline,so you can't set");
                return;   //箱子离线的情况
            }
            int length = data[1].length();
            int idLengtgh = data[0].length();
            for (int j = 1; j < data.length; j++) {
                logger.debug("order: " + data[j]);
                while (count <= 3) {
                    try {
                        nodeClientRunnable.sendOrder(data[j]);
                        String messType = data[j].substring(length - 5,
                                length - 3);
                        // 箱子回复的信息，可能有差错
                        int setType = Integer.valueOf(data[j].substring(4, 5));
                        logger.debug(messType);
                        String orginData = "189" + idLengtgh + data[0]
                                + messType;
                        String crc = GenerateCrc.geneCRC(orginData); // crc校验
                        String compareData = "#" + orginData + crc + "*";
                        logger.debug("the orgin message is : " + compareData);

                        Thread.sleep(10 * 1000); // 等待箱子返回信息
                        if (MysqlManger.readSetFormDB(messType)) {
                            if (data[j].charAt(4) != '0') {
                                boolean check = MysqlManger.writeSetting2DB(
                                        data[j], data[0]);
                                if (check == true) {
                                    logger.debug(("setting data success"));
                                    if (setType == 4) {
                                        // 设置ID时,把原来的socket断掉
                                        nodeClientRunnable.close();
                                    }
                                } else {
                                    logger.debug(("setting data failed"));
                                }
                            } else if (data[j].charAt(4) == '0') {
                                logger.debug("write node param2DB success");
                            }
                            break; // 设置成功就跳出循环
                        } else {
                            logger.debug(count + ":  send order to node failed");
                            MysqlManger.writeTransState(data[0]);
                            count++;
                            if (count == 4) {
                                logger.debug("has sent three times,but always failed");
                            }
                            Thread.sleep(5 * 1000);// 5秒后再发指令,一共发三次
                        }

                    } catch (Exception e) {
                        logger.info("node offline: "
                                + nodeClientRunnable.getNodeId());
                        nodeClientRunnable.close();
                        areas.remove(data[0]);
                        logger.info("node removed: "
                                + nodeClientRunnable.getNodeId());
                        logger.error(e.getMessage());
                    }
                }

            }
        }

    }

    public void init() {
        areas = new HashMap<String, NodeClientRunnable>();
        threadsPool = ThreadsPool.getInstance();
        logger.info("INIT!");
    }

    public void start() {
        Thread nodeconnecorThread = new Thread(new mainRunnable(this) {
        });
        nodeconnecorThread.start();
        logger.info("START!");
    }

    public void shutdown() {
        Set<String> nodeids = areas.keySet();
        for (String nodeid : nodeids) {
            areas.get(nodeid).close();
        }
        areas.clear();
        try {
            serverSocket.close();
        } catch (IOException e) {
            logger.debug(e.getMessage());
        }
        logger.info("SHUTDOWN");
    }

    /**
     * find the thread by ID
     * 
     * @param ID
     * @return the NodeClientRunnable Thread
     */
    public NodeClientRunnable findClient(String ID) {
        Set<String> nodeids = areas.keySet();
        for (String nodeid : nodeids) {
            if (nodeid.equals(ID)) {
                return areas.get(nodeid);
            }
        }
        return null;
    }

    /**
     * 
     * @param client
     * @param nodeConnector
     * @param alarms
     *            store the alarms to send to the PC after some time
     * @return nodeID
     */
    private String parseNodeID(Socket client, NodeConnector nodeConnector,
            HashMap<String, Long> alarms) {
        logger.info("parse the info : " + client.getInetAddress());
        String data = null;
        int i = 0;
        byte[] tmp = new byte[1];
        char[] arrayChar = new char[100];
        InputStream reader = null;
        long currentTime = -1;
        try {
            reader = client.getInputStream();

            while ((reader.read(tmp)) != -1) {
                if (tmp[0] != 42) {
                    arrayChar[i] = (char) tmp[0];
                    i++;
                } else {
                    arrayChar[i] = '*';
                    data = String.valueOf(arrayChar);
                    logger.debug("this is " + data);
                    if (!data.startsWith("#")) {
                        System.out.println("error");
                        arrayChar = new char[100];
                        i = 0;
                        continue;
                    }

                    NodeException myException = new NodeException(data);
                    if(myException.judgeNodeInfor() == false) {
                        logger.error("wrong information : " + data);
                        return null;
                    }else {
                        String address = String.valueOf(client.getInetAddress());
                        logger.debug(address + " : " + data);
                        String ID = null;
                        try {
                            int length = Integer.parseInt(data.substring(4, 5));
                            ID = data.substring(5, 5 + length);
                            logger.debug("箱子ID" + ":" + ID);
                        } catch (Exception e) {
                            logger.debug("the received message is incorrected");
                            logger.debug("number error"+e.getMessage());
                            return null;
                        }

                        ReceiveMessage instance = new ReceiveMessage(nodeConnector,
                                alarms, client);
                        instance.dealData(data, currentTime);
                        if (ID != null) {
                            return ID;
                        }
                    }    
                }

                if (i > 80) {
                    logger.debug("the node is blocked");
                    client.close();
                    break;
                }
            }

        } catch (IOException e) {
            logger.error("data formate error:"+e.getMessage());
        }
        return null;
    }

    /**
     * the accept thread to avoid blocking situation
     * 
     * @author jackson
     * 
     */
    public class acceptRunable implements Runnable {

        private Socket cSocket = null;
        private NodeConnector nodeConnector = null;
        private HashMap<String, Long> alarms = null;

        public acceptRunable(Socket cSocket, NodeConnector nodeConnector,
                HashMap<String, Long> alarms) {
            this.setcSocket(cSocket);
            this.nodeConnector = nodeConnector;
            this.alarms = alarms;
        }

        /**
         * @return the cSocket
         */
        public Socket getcSocket() {
            return cSocket;
        }

        /**
         * @param cSocket
         *            the cSocket to set
         */
        public void setcSocket(Socket cSocket) {
            this.cSocket = cSocket;
        }

        public void run() {
            try {
                String nodeid = parseNodeID(cSocket, nodeConnector, alarms);
                if (nodeid == null) {
                    cSocket.close();
                    logger.error("socket is closed");
                    return;
                }
                logger.info("new accept thread for : " + nodeid);
                NodeClientRunnable nodeClientRunnable = new NodeClientRunnable(
                        cSocket, nodeConnector, alarms);
                // 根据nodeid,设置socket的编号
                nodeClientRunnable.setNodeId(nodeid);
                if (!areas.containsKey(nodeid)) {
                    areas.put(nodeid, nodeClientRunnable);
                    threadsPool.execute(nodeClientRunnable);// start
                } else {
                    logger.error("the same ID,so the orgin socket is closed");
                    areas.get(nodeid).close();
                    areas.remove(nodeid);
                    this.run(); // 调用方法本身
                }
                logger.info("new handle thread for : "
                        + nodeClientRunnable.getNodeId());
            } catch (IOException e) {
                // ignore
                logger.error(e.getMessage());
            }

        }
    }

    class mainRunnable implements Runnable {
        private NodeConnector nodeConnector = null;

        public mainRunnable(NodeConnector nodeConnector) {
            this.nodeConnector = nodeConnector;
        }

        public void run() {
            try {
                serverSocket = new ServerSocket(Settings.nodePort);
                logger.info("I am in service");
                while (true) {
                    try {
                        Socket client = serverSocket.accept();
                        logger.info("node new comer:" + client.getInetAddress());
                        HashMap<String, Long> alarms = new HashMap<String, Long>();
                        threadsPool.execute(new acceptRunable(client,
                                nodeConnector, alarms));
                    } catch (Exception e) {
                        // avoid the single thread exception make the
                        // connector down
                        // ignore the exception
                        logger.debug("create serverSocket failed "
                                + e.getMessage());
                    }
                }
            } catch (IOException e) {
                logger.error(e.getMessage());
            }
        }
    }
}
