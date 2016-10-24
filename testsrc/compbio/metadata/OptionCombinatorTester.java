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

package compbio.metadata;

import static org.testng.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import javax.xml.bind.JAXBException;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import compbio.engine.conf.RunnerConfigMarshaller;
import compbio.metadata.RunnerConfig;
import compbio.runner.OptionCombinator;
import compbio.runner.msa.ClustalW;
import compbio.runner.msa.Mafft;
import compbio.runner.msa.Muscle;

public class OptionCombinatorTester {

	static final String mafftConfigFile = AllTestSuit.TEST_DATA_PATH
			+ "MafftParameters.xml";

	static final String muscleConfigFile = AllTestSuit.TEST_DATA_PATH
			+ "MuscleParameters.xml";

	static final String clustalConfigFile = AllTestSuit.TEST_DATA_PATH
			+ "ClustalParameters.xml";

	RunnerConfig<Mafft> mafftConfig = null;
	RunnerConfig<Mafft> muscleConfig = null;
	RunnerConfig<Mafft> clustalConfig = null;

	@BeforeMethod
	@SuppressWarnings("unchecked")
	void setup() {
		try {
			RunnerConfigMarshaller<Mafft> mf = new RunnerConfigMarshaller<Mafft>(
					RunnerConfig.class);
			mafftConfig = mf.read(
					new FileInputStream(new File(mafftConfigFile)),
					RunnerConfig.class);
			RunnerConfigMarshaller<Muscle> musclemarsh = new RunnerConfigMarshaller<Muscle>(
					RunnerConfig.class);
			muscleConfig = musclemarsh.read(new FileInputStream(new File(
					muscleConfigFile)), RunnerConfig.class);

			RunnerConfigMarshaller<ClustalW> clustalmarsh = new RunnerConfigMarshaller<ClustalW>(
					RunnerConfig.class);
			clustalConfig = clustalmarsh.read(new FileInputStream(new File(
					clustalConfigFile)), RunnerConfig.class);

		} catch (FileNotFoundException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (JAXBException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	@Test
	public void testgetAllOptions() {
		OptionCombinator opc = new OptionCombinator(mafftConfig);
		System.out.println(opc.getOptionsAtRandom());
		OptionCombinator muscleOpc = new OptionCombinator(muscleConfig);
		System.out.println("Mucle " + muscleOpc.getOptionsAtRandom());
		OptionCombinator clustalOpc = new OptionCombinator(clustalConfig);
		System.out.println("Clustal " + clustalOpc.getOptionsAtRandom());

	}

	@Test
	public void testgetAllParameters() {
		OptionCombinator opc = new OptionCombinator(mafftConfig);
		System.out.println(opc.getAllParameters());
		OptionCombinator muscleOpc = new OptionCombinator(muscleConfig);
		System.out.println("Muscle : " + muscleOpc.getAllParameters());
		OptionCombinator clustalOpc = new OptionCombinator(clustalConfig);
		System.out.println("Clustal : " + clustalOpc.getAllParameters());
	}
}
