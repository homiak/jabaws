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

import java.io.IOException;
import java.util.List;

import compbio.util.FileUtil;

public class FastaGeneratorManager {

	public static void main(String[] args) throws IOException {
		FastaSequenceGenerator fgen = new FastaSequenceGenerator(
				FastaSequenceGenerator.SeqType.PROTEIN, 50);

		List<FastaSequence> fslist = fgen.generateFasta(400);
		writeToFile(fslist, "SmallProtein.fasta");

		fgen = new FastaSequenceGenerator(
				FastaSequenceGenerator.SeqType.PROTEIN, 200);

		fslist = fgen.generateFasta(500);
		writeToFile(fslist, "200x500Protein.fasta");

		fgen = new FastaSequenceGenerator(FastaSequenceGenerator.SeqType.DNA,
				1000);
		fslist = fgen.generateFasta(3000);
		writeToFile(fslist, "1000x3000Dna.fasta");

		fgen = new FastaSequenceGenerator(FastaSequenceGenerator.SeqType.DNA,
				50000);
		fslist = fgen.generateFasta(300);
		writeToFile(fslist, "50000x300Dna.fasta");
		System.out.println("Done");
	}

	static void writeToFile(List<FastaSequence> fastalist, String filepath)
			throws IOException {
		StringBuilder sb = new StringBuilder();
		for (FastaSequence fs : fastalist) {
			sb.append(fs.getOnelineFasta() + "\n");
		}
		FileUtil.writeToFile(sb.toString(), filepath);
	}
}
