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

package compbio.engine.client;

import java.io.File;
import java.util.List;

import compbio.util.Util;

public final class PathValidator {

	public static boolean isValidExecutable(String command) {
		if (Util.isEmpty(command)) {
			return false;
		}
		File exec = new File(command);
		if (!exec.exists()) {
			return false;
		}
		return exec.canExecute();
	}

	public static void validateExecutable(String command)
			throws IllegalArgumentException {
		if (!PathValidator.isValidExecutable(command)) {
			throw new IllegalArgumentException(
					"External executable: "
							+ command
							+ " could not be found or executed. Under *nix systems check that the executable flag is set!");
		}
	}

	/*
	 * TODO move to Utils
	 */
	public static boolean isValidDirectory(String directory) {
		if (Util.isEmpty(directory)) {
			return false;
		}
		File exec = new File(directory);
		return exec.exists() && exec.isDirectory() && exec.canRead();
	}

	/**
	 * 
	 * @param filenames
	 * @param type
	 *            - merely a string to be added to error message to explain what
	 *            type of files are lacking
	 */
	public static void validatePathNames(List<String> filenames, String type)
			throws IllegalArgumentException {
		for (String filename : filenames) {
			if (isAbsolutePath(filename)) {
				throw new IllegalArgumentException(
						"Working directory cannot be set as absolute paths are "
								+ "defined for at least some of the "
								+ type
								+ " files! This is not advisable. Violating path is : "
								+ filename);
			}
		}
	}

	/**
	 * Whether a certain path is absolute or not is operation system dependent!
	 * 
	 * @param path
	 * @return true is the path is absolute, false otherwise
	 */
	public static boolean isAbsolutePath(String path) {
		assert path != null : "Path is NULL! Not NULL path expected";
		return new File(path).isAbsolute();
	}

	public static void validateDirectory(String workDirectory)
			throws IllegalArgumentException {
		if (!PathValidator.isValidDirectory(workDirectory)) {
			throw new IllegalArgumentException("Working directory: "
					+ workDirectory + " is inaccessible or does not exist!");
		}
	}

}
