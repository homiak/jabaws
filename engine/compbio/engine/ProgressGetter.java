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

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import compbio.metadata.ChunkHolder;

public class ProgressGetter {

	private static final Logger log = Logger.getLogger(ProgressGetter.class);

	static final ChunkHolder pull(String file, long position, long delay,
			TimeUnit unit) {
		if (compbio.util.Util.isEmpty(file)) {
			throw new NullPointerException("File must be supplied!");
		}

		FilePuller puller = PulledFileCache.get(file);
		if (puller == null) {
			puller = FilePuller.newFilePuller(file, 1024);
			// Use custom delay
			if (delay != 0) {
				puller.setDelay(delay, unit);
			}
			PulledFileCache.put(puller);
		}
		if (puller.isFileCreated()) {
			try {
				return puller.pull(position);
			} catch (IOException e) {
				log.error(e.getLocalizedMessage(), e.getCause());
			}
		}
		return null;
	}

	public final static ChunkHolder pull(String file, long position) {
		// Use default delay time
		if (position < 0) {
			throw new IllegalArgumentException(
					"Position must be greater then 0");
		}
		return pull(file, position, 0, null);
	}

	static byte getProgress(String progressFile, long delay, TimeUnit unit) {
		if (compbio.util.Util.isEmpty(progressFile)) {
			throw new NullPointerException("File must be supplied!");
		}

		FilePuller puller = PulledFileCache.get(progressFile);
		if (puller == null) {
			puller = FilePuller.newProgressPuller(progressFile);
			// Use custom delay
			if (delay != 0) {
				puller.setDelay(delay, unit);
			}
			PulledFileCache.put(puller);
		}
		if (puller.isFileCreated()) {
			try {
				byte value = puller.getProgress();
				return value;
			} catch (IOException e) {
				log.error(e.getLocalizedMessage(), e.getCause());
			}
		}
		return 0;
	}

	public final static byte getProgress(String progressFile) {
		return getProgress(progressFile, 0, null);
	}

}
