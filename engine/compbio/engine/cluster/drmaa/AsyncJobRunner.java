/* Copyright (c) 2009 Peter Troshin
 *  
 *  JAva Bioinformatics Analysis Web Services (JABAWS) @version: 1.0     
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

package compbio.engine.cluster.drmaa;

import org.apache.log4j.Logger;
import org.ggf.drmaa.DrmaaException;

import compbio.engine.AsyncExecutor;
import compbio.engine.Cleaner;
import compbio.engine.Configurator;
import compbio.engine.client.ConfiguredExecutable;
import compbio.engine.client.Util;
import compbio.metadata.JobSubmissionException;
import compbio.metadata.ResultNotAvailableException;

/**
 * Single cluster job runner class
 * 
 * @author pvtroshin
 * @version 1.0 August 2009
 * 
 *          TODO after call to submitJob() no setters really work as the job
 *          template gets deleted, this needs to be taken into account in this
 *          class design!
 */
public class AsyncJobRunner implements AsyncExecutor {

	private static Logger log = Logger.getLogger(AsyncJobRunner.class);

	@Override
	public String submitJob(ConfiguredExecutable<?> executable)
			throws JobSubmissionException {
		JobRunner jr = new JobRunner(executable);
		jr.submitJob(); // ignore cluster job id as it could be retrieved from
		// fs
		return executable.getTaskId();
	}

	@Override
	public boolean cancelJob(String jobId) {
		ClusterSession clustSession = ClusterSession.getInstance();
		return compbio.engine.cluster.drmaa.ClusterUtil.cancelJob(jobId,
				clustSession);
	}

	/**
	 * This will never return clust.engine.JobStatus.CANCELLED as for sun grid
	 * engine cancelled job is the same as failed. Cancelled jobs needs to be
	 * tracked manually!
	 */
	@Override
	public compbio.metadata.JobStatus getJobStatus(String jobId) {
		return JobRunner.getJobStatus(jobId);
	}

	@Override
	public boolean cleanup(String jobId) {
		String workDir = Configurator.getWorkDirectory(jobId);
		return Cleaner.deleteAllFiles(workDir);
	}

	@Override
	public ConfiguredExecutable<?> getResults(String jobId)
			throws ResultNotAvailableException {

		assert Util.isValidJobId(jobId);

		ClusterSession csession = ClusterSession.getInstance();
		ConfiguredExecutable<?> exec;
		try {
			exec = csession.getResults(jobId);
		} catch (DrmaaException e) {
			log.error(e.getLocalizedMessage(), e.getCause());
			throw new ResultNotAvailableException(e);
		}
		return exec;
	}

	@Override
	public String getWorkDirectory(String jobId) {
		return Configurator.getWorkDirectory(jobId);
	}

} // class end
