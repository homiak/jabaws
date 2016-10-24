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

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;

import org.testng.annotations.Test;

import compbio.engine.AsyncExecutor;
import compbio.engine.Configurator;
import compbio.engine.client.ConfiguredExecutable;
import compbio.engine.client.Executable;
import compbio.metadata.AllTestSuit;
import compbio.metadata.JobStatus;
import compbio.metadata.JobSubmissionException;
import compbio.metadata.ResultNotAvailableException;
import compbio.runner.msa.ClustalW;
import compbio.util.SysPrefs;

public class DrmaaAsyncClusterEngineTester {

	public static String test_input = AllTestSuit.TEST_DATA_PATH_ABSOLUTE
			+ "TO1381.fasta";
	public static String large_test_input = AllTestSuit.TEST_DATA_PATH_ABSOLUTE
			+ "1000x3000Dna.fasta";
	public static String cluster_test_outfile = "TO1381.clustal.cluster.out";

	/**
	 * This test uses ClustalW executable as runnable to testing, thus depends
	 * on its correct functioning
	 */
	@Test(groups = { AllTestSuit.test_group_cluster,
			AllTestSuit.test_group_engine })
	public void testSubmit() {
		ClustalW clustal = new ClustalW();
		assertFalse("Cluster execution can only be in unix environment",
				SysPrefs.isWindows);
		clustal.setInput(test_input).setOutput(cluster_test_outfile);
		try {
			ConfiguredExecutable<ClustalW> confClustal = Configurator
					.configureExecutable(clustal);
			AsyncExecutor runner = new AsyncJobRunner();
			assertNotNull("Runner is NULL", runner);
			String jobId = runner.submitJob(confClustal);
			assertEquals("Input was not set!", test_input, clustal.getInput());
			assertNotNull("JobId is null", jobId);
			JobStatus status = runner.getJobStatus(jobId);
			assertTrue("Status of the process is wrong!",
					status == JobStatus.PENDING || status == JobStatus.RUNNING);
			JobStatus info = runner.getJobStatus(jobId);
			assertNotNull("JobInfo is null", info);
			Executable<?> result = runner.getResults(jobId);

		} catch (JobSubmissionException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (ResultNotAvailableException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	@Test(expectedExceptions = ResultNotAvailableException.class, groups = {
			AllTestSuit.test_group_cluster, AllTestSuit.test_group_engine })
	// expectedExceptions = ResultNotAvailableException.class,
	public void testCancel() throws ResultNotAvailableException {
		ClustalW clustal = new ClustalW();
		assertFalse("Cluster execution can only be in unix environment",
				SysPrefs.isWindows);
		clustal.setInput(large_test_input).setOutput(cluster_test_outfile);

		try {
			ConfiguredExecutable<ClustalW> confClustal = Configurator
					.configureExecutable(clustal);
			AsyncJobRunner runner = new AsyncJobRunner();
			String jobId = runner.submitJob(confClustal);
			assertNotNull("Runner is NULL", runner);
			// assertNotNull("JobId is null", jobId1);
			Thread.sleep(200);
			JobStatus status = runner.getJobStatus(jobId);
			assertTrue("Status of the process is wrong!",
					status == JobStatus.PENDING || status == JobStatus.RUNNING);
			assertFalse("Status of the process is wrong!",
					status == JobStatus.FINISHED);
			runner.cancelJob(jobId);
			// This is never NULL as long as the job has started!
			ConfiguredExecutable<?> exec = runner.getResults(jobId);
			assertNull(exec.getResults());
		} catch (JobSubmissionException e) {
			e.printStackTrace();
			fail("DrmaaException caught:" + e.getMessage());
		} catch (InterruptedException e) {
			e.printStackTrace();
			fail("Interupted exception caught:" + e.getMessage());
		}
	}

	@Test(groups = { AllTestSuit.test_group_cluster,
			AllTestSuit.test_group_engine })
	public void testGetJobStatus() {
		ClustalW clustal = new ClustalW();
		assertFalse("Cluster execution can only be in unix environment",
				SysPrefs.isWindows);
		clustal.setInput(test_input).setOutput(cluster_test_outfile);

		try {
			AsyncJobRunner runner = new AsyncJobRunner();
			ConfiguredExecutable<ClustalW> confClustal = Configurator
					.configureExecutable(clustal);
			String jobId = runner.submitJob(confClustal);
			assertNotNull("Runner is NULL", runner);
			AsyncJobRunner runner2 = new AsyncJobRunner();

			boolean hasRun = false;
			boolean hasPended = false;
			Thread.sleep(500); 
			JobStatus status = runner2.getJobStatus(jobId);
			while (status != JobStatus.FINISHED) {
				if (status == JobStatus.CANCELLED) {
					fail("Job is not cancelled!");
				}
				if (status == JobStatus.FAILED) {
					fail("Job should not fail!");
				}
				if (status == JobStatus.RUNNING) {
					hasRun = true;
				}
				if (status == JobStatus.PENDING) {
					hasPended = true;
				}
				if (status == JobStatus.UNDEFINED) {
					System.out.println("Wrong status (UNDEFINED) reported by cluster engine!");
					break;
				}
				status = runner2.getJobStatus(jobId);
			}
			assertTrue(hasRun);
			assertTrue(hasPended);
			assertTrue(hasRun);
			// Bear in mind that if the task were not put in the queue
			// immediately
			// the status could be UNDEFINED!
			// assertFalse(hasUndefined);
			AsyncJobRunner runner3 = new AsyncJobRunner();
			Executable<?> exec = runner3.getResults(jobId);
			assertNotNull(exec);
			// Now try collecting result for the second time
			exec=null;
			Thread.sleep(1000);
			exec = runner3.getResults(jobId);
			assertNotNull(exec);
		} catch (JobSubmissionException e) {
			e.printStackTrace();
			fail("DrmaaException caught:" + e.getMessage());
		} catch (ResultNotAvailableException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (InterruptedException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
}
