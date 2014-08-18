package com.sklcc.fpp.nets.android;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AndroidClientRunnable implements Runnable {
    private static Logger logger = LogManager
            .getLogger(AndroidClientRunnable.class.getSimpleName());
    private Socket clienSocket; // Android手机客户端的socket
    // private DataInputStream dataInputStream; //读入流
    // private DataOutputStream dataOutputStream; //输出流
    private BufferedReader reader;
    private BufferedWriter writer;
    private MySQLManager mySQLManager;
    private static String WEB_URL = Settings.WEB_URL; // 需要发送回去的网址


    public AndroidClientRunnable(Socket _clientSocket) {
        clienSocket = _clientSocket;
        try {
            reader = new BufferedReader(new InputStreamReader(
                    _clientSocket.getInputStream()));
            writer = new BufferedWriter(new OutputStreamWriter(
                    _clientSocket.getOutputStream()));
            mySQLManager = MySQLManager.getInstance();
        } catch (Exception e) {
            logger.debug("Runnable error:" + e.getMessage());
        }

    }

    public void run() {
        try {
            String Password;
            //Password = dataInputStream.readUTF();
            Password = reader.readLine();
            System.out.println(Password);
            for (String accessUser : mySQLManager.getUserList()) {
                if (Password.equals(accessUser)) {
                    System.out.println("OK! Access Confirmed");
                    //dataOutputStream.writeUTF(WEB_URL);
                    writer.write(WEB_URL);
                    writer.flush();
                    clienSocket.close();
                    return;
                }
            }
            logger.debug("NO! No Access!");
            //dataOutputStream.writeUTF("Fuck you!!!!");
            writer.write("Fuck you!!!!");
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
