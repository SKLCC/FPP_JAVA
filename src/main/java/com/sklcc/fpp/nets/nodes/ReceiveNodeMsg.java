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

/**
 * 
 * @author kaiyao
 * Receive message from Node
 * 
 */

public class ReceiveNodeMsg {
	private static Logger logger = LogManager
			.getLogger(NodeClientRunnable.class.getSimpleName());
	private static NodeConnector nodeConnector;
	private static Socket client = null;
	private HashMap<String, Long> alarms = null;

	public ReceiveNodeMsg(NodeConnector nodeConnector,
			HashMap<String, Long> alarms, Socket client) {
		ReceiveNodeMsg.nodeConnector = nodeConnector;
		this.alarms = alarms;
		ReceiveNodeMsg.client = client;
	}

	// return client's IP address
	public String getInetAddress(Socket client) {
		return String.valueOf(client.getInetAddress());
	}

	// handle message from Node,use HandleNodemsg()
	public void dealData(String recdata, long currentTime) {
		NodeDataType type = defineType(recdata);
		String nodeAddress = getInetAddress(client);
		HandleNodeMsg instance = new HandleNodeMsg(nodeAddress, alarms);
		switch (type) {
		case ALIVE:
			instance.handleAlimsg(recdata);
			break;
		case PROBLEM:
			String proMsg2node = instance.handlePromsg(recdata);
			ReceiveNodeMsg.sendOrder(proMsg2node); // need open socket
			logger.debug("the PROBLEM msg has sent to Box");
			break;
		case ALARM:
			try {
				String alaMsg2node = instance.handleAlamsg2node(recdata);
				ReceiveNodeMsg.sendOrder(alaMsg2node); // need open socket
				Message message = new Message(nodeConnector);
				message.setTargetID(PCConnector.class.getSimpleName());
				String alaMsg2PC = instance.handleAlamsg2PC(recdata, currentTime);
				message.setMessageStr(alaMsg2PC);
				logger.debug(message.getMessageStr());
				if (nodeConnector.sendMessage(message)) {
					logger.debug("send Message to PC success");
				} else {
					logger.debug("send Message to PC failed");
				}
			} catch (Exception e) {
				logger.debug("");
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

	// handle NodeDataType
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

	// return message to Node(via Socket)
	public static void sendOrder(String order) {
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
