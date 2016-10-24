/* Copyright (c) 2011 Peter Troshin
 *  
 *  JAva Bioinformatics Analysis Web Services (JABAWS) @version: 2.0     
 * 
 *  This library is free software; you can redistribute it and/or modify it under the terms of the
 *  Apache License version 2 as published by the Apache Software Foundation
 * 
 *  This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the Apache 
 *  License for more details.
 * 
 *  A copy of the license is in apache_license.txt. It is also available here:
 * @see: http://www.apache.org/licenses/LICENSE-2.0.txt
 * 
 * Any republication or derived work distributed in source code form
 * must include this copyright and license notice.
 */
package compbio.stat.collector;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import compbio.engine.conf.PropertyHelperManager;
import compbio.util.Util;
import compbio.ws.client.Services;

/**
 * The database must be stored in the application root directory and called
 * "ExecutionStatistic"
 * 
 * @author pvtroshin
 * 
 */
public class StatDB {

	private static final String driver = "org.apache.derby.jdbc.EmbeddedDriver";
	private static final String protocol = "jdbc:derby:";
	private static final String statDBName = "ExecutionStatistic";

	private static final Logger log = Logger.getLogger(StatDB.class);

	static Connection conn;

	private synchronized static Connection getDBConnection()
			throws SQLException {

		if (conn != null && !conn.isClosed()) {
			return conn;
		} else {
			try {
				String dbpath = PropertyHelperManager.getLocalPath();
				log.info("Looking for JABAWS access statistics database at: "
						+ dbpath);
				System.setProperty("derby.system.home", dbpath);
				// Apparently under Tomcat webapp you cannot rely on Java
				// auto discovery and have to register the driver explicitly
				Class.forName(driver);
				conn = DriverManager.getConnection(protocol + statDBName
						+ ";create=false");

				conn.setAutoCommit(true);
				/*
				 * Runtime.getRuntime().addShutdownHook(new Thread() {
				 * 
				 * @Override public void run() { shutdownDBServer(); } });
				 */
			} catch (ClassNotFoundException e) {
				log.error(e.getMessage(), e);
			}
		}
		return conn;
	}
	public StatDB() throws SQLException {
		this.conn = getDBConnection();
	}

	/**
	 * Connect to test database
	 * 
	 * @param ignored
	 * @throws SQLException
	 */
	StatDB(boolean ignored) throws SQLException {
		this.conn = getTestDBConnection();
	}

	private static Connection getTestDBConnection() throws SQLException {
		System.setProperty("derby.system.home", "testsrc/testdata");
		Connection conn = DriverManager.getConnection(protocol + statDBName
				+ ";create=false");
		conn.setAutoCommit(true);
		log.debug("Connecting to the TEST database!");
		return conn;
	}

	// ServiceName,jobname,start,finish,inputSize,resultSize,isCancelled,isCollected
	/**
	 * 
	 * rs.getBoolean(i) will return true for any non-zero value and false for 0
	 * on SMALLINT data column.
	 * 
	 * @throws SQLException
	 */
	private void createStatTable() throws SQLException {

		/*
		 * Creating a statement object that we can use for running various SQL
		 * statements commands against the database.
		 */
		Statement s = conn.createStatement();
		String create = "create table exec_stat("
				+ "number INT GENERATED ALWAYS AS IDENTITY,"
				+ "service_name VARCHAR(15) NOT NULL, "
				+ "cluster_job_id VARCHAR(30), "
				+ "job_id VARCHAR(35) NOT NULL PRIMARY KEY, "
				+ "start TIMESTAMP," + "finish TIMESTAMP,"
				+ "inputsize BIGINT," + "resultsize BIGINT,"
				+ "isCancelled SMALLINT NOT NULL,"
				+ "isCollected SMALLINT NOT NULL, "
				+ "isClusterJob SMALLINT NOT NULL)";
		// We create a table...
		log.debug(create);
		s.execute(create);
		s.close();
		conn.close();
	}

