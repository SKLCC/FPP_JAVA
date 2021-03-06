package com.sklcc.fpp.nets.nettynodes;

import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.sklcc.fpp.nets.nodes.MysqlManger;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

/**
 * @author kaiyao
 * 
 */
public class NodeServerHandler extends ChannelInboundHandlerAdapter{
	
	private static Logger logger = LogManager.getLogger(NodeServerHandler.class
			.getSimpleName());
	
	private NodeConnector nodeConnector = null;
	private HashMap<String, Long>alarms = null;
	private HashMap<String, ChannelHandlerContext> areas = null;
	
	public NodeServerHandler(NodeConnector nodeConnector){
		this.nodeConnector = nodeConnector;
		areas = new HashMap<String, ChannelHandlerContext>();
	}
	
	public HashMap<String, ChannelHandlerContext> getareas(){
		return this.areas;
	}
	
	public void channelRead(ChannelHandlerContext ctx, Object msg){
		ByteBuf in = (ByteBuf)msg;
		String data = "";
		int i = 0;
		byte tmp;
		char[] arrayChar = new char[100];
		long currentTime = -1;
		try{
			while(in.isReadable()){
				tmp = in.readByte();
				if(tmp != 42){
					arrayChar[i] = (char)tmp;
					i++;
				}else{
					arrayChar[i] = '*';
					data = String.valueOf(arrayChar);
					logger.info("This is " + data);
					if(!data.startsWith("#")){
						logger.error("Error!");
						arrayChar = new char[100];
						i = 0;
						continue;
					}
					logger.info("Receive data: " + data);
					int protrolType;
					protrolType = Integer.valueOf(data.substring(2,4));
					if(protrolType == 89){
						MysqlManger.writeRecmsg2DB(data);
                        System.out.println("OK");
                        i = 0;
					}else if(protrolType == 88){
						logger.debug("箱子参数信息: "+ data);
                        MysqlManger.writeParam2DB(data);  //把参数信息写入数据库
                        MysqlManger.writeRecmsg2DB(data); 
                        //把ID和消息编号写入数据库,判断是是否成功
                        i = 0;
					}else{
						NodeException exception = new NodeException(data);
						if(exception.judgeNodeInfor() == true){
							String address = ctx.channel().remoteAddress().toString();
							logger.info(address + " : " + data);
							String ID = null;
							try{
								int length = Integer.parseInt(data.substring(4, 5));
								ID = data.substring(5, 5 + length);
								logger.info("箱子ID: " + ID);
								if(!areas.containsKey(ID)){
									areas.put(ID, ctx);
									ReceiveNodeMsg instance = new ReceiveNodeMsg(nodeConnector, alarms, ctx);
									instance.dealData(data, currentTime);
								}else{
									//logger.error("The same ID, so the orign socket is closed!");
									//areas.get(ID).close();
									areas.remove(ID);
								}
							}catch(Exception e){
								logger.debug("The received message is incorrected");
								logger.debug("Number error" + e.getMessage());
								logger.debug("The channel is closed");
								ctx.close();
							}
						}
					}
				}
				if(i > 80){
					logger.debug("The node is blocked!");
					logger.debug("The channel is closed");
					ctx.close();
				}
			}
		}catch(Exception e){
			logger.debug("Data formate error: " + e.getMessage());
		}finally{
			ReferenceCountUtil.release(msg);
		}
	}
	
	public void channelActive(ChannelHandlerContext ctx){
		logger.info("Node New Comer: " + ctx.channel().remoteAddress());
		alarms = new HashMap<String, Long>();
	}
	
	public void channelInactive(ChannelHandlerContext ctx){
		logger.info("Node: " + ctx.channel().remoteAddress() + "is offline");
	}
}
