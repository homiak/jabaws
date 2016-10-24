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

package compbio.engine;

import java.io.File;
import java.security.InvalidParameterException;
import java.util.List;

import org.apache.log4j.Logger;

import compbio.data.sequence.FastaSequence;
import compbio.engine.client.ConfExecutable;
import compbio.engine.client.ConfiguredExecutable;
import compbio.engine.client.Executable;
import compbio.engine.client.PathValidator;
import compbio.engine.cluster.drmaa.AsyncJobRunner;
import compbio.engine.cluster.drmaa.JobRunner;
import compbio.engine.conf.DirectoryManager;
import compbio.engine.conf.PropertyHelperManager;
import compbio.engine.local.AsyncLocalRunner;
import compbio.engine.local.LocalRunner;
import compbio.metadata.JobSubmissionException;
import compbio.util.PropertyHelper;
import compbio.util.SysPrefs;
import compbio.util.Util;

public class Configurator {

	private static Logger log = Logger.getLogger(Configurator.class);

	private static final PropertyHelper ph = PropertyHelperManager
			.getPropertyHelper();

	public static final boolean IS_LOCAL_ENGINE_ENABLED = initBooleanValue("engine.local.enable");
	public static final boolean IS_CLUSTER_ENGINE_ENABLED = initBooleanValue("engine.cluster.enable");

	public final static String LOCAL_WORK_DIRECTORY = initLocalDirectory();
	public final static String CLUSTER_WORK_DIRECTORY = initClusterWorkDirectory();

	private static boolean initBooleanValue(String key) {
		assert key != null;
		String status = ph.getProperty(key);
		log.debug("Loading property: " + key + " with value: " + status);
		if (Util.isEmpty(status)) {
			return false;
		}
		return new Boolean(status.trim()).booleanValue();
	}

	private static String initClusterWorkDirectory() {
		String tmpDir = null;
		if (IS_CLUSTER_ENGINE_ENABLED) {
			tmpDir = ph.getProperty("cluster.tmp.directory");
			if (!Util.isEmpty(tmpDir)) {
				tmpDir = tmpDir.trim();
			} else {
				throw new RuntimeException(
						"Cluster work directory must be provided! ");
			}
			if (LOCAL_WORK_DIRECTORY != null
					&& LOCAL_WORK_DIRECTORY.equals(CLUSTER_WORK_DIRECTORY)) {
				throw new InvalidParameterException(
						"Cluster engine output directory must be different of that for local engine!");
			}
		}
		return tmpDir;
	}

	private static String initLocalDirectory() {
		String tmp_dir = ph.getProperty("local.tmp.directory");
		// Use system temp directory is local.tmp.directory is not defined
		if (Util.isEmpty(tmp_dir)) {
			tmp_dir = SysPrefs.getSystemTmpDir();
			log.debug("local.tmp.directory is not defined using system tmp: "
					+ tmp_dir);
		}
		if (!PathValidator.isAbsolutePath(tmp_dir)) {
			log.debug("local.tmp.directory path is relative! " + tmp_dir);
			tmp_dir = compbio.engine.client.Util.convertToAbsolute(tmp_dir);
			log.debug("local.tmp.directory path changed to absolute: "
					+ tmp_dir);
		}
		return tmp_dir.trim();
	}

