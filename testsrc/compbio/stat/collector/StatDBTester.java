package compbio.stat.collector;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import compbio.stat.servlet.util.StatCollection;
import compbio.stat.servlet.util.Totals;
import compbio.ws.client.Services;

public class StatDBTester {
	StatDB statdb;
	Timestamp from;
	Timestamp to;
	
	@BeforeClass(alwaysRun=true )
	public void init() {
		try {
			statdb = new StatDB(true);
			Calendar fromCal = Calendar.getInstance();
			fromCal.set(2011, 4, 1);
						
			Calendar toCal = Calendar.getInstance();
			toCal.set(2011, 5, 1);
			from = new Timestamp(fromCal.getTimeInMillis());
			to = new Timestamp(toCal.getTimeInMillis());
			
		} catch (SQLException e) {
			e.printStackTrace();
			Assert.fail(e.getLocalizedMessage());
		}
	}

	/*
	 * This test fails if run with other tests. This is likely due to the fact that StatDB 
	 * is initialised with connection to other then the test database from StatCollector class.
	 * TODO look at this
	 */
	@Test(enabled = false)
	public void testReadYearData() {
		Calendar cal = Calendar.getInstance();
		cal.set(2010, 4, 1);
		try {
			List<JobStat> jobs = statdb.readData(
					new Timestamp(cal.getTimeInMillis()), to, Services.MuscleWS, false);
			assertNotNull(jobs);
			assertEquals(jobs.size(), 1294);
			// statdb.shutdownDBServer();
		} catch (SQLException e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}

	}

	/*
	 * This test fails if run with other tests. This is likely due to the fact that StatDB 
	 * is initialised with connection to other then the test database from StatCollector class.
	 * TODO look at this
	 */
	@Test(enabled =false)
	public void testReadOneMonthData() {
		try {
			List<JobStat> jobs = statdb.readData(from, to, Services.TcoffeeWS, false);
			assertNotNull(jobs);
			assertEquals(jobs.size(), 36);

			jobs = statdb.readData(from,to, Services.ClustalWS,false);

			assertNotNull(jobs);
			assertEquals(jobs.size(), 137);

			jobs = statdb.readData(from, to, Services.MafftWS,false);
			assertNotNull(jobs);
			assertEquals(jobs.size(), 136);

			jobs = statdb.readData(from,to, Services.ProbconsWS,false);
			assertNotNull(jobs);
			assertEquals(jobs.size(), 9);

			jobs = statdb.readData(from,to, Services.MuscleWS,false);
			assertNotNull(jobs);
			assertEquals(jobs.size(), 63);

		} catch (SQLException e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
	}

	/*
	 * This test fails if run with other tests. This is likely due to the fact that StatDB 
	 * is initialised with connection to other then the test database from StatCollector class.
	 * TODO look at this
	 */
	@Test(enabled=false)
	public void testGetEarliestRecord() {
		try {
			Date earliestRec = statdb.getEarliestRecord();
			
			assertEquals(1278543600000L, earliestRec.getTime());
		} catch (SQLException e) {
			e.printStackTrace();
			Assert.fail(e.getLocalizedMessage());
		}

	}

	@Test(sequential=true)
	public void testVerifyJobsCount() {

		try {
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.MONTH, -5);
			Timestamp from = new Timestamp(cal.getTimeInMillis());
			cal.add(Calendar.MONTH, 1);
			Timestamp to = new Timestamp(cal.getTimeInMillis());
			StatCollection sc = StatCollection.newStatCollecton(from, to);
			Totals t = Totals.sumStats(sc.getAllStat());

			//System.out.println(sc.getAllStat());
			
			assertEquals(t.getTotal(), statdb.getTotalJobsCount(from, to));
			assertEquals(t.getAbandoned(), statdb.getAbandonedCount(from, to));
			assertEquals(t.getCancelled(), statdb.getCancelledCount(from, to));
			assertEquals(t.getIncomplete(), statdb.getIncompleteCount(from, to));

		} catch (SQLException e) {
			e.printStackTrace();
			Assert.fail(e.getLocalizedMessage());
		}

	}
}
