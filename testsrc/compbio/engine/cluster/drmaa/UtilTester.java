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

package compbio.engine.cluster.drmaa;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.fail;

import java.text.ParseException;

import org.testng.annotations.Test;

import compbio.engine.cluster.drmaa.ClusterUtil;

public class UtilTester {

	@Test
	public void testParser() {
		try {
			Number n = ClusterUtil.CLUSTER_STAT_IN_SEC.parse("11.0000");
			assertNotNull(n);
			int t = n.intValue();
			assertEquals(11, t);
			n = ClusterUtil.CLUSTER_STAT_IN_SEC.parse("11.2300");
			assertNotNull(n);
			t = n.intValue();
			assertEquals(11, t);
			float f = n.floatValue();
			assertEquals(11.23f, f);
			n = ClusterUtil.CLUSTER_STAT_IN_SEC.parse("0.0310");
			assertNotNull(n);
			f = n.floatValue();
			assertEquals(0.031f, f);
		} catch (ParseException e) {
			e.printStackTrace();
			fail("Parsing failed: " + e.getMessage());
		}

	}
}