	/**
	 * Depending on the values defined in the properties
	 * (engine.cluster.enable=true and engine.local.enable=true) return either
	 * Cluster job submission engine {@link #JobRunner} or local job submission
	 * engine {@link #LocalRunner} If both engines enabled than ask
	 * {@link LoadBalancer} for an engine. This method will fall back and return
	 * local engine if
	 * 
	 * 1) No engines are defined in the properties or they have been defined
	 * incorrectly
	 * 
	 * 2) Execution environment is Windows as the system cannot really run
	 * cluster submission from windows
	 * 
	 * @param executable
	 * @return SyncExecutor backed up by either cluster or local engines
	 * @throws JobSubmissionException
	 */
	static Executable.ExecProvider getExecProvider(
			ConfiguredExecutable<?> executable, List<FastaSequence> dataSet)
			throws JobSubmissionException {
		// Look where executable claims to be executed
		Executable.ExecProvider provider = executable.getSupportedRuntimes();
		if (!IS_CLUSTER_ENGINE_ENABLED && !IS_LOCAL_ENGINE_ENABLED) {
			// Both engines disabled!
			throw new RuntimeException(
					"Both engines are disabled! "
							+ "Check conf/Engine.cluster.properties and conf/Engine.local.properties. "
							+ "At least one engine must be enabled!");
		}
		if (provider == Executable.ExecProvider.Local) {
			if (IS_LOCAL_ENGINE_ENABLED) {
				return Executable.ExecProvider.Local;
			} else {
				throw new JobSubmissionException(
						"Executable can be executed only on locally, but local engine is disabled!");
			}
		}
		if (provider == Executable.ExecProvider.Cluster) {
			if (IS_CLUSTER_ENGINE_ENABLED) {
				return Executable.ExecProvider.Cluster;
			} else {
				throw new JobSubmissionException(
						"Executable can be executed only on the cluster, but cluster engine is disabled!");
			}
		}
		// We are here if executable can be executed on both Cluster and Local
		// engines
		// i.e. provider = Any
		// If we still here executable supports All exec environments
		// Check whether we work on windows
		if (SysPrefs.isWindows) {
			// no matter what the settings are, we cannot send tasks to the
			// cluster from windows env
			return Executable.ExecProvider.Local;
		}
		// Now if both engines are configured that load balance them
		if (IS_CLUSTER_ENGINE_ENABLED && IS_LOCAL_ENGINE_ENABLED) {
			// If the dataset is NULL than base a decision on local engine load
			// only
			if (dataSet == null) {
				return LoadBalancer.getEngine(executable);
			}
			// If the dataset is provided, consider it
			// This should be the main root for any load balancing
			// configurations
			return LoadBalancer.getEngine(executable, dataSet);
		} else if (IS_CLUSTER_ENGINE_ENABLED) {
			return Executable.ExecProvider.Cluster;
		}
		// If we are here, than local engine is enabled or one of the two will
		// happen (1) exception is thrown if both engines are disabled
		// or (2) previous statement will return the cluster engine
		return Executable.ExecProvider.Local;
	}

	public static <T> ConfiguredExecutable<T> configureExecutable(
			Executable<T> executable) throws JobSubmissionException {

		ConfExecutable<T> confExec = new ConfExecutable<T>(executable,
				DirectoryManager.getTaskDirectory(executable.getClass()));
		Executable.ExecProvider provider = getExecProvider(confExec, null);
		confExec.setExecProvider(provider);
		setupWorkDirectory(confExec, provider);
		return confExec;
	}

	public static <T> ConfiguredExecutable<T> configureExecutable(
			Executable<T> executable, List<FastaSequence> dataSet)
			throws JobSubmissionException {

		ConfExecutable<T> confExec = new ConfExecutable<T>(executable,
				DirectoryManager.getTaskDirectory(executable.getClass()));
		Executable.ExecProvider provider = getExecProvider(confExec, dataSet);
		confExec.setExecProvider(provider);
		setupWorkDirectory(confExec, provider);
		return confExec;
	}

	static <T> void setupWorkDirectory(ConfExecutable<T> confExec,
			Executable.ExecProvider provider) {
		assert provider != null && provider != Executable.ExecProvider.Any;
		String workDir = "";
		if (provider == Executable.ExecProvider.Local) {
			workDir = Configurator.LOCAL_WORK_DIRECTORY + File.separator
					+ confExec.getTaskId();
		} else {
			workDir = Configurator.CLUSTER_WORK_DIRECTORY + File.separator
					+ confExec.getTaskId();
		}
		// Create working directory for the task
		File wdir = new File(workDir);
		wdir.mkdir();
		log.info("Creating working directory for the task in: "
				+ wdir.getAbsolutePath());
		// Tell the executable where to get the results
		confExec.setWorkDirectory(workDir);
	}

