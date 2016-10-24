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

import javax.xml.bind.ValidationException;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import compbio.engine.AsyncExecutor;
import compbio.engine.Configurator;
import compbio.engine.FilePuller;
import compbio.engine.SyncExecutor;
import compbio.engine.client.ConfExecutable;
import compbio.engine.client.ConfiguredExecutable;
import compbio.engine.client.Executable;
import compbio.engine.client.RunConfiguration;
import compbio.engine.client.Executable.ExecProvider;
import compbio.engine.cluster.drmaa.JobRunner;
import compbio.metadata.AllTestSuit;
import compbio.metadata.ChunkHolder;
import compbio.metadata.JobExecutionException;
import compbio.metadata.JobStatus;
import compbio.metadata.JobSubmissionException;
import compbio.metadata.LimitsManager;
import compbio.metadata.PresetManager;
import compbio.metadata.ResultNotAvailableException;
import compbio.metadata.RunnerConfig;
import compbio.runner.msa.Mafft;
import compbio.util.SysPrefs;

public class MafftTester {

	private Mafft mafft;

	@BeforeMethod(groups = { AllTestSuit.test_group_cluster,
			AllTestSuit.test_group_runner, AllTestSuit.test_group_non_windows })
	public void init() {
		mafft = new Mafft();
		// mafft.setParameter("--aamatrix");
		// mafft.setParameter("PAM100");
		mafft.setInput(AllTestSuit.test_input); // .setOutput("Mafft.out").setError("mafft.progress");
	}

	@Test(groups = { AllTestSuit.test_group_runner,
			AllTestSuit.test_group_non_windows })
	public void testSetInputTester() {
		Mafft mf = new Mafft();
		// System.out.println(mf.getParameters(null));
		mf.setInput("INNN");
		// System.out.println(mf.getParameters(null));
		mf.setError("ERRR");
		mf.setInput("INN222");
		mf.setOutput("OUT");
		// System.out.println(mf.getParameters(null));
	}

