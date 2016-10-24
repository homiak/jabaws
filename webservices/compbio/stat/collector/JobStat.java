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

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

import compbio.engine.client.ConfExecutable;
import compbio.util.Util;
import compbio.ws.client.Services;

public class JobStat {

	static final Comparator<JobStat> RUNTIME = new Comparator<JobStat>() {
		@Override
		public int compare(JobStat o1, JobStat o2) {
			return new Integer(o2.getRuntime()).compareTo(o1.getRuntime());
		}
	};

	static final Comparator<JobStat> STARTTIME = new Comparator<JobStat>() {
		@Override
		public int compare(JobStat o1, JobStat o2) {
			return new Long(o1.start).compareTo(o2.start);
		}
	};

	static final Comparator<JobStat> RESULTSIZE = new Comparator<JobStat>() {
		@Override
		public int compare(JobStat o1, JobStat o2) {
			return new Long(o2.resultSize).compareTo(o1.resultSize);
		}
	};

	private static DateFormat DATE_TIME = SimpleDateFormat.getDateTimeInstance(
			DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.UK);

	Services webService;
	String clusterJobId;
	String jobname;
	long start;
	long finish;
	long inputSize;
	long resultSize;
	boolean isCollected;
	boolean isCancelled;

	private JobStat(Services webService, String clusterJobId, String jobname,
			long start, long finish, long inputSize, long resultSize,
			boolean isCancelled, boolean isCollected) {
		super();
		this.webService = webService;
		this.clusterJobId = clusterJobId;
		this.jobname = jobname;
		this.start = start;
		this.finish = finish;
		this.inputSize = inputSize;
		this.resultSize = resultSize;
		this.isCancelled = isCancelled;
		this.isCollected = isCollected;
		validate();
	}

	static JobStat newInstance(Services webService, String clusterJobId,
			String jobname, long start, long finish, long inputSize,
			long resultSize, boolean isCancelled, boolean isCollected) {
		return new JobStat(webService, clusterJobId, jobname, start, finish,
				inputSize, resultSize, isCancelled, isCollected);
	}

	static JobStat newInstance(Services webService, String clusterJobId,
			String jobname, Timestamp start, Timestamp finish, long inputSize,
			long resultSize, boolean isCancelled, boolean isCollected) {
		long startm = ExecutionStatCollector.UNDEFINED;
		long stopm = ExecutionStatCollector.UNDEFINED;
		if (start != null) {
			startm = start.getTime();
		}
		if (finish != null) {
			stopm = finish.getTime();
		}
		return new JobStat(webService, clusterJobId, jobname, startm, stopm,
				inputSize, resultSize, isCancelled, isCollected);
	}

	void validate() {
		if (webService == null) {
			throw new AssertionError("webService must be defined!:\n " + this);
		}
		if (Util.isEmpty(jobname)) {
			throw new AssertionError("jobname must be defined!:\n" + this);
		}
	}

	private JobStat(String jobId) {
		assert !Util.isEmpty(jobname);
		this.jobname = jobId;
	}

	static JobStat newIncompleteStat(String jobname) {
		return new JobStat(jobname);
	}

	public boolean isClusterJob() {
		return jobname.startsWith(ConfExecutable.CLUSTER_TASK_ID_PREFIX);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((jobname == null) ? 0 : jobname.hashCode());
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
		JobStat other = (JobStat) obj;
		if (jobname == null) {
			if (other.jobname != null)
				return false;
		} else if (!jobname.equals(other.jobname))
			return false;
		return true;
	}

	public int getRuntime() {
		if (start != ExecutionStatCollector.UNDEFINED
				&& finish != ExecutionStatCollector.UNDEFINED) {
			return (int) (finish - start) / 1000;
		}
		return ExecutionStatCollector.UNDEFINED;
	}

	@Override
	public String toString() {
		return getJobReport();
	}

	String getJobReport() {
		String report = "WS: " + webService + "\n";
		report += "JOB: " + jobname + "\n";
		if (start != ExecutionStatCollector.UNDEFINED) {
			report += "Started " + new Date(start) + "\n";
		}
		if (finish != ExecutionStatCollector.UNDEFINED) {
			report += "Finished " + new Date(finish) + "\n";
		}
		if (start != ExecutionStatCollector.UNDEFINED
				&& finish != ExecutionStatCollector.UNDEFINED) {
			report += "Runtime " + getRuntime() + "\n";
		}
		report += "Input size " + inputSize + "\n";
		report += "Result size " + resultSize + "\n";
		report += "ClusterJobID " + clusterJobId + "\n";
		report += "Collected? " + isCollected + "\n";
		report += "Cancelled? " + isCancelled + "\n";
		return report;
	}

	/**
	 * Header Job Started Finished Runtime Input Result
	 */
	String getJobReportTabulated() {
		String report = webService + "\t";
		report += jobname + "\t";
		if (start != ExecutionStatCollector.UNDEFINED) {
			report += ExecutionStatCollector.DF.format(new Date(start)) + "\t";
		} else {
			report += ExecutionStatCollector.UNDEFINED + "\t";
		}
		if (finish != ExecutionStatCollector.UNDEFINED) {
			report += ExecutionStatCollector.DF.format(new Date(finish)) + "\t";
		} else {
			report += ExecutionStatCollector.UNDEFINED + "\t";
		}
		if (start != ExecutionStatCollector.UNDEFINED
				&& finish != ExecutionStatCollector.UNDEFINED) {
			report += getRuntime() + "\t";
		} else {
			report += ExecutionStatCollector.UNDEFINED + "\t";
		}
		report += inputSize + "\t";
		report += resultSize + "\t";
		report += clusterJobId + "\t";
		report += isCollected + "\t";
		report += isCancelled + "\t";
		return report;
	}

	public Services getWebService() {
		return webService;
	}

	public String getClusterJobId() {
		return clusterJobId;
	}

	public String getJobname() {
		return jobname;
	}

	public String getEscJobname() {
		String[] parts = jobname.split("#");
		return parts[0] + "%23" + parts[1];
	}

	public String getStart() {
		if (start != ExecutionStatCollector.UNDEFINED) {
			return DATE_TIME.format(new Date(start));
		}
		return "?";
	}

	public String getFinish() {
		if (finish != ExecutionStatCollector.UNDEFINED) {
			return DATE_TIME.format(new Date(finish));
		}
		return "?";
	}

	public long getInputSize() {
		if (inputSize != ExecutionStatCollector.UNDEFINED) {
			return inputSize;
		}
		return 0;
	}

	public long getResultSize() {
		if (resultSize > 0) {
			return resultSize;
		}
		return 0;
	}

	public boolean hasResult() {
		return resultSize > 0;
	}

	public boolean hasStarted() {
		return start != ExecutionStatCollector.UNDEFINED;
	}

	public boolean getIsCollected() {
		return isCollected;
	}

	public boolean getIsCancelled() {
		return isCancelled;
	}

	public boolean getIsFinished() {
		return finish != ExecutionStatCollector.UNDEFINED;
	}

}