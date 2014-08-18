package com.sklcc.fpp.nets.nettynodes;

import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.sklcc.fpp.nets.nettynodes.MysqlManger;
import com.sklcc.fpp.nets.nettynodes.NodeDataType;
import com.sklcc.fpp.utils.crc16.GenerateCrc;

/**
 * @author kaiyao
 * 
 */
public class HandleNodeMsg {
	private static Logger logger = LogManager.getLogger(HandleNodeMsg.class
			.getCanonicalName());
	private String nodeAddress = null;
	private HashMap<String, Long> alarms = null; 
	
	public HandleNodeMsg(String nodeAddress, HashMap<String, Long> alarms) { // how to set null on alarms
		this.nodeAddress = nodeAddress;
		this.alarms = alarms;
	}
	
	// handle ALIVE message
	public void handleAlimsg(String recdata){
		try{
			if(checkCRC(recdata)){
				MysqlManger.writeNodeData2DB(recdata, NodeDataType.ALIVE, nodeAddress);
			} else {
				logger.debug("the ALIVE message is wrong");
			}
		} catch (Exception e){
			logger.debug("the ALIVE message is wrong" + e.getMessage());
		}
	}
	
	// handle PROBLEM message
	public String handlePromsg(String recdata){
		String proMsg2node = null;
		try {
			int length = Integer.valueOf(recdata.substring(4, 5));
			String msgID = recdata.substring(7 + length, 9 + length); // 消息编号
			String crc = GenerateCrc.geneCRC("99" + msgID);
			if (checkCRC(recdata)) {
				proMsg2node = "#99" + msgID + crc + "*";
				MysqlManger.writeNodeData2DB(recdata, NodeDataType.PROBLEM, nodeAddress);
			} else {
				logger.debug("the PROBLEM message is wrong for crc");
			}
		} catch (Exception e) {
			logger.debug("the PROBLEM message is wrong " + e.getMessage());
		}
		return proMsg2node;
	}
	
	// handle ALARM message, and return messgae2node
	public String handleAlamsg2node(String recdata){
		String alaMsg2node = null;
		try{
			int length = Integer.valueOf(recdata.substring(4, 5)); // ID编号的长度
			String msgID = recdata.substring(6 + length,
					8 + length);
			String crc1 = GenerateCrc.geneCRC("99" + msgID);
			logger.debug("alMsg2node's crc is " + crc1);
			alaMsg2node = "#99" + msgID + crc1 + "*";
		} catch (Exception e){
			logger.debug("handleAlamsg2node error!" + e.getMessage());
		}
		return alaMsg2node;
	}
	
	
	//handle ALARM message, and return message2PC
	public String handleAlamsg2PC(String recdata, long currentTime){
		String alaMsg2PC = null;
		try {
			int length = Integer.valueOf(recdata.substring(4, 5)); // ID编号的长度
			String ID = recdata.substring(5, 5 + length); // ID编号
			String sid = recdata.substring(5 + length, 6 + length); // 设备编号
			System.out.println(alarms);
			if (currentTime == -1) {
				currentTime = System.currentTimeMillis();
				alarms.put(sid, currentTime);
				alaMsg2PC = "#" + ID + "," + sid + "*";
				MysqlManger.writeNodeData2DB(recdata, NodeDataType.ALARM, nodeAddress);
			} else if (alarms.containsKey(sid)) {
				currentTime = System.currentTimeMillis();
				System.out.println(currentTime - alarms.get(sid));
				if ((currentTime - alarms.get(sid)) > 6 * 60 * 1000) {
					alarms.remove(sid);
					alarms.put(sid, currentTime);
					alaMsg2PC = "#" + ID + "," + sid + "*";
					MysqlManger.writeNodeData2DB(recdata, NodeDataType.ALARM, nodeAddress);
				}
			} else {
				currentTime = System.currentTimeMillis();
				alarms.put(sid, currentTime);
				alaMsg2PC = "#" + ID + "," + sid + "*";
				MysqlManger.writeNodeData2DB(recdata, NodeDataType.ALARM, nodeAddress);
			}
			
		} catch (Exception e) {
			logger.debug("the ALARM message is wrong " + e.getMessage());
		}
		return alaMsg2PC;
	}

	// CRC check
	private static boolean checkCRC(String str) {
		return true;
	}
}
