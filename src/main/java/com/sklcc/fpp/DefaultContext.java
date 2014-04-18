package com.sklcc.fpp;

import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.sklcc.fpp.comps.AbstractConntext;
import com.sklcc.fpp.comps.Connector;
import com.sklcc.fpp.comps.SQLManager;
import com.sklcc.fpp.comps.messages.Message;

public class DefaultContext extends AbstractConntext {

    private static Logger               logger = LogManager.getLogger(DefaultContext.class.getName()); ;
    private HashMap<String, Connector>  connectors;
    private HashMap<String, SQLManager> sqlmanages;

    public void init() {
        connectors = new HashMap<String, Connector>();
        sqlmanages = new HashMap<String, SQLManager>();
    }

    public void start() {
        //
    }

    public void shutdown() {
        connectors.clear();
        connectors = null;
        sqlmanages.clear();
        sqlmanages = null;
    }

    public boolean handleMessage(Message message) throws NullPointerException {
        logger.info("receive : " + message);
        
        if (!connectors.containsKey(message.getTargetID())) {
            logger.warn("NO SUCH CONNECTOR : " + message.getTargetID());
            return false;
        }
        
        Connector connector = connectors.get(message.getTargetID());
        logger.info("send message");
        connector.receiveMessage(message);
        
        return true;
    }

    /**
     * @param Connector
     *            Register a connector
     */
    public void registerConnector(Connector connector) {
        if (connector == null) { throw new NullPointerException("connector is null"); }
        String ID = connector.getID();
        if (connectors.containsKey(ID)) {
            connectors.remove(ID);
        }
        connectors.put(ID, (Connector) connector);
    }

    /**
     * @param SQLManager
     *            Register a SQLManager
     */
    public void registerSQLManager(SQLManager sqlmanager) {
        if (sqlmanager == null) { throw new NullPointerException("connector is null"); }
        String ID = sqlmanager.getID();
        if (sqlmanages.containsKey(ID)) {
            sqlmanages.remove(ID);
        }
        sqlmanages.put(ID, (SQLManager) sqlmanager);
    }

}
