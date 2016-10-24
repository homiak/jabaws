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

package compbio.engine.conf;

import org.apache.log4j.Logger;

import compbio.engine.client.ConfExecutable;
import compbio.engine.client.Executable;
import compbio.util.Util;

public class DirectoryManager {

	private static Logger log = Logger.getLogger(DirectoryManager.class);

	public static final String DELIM = "#";

	public static String getTaskDirectory(Class<?> clazz) {
		return clazz.getSimpleName() + DELIM + getNonRepeatableNumber();
	}

	static long getNonRepeatableNumber() {
		// The random value is concatenated with time value not added to it and
		// then converted to long.
		// Top 3 high order bits are cut and replaced by random number
		// to make sure that the resulting number fits into long value
		String longNum = new Long(System.nanoTime()).toString();
		return Long.parseLong(Util.getRandomNumber(100, 999)
				+ longNum.substring(4));
	}

	public static Class<Executable<?>> getClass(String taskId) {
		assert compbio.engine.client.Util.isValidJobId(taskId);
		String className = null;
		if (taskId.startsWith(ConfExecutable.CLUSTER_TASK_ID_PREFIX)) {
			className = taskId.substring(1, taskId.indexOf(DELIM));
		} else {
			className = taskId.substring(0, taskId.indexOf(DELIM));
		}
		try {
			return (Class<Executable<?>>) Class.forName(className);
		} catch (ClassNotFoundException e) {
			log.error(
					"Could not parse taskId " + taskId + " Message "
							+ e.getLocalizedMessage(), e.getCause());
		}
		return null;
	}

}
