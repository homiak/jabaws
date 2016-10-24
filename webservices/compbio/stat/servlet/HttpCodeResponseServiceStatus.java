package compbio.stat.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import compbio.util.Util;
import compbio.ws.client.Services;
import compbio.ws.client.WSTester;

/**
 * This servlet checks the webservice coming as pathinfo and returns http code
 * as a reply. If the web service functions correctly then OK (200) is returned.
 * otherwise Service unavailable (503). When the webservice is not recognised
 * unknown service error (400) is sent as a response.
 * 
 * If no pathinfo is specified all web services are checked and OK is returned
 * only if all webservices are functioning.
 * 
 * @author pvtroshin
 * 
 */
public class HttpCodeResponseServiceStatus extends HttpServlet {

	private final static Logger log = Logger
			.getLogger(HttpCodeResponseServiceStatus.class);

	@Override
	protected void doHead(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		doGet(req, resp);
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		Services[] servicesToTest = null;
		String path = req.getPathInfo();
		if (!Util.isEmpty(path)) {
			// test a particular service
			// e.g. /DisEMBLWS
			path = path.trim().substring(1);
			Services service = Services.getService(path);
			if (service == null) {
				resp.sendError(
						HttpServletResponse.SC_BAD_REQUEST,
						"Unknown service name: " + path
								+ "\n Known services are: "
								+ Arrays.toString(Services.values())); // bad
				// request
				return;
			}
			servicesToTest = new Services[]{service};
		} else {
			// assume all services require testing
			servicesToTest = Services.values();
		}

		// find out the application path to give it to the tester
		// e.g.
		// http://localhost:8080/jabaws/HttpCodeResponseServiceStatus/DisEMBLWS
		// convert it to http://localhost:8080/jabaws
		StringBuffer jabawspath = req.getRequestURL();
		jabawspath = jabawspath.delete(
				jabawspath.indexOf("/"
						+ HttpCodeResponseServiceStatus.class.getSimpleName()),
				jabawspath.length());
		boolean operating = false;
		// save for the future use
		Services failedService = null;
		String failMessage = "";
		for (Services serv : servicesToTest) {
			String serverPath = jabawspath.toString();
			StringWriter testres = new StringWriter();
			PrintWriter writer = new PrintWriter(testres, true);
			// test
			try {
				WSTester tester = new WSTester(serverPath, writer);
				operating = tester.checkService(serv);
			} catch (Exception e) {
				log.info(e, e.getCause());
				writer.println("Fails to connect to a web service: " + serv
						+ " With " + e.getLocalizedMessage() + "\nDetails: ");
				// if one service fails, everything fails
				operating = false;
			} finally {
				writer.close();
			}
			if (!operating) {
				failedService = serv;
				failMessage = testres.toString();
				break;
			}
		}

		if (operating) {
			resp.setStatus(HttpServletResponse.SC_OK); // OK
		} else {
			// Internal server error - Service Unavailable
			resp.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE,
					"Service: " + failedService
							+ "\n is not available! Error message:\n "
							+ failMessage);

		}

	}
}
