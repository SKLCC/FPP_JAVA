package com.sklcc.fpp.comps;

import com.sklcc.fpp.comps.messages.Message;

public interface Connector extends Component{
    
    public boolean sendMessage(Message message);
    public void receiveMessage(Message message);
    public void setContext(Conntext conntext);
}
