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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import compbio.util.Util;

/**
 * Utility class for operations on sequences
 * 
 * @author Peter Troshin
 * @since 1.0
 * @version 2.0 June 2011
 */
public final class SequenceUtil {

	/**
	 * A whitespace character: [\t\n\x0B\f\r]
	 */
	public static final Pattern WHITE_SPACE = Pattern.compile("\\s");

	/**
	 * A digit
	 */
	public static final Pattern DIGIT = Pattern.compile("\\d");

	/**
	 * Non word
	 */
	public static final Pattern NONWORD = Pattern.compile("\\W");

	/**
	 * Valid Amino acids
	 */
	public static final Pattern AA = Pattern.compile("[ARNDCQEGHILKMFPSTWYV]+",
			Pattern.CASE_INSENSITIVE);

	/**
	 * inversion of AA pattern
	 */
	public static final Pattern NON_AA = Pattern.compile(
			"[^ARNDCQEGHILKMFPSTWYV]+", Pattern.CASE_INSENSITIVE);

	/**
	 * Same as AA pattern but with two additional letters - XU
	 */
	public static final Pattern AMBIGUOUS_AA = Pattern.compile(
			"[ARNDCQEGHILKMFPSTWYVXU]+", Pattern.CASE_INSENSITIVE);

	/**
	 * Nucleotides a, t, g, c, u
	 */
	public static final Pattern NUCLEOTIDE = Pattern.compile("[AGTCU]+",
			Pattern.CASE_INSENSITIVE);

	/**
	 * Ambiguous nucleotide
	 */
	public static final Pattern AMBIGUOUS_NUCLEOTIDE = Pattern.compile(
			"[AGTCRYMKSWHBVDNU]+", Pattern.CASE_INSENSITIVE); // see IUPAC
	/**
	 * Non nucleotide
	 */
	public static final Pattern NON_NUCLEOTIDE = Pattern.compile("[^AGTCU]+",
			Pattern.CASE_INSENSITIVE);

	private SequenceUtil() {
	} // utility class, no instantiation

	/**
	 * @return true is the sequence contains only letters a,c, t, g, u
	 */
	public static boolean isNucleotideSequence(final FastaSequence s) {
		return SequenceUtil.isNonAmbNucleotideSequence(s.getSequence());
	}

	/**
	 * Ambiguous DNA chars : AGTCRYMKSWHBVDN // differs from protein in only one
	 * (!) - B char
	 */
	public static boolean isNonAmbNucleotideSequence(String sequence) {
		sequence = SequenceUtil.cleanSequence(sequence);
		if (SequenceUtil.DIGIT.matcher(sequence).find()) {
			return false;
		}
		if (SequenceUtil.NON_NUCLEOTIDE.matcher(sequence).find()) {
			return false;
			/*
			 * System.out.format("I found the text starting at " +
			 * "index %d and ending at index %d.%n", nonDNAmatcher .start(),
			 * nonDNAmatcher.end());
			 */
		}
		final Matcher DNAmatcher = SequenceUtil.NUCLEOTIDE.matcher(sequence);
		return DNAmatcher.find();
	}

	/**
	 * Removes all whitespace chars in the sequence string
	 * 
	 * @param sequence
	 * @return cleaned up sequence
	 */
	public static String cleanSequence(String sequence) {
		assert sequence != null;
		final Matcher m = SequenceUtil.WHITE_SPACE.matcher(sequence);
		sequence = m.replaceAll("").toUpperCase();
		return sequence;
	}

	/**
	 * Removes all special characters and digits as well as whitespace chars
	 * from the sequence
	 * 
	 * @param sequence
	 * @return cleaned up sequence
	 */
	public static String deepCleanSequence(String sequence) {
		sequence = SequenceUtil.cleanSequence(sequence);
		sequence = SequenceUtil.DIGIT.matcher(sequence).replaceAll("");
		sequence = SequenceUtil.NONWORD.matcher(sequence).replaceAll("");
		final Pattern othernonSeqChars = Pattern.compile("[_-]+");
		sequence = othernonSeqChars.matcher(sequence).replaceAll("");
		return sequence;
	}

	/**
	 * Remove all non AA chars from the sequence
	 * 
	 * @param sequence
	 *            the sequence to clean
	 * @return cleaned sequence
	 */
	public static String cleanProteinSequence(String sequence) {
		return SequenceUtil.NON_AA.matcher(sequence).replaceAll("");
	}

