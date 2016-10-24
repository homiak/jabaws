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

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import compbio.engine.client.PathValidator;
import compbio.metadata.ChunkHolder;
import compbio.util.FileWatcher;

public class FilePuller implements Delayed {

	private static final Logger log = Logger.getLogger(FilePuller.class);

	// 5 minutes in nanosec
	private static long defaultDelay = 1000 * 1000 * 1000L * 5 * 60;
	private final File file;
	private long lastAccessTime;
	private FileWatcher watcher;
	final int chunkSize;

	// used for testing only
	long delay = 0;

	private FilePuller(String file, int size) {
		FileWatcher.validateInput(file, size);
		if (compbio.util.Util.isEmpty(file)) {
			throw new NullPointerException("File name must be provided!");
		}
		// The fact that the file may not exist at a time does not matter here
		if (!PathValidator.isAbsolutePath(file)) {
			throw new IllegalArgumentException("Absolute path to the File "
					+ file + " is expected but not provided!");
		}
		this.file = new File(file);
		this.chunkSize = size;
		this.lastAccessTime = System.nanoTime();
	}

	private FilePuller(String file) {
		if (compbio.util.Util.isEmpty(file)) {
			throw new NullPointerException("File name must be provided!");
		}
		// The fact that the file may not exist at a time does not matter here
		if (!PathValidator.isAbsolutePath(file)) {
			throw new IllegalArgumentException("Absolute path to the File "
					+ file + " is expected but not provided!");
		}
		this.file = new File(file);
		this.chunkSize = 3;
		this.lastAccessTime = System.nanoTime();
	}

	public static FilePuller newFilePuller(String file, int chunkSize) {
		return new FilePuller(file, chunkSize);
	}

	/**
	 * Progress Puller is designed to read 3 characters from the beginning of
	 * the file, nothing more. Intended to be used in conjunction with a tool
	 * which output progress as a percent value from 0 to 100. In any cases
	 * progress could not be more than the largest byte value 255.
	 * 
	 * @param file
	 * @return instance
	 */
	public static FilePuller newProgressPuller(String file) {
		return new FilePuller(file);
	}

	public ChunkHolder pull(long position) throws IOException {
		initPull();
		String valueUTF16 = watcher.pull(position);
		String value = null;
		if (valueUTF16 != null) {
			value = removeInvalidXMLCharacters(valueUTF16);
		}
		return new ChunkHolder(value, watcher.getCursorPosition());
	}

