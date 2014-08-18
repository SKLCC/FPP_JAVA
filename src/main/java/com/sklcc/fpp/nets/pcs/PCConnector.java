package com.sklcc.fpp.nets.pcs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.sklcc.fpp.comps.AbstractConnector;
import com.sklcc.fpp.comps.messages.Message;
import com.sklcc.fpp.nets.nodes.NodeConnector;
import com.sklcc.fpp.nets.settings.Settings;
import com.sklcc.fpp.utils.threads.ThreadPoolExecutor;
import com.sklcc.fpp.utils.threads.ThreadsPool;

public class PCConnector extends AbstractConnector {

    private static Logger logger = LogManager.getLogger(PCConnector.class
            .getSimpleName());
    private ServerSocket serverSocket = null;
    private HashMap<String, LinkedList<PCClientRunnable>> areas = null;
    private ThreadPoolExecutor threadsPool = null;

    public void receiveMessage(Message message) {
        String nodeconnector = NodeConnector.class.getSimpleName();

        if (!message.getSourceID().equals(nodeconnector)) {
            logger.info("message from a wrong connector, refuse to send : "
                    + message.getSourceID());
            return;
        }

        String content = message.getMessageStr();// get message content
        int id = MySQLManager.writeAlarm2DB(content);
        if (id != -1) { // write to db failed
            logger.debug("writeAlarm2DB success");
            PCClientRunnable[] target = findClient(content); // find target
            if (target != null) {
                for (PCClientRunnable pcClientRunnable : target) {
                    try {
                        pcClientRunnable.sendOrder(message2Alarm(content, id));
                        // send the alarm to the client
                    } catch (IOException e) {
                        logger.info("pc client offline : "
                                + pcClientRunnable.getPcid());
                        // the client offline
                        pcClientRunnable.close();
                        areas.get(pcClientRunnable.getPcid().substring(0, 4))
                                .remove(pcClientRunnable);
                        logger.info("pc client removed : "
                                + pcClientRunnable.getPcid());
                        // remove from the hashmap
                    }
                }
            }else logger.warn("No such client");
        }
        logger.info("finish receive message");
    }

    public void init() {
        areas = new HashMap<String, LinkedList<PCClientRunnable>>();
        threadsPool = ThreadsPool.getInstance();
        logger.info("INIT!");
    }

    public void start() {
        PCsTimerTask pCsTimerTask = new PCsTimerTask();
        pCsTimerTask.start();
        // start the timertask

        Thread pcconnecorThread = new Thread(new Runnable() {
            public void run() {
                try {
                    serverSocket = new ServerSocket(Settings.pcPort);
                    while (true) {
                        try {
                            Socket client = serverSocket.accept();
                            logger.info("pc new comer:"
                                    + client.getInetAddress());
                            threadsPool.execute(new acceptRunable(client));
                        } catch (Exception e) {
                            // avoid the single thread exception make the
                            // connector down
                            // ignore the exception
                        }
                    }
                } catch (IOException e) {
                    logger.error(e.getMessage());
                }
            }
        });
        pcconnecorThread.start();
        logger.info("START!");
    }

    public void shutdown() {
        Set<String> pcids = areas.keySet();
        for (String pcid : pcids) {
            for (PCClientRunnable pcClientRunnable : areas.get(pcid)) {
                pcClientRunnable.close();
            }
            areas.get(pcid).clear();
        }
        areas.clear();
        try {
            serverSocket.close();
        } catch (IOException e) {
            // ignore
        }
        logger.info("SHUTDOWN!");
    }

    // find the sockets
    private PCClientRunnable[] findClient(String alarm) {
        Matcher matcher = Pattern.compile("#(.*),(.*)\\*").matcher(alarm);
        if (matcher.find()) {
            String areaid = matcher.group(1).substring(0, 4);
            if (!areas.containsKey(areaid.toLowerCase())) {
                return null;
            }

            PCClientRunnable[] clientRunnables = new PCClientRunnable[1];
            return areas.get(areaid).toArray(clientRunnables);
        }
        return null;
    }

