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

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.ggf.drmaa.DrmaaException;
import org.ggf.drmaa.InvalidJobException;
import org.ggf.drmaa.JobInfo;
import org.ggf.drmaa.JobTemplate;
import org.ggf.drmaa.Session;

import compbio.engine.Cleaner;
import compbio.engine.ClusterJobId;
import compbio.engine.Configurator;
import compbio.engine.SyncExecutor;

import compbio.engine.client.ConfiguredExecutable;
import compbio.engine.client.Executable;
import compbio.engine.client.PathValidator;
import compbio.engine.client.PipedExecutable;
import compbio.engine.client.Util;
import compbio.engine.client.Executable.ExecProvider;
import compbio.metadata.JobExecutionException;
import compbio.metadata.JobStatus;
import compbio.metadata.JobSubmissionException;
import compbio.metadata.ResultNotAvailableException;

/**
 * Single cluster job runner class
 * 
 * @author pvtroshin
 * @date August 2009
 * 
 *       TODO after call to submitJob() no setters really work as the job
 *       template gets deleted, this needs to be taken into account in this
 *       class design!
 */
public class JobRunner implements SyncExecutor {

	final JobTemplate jobtempl;
	static ClusterSession clustSession = ClusterSession.getInstance();
	static Session session = clustSession.getSession();
	static final Logger log = Logger.getLogger(JobRunner.class);
	final ConfiguredExecutable<?> confExecutable;
	private final String workDirectory;
	String jobId;

	public JobRunner(ConfiguredExecutable<?> confExec)
			throws JobSubmissionException {
		try {
			String command = confExec.getCommand(ExecProvider.Cluster);
			PathValidator.validateExecutable(command);
			log.debug("Setting command " + command);

			jobtempl = session.createJobTemplate();
			jobtempl.setRemoteCommand(command);
			jobtempl.setJoinFiles(false);
			setJobName(confExec.getExecutable().getClass().getSimpleName());

			this.workDirectory = confExec.getWorkDirectory();
			assert !compbio.util.Util.isEmpty(workDirectory);

			// Tell the job where to get/put things
			jobtempl.setWorkingDirectory(this.workDirectory);

			/*
			 * Set environment variables for the process if any
			 */
			Map<String, String> jobEnv = confExec.getEnvironment();
			if (jobEnv != null && !jobEnv.isEmpty()) {
				setJobEnvironmentVariables(jobEnv);
			}
			List<String> args = confExec.getParameters().getCommands();
			// Set optional parameters
			if (args != null && args.size() > 0) {
				jobtempl.setArgs(args);
			}

			/*
			 * If executable need in/out data to be piped into it
			 */
			if (confExec.getExecutable() instanceof PipedExecutable<?>) {
				setPipes(confExec);
			}

			/*
			 * If executable require special cluster configuration parameters to
			 * be set e.g. queue, ram, time etc
			 */
			setNativeSpecs(confExec.getExecutable());


			log.trace("using arguments: " + jobtempl.getArgs());
			this.confExecutable = confExec;
			// Save run configuration
			confExec.saveRunConfiguration();

		} catch (DrmaaException e) {
			log.error(e.getLocalizedMessage(), e.getCause());
			throw new JobSubmissionException(e);
		} catch (IOException e) {
			log.error(e.getLocalizedMessage(), e.getCause());
			throw new JobSubmissionException(e);
		} 

	}

	void setPipes(ConfiguredExecutable<?> executable) throws DrmaaException {

		String output = executable.getOutput();
		String error = executable.getError();
		// Standard drmaa path format is hostname:path
		// to avoid supplying hostnames with all local paths just prepend colon
		// to the path
		// Input and output can be null as in and out files may be defined in
		// parameters
		/*
		 * Use this for piping input into the process if (input != null) { if
		 * (!input.contains(":")) { input = makeLocalPath(input);
		 * log.trace("converting input to " + input); }
		 * jobtempl.setInputPath(input); log.debug("use Input: " +
		 * jobtempl.getInputPath()); }
		 */
		if (output != null) {
			if (!output.contains(":")) {
				output = makeLocalPath(output);
			}
			jobtempl.setOutputPath(output);
			log.debug("Output to: " + jobtempl.getOutputPath());
		}
		if (error != null) {
			if (!error.contains(":")) {
				error = makeLocalPath(error);
			}
			jobtempl.setErrorPath(error);
			log.debug("Output errors to: " + jobtempl.getErrorPath());
		}

	}

