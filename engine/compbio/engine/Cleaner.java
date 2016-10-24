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
import java.util.List;

import org.apache.log4j.Logger;

import compbio.engine.client.ConfiguredExecutable;
import compbio.engine.client.PathValidator;
import compbio.engine.local.ExecutableWrapper;

@Deprecated
public class Cleaner {

	private static final Logger log = Logger.getLogger(Cleaner.class);

	/**
	 * This method returns true if all files specified by List files were
	 * successfully removed or there was no files to remove (files list was
	 * empty)
	 * 
	 * @param workDirectory
	 * @param files
	 * @return This method returns true if all files specified by List files
	 *         were successfully removed, false otherwise
	 */
	public static boolean deleteFiles(ConfiguredExecutable<?> exec) {

		if (exec == null) {
			throw new IllegalArgumentException("Executable must be provided!");
		}

		List<String> files = exec.getCreatedFiles();
		for (String fname : files) {
			assert PathValidator.isAbsolutePath(fname) : " Absolute path must be provided but got: "
					+ fname;
			removeFile(fname);
		}
		// Remove process std output and error capture files, do not care
		// whether succeed or not
		// as these are only created for local processes, so may not exist
		removeFile(exec.getWorkDirectory() + File.separator
				+ ExecutableWrapper.PROC_OUT_FILE);
		removeFile(exec.getWorkDirectory() + File.separator
				+ ExecutableWrapper.PROC_ERR_FILE);
		// Remove the task directory if all files were successfully removed
		return removeFile(exec.getWorkDirectory());
	}

	static boolean removeFile(String filename) {
		File outfile = new File(filename);
		boolean success = false;
		if (outfile.exists()) {
			String type = outfile.isDirectory() ? "Directory " : "File ";
			success = outfile.delete();
			if (success) {
				log.trace(type + filename + " was successfully removed");
			} else {
				log.debug("Could not remove "
						+ type
						+ ": "
						+ filename
						+ " reportedly created by executable. Insufficient access right?");
			}
		} else {
			log.debug("File: " + filename
					+ " does not appear ro exist. Could have been removed?");
		}
		return success;
	}

	public static boolean deleteAllFiles(String directory) {
		if (compbio.util.Util.isEmpty(directory)) {
			throw new NullPointerException("Direcotry must be provided! ");
		}
		File rootdir = new File(directory);
		if (!rootdir.exists()) {
			log.error("Directory " + directory
					+ " does not exist. Have been deleted already?");
			return false;
		}
		if (!rootdir.isDirectory()) {
			log.error("Directory is expected by file given! Skipping... ");
			return false;
		}

		File[] files = rootdir.listFiles();
		int deletedCount = 0;
		for (File f : files) {
			if (f.isDirectory()) {
				log.error("Cannot delete subdirecotries! Skipping...");
			} else {
				boolean deleted = f.delete();
				if (deleted) {
					deletedCount++;
				}
			}
		}
		return deletedCount == files.length;
	}

}
