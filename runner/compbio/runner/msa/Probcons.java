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
import compbio.engine.client.PipedExecutable;
import compbio.engine.client.SkeletalExecutable;
import compbio.metadata.ResultNotAvailableException;
import compbio.runner.Util;

public class Probcons extends SkeletalExecutable<Probcons>
		implements
			PipedExecutable<Probcons> {

	private static Logger log = Logger.getLogger(Probcons.class);

	private final static String ANNOTATION = "annotation.txt";

	public static final String KEY_VALUE_SEPARATOR = Util.SPACE;

	/**
	 */
	public Probcons() {
		addParameters(Arrays.asList("-v", "-clustalw", "-annot", ANNOTATION));
		/*
		 * Could either have probabilities or the alignment, but not both "-t",
		 * "probabilities"
		 */
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
		return Arrays.asList(getOutput(), ANNOTATION, getError());
	}

	@Override
	public Probcons setInput(String inFile) {
		String input = getInput();
		super.setInput(inFile);
		// TODO replace with setLast
		cbuilder.setParam(inFile);
		return this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Class<Probcons> getType() {
		return (Class<Probcons>) this.getClass();
	}
}
