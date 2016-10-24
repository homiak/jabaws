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

import java.util.Queue;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import compbio.engine.client.PathValidator;

public final class PulledFileCache {

	private static final Logger log = Logger.getLogger(PulledFileCache.class);

	private static final Queue<FilePuller> CACHE = new DelayQueue<FilePuller>();

	public static FilePuller get(final String fileName) {
		assert PathValidator.isAbsolutePath(fileName);

		for (FilePuller fp : CACHE) {
			if (fp.getFile().equals(fileName)) {
				log.debug("Retrieving element from cache: " + fp);
				return fp;
			}
		}
		return null;
	}

	/**
	 * This method allows duplicates to be added. This is a responsibility of
	 * the called to ensure this will not happen!
	 * 
	 * @param fpuller
	 * @return true if the same filepooler is already in cache
	 */
	public static boolean put(final FilePuller fpuller) {
		sweep();
		log.debug("Adding element to cache: " + fpuller);
		if (fpuller.getDelay(TimeUnit.NANOSECONDS) < 0) {
			throw new IllegalArgumentException(
					"Could not cache expired FilePullers! FilePuler: "
							+ fpuller);
		}
		return CACHE.add(fpuller);
	}

	private static void sweep() {
		FilePuller puller = null;
		do {
			// All elements with delay expired gets removed from cache here
			puller = CACHE.poll();
			if (puller != null) {
				puller.disconnect();
				log.debug("Removing element from cache: " + puller);
			}
		} while (puller != null);
	}

	static int getSize() {
		return CACHE.size();
	}

	static void clear() {
		CACHE.clear();
	}

	static void print() {
		System.out.println(CACHE);
	}
}
