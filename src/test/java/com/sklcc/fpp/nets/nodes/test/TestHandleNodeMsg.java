package com.sklcc.fpp.nets.nodes.test;

import java.util.HashMap;

import junit.framework.TestCase;

import com.sklcc.fpp.nets.nodes.HandleNodeMsg;
import com.sklcc.fpp.utils.crc16.GenerateCrc;

public class TestHandleNodeMsg extends TestCase{
	
	private String alimsg = null;
	private String promsg = null;
	private String alamsg = null;
	private long currentTime = -1;
	private HandleNodeMsg handleNodeMsg= null;
	
	protected void setUp() throws Exception{
		alimsg = "#1017tshm001  *";
		promsg = "#1027tshm002C101  *";
		alamsg = "#1037tshm003F01  *";
		currentTime = -1;
		handleNodeMsg = new HandleNodeMsg("127.0.0.1", new HashMap<String, Long>());
	}
	
	public void testhandleAlimsg(){
		handleNodeMsg.handleAlimsg(alimsg);
	}
	
	public void testhandlePromsg(){
		int length = Integer.valueOf(promsg.substring(4, 5));
		String msgID = promsg.substring(7 + length, 9 + length);
		String crc = GenerateCrc.geneCRC("99" + msgID);
		String proMsg2node = "#99" + msgID + crc + "*";
		String recmsg = handleNodeMsg.handlePromsg(promsg);
		assertEquals(proMsg2node, recmsg);
	}
	
	public void testhandleAlammsg2node(){
		int length = Integer.valueOf(alamsg.substring(4, 5));
		String msgID = alamsg.substring(6 + length, 8 + length);
		String crc = GenerateCrc.geneCRC("99" + msgID);
		String alaMsg2node = "#99" + msgID + crc + "*";
		String recmsg = handleNodeMsg.handleAlamsg2node(alamsg);
		assertEquals(alaMsg2node, recmsg);
	}
	
	
	public void testhandleAlamsg2PC(){
		int length = Integer.valueOf(alamsg.substring(4, 5));
		String ID = alamsg.substring(5, 5 + length);
		String sid = alamsg.substring(5 + length, 6 + length);
		String msg2PC = "#" + ID + "," + sid + "*";
		String recmsg = handleNodeMsg.handleAlamsg2PC(alamsg, currentTime);
		System.out.println(recmsg);
		assertEquals(msg2PC, recmsg);
	}
}
