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

package compbio.engine.client;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import org.testng.annotations.Test;

import compbio.engine.client.PathValidator;
import compbio.runner.msa.ClustalW;
import compbio.util.SysPrefs;

public class PathValidatorTester {

	@Test
	public void testIsAbsolutePath() {

		if (SysPrefs.isWindows) {
			/*
			 * slash(\) has to be prefixed with another slash to produce a
			 * single slash
			 */
			assertTrue(PathValidator.isAbsolutePath("d:\\temp"));
			assertFalse(PathValidator.isAbsolutePath("/home"));
		} else {
			assertFalse(PathValidator.isAbsolutePath("d:\\temp"));
			assertTrue(PathValidator.isAbsolutePath("/home"));
		}
		assertFalse(PathValidator.isAbsolutePath("home"));
	}

	@Test
	public void testGetClassPath() {
		String clname = ClustalW.class.getSimpleName();
		System.out.println("%" + clname);
		URL url = ClustalW.class.getResource(clname + ".class");
		URL url2 = ClassLoader.getSystemResource("Engine.local.properties");

		try {
			File f = new File(url.toURI());
			System.out.println("&" + f.getAbsolutePath());
			System.out.println("&" + f.getCanonicalPath());
			System.out.println("&" + f.getParent());
		} catch (URISyntaxException e) {
			e.printStackTrace();
			fail(e.getLocalizedMessage());
		} catch (IOException e) {
			e.printStackTrace();
			fail(e.getLocalizedMessage());
		}
		System.out.println("!!" + url2);
		System.out.println("!" + url.getFile());
		System.out.println("!" + System.getProperty("java.class.path", null));

	}
}
