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

import java.io.File;
import java.util.Arrays;
import java.util.List;

import javax.jws.WebService;

import org.apache.log4j.Logger;

import compbio.data.msa.JABAService;
import compbio.data.msa.SequenceAnnotation;
import compbio.data.sequence.FastaSequence;
import compbio.engine.Configurator;
import compbio.engine.client.ConfiguredExecutable;
import compbio.metadata.ChunkHolder;
import compbio.metadata.JobSubmissionException;
import compbio.metadata.LimitExceededException;
import compbio.metadata.UnsupportedRuntimeException;
import compbio.runner.conservation.AACon;

@WebService(endpointInterface = "compbio.data.msa.SequenceAnnotation", targetNamespace = JABAService.V2_SERVICE_NAMESPACE, serviceName = "AAConWS")
public class AAConWS extends SequenceAnnotationService<AACon>
		implements
			SequenceAnnotation<AACon> {

	private static Logger log = Logger.getLogger(AAConWS.class);

	public AAConWS() {
		super(new AACon(), log);
	}

	/*
	 * @SuppressWarnings("unchecked") public JalviewAnnotation
	 * getJalviewAnnotation(String jobId) throws ResultNotAvailableException {
	 * MultiAnnotatedSequence<Method> result = getResult(jobId); // TODO //
	 * log(jobId, "getResults"); return result.toJalviewAnnotation(); }
	 */

	@Override
	public String analize(List<FastaSequence> sequences)
			throws UnsupportedRuntimeException, LimitExceededException,
			JobSubmissionException {
		WSUtil.validateAAConInput(sequences);
		ConfiguredExecutable<AACon> confAAcon = init(sequences);

		// set default conservation method to fastest - SHENKIN
		// TODO: This violates encapsulation, should be moved to the runners
		// level.
		confAAcon.addParameters(Arrays.asList("-m=SHENKIN"));
		return WSUtil.analize(sequences, confAAcon, log, "AAConWS analize",
				getLimit(""));
	}

	@Override
	public ChunkHolder pullExecStatistics(String jobId, long position) {
		WSUtil.validateJobId(jobId);
		String file = Configurator.getWorkDirectory(jobId) + File.separator
				+ AACon.getStatFile();
		return WSUtil.pullFile(file, position);
	}

}
