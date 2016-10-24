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
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;

import java.text.ParseException;

import org.ggf.drmaa.DrmaaException;
import org.ggf.drmaa.JobInfo;
import org.testng.annotations.Test;

import compbio.data.sequence.Alignment;
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
import compbio.util.SysPrefs;

public class DrmaaClusterEngineTester {

	public static String test_input = AllTestSuit.TEST_DATA_PATH_ABSOLUTE
			+ "TO1381.fasta";
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
			ConfiguredExecutable<ClustalW> confClust = Configurator
					.configureExecutable(clustal,
							Executable.ExecProvider.Cluster);
			assertNotNull(confClust.getWorkDirectory());

			JobRunner runner = JobRunner.getInstance(confClust);
			assertEquals("Input was not set!", test_input, clustal.getInput());
			assertNotNull("Runner is NULL", runner);
			runner.executeJob();
			// assertNotNull("JobId is null", jobId1);
			JobStatus status = runner.getJobStatus();
			assertTrue("Status of the process is wrong!",
					status == JobStatus.PENDING || status == JobStatus.RUNNING);
			JobInfo info = runner.getJobInfo();

			assertFalse(Util.isMarked(confClust.getWorkDirectory(),
					JobStatus.CANCELLED));
			assertFalse(Util.isMarked(confClust.getWorkDirectory(),
					JobStatus.STARTED));
			assertFalse(Util.isMarked(confClust.getWorkDirectory(),
					JobStatus.COLLECTED));
			assertTrue(Util.isMarked(confClust.getWorkDirectory(),
					JobStatus.FINISHED));
			assertTrue(Util.isMarked(confClust.getWorkDirectory(),
					JobStatus.SUBMITTED));

			ConfiguredExecutable<?> confExec = runner.waitForResult();
			// At this point results are marked as collected
			assertTrue(Util.isMarked(confClust.getWorkDirectory(),
					JobStatus.COLLECTED));

			assertNotNull("JobInfo is null", info);
			StatisticManager sm = new StatisticManager(info);
			assertNotNull("Statistics manager is null", sm);

			try {

				String exits = sm.getExitStatus();
				assertNotNull("Exit status is null", exits);
				// cut 4 trailing zeros from the number
				int exitsInt = ClusterUtil.CLUSTER_STAT_IN_SEC.parse(exits)
						.intValue();
				assertEquals("Exit status is not 0", 0, exitsInt);
				System.out.println(sm.getAllStats());

			} catch (ParseException e) {
				e.printStackTrace();
				fail("Parse Exception: " + e.getMessage());
			}
			assertTrue("Process exited:", sm.hasExited());
			assertFalse("Process aborted:", sm.wasAborted());
			assertFalse("Process hasdump:", sm.hasDump());
			assertFalse("Process signaled:", sm.hasSignaled());

			Alignment al = confExec.getResults();
			assertNotNull(al);
			assertFalse("Could not remove some files whilst cleaning up ",
					runner.cleanup());

		} catch (JobSubmissionException e) {
			e.printStackTrace();
			fail("DrmaaException caught:" + e.getMessage());
		} catch (JobExecutionException e) {
			e.printStackTrace();
			fail("DrmaaException caught:" + e.getMessage());
		} catch (DrmaaException e) {
			e.printStackTrace();
			fail("DrmaaException caught:" + e.getMessage());
		} catch (ResultNotAvailableException e) {
			fail("DrmaaException caught:" + e.getMessage());
		}
	}

	@Test(groups = { AllTestSuit.test_group_cluster,
			AllTestSuit.test_group_engine })
	public void testCancel() {
		ClustalW clustal = new ClustalW();
		assertFalse("Cluster execution can only be in unix environment",
				SysPrefs.isWindows);
		clustal.setInput(test_input).setOutput(cluster_test_outfile);

		try {

			ConfiguredExecutable<ClustalW> confClust = Configurator
					.configureExecutable(clustal,
							Executable.ExecProvider.Cluster);
			assertNotNull(confClust.getWorkDirectory());

			JobRunner runner = JobRunner.getInstance(confClust);
			assertNotNull("Runner is NULL", runner);

			runner.executeJob();
			// assertNotNull("JobId is null", jobId1);
			Thread.sleep(500);
			JobStatus status = runner.getJobStatus();
			assertTrue("Status of the process is wrong!",
					status == JobStatus.PENDING || status == JobStatus.RUNNING);
			runner.cancelJob();
			Thread.sleep(200); // give fs time to write a file
			JobInfo info = runner.getJobInfo();
			assertTrue(Util.isMarked(confClust.getWorkDirectory(),
					JobStatus.CANCELLED));
			assertFalse(Util.isMarked(confClust.getWorkDirectory(),
					JobStatus.STARTED));
			assertFalse(Util.isMarked(confClust.getWorkDirectory(),
					JobStatus.COLLECTED));
			assertTrue(Util.isMarked(confClust.getWorkDirectory(),
					JobStatus.FINISHED));
			assertTrue(Util.isMarked(confClust.getWorkDirectory(),
					JobStatus.SUBMITTED));

		} catch (JobSubmissionException e) {
			e.printStackTrace();
			fail("DrmaaException caught:" + e.getMessage());
		} catch (JobExecutionException e) {
			e.printStackTrace();
			fail("DrmaaException caught:" + e.getMessage());
		} catch (InterruptedException e) {
			e.printStackTrace();
			fail("Interupted exception caught:" + e.getMessage());
		}
	}

}
