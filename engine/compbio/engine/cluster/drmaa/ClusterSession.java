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

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.log4j.Logger;
import org.ggf.drmaa.DrmaaException;
import org.ggf.drmaa.InvalidJobException;
import org.ggf.drmaa.JobInfo;
import org.ggf.drmaa.Session;
import org.ggf.drmaa.SessionFactory;

import compbio.engine.ClusterJobId;
import compbio.engine.Job;
import compbio.engine.client.ConfiguredExecutable;
import compbio.engine.client.PathValidator;
import compbio.engine.conf.PropertyHelperManager;
import compbio.metadata.JobStatus;
import compbio.metadata.ResultNotAvailableException;
import compbio.util.FileUtil;
import compbio.util.PropertyHelper;
import compbio.util.Util;

public final class ClusterSession {

	private static final Logger log = Logger.getLogger(ClusterSession.class);

	private static final PropertyHelper ph = PropertyHelperManager
			.getPropertyHelper();

	public static final String JOBID = "JOBID";
	// TaskId (getTaskDirectory()) -> ConfiguredExecutable<?> map
	// Cluster jobId is only stored in a file
	// static final Map<JobId, ConfiguredExecutable<?>> jobs = new
	// ConcurrentHashMap<JobId, ConfiguredExecutable<?>>();

	static final List<Job> jobs = new CopyOnWriteArrayList<Job>();
	private static boolean open = true;

	// Keep this at the end of other static initializers to avoid making
	// incomplete instance!
	private static final ClusterSession INSTANCE = new ClusterSession();

	private final Session session;
	// can be used in init method to reconnect the the session
	private final String sContact;

	// TODO deside whether the task list is needed!
	// private static BufferedWriter tasks;

	private ClusterSession() {
		log.debug("Initializing session "
				+ Util.datef.format(Calendar.getInstance().getTime()));
		SessionFactory factory = SessionFactory.getFactory();
		session = factory.getSession();
		sContact = session.getContact();
		try {
			/*
			 * String tasksFileName = ph.getProperty("cluster.tasks.file"); File
			 * taskFile = new File(tasksFileName); if(!taskFile.exists()) {
			 * taskFile.createNewFile(); } tasks = new BufferedWriter(new
			 * PrintWriter(taskFile));
			 */
			session.init(null);
			// Make sure that the session is going to be properly closed
			Runtime.getRuntime().addShutdownHook(new Thread() {
				@Override
				public void run() {
					/*
					 * try { if(tasks!=null) { tasks.close(); } }
					 * catch(IOException e) { log.error(e.getMessage()); }
					 */
					close();
				}
			});

		} catch (DrmaaException e) {
			log.error(e.getMessage());
		}
		/*
		 * throw new RuntimeException("Could not create task file! " +
		 * "Please check that Engine.cluster.properties " +
		 * "file is provided and contains cluster.tasks.file property. " +
		 * "This property should contain the file name " +
		 * "for storing tasks ids! Cause: " + e.getMessage());
		 */

	}

	synchronized static ClusterSession getInstance() {
		return INSTANCE;
	}

	public Session getSession() {
		return INSTANCE.session;
	}

	public void close() {
		try {
			if (open) {
				session.exit();
				open = false;
				log.debug("Closing the session at: "
						+ Util.datef.format(Calendar.getInstance().getTime()));
			}
		} catch (DrmaaException dre) {
			// Cannot recover at this point, just log
			dre.printStackTrace();
		}
	}

	void addJob(String jobId, ConfiguredExecutable<?> executable) {
		String taskDirectory = executable.getTaskId();
		assert !PathValidator.isValidDirectory(taskDirectory) : "Directory provided is not valid! Directory: "
				+ taskDirectory;
		assert !Util.isEmpty(jobId);

		compbio.engine.client.Util.writeStatFile(executable.getWorkDirectory(),
				JobStatus.SUBMITTED.toString());
		compbio.engine.client.Util.writeFile(executable.getWorkDirectory(),
				JOBID, jobId, false);
		log.debug("Adding taskId: " + taskDirectory + " to cluster job list");
		assert compbio.engine.client.Util.isValidJobId(taskDirectory);
		jobs.add(new Job(taskDirectory, jobId, executable));
	}

	public void removeJob(String taskId) {
		assert !Util.isEmpty(taskId);
		assert compbio.engine.client.Util.isValidJobId(taskId);
		removeJobFromListbyTaskId(taskId);
	}

	/*
	 * public List<JobInfo> waitForJobs(List<String> jobIds) throws
	 * DrmaaException { return waitForJobs(jobIds,
	 * Session.TIMEOUT_WAIT_FOREVER); }
	 * 
	 * public List<JobInfo> waitForJobs(List<String> jobIds, long waitingTime)
	 * throws DrmaaException { if (!open) { throw new
	 * IllegalStateException("Session is already closed!"); } assert jobIds !=
	 * null && jobIds.size() > 1;
	 * 
	 * session.synchronize(jobIds, waitingTime, false); List<JobInfo> jobsInfo =
	 * new ArrayList<JobInfo>(jobIds.size()); for (String jobId : jobIds) {
	 * jobsInfo.add(waitForJob(jobId)); } return jobsInfo; }
	 */

	/*
	 * public List<JobInfo> waitForAll() throws DrmaaException { assert
	 * jobs.size() > 0; return waitForJobs(new
	 * ArrayList<String>(ClusterSession.jobs.keySet())); }
	 * 
	 * 
	 * public void waitForAll_DropStatistics() throws DrmaaException { assert
	 * jobs.size() > 0; session.synchronize(new ArrayList<String>(Collections
	 * .unmodifiableCollection(ClusterSession.jobs.keySet())),
	 * Session.TIMEOUT_WAIT_FOREVER, true); }
	 */

