package compbio.ws.client;

import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.io.PrintWriter;
import java.net.ConnectException;

import javax.xml.ws.WebServiceException;

import org.testng.annotations.Test;

import compbio.metadata.AllTestSuit;

public class WSTesterTester {

	public static final String SERVER = "http://localhost:8080/jabaws";
	// public static final String SERVER = "http://192.168.202.130/jabaws";

	// public static final String SERVER =
	// "http://nanna.cluster.lifesci.dundee.ac.uk:8080/jaba";

	@Test(groups = {AllTestSuit.test_group_webservices})
	public void testAllWindowsWS() {
		WSTester tester = new WSTester(SERVER,
				new PrintWriter(System.out, true));
		try {
			assertTrue(tester.checkService(Services.AAConWS));
			assertTrue(tester.checkService(Services.JronnWS));
			assertTrue(tester.checkService(Services.ClustalWS));
			assertTrue(tester.checkService(Services.MuscleWS));
			assertTrue(tester.checkService(Services.ClustalOWS));
			assertTrue(tester.checkService(Services.IUPredWS));
		} catch (ConnectException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (WebServiceException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}

		// Will throw UnsupportedRuntimeException on windows
		// ws = Jws2Client.connect(SERVER, Services.MafftWS);
		// assertTrue(tester.checkService(ws));

	}

	@Test(groups = {AllTestSuit.test_group_webservices})
	public void testAllWS() {
		WSTester tester = new WSTester(SERVER, new PrintWriter(System.out));
		try {
			for (Services service : Services.values()) {
				assertTrue(tester.checkService(service));
			}
		} catch (ConnectException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (WebServiceException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

}
