package compbio.stat.collector;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.testng.Assert;
import org.testng.annotations.Test;

import compbio.metadata.AllTestSuit;
import compbio.stat.collector.ExecutionStatCollector.JobDirectory;

public class ExecutionStatCollectorTester {

	final static String LOCAL_JOBS = AllTestSuit.TEST_DATA_PATH_ABSOLUTE
			+ "ljobs";
	final static String CLUSTER_JOBS = AllTestSuit.TEST_DATA_PATH_ABSOLUTE
			+ "jobs";
	@Test
	public void testCollectStat() {
		ExecutionStatCollector local_jobs = new ExecutionStatCollector(
				LOCAL_JOBS, 1);
		ExecutionStatCollector cl_jobs = new ExecutionStatCollector(
				CLUSTER_JOBS, 24);
		
		// Collect statistics prior to call getStats()!
		local_jobs.collectStatistics();
		cl_jobs.collectStatistics();
		
		StatProcessor local_stats = local_jobs.getStats();
		StatProcessor cl_stats = cl_jobs.getStats();
		
		assertEquals(local_stats.getJobNumber(), 12);
		// ClustalW#1015343425414965 - empty
		assertEquals(local_stats.getIncompleteJobs().size(), 1);
		assertEquals(local_stats.getFailedJobs().size(), 0);
		assertEquals(local_stats.getAbandonedJobs().size(), 1);

	}

	@Test
	public void testUpdateStatTester() {

		ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
		ScheduledFuture<?> sf = executor.scheduleAtFixedRate(
				new ExecutionStatCollector(LOCAL_JOBS, 1), 1, 10,
				TimeUnit.SECONDS);

		try {
			Thread.sleep(1000 * 35);
			executor.shutdown();
			executor.awaitTermination(300, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
	}

	/**
	 * This test will fail in time as it relies on last access time for file in
	 * the filesystem! This OK as it has been tested by then.
	 * 
	 */
	@Test (enabled = false)
	public void testHasTimedOut() {
		ExecutionStatCollector ecol = new ExecutionStatCollector(LOCAL_JOBS, 1);
		File f = new File(LOCAL_JOBS + File.separator
				+ "ClustalW#1015373448154965");
		File timedOut = new File(LOCAL_JOBS + File.separator
				+ "Mafft#7868649707286965");

		JobDirectory jd = new JobDirectory(f);
		JobDirectory timedOutDir = new JobDirectory(timedOut);
		System.out.println("! " + new Date(f.lastModified()));

		assertTrue((System.currentTimeMillis() - f.lastModified())
				/ (1000 * 60 * 60) < 1);
		assertFalse((System.currentTimeMillis() - timedOut.lastModified())
				/ (1000 * 60 * 60) < 1);
		assertFalse(ecol.hasTimedOut(jd));
		assertTrue(ecol.hasTimedOut(timedOutDir));
	}

	@Test
	public void testHasCompleted() {
		ExecutionStatCollector ecol_no_timeout = new ExecutionStatCollector(
				LOCAL_JOBS, 10000000);
		ExecutionStatCollector ecol = new ExecutionStatCollector(LOCAL_JOBS, 1);

		File normal = new File(LOCAL_JOBS + File.separator
				+ "ClustalW#100368900075204");
		File finished = new File(LOCAL_JOBS + File.separator
				+ "ClustalW#1015373448154965");
		File cancelled = new File(LOCAL_JOBS + File.separator
				+ "Mafft#7918237850044965");
		File noresult = new File(LOCAL_JOBS + File.separator
				+ "ClustalW#1015343425414965");

		JobDirectory dnormal = new JobDirectory(normal);
		JobDirectory dfinished = new JobDirectory(finished);
		JobDirectory dcancelled = new JobDirectory(cancelled);

		JobDirectory dnoresult = new JobDirectory(noresult);

		assertTrue(ecol.hasCompleted(dnormal));
		assertTrue(ecol.hasCompleted(dfinished));
		assertTrue(ecol.hasCompleted(dcancelled));

		assertTrue(ecol.hasCompleted(dnoresult));
		assertFalse(ecol_no_timeout.hasCompleted(dnoresult));

	}
}
