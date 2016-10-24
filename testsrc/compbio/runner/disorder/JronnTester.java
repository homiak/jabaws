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
import java.util.Map;

import javax.xml.bind.ValidationException;

import org.ggf.drmaa.DrmaaException;
import org.ggf.drmaa.JobInfo;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import compbio.data.sequence.Score;
import compbio.data.sequence.ScoreManager;
import compbio.engine.AsyncExecutor;
import compbio.engine.Configurator;
import compbio.engine.FilePuller;
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
import compbio.metadata.ChunkHolder;
import compbio.metadata.JobExecutionException;
import compbio.metadata.JobStatus;
import compbio.metadata.JobSubmissionException;
import compbio.metadata.LimitsManager;
import compbio.metadata.PresetManager;
import compbio.metadata.ResultNotAvailableException;
import compbio.metadata.RunnerConfig;
import compbio.util.FileWatcher;
import compbio.util.SysPrefs;

public class JronnTester {

	public static String test_outfile = "TO1381.jronn.out"; // "/homes/pvtroshin/TO1381.clustal.cluster.out

	private Jronn jronn;

	@BeforeMethod(alwaysRun = true)
	void init() {
		jronn = new Jronn();
		jronn.setInput(AllTestSuit.test_input).setOutput(test_outfile);
	}

