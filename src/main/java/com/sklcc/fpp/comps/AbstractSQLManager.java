package com.sklcc.fpp.comps;


public abstract class AbstractSQLManager implements SQLManager {

    /**
     * @return ID String
     */
    public String getID() {
        return this.getClass().getSimpleName();
    }
}
