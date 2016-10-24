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
import java.text.NumberFormat;

import org.apache.log4j.Logger;
import org.ggf.drmaa.DrmaaException;
import org.ggf.drmaa.JobInfo;
import org.ggf.drmaa.Session;

import compbio.engine.Configurator;
import compbio.engine.client.Util;
import compbio.metadata.JobExecutionException;
import compbio.metadata.JobStatus;

public class ClusterUtil {

	private static final Logger log = Logger.getLogger(ClusterUtil.class);

	public static final NumberFormat CLUSTER_STAT_IN_SEC = NumberFormat
			.getInstance();

	static {
		CLUSTER_STAT_IN_SEC.setMinimumFractionDigits(4);
	}

	public static final boolean cancelJob(final String jobId,
			ClusterSession csession) {
		assert Util.isValidJobId(jobId);
		boolean cancelled = true;
		Session session = csession.getSession();
		try {
			log.info("Job " + jobId + " is successfully cancelled");
			compbio.engine.client.Util.writeMarker(Configurator.getWorkDirectory(jobId),
					JobStatus.CANCELLED);
			session.control(ClusterSession.getClusterJobId(jobId).getJobId(),
					Session.TERMINATE);
		} catch (DrmaaException e) {
			// Log silently
			log.error("Job " + jobId + " cancellation failed!");
			log.error("Cause: " + e.getLocalizedMessage(), e.getCause());
			cancelled = false;
		} catch (IOException e) {
			log.error(
					"Could not read JOBID file to determine cluster jobid for taskid: "
							+ jobId + " Message: " + e.getLocalizedMessage(), e
							.getCause());
		} finally {
			log
					.trace("Job "
							+ jobId
							+ " has been successfully removed from the cluster engine job list");
			csession.removeJob(jobId);
		}
		return cancelled;
	}

	public static final JobInfo waitForResult(ClusterSession csession,
			String jobId) throws JobExecutionException {
		JobInfo jinfo = null;
		assert Util.isValidJobId(jobId);
		try {
			jinfo = csession.waitForJob(jobId);
		} catch (DrmaaException e) {
			log.error(e.getLocalizedMessage(), e.getCause());
			throw new JobExecutionException(e);
		} catch (IOException e) {
			log.error("Could not read JOBID file for job " + jobId
					+ " Message " + e.getMessage(), e.getCause());
			throw new JobExecutionException(e);
		} finally {
			// at this point the job has finished
			csession.removeJob(jobId);
		}
		return jinfo;
	}

}