	/**
	 * @param sequence
	 * @return true is the sequence is a protein sequence, false overwise
	 */
	public static boolean isProteinSequence(String sequence) {
		sequence = SequenceUtil.cleanSequence(sequence);
		if (SequenceUtil.isNonAmbNucleotideSequence(sequence)) {
			return false;
		}
		if (SequenceUtil.DIGIT.matcher(sequence).find()) {
			return false;
		}
		if (SequenceUtil.NON_AA.matcher(sequence).find()) {
			return false;
		}
		final Matcher protmatcher = SequenceUtil.AA.matcher(sequence);
		return protmatcher.find();
	}

	/**
	 * Check whether the sequence confirms to amboguous protein sequence
	 * 
	 * @param sequence
	 * @return return true only if the sequence if ambiguous protein sequence
	 *         Return false otherwise. e.g. if the sequence is non-ambiguous
	 *         protein or DNA
	 */
	public static boolean isAmbiguosProtein(String sequence) {
		sequence = SequenceUtil.cleanSequence(sequence);
		if (SequenceUtil.isNonAmbNucleotideSequence(sequence)) {
			return false;
		}
		if (SequenceUtil.DIGIT.matcher(sequence).find()) {
			return false;
		}
		if (SequenceUtil.NON_AA.matcher(sequence).find()) {
			return false;
		}
		if (SequenceUtil.AA.matcher(sequence).find()) {
			return false;
		}
		final Matcher amb_prot = SequenceUtil.AMBIGUOUS_AA.matcher(sequence);
		return amb_prot.find();
	}

	/**
	 * Writes list of FastaSequeces into the outstream formatting the sequence
	 * so that it contains width chars on each line
	 * 
	 * @param outstream
	 * @param sequences
	 * @param width
	 *            - the maximum number of characters to write in one line
	 * @throws IOException
	 */
	public static void writeFasta(final OutputStream outstream,
			final List<FastaSequence> sequences, final int width)
			throws IOException {
		writeFastaKeepTheStream(outstream, sequences, width);
		outstream.close();
	}

	public static void writeFastaKeepTheStream(final OutputStream outstream,
			final List<FastaSequence> sequences, final int width)
			throws IOException {
		final OutputStreamWriter writer = new OutputStreamWriter(outstream);
		final BufferedWriter fastawriter = new BufferedWriter(writer);
		for (final FastaSequence fs : sequences) {
			fastawriter.write(">" + fs.getId() + "\n");
			fastawriter.write(fs.getFormatedSequence(width));
			fastawriter.write("\n");
		}
		fastawriter.flush();
		writer.flush();
	}

	/**
	 * Reads fasta sequences from inStream into the list of FastaSequence
	 * objects
	 * 
	 * @param inStream
	 *            from
	 * @return list of FastaSequence objects
	 * @throws IOException
	 */
	public static List<FastaSequence> readFasta(final InputStream inStream)
			throws IOException {
		final List<FastaSequence> seqs = new ArrayList<FastaSequence>();
		FastaReader reader = new FastaReader(inStream);
		while (reader.hasNext()) {
			seqs.add(reader.next());
		}
		inStream.close();
		return seqs;
	}

	/**
	 * Writes FastaSequence in the file, each sequence will take one line only
	 * 
	 * @param os
	 * @param sequences
	 * @throws IOException
	 */
	public static void writeFasta(final OutputStream os,
			final List<FastaSequence> sequences) throws IOException {
		final OutputStreamWriter outWriter = new OutputStreamWriter(os);
		final BufferedWriter fasta_out = new BufferedWriter(outWriter);
		for (final FastaSequence fs : sequences) {
			fasta_out.write(fs.getOnelineFasta());
		}
		fasta_out.close();
		outWriter.close();
	}

	/**
	 * Read IUPred output
	 * 
	 * @param result
	 * @return Map key->sequence name, value->Score
	 * @throws IOException
	 * @throws UnknownFileFormatException
	 */
	public static Map<String, Score> readIUPred(final File result)
			throws IOException, UnknownFileFormatException {
		InputStream input = new FileInputStream(result);
		Map<String, Score> sequences = readIUPred(input,
				IUPredResult.getType(result));
		input.close();
		return sequences;
	}

