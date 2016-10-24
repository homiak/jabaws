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
 * DisEMBL.py smooth_frame peak_frame join_frame fold_coils fold_hotloops
 * fold_rem465 sequence_file print
 * 
 * 'A default run would be: ./DisEMBL.py 8 8 4 1.2 1.4 1.2 fasta_file > out'
 * 
 * This version of DisEMBL is 1.4 (latest available for download in Feb 2011)
 * capable of outputting raw values
 * 
 * The values of the parameters are hard coded in DisEMBL.py script.
 * smooth_frame=8 peak_frame=8 join_frame=4 fold_coils=1.2 fold_hotloops=1.4
 * fold_rem465=1.2
 * 
 * Changing these values are not recommended by developers, apart from smoothing
 * window. However, 5 orders of magnitude changes in this parameter does not
 * change the output so allowing this change also seems pointless. Finally, the
 * binary, DisEMBL depends on - Tisean is not happy with arbitruary changes to
 * these values, so changing them can lead to problems.
 * 
 * 
 * This is not a standard DisEMBL! The script has been modified!
 * 
 */
public class Disembl extends SkeletalExecutable<Disembl>
		implements
			PipedExecutable<Disembl> {

	private static Logger log = Logger.getLogger(Disembl.class);

	public static final String KEY_VALUE_SEPARATOR = Util.SPACE;

	/**
	 * For the region to be considered disordered the values must exceed these
	 */
	public final double COILS_EXPECTATION_THRESHOLD = 0.43;
	public final double REM_EXPECTATION_THRESHOLD = 0.5;
	public final double LOOPS_EXPECTATION_THRESHOLD = 0.086;

	/* The parameter list there must not contain same values! */
	public Disembl() {
		// remove default input to prevent it to appear in the parameters list
		// that could happen if the parameters are set first
		// super.setInput("");
	}

	@SuppressWarnings("unchecked")
	@Override
	public ScoreManager getResults(String workDirectory)
			throws ResultNotAvailableException {

		InputStream inStream = null;
		ScoreManager results = null;

		try {
			inStream = new FileInputStream(new File(workDirectory, getOutput()));
			results = ScoreManager.newInstance(SequenceUtil
					.readDisembl(inStream));
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
		log.trace("DRESULTS: " + results);
		return results;
	}
	@Override
	public Disembl setInput(String inFile) {
		super.setInput(inFile);
		cbuilder.setLast(inFile);
		return this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Class<Disembl> getType() {
		return (Class<Disembl>) this.getClass();
	}

}
