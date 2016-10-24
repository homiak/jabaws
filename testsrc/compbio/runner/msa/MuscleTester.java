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

package compbio.runner.msa;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNotSame;
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

import compbio.data.sequence.Alignment;
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
import compbio.runner.msa.Muscle;
import compbio.util.FileWatcher;
import compbio.util.SysPrefs;

public class MuscleTester {

	public static String test_outfile = "TO1381.muscle.out"; // homes/pvtroshin/TO1381.clustal.out
	public static String cluster_test_outfile = "TO1381.muscle.cluster.out"; // "/homes/pvtroshin/TO1381.clustal.cluster.out

	private Muscle muscle;

	@BeforeMethod(alwaysRun = true)
	void init() {
		muscle = new Muscle();
		muscle.setInput(AllTestSuit.test_input).setOutput(cluster_test_outfile);
	}

	@Test(groups = { AllTestSuit.test_group_cluster,
			AllTestSuit.test_group_runner })
	public void testRunOnCluster() {
		assertFalse(SysPrefs.isWindows,
				"Cluster execution can only be in unix environment");
		try {
			ConfiguredExecutable<Muscle> confMuscle = Configurator
					.configureExecutable(muscle,
							Executable.ExecProvider.Cluster);
			JobRunner runner = JobRunner.getInstance(confMuscle);

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
			assertFalse(runner.cleanup());
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
	@Test(enabled = false, groups = { AllTestSuit.test_group_cluster,
			AllTestSuit.test_group_runner })
	public void testRunOnClusterAsync() {
		assertFalse(SysPrefs.isWindows,
				"Cluster execution can only be in unix environment");
		try {
			ConfiguredExecutable<Muscle> confMuscle = Configurator
					.configureExecutable(muscle,
							Executable.ExecProvider.Cluster);
			AsyncExecutor aengine = Configurator.getAsyncEngine(confMuscle);
			String jobId = aengine.submitJob(confMuscle);
			assertNotNull(jobId, "Runner is NULL");
			// let drmaa to start
			Thread.sleep(500);
			JobStatus status = aengine.getJobStatus(jobId);
			while (status != JobStatus.FINISHED || status !=JobStatus.UNDEFINED) {
				System.out.println("Job Status: " + status);
				Thread.sleep(1000);
				status = aengine.getJobStatus(jobId);
			}
		} catch (JobSubmissionException e) {
			e.printStackTrace();
			fail("DrmaaException caught:" + e.getMessage());
		} catch (InterruptedException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	@Test(groups = { AllTestSuit.test_group_runner })
	public void testRunLocally() {
		try {
			ConfiguredExecutable<Muscle> confMuscle = Configurator
					.configureExecutable(muscle, Executable.ExecProvider.Local);
			confMuscle.getParameters().setParam("-matrix", "BLOSUM62");

			// For local execution use relative
			LocalRunner lr = new LocalRunner(confMuscle);
			lr.executeJob();
			ConfiguredExecutable<?> al1 = lr.waitForResult();
			assertNotNull(al1.getResults());
			Alignment al2 = confMuscle.getResults();
			assertNotNull(al2);
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

	@Test(groups = { AllTestSuit.test_group_runner })
	public void readStatistics() {
		try {
			ConfiguredExecutable<Muscle> confMuscle = Configurator
					.configureExecutable(muscle, Executable.ExecProvider.Local);
			// For local execution use relavive

			AsyncExecutor sexec = Configurator.getAsyncEngine(confMuscle);
			String jobId = sexec.submitJob(confMuscle);
			FilePuller fw = FilePuller.newFilePuller(confMuscle
					.getWorkDirectory()
					+ File.separator + Muscle.getStatFile(),
					FileWatcher.MIN_CHUNK_SIZE_BYTES);
			int count = 0;
			long position = 0;
			fw.waitForFile(4);
			while (!(sexec.getJobStatus(jobId) == JobStatus.FINISHED || sexec
					.getJobStatus(jobId) == JobStatus.FAILED || sexec
					.getJobStatus(jobId) == JobStatus.UNDEFINED)
					|| fw.hasMoreData()) {
				ChunkHolder ch = fw.pull(position);
				String chunk = ch.getChunk();
				position = ch.getNextPosition();
				System.out.print(chunk);
				count++;
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
		}
	}

	@Test(groups = { AllTestSuit.test_group_runner })
	public void testPersistance() {
		try {
			Muscle muscle = new Muscle();
			muscle.setError("errrr.txt").setInput(AllTestSuit.test_input)
					.setOutput("outtt.txt");
			assertEquals(muscle.getInput(), AllTestSuit.test_input);
			assertEquals(muscle.getError(), "errrr.txt");
			assertEquals(muscle.getOutput(), "outtt.txt");
			ConfiguredExecutable<Muscle> cmuscle = Configurator
					.configureExecutable(muscle, Executable.ExecProvider.Local);

			SyncExecutor sexec = Configurator.getSyncEngine(cmuscle);
			sexec.executeJob();
			ConfiguredExecutable<?> al = sexec.waitForResult();
			assertNotNull(al.getResults());
			// Save run configuration
			assertTrue(cmuscle.saveRunConfiguration());

			// See if loaded configuration is the same as saved
			RunConfiguration loadedRun = RunConfiguration
					.load(new FileInputStream(new File(cmuscle
							.getWorkDirectory(), RunConfiguration.rconfigFile)));
			assertEquals(((ConfExecutable<Muscle>) cmuscle)
					.getRunConfiguration(), loadedRun);
			// Load run configuration as ConfExecutable
			ConfiguredExecutable<Muscle> resurrectedCMuscle = (ConfiguredExecutable<Muscle>) cmuscle
					.loadRunConfiguration(new FileInputStream(new File(cmuscle
							.getWorkDirectory(), RunConfiguration.rconfigFile)));
			assertNotNull(resurrectedCMuscle);
			assertEquals(resurrectedCMuscle.getExecutable().getInput(),
					AllTestSuit.test_input);
			assertEquals(resurrectedCMuscle.getExecutable().getError(),
					"errrr.txt");
			assertEquals(resurrectedCMuscle.getExecutable().getOutput(),
					"outtt.txt");
			// See in details whether executables are the same
			assertEquals(resurrectedCMuscle.getExecutable(), muscle);

			ConfiguredExecutable<Muscle> resmuscle = Configurator
					.configureExecutable(resurrectedCMuscle.getExecutable(),
							Executable.ExecProvider.Local);

			sexec = Configurator.getSyncEngine(resmuscle,
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

	@Test(groups = { AllTestSuit.test_group_runner })
	public void testConfigurationLoading() {
		try {
			RunnerConfig<Muscle> muscleConfig = ConfExecutable
					.getRunnerOptions(Muscle.class);
			assertNotNull(muscleConfig);
			assertTrue(muscleConfig.getArguments().size() > 0);

			PresetManager<Muscle> musclePresets = ConfExecutable
					.getRunnerPresets(Muscle.class);
			assertNotNull(musclePresets);
			assertTrue(musclePresets.getPresets().size() > 0);
			musclePresets.validate(muscleConfig);

			LimitsManager<Muscle> muscleLimits = ConfExecutable
					.getRunnerLimits(Muscle.class);
			assertNotNull(muscleLimits);
			assertTrue(muscleLimits.getLimits().size() > 0);
			muscleLimits.validate(musclePresets);

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
