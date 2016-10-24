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
import java.util.Set;

import javax.xml.bind.ValidationException;

import org.ggf.drmaa.DrmaaException;
import org.ggf.drmaa.JobInfo;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import compbio.data.sequence.Score;
import compbio.data.sequence.ScoreManager;
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
import compbio.util.SysPrefs;

public class GlobPlotTester {

	public static String test_outfile = "TO1381.globprot.out";

	private GlobPlot globprot;

	@BeforeMethod(alwaysRun = true)
	void init() {
		globprot = new GlobPlot();
		globprot.setInput(AllTestSuit.test_input).setOutput(test_outfile);
	}

	@Test(groups = {AllTestSuit.test_group_cluster,
			AllTestSuit.test_group_runner})
	public void testRunOnCluster() {
		assertFalse(SysPrefs.isWindows,
				"Cluster execution can only be in unix environment");
		try {
			ConfiguredExecutable<GlobPlot> confGlobPlot = Configurator
					.configureExecutable(globprot,
							Executable.ExecProvider.Cluster);
			JobRunner runner = JobRunner.getInstance(confGlobPlot);

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
			ConfiguredExecutable<GlobPlot> confGlobPlot = Configurator
					.configureExecutable(globprot,
							Executable.ExecProvider.Cluster);
			AsyncExecutor aengine = Configurator.getAsyncEngine(confGlobPlot);
			String jobId = aengine.submitJob(confGlobPlot);
			assertNotNull(jobId, "Runner is NULL");
			// let drmaa to start
			Thread.sleep(500);
			JobStatus status = aengine.getJobStatus(jobId);
			while (status != JobStatus.FINISHED) {
				System.out.println("Job Status: " + status);
				Thread.sleep(1000);
				status = aengine.getJobStatus(jobId);
				ConfiguredExecutable<GlobPlot> result = (ConfiguredExecutable<GlobPlot>) aengine
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
			ConfiguredExecutable<GlobPlot> confGlobPlot = Configurator
					.configureExecutable(globprot,
							Executable.ExecProvider.Local);

			// For local execution use relative
			LocalRunner lr = new LocalRunner(confGlobPlot);
			lr.executeJob();
			ConfiguredExecutable<?> al1 = lr.waitForResult();
			assertNotNull(al1.getResults());
			ScoreManager al2 = confGlobPlot.getResults();
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
		// No execution statistics is available!
	}

	@Test(groups = {AllTestSuit.test_group_runner})
	public void testPersistance() {
		try {
			GlobPlot disembl = new GlobPlot();
			disembl.setError("errrr.txt").setInput(AllTestSuit.test_input)
					.setOutput("outtt.txt");
			assertEquals(disembl.getInput(), AllTestSuit.test_input);
			assertEquals(disembl.getError(), "errrr.txt");
			assertEquals(disembl.getOutput(), "outtt.txt");
			ConfiguredExecutable<GlobPlot> cGlobPlot = Configurator
					.configureExecutable(disembl, Executable.ExecProvider.Local);

			SyncExecutor sexec = Configurator.getSyncEngine(cGlobPlot);
			sexec.executeJob();
			ConfiguredExecutable<?> al = sexec.waitForResult();
			assertNotNull(al.getResults());
			// Save run configuration
			assertTrue(cGlobPlot.saveRunConfiguration());

			// See if loaded configuration is the same as saved
			RunConfiguration loadedRun = RunConfiguration
					.load(new FileInputStream(new File(cGlobPlot
							.getWorkDirectory(), RunConfiguration.rconfigFile)));
			assertEquals(
					((ConfExecutable<GlobPlot>) cGlobPlot)
							.getRunConfiguration(),
					loadedRun);
			// Load run configuration as ConfExecutable
			ConfiguredExecutable<GlobPlot> resurrectedCGlobPlot = (ConfiguredExecutable<GlobPlot>) cGlobPlot
					.loadRunConfiguration(new FileInputStream(new File(
							cGlobPlot.getWorkDirectory(),
							RunConfiguration.rconfigFile)));
			assertNotNull(resurrectedCGlobPlot);
			assertEquals(resurrectedCGlobPlot.getExecutable().getInput(),
					AllTestSuit.test_input);
			assertEquals(resurrectedCGlobPlot.getExecutable().getError(),
					"errrr.txt");
			assertEquals(resurrectedCGlobPlot.getExecutable().getOutput(),
					"outtt.txt");
			// See in details whether executables are the same
			assertEquals(resurrectedCGlobPlot.getExecutable(), disembl);

			ConfiguredExecutable<GlobPlot> resGlobPlot = Configurator
					.configureExecutable(resurrectedCGlobPlot.getExecutable(),
							Executable.ExecProvider.Local);

			sexec = Configurator.getSyncEngine(resGlobPlot,
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
			RunnerConfig<GlobPlot> globplotConfig = ConfExecutable
					.getRunnerOptions(GlobPlot.class);
			// There is no GlobPlot parameters 
			assertNull(globplotConfig);
			
			PresetManager<GlobPlot> disemblPresets = ConfExecutable
					.getRunnerPresets(GlobPlot.class);
			assertNull(disemblPresets); // there is no presets

			LimitsManager<GlobPlot> disemblLimits = ConfExecutable
					.getRunnerLimits(GlobPlot.class);
			assertNotNull(disemblLimits);
			assertTrue(disemblLimits.getLimits().size() > 0);
			disemblLimits.validate(disemblPresets);

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
