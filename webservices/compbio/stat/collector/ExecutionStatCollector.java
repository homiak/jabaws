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
package compbio.stat.collector;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import compbio.engine.client.Executable;
import compbio.engine.client.PathValidator;
import compbio.engine.client.SkeletalExecutable;
import compbio.metadata.JobStatus;
import compbio.util.FileUtil;
import compbio.ws.client.Services;
import compbio.ws.client.ServicesUtil;

/**
 * Number of runs of each WS = number of folders with name
 * 
 * Number of successful runs = all runs with no result file
 * 
 * Per period of time = limit per file creating time Runtime (avg/max) =
 * 
 * started time - finished time
 * 
 * Task & result size = result.size
 * 
 * Abandoned runs - not collected runs
 * 
 * Cancelled runs - cancelled
 * 
 * Cluster vs local runs
 * 
 * Reasons for failure = look in the err out?
 * 
 * 
 * Metadata required:
 * 
 * work directory for local and cluster tasks = from Helper or cmd parameter. WS
 * names - enumeration. Status file names and content.
 * 
 * @author pvtroshin
 * 
 */
public class ExecutionStatCollector implements Runnable {

	static final int UNDEFINED = -1;

	private static final Logger log = Logger
			.getLogger(ExecutionStatCollector.class);

	static SimpleDateFormat DF = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");

	final private File workDirectory;
	final private List<JobStat> stats;
	/**
	 * Consider the job that has been working for longer than timeOutInHours
	 * completed, whatever the outcome
	 */
	final private int timeOutInHours;

	/**
	 * List subdirectories in the job directory
	 * 
	 * @param workDirectory
	 * @param timeOutInHours
	 */
	public ExecutionStatCollector(String workDirectory, int timeOutInHours) {
		log.info("Starting stat collector for directory: " + workDirectory);
		log.info("Maximum allowed runtime(h): " + timeOutInHours);
		if (!PathValidator.isValidDirectory(workDirectory)) {
			throw new IllegalArgumentException("workDirectory '"
					+ workDirectory + "' does not exist!");
		}
		this.workDirectory = new File(workDirectory);
		stats = new ArrayList<JobStat>();
		if (timeOutInHours <= 0) {
			throw new IllegalArgumentException(
					"Timeout value must be greater than 0! Given value: "
							+ timeOutInHours);
		}
		this.timeOutInHours = timeOutInHours;
	}

	boolean hasCompleted(JobDirectory jd) {
		JobStat jstat = jd.getJobStat();
		if (jstat.hasResult() || jstat.getIsCancelled()
				|| jstat.getIsFinished() || hasTimedOut(jd)) {
			return true;
		}
		return false;
	}

	boolean hasTimedOut(JobDirectory jd) {
		return ((System.currentTimeMillis() - jd.jobdir.lastModified()) / (1000 * 60 * 60)) > timeOutInHours;
	}

	/*
	 * Make sure that collectStatistics methods was called prior to calling
	 * this! TODO consider running collectStatistics from here on the first call
	 */
	StatProcessor getStats() {
		if (stats.isEmpty()) {
			log.info("Please make sure collectStatistics method was called prior to calling getStats()!");
		}
		return new StatProcessor(stats);
	}

	void writeStatToDB() throws SQLException {
		Set<JobStat> rjobs = new HashSet<JobStat>(stats);
		StatDB statdb = new StatDB();
		log.debug("Removing records that has already been recorded");

		statdb.removeRecordedJobs(rjobs);
		log.debug("New records left: " + rjobs.size());
		statdb.insertData(rjobs);
	}

	/*
	 * static void updateTime(File statFile) throws IOException { long lastMod =
	 * statFile.lastModified(); FileWriter fw = new FileWriter(statFile);
	 * fw.write(new Long(lastMod).toString()); fw.close(); }
	 */

	/**
	 * Not in use
	 */
	public static void main(String[] args) throws IOException, SQLException {

		// updateTime(new File(
		// "D:\\workspace\\JABA2\\jobsout\\AACon#170462904473672\\STARTED"));

		File[] files = FileUtil.getFiles("Y:\\fc\\www-jws2\\jaba\\jobsout",
				directories);
		List<JobStat> stats = new ArrayList<JobStat>();
		for (File file : files) {
			JobDirectory jd = new JobDirectory(file);
			stats.add(jd.getJobStat());
			// System.out.println(jd.getJobStat().getJobReportTabulated());
		}
		StatProcessor sp = new StatProcessor(stats);
		System.out.println(sp.reportStat());
		System.out.println();
		System.out.println("!!!!!!!!!!!!!!!!!!");
		System.out.println();

		Set<JobStat> rjobs = new HashSet<JobStat>(sp.stats);
		StatDB statdb = new StatDB();
		statdb.removeRecordedJobs(rjobs);
		statdb.insertData(rjobs);
	}

	static FileFilter directories = new FileFilter() {
		@Override
		public boolean accept(File pathname) {
			return pathname.isDirectory()
					&& !pathname.getName().startsWith(".");
		}
	};

	static class JobDirectory {

