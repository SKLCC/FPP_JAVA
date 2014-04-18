package com.sklcc.fpp.nets.pcs;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.alibaba.druid.pool.DruidPooledConnection;
import com.sklcc.fpp.utils.sqls.MySQLPool;

public class MySQLManager {

    private static MySQLPool mySQLPool = MySQLPool.getInstance();
    private static Logger logger = LogManager.getLogger(MySQLManager.class
            .getSimpleName());

    // getSimpleName（）返回底层类的名字

    private MySQLManager() {
    }

    /**
     * write the PC rec info to db
     * 
     * @param id
     *            alarm id
     * @param type
     *            GET/HANDLE/WRONGID
     */
    public static boolean writePCData2DB(String id, PCDataType type) {
        DruidPooledConnection connection = null;
        PreparedStatement preparedStatement = null; // 处理sql预编译对象

        try {
            connection = mySQLPool.getConnection(); // 连上数据库
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String now = sdf.format(new Date());

            switch (type) {
            case GET: {// PC get the alarm
                preparedStatement = connection
                        .prepareStatement("update fpp_alarm "
                                + "set gtime = ? " + "where id = ?");
                // 设置数据库参数
                preparedStatement.setString(1, now);
                preparedStatement.setString(2, id);

                // 判断是否处理成功
                if (preparedStatement.executeUpdate() != 0)
                    return true;
                else
                    return false;
            }
            case HANDLE: {// PC has handle the alarm
                preparedStatement = connection
                        .prepareStatement("update fpp_alarm "
                                + "set etime = ? " + "where id = ?");
                preparedStatement.setString(1, now);
                preparedStatement.setString(2, id);

                if (preparedStatement.executeUpdate() != 0)
                    return true;
                else
                    return false;
            }
            case WRONGID: {// PC get the wrong alarm
                preparedStatement = connection
                        .prepareStatement("delete from fpp_alarm "
                                + "where id = ?");
                preparedStatement.setString(1, id);
                if (preparedStatement.executeUpdate() != 0)
                    return true;
                else
                    return false;
            }
            default:
                break;
            }
            return false;
        } catch (SQLException e) {
            // write DB Error
            logger.error("DB ERROR!" + e.getMessage());
        } finally {
            if (connection != null) {
                try {
                    connection.recycle();
                } catch (SQLException e) {
                    // ignore
                }
            }
        }
        return false;
    }

    /**
     * write alarm to the db and get the alarm id
     * 
     * @param content
     *            receive from the cuo
     * 
     * @return the alarm id, -1 means wrong
     */
    public static int writeAlarm2DB(String content) {
        // #xxxx123,1*
        DruidPooledConnection connection = null;
        PreparedStatement preparedStatement = null;

        try {
            connection = mySQLPool.getConnection();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String now = sdf.format(new Date());

            Matcher matcher = Pattern.compile("#(.*),(.*)\\*").matcher(content);
            if (matcher.find()) {
                String nid = matcher.group(1);
                String sid = matcher.group(2);

                preparedStatement = connection
                        .prepareStatement("select nid from fpp_node "
                                + "where name = ?");
                preparedStatement.setString(1, nid);
                ResultSet resultSet = preparedStatement.executeQuery();
                while (resultSet.next()) {
                    nid = resultSet.getString("nid");
                    preparedStatement = connection
                            .prepareStatement("insert into fpp_alarm "
                                    + "(nid,sid,stime)" + "values (?,?,?);");
                    preparedStatement.setString(1, nid);
                    preparedStatement.setString(2, sid);
                    preparedStatement.setString(3, now);

                    if (preparedStatement.executeUpdate() == 0) {
                        return -1;
                    }

                    preparedStatement = connection
                            .prepareStatement("select id from fpp_alarm "
                                    + "where nid = ? " + "&& sid = ?"
                                    + " order by id desc limit 1");
                    preparedStatement.setString(1, nid);
                    preparedStatement.setString(2, sid);

                    resultSet.close();
                    resultSet = preparedStatement.executeQuery();
                    while (resultSet.next()) {
                        return resultSet.getInt("id");
                    }
                }
            }

        } catch (SQLException e) {
            logger.error("db erorr : " + e.getMessage());
        } finally {
            try {
                connection.recycle();
            } catch (SQLException e) {
            }
        }
        return -1;
    }

    /**
     * update the pc client status to db
     * 
     * @param clientRunnable
     *            client runnable
     * @return whether success
     */
    public static boolean checkClientStatus2DB(PCClientRunnable clientRunnable) {
        DruidPooledConnection connection = null;
        PreparedStatement preparedStatement = null;

        try {
            connection = mySQLPool.getConnection();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String now = sdf.format(new Date());

            preparedStatement = connection
                    .prepareStatement("select alive from fpp_client"
                            + " where name = ?");
            preparedStatement.setString(1, clientRunnable.getPcid());
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                if (resultSet.getInt(1) == 1) {
                    preparedStatement = connection
                            .prepareStatement("update fpp_client set ip = ?,"
                                    + "time = ? " + "where name = ?;");
                    preparedStatement.setString(1, clientRunnable.getcSocket()
                            .getInetAddress().toString().substring(1));
                    preparedStatement.setString(2, now);
                    preparedStatement.setString(3, clientRunnable.getPcid());

                    if (preparedStatement.executeUpdate() != 0)
                        return true;
                    else {
                        return false;
                    }
                } else {
                    return false;
                }
            }

        } catch (SQLException e) {
            // write DB Error
            logger.error("DB ERROR!" + e.getMessage());
        } finally {
            try {
                connection.recycle();
            } catch (SQLException e) {
            }
        }
        return false;
    }

    /**
     * get the unsolved problem and the tartget
     * 
     * @return hashmap string is the areaid, list is the message
     */
    public static HashMap<String, LinkedList<String>> getUnsolvedProblem() {
        DruidPooledConnection connection1 = null, connection2 = null;
        PreparedStatement preparedStatement1, preparedStatement2;

        HashMap<String, LinkedList<String>> targets = new HashMap<String, LinkedList<String>>();

        try {
            connection1 = mySQLPool.getConnection();
            connection2 = mySQLPool.getConnection();

            preparedStatement1 = connection1
                    .prepareStatement("select nid, sid, id from fpp_alarm "
                            + "where gtime is null " + "or etime is null;");
            ResultSet resultSet1 = preparedStatement1.executeQuery();
            while (resultSet1.next()) {
                String nid = resultSet1.getString("nid");
                String sid = resultSet1.getString("sid");
                String id = resultSet1.getString("id");

                preparedStatement2 = connection2
                        .prepareStatement("select name from fpp_node"
                                + " where nid = ?");
                preparedStatement2.setString(1, nid);

                ResultSet resultSet2 = preparedStatement2.executeQuery();
                resultSet2.next();
                String name = resultSet2.getString("name");
                String order = "#" + name + "," + sid + "," + id + "*";

                if (targets.containsKey(name.substring(0, 4).toLowerCase())) {
                    targets.get(name.substring(0, 4).toLowerCase()).add(order);
                } else {
                    LinkedList<String> orders = new LinkedList<String>();
                    orders.add(order);
                    targets.put(name.substring(0, 4).toLowerCase(), orders);
                }
            }
            return targets;
        } catch (SQLException e) {
            // write DB Error
            logger.error("DB ERROR!" + e.getMessage());
        } finally {
            try {
                connection1.recycle();
                connection2.recycle();
            } catch (SQLException e) {
            }
        }

        return null;
    }
}
