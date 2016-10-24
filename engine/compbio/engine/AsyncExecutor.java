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

import compbio.engine.client.ConfiguredExecutable;
import compbio.metadata.JobStatus;
import compbio.metadata.JobSubmissionException;
import compbio.metadata.ResultNotAvailableException;

/**
 * An asynchronous executor engine, capable of running, cancelling, 
 * obtaining results calculated by a native executable wrapper in Executable interface. 
 * Implementation agnostic. Executables can be run either locally to the JVM or on the cluster. 
 *    
 * @author pvtroshin
 *		Date October 2009
 */
public interface AsyncExecutor {

	/**
	 * Submits job for the execution
	 * Immediate execution is not guaranteed, this method puts the job in the queue. 
	 * All it guarantees that the job will be eventually executed. 
	 * The start of execution will depend on the number of jobs in the queue.
	 *  
	 * @return unique job identifier
	 * @throws JobSubmissionException
	 *             if submission fails. This usually happens due to the problem on a server side. 
	 */
	String submitJob(ConfiguredExecutable<?> executable)
			throws JobSubmissionException;

	/**
	 * Retrieve the results of the job. Please not that current implementations of this method 
	 * blocks if the task is running until the end of the calculation.  
	 * 
	 * @param jobId job identifier obtained at the job submission 
	 * @return ConfiguredExecutable object from which result can be obtained
	 * @throws ResultNotAvailableException if the result is not available for whatever reason. 
	 * Could be due to execution failure, or due to the results being removed from the server at 
	 * the time of request.  
	 */
	ConfiguredExecutable<?> getResults(String jobId)
			throws ResultNotAvailableException;

	/**
	 * 
	 * @param jobId unique job identifier 
	 * @return task working directory
	 */
	String getWorkDirectory(String jobId);

	/**
	 * Remove all files and a job directory for a jobid. 
	 * @param jobId
	 * @return true if job directory was successfully removed, false otherwise.  
	 */
	boolean cleanup(String jobId);

	/**
	 * Stop running job. Please not that this method does not guarantee to remove the job directory and files in it.  
	 * 
	 * @return true if job was cancelled successfully, false otherwise
	 */
	boolean cancelJob(String jobId);

	/**
	 *  Query the status of the job 
	 * @param String
	 *            jobId - unique job identifier
	 * @return The JobStatus object representing the status of the job
	 * @see JobStatus
	 */
	JobStatus getJobStatus(String jobId);

}
