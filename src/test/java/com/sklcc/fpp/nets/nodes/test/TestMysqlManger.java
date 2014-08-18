package com.sklcc.fpp.nets.nodes.test;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import junit.framework.TestCase;

import com.alibaba.druid.pool.DruidPooledConnection;
import com.sklcc.fpp.nets.nodes.MysqlManger;
import com.sklcc.fpp.nets.nodes.NodeDataType;
import com.sklcc.fpp.utils.sqls.MySQLPool;

/**
 * @author kaiyao
 * Test for com.sklcc.fpp.nets.nodes.MysqlManger
 */

public class TestMysqlManger extends TestCase {
	private static MySQLPool mySQLPool = MySQLPool.getInstance();

	protected void setUp() throws Exception {
	}

	/**
	 * Test writeNodeData2DB()
	 * ID:szdx004
	 */
	public void testwriteNodeData2DB() {
		DruidPooledConnection connection = null;
		PreparedStatement preparedStatement = null;
		try {
			// ALIVE
			try {
				String recdata = "#1017szdx004  *";
				NodeDataType type = NodeDataType.ALIVE;
				String address = "192.168.135.1";
				MysqlManger.writeNodeData2DB(recdata, type, address);
				connection = mySQLPool.getConnection();
				int length = Integer.parseInt(recdata.substring(4, 5));
	            String ID = recdata.substring(5, 5 + length);
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
			try {
				String recdata = "#1027szdx004z101  *";
				NodeDataType type = NodeDataType.PROBLEM;
				String address = "192.168.135.1";
				MysqlManger.writeNodeData2DB(recdata, type, address);
				connection = mySQLPool.getConnection();
				int length = Integer.parseInt(recdata.substring(4, 5));
	            String ID = recdata.substring(5, 5 + length);
				preparedStatement = connection
						.prepareStatement("select * from fpp_problem where name = ?");
				preparedStatement.setString(1, ID);
				ResultSet rs = preparedStatement.executeQuery();
				assertNotNull(rs.next());
				rs.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			// ALARM
			try {
				String recdata = "#1037szdx001F01  *";
				NodeDataType type = NodeDataType.ALARM;
				String address = "192.168.135.1";
				MysqlManger.writeNodeData2DB(recdata, type, address);
				connection = mySQLPool.getConnection();
				int length = Integer.parseInt(recdata.substring(4, 5));
	            String ID = recdata.substring(5, 5 + length);
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
				connection.recycle();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Test writeProblem2DB()
	 * ID:szdx002
	 */
	public void testwriteProblem2DB() {
		DruidPooledConnection connection = null;
		PreparedStatement preparedStatement = null;
		String recdata = "#1027szdx002z101  *";
		try {
			MysqlManger.writeProblem2DB(recdata);
			connection = mySQLPool.getConnection();
			int length = Integer.valueOf(recdata.substring(4, 5)); // ID编号的长度
			String name = recdata.substring(5, 5 + length); // ID编号
			String sid = recdata.substring(5 + length, 6 + length); // 设备编号
			int nid = 0;
			int testNid = 0;
			try {
				preparedStatement = connection
						.prepareStatement("select * from fpp_node where name = ?");
				preparedStatement.setString(1, name);
				ResultSet rsfNode = preparedStatement.executeQuery();
				while (rsfNode.next()) {
					nid = rsfNode.getInt("nid");
				}
				rsfNode.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				preparedStatement = connection
						.prepareStatement("select * from fpp_problem where sid = ?");
				preparedStatement.setString(1, sid);
				ResultSet rsfPro = preparedStatement.executeQuery();
				while (rsfPro.next()) {
					testNid = rsfPro.getInt("nid");
				}
				rsfPro.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			assertEquals(nid, testNid);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				connection.recycle();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Test writeAlarm2DB()
	 * ID:szdx003
	 */
	/*public void testwriteAlarm2DB() {
		DruidPooledConnection connection = null;
		PreparedStatement preparedStatement = null;
		String recdata = "#1037szdx003F01  ";
		try {
			MysqlManger.writeAlarm2DB(recdata);
			connection = mySQLPool.getConnection();
			int length = Integer.valueOf(recdata.substring(4, 5));// ID编号的长度
			String ID = recdata.substring(5, 5 + length); // ID编号
			String update_time = null;
			try {
				preparedStatement = connection
						.prepareStatement("select * from fpp_node where name = ?");
				preparedStatement.setString(1, ID);
				ResultSet rs = preparedStatement.executeQuery();
				while (rs.next()) {
					update_time = rs.getString("update_time");
				}
				assertNotNull(update_time);
				rs.close();
			} catch (Exception e) {
				e.printStackTrace();
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
	}*/

	/**
	 * Test writeSetting2DB()
	 * ID:szdx001
	 */
	public void testwriteSetting2DB() {
		DruidPooledConnection connection = null;
		PreparedStatement preparedStatement = null;
		try {
			// type = '4'
			try {
				String ID = "szdx001";
				String sendOrder = "#104407szdx01001  *";
				MysqlManger.writeSetting2DB(sendOrder, ID);
				connection = mySQLPool.getConnection();
				int length = Integer.valueOf(sendOrder.substring(5, 7));
				String newID = sendOrder.substring(7, length + 7);
				preparedStatement = connection
						.prepareStatement("select * from fpp_node where name = ?");
				preparedStatement.setString(1, newID);
				ResultSet rs = preparedStatement.executeQuery();
				assertNotNull(rs.next());
				rs.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			// type = '6'
			try {
				String ID = "szdx010";
				String sendOrder = "#104601401  *";
				MysqlManger.writeSetting2DB(sendOrder, ID);
				connection = mySQLPool.getConnection();
				int checkTimes = Integer.valueOf(sendOrder.substring(7, 8));
				int testcheckTimes = 0;
				preparedStatement = connection
						.prepareStatement("select * from fpp_node where name = ?");
				preparedStatement.setString(1, ID);
				ResultSet rs = preparedStatement.executeQuery();
				while (rs.next()) {
					testcheckTimes = rs.getInt("checked_times");
				}
				assertEquals(checkTimes, testcheckTimes);
				rs.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			// type = '7'
			try {
				String ID = "szdx010";
				String sendOrder = "#1047231:0,2:6,3:12,4:18,5:23,58  *";
				MysqlManger.writeSetting2DB(sendOrder, ID);
				connection = mySQLPool.getConnection();
				preparedStatement = connection
						.prepareStatement("select * from fpp_node where name = ?");
				preparedStatement.setString(1, ID);
				ResultSet rs = preparedStatement.executeQuery();
				assertEquals(true, rs.next());
				rs.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			// type = '1'
			try {
				String ID = "szdx010";
				String sendOrder = "#10410330001  *";
				MysqlManger.writeSetting2DB(sendOrder, ID);
				connection = mySQLPool.getConnection();
				int length = Integer.valueOf(sendOrder.substring(5, 7));
				int heartBeat = Integer.valueOf(sendOrder.substring(7,
						7 + length));
				int testheartBeat = 0;
				preparedStatement = connection
						.prepareStatement("select * from fpp_node where name = ?");
				preparedStatement.setString(1, ID);
				ResultSet rs = preparedStatement.executeQuery();
				while (rs.next()) {
					testheartBeat = rs.getInt("heart_beat_interval");
				}
				assertEquals(heartBeat, testheartBeat);
				rs.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			// type = '2'
			try {
				String ID = "szdx010";
				String sendOrder = "#104220'58.210.28.58',5160@58E2F9*";
				MysqlManger.writeSetting2DB(sendOrder, ID);
				connection = mySQLPool.getConnection();
				int length = Integer.valueOf(sendOrder.substring(5, 7));
				String ipPort = sendOrder.substring(7, 6 + length);
				String string[] = ipPort.split(",");
				String ip = string[0];
				String testip = null;
				int port = Integer.valueOf(string[1]);
				int testport = 0;
				preparedStatement = connection
						.prepareStatement("select * from fpp_node where name = ?");
				preparedStatement.setString(1, ID);
				ResultSet rs = preparedStatement.executeQuery();
				while (rs.next()) {
					testip = rs.getString("server_ip");
					testport = rs.getInt("server_port");
				}
				assertEquals(ip, testip);
				assertEquals(port, testport);
				rs.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			// type = '3'
			try {
				String ID = "szdx010";
				String sendOrder = "#10430400FF01 *";
				MysqlManger.writeSetting2DB(sendOrder, ID);
				connection = mySQLPool.getConnection();
				int sensorSwitcn = Integer.valueOf(sendOrder.substring(7, 11),
						16);
				String binarySwitch = Integer.toBinaryString(sensorSwitcn);
				int num = binarySwitch.length();
				String temp = "";
				if (num < 16) {
					for (int i = 0; i < 16 - num; i++) {
						temp = temp + "0";
					}
				}
				binarySwitch = temp + binarySwitch;
				String testbinarySwitch = null;
				preparedStatement = connection
						.prepareStatement("select * from fpp_node where name = ?");
				preparedStatement.setString(1, ID);
				ResultSet rs = preparedStatement.executeQuery();
				while (rs.next()) {
					testbinarySwitch = rs.getString("switch");
				}
				assertEquals(binarySwitch, testbinarySwitch);
				rs.close();
			} catch (Exception e) {
				e.printStackTrace();
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
	}
	


}
