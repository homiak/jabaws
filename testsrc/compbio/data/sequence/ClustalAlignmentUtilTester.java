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

package compbio.data.sequence;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import org.testng.annotations.Test;

import compbio.metadata.AllTestSuit;

public class ClustalAlignmentUtilTester {

	@Test()
	public void testReadClustalFile() {
		try {
			readWriteClustal("TO1381.aln");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	static void readWriteClustal(String fname) throws IOException,
			UnknownFileFormatException {
		FileInputStream fio = new FileInputStream(AllTestSuit.TEST_DATA_PATH
				+ fname);
		Alignment seqAl = ClustalAlignmentUtil.readClustalFile(fio);
		assertTrue(seqAl != null);
		assertTrue(seqAl.getSize() == 3);
		assertNotNull(seqAl.getSequences());
		assertEquals(3, seqAl.getSequences().size());

		// Now try to write the alignment read
		Writer os = new FileWriter(AllTestSuit.TEST_DATA_PATH + fname
				+ ".written");
		ClustalAlignmentUtil.writeClustalAlignment(os, seqAl);
		fio.close();
		os.close();

		fio = new FileInputStream(AllTestSuit.TEST_DATA_PATH + fname
				+ ".written");
		Alignment readseqs = ClustalAlignmentUtil.readClustalFile(fio);
		assertTrue(readseqs != null);
		assertTrue(readseqs.getSize() == 3);
		fio.close();
	}

	@Test()
	public void testReadClustalFileShortNames() {
		try {
			readWriteClustal("TO1381s.aln");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	@Test()
	public void testReadClustalFileLongNames() {
		try {
			readWriteClustal("TO1381L.aln");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

}
