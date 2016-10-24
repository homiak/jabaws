/*
 * Copyright (c) 2009 Peter Troshin JAva Bioinformatics Analysis Web Services
 * (JABAWS) @version: 1.0 This library is free software; you can redistribute it
 * and/or modify it under the terms of the Apache License version 2 as published
 * by the Apache Software Foundation This library is distributed in the hope
 * that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * Apache License for more details. A copy of the license is in
 * apache_license.txt. It is also available here:
 * @see: http://www.apache.org/licenses/LICENSE-2.0.txt Any republication or
 * derived work distributed in source code form must include this copyright and
 * license notice.
 */
package compbio.data.sequence;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.testng.annotations.Test;

import compbio.metadata.AllTestSuit;
import compbio.runner.disorder.Disembl;

public class SequenceUtilTester {

	@Test()
	public void testisNonAmbNucleotideSequence() {
		String dnaseq = "atgatTGACGCTGCTGatgtcgtgagtgga";
		assertTrue(SequenceUtil.isNonAmbNucleotideSequence(dnaseq));
		String dirtyDnaseq = "atgAGTggt\taGGTgc\ncgcACTgc gACtcgcGAt cgA ";
		assertTrue(SequenceUtil.isNonAmbNucleotideSequence(dirtyDnaseq));
		String nonDna = "atgfctgatgcatgcatgatgctga";
		assertFalse(SequenceUtil.isNonAmbNucleotideSequence(nonDna));

		nonDna = "atgc1tgatgcatgcatgatgctga";
		assertFalse(SequenceUtil.isNonAmbNucleotideSequence(nonDna));

		nonDna = "ARLGRVRWTQQRHAEAAVLLQQASDAAPEHPGIALWLGHALEDAGQAEAAAAAYTRAHQL";
		assertFalse(SequenceUtil.isNonAmbNucleotideSequence(nonDna));
		// String ambDna = "AGTCRYMKSWHBVDN"; // see IUPAC Nucleotide Code
		assertFalse(SequenceUtil.isNonAmbNucleotideSequence(nonDna));

	}

	@Test()
	public void testCleanSequence() {
		String dirtySeq = "atgAGTggt\taGGTgc\ncgcAC\rTgc gACtcgcGAt cgA ";
		assertEquals("atgAGTggtaGGTgccgcACTgcgACtcgcGAtcgA".toUpperCase(),
				SequenceUtil.cleanSequence(dirtySeq));
	}

	@Test()
	public void testDeepCleanSequence() {
		String dirtySeq = "a!t?g.A;GTggt\ta12GGTgc\ncgc23AC\rTgc gAC<>.,?!|\\|/t@cg-c¬GA=_+(0){]}[:£$&^*\"t cgA ";
		assertEquals("atgAGTggtaGGTgccgcACTgcgACtcgcGAtcgA".toUpperCase(),
				SequenceUtil.deepCleanSequence(dirtySeq));
	}

	@Test()
	public void testisProteinSequence() {
		String dirtySeq = "atgAGTggt\taGGTgc\ncgcAC\rTgc gACtcgcGAt cgA ";
		assertFalse(SequenceUtil.isProteinSequence(dirtySeq));
		String notaSeq = "atgc1tgatgcatgcatgatgctga";
		assertFalse(SequenceUtil.isProteinSequence(notaSeq));
		String AAseq = "ARLGRVRWTQQRHAEAAVLLQQASDAAPEHPGIALWLGHALEDAGQAEAAAAAYTRAHQL";
		assertTrue(SequenceUtil.isProteinSequence(AAseq));
		AAseq += "XU";
		assertFalse(SequenceUtil.isProteinSequence(AAseq));

	}

