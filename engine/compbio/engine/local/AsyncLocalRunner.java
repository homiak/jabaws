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

package compbio.engine.local;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;

import compbio.engine.AsyncExecutor;
import compbio.engine.Configurator;
import compbio.engine.SubmissionManager;
import compbio.engine.client.ConfiguredExecutable;
import compbio.engine.client.Util;
import compbio.metadata.JobStatus;
import compbio.metadata.JobSubmissionException;
import compbio.metadata.ResultNotAvailableException;

public final class AsyncLocalRunner implements AsyncExecutor {

	private static final Logger log = Logger.getLogger(AsyncLocalRunner.class);

	@Override
	public String getWorkDirectory(String jobId) {
		return Configurator.getWorkDirectory(jobId);
	}

	@Override
	public boolean cancelJob(String jobId) {
		Future<ConfiguredExecutable<?>> future = SubmissionManager
				.getTask(jobId);
		// The job has already finished or cancelled.
		if (future == null) {
			log.debug("Did not find future for local job "
					+ jobId
					+ " will not cancel it. Perhaps it has finished or cancelled already.");
			return false;
		}
		LocalEngineUtil.cancelJob(future, getWorkDirectory(jobId));
		return future.cancel(true);
	}

	@Override
	public JobStatus getJobStatus(String jobId) {
		Future<ConfiguredExecutable<?>> future = SubmissionManager
				.getTask(jobId);
		if (future == null) {
			return LocalEngineUtil.getRecordedJobStatus(jobId);
		}
		return LocalEngineUtil.getJobStatus(future);
	}

	@Override
	public String submitJob(ConfiguredExecutable<?> executable)
			throws JobSubmissionException {
		if (executable == null) {
			throw new NullPointerException("Executable expected!");
		}
		LocalRunner lrunner = new LocalRunner(executable);
		lrunner.executeJob();
		Future<ConfiguredExecutable<?>> future = lrunner.getFuture();

		if (future == null) {
			throw new RuntimeException("Future is NULL for executable "
					+ executable);
		}
		SubmissionManager.addTask(executable, future);
		return executable.getTaskId();
	}

	/**
	 * 
	 * @param jobId
	 * @return true if all files were removed, false otherwise
	 */
	@Override
	public boolean cleanup(String jobId) {
		Future<ConfiguredExecutable<?>> future = SubmissionManager
				.getTask(jobId);
		ConfiguredExecutable<?> cexec = null;
		try {
			cexec = future.get();
		} catch (InterruptedException e) {
			log.error("Cannot clean up as calculation was not completed!"
					+ e.getLocalizedMessage());
		} catch (ExecutionException e) {
			log.error("Cannot clean up due to ExecutionException "
					+ e.getLocalizedMessage());
		}
		if (cexec == null) {
			return false;
		}
		return LocalEngineUtil.cleanup(cexec);
	}

	@Override
	public ConfiguredExecutable<?> getResults(String taskId)
			throws ResultNotAvailableException {
		if (!Util.isValidJobId(taskId)) {
			// TODO should I be throwing something else?
			throw new IllegalArgumentException(taskId);
		}
		Future<ConfiguredExecutable<?>> futureExec = SubmissionManager
				.getTask(taskId);
		if (futureExec == null) {
			// If task was not find in the list of jobs, than it must have been
			// collected already
			// Resurrect the job to find out there the output is
			ConfiguredExecutable<?> exec = compbio.engine.client.Util
					.loadExecutable(taskId);
			return exec;
		}
		return LocalEngineUtil.getResults(futureExec, taskId);
	}

}
