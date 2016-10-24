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

import compbio.engine.Cleaner;
import compbio.engine.Configurator;
import compbio.engine.SubmissionManager;
import compbio.engine.client.ConfiguredExecutable;
import compbio.engine.client.Util;
import compbio.metadata.JobStatus;
import compbio.metadata.ResultNotAvailableException;

public final class LocalEngineUtil {

	private static final Logger log = Logger.getLogger(LocalEngineUtil.class);

	/**
	 * Coerce an unchecked Throwable to a RuntimeException or Error
	 * 
	 * @param throwable
	 * @return
	 */
	static RuntimeException launderThrowable(Throwable throwable) {
		if (throwable instanceof RuntimeException) {
			return (RuntimeException) throwable;
		} else if (throwable instanceof Error) {
			throw (Error) throwable;
		} else {
			// Logic error then
			throw new IllegalStateException(
					"Checked exception being thrown and unwrapped by LocalRunner.launderThrowable method",
					throwable);
		}
	}

	public static boolean cancelJob(Future<ConfiguredExecutable<?>> future,
			String workDirectory) {
		compbio.engine.client.Util.writeMarker(workDirectory,
				JobStatus.CANCELLED);
		log.debug("Cancelling local job from work directory " + workDirectory);
		return future.cancel(true);
	}

	public static JobStatus getJobStatus(Future<ConfiguredExecutable<?>> future) {
		// Order is important here as cancelled tasks also considered done!
		if (future == null) {
			throw new NullPointerException("Future must be provided!");
		}
		if (future.isCancelled()) {
			return JobStatus.CANCELLED;
		}
		if (future.isDone()) {
			return JobStatus.FINISHED;
		}
		return JobStatus.RUNNING;
	}

	public static JobStatus getRecordedJobStatus(String jobId) {
		// job has been removed from the task list
		// but there may be status written to the disk
		String workDir = Configurator.getWorkDirectory(jobId);
		if (Util.isMarked(workDir, JobStatus.FINISHED)
				|| Util.isMarked(workDir, JobStatus.COLLECTED)) {
			return JobStatus.FINISHED;
		}
		if (Util.isMarked(workDir, JobStatus.CANCELLED)) {
			return JobStatus.CANCELLED;
		}
		if (Util.isMarked(workDir, JobStatus.FAILED)) {
			return JobStatus.FAILED;
		}
		return JobStatus.UNDEFINED;
	}

	public static boolean cleanup(ConfiguredExecutable<?> confExecutable) {
		if (confExecutable == null) {
			throw new NullPointerException("Future must be provided!");
		}
		return Cleaner.deleteFiles(confExecutable);
	}

	public static ConfiguredExecutable<?> getResults(
			Future<ConfiguredExecutable<?>> future, final String taskId)
			throws ResultNotAvailableException {
		ConfiguredExecutable<?> exec = null;
		try {
			exec = future.get();
			if (exec == null) {
				throw new ResultNotAvailableException(
						"Job return null as a Result! Job work directory is "
								+ Configurator.getWorkDirectory(taskId)
								+ " Job id is " + taskId);
			}
			compbio.engine.client.Util.writeMarker(Configurator
					.getWorkDirectory(taskId), JobStatus.COLLECTED);
		} catch (InterruptedException e) {
			// reassert threads interrupted status
			Thread.currentThread().interrupt();
			compbio.engine.client.Util.writeMarker(Configurator
					.getWorkDirectory(taskId), JobStatus.FAILED);
			// Cancel the job
			log.debug("Cancelling job due to Interruption");
			future.cancel(true);
			// do not clean up leave files untouched
			// this.cleanup(taskId);
		} catch (ExecutionException e) {
			// this.cleanup(taskId);
			compbio.engine.client.Util.writeMarker(Configurator
					.getWorkDirectory(taskId), JobStatus.FAILED);
			log.debug("Job execution exception: " + e.getLocalizedMessage(), e
					.getCause());
			// ExecutionException returned as thus Throwable needs unwrapping
			LocalEngineUtil.launderThrowable(e.getCause());
		} finally {
			future.cancel(true);// harmless if the task already completed
			// whatever happens remove task from list
			SubmissionManager.removeTask(taskId);
		}
		return exec;
	}

}