	// Check the type of the file e.g. long| short or domain
	// and read
	/**
	 * ## Long Disorder
	 * 
	 * # P53_HUMAN
	 * 
	 * 1 M 0.9943
	 * 
	 * 2 E 0.9917
	 * 
	 * 3 E 0.9879
	 * 
	 * (every line)
	 * 
	 * @throws IOException
	 * @throws UnknownFileFormatException
	 * 
	 * 
	 */
	private static Map<String, Score> readIUPred(InputStream input,
			IUPredResult type) throws IOException, UnknownFileFormatException {

		Score score = null;
		final Map<String, Score> seqs = new HashMap<String, Score>();
		Scanner scan = new Scanner(input);
		scan.useDelimiter("#");
		while (scan.hasNext()) {
			String nextEntry = scan.next();
			Scanner entry = new Scanner(nextEntry);
			String name = entry.nextLine().trim();
			// inside entry:
			if (IUPredResult.Glob == type) {
				// parse domains
				TreeSet<Range> ranges = parseIUPredDomains(entry);
				score = new Score(type, ranges);
			} else {
				// parse short | long
				float[] scores = parseIUPredScores(entry);
				score = new Score(type, scores);
			}
			entry.close();
			seqs.put(name, score);
		}

		scan.close();
		return seqs;
	}

	/**
	 * # P53_HUMA
	 * 
	 * Number of globular domains: 2
	 * 
	 * globular domain 1. 98 - 269
	 * 
	 * globular domain 2. 431 - 482
	 * 
	 * >P53_HUMA
	 * 
	 * meepqsdpsv epplsqetfs dlwkllpenn vlsplpsqam ddlmlspddi eqwftedpgp
	 * 
	 * @param scan
	 */
	private static TreeSet<Range> parseIUPredDomains(Scanner scan) {
		String header = "Number of globular domains:";
		String domainPref = "globular domain";
		TreeSet<Range> ranges = new TreeSet<Range>();
		String line = scan.nextLine().trim();
		assert line.startsWith(header);
		line = line.substring(header.length()).trim();
		int domainNum = Integer.parseInt(line);
		if (domainNum == 0) {
			return ranges;
		}

		for (int i = 0; i < domainNum; i++) {
			assert scan.hasNextLine();
			line = scan.nextLine();
			assert line.trim().startsWith(domainPref);
			line = line.substring(line.indexOf(".") + 1).trim();
			Range r = new Range(line.split("-"));
			ranges.add(r);
		}

		return ranges;
	}
	/*
	 * 1 M 0.9943
	 * 
	 * 2 E 0.9917
	 */
	private static float[] parseIUPredScores(Scanner scan)
			throws UnknownFileFormatException {
		List<String> annotation = new ArrayList<String>();
		while (scan.hasNextLine()) {
			String line = scan.nextLine().trim();
			String[] val = line.split("\\s+");
			annotation.add(val[2]);
		}
		return convertToNumber(annotation
				.toArray(new String[annotation.size()]));
	}

	public static Map<String, Score> readJRonn(final File result)
			throws IOException, UnknownFileFormatException {
		InputStream input = new FileInputStream(result);
		Map<String, Score> sequences = readJRonn(input);
		input.close();
		return sequences;
	}

	/**
	 * Reader for JRonn horizontal file format
	 * 
	 * <pre>
	 * &gtFoobar M G D T T A G 0.48 0.42
	 * 0.42 0.48 0.52 0.53 0.54
	 * 
	 * <pre>
	 * Where all values are tab delimited
	 * 
	 * @param inStream
	 *            the InputStream connected to the JRonn output file
	 * @return Map key=sequence name value=Score
	 * @throws IOException
	 *             is thrown if the inStream has problems accessing the data
	 * @throws UnknownFileFormatException
	 *             is thrown if the inStream represents an unknown source of
	 * data, i.e. not a JRonn output
	 */
	public static Map<String, Score> readJRonn(final InputStream inStream)
			throws IOException, UnknownFileFormatException {
		final Map<String, Score> seqs = new HashMap<String, Score>();

		final BufferedReader infasta = new BufferedReader(
				new InputStreamReader(inStream, "UTF8"), 16000);

		String line;
		String sname = "";
		do {
			line = infasta.readLine();
			if (line == null || line.isEmpty()) {
				// skip empty lines
				continue;
			}
			if (line.startsWith(">")) {
				// read name
				sname = line.trim().substring(1);
				// read sequence line
				line = infasta.readLine();
				final String sequence = line.replace("\t", "");
				// read annotation line
				line = infasta.readLine();
				String[] annotValues = line.split("\t");
				float[] annotation = convertToNumber(annotValues);
				if (annotation.length != sequence.length()) {
					throw new UnknownFileFormatException(
							"File does not look like Jronn horizontally formatted output file!\n"
									+ JRONN_WRONG_FORMAT_MESSAGE);
				}
				seqs.put(sname, new Score(DisorderMethod.JRonn, annotation));
			}
		} while (line != null);

		infasta.close();
		return seqs;
	}