	@Test(groups = { AllTestSuit.test_group_runner,
			AllTestSuit.test_group_non_windows })
	public void testExecute() {
		try {
			ConfiguredExecutable<Mafft> cmafft = Configurator
					.configureExecutable(mafft, Executable.ExecProvider.Local);
			cmafft.getParameters().setParam("--aamatrix", "PAM120");
			SyncExecutor sexecutor = Configurator.getSyncEngine(cmafft);
			sexecutor.executeJob();
			cmafft = (ConfiguredExecutable<Mafft>) sexecutor.waitForResult();
			assertNotNull(cmafft.getResults());
		} catch (JobSubmissionException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (JobExecutionException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (ResultNotAvailableException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	@Test(groups = { AllTestSuit.test_group_runner,
			AllTestSuit.test_group_non_windows })
	public void testPersistance() {
		try {
			Mafft mafft = new Mafft();
			mafft.setError("errrr.txt").setInput(AllTestSuit.test_input)
					.setOutput("outtt.txt");
			assertEquals(mafft.getInput(), AllTestSuit.test_input);
			assertEquals(mafft.getError(), "errrr.txt");
			assertEquals(mafft.getOutput(), "outtt.txt");
			ConfiguredExecutable<Mafft> cmafft = Configurator
					.configureExecutable(mafft, Executable.ExecProvider.Local);

			SyncExecutor sexec = Configurator.getSyncEngine(cmafft);
			sexec.executeJob();
			ConfiguredExecutable<?> al = sexec.waitForResult();
			assertNotNull(al.getResults());
			// Save run configuration
			assertTrue(cmafft.saveRunConfiguration());

			// See if loaded configuration is the same as saved
			RunConfiguration loadedRun = RunConfiguration
					.load(new FileInputStream(new File(cmafft
							.getWorkDirectory(), RunConfiguration.rconfigFile)));
			assertEquals(
					((ConfExecutable<Mafft>) cmafft).getRunConfiguration(),
					loadedRun);
			// Load run configuration as ConfExecutable
			ConfiguredExecutable<Mafft> resurrectedCMafft = (ConfiguredExecutable<Mafft>) cmafft
					.loadRunConfiguration(new FileInputStream(new File(cmafft
							.getWorkDirectory(), RunConfiguration.rconfigFile)));
			assertNotNull(resurrectedCMafft);
			// See in details whether executables are the same
			assertEquals(resurrectedCMafft.getExecutable(), mafft);

			// Finally rerun the job in the new task directory
			ConfiguredExecutable<Mafft> resmafft = Configurator
					.configureExecutable(resurrectedCMafft.getExecutable(),
							Executable.ExecProvider.Local);

			sexec = Configurator.getSyncEngine(resmafft,
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

	@Test(groups = { AllTestSuit.test_group_cluster,
			AllTestSuit.test_group_runner, AllTestSuit.test_group_non_windows })
	public void testClusterExecute() {
		try {
			
			ConfiguredExecutable<Mafft> cmafft = Configurator
					.configureExecutable(mafft, Executable.ExecProvider.Cluster);
			JobRunner sexecutor = (JobRunner) Configurator.getSyncEngine(
					cmafft, Executable.ExecProvider.Cluster);
			sexecutor.executeJob();
			ConfiguredExecutable<?> al = sexecutor.waitForResult();
			assertNotNull(al.getResults());
		} catch (JobSubmissionException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (JobExecutionException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (ResultNotAvailableException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	/**
	 * This test demonstrates the problem with Drmaa which can return UNDEFINED status after Running!
	 * enable it to see the problem 
	 */
	@Test(enabled=false, groups = { AllTestSuit.test_group_cluster,
			AllTestSuit.test_group_runner, AllTestSuit.test_group_non_windows })
	public void testRunOnClusterAsyncCheckStatusShortPoolingTime() {
		assertFalse(SysPrefs.isWindows,
				"Cluster execution can only be in unix environment");
		try {
			ConfiguredExecutable<Mafft> confMafft = Configurator
					.configureExecutable(mafft, Executable.ExecProvider.Cluster);
			AsyncExecutor aengine = Configurator.getAsyncEngine(confMafft);
			String jobId = aengine.submitJob(confMafft);
			assertNotNull(jobId, "Runner is NULL");
			// let drmaa to start
			Thread.sleep(500);
			boolean run = false;
			JobStatus status = aengine.getJobStatus(jobId);
			while (status != JobStatus.FINISHED) {
				if (status == JobStatus.RUNNING) {
					run = true;
				}
				Thread.sleep(500);
				status = aengine.getJobStatus(jobId);
				// Once the job was in the RUNNING state UNDEFINED should not
				// occur
				// Unfortunately with a short pooling time like here
				// this problem occurs. There seems to be a bug in DRMAA SGE
				// implementation
				// Perhaps longer pooling time e.g. 5 second will fix the issue
				// see the next test case for this
				if (run) {
					assertNotSame(status, JobStatus.UNDEFINED);
				}
			}

		} catch (JobSubmissionException e) {
			e.printStackTrace();
			fail("DrmaaException caught:" + e.getMessage());
		} catch (InterruptedException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	@Test(groups = { AllTestSuit.test_group_cluster,
			AllTestSuit.test_group_runner, AllTestSuit.test_group_non_windows })
	public void testRunOnClusterAsyncCheckStatusLongPoolingTime() {
		assertFalse(SysPrefs.isWindows,
				"Cluster execution can only be in unix environment");
		try {
			ConfiguredExecutable<Mafft> confMafft = Configurator
					.configureExecutable(mafft, Executable.ExecProvider.Cluster);
			AsyncExecutor aengine = Configurator.getAsyncEngine(confMafft);
			String jobId = aengine.submitJob(confMafft);
			assertNotNull(jobId, "Runner is NULL");
			// let drmaa to start
			Thread.sleep(500);
			boolean run = false;
			JobStatus status = aengine.getJobStatus(jobId);
			while (status != JobStatus.FINISHED) {
				if (status == JobStatus.RUNNING) {
					run = true;
				}
				Thread.sleep(5000);
				status = aengine.getJobStatus(jobId);
				// Once the job was in the RUNNING state UNDEFINED should not
				// occur
				// Hopefully with a long pooling time like here
				// this problem should not occur. There seems to be a bug in
				// DRMAA SGE implementation
				if (run) {
					assertNotSame(status, JobStatus.UNDEFINED);
				}
			}

		} catch (JobSubmissionException e) {
			e.printStackTrace();
			fail("DrmaaException caught:" + e.getMessage());
		} catch (InterruptedException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	@Test(groups = { AllTestSuit.test_group_runner,
			AllTestSuit.test_group_non_windows })
	public void readStatistics() {
		Mafft mafft = new Mafft();
		mafft.setError("errrr.txt").setInput(AllTestSuit.test_input).setOutput(
				"outtt.txt");
		ConfiguredExecutable<Mafft> cmafft;

		try {
			cmafft = Configurator.configureExecutable(mafft,
					Executable.ExecProvider.Local);
			AsyncExecutor sexec = Configurator.getAsyncEngine(cmafft,
					ExecProvider.Local);
			String jobId = sexec.submitJob(cmafft);
			FilePuller fw = FilePuller.newFilePuller(compbio.engine.client.Util
					.getFullPath(cmafft.getWorkDirectory(), cmafft.getError()),
					256);
			int count = 0;
			long position = 0;
			fw.waitForFile(4);
			while (fw.hasMoreData()) {
				ChunkHolder ch = fw.pull(position);
				String chunk = ch.getChunk();
				position = ch.getNextPosition();
				System.out.print("CHUNK:" + chunk);
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
	public void testConfigurationLoading() {
		try {
			RunnerConfig<Mafft> mafftConfig = ConfExecutable
					.getRunnerOptions(Mafft.class);
			assertNotNull(mafftConfig);
			assertTrue(mafftConfig.getArguments().size() > 0);

			PresetManager<Mafft> mafftPresets = ConfExecutable
					.getRunnerPresets(Mafft.class);
			assertNotNull(mafftPresets);
			assertTrue(mafftPresets.getPresets().size() > 0);
			mafftPresets.validate(mafftConfig);

			LimitsManager<Mafft> mafftLimits = ConfExecutable
					.getRunnerLimits(Mafft.class);
			assertNotNull(mafftLimits);
			assertTrue(mafftLimits.getLimits().size() > 0);
			mafftLimits.validate(mafftPresets);

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
