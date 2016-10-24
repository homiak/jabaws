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
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.bind.ValidationException;

import org.ggf.drmaa.DrmaaException;
import org.ggf.drmaa.JobInfo;
import org.testng.annotations.Test;

import compbio.data.sequence.FastaSequence;
import compbio.engine.AsyncExecutor;
import compbio.engine.Configurator;
import compbio.engine.FilePuller;
import compbio.engine.SyncExecutor;
import compbio.engine.client.ConfExecutable;
import compbio.engine.client.ConfiguredExecutable;
import compbio.engine.client.Executable;
import compbio.engine.client.Executable.ExecProvider;
import compbio.engine.client.RunConfiguration;
import compbio.engine.cluster.drmaa.ClusterUtil;
import compbio.engine.cluster.drmaa.JobRunner;
import compbio.engine.cluster.drmaa.StatisticManager;
import compbio.engine.conf.RunnerConfigMarshaller;
import compbio.engine.local.AsyncLocalRunner;
import compbio.engine.local.LocalExecutorService;
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
import compbio.runner.OptionCombinator;
import compbio.runner.Util;
import compbio.util.FileWatcher;
import compbio.util.SysPrefs;

public class ClustalOTester {

	static final String clustalConfigFile = AllTestSuit.TEST_DATA_PATH
			+ "ClustaloParameters.xml";
	public static String test_outfile = "TO1381.clustalo.out";
	public static String cluster_test_outfile = "TO1381.clustalo.cluster.out";

