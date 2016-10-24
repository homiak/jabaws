/*
 * Copyright (c) 2010 Peter Troshin JAva Bioinformatics Analysis Web Services
 * (JABAWS) @version: 2.0 
 * 
 * This library is free software; you can redistribute it and/or modify it under 
 * the terms of the Apache License version 2 as published
 * by the Apache Software Foundation This library is distributed in the hope
 * that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * Apache License for more details. A copy of the license is in
 * apache_license.txt. It is also available here:
 * 
 * @see: http://www.apache.org/licenses/LICENSE-2.0.txt 
 * 
 * Any republication or derived work distributed in source code form must include 
 * this copyright and license notice.
 */
package compbio.runner.conservation;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.HashSet;

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
import compbio.metadata.Preset;
import compbio.metadata.PresetManager;
import compbio.metadata.ResultNotAvailableException;
import compbio.metadata.RunnerConfig;
import compbio.runner.Util;
import compbio.util.FileWatcher;
import compbio.util.SysPrefs;

public class AAConTester {

	public static final String CURRENT_DIRECTORY = SysPrefs
			.getCurrentDirectory() + File.separator;

	public static String test_outfile = "TO1381.aacon.out"; // "/homes/pvtroshin/TO1381.clustal.cluster.out
	public static String test_alignment_input = CURRENT_DIRECTORY + "testsrc"
			+ File.separator + "testdata" + File.separator + "TO1381.fasta.aln";
	private AACon aacon;

	@BeforeMethod(alwaysRun = true)
	void init() {
		aacon = new AACon();
		aacon.setInput(test_alignment_input).setOutput(test_outfile);
	}