	public JobInfo waitForJob(String taskId) throws DrmaaException, IOException {
		// String clusterJobId = ClusterSession.getClusterJobId(jobId);
		return waitForJob(taskId, Session.TIMEOUT_WAIT_FOREVER);
	}

	public static ClusterJobId getClusterJobId(String taskId)
			throws IOException {
		Job job = Job.getByTaskId(taskId, jobs);
		if (job != null) {
			return job.getJobId();
		}
		// The job must have been removed from the task list use work
		// directory to find out jobid
		String workDir = compbio.engine.Configurator.getWorkDirectory(taskId);
		assert !Util.isEmpty(workDir);
		File file = new File(workDir, JOBID);
		log.debug("Looking up cluster jobid by the task id " + taskId
				+ " File path is " + file.getAbsolutePath());
		assert file.exists();
		return new ClusterJobId(FileUtil.readFileToString(file));
	}

	public JobInfo waitForJob(String jobId, long waitingTime)
			throws DrmaaException, IOException {
		ClusterJobId cjobId = getClusterJobId(jobId);
		JobInfo status = session.wait(cjobId.getJobId(), waitingTime);
		// Once the job has been waited for it will be finished
		// Next time it will not be found in the session, so removed from the
		// job list
		compbio.engine.client.Util.writeStatFile(
				compbio.engine.Configurator.getWorkDirectory(jobId),
				JobStatus.FINISHED.toString());

		return status;
	}

	private static void removeJobFromListbyTaskId(String taskId) {
		assert !Util.isEmpty(taskId);
		Job job = Job.getByTaskId(taskId, jobs);
		if (job != null) {
			log.debug("Removing taskId" + taskId + " from cluster job list");
			jobs.remove(job);
		}
	}

	public ConfiguredExecutable<?> getResults(String taskId)
			throws DrmaaException, ResultNotAvailableException {

		compbio.engine.client.Util.isValidJobId(taskId);
		try {
			JobInfo status = waitForJob(taskId);
		} catch (InvalidJobException e) {
			// Its OK to continue, the job may have already completed normally
			log.warn("Could not find the cluster job with id " + taskId
					+ " perhaps it has completed", e.getCause());
		} catch (IOException e) {
			log.error("Could not read JOBID file for the job " + taskId
					+ " Message " + e.getLocalizedMessage(), e.getCause());
		}
		// Once the job has been waited for it will be finished
		// Next time it will not be found in the session, so removed from the
		// job list
		ConfiguredExecutable<?> exec = null;
		Job job = Job.getByTaskId(taskId, jobs);
		if (job != null) {
			exec = job.getConfExecutable();
			removeJobFromListbyTaskId(taskId);
		} else {
			// If task was not find in the list of jobs, than it must have been
			// collected already
			// Resurrect the job to find out there the output is
			exec = compbio.engine.client.Util.loadExecutable(taskId);
		}
		if (exec != null) {
			compbio.engine.client.Util.writeMarker(exec.getWorkDirectory(),
					JobStatus.COLLECTED);
		}
		return exec;
	}

	public static StatisticManager getStatistics(JobInfo status)
			throws DrmaaException {
		return new StatisticManager(status);
	}

	static void logStatistics(JobInfo status) throws DrmaaException {
		log.info(getStatistics(status).getAllStats());
	}

	/**
	 * Apparently completed jobs cannot be found! If this happened most likely
	 * that the job is not running any more and Most likely it has been
	 * cancelled, finished or failed.
	 * 
	 * @throws InvalidJobException
	 *             if the job is no longer in the queue or running. basically it
	 *             will throw this exception for all finished or cancelled jobs
	 */
	public int getJobStatus(ClusterJobId jobId) throws DrmaaException,
			InvalidJobException {
		return session.getJobProgramStatus(jobId.getJobId());
	}

	/**
	 * Method for getting jobs status by quering the cluster, It returns status
	 * in therms of a Sessions, not a JobStatus Should only be used for testing!
	 * 
	 * @param status
	 * @return job status
	 * @throws DrmaaException
	 */
	@Deprecated
	public static String getJobStatus(final int status) throws DrmaaException {
		String statusString = null;
		switch (status) {
			case Session.UNDETERMINED :
				statusString = "Job status cannot be determined\n";
				break;
			case Session.QUEUED_ACTIVE :
				statusString = "Job is queued and active\n";
				break;
			case Session.SYSTEM_ON_HOLD :
				statusString = "Job is queued and in system hold\n";
				break;
			case Session.USER_ON_HOLD :
				statusString = "Job is queued and in user hold\n";
				break;
			case Session.USER_SYSTEM_ON_HOLD :
				statusString = "Job is queued and in user and system hold\n";
				break;
			case Session.RUNNING :
				statusString = "Job is running\n";
				break;
			case Session.SYSTEM_SUSPENDED :
				statusString = "Job is system suspended\n";
				break;
			case Session.USER_SUSPENDED :
				statusString = "Job is user suspended\n";
				break;
			case Session.USER_SYSTEM_SUSPENDED :
				statusString = "Job is user and system suspended\n";
				break;
			case Session.DONE :
				statusString = "Job finished normally\n";
				break;
			case Session.FAILED :
				statusString = "Job finished, but failed\n";
				break;
		}
		return statusString;
	}

}
