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

package compbio.engine.client;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotSame;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.testng.annotations.Test;

import compbio.metadata.RunnerConfig;
import compbio.runner.msa.ClustalW;
import compbio.runner.msa.Mafft;
import compbio.runner.msa.Muscle;

public class CommandBuilderTester {

	@Test()
	public void testCommandBuilding() {
		CommandBuilder<ClustalW> builder = new CommandBuilder<ClustalW>("=");
		builder.setParam("-MATRIX", "blosum65");
		assertTrue(builder.getCommands().size() == 1);
		String p = builder.getParamValue("-MATRIX");
		builder.setParam("-clustalw");
		builder.setParam("-in", "\\gile\\path\\abs.txt");
		System.out.println(builder.getCommands());
		assertTrue(builder.getCommands().size() == 3);
		builder.setParam("-stat=/test.log");
		assertTrue(builder.getCommands().size() == 4);
		System.out.println("CP:" + builder.getCommands());
		assertTrue(builder.setParam("-stat=/newtest.log"));

		List<String> clist = Arrays.asList("-prop", "-tree=treefile.txt",
				"-clustalw");
		builder.addParams(clist);
		assertTrue(builder.size() == 6);
		builder.setParams(clist);
		assertTrue(builder.size() == 3);
		System.out.println("CP:" + builder.getCommands());
		String option = "-log=error.txt";
		builder.setFirst(option);
		assertEquals(builder.getCommands().get(0), option);
		builder.setParam("-newParam2");
		assertEquals(builder.getCommands().get(0), option);
		builder.setLast(option);
		assertNotSame(builder.getCommands().get(0), option);
		assertEquals(builder.getCommands().get(builder.size() - 1), option);
		builder.setParam("-newParam3");
		assertEquals(builder.getCommands().get(builder.size() - 1), option);
		builder.setParam("-prm5", "prm5Value");
		assertTrue(builder.hasParam("-prm5"));
	}

	@Test
	public void testSpaceDelimiterExecutable() {
		CommandBuilder<Muscle> cbuilder = new CommandBuilder<Muscle>(" ");
		cbuilder.addParams(Arrays.asList("-clwstrict", "-quiet", "-verbose",
				"-log", "EXEC_STAT_FILE"));
	}

	@Test
	public void testOptionsToCommand() {
		try {
			RunnerConfig<Mafft> rconf = ConfExecutable
					.getRunnerOptions(Mafft.class);
			CommandBuilder<Mafft> cbuilder = CommandBuilder.newCommandBuilder(
					rconf.getArguments(), Mafft.KEY_VALUE_SEPARATOR);
			String comm = "";
			for (String val : cbuilder.getCommands()) {
				assertFalse(val.contains(cbuilder.nameValueSeparator));
				comm += val + cbuilder.nameValueSeparator;
			}
			assertEquals(cbuilder.getCommandString(), comm);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			fail(e.getLocalizedMessage());
		} catch (IOException e) {
			e.printStackTrace();
			fail(e.getLocalizedMessage());
		}

	}
}
