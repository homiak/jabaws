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

package compbio.engine.local;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.AssertJUnit.assertNotSame;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.fail;

import java.util.concurrent.CancellationException;

import org.testng.annotations.Test;

import compbio.engine.Configurator;
import compbio.engine.client.ConfiguredExecutable;
import compbio.engine.client.Executable;
import compbio.engine.client.Util;
import compbio.metadata.AllTestSuit;
import compbio.metadata.JobExecutionException;
import compbio.metadata.JobStatus;
import compbio.metadata.JobSubmissionException;
import compbio.metadata.ResultNotAvailableException;
import compbio.runner.msa.ClustalW;

public class LocalRunnerTester {

	public static String cluster_test_outfile = "TO1381.clustal.cluster.out"; // "/homes/pvtroshin/TO1381.clustal.cluster.out
	// go up 2 directories from workspace: workspace/clustengine
	public static String test_input = AllTestSuit.TEST_DATA_PATH_ABSOLUTE
			+ "TO1381.fasta";
	public static String test_outfile = "TO1381.clustal.out";

	@Test(expectedExceptions = CancellationException.class, groups = { AllTestSuit.test_group_engine })
	public void testCancelLocally() {
		ClustalW clustal = new ClustalW();
		clustal.setInput(test_input).setOutput(test_outfile);

		try {
			ConfiguredExecutable<ClustalW> confClust = Configurator
					.configureExecutable(clustal);
			LocalRunner lr = new LocalRunner(confClust);

			lr.executeJob();
			// Thread.sleep(10); //wait for 100ms
			assertNotSame("Job has finished already. Too late to test cancel!",
					JobStatus.FINISHED, lr.getJobStatus());
			lr.cancelJob();
			// This call causes CancellationException to be thrown
			Executable<?> clustalr = lr.waitForResult();
			assertTrue(Util.isMarked(confClust.getWorkDirectory(),
					JobStatus.CANCELLED));
			assertTrue(Util.isMarked(confClust.getWorkDirectory(),
					JobStatus.STARTED));
			assertTrue(Util.isMarked(confClust.getWorkDirectory(),
					JobStatus.COLLECTED));
			assertTrue(Util.isMarked(confClust.getWorkDirectory(),
					JobStatus.FINISHED));
			assertFalse(Util.isMarked(confClust.getWorkDirectory(),
					JobStatus.SUBMITTED));
		} catch (JobSubmissionException e) {
			e.printStackTrace();
			fail(e.getLocalizedMessage());
		} catch (JobExecutionException e) {
			e.printStackTrace();
			fail(e.getLocalizedMessage());
		}
	}

	@Test(expectedExceptions = { CancellationException.class,
			JobExecutionException.class }, groups = { AllTestSuit.test_group_engine })
	public void testMultipleCancelLocally() throws JobExecutionException {
		ClustalW clustal = new ClustalW();
		ClustalW clustal2 = new ClustalW();
		clustal.setInput(test_input).setOutput(test_outfile);
		clustal2.setInput(test_input).setOutput(test_outfile);
		try {
			ConfiguredExecutable<ClustalW> confClust = Configurator
					.configureExecutable(clustal);
			ConfiguredExecutable<ClustalW> confClust2 = Configurator
					.configureExecutable(clustal2);

			LocalRunner lr = new LocalRunner(confClust);
			LocalRunner lr2 = new LocalRunner(confClust2);

			lr.executeJob();
			lr2.executeJob();

			// Thread.sleep(10); //wait for 100ms
			assertNotSame("Job has finished already. Too late to test cancel!",
					JobStatus.FINISHED, lr.getJobStatus());
			lr.cancelJob();
			// Thread.sleep(10);
			assertNotSame("Job has finished already. Too late to test cancel!",
					JobStatus.FINISHED, lr2.getJobStatus());
			lr2.cancelJob();
			// This call causes CancellationException to be thrown
			Executable<?> clustalr = lr.waitForResult();
			Executable<?> clustalr2 = lr2.waitForResult();

		} catch (JobSubmissionException e) {
			e.printStackTrace();
			fail(e.getLocalizedMessage());
		}
	}

	@Test(expectedExceptions = { CancellationException.class }, groups = { AllTestSuit.test_group_engine })
	public void testCancelCompletedTaskLocally() throws JobExecutionException {
		ClustalW clustal = new ClustalW();
		clustal.setInput(test_input).setOutput(test_outfile);

		try {
			ConfiguredExecutable<ClustalW> confClust = Configurator
					.configureExecutable(clustal, Executable.ExecProvider.Local);
			LocalRunner lr = new LocalRunner(confClust);
			lr.executeJob();
			Thread.currentThread();
			Thread.sleep(30); // wait for 100ms
			assertNotSame("Job has not finished!", JobStatus.FINISHED, lr
					.getJobStatus());
			lr.cancelJob();
			// This call causes ResultNotAvailableException to be thrown
			ConfiguredExecutable<?> clustalr = lr.waitForResult();
			assertNull(clustalr.getResults());
		} catch (JobSubmissionException e) {
			e.printStackTrace();
			fail(e.getLocalizedMessage());
		} catch (InterruptedException e) {
			e.printStackTrace();
			fail(e.getLocalizedMessage());
		} catch (ResultNotAvailableException e) {
			e.printStackTrace();
			fail(e.getLocalizedMessage());
		}
	}

}