    // find the areaid from the socket
    private String parsePCID(Socket client) throws IOException {
        logger.info("parse the info : " + client.getInetAddress());
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                client.getInputStream()));
        String data = reader.readLine();

        logger.debug(client.getInetAddress() + " : " + data);
        Matcher matcher = Pattern.compile("#([1-4]),(.*)#").matcher(data);
        if (matcher.find()) {
            if (matcher.group(1).equals("1")) {// it means the rec data is to
                                               // create the socket
                return matcher.group(2);
            }
        }
        return null;
    }

    // #xxxx123,1* to #xxxx123,1,id#
    private String message2Alarm(String message, int id) {
        Matcher matcher = Pattern.compile("#(.*),(.*)\\*").matcher(message);
        if (matcher.find()) {
            StringBuilder builder = new StringBuilder();
            builder.append('#');
            builder.append(matcher.group(1));
            builder.append(',');
            builder.append(matcher.group(2));
            builder.append(',');
            builder.append(id);
            builder.append('#');
            return builder.toString();
        }
        return null;
    }

    /**
     * the accept thread to avoid blocking situation
     * 
     * @author mike
     * 
     */
    class acceptRunable implements Runnable {

        private Socket cSocket = null;

        public acceptRunable(Socket cSocket) {
            this.setcSocket(cSocket);
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
                String pcid = parsePCID(cSocket);
                if (pcid == null) {
                    cSocket.close();
                    return;
                }
                logger.info("new accept thread for : " + pcid);
                String areaid = pcid.substring(0, 4);
                PCClientRunnable pcClientRunnable = new PCClientRunnable(
                        cSocket);
                pcClientRunnable.setPcid(pcid);

                if (MySQLManager.checkClientStatus2DB(pcClientRunnable)) {
                    logger.debug("data update success");
                    if (areas.containsKey(areaid.toLowerCase())) {
                        areas.get(areaid).add(pcClientRunnable);// add
                    } else {
                        LinkedList<PCClientRunnable> arealist = new LinkedList<PCClientRunnable>();
                        arealist.add(pcClientRunnable);
                        areas.put(areaid, arealist);
                    }
                    threadsPool.execute(pcClientRunnable);// start
                    logger.info("new handle thread for : "
                            + pcClientRunnable.getPcid());
                } else {
                    logger.info("shut down client : "
                            + cSocket.getInetAddress() + " : " + pcid);
                    pcClientRunnable.close();
                }
                // logger.info("shut down accept thread for : " + pcid);
            } catch (IOException e) {
                // ignore
            }

        }
    }

    class PCsTimerTask {

        private TimerTask timerTask = null;

        private Timer timer = new Timer();

        public void start() {
            timerTask = new TimerTask() {
                @Override
                public void run() {
                    HashMap<String, LinkedList<String>> targets = MySQLManager
                            .getUnsolvedProblem();
                    for (String target : targets.keySet()) {
                        //logger.info("target area: " + target);
                        if (areas.containsKey(target.toLowerCase())) {
                            for (PCClientRunnable pcClientRunnable : areas
                                    .get(target.toLowerCase())) {
                                for (String order : targets.get(target)) {
                                    try {
                                        pcClientRunnable.sendOrder(order);
                                        logger.info("send order: " + order + " to " + pcClientRunnable.getPcid());
                                    } catch (IOException e) {
                                        // send failed
                                        logger.warn(target + " : "
                                                + e.getMessage());
                                        areas.get(target).remove(
                                                pcClientRunnable);
                                        logger.info("pc client removed : "
                                                + pcClientRunnable.getPcid());
                                        // removed
                                    }
                                }
                            }
                        }
                    }
                    //logger.info("timertask finished.");
                }
            };
            timer.schedule(timerTask, 10000, 1000 * 6);
            // delay 10sec, and redo in 6s
        }
    }
}
