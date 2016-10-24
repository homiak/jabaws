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

package compbio.runner;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import compbio.data.sequence.Alignment;
import compbio.data.sequence.ClustalAlignmentUtil;
import compbio.data.sequence.FastaSequence;
import compbio.data.sequence.Score;
import compbio.data.sequence.SequenceUtil;
import compbio.data.sequence.UnknownFileFormatException;
import compbio.engine.client.ConfExecutable;
import compbio.engine.client.ConfiguredExecutable;
import compbio.engine.client.Executable;
import compbio.engine.conf.PropertyHelperManager;
import compbio.metadata.JobSubmissionException;
import compbio.metadata.PresetManager;
import compbio.metadata.RunnerConfig;
import compbio.util.PropertyHelper;

public final class Util {

	public static Logger log = Logger.getLogger(Util.class);

	private static final PropertyHelper ph = PropertyHelperManager
			.getPropertyHelper();

	public static final String SPACE = " ";

	public static synchronized <T> RunnerConfig<T> getSupportedOptions(
			Class<? extends Executable<T>> clazz) {
		try {
			return ConfExecutable.getRunnerOptions(clazz);
		} catch (FileNotFoundException e) {
			log.error(
					"Could not load " + clazz + " Parameters !"
							+ e.getMessage(), e.getCause());
		} catch (IOException e) {
			log.error("IO exception while reading " + clazz + " Parameters !"
					+ e.getMessage(), e.getCause());
		}
		return null;
	}

	public static <T> PresetManager<T> getPresets(
			Class<? extends Executable<T>> clazz) {
		try {
			return ConfExecutable.getRunnerPresets(clazz);
		} catch (FileNotFoundException e) {
			log.warn(
					"No presets are found for " + clazz + " executable! "
							+ e.getLocalizedMessage(), e.getCause());
		} catch (IOException e) {
			log.warn("IO exception while reading presents! for " + clazz
					+ " executable! " + e.getLocalizedMessage(), e.getCause());
		}
		return null;
	}

	public static final Alignment readClustalFile(String workDirectory,
			String clustFile) throws UnknownFileFormatException, IOException,
			FileNotFoundException, NullPointerException {
		assert !compbio.util.Util.isEmpty(workDirectory);
		assert !compbio.util.Util.isEmpty(clustFile);
		File cfile = new File(compbio.engine.client.Util.getFullPath(
				workDirectory, clustFile));
		log.trace("CLUSTAL OUTPUT FILE PATH: " + cfile.getAbsolutePath());
		if (!(cfile.exists() && cfile.length() > 0)) {
			throw new FileNotFoundException("Result for the jobId "
					+ workDirectory + " with file name " + clustFile
					+ " is not found!");
		}
		return ClustalAlignmentUtil.readClustalFile(cfile);
	}

	public static final Map<String, Score> readJronnFile(String workDirectory,
			String clustFile) throws UnknownFileFormatException, IOException,
			FileNotFoundException, NullPointerException {
		assert !compbio.util.Util.isEmpty(workDirectory);
		assert !compbio.util.Util.isEmpty(clustFile);
		File cfile = new File(compbio.engine.client.Util.getFullPath(
				workDirectory, clustFile));
		log.trace("Jronn OUTPUT FILE PATH: " + cfile.getAbsolutePath());
		if (!(cfile.exists() && cfile.length() > 0)) {
			throw new FileNotFoundException("Result for the jobId "
					+ workDirectory + " with file name " + clustFile
					+ " is not found!");
		}
		return SequenceUtil.readJRonn(cfile);
	}

	public static void writeInput(List<FastaSequence> sequences,
			ConfiguredExecutable<?> exec) throws JobSubmissionException {

		try {
			File filein = new File(exec.getInput());
			FileOutputStream fout = new FileOutputStream(filein);
			log.debug("File path: " + filein.getAbsolutePath());
			SequenceUtil.writeFasta(fout, sequences);
			fout.close();
		} catch (IOException e) {
			log.error("IOException while writing input file into the disk: "
					+ e.getLocalizedMessage(), e);
			throw new JobSubmissionException(
					"We are sorry by JABAWS server seems to have a problem! "
							+ e.getLocalizedMessage(), e);
		}
	}

}
