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

package compbio.engine.conf;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.log4j.Logger;

import compbio.util.PropertyHelper;
import compbio.util.Util;

public final class PropertyHelperManager {

	private static Logger log = Logger.getLogger(PropertyHelperManager.class);
	private static PropertyHelper ph = null;
	public static final String confDir = "conf" + File.separator;

	/**
	 * Ways to fix path problem: 1) find a path to WEB-INF directory based on
	 * the path to a known class. Then prepend this absolute path to the rest of
	 * paths pros: no input from user cons: relocation of the source may cause
	 * problems 2) Require users to add configuration directories to the class
	 * path and then load entries from it. pros: cons: Many paths needs to be
	 * added. Put significant burden on the user. Hard to tell web appl server
	 * to add these entries to its class path. 3) Ask for project source
	 * directory explicitly in the configuration. pros cons: similar to 1, but
	 * this initial configuration file must reside in well known location! Why
	 * ask users what can be found automatically? 4) Have everything in the
	 * location already in class path for tomcat. cons: only classes and
	 * lib/*.jar are added, eclipse will remove non classses from classes dir.
	 * 
	 * Try 1 - succeed.
	 * 
	 * @return an instance
	 */
	public static PropertyHelper getPropertyHelper() {
		if (ph == null) {
			try {
				File locEngineProp = getResourceFromClasspath(confDir
						+ "Engine.local.properties");
				File clustEngineProp = getResourceFromClasspath(confDir
						+ "Engine.cluster.properties");
				File execProp = getResourceFromClasspath(confDir
						+ "Executable.properties");
				File gaProp = getResourceFromClasspath(confDir
						+ "GA.properties");
				ph = new PropertyHelper(locEngineProp, clustEngineProp,
						execProp, gaProp);
			} catch (IOException e) {
				log.warn(
						"Cannot read property files! Reason: "
								+ e.getLocalizedMessage(), e.getCause());
			}
		}
		return ph;
	}

	static File getResourceFromClasspath(String resourceName) {
		assert !Util.isEmpty(resourceName);
		String locPath = getLocalPath();
		File prop = new File(locPath + resourceName);
		if (!prop.exists()) {
			log.warn("Could not find a resource " + resourceName
					+ " in the classpath!");
		}
		return prop;
	}

	/**
	 * Method return the absolute path to the project root directory. It assumes
	 * the following structure of the project project root conf settings
	 * binaries WEB-INF classes compbio engine conf If the structure changes it
	 * must be reflected in this method
	 * 
	 * @return the local path
	 * @throws RuntimeException
	 *             if cannot determine the local path
	 */
	public static String getLocalPath() {
		String clname = PropertyHelperManager.class.getSimpleName();
		URL url = PropertyHelperManager.class.getResource(clname + ".class");
		File f = null;
		try {
			f = new File(url.toURI());
			// Iterate up the hierarchy to find a root project directory
			for (int i = 0; i < 6; i++) {
				f = f.getParentFile();
			}
		} catch (URISyntaxException e) {
			String message = "Could not find resources path! Problems locating PropertyHelperManager class! "
					+ e.getLocalizedMessage();
			log.error(message, e.getCause());
			throw new RuntimeException(message, e.getCause());
		} catch (IllegalArgumentException e) {
			// Classes are in the jar file, using different method to determine
			// the path new File(INCORRECT URL) throws it
			log.debug(
					"It looks like classes are in the jar file. "
							+ "Attempting a different method to determinine the path to the resources "
							+ e.getLocalizedMessage(), e.getCause());
			try {
				f = new File(PropertyHelperManager.class.getProtectionDomain()
						.getCodeSource().getLocation().toURI().getPath());

				// Iterate up the hierarchy to find a root project directory
				// This time there is not need to walk up all class packages
				// WEB_APPL_NAME\WEB-INF\lib\JAR-FILE-NAME
				// jws2-1.0\WEB-INF\lib\full-jws2-1.0.jar
				for (int i = 0; i < 3; i++) {
					f = f.getParentFile();
				}
			} catch (URISyntaxException e1) {
				log.error(
						"Could not find resources path! "
								+ e1.getLocalizedMessage(), e1.getCause());
				throw new RuntimeException("Could not find resources path! ",
						e1.getCause());
			}
		}
		log.debug("Project directory is: " + f.getAbsolutePath());
		return f.getAbsolutePath() + File.separator;
	}
}
