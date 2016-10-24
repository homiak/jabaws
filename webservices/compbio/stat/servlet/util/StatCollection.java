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
package compbio.stat.servlet.util;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.TreeMap;

import compbio.stat.collector.StatDB;
import compbio.stat.collector.StatProcessor;
import compbio.ws.client.Services;

public class StatCollection {

	/**
	 * Total number of requests
	 * 
	 * incomplete abandoned cancelled
	 * 
	 * @author pvtroshin
	 * 
	 */

	public enum Stattype {
		CLUSTER, LOCAL, ALL
	}

	private Map<Services, StatProcessor> allStat;
	private Map<Services, StatProcessor> clusterStat;
	private Map<Services, StatProcessor> localStat;

	public Map<Services, StatProcessor> getAllStat() {
		return allStat;
	}
	public Map<Services, StatProcessor> getClusterStat() {
		return clusterStat;
	}
	public Map<Services, StatProcessor> getLocalStat() {
		return localStat;
	}

	public static Map<Date, Totals> getStats(int monthsToReport)
			throws SQLException {
		Calendar fromCal = GregorianCalendar.getInstance();
		fromCal.add(Calendar.MONTH, -monthsToReport);
		return getStats(fromCal.getTime());
	}

	public static Map<Date, Totals> getStats(Date fromDate) throws SQLException {
		Map<Date, Totals> allstats = new TreeMap<Date, Totals>();

		Calendar fromCal = GregorianCalendar.getInstance();
		fromCal.setTime(fromDate);
		fromCal.set(Calendar.DAY_OF_MONTH, 1);

		Calendar toCal = GregorianCalendar.getInstance();
		toCal.setTime(new Date());
		if (fromCal.after(toCal)) {
			throw new AssertionError("From Date must be before ToDate! ");
		}
		while (fromCal.before(toCal)) {
			Date from = fromCal.getTime();
			fromCal.add(Calendar.MONTH, 1);
			allstats.put(from, getJobCounts(from, fromCal.getTime()));
		}
		return allstats;
	}

	private static Totals getJobCounts(Date from, Date to) throws SQLException {
		StatDB db = new StatDB();
		Totals t = new Totals();
		Timestamp fromTime = new Timestamp(from.getTime());
		Timestamp toTime = new Timestamp(to.getTime());
		t.total = db.getTotalJobsCount(fromTime, toTime);
		t.incomplete = db.getIncompleteCount(fromTime, toTime);
		t.abandoned = db.getAbandonedCount(fromTime, toTime);
		t.cancelled = db.getCancelledCount(fromTime, toTime);
		return t;
	}

	public static StatCollection newStatCollecton(Date startDate, Date endDate)
			throws SQLException {

		Timestamp startStamp = new Timestamp(startDate.getTime());
		Timestamp stopStamp = new Timestamp(endDate.getTime());
		StatCollection collection = new StatCollection();
		StatDB statdb = new StatDB();

		// Total
		collection.allStat = new TreeMap<Services, StatProcessor>();
		for (Services service : Services.values()) {
			collection.allStat.put(
					service,
					new StatProcessor(statdb.readData(startStamp, stopStamp,
							service, null)));
		}

		// Cluster
		collection.clusterStat = new TreeMap<Services, StatProcessor>();
		for (Services service : Services.values()) {
			collection.clusterStat.put(
					service,
					new StatProcessor(statdb.readData(startStamp, stopStamp,
							service, true)));
		}

		// Local
		collection.localStat = new TreeMap<Services, StatProcessor>();
		for (Services service : Services.values()) {
			collection.localStat.put(
					service,
					new StatProcessor(statdb.readData(startStamp, stopStamp,
							service, false)));
		}
		return collection;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((allStat == null) ? 0 : allStat.hashCode());
		result = prime * result
				+ ((clusterStat == null) ? 0 : clusterStat.hashCode());
		result = prime * result
				+ ((localStat == null) ? 0 : localStat.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		StatCollection other = (StatCollection) obj;
		if (allStat == null) {
			if (other.allStat != null)
				return false;
		} else if (!allStat.equals(other.allStat))
			return false;
		if (clusterStat == null) {
			if (other.clusterStat != null)
				return false;
		} else if (!clusterStat.equals(other.clusterStat))
			return false;
		if (localStat == null) {
			if (other.localStat != null)
				return false;
		} else if (!localStat.equals(other.localStat))
			return false;
		return true;
	}
	@Override
	public String toString() {
		String value = "";
		for (Map.Entry<Services, StatProcessor> entry : allStat.entrySet()) {
			value += entry.getKey() + ": ";
			value += entry.getValue() + "\n";
		}
		for (Map.Entry<Services, StatProcessor> entry : clusterStat.entrySet()) {
			value += entry.getKey() + ": ";
			value += entry.getValue() + "\n";
		}
		for (Map.Entry<Services, StatProcessor> entry : localStat.entrySet()) {
			value += entry.getKey() + ": ";
			value += entry.getValue() + "\n";
		}
		return value;
	}

}
