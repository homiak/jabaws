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
import compbio.engine.client.SkeletalExecutable;
import compbio.metadata.ResultNotAvailableException;
import compbio.runner.Util;

public class ClustalW extends SkeletalExecutable<ClustalW> {

	private static Logger log = Logger.getLogger(ClustalW.class);
	private static final String EXEC_STAT_FILE = "stat.log";
	private static final String TREE_FILE_EXT = ".dnd";

	public static final String KEY_VALUE_SEPARATOR = "=";

	public ClustalW() {
		super(KEY_VALUE_SEPARATOR);
		addParameters(Arrays.asList("-OUTORDER=ALIGNED", "-QUIET", "-STATS="
				+ EXEC_STAT_FILE));
		// set default in, outs and err files
		this.setInput(super.inputFile);
		this.setOutput(super.outputFile);
		this.setError(super.errorFile);
	}

	@Override
	public ClustalW setOutput(String outFile) {
		super.setOutput(outFile);
		cbuilder.setParam("-OUTFILE=" + outFile);
		return this;
	}

	@Override
	public ClustalW setInput(String inFile) {
		super.setInput(inFile);
		cbuilder.setParam("-INFILE=" + inFile);
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

	@Override
	public List<String> getCreatedFiles() {
		return Arrays.asList(getOutput(), EXEC_STAT_FILE,
				convertInputNameToTreeName());
	}

	/*
	 * Clustal output tree with same name as input file but .dnd extension e.g.
	 * this methods do similar conversion TO122.fasta -> TO122.dnd or
	 * TO122.fasta.in -> TO122.fasta.dnd It does not seems that there is any
	 * limits on the name length
	 * 
	 * @return
	 */
	private String convertInputNameToTreeName() {
		assert super.getInput() != null;
		int dotIdx = getInput().lastIndexOf(".");
		String treeFileName = "";
		if (dotIdx > 0) {
			treeFileName = getInput().substring(0, dotIdx);
		}
		return treeFileName + TREE_FILE_EXT;
	}

	public static String getStatFile() {
		return EXEC_STAT_FILE;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Class<ClustalW> getType() {
		return (Class<ClustalW>) this.getClass();
	}

}
