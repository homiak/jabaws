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

package compbio.runner.disorder;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;

import javax.xml.bind.ValidationException;

import org.ggf.drmaa.DrmaaException;
import org.ggf.drmaa.JobInfo;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import compbio.data.sequence.Score;
import compbio.data.sequence.ScoreManager;
import compbio.data.sequence.ScoreManager.ScoreHolder;
import compbio.data.sequence.SequenceUtil;
import compbio.engine.AsyncExecutor;
import compbio.engine.Configurator;
import compbio.engine.SyncExecutor;
import compbio.engine.client.ConfExecutable;
import compbio.engine.client.ConfiguredExecutable;
import compbio.engine.client.Executable;
import compbio.engine.client.RunConfiguration;
import compbio.engine.cluster.drmaa.ClusterUtil;
import compbio.engine.cluster.drmaa.JobRunner;
import compbio.engine.cluster.drmaa.StatisticManager;
import compbio.engine.local.LocalRunner;
import compbio.metadata.AllTestSuit;
import compbio.metadata.JobExecutionException;
import compbio.metadata.JobStatus;
import compbio.metadata.JobSubmissionException;
import compbio.metadata.LimitsManager;
import compbio.metadata.PresetManager;
import compbio.metadata.ResultNotAvailableException;
import compbio.metadata.RunnerConfig;
import compbio.runner.msa.Mafft;
import compbio.util.SysPrefs;

public class IUPredTester {

	private IUPred iupred;

	@BeforeMethod(alwaysRun = true)
	void init() {
		iupred = new IUPred();
		iupred.setInput(AllTestSuit.test_input);
	}

	@Test(groups = {AllTestSuit.test_group_cluster,
			AllTestSuit.test_group_runner})
	public void testRunOnCluster() {
		assertFalse(SysPrefs.isWindows,
				"Cluster execution can only be in unix environment");
		try {
			ConfiguredExecutable<IUPred> confIUPred = Configurator
					.configureExecutable(iupred,
							Executable.ExecProvider.Cluster);
			JobRunner runner = JobRunner.getInstance(confIUPred);

			assertNotNull(runner, "Runner is NULL");
			runner.executeJob();
			// assertNotNull("JobId is null", jobId1);
			JobStatus status = runner.getJobStatus();
			assertTrue(status == JobStatus.PENDING
					|| status == JobStatus.RUNNING,
					"Status of the process is wrong!");
			JobInfo info = runner.getJobInfo();
			assertNotNull(info, "JobInfo is null");
			StatisticManager sm = new StatisticManager(info);
			assertNotNull(sm, "Statictic manager is null");
			try {

				String exits = sm.getExitStatus();
				assertNotNull("Exit status is null", exits);
				// cut 4 trailing zeros from the number
				int exitsInt = ClusterUtil.CLUSTER_STAT_IN_SEC.parse(exits)
						.intValue();
				assertEquals(0, exitsInt);
				System.out.println(sm.getAllStats());

			} catch (ParseException e) {
				e.printStackTrace();
				fail("Parse Exception: " + e.getMessage());
			}
			// assertFalse(runner.cleanup());
			assertTrue(sm.hasExited());
			assertFalse(sm.wasAborted());
			assertFalse(sm.hasDump());
			assertFalse(sm.hasSignaled());

		} catch (JobSubmissionException e) {
			e.printStackTrace();
			fail("DrmaaException caught:" + e.getMessage());
		} catch (JobExecutionException e) {
			e.printStackTrace();
			fail("DrmaaException caught:" + e.getMessage());
		} catch (DrmaaException e) {
			e.printStackTrace();
			fail("DrmaaException caught:" + e.getMessage());
		}
	}

