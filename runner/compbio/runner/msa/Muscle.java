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

import javax.xml.bind.annotation.XmlTransient;

import org.apache.log4j.Logger;

import compbio.data.sequence.Alignment;
import compbio.data.sequence.UnknownFileFormatException;
import compbio.engine.client.SkeletalExecutable;
import compbio.metadata.ResultNotAvailableException;
import compbio.runner.Util;

public class Muscle extends SkeletalExecutable<Muscle> {

	/*
	 * Tell JAXB to ignore this while marshalling
	 */
	@XmlTransient
	private static Logger log = Logger.getLogger(Muscle.class);

	private static final String EXEC_STAT_FILE = "stat.log";

	public static final String KEY_VALUE_SEPARATOR = Util.SPACE;

	/**
	 * Default options are
	 * 
	 * -clwstrict - write output in clustal format
	 * 
	 */
	public Muscle() {
		/*
		 * The –quiet command-line option disables writing progress messages to
		 * standard error. If the –verbose command-line option is specified, a
		 * progress message will be written to the log file when each iteration
		 * completes. So –quiet and –verbose are not contradictory."-quiet",
		 * "-verbose"
		 */
		addParameters(Arrays.asList("-clwstrict", "-quiet", "-verbose",
				"-nocore"));
		cbuilder.setParam("-log", EXEC_STAT_FILE);
	}

	@Override
	public Muscle setOutput(String outFile) {
		super.setOutput(outFile);
		cbuilder.setParam("-out", outFile);
		return this;
	}

	@Override
	public Muscle setInput(String inFile) {
		super.setInput(inFile);
		cbuilder.setParam("-in", inFile);
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
		return Arrays.asList(getOutput(), EXEC_STAT_FILE);
	}

	public static String getStatFile() {
		return EXEC_STAT_FILE;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Class<Muscle> getType() {
		return (Class<Muscle>) this.getClass();
	}

}