	/**
	 * This method ensures that the output String has only valid XML unicode
	 * characters as specified by the XML 1.0 standard. For reference, please
	 * see the standard.
	 * 
	 * @param The
	 *            String whose non-valid characters we want to remove.
	 * 
	 * @return The in String, stripped of non-valid characters.
	 */
	static String removeInvalidXMLCharacters(String str) {
		assert str != null;

		StringBuilder out = new StringBuilder(); // Used to hold the output.
		int codePoint; // Used to reference the current character.

		// For test
		// String ss = "\ud801\udc00"; // This is actualy one unicode character,
		// represented by two code units!!!.
		// System.out.println(ss.codePointCount(0, ss.length()));// See: 1
		int i = 0;
		String value = null;
		try {
			// make sure the string contain only UTF-8 characters
			value = new String(str.getBytes("UTF-8"), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// will not happen
			throw new AssertionError("UTF-8 charset is not supported!!!");
		}
		while (i < value.length()) {
			codePoint = value.codePointAt(i); // This is the unicode code of the
			// character.
			if ((codePoint == 0x9)
					|| // Consider testing larger ranges first to
						// improve speed.
					(codePoint == 0xA) || (codePoint == 0xD)
					|| ((codePoint >= 0x20) && (codePoint <= 0xD7FF))
					|| ((codePoint >= 0xE000) && (codePoint <= 0xFFFD))
					|| ((codePoint >= 0x10000) && (codePoint <= 0x10FFFF))) {

				out.append(Character.toChars(codePoint));
			}

			i += Character.charCount(codePoint);
			/*
			 * Increment with the number of code units(java chars) needed to
			 * represent a Unicode char.
			 */
		}
		return out.toString();
	}

	public void initPull() {
		this.lastAccessTime = System.nanoTime();
		if (!isFileCreated()) {
			throw new IllegalStateException("File " + file.getAbsolutePath()
					+ " has not been created yet! Cannot pull.");
		}
		if (watcher == null) {
			init();
		}
	}

	public String getFile() {
		return file.getAbsolutePath();
	}

	public boolean isFileCreated() {
		this.lastAccessTime = System.nanoTime();
		return file.exists();
	}

	public void waitForFile(long maxWaitSeconds) {
		long waited = 0;
		int step = 500;
		while (true) {
			if (isFileCreated()) {
				break;
			}
			try {
				Thread.sleep(step);
				// TODO is this needed? this.lastAccessTime = System.nanoTime();
			} catch (InterruptedException e) {
				// Propagate interruption up the stack trace for anyone
				// interested
				Thread.currentThread().interrupt();
				log.debug("Thread interruped during waiting for file "
						+ file.getAbsolutePath() + " to be created. Message: "
						+ e.getMessage());
				break;
			}
			waited += step;
			if (waited / 1000 >= maxWaitSeconds) {
				break;
			}
		}
	}

	public boolean hasMoreData() throws IOException {
		this.lastAccessTime = System.nanoTime();
		if (!isFileCreated()) {
			throw new IllegalStateException("File " + file.getAbsolutePath()
					+ " has not been created yet! Cannot pull.");
		}
		if (watcher == null) {
			init();
		}
		return watcher.hasMore();
	}

	private synchronized void init() {
		// Check watcher==null is duplicated to avoid adding synchronization to
		// access methods
		if (watcher == null) {
			if (chunkSize < FileWatcher.MIN_CHUNK_SIZE_BYTES) {
				watcher = FileWatcher
						.newProgressWatcher(file.getAbsolutePath());
				log.debug("Init Progress watcher with file: "
						+ file.getAbsolutePath());
			} else {
				watcher = FileWatcher.newFileWatcher(file.getAbsolutePath(),
						chunkSize);
				log.debug("Init File watcher with file: "
						+ file.getAbsolutePath());
			}

		}
	}

	@Override
	public int compareTo(Delayed o) {
		return new Long(this.getDelay(TimeUnit.NANOSECONDS)).compareTo(o
				.getDelay(TimeUnit.NANOSECONDS));
	}

	/*
	 * This must return remaining delay associated with the object!
	 * (non-Javadoc)
	 * 
	 * @see java.util.concurrent.Delayed#getDelay(java.util.concurrent.TimeUnit)
	 */
	@Override
	public long getDelay(final TimeUnit unit) {
		long idleTime = System.nanoTime() - lastAccessTime;
		long delayVal = (delay == 0 ? defaultDelay : delay);
		return unit.convert(delayVal - idleTime, TimeUnit.NANOSECONDS);
	}

	void setDelay(long delay, TimeUnit unit) {
		assert delay > 0;
		this.delay = TimeUnit.NANOSECONDS.convert(delay, unit);
		assert delay < defaultDelay;
	}

	long getDelayValue(TimeUnit unit) {
		return (delay == 0
				? unit.convert(defaultDelay, TimeUnit.NANOSECONDS)
				: unit.convert(delay, TimeUnit.NANOSECONDS));
	}

	public void disconnect() {
		synchronized (this) {
			if (watcher != null) {
				watcher.disconnect();
				watcher = null;
			}
		}
	}

	boolean disconnected() {
		return watcher == null;
	}

	@Override
	public String toString() {
		String value = "File: " + this.file.getAbsolutePath() + "\n";
		value += "Delay (s): " + getDelayValue(TimeUnit.SECONDS) + "\n";
		long exp = getDelay(TimeUnit.MILLISECONDS);
		if (exp > 0) {
			value += "Expire in (ms): " + exp + "\n";
		} else {
			value += "This object has expired" + "\n";
		}
		value += "ChunkSize  " + this.chunkSize + "\n";
		return value;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof FilePuller)) {
			return false;
		}
		FilePuller fp = (FilePuller) obj;
		if (!this.file.equals(fp.file)) {
			return false;
		}
		// Other fields does not matter
		return true;
	}

	@Override
	public int hashCode() {
		return this.file.hashCode();
	}

	public byte getProgress() throws IOException {
		initPull();
		return watcher.getProgress();
	}

}
