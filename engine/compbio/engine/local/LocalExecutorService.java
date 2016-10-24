/* Copyright (c) 2009 Peter Troshin
 *  
 *  JAva Bioinformatics Analysis Web Services (JABAWS) @version: 1.0     
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

package compbio.engine.local;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.log4j.Logger;

import compbio.engine.conf.PropertyHelperManager;
import compbio.util.PropertyHelper;
import compbio.util.Util;

public final class LocalExecutorService extends ThreadPoolExecutor {

	private final static Logger log = Logger
			.getLogger(LocalExecutorService.class);
	private final static String threadNumPropName = "engine.local.thread.number";

	private static LocalExecutorService INSTANCE = null;
	private final ThreadLocal<Long> startTime = new ThreadLocal<Long>();
	private final AtomicLong numTasks = new AtomicLong();
	private final AtomicLong totalTime = new AtomicLong();

	private LocalExecutorService(int corePoolSize, int maximumPoolSize,
			long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
	}

	/**
	 * This method returns the single instance of CachedThreadPoolExecutor which
	 * it cashes internally
	 * 
	 * @return the instance
	 */
	public synchronized static LocalExecutorService getExecutor() {
		if (INSTANCE == null) {
			INSTANCE = init();
		}
		log.info("Current Active Threads Count: " + INSTANCE.getActiveCount());
		return INSTANCE;
	}

	private static LocalExecutorService init() {
		int procNum = Runtime.getRuntime().availableProcessors();
		// Add safety net if this function is unavailable
		if (procNum < 1) {
			procNum = 1;
		}
		if (procNum > 4) {
			procNum = procNum - 1; // leave one processor for overhead
			// management
		}
		PropertyHelper ph = PropertyHelperManager.getPropertyHelper();
		String threadNum = ph.getProperty(threadNumPropName);
		log.debug("Thread number for local execution from conf file is "
				+ threadNum);
		int threads = 0;
		if (!Util.isEmpty(threadNum)) {
			try {
				threads = Integer.parseInt(threadNum);
				if (threads > 1 && threads < procNum * 2) {
					procNum = threads;
				}
			} catch (NumberFormatException e) {
				log.error("Cannot understand " + threadNumPropName
						+ " property. Expecting whole number, but given "
						+ threadNum);
			}
		}

		log.debug("Constructing thread pool for executor with " + procNum
				+ " thread(s)");
		LocalExecutorService executor = new LocalExecutorService(procNum,
				procNum, 0L, TimeUnit.MILLISECONDS,
				new LinkedBlockingQueue<Runnable>());
		// Make sure that the executor is going to be properly closed
		Runtime.getRuntime().addShutdownHook(new Thread() {

			@Override
			public void run() {
				shutDown();
			}
		});
		return executor;
	}

	/**
	 * This stops all executing processes via interruption. Thus it is vital
	 * that all processes that use this service respond to interruption
	 * 
	 * Stops internal executor service which captures streams of native
	 * executables. This method is intended for stopping service if deployed in
	 * the web application context. There is NO NEED of using this method
	 * otherwise as the executor service is taken care of internally.
	 */
	public static void shutDown() {
		if (INSTANCE != null) {
			INSTANCE.shutdownNow();
		}
	}

	/**
	 * If the Executor queue is empty
	 * 
	 * @return true is not all threads are busy, false otherwise
	 */
	public boolean canAcceptMoreWork() {
		// alternative to use: INSTANCE.getQueue().isEmpty(); - but this will
		// inevitably put the last task to the queue
		return INSTANCE.getMaximumPoolSize() > INSTANCE.getActiveCount();
	}

	@Override
	protected void beforeExecute(Thread t, Runnable r) {
		super.beforeExecute(t, r);
		// class of r is java.util.concurrent.FutureTask
		log.info(String.format("Thread %s: start %s", t, r));
		startTime.set(System.nanoTime());
	}

	@Override
	protected void afterExecute(Runnable r, Throwable t) {
		try {
			long endTime = System.nanoTime();
			long taskTime = endTime - startTime.get();
			numTasks.incrementAndGet();
			totalTime.addAndGet(taskTime);
			log.info(String.format("Throwable %s: end %s, time=%dns", t, r,
					taskTime));
		} finally {
			super.afterExecute(r, t);
		}
	}

	@Override
	protected void terminated() {
		try {
			if (numTasks.get() != 0) {
				log.info(String.format("Terminated : avg time=%dns",
						totalTime.get() / numTasks.get()));
			}
		} finally {
			super.terminated();
		}
	}
}
