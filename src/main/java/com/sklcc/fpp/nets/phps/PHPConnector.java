package com.sklcc.fpp.nets.phps;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.sklcc.fpp.newBoot;
import com.sklcc.fpp.comps.AbstractConnector;
import com.sklcc.fpp.comps.messages.Message;
import com.sklcc.fpp.nets.nodes.NodeConnector;
import com.sklcc.fpp.utils.crc16.GenerateCrc;
import com.sklcc.fpp.utils.threads.ThreadPoolExecutor;
import com.sklcc.fpp.utils.threads.ThreadsPool;

/**
 * 
 * @author john
 * 
 */
public class PHPConnector extends AbstractConnector {
    private static Logger      logger       = LogManager.getLogger(PHPConnector.class
                                                                                     .getSimpleName());
    private ServerSocket       serverSocket = null;
    private ThreadPoolExecutor threadsPool  = null;
    private Socket             client       = null;

    public void receiveMessage(Message message) {
        // TODO Auto-generated method stub

    }

    public void init() {
        // TODO Auto-generated method stub
        threadsPool = ThreadsPool.getInstance();
        logger.info("INIT!");
    }

    public void start() {
        // TODO Auto-generated method stub
        Thread phpconnectorThread = new Thread(new mainRunnable(this));
        phpconnectorThread.start();
        logger.info("START!");
    }

    public void shutdown() {
        // TODO Auto-generated method stub
        try {
            client.close();
            serverSocket.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    class mainRunnable implements Runnable {
        private PHPConnector phpConnector = null;

        public mainRunnable(PHPConnector phpConnector) {
            this.phpConnector = phpConnector;
        }

        public void run() {
            try {
                System.out.println("Start servering!");
                serverSocket = new ServerSocket(8777);
                while (true) {
                    try {
                        client = serverSocket.accept();
                        logger.info("new comer:" + client.getInetAddress());
                        threadsPool.execute(new acceptRunable(client,
                                                              phpConnector));
                    } catch (Exception e) {
                        // ----------------
                    }
                }
            } catch (IOException e) {
                logger.error(e.getMessage());
            }
        }

    }

    class acceptRunable implements Runnable {
        private Socket       cSocket      = null;
        private PHPConnector phpConnector = null;

        public acceptRunable(Socket cSocket, PHPConnector phpConnector) {
            this.setSocket(cSocket);
            this.setPHPConnector(phpConnector);
        }

        public Socket getSocket() {
            return cSocket;
        }

        public void setSocket(Socket cSocket) {
            this.cSocket = cSocket;
        }

        public void setPHPConnector(PHPConnector phpConnector) {
            this.phpConnector = phpConnector;
        }

        public void close() {
            logger.info("shut down the socket:PHP");
            try {
                cSocket.close();
            } catch (IOException e) {
                // ignore
            }
        }

        public void run() {
            System.out.println("yes");
            String string = null;
            StringBuilder strings = new StringBuilder();
            LinkedList<String> columnValues = new LinkedList<String>();
            Message message = new Message(phpConnector);
            message.setTargetID(NodeConnector.class.getSimpleName());
            try {
                BufferedReader reader = new BufferedReader(
                                                           new InputStreamReader(cSocket.getInputStream()));
                BufferedWriter writer = new BufferedWriter(
                                                           new OutputStreamWriter(cSocket.getOutputStream()));
                while ((string = reader.readLine()) != null) {
                    System.out.println(string);
                    String[] strs = string.split("\\$");
                    String temp = null;
                    for (String item : strs) {
                        item = item.substring(1, item.length());
                        String[] nums = item.split("\\+");
                        temp = nums[0];
                        String crcStr = GenerateCrc.geneCRC((nums[1].substring(
                                                                               0, nums[1].length() - 1)));
                        if (temp.length() < 1) {
                            columnValues = MySQLManager.getColumnValues("name");
                            // get all values of one column
                            while (!columnValues.isEmpty()) {
                                temp = columnValues.pop();
                                strings.append(temp + "+#" + nums[1]);
                                strings.insert(strings.length() - 1, crcStr);
                                strings.append("$");
                            }

                        } else {
                            strings.append(temp + "+#" + nums[1]);
                            strings.insert(strings.length() - 1, crcStr);
                            strings.append("$");
                        }

                    }
                    writer.write("receieve success!");
                    writer.flush();
                }
                logger.info("Receive settings from the web successfully!");

                // strings.setLength(strings.length() - 1);
                logger.debug("php receive: " + strings.toString());
                message.setMessageStr(strings.toString());
                phpConnector.sendMessage(message);
//                 System.out.println(strings.toString());
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            try {
                cSocket.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    }
}
