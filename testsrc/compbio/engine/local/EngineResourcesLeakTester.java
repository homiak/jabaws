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
package compbio.engine.local;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.annotations.Test;

import compbio.engine.AsyncExecutor;
import compbio.engine.Configurator;
import compbio.engine.SubmissionManager;
import compbio.engine.client.ConfiguredExecutable;
import compbio.engine.client.Executable;
import compbio.metadata.AllTestSuit;
import compbio.metadata.JobStatus;
import compbio.metadata.JobSubmissionException;
import compbio.metadata.ResultNotAvailableException;
import compbio.runner.msa.ClustalW;
import compbio.runner.msa.Muscle;

public class EngineResourcesLeakTester {

	static int numberOfExecutions = 50;

	@Test(groups = { AllTestSuit.test_group_long })
	public void loadEngineRetrieveResultsManyTimes() {

		List<String> idlist = new ArrayList<String>();
		ClustalW clu = new ClustalW();
		clu.setInput(AllTestSuit.test_input_real);
		try {
			for (int i = 0; i < numberOfExecutions; i++) {
				ConfiguredExecutable<ClustalW> cclustal = Configurator
						.configureExecutable(clu, Executable.ExecProvider.Local);
				AsyncExecutor engine = Configurator.getAsyncEngine(cclustal);
				String jobId = engine.submitJob(cclustal);
				assertNotNull(jobId);
				Thread.sleep(50);
				assertNotNull(SubmissionManager.getTask(jobId));
				assertTrue(idlist.add(jobId));
			}
		} catch (JobSubmissionException e) {
			e.printStackTrace();
			fail(e.getLocalizedMessage());
		} catch (InterruptedException e) {
			e.printStackTrace();
			fail(e.getLocalizedMessage());
		}

		assertEquals(idlist.size(), numberOfExecutions);
		// assertEquals(SubmissionManager.submittedTasks.size(),
		// numberOfExecutions * 2);

		try {
			while (true) {
				int doneCounter = 0;
				for (String taskId : idlist) {
					AsyncExecutor engine = Configurator.getAsyncEngine(taskId);
					JobStatus status = engine.getJobStatus(taskId);
					System.out.println("Status for job: " + taskId + " "
							+ status);

					if (status == JobStatus.FINISHED) {
						assertNotNull(taskId);
						System.out.println(taskId);
						// System.out.println(SubmissionManager.submittedTasks);
						// assertNotNull(SubmissionManager.getTask(taskId));
						Thread.sleep(200); // let buffers to be written
						ConfiguredExecutable<ClustalW> confclust = (ConfiguredExecutable<ClustalW>) engine
								.getResults(taskId);
						assertNotNull(confclust);
						assertNotNull(confclust.getResults());
						doneCounter++;
					}
				}
				if (doneCounter == numberOfExecutions) {
					break;
				}
			}
		} catch (ResultNotAvailableException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (InterruptedException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	@Test(groups = { AllTestSuit.test_group_long  })
	public void loadEngineRetrieveResultsOnce() {

		Map<String, Boolean> idlist = new HashMap<String, Boolean>();
		Muscle muscle = new Muscle();
		muscle.setInput(AllTestSuit.test_input_real);
		muscle.setOutput("muscle.out");
		try {
			for (int i = 0; i < numberOfExecutions; i++) {
				ConfiguredExecutable<Muscle> cmuscle = Configurator
						.configureExecutable(muscle,
								Executable.ExecProvider.Local);
				AsyncExecutor engine = Configurator.getAsyncEngine(cmuscle);
				String jobId = engine.submitJob(cmuscle);
				assertNotNull(jobId);
				Thread.sleep(50);
				assertNotNull(SubmissionManager.getTask(jobId));
				assertTrue(idlist.put(jobId, false) == null);
			}
		} catch (JobSubmissionException e) {
			e.printStackTrace();
			fail(e.getLocalizedMessage());
		} catch (InterruptedException e) {
			e.printStackTrace();
			fail(e.getLocalizedMessage());
		}

		assertEquals(idlist.size(), numberOfExecutions);
		// assertEquals(SubmissionManager.submittedTasks.size(), 20);

		try {
			int doneCounter = 0;
			while (true) {
				if (doneCounter == numberOfExecutions) {
					break;
				}
				for (String taskId : idlist.keySet()) {
					Thread.sleep(50); // slow checking down
					AsyncExecutor engine = Configurator.getAsyncEngine(taskId);
					JobStatus status = engine.getJobStatus(taskId);
					System.out.println("Status for job: " + taskId + " "
							+ status);

					if (status == JobStatus.FINISHED) {
						assertNotNull(taskId);
						if (idlist.get(taskId)) {
							continue;
						}
						System.out.println(taskId);
						// System.out.println(SubmissionManager.submittedTasks);
						// assertNotNull(SubmissionManager.getTask(taskId));
						Thread.sleep(100);// let the buffers to write things
						ConfiguredExecutable<ClustalW> confclust = (ConfiguredExecutable<ClustalW>) engine
								.getResults(taskId);
						assertNotNull(confclust);
						assertNotNull(confclust.getResults());
						idlist.put(taskId, true);
						doneCounter++;
					}
				}
			}
		} catch (ResultNotAvailableException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (InterruptedException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
}
