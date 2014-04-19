package com.sklcc.fpp.nets.nodes;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.alibaba.druid.pool.DruidPooledConnection;
import com.sklcc.fpp.nets.pcs.MySQLManager;
import com.sklcc.fpp.utils.sqls.MySQLPool;

public class MysqlManger {
    private static MySQLPool mySQLPool = MySQLPool.getInstance();
    private static Logger logger = LogManager.getLogger(MySQLManager.class
            .getSimpleName());

    private MysqlManger() {
    }

    public static boolean writeNodeData2DB(String recdata, NodeDataType type,
            String address) {
        DruidPooledConnection connection = null;
        PreparedStatement preparedStatement = null; // 处理sql预编译对象

        try {
            connection = mySQLPool.getConnection(); // 连上数据库
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String now = sdf.format(new Date());
            switch (type) {
            case ALIVE: { //
                int length = Integer.parseInt(recdata.substring(4, 5));
                String name = recdata.substring(5, 5 + length); // ID编号
                System.out.println(name + "  " + now);
                preparedStatement = connection
                        .prepareStatement("update fpp_node "
                                + "set update_time = ? " + ",ip = ? "
                                + ",msg_type = ? " + "where name = ?");
                preparedStatement.setString(1, now);
                preparedStatement.setString(2, address.substring(1));
                preparedStatement.setInt(3, 1);
                preparedStatement.setString(4, name);
                if (preparedStatement.executeUpdate() != 0) {
                    logger.debug("update alive success");
                    return true;
                } else
                    logger.debug("update alive failed");
                return false;
            }
            case PROBLEM: {
                boolean state;
                state = writeProblem2DB(recdata);
                if (state == true) {
                    logger.debug(("insert to the fpp_problem succeed!"));
                    return true;
                } else {
                    logger.debug("insert to the fpp_problem failed");
                    return false;
                }
            }
            case ALARM: {
                boolean state;
                state = writeAlarm2DB(recdata);
                if (state == true) {
                    System.out.println("insert to the fpp_alarm succeed!");
                    return true;
                } else {
                    logger.debug("insert to the fpp_alarm failed");
                    return false;
                }
            }
            default:
                break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                connection.recycle();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static boolean writeProblem2DB(String recdata) {
        DruidPooledConnection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            int nid = 0;
            connection = mySQLPool.getConnection();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String now = sdf.format(new Date());
            int length = Integer.valueOf(recdata.substring(4, 5)); // ID编号的长度
            String temp = recdata.substring(5, 5 + length); // ID编号
            String sid = recdata.substring(5 + length, 6 + length); // 设备编号
            int problemType = Integer.valueOf(recdata.substring(6 + length,
                    7 + length));
            if (!sid.equals("Z")) {
                problemType += 10;
            }
            preparedStatement = connection
                    .prepareStatement("update fpp_node set update_time=?"
                            + ",msg_type = ?" + " where name = ? ");
            preparedStatement.setString(1, now);
            preparedStatement.setInt(2, 2);
            preparedStatement.setString(3, temp);
            if (preparedStatement.executeUpdate() != 0) {
                preparedStatement = connection
                        .prepareStatement("select * from fpp_node where name =?");
                preparedStatement.setString(1, temp);
                try {
                    ResultSet rs = preparedStatement.executeQuery();
                    while (rs.next()) {
                        nid = rs.getInt("nid");
                    }
                    rs.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                String sql_2 = "INSERT INTO fpp_problem(sid,type,stime,nid) "
                        + "values(?,?,?,?)";
                preparedStatement = connection.prepareStatement(sql_2);
                // System.out.println(sid + " " + problemType + " " + now);
                preparedStatement.setString(1, sid);
                preparedStatement.setInt(2, problemType);
                preparedStatement.setString(3, now);
                preparedStatement.setInt(4, nid);
                if (preparedStatement.executeUpdate() != 0) {
                    return true;
                } else
                    return false;
            } else {
                logger.debug("update error");
            }

        } catch (Exception e) {
            logger.error("DB ERROR!" + e.getMessage());
        } finally {
            try {
                connection.recycle();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static boolean writeAlarm2DB(String recdata) {
        DruidPooledConnection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            connection = mySQLPool.getConnection();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String now = sdf.format(new Date());
            int length = Integer.valueOf(recdata.substring(4, 5));// ID编号的长度
            String temp = recdata.substring(5, 5 + length); // ID编号
            preparedStatement = connection
                    .prepareStatement("update fpp_node set update_time=?"
                            + ",msg_type = ?" + " where name = ? ");
            preparedStatement.setString(1, now);
            preparedStatement.setInt(2, 3);
            preparedStatement.setString(3, temp);
            if (preparedStatement.executeUpdate() != 0) {
                logger.debug(("update alarm success"));
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                connection.recycle();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static boolean writeSetting2DB(String sendOrder, String ID) {
        char type = sendOrder.charAt(4);
        DruidPooledConnection connection = null;
        PreparedStatement preparedStatement = null;
        if (type == '4') { // 设置模块的ID
            int length = Integer.valueOf(sendOrder.substring(5, 7));
            String newID = sendOrder.substring(7, length + 7);
            try {
                connection = mySQLPool.getConnection();
                preparedStatement = connection
                        .prepareStatement("update fpp_node set name=?"
                                + " where name = ?");
                preparedStatement.setString(1, newID);
                preparedStatement.setString(2, ID);
                if (preparedStatement.executeUpdate() != 0) {
                    logger.debug("update ID success");
                    return true;
                } else {
                    logger.debug("update ID failed");
                    return false;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                try {
                    connection.recycle();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else if (type == '6') { // 设置自检次数
            int chieckTimes = Integer.valueOf(sendOrder.substring(7, 8));
            try {
                connection = mySQLPool.getConnection();
                preparedStatement = connection
                        .prepareStatement("update fpp_node set checked_times = ?"
                                + " where name = ? ");
                preparedStatement.setInt(1, chieckTimes);
                preparedStatement.setString(2, ID);
                if (preparedStatement.executeUpdate() != 0) {
                    logger.debug(("插入自检次数成功"));
                    return true;
                } else {
                    logger.debug("插入自检次数失败");
                    return false;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                try {
                    connection.recycle();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else if (type == '7') { // 设置自检时间
            int length = Integer.valueOf(sendOrder.substring(5, 7));
            String setTimes = sendOrder.substring(7, 6 + length);
            String setTime[] = setTimes.split(",");
            int times = setTime.length;
            String date[][] = new String[times][2];
            for (int i = 0; i < times; i++) {
                date[i] = setTime[i].split(":");
            }
            StringBuffer buffer = new StringBuffer();
            for (int i = 0; i < times - 1; i++) {
                date[i][1] += ":00,";
                buffer.append(date[i][1]);
            }
            buffer.append(date[times - 1][1] + ":00");
            try {
                connection = mySQLPool.getConnection();
                preparedStatement = connection
                        .prepareStatement("update fpp_node set check_clock = ?"
                                + " where name = ?");
                preparedStatement.setString(1, buffer.toString());
                preparedStatement.setString(2, ID);
                if (preparedStatement.executeUpdate() != 0) {
                    logger.debug("set check_clock success");
                    return true;
                } else {
                    logger.debug("set check_clock failed");
                    return false;
                }

            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                try {
                    connection.recycle();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else if (type == '1') { // 设置心跳间隔
            int length = Integer.valueOf(sendOrder.substring(5, 7));
            int hertBeat = Integer.valueOf(sendOrder.substring(7, 7 + length));
            try {
                connection = mySQLPool.getConnection();
                preparedStatement = connection
                        .prepareStatement("update fpp_node set heart_beat_interval = ?"
                                + " where name = ?");
                preparedStatement.setInt(1, hertBeat);
                preparedStatement.setString(2, ID);
                if (preparedStatement.executeUpdate() != 0) {
                    logger.debug("update heart success");
                    return true;
                } else {
                    logger.debug("update heart failed");
                    return false;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                try {
                    connection.recycle();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else if (type == '2') { // 设置IP与PORT
            int length = Integer.valueOf(sendOrder.substring(5, 7));
            String ipPort = sendOrder.substring(7, 6 + length);
            String string[] = ipPort.split(",");
            String ip = string[0];
            int port = Integer.valueOf(string[1]);
            try {
                connection = mySQLPool.getConnection();
                preparedStatement = connection
                        .prepareStatement("update fpp_node set server_ip = ?,server_port = ?"
                                + " where name = ?");
                preparedStatement.setString(1, ip);
                preparedStatement.setInt(2, port);
                preparedStatement.setString(3, ID);
                if (preparedStatement.executeUpdate() != 0) {
                    logger.debug("update ip and port success");
                    return true;
                } else {
                    logger.debug("update ip and port failed");
                    return false;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                try {
                    connection.recycle();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        } else if (type == '3') { // 报警开关设置
            int sensorSwitcn = Integer.valueOf(sendOrder.substring(7, 11), 16);
            String binarySwitch = Integer.toBinaryString(sensorSwitcn);
            int num = binarySwitch.length();
            String temp = "";
            if (num < 16) {
                for (int i = 0; i < 16 - num; i++) {
                    temp = temp + "0";
                }
            }
            binarySwitch = temp + binarySwitch;
            try {
                connection = mySQLPool.getConnection();
                preparedStatement = connection
                        .prepareStatement("update fpp_node set switch = ?"
                                + " where name = ?");
                preparedStatement.setString(1, binarySwitch);
                preparedStatement.setString(2, ID);
                if (preparedStatement.executeUpdate() != 0) {
                    logger.debug("update switch success");
                    return true;
                } else {
                    logger.debug("update switch failed");
                    return false;
                }
            } catch (SQLException e) {
                logger.debug(e.getMessage());
            } finally {
                try {
                    connection.recycle();
                } catch (Exception e) {
                    logger.debug(e.getMessage());
                }
            }

        }
        return false;
    }

    /**
     * 把箱子返回的自己的参数信息写入数据库
     * 
     * @param 箱子的参数返回信息
     */
    public static boolean writeParam2DB(String recData) {
        DruidPooledConnection connection = null;
        PreparedStatement preparedStatement = null;
        int id_length = Integer.valueOf(recData.substring(4, 5));
        String ID = recData.substring(5, 5 + id_length);
        String lastData = recData.substring(5 + id_length);
        String setting[] = lastData.split(",");
        int heartBeat = Integer.valueOf(setting[1]);
        int sensorSwitcn = Integer.valueOf(setting[3]);
        String binarySwitch = Integer.toBinaryString(sensorSwitcn);
        // 开关设置
        int num = binarySwitch.length();
        String temp = "";
        if (num < 16) {
            for (int i = 0; i < 16 - num; i++) {
                temp = temp + "0";
            }
        }
        binarySwitch = temp + binarySwitch; // 开关设置
        int checked_times = Integer.valueOf(setting[5]); // 自检次数
        String[] check_clock = new String[checked_times];
        String check_clocks = "";
        int init = 0;
        for (int i = 0; i < checked_times; i++) {
            check_clock[i] = setting[7].substring(init, init + 2);
            init = init + 4;
            check_clocks += check_clock[i] + ":00" + ",";
        }
        check_clocks = check_clocks.substring(0, check_clocks.length() - 1);
        // 设置自检时间
        try {
            connection = mySQLPool.getConnection();
            preparedStatement = connection
                    .prepareStatement("update fpp_node set heart_beat_interval = ?,switch = ?,checked_times = ?,"
                            +"check_clock = ?" +" where name = ?");
            preparedStatement.setInt(1, heartBeat);
            preparedStatement.setString(2,binarySwitch );
            preparedStatement.setInt(3, checked_times);
            preparedStatement.setString(4, check_clocks);
            preparedStatement.setString(5, ID);
            if(preparedStatement.executeUpdate() != 0) {
                logger.debug("read node params success");
                return true;
            }
            else {
                logger.debug("read node params failed");
                return false;
            }
        } catch (SQLException e) {
            logger.debug(e.getMessage());
        }finally {
            try {
                connection.recycle();
            } catch (Exception e) {
                logger.debug(e.getMessage());
            }
        }
        return false;
    }

    /**
     * 存放箱子的反馈信息
     * 
     * @param recData
     * @return
     */
    public static boolean writeRecmsg2DB(String recData) {
        DruidPooledConnection connection = null;
        PreparedStatement preparedStatement = null;
        int last_index = recData.indexOf("*");
        //if.....else....的处理只要是为了防止crc中出现'*'
        if(recData.charAt(last_index+1) == '\0') {
            recData = recData.substring(0,recData.indexOf("*")+1);
        }
        else {
            int count = 0;
            for (int i = last_index+2; ; i++) {
                if(recData.charAt(i) == '\0') {
                    count = i;
                    break;
                }
            }
            recData = recData.substring(0,count);
        }
        logger.debug("recdata is "+recData);
        int id_length = Integer.valueOf(recData.substring(4, 5));
        String ID = recData.substring(5, 5 + id_length);
        String msg_type = recData.substring(recData.length()-5,recData.length()-3);
        logger.debug("receive msg_type is: " + ID + "    " + msg_type);
        try {
            connection = mySQLPool.getConnection();
            preparedStatement = connection
                    .prepareStatement("insert into fpp_set(name,msg_type) values(?,?)");
            preparedStatement.setString(1, ID);
            preparedStatement.setString(2, msg_type);
            if (preparedStatement.executeUpdate() != 0) {
                logger.debug("store node_return_message successfully");
                return true;
            } else {
                logger.debug("store node_return_message failed");
                return false;
            }
        } catch (SQLException e) {
            logger.debug(e.getMessage());
        } finally {
            try {
                connection.recycle();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * judge if the node has received the settingData
     * 
     */
    public static boolean readSetFormDB(String msg_type) {
        DruidPooledConnection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            connection = mySQLPool.getConnection();

            preparedStatement = connection
                    .prepareStatement("delete from fpp_set where msg_type = ?");
            preparedStatement.setString(1, msg_type);
            if (preparedStatement.executeUpdate() != 0) {
                logger.debug("send order to node success");
                return true;
            } else {
                return false;
            }

        } catch (SQLException e) {
            logger.debug(e.getMessage());
        }
        return false;
    }
}