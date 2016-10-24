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

package compbio.engine;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.fail;

import java.util.concurrent.TimeUnit;

import org.testng.annotations.Test;

import compbio.metadata.AllTestSuit;

public class PulledFileCacheTester {

	@Test
	public void test() {
		try {
			FilePuller fp1 = FilePuller.newFilePuller(
					AllTestSuit.TEST_DATA_PATH_ABSOLUTE + "1", 256);
			fp1.setDelay(2, TimeUnit.SECONDS);

			FilePuller fp2 = FilePuller.newFilePuller(
					AllTestSuit.TEST_DATA_PATH_ABSOLUTE + "2", 256);
			fp2.setDelay(1, TimeUnit.SECONDS);

			FilePuller fp3 = FilePuller.newFilePuller(
					AllTestSuit.TEST_DATA_PATH_ABSOLUTE + "3", 256);
			fp3.setDelay(1, TimeUnit.SECONDS);

			FilePuller fp4 = FilePuller.newFilePuller(
					AllTestSuit.TEST_DATA_PATH_ABSOLUTE + "4", 256);
			fp4.setDelay(5, TimeUnit.SECONDS);

			// This only hold if tested in isolation thus clear is essential
			PulledFileCache.clear();
			assertEquals(PulledFileCache.getSize(), 0);
			PulledFileCache.put(fp1);
			PulledFileCache.put(fp2);
			PulledFileCache.put(fp3);
			PulledFileCache.put(fp4);
			assertEquals(PulledFileCache.getSize(), 4);
			Thread.sleep(1000);
			// sweep was not called yet
			assertEquals(PulledFileCache.getSize(), 4);
			// now sweep is called
			PulledFileCache.put(fp1);
			// fp1 and fp1 and fp4 only remains - copies are allowed this is
			// responsibility of the caller to ensure they are not there
			assertEquals(PulledFileCache.getSize(), 3);
			assertNotNull(PulledFileCache
					.get(AllTestSuit.TEST_DATA_PATH_ABSOLUTE + "1"));
			assertNotNull(PulledFileCache
					.get(AllTestSuit.TEST_DATA_PATH_ABSOLUTE + "4"));

			for (int i = 0; i < 4; i++) {
				Thread.sleep(1000);
				FilePuller fp = PulledFileCache
						.get(AllTestSuit.TEST_DATA_PATH_ABSOLUTE + "4");
				// This will update access time
				fp.isFileCreated();
			}
			// still fp1 and fp4 only remains

			assertEquals(PulledFileCache.getSize(), 3);
			PulledFileCache.put(FilePuller.newFilePuller(
					AllTestSuit.TEST_DATA_PATH_ABSOLUTE + "5", 256));
			// at this point only fp4 and 5 will remain
			assertEquals(PulledFileCache.getSize(), 2);
			assertNotNull(PulledFileCache
					.get(AllTestSuit.TEST_DATA_PATH_ABSOLUTE + "4"));
			assertNotNull(PulledFileCache
					.get(AllTestSuit.TEST_DATA_PATH_ABSOLUTE + "5"));

		} catch (InterruptedException e) {
			fail(e.getMessage());
		}
	}

}
