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

package compbio.engine;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import compbio.engine.client.ConfiguredExecutable;
import compbio.engine.client.Executable;
import compbio.engine.local.AsyncLocalRunner;
import compbio.metadata.AllTestSuit;
import compbio.metadata.ChunkHolder;
import compbio.metadata.JobSubmissionException;
import compbio.metadata.ResultNotAvailableException;
import compbio.runner.msa.ClustalW;
import compbio.runner.msa.Muscle;

public class FilePullerTester {

	public static String test_input = AllTestSuit.TEST_DATA_PATH_ABSOLUTE
			+ "TO1381.fasta";

	String jobId;
	String jobId2;
	String jobId3;

	@BeforeTest(alwaysRun = true)
	public void init() {
		ClustalW clustal = new ClustalW();
		clustal.setInput(test_input);
		Muscle ms = new Muscle().setInput(test_input);
		Muscle ms2 = new Muscle().setInput(test_input);

		try {
			// For local execution use relavive
			ConfiguredExecutable<ClustalW> confClustal = Configurator
					.configureExecutable(clustal, Executable.ExecProvider.Local);
			ConfiguredExecutable<Muscle> confms = Configurator
					.configureExecutable(ms, Executable.ExecProvider.Local);

			ConfiguredExecutable<Muscle> confms2 = Configurator
					.configureExecutable(ms2, Executable.ExecProvider.Local);

			AsyncLocalRunner as = new AsyncLocalRunner();
			jobId = as.submitJob(confClustal);
			jobId2 = as.submitJob(confms);
			jobId3 = as.submitJob(confms2);

			ConfiguredExecutable<?> al = as.getResults(jobId);
			ConfiguredExecutable<?> al2 = as.getResults(jobId2);
			ConfiguredExecutable<?> al3 = as.getResults(jobId3);
			assertNotNull(al);
			assertNotNull(al2);
			assertNotNull(al3);
		} catch (JobSubmissionException e) {
			e.printStackTrace();
			fail(e.getLocalizedMessage());
		} catch (ResultNotAvailableException e) {
			e.printStackTrace();
			fail(e.getLocalizedMessage());
		}
	}

	@Test(groups = { AllTestSuit.test_group_engine })
	public void testPull() {
		assertNotNull(jobId, "init() method failed!");
		String workDir = Configurator.getWorkDirectory(jobId);
		String statFile = workDir + File.separator + ClustalW.getStatFile();

		ChunkHolder ch = ProgressGetter.pull(statFile, 0);
		while (ch == null) {
			ch = ProgressGetter.pull(statFile, 0);
		}
		String chunk = "";
		long pos = 0;
		do {
			chunk = ch.getChunk();
			assertNotNull(chunk);
			pos = ch.getNextPosition();
			ch = ProgressGetter.pull(statFile, pos);
		} while (chunk.length() > 0);

		// All consequent pulls just return empty chunk and same position =
		// file.length
		ch = ProgressGetter.pull(statFile, pos);
		assertNotNull(ch);
		assertEquals(ch.getChunk().length(), 0);
		// Output file size depends on the operation system fs!
		// assertEquals(ch.getNextPosition(), 668);

		ch = ProgressGetter.pull(statFile, pos);
		assertNotNull(ch);
		assertEquals(ch.getChunk().length(), 0);
		// Output file size depends on the operation system and fs!
		// assertEquals(ch.getNextPosition(), 668);

	}

	@Test(groups = { AllTestSuit.test_group_engine })
	public void testGetDelay() {
		FilePuller fp = FilePuller.newFilePuller(Configurator
				.getWorkDirectory(jobId)
				+ File.separator + "stat.log", 256);
		// default delay is 5 minutes
		assertEquals(fp.getDelayValue(TimeUnit.SECONDS), 5 * 60);
		long d = 1000 * 1000 * 1000L * 60; // 1m in nanoseconds
		fp.setDelay(d, TimeUnit.NANOSECONDS);
		assertEquals(fp.getDelayValue(TimeUnit.NANOSECONDS), d);
		assertEquals(fp.getDelayValue(TimeUnit.SECONDS), 60);
		assertEquals(fp.getDelayValue(TimeUnit.MINUTES), 1);
	}

	@Test(groups = { AllTestSuit.test_group_engine }, dependsOnMethods = { "testPull" })
	public void testCache() {
		assertNotNull(jobId, "init() method failed!");
		assertNotNull(jobId2, "init() method failed!");
		assertEquals(PulledFileCache.getSize(), 1); // One is from previous test

		String statFile = Configurator.getWorkDirectory(jobId) + File.separator
				+ ClustalW.getStatFile();
		String statFile2 = Configurator.getWorkDirectory(jobId2)
				+ File.separator + Muscle.getStatFile();
		String statFile3 = Configurator.getWorkDirectory(jobId3)
				+ File.separator + Muscle.getStatFile();

		ChunkHolder ch = ProgressGetter.pull(statFile, 0);
		assertEquals(PulledFileCache.getSize(), 1); // Still one as job has been
		// retrieved from cache
		ChunkHolder ch2 = ProgressGetter
				.pull(statFile2, 0, 5, TimeUnit.SECONDS); // 5
		// second delay
		assertEquals(PulledFileCache.getSize(), 2); // One is from previous test

		// Pull the first job completely
		while (ch == null) {
			ch = ProgressGetter.pull(statFile, 0);
		}
		String chunk = "";
		long pos = 0;
		do {
			chunk = ch.getChunk();
			assertNotNull(chunk);
			pos = ch.getNextPosition();
			ch = ProgressGetter.pull(statFile, pos);
		} while (chunk.length() > 0);

		try {
			Thread.sleep(1000 * 6);
		} catch (InterruptedException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		// Elements are removed on put operation only
		assertEquals(PulledFileCache.getSize(), 2); // One is from previous test
		ChunkHolder ch3 = ProgressGetter.pull(statFile3, 0);
		// Now old element was removed, but new added, thus size remains
		// constant
		assertEquals(PulledFileCache.getSize(), 2); // One is from previous test

	}

	@Test
	public void testGet() {

		FilePuller pp = FilePuller
				.newProgressPuller(AllTestSuit.TEST_DATA_PATH_ABSOLUTE
						+ "percentProgress.txt");
		try {
			assertEquals(pp.getProgress(), 12);
			pp.disconnect();
		} catch (NumberFormatException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
}