	private static float[] convertToNumber(String[] annotValues)
			throws UnknownFileFormatException {
		float[] annotation = new float[annotValues.length];
		try {
			for (int i = 0; i < annotation.length; i++) {
				annotation[i] = Float.parseFloat(annotValues[i]);
			}
		} catch (NumberFormatException e) {
			throw new UnknownFileFormatException(JRONN_WRONG_FORMAT_MESSAGE,
					e.getCause());
		}
		return annotation;
	}

	private static final String JRONN_WRONG_FORMAT_MESSAGE = "Jronn file must be in the following format:\n"
			+ ">sequence_name\n "
			+ "M	V	S\n"
			+ "0.43	0.22	0.65\n"
			+ "Where first line is the sequence name,\n"
			+ "second line is the tab delimited sequence,\n"
			+ "third line contains tab delimited disorder prediction values.\n"
			+ "No lines are allowed between these three. Additionally, the number of  "
			+ "sequence residues must be equal to the number of the disorder values.";

	/**
	 * Closes the Closable and logs the exception if any
	 * 
	 * @param log
	 * @param stream
	 */
	public final static void closeSilently(java.util.logging.Logger log,
			Closeable stream) {
		if (stream != null) {
			try {
				stream.close();
			} catch (IOException e) {
				log.log(Level.WARNING, e.getLocalizedMessage(), e.getCause());
			}
		}
	}

	/**
	 * 
	 > Foobar_dundeefriends
	 * 
	 * # COILS 34-41, 50-58, 83-91, 118-127, 160-169, 191-220, 243-252, 287-343
	 * 
	 * # REM465 355-368
	 * 
	 * # HOTLOOPS 190-204
	 * 
	 * # RESIDUE COILS REM465 HOTLOOPS
	 * 
	 * M 0.86010 0.88512 0.37094
	 * 
	 * T 0.79983 0.85864 0.44331
	 * 
	 * >Next Sequence name
	 * 
	 * 
	 * @param input
	 *            the InputStream
	 * @return Map key=sequence name, value=set of score
	 * @throws IOException
	 * @throws UnknownFileFormatException
	 */
	public static HashMap<String, Set<Score>> readDisembl(
			final InputStream input) throws IOException,
			UnknownFileFormatException {
		Scanner scan = new Scanner(input);
		scan.useDelimiter(">");
		if (!scan.hasNext()) {
			throw new UnknownFileFormatException(
					"In Disembl score format each sequence score is expected "
							+ "to start from the line: >Sequence name "
							+ " No such line was found!");
		}

		HashMap<String, Set<Score>> results = new HashMap<String, Set<Score>>();
		int seqCounter = 0;
		while (scan.hasNext()) {
			seqCounter++;
			String singleSeq = scan.next();
			Scanner scansingle = new Scanner(singleSeq);
			if (!scansingle.hasNextLine()) {
				throw new RuntimeException(
						"The input looks like an incomplete disembl file - cannot parse!");
			}

			StringBuffer seqbuffer = new StringBuffer();
			ArrayList<Float> coils = new ArrayList<Float>();
			ArrayList<Float> rem = new ArrayList<Float>();
			ArrayList<Float> hotloops = new ArrayList<Float>();

			String sequenceName = scansingle.nextLine().trim();
			TreeSet<Range> coilsR = parseRanges(DisemblResult.COILS,
					scansingle.nextLine());
			TreeSet<Range> rem465R = parseRanges(DisemblResult.REM465,
					scansingle.nextLine());
			TreeSet<Range> loopsR = parseRanges(DisemblResult.HOTLOOPS,
					scansingle.nextLine());

			String title = scansingle.nextLine();
			assert title.startsWith("# RESIDUE COILS REM465 HOTLOOPS") : ">Sequence_name must follow column title: # RESIDUE COILS REM465 HOTLOOPS!";

			while (scansingle.hasNext()) {
				seqbuffer.append(scansingle.next());
				coils.add(scansingle.nextFloat());
				rem.add(scansingle.nextFloat());
				hotloops.add(scansingle.nextFloat());
			}
			/*
			 * Also possible FastaSequence fs = new FastaSequence(sequenceName,
			 * seqbuffer.toString());
			 */
			HashSet<Score> scores = new HashSet<Score>();
			scores.add(new Score(DisemblResult.COILS, coils, coilsR));
			scores.add(new Score(DisemblResult.REM465, rem, rem465R));
			scores.add(new Score(DisemblResult.HOTLOOPS, hotloops, loopsR));
			results.put(sequenceName, scores);

			scansingle.close();
		}
		scan.close();
		input.close();
		return results;
	}

