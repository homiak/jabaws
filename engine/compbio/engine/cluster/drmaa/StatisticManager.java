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

import java.util.Date;
import java.util.Map;

import org.ggf.drmaa.DrmaaException;
import org.ggf.drmaa.JobInfo;

import compbio.util.Util;

public class StatisticManager {

	private final Map<String, String> usage;
	final JobInfo jobinfo;
	private static String newLine = "\n";

	public StatisticManager(JobInfo status) throws DrmaaException {
		assert status != null;
		// TODO this is potentially unsafe may need reviewing
		this.jobinfo = status;
		Map<String, String> resourcesUsage = status.getResourceUsage();
		this.usage = resourcesUsage;
	}

	public String getJobId() throws DrmaaException {
		return jobinfo.getJobId();
	}

	public boolean hasExited() throws DrmaaException {
		return jobinfo.hasExited();
	}

	public boolean hasSignaled() throws DrmaaException {
		return jobinfo.hasSignaled();
	}

	public boolean hasDump() throws DrmaaException {
		return jobinfo.hasCoreDump();
	}

	public String termSignal() throws DrmaaException {
		return jobinfo.getTerminatingSignal();
	}

	public boolean wasAborted() throws DrmaaException {
		return jobinfo.wasAborted();
	}

	public String getSubmissionTime() {
		return usage.get(submission_time);
	}

	public String getVMem() {
		return usage.get(vmem);
	}

	public String getMaxVMem() {
		return usage.get(maxvmem);
	}

	public String getUsedSysTime() {
		return usage.get(ru_stime);
	}

	public String getUsedUserTime() {
		return usage.get(ru_utime);
	}

	public String getCalculationTime() {
		return usage.get(ru_wallclock);
	}

	public String getEndTime() {
		return usage.get(end_time);
	}

	public String getStartTime() {
		return usage.get(start_time);
	}

	public String getIOWait() {
		return usage.get(iow);
	}

	public String getCPUUsageTime() {
		return usage.get(cpu);
	}

	public String getDataTransfered() {
		return usage.get(io);
	}

	public String getJobPriority() {
		return usage.get(priority);
	}

	public String getExitStatus() {
		return usage.get(exit_status);
	}

	public String getAllStats() throws DrmaaException {

		String stats = "JobID: " + getJobId() + newLine;
		stats += getExecutionStat() + newLine;
		stats += getTimeStat() + newLine;
		stats += getCPUTimeStat() + newLine;
		stats += getMemoryStat() + newLine;

		return stats;
	}

	public String getExecutionStat() throws DrmaaException {
		String stats = "Priority:" + getJobPriority() + newLine;
		/* report how job finished */
		if (wasAborted()) {
			stats += "job \"" + getJobId() + "\" was aborted (never ran)"
					+ newLine;
		} else if (hasExited()) {
			stats += "job \"" + getJobId()
					+ "\" finished regularly with exit status "
					+ getExitStatus() + newLine;
		} else if (hasSignaled()) {
			stats += "job \"" + getJobId() + "\" finished due to signal "
					+ termSignal() + newLine;
		} else {
			stats += "job \"" + getJobId()
					+ "\" finished with unclear conditions" + newLine;
		}
		stats += "Has Core Dump: " + hasDump() + newLine;
		return stats;
	}

	public String getMemoryStat() {
		String stats = "Data transfered: " + getDataTransfered() + newLine;
		stats += "IO wait: " + getIOWait() + newLine;
		stats += "Max Virtual Memory: " + getMaxVMem() + newLine;
		stats += "Virtual Memory: " + getVMem() + newLine;
		return stats;
	}

	public String getCPUTimeStat() {
		String stats = "CPU time (s): " + getCPUUsageTime() + newLine;
		stats += "Sys time (s): " + getUsedSysTime() + newLine;
		stats += "User time (s): " + getUsedUserTime() + newLine;
		return stats;
	}

