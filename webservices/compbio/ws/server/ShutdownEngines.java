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

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.log4j.Logger;

import compbio.engine.local.ExecutableWrapper;
import compbio.engine.local.LocalExecutorService;

/**
 * Switch off engines if JABAWS web application is undeployed, or web server is
 * shutdown
 * 
 * @author Peter Troshin
 * @version 1.0
 */
public class ShutdownEngines implements ServletContextListener {

	private final Logger log = Logger.getLogger(ShutdownEngines.class);

	@Override
	public void contextDestroyed(ServletContextEvent ignored) {
		// Shutdown local engine
		log.info("JABAWS context is destroyed. Shutting down engines...");
		LocalExecutorService.shutDown();
		log.info("Local engine is shutdown OK");
		ExecutableWrapper.shutdownService();
		log.info("Individual executables stream engine is shutdown OK");
	}

	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		// Do nothing
	}

}
