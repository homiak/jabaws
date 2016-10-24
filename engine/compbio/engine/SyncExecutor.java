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
import compbio.metadata.JobExecutionException;
import compbio.metadata.JobStatus;
import compbio.metadata.JobSubmissionException;

/**
 * Synchronous executor, is an engine to run the Executable synchronously.
 *  
 * @author pvtroshin
 *		Date October 2009
 */
public interface SyncExecutor {

	/**
	 * Execute the job 
	 * 
	 * @return String - job unique identifier
	 * @throws JobSubmissionException
	 *             if submission fails
	 */
	void executeJob() throws JobSubmissionException;

	/**
	 * Clean up after the job 
	 * @return true if all the files created by this job have been removed successfully, false otherwise
	 */
	boolean cleanup();

	
	/**
	 *
	 * Call to this method block for as long as it is required for an executable to finish its job. 
	 * If the calculation has been completed already, the this method returns results immediately. 
	 * This could return the result directly, but that would be type unsafe
	 *
	 * @return object from wich the result can be obtained
	 * @throws JobExecutionException
	 */
	ConfiguredExecutable<?> waitForResult() throws JobExecutionException;

	/**
	 * 
	 * @return working directory if the task 
	 */
	String getWorkDirectory();

	/**
	 * Stops running job. 
	 * Clean up is not performed. 
	 * 
	 * @return true if job was cancelled successfully, false otherwise
	 */
	boolean cancelJob();

	/**
	 * Query the status of the job by its id. 
	 * @return - JobStatus 
	 */
	JobStatus getJobStatus();

}