	static void clearStatTable() throws SQLException {
		Connection conn = getDBConnection();
		String query = "delete from exec_stat";
		Statement st = conn.createStatement();
		st.executeUpdate(query);
		st.close();
		conn.commit();
		conn.close();
	}

	void insertData(Set<JobStat> jobstatus) throws SQLException {
		log.info("Inserting " + jobstatus.size()
				+ " new records into the statistics database");

		conn.setAutoCommit(false);
		String insert = "insert into exec_stat (service_name, cluster_job_id, job_id, start, finish, "
				+ "inputsize, resultsize, isCancelled, isCollected, isClusterJob) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		PreparedStatement pstm = conn.prepareStatement(insert);
		for (JobStat js : jobstatus) {
			// Has to present
			pstm.setString(1, js.webService.toString());

			if (!Util.isEmpty(js.clusterJobId)) {
				pstm.setString(2, js.clusterJobId);
			} else {
				pstm.setString(2, null);
			}
			// Has to present
			pstm.setString(3, js.jobname);

			if (js.start != ExecutionStatCollector.UNDEFINED) {
				pstm.setTimestamp(4, new Timestamp(js.start));
			} else {
				pstm.setTimestamp(4, null);
			}
			if (js.finish != ExecutionStatCollector.UNDEFINED) {
				pstm.setTimestamp(5, new Timestamp(js.finish));
			} else {
				pstm.setTimestamp(5, null);
			}
			// -1 if UNDEFINED
			pstm.setLong(6, js.inputSize);
			// -1 if UNDEFINED
			pstm.setLong(7, js.resultSize);

			pstm.setBoolean(8, js.isCancelled);
			pstm.setBoolean(9, js.isCollected);
			pstm.setBoolean(10, js.isClusterJob());
			pstm.executeUpdate();
		}
		conn.commit();
		conn.setAutoCommit(true);
		pstm.close();
	}

	public Date getEarliestRecord() throws SQLException {
		String query = "select min(start) from exec_stat";
		Statement st = conn.createStatement();
		ResultSet res = st.executeQuery(query);
		boolean exist = res.next();
		Date date = new Date();
		if (exist) {
			date = res.getDate(1);
		}

		res.close();
		st.close();
		return date;
	}

	public int getTotalJobsCount(Timestamp from, Timestamp to)
			throws SQLException {
		String allQuery = "select count(*) from exec_stat where start BETWEEN ? and ? ";
		return getIntResult(from, to, allQuery);
	}

	public int getCancelledCount(Timestamp from, Timestamp to)
			throws SQLException {
		// js.isCancelled
		String cancelledQuery = "select count(*) from exec_stat where start BETWEEN ? and ?  and  isCancelled=1 ";
		return getIntResult(from, to, cancelledQuery);
	}

	public int getAbandonedCount(Timestamp from, Timestamp to)
			throws SQLException {
		// !js.isCollected && !js.isCancelled && js.hasResult()
		String abandonedQuery = "select count(*) from exec_stat where start BETWEEN ? and ? and isCollected=0 and isCancelled=0 and resultsize>0 ";
		return getIntResult(from, to, abandonedQuery);
	}

	public int getIncompleteCount(Timestamp from, Timestamp to)
			throws SQLException {
		// !js.hasResult()
		String incompleteQuery = "select count(*) from exec_stat where start BETWEEN ? and ? and resultsize<=0 and isCancelled=0";
		return getIntResult(from, to, incompleteQuery);
	}

	private int getIntResult(Timestamp from, Timestamp to, String query)
			throws SQLException {

		log.debug("getIntRes: QUERY: " + query);
		log.debug("getIntRes: FROM: " + from);
		log.debug("getIntRes: TO: " + to);

		PreparedStatement pstm = conn.prepareStatement(query);
		pstm.setTimestamp(1, from);
		pstm.setTimestamp(2, to);
		pstm.execute();
		ResultSet res = pstm.getResultSet();
		boolean exist = res.next();
		int count = 0;
		if (exist) {
			count = res.getInt(1);
		}
		log.debug("getIntRes: RES: " + count);
		res.close();
		pstm.close();
		return count;
	}