	public static <T> ConfiguredExecutable<T> configureExecutable(
			Executable<T> executable, Executable.ExecProvider provider)
			throws JobSubmissionException {
		if (executable == null) {
			throw new InvalidParameterException("Executable must be provided!");
		}
		ConfExecutable<T> confExec = new ConfExecutable<T>(executable,
				DirectoryManager.getTaskDirectory(executable.getClass()));
		if (provider == Executable.ExecProvider.Cluster
				&& !IS_CLUSTER_ENGINE_ENABLED) {
			throw new JobSubmissionException(
					"Cluster engine is disabled or not configured!");
		}
		if (provider == Executable.ExecProvider.Local
				&& !IS_LOCAL_ENGINE_ENABLED) {
			throw new JobSubmissionException(
					"Local engine is disabled or not configured!");
		}
		confExec.setExecProvider(provider);
		setupWorkDirectory(confExec, provider);
		return confExec;
	}

	public static AsyncExecutor getAsyncEngine(
			ConfiguredExecutable<?> executable, Executable.ExecProvider provider) {

		assert provider != Executable.ExecProvider.Any && provider != null;
		if (provider == Executable.ExecProvider.Cluster) {
			return new AsyncJobRunner();
		}
		return new AsyncLocalRunner();
	}

	public static SyncExecutor getSyncEngine(
			ConfiguredExecutable<?> executable, Executable.ExecProvider provider)
			throws JobSubmissionException {

		assert provider != Executable.ExecProvider.Any && provider != null;
		if (provider == Executable.ExecProvider.Cluster) {
			return JobRunner.getInstance(executable);
		}
		return new LocalRunner(executable);
	}

	public static AsyncExecutor getAsyncEngine(
			ConfiguredExecutable<?> executable) {
		if (isTargetedForLocalExecution(executable)) {
			return new AsyncLocalRunner();
		}
		return new AsyncJobRunner();
	}

	public static AsyncExecutor getAsyncEngine(String taskId) {
		if (isLocal(taskId)) {
			return new AsyncLocalRunner();
		}
		return new AsyncJobRunner();
	}

	public static SyncExecutor getSyncEngine(ConfiguredExecutable<?> executable)
			throws JobSubmissionException {
		if (isTargetedForLocalExecution(executable)) {
			return new LocalRunner(executable);
		}
		return JobRunner.getInstance(executable);
	}

	static boolean isTargetedForLocalExecution(
			ConfiguredExecutable<?> executable) {
		// In the uncommon case that the cluster and local execution temp
		// directories are the same,
		// in this case the method return true anyway

		/*
		 * Could have done this String taskDir = executable.getWorkDirectory();
		 * int idx = taskDir.lastIndexOf(File.separator); String workDir =
		 * taskDir.substring(0, idx); assert
		 * !(workDir.equals(CLUSTER_WORK_DIRECTORY) && workDir
		 * .equals(LOCAL_WORK_DIRECTORY)) :
		 * "Could not determine executable target!"; if
		 * (workDir.equals(LOCAL_WORK_DIRECTORY)) { return true; }
		 */
		String taskDir = executable.getTaskId();
		return isLocal(taskDir);
	}

	static boolean isLocal(String taskId) {
		if (Util.isEmpty(taskId)) {
			throw new NullPointerException("TaskId must be provided!");
		}
		if (!compbio.engine.client.Util.isValidJobId(taskId)) {
			throw new InvalidParameterException("TaskId is not valid!");
		}
		return !taskId.startsWith(ConfExecutable.CLUSTER_TASK_ID_PREFIX);
	}

	public static String getWorkDirectory(String taskId) {
		assert !compbio.util.Util.isEmpty(taskId);
		assert compbio.engine.client.Util.isValidJobId(taskId);
		log.info("Getting workdirectory for TaskID: " + taskId);
		if (taskId.startsWith(ConfExecutable.CLUSTER_TASK_ID_PREFIX)) {
			return CLUSTER_WORK_DIRECTORY + File.separator + taskId;
		}
		return LOCAL_WORK_DIRECTORY + File.separator + taskId;
	}
}
