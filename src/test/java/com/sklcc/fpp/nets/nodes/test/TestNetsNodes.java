package com.sklcc.fpp.nets.nodes.test;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import junit.framework.TestCase;

import com.alibaba.druid.pool.DruidPooledConnection;
import com.sklcc.fpp.utils.sqls.MySQLPool;

/**
 * 
 * @author kaiyao
 * Test com.sklcc.fpp.nets.nodes
 */
/*
public class TestNetsNodes extends TestCase {
	private String ip = null;
	private int port = 0;
	private Socket virtualBox = null;
	private BufferedWriter writer = null;
	private DruidPooledConnection connection = null;
	private PreparedStatement preparedStatement = null;
	private static MySQLPool mySQLPool = MySQLPool.getInstance();

	protected void setUp() throws Exception {
		ip = "127.0.0.1";
		port = 9500;
		virtualBox = new Socket(ip, port);
		writer = new BufferedWriter(new OutputStreamWriter(
				virtualBox.getOutputStream()));
	}

	public void testmainRunnable() {
		try {
			// ALIVE 
			// ID:test001
			try {
				String aliveData = "#1017tsnn001  *";
				String ID = null;
				int IDlength = Integer.parseInt(aliveData.substring(4, 5));
				ID = aliveData.substring(5, 5 + IDlength); // ID编号
				writer.write(aliveData);
				writer.flush();
				Thread.sleep(2 * 1000);
				connection = mySQLPool.getConnection();
				preparedStatement = connection
						.prepareStatement("select * from fpp_node where name = ?");
				preparedStatement.setString(1, ID);
				ResultSet rs = preparedStatement.executeQuery();
				assertNotNull(rs.next());
				rs.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			// PROBLEM
			// ID:test002
			// sID = A
			try {
				String proData = "#1027tsnn002C101  *";
				connection = mySQLPool.getConnection();
				int length = Integer.valueOf(proData.substring(4, 5)); // ID编号的长度
				String ID = proData.substring(5, 5 + length); // ID编号
				String sID = proData.substring(5 + length, 6 + length); // 设备编号
				int nID = 0;
				int testnID = 0;
				try {
					preparedStatement = connection
							.prepareStatement("select * from fpp_node where name = ?");
					preparedStatement.setString(1, ID);
					ResultSet rsfNode = preparedStatement.executeQuery();
					while (rsfNode.next()) {
						nID = rsfNode.getInt("nid");
					}
					rsfNode.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
				try {
					preparedStatement = connection
							.prepareStatement("select * from fpp_problem where sid = ?");
					preparedStatement.setString(1, sID);
					ResultSet rsfPro = preparedStatement.executeQuery();
					while (rsfPro.next()) {
						testnID = rsfPro.getInt("nid");
					}
					rsfPro.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
				assertEquals(nID, testnID);
			} catch (Exception e) {
				e.printStackTrace();
			}
			// ALARM
			// ID:test003
			try {
				String alarmData = "#1037tsnn003F01  *";
				String ID = null;
				int IDlength = Integer.parseInt(alarmData.substring(4, 5));
				ID = alarmData.substring(5, 5 + IDlength); // ID编号
				writer.write(alarmData);
				writer.flush();
				Thread.sleep(2 * 1000);
				connection = mySQLPool.getConnection();
				preparedStatement = connection
						.prepareStatement("select * from fpp_node where name = ?");
				preparedStatement.setString(1, ID);
				ResultSet rs = preparedStatement.executeQuery();
				assertNotNull(rs.next());
				rs.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				virtualBox.close();
				connection.recycle();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
*/