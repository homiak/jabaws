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

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import compbio.data.sequence.FastaSequence;
import compbio.data.sequence.ScoreManager;
import compbio.engine.AsyncExecutor;
import compbio.engine.Configurator;
import compbio.engine.ProgressGetter;
import compbio.engine.client.ConfiguredExecutable;
import compbio.metadata.ChunkHolder;
import compbio.metadata.JobStatus;
import compbio.metadata.JobSubmissionException;
import compbio.metadata.Limit;
import compbio.metadata.LimitExceededException;
import compbio.metadata.Option;
import compbio.metadata.ResultNotAvailableException;
import compbio.ws.client.Services;
import compbio.ws.client.ServicesUtil;

public final class WSUtil {

	public static final void validateJobId(String jobId)
			throws InvalidParameterException {
		if (!compbio.engine.client.Util.isValidJobId(jobId)) {
			throw new InvalidParameterException(
					"JobId is not provided or cannot be recognised! Given value: "
							+ jobId);
		}
	}

	public static final void validateFastaInput(List<FastaSequence> sequences)
			throws JobSubmissionException {
		if (sequences == null || sequences.isEmpty()) {
			throw new JobSubmissionException(
					"List of fasta sequences required but not provided! ");
		}
		Set<String> names = new HashSet<String>();
		for (FastaSequence fs : sequences) {
			boolean unique = names.add(fs.getId());
			if (!unique) {
				throw new JobSubmissionException(
						"Input sequences must have unique names! \n"
								+ "Sequence " + fs.getId() + " is a duplicate!");
			}
			if (fs.getLength() == 0) {
				throw new JobSubmissionException(
						"Sequence must not be empty! Sequence: " + fs.getId()
								+ " was empty");
			}
		}
	}

	public static JobStatus getJobStatus(String jobId) {
		AsyncExecutor asyncEngine = Configurator.getAsyncEngine(jobId);
		return asyncEngine.getJobStatus(jobId);
	}

	public static ChunkHolder pullFile(String file, long position) {
		return ProgressGetter.pull(file, position);
	}

	public static byte getProgress(String jobId) {
		throw new UnsupportedOperationException();
	}

	public static AsyncExecutor getEngine(ConfiguredExecutable<?> confClustal) {
		assert confClustal != null;
		return Configurator.getAsyncEngine(confClustal);
	}

	public static boolean cancelJob(String jobId) {
		AsyncExecutor asyncEngine = Configurator.getAsyncEngine(jobId);
		return asyncEngine.cancelJob(jobId);
	}

	public static <T> String align(List<FastaSequence> sequences,
			ConfiguredExecutable<T> confExec, Logger logger,
			String callingMethod, Limit<T> limit)
			throws LimitExceededException, JobSubmissionException {

		if (limit != null && limit.isExceeded(sequences)) {
			throw LimitExceededException.newLimitExceeded(limit, sequences);
		}
		compbio.runner.Util.writeInput(sequences, confExec);
		AsyncExecutor engine = Configurator.getAsyncEngine(confExec);
		String jobId = engine.submitJob(confExec);
		reportUsage(confExec, logger);
		return jobId;
	}

	static <T> void reportUsage(ConfiguredExecutable<T> confExec, Logger logger) {
		if (GAUtils.IS_GA_ENABLED) {
			Services service = ServicesUtil.getServiceByRunner(confExec
					.getExecutable().getClass());
			GAUtils.reportUsage(service);
			logger.info("Reporting GA usage for " + service);
		}
	}

	public static <T> String analize(List<FastaSequence> sequences,
			ConfiguredExecutable<T> confExec, Logger log, String method,
			Limit<T> limit) throws JobSubmissionException {
		if (limit != null && limit.isExceeded(sequences)) {
			throw LimitExceededException.newLimitExceeded(limit, sequences);
		}
		log.debug("Method: " + method + " with task: " + confExec.getTaskId());

		compbio.runner.Util.writeInput(sequences, confExec);
		AsyncExecutor engine = Configurator.getAsyncEngine(confExec);
		String jobId = engine.submitJob(confExec);
		reportUsage(confExec, log);
		return jobId;
	}

	/*
	 * TODO Rewrite using purely CommandBuilder. This is breaking encapsulation
	 */
	public static final <T> List<String> getCommands(List<Option<T>> options,
			String keyValueSeparator) {
		List<String> oList = new ArrayList<String>();
		for (Option<T> o : options) {
			oList.add(o.toCommand(keyValueSeparator));
		}
		return oList;
	}

	public static void validateAAConInput(List<FastaSequence> sequences)
			throws JobSubmissionException {
		validateFastaInput(sequences);
		int len = 0;
		for (FastaSequence fs : sequences) {
			if (len == 0) {
				len = fs.getLength();
				continue;
			}
			if (fs.getLength() != len) {
				throw new JobSubmissionException(
						"All sequences must be of the same length. Please align "
								+ "the sequences prior to submission! The first sequence length is : "
								+ len + " but the sequence '" + fs.getId()
								+ "' length is " + fs.getLength());
			}
		}
	}

	public static <T> ScoreManager getAnnotation(String jobId, Logger log)
			throws ResultNotAvailableException {
		WSUtil.validateJobId(jobId);
		AsyncExecutor asyncEngine = Configurator.getAsyncEngine(jobId);
		ConfiguredExecutable<T> aacon = (ConfiguredExecutable<T>) asyncEngine
				.getResults(jobId);
		ScoreManager mas = aacon.getResults();
		log.trace(jobId + " getConservation : " + mas);
		return mas;
	}

	/*
	 * UNUSED
	 * 
	 * @SuppressWarnings("unchecked") static <T> LimitsManager<T>
	 * getLimits(Class<? extends Executable<T>> clazz, WebServiceContext
	 * wsContext) {
	 * 
	 * String LIMIT_KEY = CACHE_KEY + clazz.getCanonicalName(); LimitsManager<T>
	 * limit = (LimitsManager<T>) getObjectFromApplContext( LIMIT_KEY,
	 * wsContext); if (limit == null) { synchronized (WSUtil.class) { limit =
	 * (LimitsManager<T>) getObjectFromApplContext(LIMIT_KEY, wsContext); if
	 * (limit == null) { limit = compbio.runner.Util
	 * .getLimits((Class<Executable<T>>) clazz);
	 * addObjectToApplContext(wsContext, LIMIT_KEY, limit); } } } return limit;
	 * }
	 * 
	 * static void addObjectToApplContext(WebServiceContext wsContext, String
	 * objKey, Object obj) { assert !Util.isEmpty(objKey) :
	 * "Key for the object must not be empty! "; assert wsContext != null;
	 * 
	 * ServletContext ctx = ((javax.servlet.ServletContext) wsContext
	 * .getMessageContext().get(MessageContext. SERVLET_CONTEXT)); assert ctx !=
	 * null; log.debug("Adding object with key '" + objKey + "' and value '" +
	 * obj + "' to the application context"); ctx.setAttribute(objKey, obj); }
	 * static Object getObjectFromApplContext(String objKey, WebServiceContext
	 * wsContext) { assert !Util.isEmpty(objKey) :
	 * "Key for the object must not be empty! "; assert wsContext != null;
	 * 
	 * ServletContext ctx = ((javax.servlet.ServletContext) wsContext
	 * .getMessageContext().get(MessageContext. SERVLET_CONTEXT)); Object obj =
	 * ctx.getAttribute(objKey); log.trace("Retrieving object with key '" +
	 * objKey + "' and value '" + obj + "' from the application context");
	 * return obj; }
	 */
}