	@Test(groups = {AllTestSuit.test_group_runner})
	public void testRunOnCluster() {
		assertFalse(SysPrefs.isWindows,
				"Cluster execution can only be in unix environment");
		try {
			ConfiguredExecutable<AACon> confAAcon = Configurator
					.configureExecutable(aacon, Executable.ExecProvider.Cluster);
			JobRunner runner = JobRunner.getInstance(confAAcon);

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
	@Test(groups = {AllTestSuit.test_group_runner, AllTestSuit.test_group_cluster})
	public void testRunOnClusterAsync() {
		assertFalse(SysPrefs.isWindows,
				"Cluster execution can only be in unix environment");
		try {
			ConfiguredExecutable<AACon> confAAcon = Configurator
					.configureExecutable(aacon, Executable.ExecProvider.Cluster);
			AsyncExecutor aengine = Configurator.getAsyncEngine(confAAcon);
			String jobId = aengine.submitJob(confAAcon);
			assertNotNull(jobId, "Runner is NULL");
			// let drmaa to start
			Thread.sleep(500);
			JobStatus status = aengine.getJobStatus(jobId);
			while (status != JobStatus.FINISHED) {
				System.out.println("Job Status: " + status);
				Thread.sleep(1000);
				status = aengine.getJobStatus(jobId);
				ConfiguredExecutable<AACon> result = (ConfiguredExecutable<AACon>) aengine
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
			ConfiguredExecutable<AACon> confAAcon = Configurator
					.configureExecutable(aacon, Executable.ExecProvider.Local);

			// For local execution use relative
			LocalRunner lr = new LocalRunner(confAAcon);
			lr.executeJob();
			ConfiguredExecutable<?> al1 = lr.waitForResult();
			assertNotNull(al1.getResults());
			ScoreManager annotations = confAAcon.getResults();
			assertNotNull(annotations);
			assertEquals(annotations.asSet().size(), 18);
			assertEquals(al1.getResults(), annotations);
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
	public void testRunLocallyWithPreset() {
		try {
			PresetManager<AACon> aaconPresets = Util.getPresets(AACon.class);
			assert aaconPresets != null;
			ConfiguredExecutable<AACon> confAAcon = Configurator
					.configureExecutable(aacon, Executable.ExecProvider.Local);
			Preset<AACon> quick = aaconPresets
					.getPresetByName("Quick conservation");
			confAAcon.addParameters(quick.getOptions());
			// For local execution use relative
			LocalRunner lr = new LocalRunner(confAAcon);
			lr.executeJob();
			ConfiguredExecutable<?> al1 = lr.waitForResult();
			assertNotNull(al1.getResults());
			ScoreManager annotations = confAAcon.getResults();
			assertNotNull(annotations);
			assertEquals(annotations.asSet().size(), 13);
			assertEquals(al1.getResults(), annotations);
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
			aacon = new AACon();
			aacon.setInput(test_alignment_input).setOutput(test_outfile);

			aacon.setNCore(2);
			ConfiguredExecutable<AACon> confAAcon = Configurator
					.configureExecutable(aacon, Executable.ExecProvider.Local);
			confAAcon.addParameters(Arrays.asList("-m=KABAT,JORES"));
			confAAcon.addParameters(Arrays.asList("-m=TAYLOR_GAPS"));
			// For local execution use relative
			LocalRunner lr = new LocalRunner(confAAcon);

			lr.executeJob();
			ConfiguredExecutable<?> al1 = lr.waitForResult();
			assertNotNull(al1.getResults());
			ScoreManager annotations = confAAcon.getResults();
			assertNotNull(annotations);
			assertEquals(annotations.asSet().size(), 3);
			assertEquals(al1.getResults(), annotations);

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
			ConfiguredExecutable<AACon> confAAcon = Configurator
					.configureExecutable(aacon, Executable.ExecProvider.Local);
			// For local execution use relative

			AsyncExecutor sexec = Configurator.getAsyncEngine(confAAcon);
			String jobId = sexec.submitJob(confAAcon);
			FilePuller fw = FilePuller.newFilePuller(
					confAAcon.getWorkDirectory() + File.separator
							+ AACon.getStatFile(),
					FileWatcher.MIN_CHUNK_SIZE_BYTES);
			int count = 0;
			long position = 0;
			fw.waitForFile(2);
			JobStatus status = sexec.getJobStatus(jobId);
			do {
				ChunkHolder ch = fw.pull(position);
				String chunk = ch.getChunk();
				position = ch.getNextPosition();
				// System.out.println(chunk);
				count++;
				// Make sure the loop is terminated if the job fails
				if ((status == JobStatus.UNDEFINED || status == JobStatus.FAILED)) {
					fail("job failed!");
					break;
				}
				Thread.sleep(300);
				status = sexec.getJobStatus(jobId);
			} while (status != JobStatus.FINISHED || fw.hasMoreData());

			assertTrue(count >= 1);
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
			AACon aacon = new AACon();
			aacon.setError("errrr.txt").setInput(test_alignment_input)
					.setOutput("outtt.txt");
			assertEquals(aacon.getInput(), test_alignment_input);
			assertEquals(aacon.getError(), "errrr.txt");
			assertEquals(aacon.getOutput(), "outtt.txt");
			ConfiguredExecutable<AACon> cAAcon = Configurator
					.configureExecutable(aacon, Executable.ExecProvider.Local);

			SyncExecutor sexec = Configurator.getSyncEngine(cAAcon);
			sexec.executeJob();
			ConfiguredExecutable<?> al = sexec.waitForResult();
			assertNotNull(al.getResults());
			// Save run configuration
			assertTrue(cAAcon.saveRunConfiguration());

			// See if loaded configuration is the same as saved
			RunConfiguration loadedRun = RunConfiguration
					.load(new FileInputStream(new File(cAAcon
							.getWorkDirectory(), RunConfiguration.rconfigFile)));
			assertEquals(
					((ConfExecutable<AACon>) cAAcon).getRunConfiguration(),
					loadedRun);
			// Load run configuration as ConfExecutable
			ConfiguredExecutable<AACon> resurrectedCAAcon = (ConfiguredExecutable<AACon>) cAAcon
					.loadRunConfiguration(new FileInputStream(new File(cAAcon
							.getWorkDirectory(), RunConfiguration.rconfigFile)));
			assertNotNull(resurrectedCAAcon);
			assertEquals(resurrectedCAAcon.getExecutable().getInput(),
					test_alignment_input);
			assertEquals(resurrectedCAAcon.getExecutable().getError(),
					"errrr.txt");
			assertEquals(resurrectedCAAcon.getExecutable().getOutput(),
					"outtt.txt");
			// See in details whether executables are the same
			assertEquals(resurrectedCAAcon.getExecutable(), aacon);

			ConfiguredExecutable<AACon> resAAcon = Configurator
					.configureExecutable(resurrectedCAAcon.getExecutable(),
							Executable.ExecProvider.Local);

			sexec = Configurator.getSyncEngine(resAAcon,
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
			RunnerConfig<AACon> aaconConfig = ConfExecutable
					.getRunnerOptions(AACon.class);
			assertNotNull(aaconConfig);
			assertTrue(aaconConfig.getArguments().size() > 0);

			PresetManager<AACon> aaconPresets = ConfExecutable
					.getRunnerPresets(AACon.class);
			assertNotNull(aaconPresets);

			LimitsManager<AACon> jronnLimits = ConfExecutable
					.getRunnerLimits(AACon.class);
			assertNotNull(jronnLimits);
			assertTrue(jronnLimits.getLimits().size() > 0);
			jronnLimits.validate(aaconPresets);

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
