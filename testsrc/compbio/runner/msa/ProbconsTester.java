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
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
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
import compbio.engine.cluster.drmaa.JobRunner;
import compbio.metadata.AllTestSuit;
import compbio.metadata.ChunkHolder;
import compbio.metadata.JobExecutionException;
import compbio.metadata.JobSubmissionException;
import compbio.metadata.LimitsManager;
import compbio.metadata.PresetManager;
import compbio.metadata.ResultNotAvailableException;
import compbio.metadata.RunnerConfig;

public class ProbconsTester {

	private Probcons probc;

	@BeforeMethod(groups = { AllTestSuit.test_group_cluster,
			AllTestSuit.test_group_runner, AllTestSuit.test_group_non_windows })
	public void init() {
		probc = new Probcons();
		probc.setInput(AllTestSuit.test_input); // .setOutput("Mafft.out").setError("mafft.progress");
	}

	@Test(groups = { AllTestSuit.test_group_runner,
			AllTestSuit.test_group_non_windows })
	public void testSetInputTester() {
		Probcons mf = new Probcons();
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
			ConfiguredExecutable<Probcons> cmafft = Configurator
					.configureExecutable(probc, Executable.ExecProvider.Local);
			// option for sub matrix is not supported
			// cmafft.getParameters().setParam("--matrixfile", "PAM200");
			SyncExecutor sexecutor = Configurator.getSyncEngine(cmafft);
			sexecutor.executeJob();
			cmafft = (ConfiguredExecutable<Probcons>) sexecutor.waitForResult();
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
			Probcons mafft = new Probcons();
			mafft.setError("errrr.txt").setInput(AllTestSuit.test_input)
					.setOutput("outtt.txt");
			assertEquals(mafft.getInput(), AllTestSuit.test_input);
			assertEquals(mafft.getError(), "errrr.txt");
			assertEquals(mafft.getOutput(), "outtt.txt");
			ConfiguredExecutable<Probcons> cmafft = Configurator
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
			assertEquals(((ConfExecutable<Probcons>) cmafft)
					.getRunConfiguration(), loadedRun);
			// Load run configuration as ConfExecutable
			ConfiguredExecutable<Probcons> resurrectedCMafft = (ConfiguredExecutable<Probcons>) cmafft
					.loadRunConfiguration(new FileInputStream(new File(cmafft
							.getWorkDirectory(), RunConfiguration.rconfigFile)));
			assertNotNull(resurrectedCMafft);
			// See in details whether executables are the same
			assertEquals(resurrectedCMafft.getExecutable(), mafft);

			// Finally rerun the job in the new task directory
			ConfiguredExecutable<Probcons> resmafft = Configurator
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
			ConfiguredExecutable<Probcons> cmafft = Configurator
					.configureExecutable(probc, Executable.ExecProvider.Cluster);
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

	@Test(groups = { AllTestSuit.test_group_runner,
			AllTestSuit.test_group_non_windows })
	public void readStatistics() {
		Probcons probs = new Probcons();
		probs.setError("errrr.txt").setInput(AllTestSuit.test_input).setOutput(
				"outtt.txt");
		ConfiguredExecutable<Probcons> cprobs;

		try {
			cprobs = Configurator.configureExecutable(probs,
					Executable.ExecProvider.Local);
			AsyncExecutor sexec = Configurator.getAsyncEngine(cprobs);
			String jobId = sexec.submitJob(cprobs);
			FilePuller fw = FilePuller.newFilePuller(compbio.engine.client.Util
					.getFullPath(cprobs.getWorkDirectory(), cprobs.getError()),
					256);
			ConfiguredExecutable<?> al = sexec.getResults(jobId);
			assertNotNull(al.getResults());
			// Code below is performance dependent 
			// thus cannot be moved up before the results is obtained
			int count = 0;
			long position = 0;
			fw.waitForFile(6);
			while (fw.hasMoreData()) {
				ChunkHolder ch = fw.pull(position);
				String chunk = ch.getChunk();
				position = ch.getNextPosition();
				System.out.print("CHUNK:" + chunk);
				count++;
			}
			assertTrue(count > 1, "TaskId:" + jobId);

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
			RunnerConfig<Probcons> probsConfig = ConfExecutable
					.getRunnerOptions(Probcons.class);
			assertNotNull(probsConfig);
			assertTrue(probsConfig.getArguments().size() > 0);

			PresetManager<Probcons> probsPresets = ConfExecutable
					.getRunnerPresets(Probcons.class);
			assertNull(probsPresets);

			LimitsManager<Probcons> probsLimits = ConfExecutable
					.getRunnerLimits(Probcons.class);
			assertNotNull(probsLimits);
			assertTrue(probsLimits.getLimits().size() > 0);
			probsLimits.validate(probsPresets);

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