	void setNativeSpecs(Executable<?> executable) throws DrmaaException {
		String nativeSpecs = executable.getClusterJobSettings(); 
		if(!compbio.util.Util.isEmpty(nativeSpecs)) {
			log.debug("Using cluster job settings: " + nativeSpecs);
			jobtempl.setNativeSpecification(nativeSpecs);
		}
	}

	void setEmail(String email) {
		log.trace("Setting email to:" + email);
		try {
			jobtempl.setEmail(Collections.singleton(email));
			jobtempl.setBlockEmail(false);
		} catch (DrmaaException e) {
			log.debug(e.getLocalizedMessage());
			throw new IllegalArgumentException(e);
		}
	}

	void setJobName(String name) {
		log.trace("Setting job name to:" + name);
		try {
			jobtempl.setJobName(name);
		} catch (DrmaaException e) {
			log.debug(e.getLocalizedMessage());
			throw new IllegalArgumentException(e);
		}
	}

	@SuppressWarnings("unchecked")
	void setJobEnvironmentVariables(Map<String, String> env_variables) {
		assert env_variables != null && !env_variables.isEmpty();
		try {
			log.trace("Setting job environment to:" + env_variables);
			Map<String, String> sysEnv = jobtempl.getJobEnvironment();
			if (sysEnv != null && !sysEnv.isEmpty()) {
				Util.mergeEnvVariables(sysEnv, env_variables);
			} else {
				sysEnv = env_variables;
			}
			jobtempl.setJobEnvironment(sysEnv);

		} catch (DrmaaException e) {
			log.debug(e.getLocalizedMessage());
			throw new IllegalArgumentException(e);
		}
	}

	private static String makeLocalPath(String path) {
		return ":" + path;
	}

	public boolean deepClean() {
		throw new UnsupportedOperationException();
		// TODO
		/*
		 * remove all files from these this.jobtempl.getInputPath();
		 * this.jobtempl.getOutputPath(); this.jobtempl.getWorkingDirectory();
		 */
		// executable.getInputFiles();
	}

	/**
	 * This will never return clust.engine.JobStatus.CANCELLED as for sun grid
	 * engine cancelled job is the same as failed. Cancelled jobs needs to be
	 * tracked manually!
	 */
	static compbio.metadata.JobStatus getJobStatus(String jobId) {
		try {
			ClusterJobId clusterJobId = ClusterSession.getClusterJobId(jobId);
			switch (clustSession.getJobStatus(clusterJobId)) {
			case Session.DONE:
				compbio.engine.client.Util.writeStatFile(Configurator.getWorkDirectory(jobId),
						JobStatus.FINISHED.toString());

				return compbio.metadata.JobStatus.FINISHED;
			case Session.FAILED:
				compbio.engine.client.Util.writeMarker(Configurator.getWorkDirectory(jobId),
						JobStatus.FAILED);

				return compbio.metadata.JobStatus.FAILED;

			case Session.RUNNING:
				// will not persist this status as temporary
				return compbio.metadata.JobStatus.RUNNING;

			case Session.SYSTEM_SUSPENDED:
			case Session.USER_SYSTEM_SUSPENDED:
			case Session.USER_SUSPENDED:
			case Session.USER_SYSTEM_ON_HOLD:
			case Session.USER_ON_HOLD:
			case Session.SYSTEM_ON_HOLD:
			case Session.QUEUED_ACTIVE:
				// will not persist this status as temporary
				return compbio.metadata.JobStatus.PENDING;

			default:
				// It is possible that the this status is returned for a job that is almost completed
				// when a state is changed from RUNNING to DONE
				// It looks like a bug in DRMAA SGE implementation 
				return compbio.metadata.JobStatus.UNDEFINED;
			}
		} catch (InvalidJobException e) {
			log.info("Job " + jobId + " could not be located by DRMAA "
					+ e.getLocalizedMessage(), e.getCause());
			log.info("Attempting to determine the status by marker files");
			return getRecordedJobStatus(jobId);
		} catch (DrmaaException e) {
			log.error(
					"Exception in DRMAA system while quering the job status: "
							+ e.getLocalizedMessage(), e.getCause());
		} catch (IOException e) {
			log.error("Could not read JOBID for taskId: " + jobId
					+ " Message: " + e.getLocalizedMessage(), e.getCause());
		}

		return JobStatus.UNDEFINED;
	}

