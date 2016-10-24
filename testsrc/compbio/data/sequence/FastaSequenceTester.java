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

import org.testng.annotations.Test;

public class FastaSequenceTester {

	@Test()
	public void testGetFormattedFasta() {
		FastaSequence fs = new FastaSequence(
				"test",
				"kjashf asjkdfhjkahsdfkadf fewyweyrfhjajfasdjkfkjhasdjkfaskhdfjhasdjkf "
						+ "hdflhdghuetirwhglhasdghfjkhdfs gdsfkjghsdjfgjhdsfglkhskdjfgjhjsdkfgkhsdjkfghsdkjfgh "
						+ "sdfjglkjhsdflkjg sdfgklhsdkfgdfkjghjkshgdflsdfgjkshdfghksdjfgkjhsdfgjkh");
		assertEquals(219, fs.getSequence().length());
		assertEquals(11, fs.getFormatedSequence(20).split("\n").length);
		assertEquals(3, fs.getFormatedSequence(80).split("\n").length);
		fs = new FastaSequence("test", "kjashf f ");
		assertEquals(1, fs.getFormatedSequence(80).split("\n").length);
		assertEquals(7, fs.getFormatedSequence(1).split("\n").length);
	}
}
