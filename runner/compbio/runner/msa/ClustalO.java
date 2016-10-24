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

package compbio.runner.msa;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;

import org.apache.log4j.Logger;

import compbio.data.sequence.Alignment;
import compbio.data.sequence.UnknownFileFormatException;
import compbio.engine.client.CommandBuilder;
import compbio.engine.client.Executable;
import compbio.engine.client.SkeletalExecutable;
import compbio.engine.client.Executable.ExecProvider;
import compbio.metadata.ResultNotAvailableException;
import compbio.runner.Util;

public class ClustalO extends SkeletalExecutable<ClustalO> {

	private static Logger log = Logger.getLogger(ClustalO.class);
	private static final String EXEC_STAT_FILE = "stat.log";
	

	public static final String KEY_VALUE_SEPARATOR = "=";
	
	/*
	 * Number of cores parameter name
	 */
	private final static String ncorePrm = "--threads";

	/**
	 * Number of cores to use, defaults to 1 for local execution or the value of
	 * "tcoffee.cluster.cpunum" property for cluster execution
	 */
	private int ncoreNumber = 0;

	
	/**
	 * --threads=<n> Number of processors to use
	 * 
	 * -l, --log=<file> Log all non-essential output to this file
	 */
	public ClustalO() {
		super(KEY_VALUE_SEPARATOR);
		addParameters(Arrays.asList("--outfmt=clustal", "-v", "--log="
				+ EXEC_STAT_FILE));
		// set default in, outs and err files
		this.setInput(super.inputFile);
		this.setOutput(super.outputFile);
		this.setError(super.errorFile);
	}

	@Override
	public ClustalO setOutput(String outFile) {
		super.setOutput(outFile);
		cbuilder.setParam("--outfile=" + outFile);
		return this;
	}

	@Override
	public ClustalO setInput(String inFile) {
		super.setInput(inFile);
		cbuilder.setParam("--infile=" + inFile);
		return this;
	}

	@SuppressWarnings("unchecked")
	public Alignment getResults(String workDirectory)
			throws ResultNotAvailableException {
		try {
			return Util.readClustalFile(workDirectory, getOutput());
		} catch (FileNotFoundException e) {
			log.error(e.getMessage(), e.getCause());
			throw new ResultNotAvailableException(e);
		} catch (IOException e) {
			log.error(e.getMessage(), e.getCause());
			throw new ResultNotAvailableException(e);
		} catch (UnknownFileFormatException e) {
			log.error(e.getMessage(), e.getCause());
			throw new ResultNotAvailableException(e);
		} catch (NullPointerException e) {
			log.error(e.getMessage(), e.getCause());
			throw new ResultNotAvailableException(e);
		}
	}

	public static String getStatFile() {
		return EXEC_STAT_FILE;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Class<ClustalO> getType() {
		return (Class<ClustalO>) this.getClass();
	}
	
	@Override
	public CommandBuilder<ClustalO> getParameters(ExecProvider provider) {
		// Limit number of cores to 1 for ANY execution which does not set
		// Ncores explicitly using setNCore method
		if (ncoreNumber == 0) {
			setNCore(1);
		}
		if (provider == Executable.ExecProvider.Cluster) {
			int cpunum = SkeletalExecutable.getClusterCpuNum(getType());
			if (cpunum != 0) {
				setNCore(cpunum);
			} 
		}
		return super.getParameters(provider);
	}
	
	public void setNCore(int ncoreNumber) {
		if (ncoreNumber < 1 || ncoreNumber > 100) {
			throw new IndexOutOfBoundsException(
					"Number of cores must be within 1 and 100 ");
		}
		this.ncoreNumber = ncoreNumber;
		cbuilder.setParam(ncorePrm, Integer.toString(getNCore()));
	}

	int getNCore() {
		return ncoreNumber;
	}


}
