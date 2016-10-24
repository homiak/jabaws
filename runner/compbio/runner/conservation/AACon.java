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

package compbio.runner.conservation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;

import compbio.data.sequence.SMERFSConstraints;
import compbio.data.sequence.ScoreManager;
import compbio.data.sequence.SequenceUtil;
import compbio.engine.client.CommandBuilder;
import compbio.engine.client.Executable;
import compbio.engine.client.SkeletalExecutable;
import compbio.metadata.ResultNotAvailableException;

/**
 * Command line
 * 
 * java -Xmx512 -jar jronn_v3.jar -i=test_seq.txt -n=1 -o=out.txt -s=stat.out
 * 
 * @author pvtroshin
 * 
 */
public class AACon extends SkeletalExecutable<AACon> {

	private static Logger log = Logger.getLogger(AACon.class);

	/**
	 * Number of cores to use, defaults to 1 for local execution or the value of
	 * "jronn.cluster.cpunum" property for cluster execution
	 */
	private int ncoreNumber = 0;

	private final String ncorePrm = "-t=";

	public static final String KEY_VALUE_SEPARATOR = "=";
	public static final String STAT_FILE = "stat.txt";

	private final int windowWidth = SMERFSConstraints.DEFAULT_WINDOW_SIZE;
	private final SMERFSConstraints colScoreMethod = SMERFSConstraints.MID_SCORE;
	private final double gapTreshold = SMERFSConstraints.DEFAULT_GAP_THRESHOLD;

	public AACon() {
		addParameters(Arrays.asList("-jar", getLibPath(), "-d=" + STAT_FILE,
				"-f=RESULT_NO_ALIGNMENT"));
	}
	// HashMap<Method, float[]>
	@Override
	public ScoreManager getResults(String workDirectory)
			throws ResultNotAvailableException {
		ScoreManager annotations = null;
		try {
			InputStream inStream = new FileInputStream(new File(workDirectory,
					getOutput()));
			annotations = ScoreManager.newInstanceSingleSequence(SequenceUtil
					.readAAConResults(inStream));
			inStream.close();
		} catch (FileNotFoundException e) {
			log.error(e.getMessage(), e.getCause());
			throw new ResultNotAvailableException(e);
		} catch (IOException e) {
			log.error(e.getMessage(), e.getCause());
			throw new ResultNotAvailableException(e);
		} catch (NullPointerException e) {
			log.error(e.getMessage(), e.getCause());
			throw new ResultNotAvailableException(e);
		}
		return annotations;
	}

	private static String getLibPath() {

		String settings = ph.getProperty("aacon.jar.file");
		if (compbio.util.Util.isEmpty(settings)) {
			throw new NullPointerException(
					"Please define aacon.jar.file property in Executable.properties file"
							+ "and initialize it with the location of jronn jar file");
		}
		if (new File(settings).isAbsolute()) {
			// the jar can be found so no actions necessary
			// no further actions is necessary
			return settings;
		}
		return compbio.engine.client.Util.convertToAbsolute(settings);
	}

	@Override
	public List<String> getCreatedFiles() {
		return Arrays.asList(getOutput(), getError());
	}

	@Override
	public AACon setInput(String inFile) {
		super.setInput(inFile);
		cbuilder.setParam("-i=" + inFile);
		return this;
	}

	@Override
	public AACon setOutput(String outFile) {
		super.setOutput(outFile);
		cbuilder.setParam("-o=" + outFile);
		return this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Class<AACon> getType() {
		return (Class<AACon>) this.getClass();
	}

	public static String getStatFile() {
		return STAT_FILE;
	}

	public void setNCore(int ncoreNumber) {
		if (ncoreNumber < 1 || ncoreNumber > 100) {
			throw new IndexOutOfBoundsException(
					"Number of cores must be within 1 and 100 ");
		}
		this.ncoreNumber = ncoreNumber;
		cbuilder.setParam(ncorePrm + Integer.toString(getNCore()));
	}

	int getNCore() {
		return ncoreNumber;
	}

	@Override
	public CommandBuilder<AACon> getParameters(ExecProvider provider) {
		// If number of cores is provided, set it for the cluster execution
		// only!
		if (provider == Executable.ExecProvider.Cluster) {
			int cpunum = SkeletalExecutable.getClusterCpuNum(getType());
			cpunum = (cpunum == 0) ? 1 : cpunum;
			setNCore(cpunum);
		} else {
			// Limit number of cores to 1 for ANY execution which does not set
			// Ncores explicitly using setNCore method or is run on local VM
			if (ncoreNumber == 0) {
				setNCore(1);
			}
		}
		return super.getParameters(provider);
	}

}
