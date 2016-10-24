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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import compbio.ws.client.Services;

public class StatProcessor {

	List<JobStat> stats;

	public StatProcessor(List<JobStat> stats) {
		this.stats = stats;
	}

	public List<JobStat> getClusterJobs() {
		return getJobSubset(true);
	}

	public List<JobStat> getLocalJobs() {
		return getJobSubset(false);
	}

	private List<JobStat> getJobSubset(boolean cluster) {
		List<JobStat> clusterjobs = new ArrayList<JobStat>();
		for (JobStat js : stats) {
			if (cluster) {
				if (js.isClusterJob()) {
					clusterjobs.add(js);
				}
			} else {
				if (!js.isClusterJob()) {
					clusterjobs.add(js);
				}
			}
		}
		return clusterjobs;

	}

	/*
	 * TODO List<JobStat> getNewStat() throws SQLException { Set<String> jobids
	 * = new HashSet<String>(); for(JobStat js: stats) { jobids.add(js.jobname);
	 * } StatDB.removeRecordedJobs(jobids); List<String> newjobs = new
	 * HashSet<String>(); for(String jobid: jobids) { if(newjobs.co)
	 * jobids.add(js.jobname); } }
	 */

	/**
	 * Not collected. Excludes all cancelled jobs, and jobs with no results as
	 * these are reported separately.
	 */
	public List<JobStat> getAbandonedJobs() {
		List<JobStat> abJobs = new ArrayList<JobStat>();
		for (JobStat js : stats) {
			if (!js.isCollected && !js.isCancelled && js.hasResult()) {
				abJobs.add(js);
			}
		}
		return abJobs;
	}

	/**
	 * Started & finished but did not produce result
	 * 
	 * @return
	 */
	public List<JobStat> getFailedJobs() {
		List<JobStat> failedJobs = new ArrayList<JobStat>();
		for (JobStat js : stats) {
			if (js.hasStarted() && js.getIsFinished() && !js.hasResult()) {
				failedJobs.add(js);
			}
		}
		return failedJobs;
	}

	public List<JobStat> getCancelledJobs() {
		List<JobStat> abJobs = new ArrayList<JobStat>();
		for (JobStat js : stats) {
			if (js.isCancelled) {
				abJobs.add(js);
			}
		}
		return abJobs;
	}

	public List<JobStat> sortByRuntime() {
		List<JobStat> abJobs = new ArrayList<JobStat>(stats);
		Collections.sort(abJobs, JobStat.RUNTIME);
		return abJobs;
	}

	public List<JobStat> sortByStartTime() {
		List<JobStat> abJobs = new ArrayList<JobStat>(stats);
		Collections.sort(abJobs, JobStat.STARTTIME);
		return abJobs;
	}

	public List<JobStat> sortByResultSize() {
		List<JobStat> abJobs = new ArrayList<JobStat>(stats);
		Collections.sort(abJobs, JobStat.RESULTSIZE);
		return abJobs;
	}

	public int getJobNumber() {
		return stats.size();
	}

	public List<JobStat> getJobs() {
		return stats;
	}

	public StatProcessor getSingleWSStat(Services webService) {
		List<JobStat> wsStat = new ArrayList<JobStat>();
		for (JobStat js : stats) {
			if (js.webService == webService) {
				wsStat.add(js);
			}
		}
		return new StatProcessor(wsStat);
	}

	public long getTotalRuntime() {
		long counter = 0;
		for (JobStat js : stats) {
			int jobtime = js.getRuntime();
			if (jobtime != ExecutionStatCollector.UNDEFINED) {
				counter += jobtime;
			}
		}
		return counter;
	}

	public List<JobStat> getIncompleteJobs() {
		List<JobStat> aJobs = new ArrayList<JobStat>();
		for (JobStat js : stats) {
			if (!js.hasResult() && !js.getIsCancelled()) {
				aJobs.add(js);
			}
		}
		return aJobs;
	}

	public String reportStat() {
		String report = "Total Jobs: " + getJobNumber() + "\n";
		report += "Abandoned Jobs: " + getAbandonedJobs().size() + "\n";
		report += "Cancelled Jobs: " + getCancelledJobs().size() + "\n";
		report += "Total Runtime (s): " + getTotalRuntime() + "\n";
		report += "Unsuccessful Jobs: " + getIncompleteJobs().size() + "\n";
		if (sortByRuntime().size() > 10) {
			report += "10 longest jobs: \n\n" + sortByRuntime().subList(0, 9)
					+ "\n";
		} else {
			report += "longest jobs: \n\n" + sortByRuntime() + "\n";
		}
		if (sortByResultSize().size() > 10)
			report += "10 biggest jobs: \n\n"
					+ sortByResultSize().subList(0, 9) + "\n";
		else {
			report += "biggest jobs: \n\n" + sortByResultSize() + "\n";
		}
		return report;
	}

	@Override
	public String toString() {
		return this.reportStat();
	}

}
