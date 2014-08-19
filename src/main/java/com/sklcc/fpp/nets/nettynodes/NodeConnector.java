package com.sklcc.fpp.nets.nettynodes;

import java.util.HashMap;
import java.util.Set;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.sklcc.fpp.comps.AbstractConnector;
import com.sklcc.fpp.comps.messages.Message;
import com.sklcc.fpp.nets.nettynodes.MysqlManger;
import com.sklcc.fpp.nets.phps.PHPConnector;
import com.sklcc.fpp.nets.settings.Settings;
import com.sklcc.fpp.utils.crc16.GenerateCrc;

/**
 * @author kaiyao
 * 
 */

public class NodeConnector extends AbstractConnector {

	private static Logger logger = LogManager.getLogger(NodeConnector.class
			.getSimpleName());

	private HashMap<String, ChannelHandlerContext> areas = null;
	private EventLoopGroup bossGroup = null;
	private EventLoopGroup workerGroup = null;
	private ServerBootstrap b = null;
	private NodeServerInitializer nodeServerInitializer = null;

	/**
	 * receive message from PHPConnector
	 */
 	public void receiveMessage(Message message) {
		String phpconnector = PHPConnector.class.getSimpleName();
		if (!message.getSourceID().equals(phpconnector)) {
			logger.info("message from a wrong connector ,refuse to send: "
					+ message.getSourceID());
			return;
		}
		int count = 1;
		String content = message.getMessageStr();
		String receiveMessage[] = content.split("\\$");
		for (int i = 0; i < receiveMessage.length; i++) {
			logger.debug("receiveMessage[]: " + receiveMessage[i]);
			String data[] = receiveMessage[i].split("\\+");
			ChannelHandlerContext ctx = findClient(data[0]);
			if (ctx == null) {
				logger.error("the node is offline, so you can't set");
				return; // 箱子离线的情况
			}
			int length = data[1].length();
			int idLengtgh = data[0].length();
			for (int j = 1; j < data.length; j++) {
				logger.debug("order: " + data[j]);
				while (count <= 3) {
					try {
						byte[] responseByteArray = data[j].getBytes();
						ByteBuf out = ctx.alloc().buffer(responseByteArray.length);
						out.writeBytes(responseByteArray);
						ctx.writeAndFlush(out);
						String messType = data[j].substring(length - 5,
								length - 3);
						// 箱子回复的信息，可能有差错
						int setType = Integer.valueOf(data[j].substring(4, 5));
						logger.debug(messType);
						String orginData = "189" + idLengtgh + data[0]
								+ messType;
						String crc = GenerateCrc.geneCRC(orginData); // crc校验
						String compareData = "#" + orginData + crc + "*";
						logger.debug("the orgin message is : " + compareData);

						Thread.sleep(10 * 1000); // 等待箱子返回信息
						if (MysqlManger.readSetFormDB(messType)) {
							if (data[j].charAt(4) != '0') {
								boolean check = MysqlManger.writeSetting2DB(
										data[j], data[0]);
								if (check == true) {
									logger.debug(("setting data success"));
									if (setType == 4) {
										// 设置ID时,把原来的socket断掉
										ctx.close();
									}
								} else {
									logger.debug(("setting data failed"));
								}
							} else if (data[j].charAt(4) == '0') {
								logger.debug("write node param2DB success");
							}
							break; // 设置成功就跳出循环
						} else {
							logger.debug(count + ":  send order to node failed");
							MysqlManger.writeTransState(data[0]);
							count++;
							if (count == 4) {
								logger.debug("has sent three times,but always failed");
							}
							Thread.sleep(5 * 1000);// 5秒后再发指令,一共发三次
						}

					} catch (Exception e) {
						ctx.close();
						areas.remove(data[0]);
						logger.error(e.getMessage());
					}
				}

			}
		}

	}

	public void init() {
		areas = new HashMap<String, ChannelHandlerContext>();
		initNodeServer();
		logger.info("NodeServer Init!");
	}

	public void start() {
		try{
			ChannelFuture f = this.b.bind(Settings.nodePort).sync();
			logger.info("NoderServer Start!");
			f.channel().closeFuture().sync();
		}catch(Exception e){
			logger.debug(e.getMessage());
		}finally{
			shutdown();
		}
	}

	public void shutdown() {
		Set<String> nodeids = areas.keySet();
		for(String nodeid: nodeids){
			areas.get(nodeid).close();
		}
		areas.clear();
		this.workerGroup.shutdownGracefully();
		this.bossGroup.shutdownGracefully();
		logger.info("NodeServer Shutdown!");
	}

	public void initNodeServer() {
		this.bossGroup = new NioEventLoopGroup();
		this.workerGroup = new NioEventLoopGroup();
		try {
			this.b = new ServerBootstrap();
			this.nodeServerInitializer = new NodeServerInitializer(this);
			this.b.group(this.bossGroup, this.workerGroup)
					.channel(NioServerSocketChannel.class)
					.childHandler(this.nodeServerInitializer);
		}catch(Exception e){
			logger.error("NodeServer Init Error: " + e.getMessage());
			shutdown();
		}
	}
	
	public ChannelHandlerContext findClient(String ID) {
		//每次findClient之前需要更新一下areas
		areas = this.nodeServerInitializer.getHandler().getareas();
		Set<String> nodeids = areas.keySet();
		for(String nodeid: nodeids){
			if(nodeid.equals(ID)){
				return areas.get(nodeid);
			}
		}
		return null;
	}
}
