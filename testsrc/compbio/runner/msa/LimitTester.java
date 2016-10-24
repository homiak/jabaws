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

package compbio.runner.msa;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import compbio.data.sequence.FastaSequence;
import compbio.data.sequence.SequenceUtil;
import compbio.engine.conf.RunnerConfigMarshaller;
import compbio.metadata.AllTestSuit;
import compbio.metadata.Limit;
import compbio.metadata.LimitsManager;
import compbio.metadata.PresetManager;

public class LimitTester {

	static final String clustalLimitsFile = AllTestSuit.TEST_DATA_PATH
			+ "ClustalLimits.xml";
	static String test_input = AllTestSuit.TEST_DATA_PATH_ABSOLUTE
			+ "TO1381.fasta"; //
	static final String input = AllTestSuit.TEST_DATA_PATH
			+ "ClustalPresets.xml";

	LimitsManager<ClustalW> clustalLimitConfig = null;
	PresetManager<ClustalW> presets = null;

	@BeforeMethod(groups = { AllTestSuit.test_group_runner })
	public void setup() {
		try {
			RunnerConfigMarshaller<ClustalW> clustalmarsh = new RunnerConfigMarshaller<ClustalW>(
					LimitsManager.class);
			clustalLimitConfig = clustalmarsh.read(new FileInputStream(
					new File(clustalLimitsFile)), LimitsManager.class);
			assertNotNull(clustalLimitConfig.getLimits());
			assertEquals(clustalLimitConfig.getLimits().size(), 3);
			// Load presets
			RunnerConfigMarshaller<ClustalW> rconfigPresets = new RunnerConfigMarshaller<ClustalW>(
					PresetManager.class);
			File infile = new File(input);
			assertTrue(infile.exists());
			presets = rconfigPresets.read(new FileInputStream(infile),
					PresetManager.class);
			assertNotNull(presets);
			assertFalse(presets.getPresets().isEmpty());

		} catch (JAXBException e) {
			e.printStackTrace();
			fail(e.getLocalizedMessage());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			fail(e.getLocalizedMessage());
		}

	}

	@Test
	public void testLoadLimits() {
		assertNotNull(clustalLimitConfig);
		List<Limit<ClustalW>> limits = clustalLimitConfig.getLimits();
		assertEquals(limits.size(), 3);
		Limit<ClustalW> limit = limits.get(0);
		assertNotNull(limit);
		assertEquals(limit.getPreset(),
				"Disable gap weighting (Speed-oriented)");
		assertEquals(limit.getSeqNumber(), 400);
		assertEquals(limit.getAvgSeqLength(), 600);
		assertFalse(limit.isDefault());

		limit = limits.get(1);
		assertNotNull(limit);
		assertEquals(limit.getPreset(), null);
		assertEquals(limit.getSeqNumber(), 1000);
		assertEquals(limit.getAvgSeqLength(), 400);
		assertTrue(limit.isDefault());
	}

	@Test
	public void testLimitExceeded() {

		String test_input = AllTestSuit.TEST_DATA_PATH_ABSOLUTE
				+ "testlimit.fasta";

		FileInputStream fio;
		try {
			fio = new FileInputStream(test_input);
			List<FastaSequence> data = SequenceUtil.readFasta(fio);
			fio.close();
			assertNotNull(data);
			assertEquals(data.size(), 6);
			assertEquals(Limit.getAvgSequenceLength(data), 20486);
			Limit small = new Limit(40, 500, "default");

			assertTrue(small.isExceeded(data));

			Limit large = new Limit(500, 500, "default");
			assertFalse(large.isExceeded(data));

			Limit numSeqOnly = new Limit(6, 0, "default");
			assertFalse(numSeqOnly.isExceeded(data));

			Limit exnumSeqOnly = new Limit(5, 0, "default");
			assertTrue(exnumSeqOnly.isExceeded(data));

			Limit numSeq3 = new Limit(5, 1000000, "default");
			assertTrue(numSeq3.isExceeded(data));

		} catch (FileNotFoundException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
}
