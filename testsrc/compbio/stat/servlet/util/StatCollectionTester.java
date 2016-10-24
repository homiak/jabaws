package compbio.stat.servlet.util;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.Test;

public class StatCollectionTester {

	@Test
	public void testGetStats() {
		Map<Date, Totals> stats;
		Calendar cal = GregorianCalendar.getInstance();
		cal.add(Calendar.MONTH, -3);

		try {
			stats = StatCollection.getStats(cal.getTime());

		} catch (SQLException e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}

	}

}