	@Test(groups = {AllTestSuit.test_group_cluster,
			AllTestSuit.test_group_runner})
	public void testRunOnCluster() {
		assertFalse(SysPrefs.isWindows,
				"Cluster execution can only be in unix environment");
		try {
			ConfiguredExecutable<Jronn> confJronn = Configurator
					.configureExecutable(jronn, Executable.ExecProvider.Cluster);
			JobRunner runner = JobRunner.getInstance(confJronn);

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
			ConfiguredExecutable<Jronn> confJronn = Configurator
					.configureExecutable(jronn, Executable.ExecProvider.Cluster);
			AsyncExecutor aengine = Configurator.getAsyncEngine(confJronn);
			String jobId = aengine.submitJob(confJronn);
			assertNotNull(jobId, "Runner is NULL");
			// let drmaa to start
			Thread.sleep(500);
			JobStatus status = aengine.getJobStatus(jobId);
			while (status != JobStatus.FINISHED) {
				System.out.println("Job Status: " + status);
				Thread.sleep(1000);
				status = aengine.getJobStatus(jobId);
				ConfiguredExecutable<Jronn> result = (ConfiguredExecutable<Jronn>) aengine
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
	public void testRunLocally() {
		try {
			ConfiguredExecutable<Jronn> confJronn = Configurator
					.configureExecutable(jronn, Executable.ExecProvider.Local);

			// For local execution use relative
			LocalRunner lr = new LocalRunner(confJronn);
			lr.executeJob();
			ConfiguredExecutable<?> al1 = lr.waitForResult();
			assertNotNull(al1.getResults());
			ScoreManager al2 = confJronn.getResults();
			assertNotNull(al2);
			assertEquals(al2.asMap().size(), 3);
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
	public void testRunLocallyOnTwoCpu() {
		try {
			jronn.setNCore(2);
			ConfiguredExecutable<Jronn> confJronn = Configurator
					.configureExecutable(jronn, Executable.ExecProvider.Local);

			// For local execution use relative
			LocalRunner lr = new LocalRunner(confJronn);
			lr.executeJob();
			ConfiguredExecutable<?> al1 = lr.waitForResult();
			assertNotNull(al1.getResults());
			ScoreManager al2 = confJronn.getResults();
			assertNotNull(al2);
			assertEquals(al2.asMap().size(), 3);
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
	public void readStatistics() {
		try {
			ConfiguredExecutable<Jronn> confJronn = Configurator
					.configureExecutable(jronn, Executable.ExecProvider.Local);
			// For local execution use relavive

			AsyncExecutor sexec = Configurator.getAsyncEngine(confJronn);
			String jobId = sexec.submitJob(confJronn);
			FilePuller fw = FilePuller.newFilePuller(
					confJronn.getWorkDirectory() + File.separator
							+ Jronn.getStatFile(),
					FileWatcher.MIN_CHUNK_SIZE_BYTES);
			int count = 0;
			long position = 0;
			fw.waitForFile(4);
			JobStatus status = sexec.getJobStatus(jobId);
			while (status != JobStatus.FINISHED) {
				if (fw.hasMoreData()) {
					ChunkHolder ch = fw.pull(position);
					String chunk = ch.getChunk();
					position = ch.getNextPosition();
				}
				count++;
				// Make sure the loop is terminated if the job fails
				if ((status == JobStatus.UNDEFINED || status == JobStatus.FAILED)) {
					break;
				}
				Thread.sleep(300);
				status = sexec.getJobStatus(jobId);
			}
			assertTrue(count > 1);
			ConfiguredExecutable<?> al = sexec.getResults(jobId);
			assertNotNull(al.getResults());
		} catch (JobSubmissionException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (ResultNotAvailableException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (InterruptedException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	@Test(groups = {AllTestSuit.test_group_runner})
	public void testPersistance() {
		try {
			Jronn jronn = new Jronn();
			jronn.setError("errrr.txt").setInput(AllTestSuit.test_input)
					.setOutput("outtt.txt");
			assertEquals(jronn.getInput(), AllTestSuit.test_input);
			assertEquals(jronn.getError(), "errrr.txt");
			assertEquals(jronn.getOutput(), "outtt.txt");
			ConfiguredExecutable<Jronn> cJronn = Configurator
					.configureExecutable(jronn, Executable.ExecProvider.Local);

			SyncExecutor sexec = Configurator.getSyncEngine(cJronn);
			sexec.executeJob();
			ConfiguredExecutable<?> al = sexec.waitForResult();
			assertNotNull(al.getResults());
			// Save run configuration
			assertTrue(cJronn.saveRunConfiguration());

			// See if loaded configuration is the same as saved
			RunConfiguration loadedRun = RunConfiguration
					.load(new FileInputStream(new File(cJronn
							.getWorkDirectory(), RunConfiguration.rconfigFile)));
			assertEquals(
					((ConfExecutable<Jronn>) cJronn).getRunConfiguration(),
					loadedRun);
			// Load run configuration as ConfExecutable
			ConfiguredExecutable<Jronn> resurrectedCMuscle = (ConfiguredExecutable<Jronn>) cJronn
					.loadRunConfiguration(new FileInputStream(new File(cJronn
							.getWorkDirectory(), RunConfiguration.rconfigFile)));
			assertNotNull(resurrectedCMuscle);
			assertEquals(resurrectedCMuscle.getExecutable().getInput(),
					AllTestSuit.test_input);
			assertEquals(resurrectedCMuscle.getExecutable().getError(),
					"errrr.txt");
			assertEquals(resurrectedCMuscle.getExecutable().getOutput(),
					"outtt.txt");
			// See in details whether executables are the same
			assertEquals(resurrectedCMuscle.getExecutable(), jronn);

			ConfiguredExecutable<Jronn> resJronn = Configurator
					.configureExecutable(resurrectedCMuscle.getExecutable(),
							Executable.ExecProvider.Local);

			sexec = Configurator.getSyncEngine(resJronn,
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
			RunnerConfig<Jronn> jronnConfig = ConfExecutable
					.getRunnerOptions(Jronn.class);
			// There is not supported parameters for Jronn
			assertNull(jronnConfig);

			PresetManager<Jronn> jronnPresets = ConfExecutable
					.getRunnerPresets(Jronn.class);
			assertNull(jronnPresets); // there is no presets

			LimitsManager<Jronn> jronnLimits = ConfExecutable
					.getRunnerLimits(Jronn.class);
			assertNotNull(jronnLimits);
			assertTrue(jronnLimits.getLimits().size() > 0);
			jronnLimits.validate(jronnPresets);

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
