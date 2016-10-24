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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNotSame;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.io.File;
import java.util.concurrent.CancellationException;

import org.testng.annotations.Test;

import compbio.data.sequence.Alignment;
import compbio.engine.AsyncExecutor;
import compbio.engine.Configurator;
import compbio.engine.client.ConfiguredExecutable;
import compbio.engine.client.Executable;
import compbio.engine.client.Util;
import compbio.metadata.AllTestSuit;
import compbio.metadata.JobStatus;
import compbio.metadata.JobSubmissionException;
import compbio.metadata.ResultNotAvailableException;
import compbio.runner.msa.ClustalW;
import compbio.runner.msa.Muscle;
import compbio.util.SysPrefs;

public class AsyncLocalRunnerTester {

	// Input path must be absolute to avoid coping input in working directory
	public static String unix_test_input = AllTestSuit.CURRENT_DIRECTORY
			+ File.separator + AllTestSuit.TEST_DATA_PATH + "TO1381.fasta";

	public static String unix_test_AVG_input = AllTestSuit.CURRENT_DIRECTORY
			+ File.separator + AllTestSuit.TEST_DATA_PATH
			+ "50x500Protein.fasta";

	public static String unix_test_LARGE_input = AllTestSuit.CURRENT_DIRECTORY
			+ File.separator + AllTestSuit.TEST_DATA_PATH
			+ "200x500Protein.fasta";

	// Output file will be located in the task working directory, thus only name
	// is required
	public static String unix_test_outfile = "TO1381.alignment.out";
	public static String cluster_test_outfile = "TO1381.alignment.cluster.out";

	// Input path must be absolute to avoid coping input in working directory
	public static String win_test_input = AllTestSuit.CURRENT_DIRECTORY
			+ File.separator + AllTestSuit.TEST_DATA_PATH + "TO1381.fasta";

	public static String win_test_LARGE_input = AllTestSuit.CURRENT_DIRECTORY
			+ File.separator + AllTestSuit.TEST_DATA_PATH
			+ "200x500Protein.fasta";

	public static String win_test_AVG_input = AllTestSuit.CURRENT_DIRECTORY
			+ File.separator + AllTestSuit.TEST_DATA_PATH
			+ "50x500Protein.fasta";

	// Output file will be located in the task working directory, thus only name
	// is required
	public static String win_test_outfile = "TO1381.alignment.out";

	@Test(expectedExceptions = { CancellationException.class,
			ResultNotAvailableException.class }, groups = { AllTestSuit.test_group_engine })
	public void testCancelLocally() throws ResultNotAvailableException {
		System.out.println("Running testCancelLocally");
		ClustalW clustal = new ClustalW();

		if (SysPrefs.isWindows) {
			System.out.println("Working in WINDOWS environment");
			clustal.setInput(win_test_input).setOutput(win_test_outfile);
		} else {
			System.out.println("Working in UNIX environment");
			clustal.setInput(unix_test_input).setOutput(unix_test_outfile);
		}
		AsyncExecutor lr = new AsyncLocalRunner();
		try {
			ConfiguredExecutable<ClustalW> confClust = Configurator
					.configureExecutable(clustal);
			String jobId = lr.submitJob(confClust);
			// Thread.sleep(10); //wait for 100ms
			assertNotSame(lr.getJobStatus(jobId), JobStatus.FINISHED,
					"Job has finished already. Too late to test cancel!");
			lr.cancelJob(jobId);
			// This call causes CancellationException to be thrown
			Executable<?> clustalr = lr.getResults(jobId);
			// @see AsyncLocalRunner.cleanup documents
			// assertTrue(lr.cleanup(jobId));
		} catch (JobSubmissionException e) {
			e.printStackTrace();
			fail(e.getLocalizedMessage());
		}
	}

