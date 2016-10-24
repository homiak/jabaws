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

package compbio.engine;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;

import compbio.engine.client.ConfiguredExecutable;

/**
 * Submit jobs for execution
 * 
 * @author pvtroshin
 * 
 */
public class SubmissionManager {

	private static final Logger log = Logger.getLogger(SubmissionManager.class);
	static final Map<String, Future<ConfiguredExecutable<?>>> submittedTasks = new ConcurrentHashMap<String, Future<ConfiguredExecutable<?>>>();

	private SubmissionManager() {
		// uninitializable
	}

	public static void addTask(ConfiguredExecutable<?> executable,
			Future<ConfiguredExecutable<?>> future) {
		Future<ConfiguredExecutable<?>> replacedTask = submittedTasks.put(
				executable.getTaskId(), future);
		// just a percussion should never happened
		if (replacedTask != null) {
			log.fatal("Duplicated task id is detected by local engine for: "
					+ executable);
			throw new RuntimeException("Duplicated task ID is detected: "
					+ executable.getTaskId());
		}
	}

	public static Future<ConfiguredExecutable<?>> getTask(String taskId) {
		return submittedTasks.get(taskId);
	}

	public static void removeTask(ConfiguredExecutable<?> executable) {
		synchronized (submittedTasks) {
			assert executable != null;
			String taskId = executable.getTaskId();
			assert compbio.engine.client.Util.isValidJobId(taskId);
			submittedTasks.remove(taskId);
		}
	}

	public static void removeTask(String key) {
		synchronized (submittedTasks) {
			if (compbio.engine.client.Util.isValidJobId(key)) {
				submittedTasks.remove(key);
			} else {
				log.error("Invalid key is given! " + key);
			}
		}
	}

}
