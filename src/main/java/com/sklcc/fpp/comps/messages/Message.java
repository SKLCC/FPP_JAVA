package com.sklcc.fpp.comps.messages;

import com.sklcc.fpp.comps.Component;

public class Message {

    private String      SourceID   = null;
    private String      TargetID   = null;
    private Object      Attach     = null;  //干什么用的啊
    private String      MessageStr = null;
    private MessageType type       = null;

    public Message(Component component) {
        this.SourceID = component.getID();
    }

    
    
    /**
     * @return the sourceID
     */
    public String getSourceID() {
        return SourceID;
    }



    /**
     * @return the type
     */
    public MessageType getType() {
        return type;
    }

    /**
     * @param type
     *            the type to set
     */
    public void setType(MessageType type) {
        this.type = type;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((Attach == null) ? 0 : Attach.hashCode());
        result = prime * result + ((MessageStr == null) ? 0 : MessageStr.hashCode());
        result = prime * result + ((SourceID == null) ? 0 : SourceID.hashCode());
        result = prime * result + ((TargetID == null) ? 0 : TargetID.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (!(obj instanceof Message)) return false;
        Message other = (Message) obj;
        if (Attach == null) {
            if (other.Attach != null) return false;
        } else if (!Attach.equals(other.Attach)) return false;
        if (MessageStr == null) {
            if (other.MessageStr != null) return false;
        } else if (!MessageStr.equals(other.MessageStr)) return false;
        if (SourceID == null) {
            if (other.SourceID != null) return false;
        } else if (!SourceID.equals(other.SourceID)) return false;
        if (TargetID == null) {
            if (other.TargetID != null) return false;
        } else if (!TargetID.equals(other.TargetID)) return false;
        if (type != other.type) return false;
        return true;
    }

    /**
     * @return the iD
     */
    public String getTargetID() {
        return TargetID;
    }

    /**
     * @param iD
     *            the iD to set
     */
    public void setTargetID(String iD) {
        TargetID = iD;
    }

    /**
     * @return the attach
     */
    public Object getAttach() {
        return Attach;
    }

    /**
     * @param attach
     *            the attach to set
     */
    public void setAttach(Object attach) {
        Attach = attach;
    }

    /**
     * @return the messageStr
     */
    public String getMessageStr() {
        return MessageStr;
    }

    /**
     * @param messageStr
     *            the messageStr to set
     */
    public void setMessageStr(String messageStr) {
        MessageStr = messageStr;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "Message [SourceID=" + SourceID + ", TargetID=" + TargetID + ", Attach=" + Attach + ", MessageStr="
               + MessageStr + ", type=" + type + "]";
    }

}