	@Test(groups = {AllTestSuit.test_group_cluster,
			AllTestSuit.test_group_runner})
	public void testRunOnCluster() {
		ClustalO clustal = new ClustalO();
		assertFalse(SysPrefs.isWindows,
				"Cluster execution can only be in unix environment");
		clustal.setInput(AllTestSuit.test_input)
				.setOutput(cluster_test_outfile);

		try {

			ConfiguredExecutable<ClustalO> confClustal = Configurator
					.configureExecutable(clustal);
			JobRunner runner = JobRunner.getInstance(confClustal);
			// ClusterSession csession = JobRunner.getSession();
			assertNotNull(runner);
			runner.executeJob();
			// assertNotNull("JobId is null", jobId1);
			JobStatus status = runner.getJobStatus();
			assertTrue(status == JobStatus.PENDING
					|| status == JobStatus.RUNNING);
			JobInfo info = runner.getJobInfo();
			assertNotNull(info);
			StatisticManager sm = new StatisticManager(info);
			assertNotNull(sm);
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
			// At present the task directory could not be completely removed
			// @see JobRunner.cleanup()
			assertFalse(runner.cleanup(),
					"Could not remove some files whilst cleaning up ");
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

	@Test(groups = {AllTestSuit.test_group_runner})
	public void testRunLocally() {
		ClustalO clustal = new ClustalO();
		clustal.setInput(AllTestSuit.test_input).setOutput(test_outfile);
		try {

			// For local execution use relavive
			ConfiguredExecutable<ClustalO> confClustal = Configurator
					.configureExecutable(clustal, Executable.ExecProvider.Local);
			LocalRunner lr = new LocalRunner(confClustal);
			lr.executeJob();
			confClustal = (ConfiguredExecutable<ClustalO>) lr.waitForResult();
			assertNotNull(confClustal.getResults());
		} catch (JobSubmissionException e) {
			e.printStackTrace();
			fail(e.getLocalizedMessage());
		} catch (JobExecutionException e) {
			e.printStackTrace();
			fail(e.getLocalizedMessage());
		} catch (ResultNotAvailableException e) {
			e.printStackTrace();
			fail(e.getLocalizedMessage());
		}
	}

	@Test(groups = {AllTestSuit.test_group_runner})
	public void testConfigurationLoading() {
		try {
			RunnerConfig<ClustalO> clustalConfig = ConfExecutable
					.getRunnerOptions(ClustalO.class);
			assertNotNull(clustalConfig);
			assertTrue(clustalConfig.getArguments().size() > 0);

			/* Unsupported
			PresetManager<ClustalO> clustalPresets = ConfExecutable
					.getRunnerPresets(ClustalO.class);
			assertNotNull(clustalPresets);
			assertTrue(clustalPresets.getPresets().size() > 0);
			clustalPresets.validate(clustalConfig);
			 */
			LimitsManager<ClustalO> clustalLimits = ConfExecutable
					.getRunnerLimits(ClustalO.class);
			assertNotNull(clustalLimits);
			assertTrue(clustalLimits.getLimits().size() > 0);
			//clustalLimits.validate(clustalPresets);

		} catch (FileNotFoundException e) {
			e.printStackTrace();
			fail(e.getLocalizedMessage());
		} catch (IOException e) {
			e.printStackTrace();
			fail(e.getLocalizedMessage());
		}
	}

	@Test(groups = {AllTestSuit.test_group_runner})
	public void testOptionsLocally() {
		try {

			RunnerConfigMarshaller<ClustalO> clustalmarsh = new RunnerConfigMarshaller<ClustalO>(
					RunnerConfig.class);

			RunnerConfig<ClustalO> clustalConfig = clustalmarsh.read(
					new FileInputStream(new File(clustalConfigFile)),
					RunnerConfig.class);

			OptionCombinator clustalOpc = new OptionCombinator(clustalConfig);
			List<String> options = clustalOpc.getOptionsAtRandom();
			for (int i = 0; i < options.size(); i++) {
				System.out.println("Using options: " + options);
				ClustalO clustal = new ClustalO();
				clustal.setInput(AllTestSuit.test_input)
						.setOutput(test_outfile);

				// For local execution use relavive
				ConfiguredExecutable<ClustalO> confClustal = Configurator
						.configureExecutable(clustal, ExecProvider.Local);

				// Add options to the executable
				confClustal.addParameters(options);

				LocalRunner lr = new LocalRunner(confClustal);
				lr.executeJob();
				confClustal = (ConfiguredExecutable<ClustalO>) lr
						.waitForResult();
				assertNotNull(confClustal.getResults());
				Collections.shuffle(options);
			}

		} catch (JobSubmissionException e) {
			e.printStackTrace();
			fail(e.getLocalizedMessage());
		} catch (JobExecutionException e) {
			e.printStackTrace();
			fail(e.getLocalizedMessage());
		} catch (JAXBException e) {
			e.printStackTrace();
			fail(e.getLocalizedMessage());
		} catch (ResultNotAvailableException e) {
			e.printStackTrace();
			fail(e.getLocalizedMessage());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			fail(e.getLocalizedMessage());
		}
	}

	public static final void main(String[] args) throws JobSubmissionException,
			JobExecutionException, InterruptedException {
		ClustalO clustal = new ClustalO();
		clustal.setInput(AllTestSuit.test_input).setOutput(test_outfile);
		// For local execution use relavive
		ConfiguredExecutable<ClustalO> confClustal = Configurator
				.configureExecutable(clustal);
		AsyncExecutor lr = new AsyncLocalRunner();
		lr.submitJob(confClustal);
		Thread.sleep(3000);
		LocalExecutorService.shutDown();
	}

	@Test(groups = {AllTestSuit.test_group_runner})
	public void testPersistance() {
		try {
			ClustalO clustal = new ClustalO();
			clustal.setError("errrr.txt").setInput(AllTestSuit.test_input)
					.setOutput("outtt.txt");
			assertEquals(clustal.getInput(), AllTestSuit.test_input);
			assertEquals(clustal.getError(), "errrr.txt");
			assertEquals(clustal.getOutput(), "outtt.txt");
			ConfiguredExecutable<ClustalO> cClustal = Configurator
					.configureExecutable(clustal, Executable.ExecProvider.Local);

			SyncExecutor sexec = Configurator.getSyncEngine(cClustal);
			sexec.executeJob();
			cClustal = (ConfiguredExecutable<ClustalO>) sexec.waitForResult();
			assertNotNull(cClustal.getResults());
			// Save run configuration
			assertTrue(cClustal.saveRunConfiguration());

			// See if loaded configuration is the same as saved
			RunConfiguration loadedRun = RunConfiguration
					.load(new FileInputStream(new File(cClustal
							.getWorkDirectory(), RunConfiguration.rconfigFile)));
			assertTrue(((ConfExecutable<ClustalO>) cClustal)
					.getRunConfiguration().equals(loadedRun));
			// Load run configuration as ConfExecutable
			ConfiguredExecutable<ClustalO> resurrectedCclustal = (ConfiguredExecutable<ClustalO>) cClustal
					.loadRunConfiguration(new FileInputStream(new File(cClustal
							.getWorkDirectory(), RunConfiguration.rconfigFile)));
			assertNotNull(resurrectedCclustal);
			// See in details whether executables are the same
			assertEquals(resurrectedCclustal.getExecutable(), clustal);

			// Finally rerun the job in the new task directory
			ConfiguredExecutable<ClustalO> resclustal = Configurator
					.configureExecutable(resurrectedCclustal.getExecutable(),
							Executable.ExecProvider.Local);

			sexec = Configurator.getSyncEngine(resclustal,
					Executable.ExecProvider.Local);
			sexec.executeJob();
			cClustal = (ConfiguredExecutable<ClustalO>) sexec.waitForResult();
			assertNotNull(cClustal.getResults());

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
	public void readStatistics() {
		try {
			ClustalO clustal = new ClustalO().setInput(AllTestSuit.test_input)
					.setOutput(test_outfile);
			ConfiguredExecutable<ClustalO> confClustal = Configurator
					.configureExecutable(clustal, Executable.ExecProvider.Local);

			AsyncExecutor sexec = Configurator.getAsyncEngine(confClustal);
			String jobId = sexec.submitJob(confClustal);
			FilePuller fw = FilePuller.newFilePuller(
					confClustal.getWorkDirectory() + File.separator
							+ ClustalW.getStatFile(),
					FileWatcher.MIN_CHUNK_SIZE_BYTES);
			int count = 0;
			long position = 0;
			fw.waitForFile(4);
			while (!(sexec.getJobStatus(jobId) == JobStatus.FINISHED
					|| sexec.getJobStatus(jobId) == JobStatus.FAILED || sexec
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

	@Test(groups = {AllTestSuit.test_group_cluster,
			AllTestSuit.test_group_runner})
	public void readStatisticsClusterExecution() {
		try {
			ClustalO clustal = new ClustalO().setInput(AllTestSuit.test_input)
					.setOutput(test_outfile);
			ConfiguredExecutable<ClustalO> confClustal = Configurator
					.configureExecutable(clustal,
							Executable.ExecProvider.Cluster);

			AsyncExecutor sexec = Configurator.getAsyncEngine(confClustal);
			String jobId = sexec.submitJob(confClustal);
			FilePuller fw = FilePuller.newFilePuller(
					confClustal.getWorkDirectory() + File.separator
							+ ClustalW.getStatFile(),
					FileWatcher.MIN_CHUNK_SIZE_BYTES);
			int count = 0;
			long position = 0;
			fw.waitForFile(200);
			/*
			 * Under certain circumstances DRMAA could report the status wrongly
			 * thus this loop never ends TODO deal with this!
			 */
			while (!(sexec.getJobStatus(jobId) == JobStatus.FINISHED || sexec
					.getJobStatus(jobId) == JobStatus.FAILED)
					|| fw.hasMoreData()) {
				ChunkHolder ch = fw.pull(position);
				String chunk = ch.getChunk();
				position = ch.getNextPosition();
				System.out.print(chunk);
				count++;
				Thread.sleep(200);
				if (sexec.getJobStatus(jobId) == JobStatus.UNDEFINED) {
					System.out.println("DRMAA reported wrong status for job + "
							+ jobId + " continue anyway!");
					break;
				}
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
}