	public String getTimeStat() {
		String stats = "Submission time: " + formatTime(getSubmissionTime())
				+ newLine;
		stats += "Calculation time (s): " + getCalculationTime() + newLine;
		stats += "Start time: " + formatTime(getStartTime()) + newLine;
		stats += "End time: " + formatTime(getEndTime()) + newLine;
		return stats;
	}

	public Map<String, String> getRawUsage() {
		return usage;
	}

	@Override
	public String toString() {
		String stat = "";
		try {
			stat = getAllStats();
		} catch (DrmaaException e) {
			// Should not happen
			throw new RuntimeException("exception during toString execution! ",
					e);
		}
		return stat;
	}

	/**
	 * Convert grid engine time format End time: 1250701672.0000 to a readable
	 * time representation as defined in Util.dataf
	 */
	String formatTime(String time) {
		int dotIdx = time.indexOf(".");
		if (dotIdx > 0) {
			// get rid of .0000 part
			time = time.substring(0, dotIdx);
			Date d = new Date();
			d.setTime(Long.parseLong(time + "000"));
			time = Util.datef.format(d);
		} // else do not know how to format
		return time;
	}

	/**
	 * see man 5 accounting on sun grid engine installed workstation see also
	 * man 2 getrusage
	 * 
	 */
	String signal = "signal";
	String submission_time = "submission_time"; // Submission time (GMT unix
	// time stamp).
	String vmem = "vmem";
	String maxvmem = "maxvmem"; // The maximum vmem size in bytes.
	String ru_stime = "ru_stime"; // system time used
	String ru_utime = "ru_utime"; // user time used
	String ru_wallclock = "ru_wallclock"; // Difference between end_time and
	// start_time
	String end_time = "end_time"; // End time (GMT unix time stamp).
	String start_time = "start_time"; // Start time (GMT unix time stamp).

	String iow = "iow"; // The io wait time in seconds.
	String cpu = "cpu"; // The cpu time usage in seconds.
	String io = "io"; // The amount of data transferred in input/output
	// operations.

	String priority = "priority";
	/*
	 * Priority value assigned to the job corresponding to the priority
	 * parameter in the queue configuration
	 */
	String exit_status = "exit_status";
	/*
	 * Exit status of the job script (or Sun Grid Engine specific status in case
	 * of certain error conditions). The exit status is determined by following
	 * the normal shell conventions. If the command terminates normally the
	 * value of the command is its exit status. However, in the case that the
	 * command exits abnormally, a value of 0200 (octal), 128 (decimal) is added
	 * to the value of the command to make up the exit status. For example: If a
	 * job dies through signal 9 (SIGKILL) then the exit status becomes 128 + 9
	 * = 137.
	 */

	// Could not find a discription for these
	String acct_cpu = "acct_cpu";
	String acct_mem = "acct_mem";
	String acct_iow = "acct_iow";
	String acct_io = "acct_io";
	String acct_maxvmem = "acct_maxvmem";

	/*
	 * Appears to be not reported String ru_idrss=0.000; // integral unshared
	 * data size String ru_inblock=0.000; String ru_ismrss=0.0000 String
	 * ru_majflt=0.0000 String ru_msgrcv=0.0000 String ru_isrss="ru_isrss" //
	 * integral unshared stack size String ru_nswap=0.0000 String
	 * ru_oublock=0.0000 String ru_msgsnd=0.0000 String ru_nsignals=0.0000
	 * String ru_maxrss="ru_maxrss"; // maximum resident set size // Too
	 * technical to be useful String ru_nivcsw=524.0000 // involuntary context
	 * switches String ru_minflt=9668.0000 // page reclaims String
	 * ru_nvcsw=97.0000 // voluntary context switches String
	 * ru_ixrss="ru_ixrss"; // integral shared memory size String mem="mem"; //
	 * The integral memory usage in Gbytes cpu seconds.
	 */

}
