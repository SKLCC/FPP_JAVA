package com.sklcc.fpp.nets.nettynodes;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.sklcc.fpp.comps.messages.Message;
import com.sklcc.fpp.nets.nettynodes.HandleNodeMsg;
import com.sklcc.fpp.nets.nettynodes.NodeConnector;
import com.sklcc.fpp.nets.nettynodes.NodeDataType;
import com.sklcc.fpp.nets.pcs.PCConnector;

public class ReceiveNodeMsg {
	private static Logger logger = LogManager
			.getLogger(ReceiveNodeMsg.class.getSimpleName());
	private static NodeConnector nodeConnector;
	private static ChannelHandlerContext ctx = null;
	private HashMap<String, Long> alarms = null;

	public ReceiveNodeMsg(NodeConnector nodeConnector,
			HashMap<String, Long> alarms, ChannelHandlerContext ctx) {
		ReceiveNodeMsg.nodeConnector = nodeConnector;
		this.alarms = alarms;
		this.ctx = ctx;
	}

	// return client's IP address
	public String getInetAddress(Socket client) {
		return String.valueOf(client.getInetAddress());
	}

	// handle message from Node,use HandleNodemsg()
	public void dealData(String recdata, long currentTime) {
		NodeDataType type = defineType(recdata);
		String nodeAddress = ctx.channel().remoteAddress().toString();
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
				ctx.close(); // 关闭套接字
			} catch (Exception e) {
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
			byte[] responseByteArray = order.getBytes("UTF-8");
			ByteBuf out = ctx.alloc().buffer(responseByteArray.length);
			out.writeBytes(responseByteArray);
			ctx.writeAndFlush(out);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
