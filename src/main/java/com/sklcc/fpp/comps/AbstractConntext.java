package com.sklcc.fpp.comps;

public abstract class AbstractConntext implements Conntext{

    /**
     * @return ID String
     */
    public String getID() {
        return this.getClass().getSimpleName();
    }
}
