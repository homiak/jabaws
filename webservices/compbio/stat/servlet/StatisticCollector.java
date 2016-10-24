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
package compbio.stat.servlet;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.log4j.Logger;

import compbio.engine.conf.PropertyHelperManager;
import compbio.stat.collector.ExecutionStatCollector;
import compbio.stat.collector.StatDB;
import compbio.util.PropertyHelper;
import compbio.util.Util;

public class StatisticCollector implements ServletContextListener {

	static PropertyHelper ph = PropertyHelperManager.getPropertyHelper();

	private final Logger log = Logger.getLogger(StatisticCollector.class);

	private ScheduledFuture<?> localcf;
	private ScheduledFuture<?> clustercf;
	private ScheduledExecutorService executor;

	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		try {
			if (localcf != null) {
				localcf.cancel(true);
			}
			if (clustercf != null) {
				clustercf.cancel(true);
			}
			executor.shutdown();
			executor.awaitTermination(3, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			log.warn(e.getMessage(), e);
		} finally {
			StatDB.shutdownDBServer();
			executor.shutdownNow();
		}
	}

	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		String clusterWorkDir = getClusterJobDir();
		int clusterMaxRuntime = getClusterJobTimeOut();

		int localMaxRuntime = getLocalJobTimeOut();
		String localWorkDir = compbio.engine.client.Util
				.convertToAbsolute(getLocalJobDir());

		log.info("Initializing statistics collector");
		executor = Executors.newScheduledThreadPool(2);

		if (collectClusterStats()) {

			ExecutionStatCollector clusterCollector = new ExecutionStatCollector(
					clusterWorkDir, clusterMaxRuntime);
			clustercf = executor.scheduleAtFixedRate(clusterCollector, 60,
					24 * 60, TimeUnit.MINUTES);
			// clustercf = executor.scheduleAtFixedRate(clusterCollector, 15,
			// 30,
			// TimeUnit.SECONDS);
			log.info("Collecting cluster statistics ");
		} else {
			log.info("Cluster statistics collector is disabled or not configured! ");
		}
		if (collectLocalStats()) {
			ExecutionStatCollector localCollector = new ExecutionStatCollector(
					localWorkDir, localMaxRuntime);
			// localcf = executor.scheduleAtFixedRate(localCollector, 100, 60,
			// TimeUnit.SECONDS);

			localcf = executor.scheduleAtFixedRate(localCollector, 10, 24 * 60,
					TimeUnit.MINUTES);
			log.info("Collecting local statistics ");
		} else {
			log.info("Local statistics collector is disabled or not configured! ");
		}
	}

	static String getClusterJobDir() {
		return getStringProperty(ph.getProperty("cluster.tmp.directory"));
	}

	static int getClusterJobTimeOut() {
		int maxRunTime = 24 * 7;
		String clusterMaxRuntime = ph.getProperty("cluster.stat.maxruntime");
		if (clusterMaxRuntime != null) {
			clusterMaxRuntime = clusterMaxRuntime.trim();
			maxRunTime = Integer.parseInt(clusterMaxRuntime);
		}
		return maxRunTime;
	}

	static int getLocalJobTimeOut() {
		int maxRunTime = 24;
		String localMaxRuntime = ph.getProperty("local.stat.maxruntime");
		if (localMaxRuntime != null) {
			localMaxRuntime = localMaxRuntime.trim();
			maxRunTime = Integer.parseInt(localMaxRuntime);
		}

		return maxRunTime;
	}

	static String getLocalJobDir() {
		return getStringProperty(ph.getProperty("local.tmp.directory"));
	}

	private static String getStringProperty(String propName) {
		if (propName != null) {
			propName = propName.trim();
		}
		return propName;
	}

	static boolean collectClusterStats() {
		return getBooleanProperty(ph
				.getProperty("cluster.stat.collector.enable"));
	}

	static boolean collectLocalStats() {
		return getBooleanProperty(ph.getProperty("local.stat.collector.enable"));
	}

	private static boolean getBooleanProperty(String propValue) {
		boolean enabled = false;
		if (!Util.isEmpty(propValue)) {
			propValue = propValue.trim();
			enabled = Boolean.parseBoolean(propValue);
		}
		return enabled;
	}

}
