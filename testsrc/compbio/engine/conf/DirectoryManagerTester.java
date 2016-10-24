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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.testng.annotations.Test;

import compbio.runner.msa.ClustalW;
import compbio.util.SysPrefs;

public class DirectoryManagerTester {

	@Test(invocationCount = 4, threadPoolSize = 4, sequential = false)
	public void testGetDirectory() {
		Set<Long> set = new HashSet<Long>();
		for (int i = 0; i < 20000; i++) {
			long number = DirectoryManager.getNonRepeatableNumber();
			assertTrue(set.add(number));
			// Cannot rely on the length is the returned long, as precision is
			// implementation dependent
			// see testNanoTime method below
			// assertEquals(Long.toString(number).length(), 17);
		}
		String name = DirectoryManager.getTaskDirectory(ClustalW.class);
		assertTrue(name.startsWith(ClustalW.class.getSimpleName()));
	}

	/*
	 * In fact the precision of System.nanoTime() varies depending on the JVM
	 * Sun 1.6.0_17 JVM gives the same precision on linux x64 as on windows x32
	 * but openjdk 1.6.x gives more digits on linux than sun JVM It looks like
	 * the precision is desided at runtime, so the number of digits returned is
	 * unknown
	 */
	@Test(enabled = false)
	public void testNanoTime() {
		if (SysPrefs.isWindows) {
			assertEquals(Long.toString(System.nanoTime()).length(), 16);
		} else {
			// This is not always true for Linux
			assertEquals(Long.toString(System.nanoTime()).length(), 14);
		}
	}

}
