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
import static org.testng.AssertJUnit.assertTrue;

import java.util.List;

import org.testng.annotations.Test;

public class FastaSequenceGeneratorTester {

	@Test()
	public void testProteinSequenceGeneration() {
		FastaSequenceGenerator fs = new FastaSequenceGenerator(
				FastaSequenceGenerator.SeqType.PROTEIN, 100);
		assertEquals(100, fs.generateFasta(100).size());

		fs = new FastaSequenceGenerator(FastaSequenceGenerator.SeqType.DNA, 50);
		List<FastaSequence> flist = fs.generateFasta(100);
		assertEquals(50, flist.size());
		for (FastaSequence s : flist) {
			assertTrue(s.getLength() >= 50);
		}

	}
}