	@Test()
	public void testCleanProteinSequence() {
		String dirtySeq = "atgAGTggt\taGGTgc\ncgcAC\rTgc gACtcgcGAt cgA ";
		assertFalse(SequenceUtil.isProteinSequence(dirtySeq));
		// This will still be NON protein sequence despite having only correct
		// letters because the letters match perfectly the nucleotide sequence!
		assertFalse(SequenceUtil.isProteinSequence(SequenceUtil
				.cleanProteinSequence(dirtySeq)));

		String notaSeq = "atgc1tgatgcatgcatgatgmctga";
		assertFalse(SequenceUtil.isProteinSequence(notaSeq));
		assertTrue(SequenceUtil.isProteinSequence(SequenceUtil
				.cleanProteinSequence(notaSeq)));

		String AAseq = "ARLGRVRWTQQRHAEAAVLLQQASDAAPEHPGIALWLGHALEDAGQAEAAAAAYTRAHQL";
		assertTrue(SequenceUtil.isProteinSequence(AAseq));
		assertTrue(SequenceUtil.isProteinSequence(SequenceUtil
				.cleanProteinSequence(AAseq)));
		AAseq += "XU";

		assertFalse(SequenceUtil.isProteinSequence(AAseq));
		assertTrue(SequenceUtil.isProteinSequence(SequenceUtil
				.cleanProteinSequence(AAseq)));
	}

	@Test()
	public void testReadWriteFasta() {

		try {
			FileInputStream fio = new FileInputStream(
					AllTestSuit.TEST_DATA_PATH + "TO1381.fasta");
			assertNotNull(fio);
			List<FastaSequence> fseqs = SequenceUtil.readFasta(fio);
			assertNotNull(fseqs);
			assertEquals(3, fseqs.size());
			assertEquals(3, fseqs.size());
			fio.close();
			FileOutputStream fou = new FileOutputStream(
					AllTestSuit.TEST_DATA_PATH + "TO1381.fasta.written");
			SequenceUtil.writeFasta(fou, fseqs);
			fou.close();
			FileOutputStream fou20 = new FileOutputStream(
					AllTestSuit.TEST_DATA_PATH + "TO1381.fasta20.written");
			SequenceUtil.writeFasta(fou20, fseqs, 21);
			fou20.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
			fail(e.getLocalizedMessage());
		} catch (IOException e) {
			e.printStackTrace();
			fail(e.getLocalizedMessage());
		}
	}

	/**
	 * This test tests the loading of horizontally formatted Jronn output file
	 */
	@Test
	public void loadJronnFile() {

		FileInputStream fio;
		try {
			fio = new FileInputStream(AllTestSuit.TEST_DATA_PATH + "jronn.out");
			Map<String, Score> aseqs = SequenceUtil.readJRonn(fio);
			assertNotNull(aseqs);
			assertEquals(aseqs.size(), 3);
			Score aseq = aseqs.get("Foobar");
			assertNotNull(aseq);
			assertNotNull(aseq.getScores());
			// System.out.println(aseq);
			assertEquals(aseq.getScores().size(), aseq.getScores().size());
			fio.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			fail(e.getLocalizedMessage());
		} catch (IOException e) {
			e.printStackTrace();
			fail(e.getLocalizedMessage());
		} catch (UnknownFileFormatException e) {
			e.printStackTrace();
			fail(e.getLocalizedMessage());
		}

	}

	enum Trial {
		one, two, three
	};

