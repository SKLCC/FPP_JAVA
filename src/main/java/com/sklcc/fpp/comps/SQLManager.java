package com.sklcc.fpp.comps;

import java.sql.SQLException;

import com.sklcc.fpp.comps.messages.Message;

public interface SQLManager extends Component{
    
    public void write2DB(Message message) throws SQLException;
    
}
