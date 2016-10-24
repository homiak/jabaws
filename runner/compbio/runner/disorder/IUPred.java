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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import compbio.data.sequence.Score;
import compbio.data.sequence.ScoreManager;
import compbio.data.sequence.SequenceUtil;
import compbio.data.sequence.UnknownFileFormatException;
import compbio.engine.client.SkeletalExecutable;
import compbio.metadata.ResultNotAvailableException;
import compbio.runner.msa.Mafft;

/**
 * iupred sequenceFile <short long glob >
 * 
 * Maximum sequence length is 40000 chars. Single string length max is a 1000
 * chars!
 * 
 */
public class IUPred extends SkeletalExecutable<IUPred> {

	private static Logger log = Logger.getLogger(IUPred.class);
	private static final String GLOB_OUTPUT = "out.glob";
	private static final String SHORT_OUTPUT = "out.short";
	private static final String LONG_OUTPUT = "out.long";

	
	@Override
	@SuppressWarnings("unchecked")
	public ScoreManager getResults(String workDirectory)
			throws ResultNotAvailableException {

		ScoreManager results = null;
		try {
			Map<String, Set<Score>> combined = new TreeMap<String, Set<Score>>();
			
			File shortf = new File(workDirectory, SHORT_OUTPUT);
			if(shortf.exists()) {
				combineScores(combined, SequenceUtil.readIUPred(shortf));
			}
			File longf = new File(workDirectory, LONG_OUTPUT);
			if(longf.exists()) {
				combineScores(combined, SequenceUtil.readIUPred(longf));
			} 
			
			File glob = new File(workDirectory, GLOB_OUTPUT);
			if(glob.exists()) {
				combineScores(combined, SequenceUtil.readIUPred(glob));
			}
			if(combined.isEmpty()) {
				throw new ResultNotAvailableException("Could not find any result " +
						"files for the job: " + workDirectory); 
			}
			results = ScoreManager.newInstance(combined);

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

	void combineScores(Map<String, Set<Score>> combined, Map<String, Score> scores)  { 
		if(scores==null) { 
			return; 
		}
		if (combined.isEmpty()) {
			for (String key : scores.keySet()) {
				Set<Score> allScores = new TreeSet<Score>();
				combined.put(key, allScores);
			}
		}
		for (String key : scores.keySet()) {
			Set<Score> 	allScores= combined.get(key);
			assert allScores!=null; 
			Score score = scores.get(key);
			allScores.add(score);
			combined.put(key, allScores);
		}
	}

	
	
	@Override
	public IUPred setInput(String inFile) {
		super.setInput(inFile);
		cbuilder.setFirst(inFile);
		return this;
	} 

		
	@Override
	public IUPred setOutput(String outFile) {
		log.warn("IUpred output is predefined and cannot be set!"); 
		return this; 
	}

	UnsupportedOperationException newUnsupportedOutputException() { 
		return new UnsupportedOperationException("The outputs from this executable " +
				"are always either of those 3 files (depending on the method called): " + GLOB_OUTPUT + ", " 
				+ SHORT_OUTPUT + ", "+ LONG_OUTPUT);
	}
	@Override
	public String getOutput() {
		log.warn("IUpred output is predefined and is one of the three files " +
				"(depending on the method called): " + GLOB_OUTPUT + ", " 
				+ SHORT_OUTPUT + ", "+ LONG_OUTPUT);
		return LONG_OUTPUT;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Class<IUPred> getType() {
		return (Class<IUPred>) this.getClass();
	}

}
