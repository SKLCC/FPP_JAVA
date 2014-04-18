package com.sklcc.fpp.nets.pcs;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PCClientRunnable implements Runnable {

    private static Logger  logger  = LogManager.getLogger(PCClientRunnable.class.getSimpleName());

    private Socket         cSocket = null;
    private String         pcid    = null;
    private BufferedReader reader  = null;
    private BufferedWriter writer  = null;

    public PCClientRunnable(Socket clinet) throws IOException {
        this.setcSocket(clinet);
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
     * @throws IOException
     *             set failed
     */
    public void setcSocket(Socket cSocket) throws IOException {
        this.cSocket = cSocket;
        writer = new BufferedWriter(new OutputStreamWriter(cSocket.getOutputStream()));
        reader = new BufferedReader(new InputStreamReader(cSocket.getInputStream()));
    }

    /**
     * @return the pcid
     */
    public String getPcid() {
        return pcid;
    }

    /**
     * @param pcid
     *            the pcid to set
     */
    public void setPcid(String pcid) {
        this.pcid = pcid;
    }

    /**
     * shut the socket down!
     */
    public void close() {
        logger.info("shut down the socket:" + pcid);
        try {
            cSocket.close();
        } catch (Exception e) {
            // ignore
        }
    }

    /**
     * send the message to the pc client
     * 
     * @param order
     *            order
     * @throws IOException
     *             the client may be offline
     */
    public void sendOrder(String order) throws IOException {
        writer.write(order + "\n");
        writer.flush();
    }

    public void run() {
        String recdata = "";
        try {
            while (!(recdata = reader.readLine()).equals("null")) {
                logger.debug("receive message : " + recdata);
                PCDataType type = defineType(recdata);
                //recdata = reader.r
                if (type == null) {
                    continue;
                }
                logger.debug("type : " + type);
                switch (type) {
                    case GET:
                    case WRONGID:
                    case HANDLE: {
                        MySQLManager.writePCData2DB(parseData2ID(recdata), type);
                        break;
                    }
                    // -----------------------
                    case CREATE:// the last two is the same way
                    default: {
                        // ignore such cases
                        break;
                    }
                }
                recdata = "";
            }
        } catch (IOException e) {
            try {
                this.cSocket.close();
            } catch (IOException e1) {}
        }
        logger.info("shut down accept thread for : " + pcid);
    }

    private PCDataType defineType(String recdata) {
        try {
            Matcher matcher = Pattern.compile("#(.*),(.*)#").matcher(recdata);
            if (matcher.find()) {
                switch (Integer.valueOf(matcher.group(1))) {
                    case 1:
                        return PCDataType.CREATE;
                    case 2:
                        return PCDataType.GET;
                    case 3:
                        return PCDataType.HANDLE;
                    case 4:
                        return PCDataType.WRONGID;
                }
            }
        } catch (Exception e) {}
        return null;
    }

    // #2,id#确认报警信息已收到
    private String parseData2ID(String recdata) {
        try {
            Matcher matcher = Pattern.compile("#(.*),(.*)#").matcher(recdata);
            if (matcher.find()) { return matcher.group(2); }
        } catch (Exception e) {}
        return null;
    }

    /*
     * module test code
     */
    public static void main(String[] args) {
        PCClientRunnable dClientRunnable = null;
        try {
            dClientRunnable = new PCClientRunnable(new Socket("192.168.135.8", 9500));
        } catch (IOException e) {}
        System.out.println(dClientRunnable.defineType("#2,awda001#"));
        System.out.println(dClientRunnable.parseData2ID("#2,234234#"));

    }
}
