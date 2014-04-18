package com.sklcc.fpp.comps;

import com.sklcc.fpp.comps.messages.Message;

public interface Conntext extends Component {

    public void registerConnector(Connector connector);    //注册连接者

    public void registerSQLManager(SQLManager sqlmanager);  //注册数据库管理

    public boolean handleMessage(Message message);  //处理消息
}
