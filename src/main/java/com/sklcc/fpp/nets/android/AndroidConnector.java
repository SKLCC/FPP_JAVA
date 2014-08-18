package com.sklcc.fpp.nets.android;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.sklcc.fpp.comps.AbstractConnector;
import com.sklcc.fpp.comps.messages.Message;
import com.sklcc.fpp.nets.settings.Settings;
import com.sklcc.fpp.utils.threads.ThreadPoolExecutor;
import com.sklcc.fpp.utils.threads.ThreadsPool;

public class AndroidConnector extends AbstractConnector{
    private static Logger logger = LogManager.getLogger(AndroidConnector.class
            .getSimpleName());
    private ThreadPoolExecutor threadsPool = null;
    private static ServerSocket serverSocket;
    private MySQLManager mySQLManager = null;
    
    public void init() {
        threadsPool = ThreadsPool.getInstance();
        logger.info("INIT!");
    }
    
    public void start() {
        Thread androidconnectorThread = new Thread(new Runnable() {
            public void run() {
                try {
                    mySQLManager = MySQLManager.getInstance();
                    mySQLManager.startUpdate();
                    serverSocket = new ServerSocket(Settings.androidPort);
                    while (true) {
                        try {
                            Socket client = serverSocket.accept();
                            logger.info("android new comer:"
                                    + client.getInetAddress());
                            threadsPool.execute(new AndroidClientRunnable(
                                    client));
                        } catch (Exception e) {

                        }
                    }
                } catch (Exception e) {
                    logger.debug("start error: " + e.getMessage());
                }
            }
        });
        androidconnectorThread.start();
        logger.info("START!");
    }

    public void shutdown() {
        try {
            serverSocket.close();
        } catch (IOException e) {
            logger.debug(e.getMessage());
        }
        logger.info("SHUIDOWN");
    }

    public void receiveMessage(Message message) {
        
    }

}