	/**
	 * Parsing:
	 * 
	 * # COILS 34-41, 50-58, 83-91, 118-127, 160-169, 191-220, 243-252, 287-343,
	 * 350-391, 429-485, 497-506, 539-547
	 * 
	 * # REM465 355-368
	 * 
	 * # HOTLOOPS 190-204
	 * 
	 * @param lines
	 * @return
	 */
	private static TreeSet<Range> parseRanges(Enum resultType, String lines) {
		TreeSet<Range> ranges = new TreeSet<Range>();

		Scanner scan = new Scanner(lines);

		assert scan.hasNext();
		String del = scan.next();
		assert "#".equals(del); // pass delimiter #
		String type = scan.next(); // pass enum name e.g. COILS
		assert resultType.toString().equalsIgnoreCase(type) : "Unknown result type: "
				+ resultType.toString();

		// beginning of the ranges
		scan.useDelimiter(",");
		while (scan.hasNext()) {
			String range = scan.next();
			if (!Util.isEmpty(range)) {
				ranges.add(new Range(range.split("-")));
			}
		}
		return ranges;
	}

	/**
	 * 
	 > Foobar_dundeefriends
	 * 
	 * # COILS 34-41, 50-58, 83-91, 118-127, 160-169, 191-220, 243-252, 287-343
	 * 
	 * # REM465 355-368
	 * 
	 * # HOTLOOPS 190-204
	 * 
	 * # RESIDUE COILS REM465 HOTLOOPS
	 * 
	 * M 0.86010 0.88512 0.37094
	 * 
	 * T 0.79983 0.85864 0.44331
	 * 
	 * >Next Sequence name
	 * 
	 * 
	 * @param input
	 * @return Map key=sequence name, value=set of score
	 * @throws IOException
	 * @throws UnknownFileFormatException
	 */
	public static HashMap<String, Set<Score>> readGlobPlot(
			final InputStream input) throws IOException,
			UnknownFileFormatException {
		Scanner scan = new Scanner(input);
		scan.useDelimiter(">");
		if (!scan.hasNext()) {
			throw new UnknownFileFormatException(
					"In GlobPlot score format each sequence score is expected "
							+ "to start from the line: >Sequence name "
							+ " No such line was found!");
		}

		HashMap<String, Set<Score>> results = new HashMap<String, Set<Score>>();
		int seqCounter = 0;
		while (scan.hasNext()) {
			seqCounter++;
			String singleSeq = scan.next();
			Scanner scansingle = new Scanner(singleSeq);
			if (!scansingle.hasNextLine()) {
				throw new RuntimeException(
						"The input looks like an incomplete GlobPlot file - cannot parse!");
			}

			StringBuffer seqbuffer = new StringBuffer();
			ArrayList<Float> dydxScore = new ArrayList<Float>();
			ArrayList<Float> rawScore = new ArrayList<Float>();
			ArrayList<Float> smoothedScore = new ArrayList<Float>();

			String sequenceName = scansingle.nextLine().trim();
			TreeSet<Range> domsR = parseRanges(GlobProtResult.GlobDoms,
					scansingle.nextLine());
			TreeSet<Range> disorderR = parseRanges(GlobProtResult.Disorder,
					scansingle.nextLine());

			String title = scansingle.nextLine();
			assert title.startsWith("# RESIDUE	DYDX") : ">Sequence_name must follow column title: # RESIDUE DYDX RAW SMOOTHED!";

			while (scansingle.hasNext()) {
				seqbuffer.append(scansingle.next());
				dydxScore.add(scansingle.nextFloat());
				rawScore.add(scansingle.nextFloat());
				smoothedScore.add(scansingle.nextFloat());
			}
			/*
			 * Also possible FastaSequence fs = new FastaSequence(sequenceName,
			 * seqbuffer.toString());
			 */
			Set<Score> scores = new TreeSet<Score>();
			scores.add(new Score(GlobProtResult.Disorder, disorderR));
			scores.add(new Score(GlobProtResult.GlobDoms, domsR));
			scores.add(new Score(GlobProtResult.Dydx, dydxScore));
			scores.add(new Score(GlobProtResult.RawScore, rawScore));
			scores.add(new Score(GlobProtResult.SmoothedScore, smoothedScore));
			results.put(sequenceName, scores);

			scansingle.close();
		}
		scan.close();
		input.close();
		return results;
	}
	/**
	 * Read AACon result with no alignment files. This method leaves incoming
	 * InputStream open!
	 * 
	 * @param results
	 *            output file of AAConservation
	 * @return Map with keys {@link ConservationMethod} -> float[]
	 */
	public static HashSet<Score> readAAConResults(InputStream results) {
		if (results == null) {
			throw new NullPointerException(
					"InputStream with results must be provided");
		}
		HashSet<Score> annotations = new HashSet<Score>();
		Scanner sc = new Scanner(results);
		sc.useDelimiter("#");
		while (sc.hasNext()) {
			String line = sc.next();
			int spacePos = line.indexOf(" ");
			assert spacePos > 0 : "Space is expected as delimited between method "
					+ "name and values!";
			String methodLine = line.substring(0, spacePos);
			ConservationMethod method = ConservationMethod
					.getMethod(methodLine);
			assert method != null : "Method " + methodLine
					+ " is not recognized! ";
			Scanner valuesScanner = new Scanner(line.substring(spacePos));
			ArrayList<Float> values = new ArrayList<Float>();
			while (valuesScanner.hasNextDouble()) {
				Double value = valuesScanner.nextDouble();
				values.add(value.floatValue());
			}
			annotations.add(new Score(method, values));
		}
		return annotations;
	}

