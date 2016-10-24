/* Copyright (c) 2011 Peter Troshin
 *  
 *  JAva Bioinformatics Analysis Web Services (JABAWS) @version: 2.0     
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
package compbio.ws.server;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Resource;
import javax.jws.WebService;
import javax.servlet.http.HttpServletRequest;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;

import org.apache.log4j.Logger;

import compbio.data.msa.Category;
import compbio.data.msa.JABAService;
import compbio.ws.client.Services;
import compbio.ws.client.WSTester;

/**
 * JABAWS services registry
 * 
 * @author pvtroshin
 * 
 */
@WebService(endpointInterface = "compbio.data.msa.RegistryWS", targetNamespace = JABAService.V2_SERVICE_NAMESPACE, serviceName = "RegistryWS")
public class RegistryWS implements compbio.data.msa.RegistryWS, JABAService {

	// Ask for resource injection
	@Resource
	WebServiceContext wsContext;

	private static Logger log = Logger.getLogger(RegistryWS.class);

	/**
	 * Stores tested and passed (the test) services and their testing time
	 */
	private final static Map<Services, Date> operating = new ConcurrentHashMap<Services, Date>();

	/**
	 * Indicate whether the services were tested at all
	 */
	private static boolean allTested = false;

	@Override
	public Set<Services> getSupportedServices() {
		init();
		return operating.keySet();
	}

	private void init() {
		// Do not allow tests to run concurrently
		if (timeToTest()) {
			synchronized (operating) {
				if (timeToTest()) {
					testAllServices();
					allTested = true;
				}
			}
		}
	}

	private boolean timeToTest() {
		if (!allTested) {
			return true;
		}
		// 24 h
		if (getLongestUntestedServiceTime() > 3600 * 24) {
			return true;
		}
		return false;
	}

	/**
	 * Return time in seconds for the test for the oldest unchecked service
	 * 
	 * @return
	 */
	private int getLongestUntestedServiceTime() {
		int timePassed = 0;
		for (Services serv : operating.keySet()) {
			int lasttimepassed = getLastTested(serv);
			if (timePassed < lasttimepassed) {
				timePassed = lasttimepassed;
			}
		}
		return timePassed;
	}

	@Override
	public int getLastTested(Services service) {
		Date testedOn = getLastTestedOn(service);
		if (testedOn != null) {
			return (int) ((System.currentTimeMillis() - testedOn.getTime()) / 1000);
		}
		return 0;
	}

	/**
	 * Can potentially return null if the service has not been tested yet.
	 */
	@Override
	public Date getLastTestedOn(Services service) {
		if (operating.containsKey(service)) {
			return operating.get(service);
		}
		return null;
	}

	/**
	 * TODO improve reporting. stop testing service on unsupported runtime env
	 * exception
	 */
	@Override
	public String testAllServices() {
		Writer testlog = new StringWriter();
		PrintWriter writer = new PrintWriter(testlog, true);
		WSTester tester = new WSTester(getServicePath(), writer);
		// This is done deliberately to prevent malicious user from overloading
		// the server
		synchronized (operating) {
			for (Services service : Services.values()) {
				try {
					if (tester.checkService(service)) {
						operating.put(service, new Date());
					}
				} catch (Exception e) {
					log.info(e, e.getCause());
					writer.println("Fails to connect to a web service: "
							+ service + " With " + e.getLocalizedMessage()
							+ "\nDetails: ");
					e.printStackTrace(writer);
				}
			}
		}
		writer.close();
		return testlog.toString();
	}

	private String getServicePath() {
		assert wsContext != null : "WS context injection failed!";
		MessageContext msContext = wsContext.getMessageContext();
		HttpServletRequest request = (HttpServletRequest) msContext
				.get(MessageContext.SERVLET_REQUEST);

		StringBuffer server = request.getRequestURL();
		server = server.delete(server.lastIndexOf("/"), server.length());
		return server.toString();
	}

	@Override
	public String testService(Services service) {
		String server = getServicePath();
		Writer testlog = new StringWriter();
		PrintWriter writer = new PrintWriter(testlog, true);
		WSTester tester = new WSTester(server, writer);
		try {
			synchronized (operating) {
				boolean succeed = tester.checkService(service);
				if (succeed) {
					operating.put(service, new Date());
				}
			}
		} catch (Exception e) {
			log.info(e, e.getCause());
			writer.println("Fails to connect to a web service: " + service
					+ " With " + e.getLocalizedMessage() + "\nDetails: ");
			e.printStackTrace(writer);
		} finally {
			writer.close();
		}
		return testlog.toString();
	}
	@Override
	public boolean isOperating(Services service) {
		init();
		return operating.containsKey(service);
	}

	@Override
	public String getServiceDescription(Services service) {
		return service.getServiceInfo();
	}

	@Override
	public Set<Category> getServiceCategories() {
		return Category.getCategories();
	}

}
