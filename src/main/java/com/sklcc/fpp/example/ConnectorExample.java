package com.sklcc.fpp.example;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.sklcc.fpp.DefaultContext;
import com.sklcc.fpp.comps.AbstractConnector;
import com.sklcc.fpp.comps.messages.Message;
import com.sklcc.fpp.comps.messages.MessageType;

public class ConnectorExample extends AbstractConnector {

    private static Logger logger = LogManager.getLogger(ConnectorExample.class.getName());

    public void receiveMessage(Message message) {
        logger.info("receive message : " + message);
        logger.info("from : " + message.getSourceID());
    }

    public void init() {
        
    }

    public void start() {

    }

    public void shutdown() {

    }

    public static void main(String[] args) {
        logger.info("GO!");
        DefaultContext defaultContext = new DefaultContext();
        defaultContext.init();

        ConnectorExample connectorExample = new ConnectorExample();
        connectorExample.setContext(defaultContext);

        // build the message
        Message exampleMessage = new Message(connectorExample);
        exampleMessage.setAttach(null);
        exampleMessage.setMessageStr("shit");
        exampleMessage.setType(MessageType.ALARM);
        exampleMessage.setTargetID(ConnectorExample.class.getSimpleName());
        
        // send message
        connectorExample.sendMessage(exampleMessage);
    }

}