	public List<JobStat> readData(Timestamp from, Timestamp to,
			Services wservice, Boolean clusterOnly) throws SQLException {

		String query = "select service_name, cluster_job_id, job_id, start, finish, inputsize, "
				+ "resultsize, isCancelled, isCollected from exec_stat where start BETWEEN ? and ? ";

		if (wservice != null) {
			query += " and service_name=? ";
		}

		if (clusterOnly != null) {
			if (clusterOnly) {
				query += " and isClusterJob!=0 ";
			} else {
				query += " and isClusterJob=0 ";
			}
		}

		log.debug("QUERY: " + query);
		log.debug("FROM: " + from);
		log.debug("TO: " + to);
		log.debug("WS: " + wservice);

		PreparedStatement pstm = conn.prepareStatement(query);
		pstm.setTimestamp(1, from);
		pstm.setTimestamp(2, to);
		if (wservice != null) {
			pstm.setString(3, wservice.toString());
		}
		pstm.execute();
		List<JobStat> stats = new ArrayList<JobStat>();
		ResultSet rs = pstm.getResultSet();
		int rcount = 0;

		while (rs.next()) {
			rcount++;
			stats.add(JobStat.newInstance(Services.getService(rs.getString(1)),
					rs.getString(2), rs.getString(3), rs.getTimestamp(4),
					rs.getTimestamp(5), rs.getLong(6), rs.getLong(7),
					rs.getBoolean(8), rs.getBoolean(9)));
		}

		log.debug("QUERY result len: " + rcount);
		rs.close();
		pstm.close();

		return stats;
	}

	/**
	 * Removes the job if
	 * 
	 * 1) It has already been recorded
	 * 
	 * 2) It has not completed and did not timeout - this is to prevent
	 * recording the information on the incomplete jobs.
	 * 
	 * @param fsJobs
	 * @throws SQLException
	 */
	public void removeRecordedJobs(Set<JobStat> fsJobs) throws SQLException {

		String query = "select job_id from exec_stat";

		Statement st = conn.createStatement();
		ResultSet result = st.executeQuery(query);

		while (result.next()) {
			String recordedJob = result.getString(1);
			JobStat recStat = JobStat.newIncompleteStat(recordedJob);
			if (fsJobs.contains(recStat)) {
				fsJobs.remove(recStat);
			}
		}
		result.close();
		st.close();
	}

	public static synchronized final void shutdownDBServer() {
		// ## DATABASE SHUTDOWN SECTION ##
		/***
		 * In embedded mode, an application should shut down Derby. Shutdown
		 * throws the XJ015 exception to confirm success.
		 ***/
		try {
			if (conn != null) {
				conn.close();
			}
		} catch (SQLException e) {
			log.warn("Database commit failed with " + e.getLocalizedMessage());
		}
		boolean gotSQLExc = false;
		try {
			DriverManager.getConnection("jdbc:derby:;shutdown=true");
		} catch (SQLException se) {
			if (se.getSQLState().equals("XJ015")) {
				gotSQLExc = true;
			}
		}
		if (!gotSQLExc) {
			log.warn("Database did not shut down normally");
		} else {
			log.info("Database shut down normally");
		}
	}
	public static void main(String[] args) {
		// This is called from Ant cleanStatTable task
		try {
			clearStatTable();
			shutdownDBServer();
		} catch (SQLException e) {
			System.err.println("Fails to clean up JABAWS stat database!");
			e.printStackTrace();
		}
		// new StatDB().createStatTable();
		// insertData(null);
		/*
		 * StatDB statdb = new StatDB(); Date from = new Date();
		 * from.setMonth(1); System.out.println(new
		 * StatProcessor(statdb.readData( new Timestamp(from.getTime()), new
		 * Timestamp(new Date().getTime()), null, null)).reportStat());
		 */
	}
}
