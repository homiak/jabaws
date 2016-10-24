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

import java.util.List;

import compbio.engine.client.ConfiguredExecutable;
import compbio.engine.client.Util;
import compbio.util.annotation.Immutable;

@Immutable
public final class Job {

	private final ClusterJobId jobId;
	private final String taskId;
	private final ConfiguredExecutable<?> cexecutable;

	public Job(String taskId, String jobId, ConfiguredExecutable<?> cexecutable) {
		if (!Util.isValidJobId(taskId)) {
			throw new IllegalArgumentException("TaskId " + taskId
					+ " is not valid!");
		}
		if (jobId == null) {
			throw new NullPointerException("Cluster JobId must be provided!");
		}
		if (cexecutable == null) {
			throw new NullPointerException(
					"ConfiguredExecutable must be provided!");
		}
		this.jobId = new ClusterJobId(jobId);
		this.taskId = taskId;
		this.cexecutable = cexecutable;
	}

	public ClusterJobId getJobId() {
		return jobId;
	}

	public String getTaskId() {
		return taskId;
	}

	public ConfiguredExecutable<?> getConfExecutable() {
		return cexecutable;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((jobId == null) ? 0 : jobId.hashCode());
		result = prime * result + ((taskId == null) ? 0 : taskId.hashCode());
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
		Job other = (Job) obj;
		if (jobId == null) {
			if (other.jobId != null)
				return false;
		} else if (!jobId.equals(other.jobId))
			return false;
		if (taskId == null) {
			if (other.taskId != null)
				return false;
		} else if (!taskId.equals(other.taskId))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Job [cexecutable=" + cexecutable + ", jobId=" + jobId
				+ ", taskId=" + taskId + "]";
	}

	public static Job getByTaskId(String taskId, List<Job> jobs) {
		for (Job j : jobs) {
			if (taskId.equals(j.getTaskId())) {
				return j;
			}
		}
		return null;
	}

	public static Job getByJobId(String jobId, List<Job> jobs) {
		for (Job j : jobs) {
			if (jobId.equals(j.getJobId())) {
				return j;
			}
		}
		return null;
	}
}
