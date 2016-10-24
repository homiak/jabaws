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

package compbio.metadata;

/**
 * The status of the job.
 * 
 * @author pvtroshin
 * 
 * @version 1.0 October 2009
 */
public enum JobStatus {

	/**
	 * Jobs which are in the queue and awaiting execution reported for cluster
	 * jobs only
	 */
	PENDING,

	/**
	 * Jobs that are running
	 */
	RUNNING,

	/**
	 * Jobs that has been cancelled
	 */
	CANCELLED,

	/**
	 * Finished jobs
	 */
	FINISHED,

	/**
	 * Failed jobs
	 */
	FAILED,

	/**
	 * Represents jobs with unknown status
	 */
	UNDEFINED,

	// These relates to the status recorded on the file system
	/**
	 * Job calculation has been started. First status reported by the local
	 * engine
	 */
	STARTED,

	/**
	 * Job has been submitted. This status is only set for cluster jobs
	 */
	SUBMITTED,

	/**
	 * Results has been collected
	 */
	COLLECTED

}
