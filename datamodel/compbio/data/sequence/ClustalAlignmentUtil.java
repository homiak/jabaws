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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Logger;

/**
 * Tools to read and write clustal formated files
 * 
 * @author Petr Troshin based on jimp class
 * 
 * @version 1.0 September 2009
 * 
 */
public final class ClustalAlignmentUtil {

	private static final Logger log = Logger
			.getLogger(ClustalAlignmentUtil.class.getCanonicalName());

	/**
	 * Dash char to be used as gap char in the alignments
	 */
	public static final char gapchar = '-';

	/*
	 * Number of spaces separating the name and the sequence
	 */
	private static final String spacer = "      "; // 6 space characters
	/*
	 * name length limit is 30 characters! 2.0.7 - 2.0.12 clustalw /* if name is
	 * longer than that it gets trimmed in the end
	 */
	private static final int maxNameLength = 30; // Maximum name length
	/*
	 * If all sequences names in the alignment is shorter than
	 * minNameHolderLength than spaces are added to complete the name up to
	 * minNameHolderLength
	 */
	private static final int minNameHolderLength = 10; // Minimum number of

	// TODO check whether clustal still loads data if length is 60!
	private static final int oneLineAlignmentLength = 60; // this could in fact

	// be 50

	// for long names ~30 chars

	/**
	 * Read Clustal formatted alignment. Limitations: Does not read consensus
	 * 
	 * Sequence names as well as the sequences are not guaranteed to be unique!
	 * 
	 * @throws {@link IOException}
	 * @throws {@link UnknownFileFormatException}
	 */
	public static Alignment readClustalFile(InputStream instream)
			throws IOException, UnknownFileFormatException {

		boolean flag = false;

		List<String> headers = new ArrayList<String>();
		Map<String, StringBuffer> seqhash = new HashMap<String, StringBuffer>();
		FastaSequence[] seqs = null;

		String line;

		BufferedReader breader = new BufferedReader(new InputStreamReader(
				instream));
		while ((line = breader.readLine()) != null) {
			if (line.indexOf(" ") != 0) {
				java.util.StringTokenizer str = new StringTokenizer(line, " ");
				String id = "";

				if (str.hasMoreTokens()) {
					id = str.nextToken();
					// PROBCONS output clustal formatted file with not mention
					// of CLUSTAL (:-))
					if (id.equals("CLUSTAL") || id.equals("PROBCONS")) {
						flag = true;
					} else {
						if (flag) {
							StringBuffer tempseq;
							if (seqhash.containsKey(id)) {
								tempseq = seqhash.get(id);
							} else {
								tempseq = new StringBuffer();
								seqhash.put(id, tempseq);
							}

							if (!(headers.contains(id))) {
								headers.add(id);
							}

							tempseq.append(str.nextToken());
						}
					}
				}
			}
		}
		breader.close();

		// TODO improve this bit
		if (flag) {

			// Add sequences to the hash
			seqs = new FastaSequence[headers.size()];
			for (int i = 0; i < headers.size(); i++) {
				if (seqhash.get(headers.get(i)) != null) {

					FastaSequence newSeq = new FastaSequence(headers.get(i),
							seqhash.get(headers.get(i)).toString());

					seqs[i] = newSeq;

				} else {
					// should not happened
					throw new AssertionError(
							"Bizarreness! Can't find sequence for "
									+ headers.get(i));
				}
			}
		}
		if (seqs == null || seqs.length == 0) {
			throw new UnknownFileFormatException(
					"Input does not appear to be a clustal file! ");
		}
		return new Alignment(Arrays.asList(seqs), new AlignmentMetadata(
				Program.CLUSTAL, gapchar));
	}

	/**
	 * Please note this method closes the input stream provided as a parameter
	 * 
	 * @param input
	 * @return true if the file is recognised as Clustal formatted alignment,
	 *         false otherwise
	 */
	public static boolean isValidClustalFile(InputStream input) {
		if (input == null) {
			throw new NullPointerException("Input is expected!");
		}
		BufferedReader breader = new BufferedReader(
				new InputStreamReader(input));
		try {
			if (input.available() < 10) {
				return false;
			}
			// read first 10 lines to find "Clustal"
			for (int i = 0; i < 10; i++) {
				String line = breader.readLine();
				if (line != null) {
					line = line.toUpperCase().trim();
					if (line.contains("CLUSTAL") || line.contains("PROBCONS")) {
						return true;
					}
				}
			}

			breader.close();
		} catch (IOException e) {
			log.severe("Could not read from the stream! "
					+ e.getLocalizedMessage() + e.getCause());
		} finally {
			SequenceUtil.closeSilently(log, breader);
		}
		return false;
	}

	/**
	 * Write Clustal formatted alignment Limitations: does not record the
	 * consensus. Potential bug - records 60 chars length alignment where
	 * Clustal would have recorded 50 chars.
	 * 
	 * @param out
	 * 
	 * @param alignment
	 * @throws IOException
	 */
	public static void writeClustalAlignment(final Writer out,
			final Alignment alignment) throws IOException {
		List<FastaSequence> seqs = alignment.getSequences();

		out.write("CLUSTAL\n\n\n");

		int max = 0;
		int maxidLength = 0;

		int i = 0;
		// Find the longest sequence name
		for (FastaSequence fs : seqs) {
			String tmp = fs.getId();

			if (fs.getSequence().length() > max) {
				max = fs.getSequence().length();
			}
			if (tmp.length() > maxidLength) {
				maxidLength = tmp.length();
			}
			i++;
		}
		if (maxidLength < minNameHolderLength) {
			maxidLength = minNameHolderLength;
		}
		if (maxidLength > maxNameLength) {
			maxidLength = 30; // the rest will be trimmed
		}

		int oneLineAlignmentLength = 60;
		int nochunks = max / oneLineAlignmentLength + 1;

		for (i = 0; i < nochunks; i++) {
			int j = 0;
			for (FastaSequence fs : seqs) {

				String name = fs.getId();
				// display at most 30 characters in the name, keep the names
				// 6 spaces away from the alignment for longest sequence names,
				// and more than this for shorter names
				out.write(String.format(
						"%-" + maxidLength + "s" + spacer,
						(name.length() > maxNameLength ? name.substring(0,
								maxidLength) : name)));
				int start = i * oneLineAlignmentLength;
				int end = start + oneLineAlignmentLength;

				if (end < fs.getSequence().length()
						&& start < fs.getSequence().length()) {
					out.write(fs.getSequence().substring(start, end) + "\n");
				} else {
					if (start < fs.getSequence().length()) {
						out.write(fs.getSequence().substring(start) + "\n");
					}
				}
				j++;
			}
			out.write("\n");
		}
		try {
			out.close();
		} finally {
			SequenceUtil.closeSilently(log, out);
		}
	}

	public static Alignment readClustalFile(File file)
			throws UnknownFileFormatException, IOException {
		if (file == null) {
			throw new NullPointerException("File is expected!");
		}
		FileInputStream fio = new FileInputStream(file);
		Alignment seqAl = ClustalAlignmentUtil.readClustalFile(fio);
		try {
			fio.close();
		} finally {
			SequenceUtil.closeSilently(log, fio);
		}
		return seqAl;
	}
}
