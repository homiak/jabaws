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

package compbio.engine.cluster.drmaa;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import org.testng.annotations.Test;

import compbio.engine.AsyncExecutor;
import compbio.engine.Configurator;
import compbio.engine.Job;
import compbio.engine.client.ConfiguredExecutable;
import compbio.engine.client.Executable;
import compbio.metadata.AllTestSuit;
import compbio.metadata.JobSubmissionException;
import compbio.metadata.ResultNotAvailableException;
import compbio.runner.msa.ClustalW;
import compbio.util.Util;

public class ClusterSessionTester {

	@Test(sequential = true, groups = { AllTestSuit.test_group_cluster,
			AllTestSuit.test_group_engine })
	public void testTaskList() {
		ClustalW cl = new ClustalW();

		try {
			ConfiguredExecutable<ClustalW> clw = Configurator
					.configureExecutable(cl, Executable.ExecProvider.Cluster);
			JobRunner jr = JobRunner.getInstance(clw);
			String jobId = jr.submitJob();
			ClusterSession cs = ClusterSession.getInstance();
			// this only holds for sequential execution
			//assertEquals(cs.jobs.size(), 1);
			assertTrue(Job.getByTaskId(jobId, cs.jobs) != null);
			assertEquals(Job.getByTaskId(jobId, cs.jobs).getConfExecutable(),
					clw);
			jr.cancelJob(jobId);
			// this only holds for sequential execution
			assertEquals(cs.jobs.size(), 0);

			clw = Configurator.configureExecutable(cl,
					Executable.ExecProvider.Cluster);
			assertFalse(Util.isEmpty(clw.getWorkDirectory()));
			AsyncExecutor aengine = Configurator.getAsyncEngine(clw);
			jobId = aengine.submitJob(clw);
			assertEquals(cs.jobs.size(), 1);
			assertTrue(Job.getByTaskId(jobId, cs.jobs) != null);
			assertEquals(Job.getByTaskId(jobId, cs.jobs).getConfExecutable(),
					clw);
			assertFalse(Util.isEmpty(compbio.engine.Configurator
					.getWorkDirectory(jobId)));
			String workDir = clw.getWorkDirectory();
			assertEquals(workDir, compbio.engine.Configurator
					.getWorkDirectory(jobId));

			aengine.getResults(jobId);
			assertEquals(cs.jobs.size(), 0);
			assertFalse(Util.isEmpty(clw.getWorkDirectory()));
			// after job has been removed from the list (e.g when completed)
			// reference to a work directory can still be obtained by other
			// means
			assertFalse(Util.isEmpty(compbio.engine.Configurator
					.getWorkDirectory(jobId)));
			assertEquals(compbio.engine.Configurator.getWorkDirectory(jobId),
					workDir);

		} catch (JobSubmissionException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (ResultNotAvailableException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

}