	@Test(invocationCount = 5, threadPoolSize = 4, groups = { AllTestSuit.test_group_engine })
	public void testSubmitLocally() {
		System.out.println("Running testSubmitLocally()");
		Muscle muscle = new Muscle();

		if (SysPrefs.isWindows) {
			System.out.println("Working in WINDOWS environment");
			muscle.setInput(win_test_input).setOutput(win_test_outfile);
		} else {
			System.out.println("Working in UNIX environment");
			muscle.setInput(unix_test_input).setOutput(unix_test_outfile);
		}
		AsyncExecutor lr = new AsyncLocalRunner();
		try {
			ConfiguredExecutable<Muscle> confMuscle = Configurator
					.configureExecutable(muscle);
			String jobId = lr.submitJob(confMuscle);
			// Thread.sleep(10); //wait for 100ms
			// assertNotSame("Job has finished already. Too late to test cancel!",
			// JobStatus.FINISHED, lr.getJobStatus(jobId));
			// This call causes CancellationException to be thrown
			Executable<?> muscler = lr.getResults(jobId);
			Alignment al = confMuscle.getResults();
			assertNotNull(al);
		} catch (JobSubmissionException e) {
			e.printStackTrace();
			fail(e.getLocalizedMessage());
		} catch (ResultNotAvailableException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	@Test(invocationCount = 5, threadPoolSize = 4, groups = { AllTestSuit.test_group_engine })
	public void testGetStatus() {
		System.out.println("Running testGetStatus");
		Muscle muscle = new Muscle();

		if (SysPrefs.isWindows) {
			System.out.println("Working in WINDOWS environment");
			muscle.setInput(win_test_AVG_input).setOutput(win_test_outfile);
		} else {
			System.out.println("Working in UNIX environment");
			muscle.setInput(unix_test_AVG_input).setOutput(unix_test_outfile);
		}
		AsyncExecutor lr = new AsyncLocalRunner();
		try {
			ConfiguredExecutable<Muscle> confMuscle = Configurator
					.configureExecutable(muscle);
			String jobId = lr.submitJob(confMuscle);
			// Thread.sleep(10); //wait for 100ms
			JobStatus status = lr.getJobStatus(jobId);
			while (status == JobStatus.UNDEFINED) {
				assertTrue(status == JobStatus.UNDEFINED);
				Thread.sleep(100);
				status = lr.getJobStatus(jobId);
			}
			while (status != JobStatus.FINISHED) {
				Thread.sleep(500);
				assertTrue(status == JobStatus.RUNNING);
				status = lr.getJobStatus(jobId);
			}
			// assert that we get here, means that the job reached FINISHED
			// status
			// This call causes CancellationException to be thrown
			Executable<?> muscler = lr.getResults(jobId);
			/*
			 * After results were obtained the task were removed for the queue,
			 * and it status could not be determined
			 */
			// Make sure list has been updated
			Thread.sleep(200);
			status = lr.getJobStatus(jobId);
			assertTrue(status == JobStatus.FINISHED);

			Alignment al = confMuscle.getResults();

			assertNotNull(al);

		} catch (JobSubmissionException e) {
			e.printStackTrace();
			fail(e.getLocalizedMessage());
		} catch (InterruptedException e) {
			e.printStackTrace();
			fail(e.getLocalizedMessage());
		} catch (ResultNotAvailableException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	@Test(expectedExceptions = CancellationException.class, groups = { AllTestSuit.test_group_engine })
	public void testMultipleCancelLocally() {
		System.out.println("Running testMultipleCancelLocally()");
		ClustalW clustal = new ClustalW();
		ClustalW clustal2 = new ClustalW();
		if (SysPrefs.isWindows) {
			System.out.println("Working in WINDOWS environment");
			clustal.setInput(win_test_input).setOutput(win_test_outfile);
			clustal2.setInput(win_test_input).setOutput(win_test_outfile);
		} else {
			System.out.println("Working in UNIX environment");
			clustal.setInput(unix_test_input).setOutput(unix_test_outfile);
			clustal2.setInput(unix_test_input).setOutput(unix_test_outfile);
		}
		try {
			ConfiguredExecutable<ClustalW> confClustal1 = Configurator
					.configureExecutable(clustal);
			ConfiguredExecutable<ClustalW> confClustal2 = Configurator
					.configureExecutable(clustal2);
			AsyncLocalRunner lr = new AsyncLocalRunner();
			AsyncLocalRunner lr2 = new AsyncLocalRunner();

			String jobId1 = lr.submitJob(confClustal1);
			String jobId2 = lr2.submitJob(confClustal2);

			// Thread.sleep(10); //wait for 100ms
			assertNotSame(lr.getJobStatus(jobId1), JobStatus.FINISHED,
					"Job has finished already. Too late to test cancel!");
			lr.cancelJob(jobId1);
			// Thread.sleep(10);
			assertNotSame(lr2.getJobStatus(jobId2), JobStatus.FINISHED,
					"Job has finished already. Too late to test cancel!");
			lr2.cancelJob(jobId2);
			// This call causes CancellationException to be thrown
			Executable<?> clustalr = lr.getResults(jobId1);
			Executable<?> clustalr2 = lr2.getResults(jobId2);

			assertTrue(lr.cleanup(jobId1));
			assertTrue(lr2.cleanup(jobId2));

		} catch (JobSubmissionException e) {
			e.printStackTrace();
			fail(e.getLocalizedMessage());
		} catch (ResultNotAvailableException e) {
			e.printStackTrace();
			fail(e.getLocalizedMessage());
		}
	}

	@Test(groups = { AllTestSuit.test_group_engine })
	public void testCancelCompletedTaskLocally() {
		System.out.println("Running testCancelCompletedTaskLocally");
		ClustalW clustal = new ClustalW();
		if (SysPrefs.isWindows) {
			System.out.println("Working in WINDOWS environment");
			clustal.setInput(win_test_input).setOutput(win_test_outfile);
		} else {
			System.out.println("Working in UNIX environment");
			clustal.setInput(unix_test_input).setOutput(unix_test_outfile);
		}
		try {
			ConfiguredExecutable<ClustalW> confClustal1 = Configurator
					.configureExecutable(clustal);
			AsyncLocalRunner lr = new AsyncLocalRunner();
			String jobId = lr.submitJob(confClustal1);
			Thread.currentThread();
			Thread.sleep(3000); // wait for 100ms
			assertEquals(lr.getJobStatus(jobId), JobStatus.FINISHED,
					"Job has not finished!");
			lr.cancelJob(jobId);
			// This call causes CancellationException to be thrown
			Executable<?> clustalr = lr.getResults(jobId);
			assertNotNull(clustalr);

			assertTrue(Util.isMarked(confClustal1.getWorkDirectory(),
					JobStatus.CANCELLED));
			assertTrue(Util.isMarked(confClustal1.getWorkDirectory(),
					JobStatus.STARTED));
			assertTrue(Util.isMarked(confClustal1.getWorkDirectory(),
					JobStatus.COLLECTED));
			assertTrue(Util.isMarked(confClustal1.getWorkDirectory(),
					JobStatus.FINISHED));
			assertFalse(Util.isMarked(confClustal1.getWorkDirectory(),
					JobStatus.SUBMITTED));
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

	@Test(groups = { AllTestSuit.test_group_engine })
	public void testAsyncRetrievOperation() {
		System.out.println("Running testAsyncOperation()");
		Muscle muscle = new Muscle();

		if (SysPrefs.isWindows) {
			System.out.println("Working in WINDOWS environment");
			muscle.setInput(win_test_input).setOutput(win_test_outfile);
		} else {
			System.out.println("Working in UNIX environment");
			muscle.setInput(unix_test_input).setOutput(unix_test_outfile);
		}

		try {
			ConfiguredExecutable<Muscle> confMuscle = Configurator
					.configureExecutable(muscle);
			AsyncExecutor lr = new AsyncLocalRunner();
			String jobId = lr.submitJob(confMuscle);
			AsyncLocalRunner as = new AsyncLocalRunner();
			ConfiguredExecutable<Muscle> muscler = (ConfiguredExecutable<Muscle>) as
					.getResults(jobId);
			assertNotNull(muscler);
			Alignment al1 = muscler.getResults();

			assertTrue(Util.isMarked(muscler.getWorkDirectory(),
					JobStatus.STARTED));
			assertTrue(Util.isMarked(muscler.getWorkDirectory(),
					JobStatus.COLLECTED));
			assertTrue(Util.isMarked(muscler.getWorkDirectory(),
					JobStatus.FINISHED));
			assertFalse(Util.isMarked(muscler.getWorkDirectory(),
					JobStatus.CANCELLED));
			assertFalse(Util.isMarked(muscler.getWorkDirectory(),
					JobStatus.SUBMITTED));
			Alignment al2 = confMuscle.getResults();
			assertNotNull(al1);
			assertNotNull(al2);
			assertTrue(al1.equals(al2));
		} catch (JobSubmissionException e) {
			e.printStackTrace();
			fail(e.getLocalizedMessage());
		} catch (ResultNotAvailableException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	@Test(groups = { AllTestSuit.test_group_engine })
	public void testAsyncCancelOperation() {
		System.out.println("Running testAsyncOperation()");
		Muscle muscle = new Muscle();

		if (SysPrefs.isWindows) {
			System.out.println("Working in WINDOWS environment");
			muscle.setInput(win_test_input).setOutput(win_test_outfile);
		} else {
			System.out.println("Working in UNIX environment");
			muscle.setInput(unix_test_input).setOutput(unix_test_outfile);
		}

		try {
			ConfiguredExecutable<Muscle> confMuscle = Configurator
					.configureExecutable(muscle);
			AsyncExecutor lr = new AsyncLocalRunner();
			String jobId = lr.submitJob(confMuscle);
			// This call causes CancellationException to be thrown
			AsyncLocalRunner as = new AsyncLocalRunner();
			assertTrue(as.getJobStatus(jobId) != JobStatus.FINISHED);
			as.cancelJob(jobId);
			assertTrue(as.getJobStatus(jobId) == JobStatus.CANCELLED);

			assertTrue(Util.isMarked(confMuscle.getWorkDirectory(),
					JobStatus.CANCELLED));
			// could be both
			// assertFalse(Util.isMarked(confMuscle.getWorkDirectory(),
			// Util.StatFileType.STARTED));
			assertFalse(Util.isMarked(confMuscle.getWorkDirectory(),
					JobStatus.COLLECTED));
			assertFalse(Util.isMarked(confMuscle.getWorkDirectory(),
					JobStatus.FINISHED));
			assertFalse(Util.isMarked(confMuscle.getWorkDirectory(),
					JobStatus.SUBMITTED));
			// Executable<?> muscler = as.getResult(jobId);
			// assertNotNull(muscler);
			// Alignment al = muscle.getResults();
			// assertNotNull(al);
		} catch (JobSubmissionException e) {
			e.printStackTrace();
			fail(e.getLocalizedMessage());
		}
	}
}
