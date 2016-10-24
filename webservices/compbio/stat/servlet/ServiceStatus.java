package compbio.stat.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import compbio.ws.client.Services;
import compbio.ws.client.WSTester;

/**
 * Use cases:
 * <dl>
 * <li>Test web services and display results on the web page</li>
 * </dl>
 * 
 * @author pvtroshin
 * 
 */
public class ServiceStatus extends HttpServlet {

	private final static Logger log = Logger.getLogger(ServiceStatus.class);

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		StringBuffer jabawspath = req.getRequestURL();
		jabawspath = jabawspath.delete(jabawspath.lastIndexOf("/"),
				jabawspath.length());
		String serverPath = jabawspath.toString();

		List<ServiceTestResult> testResults = new ArrayList<ServiceTestResult>();

		for (Services service : Services.values()) {
			StringWriter testres = new StringWriter();
			PrintWriter writer = new PrintWriter(testres, true);
			WSTester tester = new WSTester(serverPath, writer);
			ServiceTestResult result = new ServiceTestResult(service);
			try {
				result.failed = tester.checkService(service);
			} catch (Exception e) {
				log.info(e, e.getCause());
				writer.println("Fails to connect to a web service: " + service
						+ " With " + e.getLocalizedMessage() + "\nDetails: ");
				e.printStackTrace(writer);
			} finally {
				writer.close();
			}
			result.details = testres.toString();
			testResults.add(result);
		}
		req.setAttribute("results", testResults);
		RequestDispatcher rd = req
				.getRequestDispatcher("statpages/ServicesStatus.jsp");
		rd.forward(req, resp);
	}

}
