package com.sklcc.fpp.nets.nettynodes;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;

public class NodeServerInitializer extends ChannelInitializer<SocketChannel>{
	
	private static Logger logger = LogManager.getLogger(NodeServerInitializer.class
			.getSimpleName());
	
	private NodeConnector nodeConnector = null;
	
	private NodeServerHandler nodeServerHandler = null;
	
	NodeServerInitializer(NodeConnector nodeConnector){
		this.nodeConnector = nodeConnector;
	}
	
	public NodeServerHandler getHandler(){
		return nodeServerHandler;
	}
	
	public void initChannel(SocketChannel ch){
		try{
			ChannelPipeline p = ch.pipeline();
			nodeServerHandler = new NodeServerHandler(nodeConnector);
			p.addLast(nodeServerHandler);
		}catch(Exception e){
			logger.debug(e.getMessage());
		}
		
	}
}