		File jobdir;
		Map<String, File> files = new HashMap<String, File>();

		JobDirectory(File directory) {
			this.jobdir = directory;
			for (File f : jobdir.listFiles()) {
				files.put(f.getName(), f);
			}
		}

		boolean hasStatus(JobStatus status) {
			return files.containsKey(status.toString());
		}

		boolean isCollected() {
			return hasStatus(JobStatus.COLLECTED);
		}

		boolean isCancelled() {
			return hasStatus(JobStatus.CANCELLED);
		}

		long getStartTime() {
			long starttime = UNDEFINED;
			File startfile = files.get(JobStatus.STARTED.toString());
			if (startfile == null) {
				startfile = files.get(JobStatus.SUBMITTED.toString());
			}
			try {
				if (startfile != null) {
					String start = FileUtil.readFileToString(startfile);
					starttime = Long.parseLong(start.trim());
				}
			} catch (IOException ignore) {
				log.warn(
						"IOException while reading STARTED status file! Ignoring...",
						ignore);
				// fall back
				starttime = startfile.lastModified();
			} catch (NumberFormatException ignore) {
				log.warn(
						"NumberFormatException while reading STARTED status file! Ignoring...",
						ignore);
				// fall back
				starttime = startfile.lastModified();
			}

			return starttime;
		}

		String getClusterJobID() {
			String clustjobId = "";
			File jobid = files.get("JOBID");
			try {
				if (jobid != null) {
					clustjobId = FileUtil.readFileToString(jobid);
				}
			} catch (IOException ioe) {
				log.error(
						"IO Exception while reading the content of JOBID file for job "
								+ jobid, ioe);
			}
			return clustjobId.trim();
		}

		long getFinishedTime() {
			long ftime = UNDEFINED;
			File finished = files.get(JobStatus.FINISHED.toString());
			if (finished != null) {
				try {
					if (finished != null) {
						String start = FileUtil.readFileToString(finished);
						ftime = Long.parseLong(start.trim());
					}
				} catch (IOException ignore) {
					log.warn(
							"IOException while reading FINISHED status file! Ignoring...",
							ignore);
					// fall back
					ftime = finished.lastModified();
				} catch (NumberFormatException ignore) {
					log.warn(
							"NumberFormatException while reading FINISHED status file! Ignoring...",
							ignore);
					// fall back
					ftime = finished.lastModified();
				}
			}
			return ftime;
		}

		private Services getService() {
			return ServicesUtil.getServiceByJobDirectory(jobdir);
		}

		long getResultSize() {
			Class<? extends Executable<?>> name = ServicesUtil
					.getRunnerByJobDirectory(jobdir);

			File f = null;
			if (name.getSimpleName().equalsIgnoreCase("IUPred")) {
				f = files.get("out.glob");
				if (f == null)
					f = files.get("out.short");
				if (f == null)
					f = files.get("out.long");
			} else {
				f = files.get(SkeletalExecutable.OUTPUT);
			}
			if (f != null) {
				return f.length();
			}
			return UNDEFINED;
		}

		long getInputSize() {
			Class<? extends Executable<?>> name = ServicesUtil
					.getRunnerByJobDirectory(jobdir);

			File input = files.get(SkeletalExecutable.INPUT);
			if (input != null) {
				return input.length();
			}
			return UNDEFINED;
		}

		JobStat getJobStat() {
			return JobStat.newInstance(getService(), getClusterJobID(),
					jobdir.getName(), getStartTime(), getFinishedTime(),
					getInputSize(), getResultSize(), isCancelled(),
					isCollected());
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((jobdir == null) ? 0 : jobdir.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			JobDirectory other = (JobDirectory) obj;
			if (jobdir == null) {
				if (other.jobdir != null)
					return false;
			} else if (!jobdir.equals(other.jobdir))
				return false;
			return true;
		}
	}

	// TODO test!
	void collectStatistics() {
		File[] files = workDirectory.listFiles(directories);
		for (File file : files) {
			if (!InputFilter.accept(new File(file.getPath() + File.separator
					+ SkeletalExecutable.INPUT))) {
				// skip work directory with test input
				continue;
			}
			JobDirectory jd = new JobDirectory(file);
			JobStat jstat = jd.getJobStat();
			// Do not record stats on the job that has not completed yet
			if (hasCompleted(jd)) {
				stats.add(jstat);
			} else {
				log.debug("Skipping the job: " + jstat);
				log.debug("As it has not completed yet");
			}
			// System.out.println(jd.getJobStat().getJobReportTabulated());
		}
	}
	@Override
	public void run() {
		log.info("Started updating statistics at " + new Date());
		log.info("For directory: " + workDirectory.getAbsolutePath());

		collectStatistics();

		StatProcessor local_stats = getStats();
		log.info("Found " + local_stats.getJobNumber() + " jobs!");
		try {
			writeStatToDB();
		} catch (SQLException e) {
			log.error("Fails to update jobs statistics database!");
			log.error(e.getLocalizedMessage(), e);
		}
		log.info("Finished updating statistics at " + new Date());
	}
}
