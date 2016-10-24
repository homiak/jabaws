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

import java.io.IOException;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;

import compbio.engine.SyncExecutor;
import compbio.engine.client.ConfiguredExecutable;
import compbio.metadata.JobExecutionException;
import compbio.metadata.JobStatus;
import compbio.metadata.JobSubmissionException;
import compbio.metadata.ResultNotAvailableException;

public final class LocalRunner implements SyncExecutor {

	private static final Logger log = Logger.getLogger(LocalRunner.class);
	/*
	 * If is not advisable to start threads statically, thus this field is not
	 * static
	 */
	private final ExecutorService executor;
	private final ConfiguredExecutable<?> executable;
	private Future<ConfiguredExecutable<?>> future;
	private final String workDirectory;

	public LocalRunner(ConfiguredExecutable<?> executable) {
		if (executable == null) {
			throw new IllegalArgumentException(
					"Executable value is NULL. Executable must be provided!");
		}
		this.executor = LocalExecutorService.getExecutor();
		this.executable = executable;
		this.workDirectory = executable.getWorkDirectory();
		// Save run configuration
		try {
			executable.saveRunConfiguration();
		} catch (IOException e) {
			log.error("Could not save run configuration! " + e.getMessage(), e
					.getCause());
		}
	}

	@Override
	public String getWorkDirectory() {
		return this.workDirectory;
	}

	@Override
	public boolean cancelJob() {
		return LocalEngineUtil.cancelJob(future, getWorkDirectory());
	}

	Future<ConfiguredExecutable<?>> getFuture() {
		return this.future;
	}

	@Override
	public JobStatus getJobStatus() {
		if (future == null) {
			return LocalEngineUtil.getRecordedJobStatus(executable.getTaskId());
		}
		return LocalEngineUtil.getJobStatus(future);
	}

	@Override
	public void executeJob() throws JobSubmissionException {
		ExecutableWrapper ewrapper = new ExecutableWrapper(executable,
				workDirectory);
		this.future = executor.submit(ewrapper);
	}

	/**
	 * @throws CancellationException
	 */
	@Override
	public ConfiguredExecutable<?> waitForResult() throws JobExecutionException {
		try {
			return LocalEngineUtil.getResults(future, executable.getTaskId());
		} catch (ResultNotAvailableException e) {
			throw new JobExecutionException(e);
		}
	}

	@Override
	public boolean cleanup() {
		return LocalEngineUtil.cleanup(executable);
	}

}