	/**
	 * Reads and parses Fasta or Clustal formatted file into a list of
	 * FastaSequence objects
	 * 
	 * @param inFilePath
	 *            the path to the input file
	 * @throws IOException
	 *             if the file denoted by inFilePath cannot be read
	 * @throws UnknownFileFormatException
	 *             if the inFilePath points to the file which format cannot be
	 *             recognised
	 * @return the List of FastaSequence objects
	 * 
	 */
	public static List<FastaSequence> openInputStream(String inFilePath)
			throws IOException, UnknownFileFormatException {

		// This stream gets closed in isValidClustalFile method
		InputStream inStrForValidation = new FileInputStream(inFilePath);
		// This stream is closed in the calling methods
		InputStream inStr = new FileInputStream(inFilePath);
		List<FastaSequence> fastaSeqs = null;
		if (ClustalAlignmentUtil.isValidClustalFile(inStrForValidation)) {
			Alignment al = ClustalAlignmentUtil.readClustalFile(inStr);
			// alignment cannot be null see
			// ClustalAlignmentUtil.readClustalFile(inStr);
			fastaSeqs = al.getSequences();
		} else {
			fastaSeqs = SequenceUtil.readFasta(inStr);
		}
		return fastaSeqs;
	}

}

enum DisemblResult {
	/** These contains ranges and scores */
	COILS, REM465, HOTLOOPS
}
enum GlobProtResult {
	/** This a range with no scores */
	GlobDoms,
	/** This a range with no scores */
	Disorder,
	/** This a score with no range */
	Dydx,
	/** This a score with no range */
	SmoothedScore,
	/** This a score with no range */
	RawScore
}

enum IUPredResult {
	/**
	 * Short disorder
	 */
	Short,
	/**
	 * Long disorder
	 */
	Long,
	/**
	 * Globular domains
	 */
	Glob;

	static IUPredResult getType(File file) {
		assert file != null;
		String name = file.getName();
		if (name.endsWith(Long.toString().toLowerCase())) {
			return Long;
		}
		if (name.endsWith(Short.toString().toLowerCase())) {
			return Short;
		}
		if (name.endsWith(Glob.toString().toLowerCase())) {
			return Glob;
		}
		throw new AssertionError(
				"IUPred result file type cannot be recognised! "
						+ "\nFile must ends with one of [glob, long or short]"
						+ "\n but given file name was: " + file.getName());
	}
}