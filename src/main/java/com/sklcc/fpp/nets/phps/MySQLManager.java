/**
 * 
 */
package com.sklcc.fpp.nets.phps;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.alibaba.druid.pool.DruidPooledConnection;
import com.sklcc.fpp.utils.sqls.MySQLPool;

/**
 * @author john
 * 
 */
public class MySQLManager {

    private static MySQLPool mySQLPool = MySQLPool.getInstance();
    private static Logger    logger    = LogManager.getLogger(MySQLManager.class.getSimpleName());

    public MySQLManager() {}

    /**
     * Get all values of one column
     * 
     * @param The
     *            name of the column
     * @return all values in this column in a LinkedList
     */
    public static LinkedList<String> getColumnValues(String column) {
        LinkedList<String> columnValues = new LinkedList<String>();
        DruidPooledConnection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            connection = mySQLPool.getConnection();
            preparedStatement = connection.prepareStatement("select "+ column + " from fpp_node");
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                columnValues.add(resultSet.getString(column));
            }
        } catch (SQLException e) {
            logger.error("db error" + e.getMessage());
        }
        return columnValues;
    }

}
