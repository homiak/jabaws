/*
 * Copyright (c) 2011 Peter Troshin JAva Bioinformatics Analysis Web Services
 * (JABAWS) @version: 2.0 This library is free software; you can redistribute it
 * and/or modify it under the terms of the Apache License version 2 as published
 * by the Apache Software Foundation This library is distributed in the hope
 * that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * Apache License for more details. A copy of the license is in
 * apache_license.txt. It is also available here:
 * @see: http://www.apache.org/licenses/LICENSE-2.0.txt Any republication or
 * derived work distributed in source code form must include this copyright and
 * license notice.
 */

package compbio.runner.disorder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.apache.log4j.Logger;

import compbio.data.sequence.ScoreManager;
import compbio.data.sequence.SequenceUtil;
import compbio.data.sequence.UnknownFileFormatException;
import compbio.engine.client.PipedExecutable;
import compbio.engine.client.SkeletalExecutable;
import compbio.metadata.ResultNotAvailableException;
import compbio.runner.Util;

/**
 * ./GlobPipe.py SmoothFrame DOMjoinFrame DOMpeakFrame DISjoinFrame DISpeakFrame
 * 
 * FASTAfile' Optimised for ELM: ./GlobPlot.py 10 8 75 8 8 sequence_file'
 * Webserver settings: ./GlobPlot.py 10 15 74 4 5 sequence_file'
 * 
 * Hard-coded values are 10 15 74 4 5.
 * 
 * Changing these values are not recommended by developers, apart from smoothing
 * window. However, the binary, GlobPlot depends on - Tisean which is not happy
 * with arbitrary changes to these values, so changing them can lead to
 * problems. May be we can offer preset?
 * 
 * This is not a standard GlobPlot! The script has been modified!
 * 
 */
public class GlobPlot extends SkeletalExecutable<GlobPlot>
		implements
			PipedExecutable<GlobPlot> {

	private static Logger log = Logger.getLogger(GlobPlot.class);

	public static final String KEY_VALUE_SEPARATOR = Util.SPACE;

	/* The parameter list there must not contain same values! */
	public GlobPlot() {
		// remove default input to prevent it to appear in the parameters list
		// that could happen if the parameters are set first
		// super.setInput("");
	}

	@Override
	@SuppressWarnings("unchecked")
	public ScoreManager getResults(String workDirectory)
			throws ResultNotAvailableException {

		InputStream inStream = null;
		ScoreManager results = null;
		// How about getting ranges?
		try {
			inStream = new FileInputStream(new File(workDirectory, getOutput()));
			results = ScoreManager.newInstance(SequenceUtil
					.readGlobPlot(inStream));
			inStream.close();
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

		return results;
	}

	@Override
	public GlobPlot setInput(String inFile) {
		super.setInput(inFile);
		cbuilder.setLast(inFile);
		return this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Class<GlobPlot> getType() {
		return (Class<GlobPlot>) this.getClass();
	}

}
