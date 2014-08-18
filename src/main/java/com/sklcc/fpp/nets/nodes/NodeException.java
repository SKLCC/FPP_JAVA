package com.sklcc.fpp.nets.nodes;

public class NodeException {
    private String recData = null;

    public NodeException(String recData) {
        this.recData = recData;
    }

    public boolean judgeNodeInfor() {
        int length = recData.trim().length();
        // #+1位产品类型编号+02+1位ID长度+ID编号+1位设备编号+1位故障类型编号+2位消息编号+2位校验码*
        //1+1+2+1+9+1+1+2+2+1=21
        if (length > 21) {
            return false;
        }else {
            String type = recData.substring(2,4);
            if(type.equals("01")) {   //心跳
                String ID = recData.substring(5,length-3);
               if (ID.length() == Integer.valueOf(recData.substring(4,5))) {
                   return true;
               }
            }else if(type.equals("02")) { //问题
                String ID = recData.substring(5,length-7);
                if (ID.length() == Integer.valueOf(recData.substring(4,5))) {
                    return true;
                }
            }else if(type.equals("03")) {   //报警
                String ID = recData.substring(5,length-6);
                if (ID.length() == Integer.valueOf(recData.substring(4,5))) {
                    return true;
                }
            }
        }
        return false;
    }
}