	/**
	 * This test tests the loading of horizontally formatted Jronn output file
	 * 
	 * First seq
	 * 
	 * M 0.86010 0.88512 0.37094
	 * 
	 * T 0.79983 0.85864 0.44331
	 * 
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testReadDisemblResults() {


		Map<String, Map<String,Set<Range>>> _ranges=new HashMap<String, Map<String,Set<Range>>>();
		Map<String, Set<Range>> ranges=new HashMap<String,Set<Range>>();
		Map<String,Map<String, Float>>  _values=new HashMap<String, Map<String,Float>>();
		Map<String, Float> values = new HashMap<String, Float>();
		Set<Range> rset;
		rset = new HashSet<Range>();
		for (String[] se:new String[][] { { "34","41"},{"50","58"},{"83","91"},{"118","127"},{" 160","169"},{" 191","220"},{" 243","252"},{" 287","343"},{" 350","391"},{" 429","485"},{" 497","506"},{"539","547"}})
		{
			rset.add(new Range(se));
		}
		ranges.put(DisemblResult.COILS.toString(), rset);
		values.put(DisemblResult.COILS.toString(), Float.valueOf(0.86010f));
		rset = new HashSet<Range>();
		for (String[] se:new String[][] { { "355","368"}})
		{
			rset.add(new Range(se));
		}
		ranges.put(DisemblResult.REM465.toString(), rset);
		values.put(DisemblResult.REM465.toString(), Float.valueOf(0.88512f));
		rset = new HashSet<Range>();
		for (String[] se:new String[][] { { "190","204"}})
		{
			rset.add(new Range(se));
		}
		ranges.put(DisemblResult.HOTLOOPS.toString(), rset);
		values.put(DisemblResult.HOTLOOPS.toString(), Float.valueOf(0.37094f));
		_ranges.put("Foobar_dundeefriends", ranges);
		_values.put("Foobar_dundeefriends", values);
		FileInputStream fio;
		try {
			fio = new FileInputStream(AllTestSuit.TEST_DATA_PATH
					+ "disembl.out");
			Map<String, Set<Score>> aseqs = SequenceUtil.readDisembl(fio);
			assertNotNull(aseqs);
			assertEquals(aseqs.size(), 3);
			ScoreManager sman = ScoreManager.newInstance(aseqs);

			for (String fs : aseqs.keySet()) {
				assertTrue(" Foobar_dundeefriends Foobar dundeefriends "
						.contains(fs));
				Set<Score> scores = aseqs.get(fs);
				assertEquals(scores.size(), 3);
				for (Score sc:scores) {
					if (_ranges.containsKey(fs))
					{
					assertEquals("Checking range for Method "+sc.getMethod(),_ranges.get(fs).get(sc.getMethod()), sc.getRanges());
					assertEquals("Checking first value for Method "+sc.getMethod(), _values.get(fs).get(sc.getMethod()), sc.getScores().get(0));
					}
				}
			}
			fio.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			fail(e.getLocalizedMessage());
		} catch (IOException e) {
			e.printStackTrace();
			fail(e.getLocalizedMessage());
		} catch (UnknownFileFormatException e) {
			e.printStackTrace();
			fail(e.getLocalizedMessage());
		}
	}
	/**
	 * This test tests the loading of horizontally formatted Jronn output file
	 * 
	 * First sequence:
	 * 
	 * >Foobar_dundeefriends
	 * 
	 * # GlobDoms 2-358, 373-568
	 * 
	 * # Disorder 1-5, 206-218, 243-250, 288-300, 313-324, 359-372, 475-481
	 * 
	 * # RESIDUE DYDX RAW SMOOTHED
	 * 
	 * M 0.0044 -0.2259 -0.2259
	 * 
	 * T -0.1308 -0.2170 -0.2170
	 * 
	 * ............
	 * 
	 * > Second sequence
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testReadGlobPlotResults() {

		FileInputStream fio;
		try {
			fio = new FileInputStream(AllTestSuit.TEST_DATA_PATH
					+ "globplot.out");
			HashMap<String, Set<Score>> aseqs = SequenceUtil.readGlobPlot(fio);
			assertNotNull(aseqs);
			assertEquals(aseqs.size(), 3);

			String fsdf = null;
			Set<Score> scores = null;
			for (String fs : aseqs.keySet()) {
				if ("Foobar_dundeefriends".contains(fs)) {
					fsdf = fs;
					scores = aseqs.get(fs);
				}
				assertEquals(scores.size(), 5);
			}

			ScoreManager sm = ScoreManager.newInstanceSingleSequence(scores);
			sm.writeOut(new PrintWriter(System.out, true));

			for (Score score : scores) {

				if (score.getMethod()
						.equals(GlobProtResult.Disorder.toString())) {
					assertEquals(score.getRanges().size(), 7);
					assertTrue(score.getScores().isEmpty());
				}
				if (GlobProtResult.valueOf(score.getMethod()) == GlobProtResult.Dydx) {
					assertFalse(score.getScores().isEmpty());
					assertTrue(score.getRanges().isEmpty());
				}
			}
			fio.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			fail(e.getLocalizedMessage());
		} catch (IOException e) {
			e.printStackTrace();
			fail(e.getLocalizedMessage());
		} catch (UnknownFileFormatException e) {
			e.printStackTrace();
			fail(e.getLocalizedMessage());
		}
	}

	@Test
	public void testReadIUPredForShortAndLongDisorder() {
		try {
			Map<String, Score> scores = SequenceUtil.readIUPred(new File(
					AllTestSuit.TEST_DATA_PATH, "out.long"));
			ScoreManager man = ScoreManager.newInstanceSingleScore(scores);
			// man.writeOut(new PrintWriter(System.out, true));
			assertNotNull(scores);
			assertEquals(3, scores.size());

			Score score = scores.get("Foobar_dundeefriends");
			assertNotNull(score);
			assertEquals(0, score.getRanges().size());
			assertEquals(568, score.getScores().size());
			assertEquals("Long", score.getMethod());

			score = scores.get("Foobar");
			assertNotNull(score);
			assertEquals(0, score.getRanges().size());
			assertEquals(481, score.getScores().size());
			assertEquals("Long", score.getMethod());

			score = scores.get("dundeefriends");
			assertNotNull(score);
			assertEquals(0, score.getRanges().size());
			assertEquals(513, score.getScores().size());
			assertEquals("Long", score.getMethod());

		} catch (IOException e) {
			e.printStackTrace();
			fail(e.getLocalizedMessage());
		} catch (UnknownFileFormatException e) {
			e.printStackTrace();
			fail(e.getLocalizedMessage());
		}
	}

	@Test
	public void testReadIUPredForGlobDomain() {
		try {
			Map<String, Score> scores = SequenceUtil.readIUPred(new File(
					AllTestSuit.TEST_DATA_PATH, "output.glob"));
			assertNotNull(scores);
			assertEquals(2, scores.size());
			ScoreManager man = ScoreManager.newInstanceSingleScore(scores);
			// man.writeOut(new PrintWriter(System.out, true));
			assertEquals(2, man.getNumberOfSeq());
			Score score = scores.get("P53_HUMA");
			assertNotNull(score);
			assertEquals(2, score.getRanges().size());
			assertEquals(0, score.getScores().size());
			assertEquals("Glob", score.getMethod());

			score = scores.get("Foobar_dundeefriends");
			assertEquals(0, score.getRanges().size());
		} catch (IOException e) {
			e.printStackTrace();
			fail(e.getLocalizedMessage());
		} catch (UnknownFileFormatException e) {
			e.printStackTrace();
			fail(e.getLocalizedMessage());
		}
	}
	@Test
	public void testReadAAConResults() {
		try {
			InputStream inStream = new FileInputStream(
					AllTestSuit.TEST_DATA_PATH + "aacon_results.txt");
			HashSet<Score> result = SequenceUtil.readAAConResults(inStream);
			inStream.close();
			assertNotNull(result);
			assertEquals(result.size(), 18);

			inStream = new FileInputStream(AllTestSuit.TEST_DATA_PATH
					+ "aacon_result_single.out");
			result = SequenceUtil.readAAConResults(inStream);
			inStream.close();
			assertNotNull(result);
			assertEquals(result.size(), 1);
			assertEquals(result.iterator().next().getScores().size(), 568);
		} catch (IOException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
}
