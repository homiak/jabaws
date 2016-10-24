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
import java.util.List;

import org.apache.log4j.Logger;

import compbio.data.sequence.Alignment;
import compbio.data.sequence.UnknownFileFormatException;
import compbio.engine.client.CommandBuilder;
import compbio.engine.client.Executable;
import compbio.engine.client.PipedExecutable;
import compbio.engine.client.SkeletalExecutable;
import compbio.engine.conf.PropertyHelperManager;
import compbio.metadata.ResultNotAvailableException;
import compbio.runner.Util;
import compbio.util.PropertyHelper;

public class Tcoffee extends SkeletalExecutable<Tcoffee>
		implements
			PipedExecutable<Tcoffee> {

	private static Logger log = Logger.getLogger(Tcoffee.class);

	private static PropertyHelper ph = PropertyHelperManager
			.getPropertyHelper();

	public static final String KEY_VALUE_SEPARATOR = "=";

	/**
	 * Number of cores to use, defaults to 1 for local execution or the value of
	 * "tcoffee.cluster.cpunum" property for cluster execution
	 */
	private int ncoreNumber = 0;

	/*
	 * Number of cores parameter name
	 */
	private final static String ncorePrm = "-n_core";

	/**
	 */
	public Tcoffee() {
		super(KEY_VALUE_SEPARATOR);
		/*
		 * Use "-quiet" to disable sdtout and progress to stderr inorder=input -
		 * prevent t-coffee from sorting sequences
		 */
		addParameters(Arrays.asList("-output=clustalw"));
		setInput(super.inputFile);
	}

	@Override
	public Tcoffee setInput(String inFile) {
		super.setInput(inFile);
		cbuilder.setParam("-seq", inFile);
		return this;
	}

	@SuppressWarnings("unchecked")
	@Override
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

	@Override
	public List<String> getCreatedFiles() {
		return Arrays.asList(getOutput());
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

	@Override
	public CommandBuilder<Tcoffee> getParameters(ExecProvider provider) {
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

	@SuppressWarnings("unchecked")
	@Override
	public Class<Tcoffee> getType() {
		return (Class<Tcoffee>) this.getClass();
	}
}