	/**
	 * This tests fails from time to time depending on the cluster load or some
	 * other factors. Any client code has to adjust for this issue
	 */
	@Test(groups = {AllTestSuit.test_group_cluster,
			AllTestSuit.test_group_runner})
	public void testRunOnClusterAsync() {
		assertFalse(SysPrefs.isWindows,
				"Cluster execution can only be in unix environment");
		try {
			ConfiguredExecutable<IUPred> confIUPred = Configurator
					.configureExecutable(iupred,
							Executable.ExecProvider.Cluster);
			AsyncExecutor aengine = Configurator.getAsyncEngine(confIUPred);
			String jobId = aengine.submitJob(confIUPred);
			assertNotNull(jobId, "Runner is NULL");
			// let drmaa to start
			Thread.sleep(500);
			JobStatus status = aengine.getJobStatus(jobId);
			while (status != JobStatus.FINISHED) {
				System.out.println("Job Status: " + status);
				Thread.sleep(1000);
				status = aengine.getJobStatus(jobId);
				ConfiguredExecutable<IUPred> result = (ConfiguredExecutable<IUPred>) aengine
						.getResults(jobId);
				assertNotNull(result);
				System.out.println("RES:" + result);
				// Some times the job could be removed from the cluster
				// accounting
				// before it has been reported to finish. Make sure
				// to stop waiting in such case
				if (status == JobStatus.UNDEFINED) {
					break;
				}
			}
		} catch (JobSubmissionException e) {
			e.printStackTrace();
			fail("DrmaaException caught:" + e.getMessage());
		} catch (InterruptedException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (ResultNotAvailableException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	
	@Test(groups = {AllTestSuit.test_group_runner})
	public void testSetParameter() {
		assertEquals(iupred.getInput(), AllTestSuit.test_input);
		assertTrue(iupred.getParameters(null).getCommandString().trim().endsWith(AllTestSuit.test_input));
		iupred.setParameter("long");
		assertEquals(iupred.getInput(), AllTestSuit.test_input);
		String cmd = iupred.getParameters(null).getCommandString().trim();  
		assertTrue(cmd.endsWith("long"));
		assertTrue(cmd.contains(AllTestSuit.test_input)); 
	}
	
	
	@Test(groups = {AllTestSuit.test_group_runner})
	public void testReadResults() {
		ScoreManager scoreMan;
		try {
			scoreMan = iupred.getResults(AllTestSuit.TEST_DATA_PATH_ABSOLUTE);
			System.out.println(scoreMan.asMap());
		} catch (ResultNotAvailableException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	@Test(groups = {AllTestSuit.test_group_runner})
	public void testRunLocally() {
		try {
			ConfiguredExecutable<IUPred> confIUPred = Configurator
					.configureExecutable(iupred, Executable.ExecProvider.Local);

			// For local execution use relative
			LocalRunner lr = new LocalRunner(confIUPred);
			lr.executeJob();
			ConfiguredExecutable<?> al1 = lr.waitForResult();
			assertNotNull(al1.getResults());
			ScoreManager al2 = confIUPred.getResults();
			assertNotNull(al2);
			assertEquals(al2.asMap().size(), 3);
			assertEquals(al2.getNumberOfSeq(),3);
			
			ScoreHolder sch = al2.getAnnotationForSequence("Foobar");
			assertEquals(sch.getNumberOfScores(), 3);
			
			Score sc = sch.getScoreByMethod("Long");
			assertEquals(sc.getMethod(), "Long");
			assertTrue(sc.getRanges().isEmpty());
			assertNotNull(sc.getScores());
			assertEquals(sc.getScores().size(), 481);
			assertEquals(al1.getResults(), al2);

		
		} catch (JobSubmissionException e) {
			e.printStackTrace();
			fail(e.getLocalizedMessage());
		} catch (ResultNotAvailableException e) {
			e.printStackTrace();
			fail(e.getLocalizedMessage());
		} catch (JobExecutionException e) {
			e.printStackTrace();
			fail(e.getLocalizedMessage());
		}
	}

	@Test(groups = {AllTestSuit.test_group_runner})
	public void testRunLocallyWithParams() {
		try {
			iupred.setParameter("short");
			ConfiguredExecutable<IUPred> confIUPred = Configurator
					.configureExecutable(iupred, Executable.ExecProvider.Local);

			// For local execution use relative
			LocalRunner lr = new LocalRunner(confIUPred);
			lr.executeJob();
			ConfiguredExecutable<?> al1 = lr.waitForResult();
			assertNotNull(al1.getResults());
			ScoreManager al2 = confIUPred.getResults();
	
			assertNotNull(al2.asMap());
			assertEquals(al2.asMap().size(), 3);
			ScoreHolder sch  = al2.getAnnotationForSequence("Foobar");
			assertNotNull(sch);
			assertEquals(sch.getNumberOfScores(), 1); 
			
			/*
			assertNotNull(al2);
			assertEquals(al2.asSet().size(), 1);
			assertEquals(al2.getNumberOfSeq(),1);
			Score sc = al2.asSet().iterator().next(); 
			assertEquals(sc.getMethod(), "Long");
			assertTrue(sc.getRanges().isEmpty());
			assertNotNull(sc.getScores());
			assertEquals(sc.getScores().size(), 568);
			assertEquals(al1.getResults(), al2);
*/
			} catch (JobSubmissionException e) {
			e.printStackTrace();
			fail(e.getLocalizedMessage());
		} catch (ResultNotAvailableException e) {
			e.printStackTrace();
			fail(e.getLocalizedMessage());
		} catch (JobExecutionException e) {
			e.printStackTrace();
			fail(e.getLocalizedMessage());
		}
	}

	@Test(groups = {AllTestSuit.test_group_runner})
	public void testPersistance() {
		try {
			IUPred iupred = new IUPred();
			iupred.setError("errrr.txt").setInput(AllTestSuit.test_input);
			assertEquals(iupred.getInput(), AllTestSuit.test_input);
			assertEquals(iupred.getError(), "errrr.txt");

			ConfiguredExecutable<IUPred> cIUPred = Configurator
					.configureExecutable(iupred, Executable.ExecProvider.Local);

			SyncExecutor sexec = Configurator.getSyncEngine(cIUPred);
			sexec.executeJob();
			ConfiguredExecutable<?> al = sexec.waitForResult();
			assertNotNull(al.getResults());
			// Save run configuration
			assertTrue(cIUPred.saveRunConfiguration());

			// See if loaded configuration is the same as saved
			RunConfiguration loadedRun = RunConfiguration
					.load(new FileInputStream(new File(cIUPred
							.getWorkDirectory(), RunConfiguration.rconfigFile)));
			assertEquals(
					((ConfExecutable<IUPred>) cIUPred).getRunConfiguration(),
					loadedRun);
			// Load run configuration as ConfExecutable
			ConfiguredExecutable<IUPred> resurrectedCIUPred = (ConfiguredExecutable<IUPred>) cIUPred
					.loadRunConfiguration(new FileInputStream(new File(cIUPred
							.getWorkDirectory(), RunConfiguration.rconfigFile)));
			assertNotNull(resurrectedCIUPred);
			assertEquals(resurrectedCIUPred.getExecutable().getInput(),
					AllTestSuit.test_input);
			assertEquals(resurrectedCIUPred.getExecutable().getError(),
					"errrr.txt");

			// See in details whether executables are the same
			assertEquals(resurrectedCIUPred.getExecutable(), iupred);

			ConfiguredExecutable<IUPred> resIUPred = Configurator
					.configureExecutable(resurrectedCIUPred.getExecutable(),
							Executable.ExecProvider.Local);

			sexec = Configurator.getSyncEngine(resIUPred,
					Executable.ExecProvider.Local);
			sexec.executeJob();
			al = sexec.waitForResult();
			assertNotNull(al);

		} catch (JobSubmissionException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (JobExecutionException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (ResultNotAvailableException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	@Test(groups = {AllTestSuit.test_group_runner})
	public void testConfigurationLoading() {
		try {
			RunnerConfig<IUPred> iupredConfig = ConfExecutable
					.getRunnerOptions(IUPred.class);

			assertNotNull(iupredConfig);

			assertTrue(iupredConfig.getArguments().size() != 0);

			PresetManager<IUPred> iupredPresets = ConfExecutable
					.getRunnerPresets(IUPred.class);
			assertNull(iupredPresets); // there is no presets

			LimitsManager<IUPred> iupredLimits = ConfExecutable
					.getRunnerLimits(IUPred.class);
			assertNotNull(iupredLimits);
			assertTrue(iupredLimits.getLimits().size() > 0);
			iupredLimits.validate(iupredPresets);

		} catch (FileNotFoundException e) {
			e.printStackTrace();
			fail(e.getLocalizedMessage());
		} catch (IOException e) {
			e.printStackTrace();
			fail(e.getLocalizedMessage());
		} catch (ValidationException e) {
			e.printStackTrace();
			fail(e.getLocalizedMessage());
		}
	}

}
