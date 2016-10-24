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
package compbio.ws.server;

import java.util.List;

import org.apache.log4j.Logger;

import compbio.data.sequence.FastaSequence;
import compbio.data.sequence.ScoreManager;
import compbio.engine.client.ConfiguredExecutable;
import compbio.engine.client.SkeletalExecutable;
import compbio.metadata.JobSubmissionException;
import compbio.metadata.Limit;
import compbio.metadata.LimitExceededException;
import compbio.metadata.Option;
import compbio.metadata.Preset;
import compbio.metadata.ResultNotAvailableException;
import compbio.metadata.UnsupportedRuntimeException;
import compbio.metadata.WrongParameterException;
import compbio.runner.conservation.AACon;

/**
 * Common methods for all SequenceAnnotation web services
 * 
 * @author pvtroshin
 * 
 * @param <T>
 * 
 * @version 1.0 June 2011
 * @since 2.0
 */
public abstract class SequenceAnnotationService<T> extends GenericMetadataService {

	/*
	 * FIXME - instances of the Runner (?) and their types should be defined in
	 * Executable IF
	 */
	SequenceAnnotationService(SkeletalExecutable<T> exec, Logger log) {
		super(exec, log);
	}

	public ScoreManager getAnnotation(String jobId)
			throws ResultNotAvailableException {
		return WSUtil.getAnnotation(jobId, log);
	}

	public String analize(List<FastaSequence> sequences)
			throws UnsupportedRuntimeException, LimitExceededException,
			JobSubmissionException {
		WSUtil.validateFastaInput(sequences);
		ConfiguredExecutable<T> confIUPred = init(sequences);
		return WSUtil.analize(sequences, confIUPred, log, "analize",
				getLimit(""));
	}

	public String customAnalize(List<FastaSequence> sequences,
			List<Option<T>> options) throws UnsupportedRuntimeException,
			LimitExceededException, JobSubmissionException,
			WrongParameterException {
		WSUtil.validateAAConInput(sequences);
		ConfiguredExecutable<T> confAACon = init(sequences);
		// Could not do that! Space separated values
		// will all be treated as keys! thus duplicates removed
		// String params = cbuilder.getCommand();
		List<String> params = WSUtil.getCommands(options,
				AACon.KEY_VALUE_SEPARATOR);
		confAACon.addParameters(params);
		return WSUtil.analize(sequences, confAACon, log, "customAnalize",
				getLimit(""));
	}

	public String presetAnalize(List<FastaSequence> sequences, Preset<T> preset)
			throws UnsupportedRuntimeException, LimitExceededException,
			JobSubmissionException, WrongParameterException {
		WSUtil.validateAAConInput(sequences);
		if (preset == null) {
			throw new WrongParameterException("Preset must be provided!");
		}
		ConfiguredExecutable<T> confAAcon = init(sequences);
		confAAcon.addParameters(preset.getOptions());
		Limit<T> limit = getLimit(preset.getName());
		return WSUtil
				.analize(sequences, confAAcon, log, "presetAnalize", limit);
	}

}
