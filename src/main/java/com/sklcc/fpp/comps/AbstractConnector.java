package com.sklcc.fpp.comps;

import com.sklcc.fpp.comps.messages.Message;


public abstract class AbstractConnector implements Connector {
    
    private Conntext conntext;
    
    public void setContext(Conntext conntext) {
        this.conntext = conntext;
        conntext.registerConnector(this);
    }
    
    public boolean sendMessage(Message message) { 
        return conntext.handleMessage(message);                   //处理数据
    }
    
    /**
     * @return ID String
     */
    public String getID() {
        return this.getClass().getSimpleName();
    }
}
