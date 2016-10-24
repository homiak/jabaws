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
package compbio.data.sequence;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Scanner;
import java.util.regex.MatchResult;

import compbio.util.Util;

/**
 * Reads files with FASTA formatted sequences. All the information in the FASTA
 * header is preserved including trailing white spaces. All the white spaces are
 * removed from the sequence.
 * 
 * Examples of the correct input:
 * 
 * <pre>
 * 
 * >zedpshvyzg
 * GCQDKNNIAELNEIMGTTRSPSDWQHMKGASPRAEIGLTGKKDSWWRHCCSKEFNKTPPPIHPDMKRWGWMWNRENFEKFLIDNFLNPPCPRLMLTKGTWWRHEDLCHEIFWSTLRWLCLGNQSFSAMIWGHLCECHRMIWWESNEHMFWLKFRRALKKMNSNGPCMGPDNREWMITNRMGKEFCGPAFAGDCQSCWRKCHKTNKICFNEKKGTPTKIDHEQKDIMDILKDIDNHRNWKQCQLWLLTSKSTDQESTTMLTWSTWRDFFIIIKQPFDHKCRGALDANGDFQIAAELKWPAPMIILRQNQKTMHDKSCHHFFTNRCPLMHTTRANDKQCSWHTRKQFICQQDFTTWQHRPDTHRILPSWCMSTRRKNHIKNTPALAFSTCEMGDLPNGWAPGTIILQRQFTQAIKLPQETTGWPRCDPKFDHWNMSKWLRQLLGRDDEMIPPQCD
 * 
 * >xovkactesa
 * CPLSKWWNRRAFLSHTANHWMILMTWEGPHDGESKMRIAMMKWSPCKPTMSHFRCGLDAWAEPIRQIACESTFRM
 * FCTTPRPIHKLTEMWGHMNGWTGAFCRQLECEWMMPPRHPHPCTSTFNNNKKRLIGQIPNEGKQLFINFQKPQHG
 * FSESDIWIWKDNPTAWHEGLTIAGIGDGQHCWNWMPMPWSGAPTSNALIEFWTWLGMIGTRCKTQGMWWDAMNHH
 * DQFELSANAHIAAHHMEKKMILKPDDRNLGDDTWMPPGKIWMRMFAKNTNACWPEGCRDDNEEDDCGTHNLHRMC
 * 
 * >ntazzewyvv
 * CGCKIF D D NMKDNNRHG TDIKKHGFMH IRHPE KRDDC FDNHCIMPKHRRWGLWD
 * EASINM	AQQWRSLPPSRIMKLNG	HGCDCMHSHMEAD	DTKQSGIKGTFWNG	HDAQWLCRWG	
 * EFITEA	WWGRWGAITFFHAH	ENKNEIQECSDQNLKE	SRTTCEIID   TCHLFTRHLDGW 
 *   RCEKCQANATHMTW ACTKSCAEQW  FCAKELMMN    
 *   W        KQMGWRCKIFRKLFRDNCWID  FELPWWPICFCCKGLSTKSHSAHDGDQCRRW    WPDCARDWLGPGIRGEF   
 *   FCTHICQQLQRNFWCGCFRWNIEKRMFEIFDDNMAAHWKKCMHFKFLIRIHRHGPITMKMTWCRSGCCFGKTRRLPDSSFISAFLDPKHHRDGSGMMMWSSEMRSCAIPDPQQAWNQGKWIGQIKDWNICFAWPIRENQQCWATPHEMPSGFHFILEKWDALAHPHMHIRQKKCWAWAFLSLMSSTHSDMATFQWAIPGHNIWSNWDNIICGWPRI
 * 
 *    > 12 d t y wi 		k	jbke  	
 *   KLSHHDCD
 *    N
 *     H
 *     HSKCTEPHCGNSHQMLHRDP
 *     CCDQCQSWEAENWCASMRKAILF
 * 
 * </pre>
 * 
 * @author Peter Troshin
 * @version 1.0 April 2011
 * 
 */
public class FastaReader implements Iterator<FastaSequence> {

	private final Scanner input;
	/**
	 * Delimiter for the scanner
	 */
	private final String DELIM=">";
	/**
	 * Header data can contain non-ASCII symbols and read in UTF8
	 * 
	 * @param inputFile
	 *            the file containing the list of FASTA formatted sequences to
	 *            read from
	 * @throws FileNotFoundException
	 *             if the input file is not found
	 * @throws IllegalStateException
	 *             if the close method was called on this instance
	 * 
	 */
	public FastaReader(final String inputFile) throws FileNotFoundException {
		input = new Scanner(new File(inputFile), "UTF8");
		input.useDelimiter(DELIM);
		Runtime.getRuntime().addShutdownHook(new Thread() {

			@Override
			public void run() {
				if (input != null) {
					input.close();
				}
			}
		});
	}

	/**
	 * This class will not close the incoming stream! So the client should do
	 * so.
	 * 
	 * @param inputStream
	 * @throws FileNotFoundException
	 */
	public FastaReader(final InputStream inputStream)
			throws FileNotFoundException {
		input = new Scanner(inputStream);
		input.useDelimiter(DELIM);
	}
	/**
	 * {@inheritDoc}
	 * 
	 * @throws IllegalStateException
	 *             if the close method was called on this instance
	 */
	@Override
	public boolean hasNext() {
		return input.hasNext();
	}

	/**
	 * Reads the next FastaSequence from the input
	 * 
	 * @throws AssertionError
	 *             if the header or the sequence is missing
	 * @throws IllegalStateException
	 *             if the close method was called on this instance
	 *             @throws MismatchException - if there were no more FastaSequence's.
	 */
	@Override
	public FastaSequence next() {
		String fastaHeader=input.next();
		while (fastaHeader.indexOf("\n")<0 && input.hasNext())
		{
			fastaHeader = fastaHeader.concat(">");
			fastaHeader = fastaHeader.concat(input.next());
		}
		return FastaReader.toFastaSequence(fastaHeader);
	}

	/**
	 * Not implemented
	 */
	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Call this method to close the connection to the input file if you want to
	 * free up the resources. The connection will be closed on the JVM shutdown
	 * if this method was not called explicitly. No further reading on this
	 * instance of the FastaReader will be possible after calling this method.
	 */
	public void close() {
		input.close();
	}

	private static FastaSequence toFastaSequence(final String singleFastaEntry) {

		assert !Util.isEmpty(singleFastaEntry) : "Empty String where FASTA sequence is expected!";

		int nlineidx = singleFastaEntry.indexOf("\n");
		if (nlineidx < 0) {
			throw new AssertionError(
					"The FASTA sequence must contain the header information"
							+ " separated by the new line from the sequence. Given sequence does not appear to "
							+ "contain the header! Given data:\n "
							+ singleFastaEntry);
		}
		String header = singleFastaEntry.substring(0, nlineidx);

		// Get rid of the new line chars (should cover common cases)
		header = header.replaceAll("\r", "");

		String sequence = singleFastaEntry.substring(nlineidx);

		if (Util.isEmpty(sequence)) {
			throw new AssertionError(
					"Empty sequences are not allowed! Please make sure the "
							+ " data is in the FASTA format! Given data:\n "
							+ singleFastaEntry);
		}
		return new FastaSequence(header, sequence);
	}
}