	static JobStatus getRecordedJobStatus(String jobId) { 
		/*
		 * Job has already been removed from the task list, so it running
		 * status could not be determined. Most likely it has been
		 * cancelled, finished or failed.
		 */
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
	
	
	@Override
	public boolean cleanup() {
		/*
		 * TODO there is two additional files created by sun grid engine which
		 * are named as follows: output this.getWorkDirectory() +
		 * executable.getClass().getSimpleName() + "." + "o" + this.jobId; error
		 * this.getWorkDirectory() + executable.getClass().getSimpleName() + "."
		 * + "e" + this.jobId; individual executable does not know about these
		 * two unless it implements PipedExecutable which need to collect data
		 * from these streams Thus this method will fail to remove the task
		 * directory completely
		 */
		return Cleaner.deleteFiles(confExecutable);
	}

	JobInfo waitForJob(String jobId) throws JobExecutionException {
		assert Util.isValidJobId(jobId);
		return ClusterUtil.waitForResult(clustSession, jobId);
	}

	boolean cancelJob(String jobId) {
		assert Util.isValidJobId(jobId);
		return compbio.engine.cluster.drmaa.ClusterUtil.cancelJob(jobId,
				clustSession);
	}

	@Override
	public boolean cancelJob() {
		return cancelJob(this.jobId);
	}

	String submitJob() throws JobSubmissionException {

		String jobId;
		try {
			jobId = session.runJob(jobtempl);
			log.info("submitted single job with jobids:");
			log.info("\t \"" + jobId + "\"");
			session.deleteJobTemplate(jobtempl);
			clustSession.addJob(jobId, confExecutable);
		} catch (DrmaaException e) {
			e.printStackTrace();
			throw new JobSubmissionException(e);
		}

		return this.confExecutable.getTaskId();
	}

	public String getWorkDirectory() {
		return this.workDirectory;
	}

	@Override
	public void executeJob() throws JobSubmissionException {
		this.jobId = submitJob();
	}

	/**
	 * This method will block before the calculation has completed and then
	 * return the object containing a job execution statistics
	 * 
	 * @return
	 * @throws JobExecutionException
	 */
	public JobInfo getJobInfo() throws JobExecutionException {
		return waitForJob(this.jobId);
	}

	@Override
	public ConfiguredExecutable<?> waitForResult() throws JobExecutionException {
		ConfiguredExecutable<?> confExec;
		try {
			confExec = new AsyncJobRunner().getResults(this.jobId);
			if (confExec == null) {
				log.warn("Could not find results of job " + this.jobId);
			}
		} catch (ResultNotAvailableException e) {
			log.error(e.getMessage(), e.getCause());
			throw new JobExecutionException(e);
		}
		return confExec;
	}

	@Override
	public compbio.metadata.JobStatus getJobStatus() {
		return getJobStatus(this.jobId);
	}

	public static JobRunner getInstance(ConfiguredExecutable<?> executable)
			throws JobSubmissionException {
		return new JobRunner(executable);
	}

} // class end
