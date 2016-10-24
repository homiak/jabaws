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
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.xml.bind.ValidationException;

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
import compbio.runner.msa.Tcoffee;
import compbio.util.FileWatcher;

public class TcoffeeTester {

	private Tcoffee tcoffee;

	@BeforeMethod(groups = { AllTestSuit.test_group_cluster,
			AllTestSuit.test_group_runner, AllTestSuit.test_group_non_windows })
	public void init() {
		tcoffee = new Tcoffee();
		tcoffee.setInput(AllTestSuit.test_input).setOutput("tcoffee.out")
				.setError("tcoffee.progress");
	}

	@Test(groups = { AllTestSuit.test_group_runner,
			AllTestSuit.test_group_non_windows })
	public void testExecute() {
		try {
			ConfiguredExecutable<Tcoffee> ctcoffee = Configurator
					.configureExecutable(tcoffee, Executable.ExecProvider.Local);
			// matrix does not appear to work
			// ctcoffee.getParameters().setParam("-matrix","BLOSUM62");
			SyncExecutor sexecutor = Configurator.getSyncEngine(ctcoffee);
			sexecutor.executeJob();
			ConfiguredExecutable<?> al = sexecutor.waitForResult();
			Alignment align = al.getResults();
			assertNotNull(align);

			// System.out.println("Tcoffee stat file: " + tcoffee.getError());
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
		try {
			Tcoffee tcoffee = new Tcoffee().setInput(AllTestSuit.test_input);
			ConfiguredExecutable<Tcoffee> confTcoffee = Configurator
					.configureExecutable(tcoffee, Executable.ExecProvider.Local);

			AsyncExecutor sexec = Configurator.getAsyncEngine(confTcoffee);
			String jobId = sexec.submitJob(confTcoffee);
			FilePuller fw = FilePuller.newFilePuller(confTcoffee
					.getWorkDirectory()
					+ File.separator + tcoffee.getError(),
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

	@Test(groups = { AllTestSuit.test_group_cluster,
			AllTestSuit.test_group_runner, AllTestSuit.test_group_non_windows })
	public void testClusterExecute() {
		try {
			ConfiguredExecutable<Tcoffee> cmafft = Configurator
					.configureExecutable(tcoffee,
							Executable.ExecProvider.Cluster);
			JobRunner sexecutor = (JobRunner) Configurator.getSyncEngine(
					cmafft, Executable.ExecProvider.Cluster);
			sexecutor.executeJob();
			ConfiguredExecutable<?> al = sexecutor.waitForResult();
			Alignment align = al.getResults();
			assertNotNull(align);
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
	public void testNcore() {
		Tcoffee tc = new Tcoffee();
		// System.out.println("TCPRM:" + tc.getParameters(null));
		assertEquals(tc.getParameters(null).size(), 3);
		tc.setNCore(2);
		assertEquals(2, tc.getNCore());
		assertEquals(tc.getParameters(null).size(), 3);
		tc.setNCore(4);
		assertEquals(4, tc.getNCore());
		assertEquals(tc.getParameters(null).size(), 3);
	}

	@Test(groups = { AllTestSuit.test_group_cluster,
			AllTestSuit.test_group_runner, AllTestSuit.test_group_non_windows })
	public void testPersistance() {
		try {
			Tcoffee tcoffee = new Tcoffee();
			tcoffee.setError("errrr.txt").setInput(AllTestSuit.test_input)
					.setOutput("outtt.txt");
			assertEquals(tcoffee.getInput(), AllTestSuit.test_input);
			assertEquals(tcoffee.getError(), "errrr.txt");
			assertEquals(tcoffee.getOutput(), "outtt.txt");
			ConfiguredExecutable<Tcoffee> ctcofee = Configurator
					.configureExecutable(tcoffee, Executable.ExecProvider.Local);

			SyncExecutor sexec = Configurator.getSyncEngine(ctcofee);
			sexec.executeJob();
			ConfiguredExecutable<?> al = sexec.waitForResult();
			assertNotNull(al.getResults());
			// Save run configuration
			assertTrue(ctcofee.saveRunConfiguration());

			// See if loaded configuration is the same as saved
			RunConfiguration loadedRun = RunConfiguration
					.load(new FileInputStream(new File(ctcofee
							.getWorkDirectory(), RunConfiguration.rconfigFile)));
			assertEquals(((ConfExecutable<Tcoffee>) ctcofee)
					.getRunConfiguration(), loadedRun);
			// Load run configuration as ConfExecutable
			ConfiguredExecutable<Tcoffee> resurrectedCTcoffee = (ConfiguredExecutable<Tcoffee>) ctcofee
					.loadRunConfiguration(new FileInputStream(new File(ctcofee
							.getWorkDirectory(), RunConfiguration.rconfigFile)));
			assertNotNull(resurrectedCTcoffee);
			// See in details whether executables are the same
			assertEquals(resurrectedCTcoffee.getExecutable(), tcoffee);

			// Finally rerun the job in the new task directory
			ConfiguredExecutable<Tcoffee> restcoffee = Configurator
					.configureExecutable(resurrectedCTcoffee.getExecutable(),
							Executable.ExecProvider.Local);

			sexec = Configurator.getSyncEngine(restcoffee,
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
			RunnerConfig<Tcoffee> tcoffeeConfig = ConfExecutable
					.getRunnerOptions(Tcoffee.class);
			assertNotNull(tcoffeeConfig);
			assertTrue(tcoffeeConfig.getArguments().size() > 0);

			PresetManager<Tcoffee> tcoffeePresets = ConfExecutable
					.getRunnerPresets(Tcoffee.class);
			assertNotNull(tcoffeePresets);
			assertTrue(tcoffeePresets.getPresets().size() > 0);
			tcoffeePresets.validate(tcoffeeConfig);

			LimitsManager<Tcoffee> tcoffeeLimits = ConfExecutable
					.getRunnerLimits(Tcoffee.class);
			assertNotNull(tcoffeeLimits);
			assertTrue(tcoffeeLimits.getLimits().size() > 0);
			tcoffeeLimits.validate(tcoffeePresets);
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
