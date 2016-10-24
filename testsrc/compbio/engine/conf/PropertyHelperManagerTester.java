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

package compbio.engine.conf;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.io.File;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import compbio.metadata.AllTestSuit;
import compbio.util.PropertyHelper;

public class PropertyHelperManagerTester {

	PropertyHelper ph;

	@BeforeClass(alwaysRun=true)
	public void testLoadResources() {
		ph = PropertyHelperManager.getPropertyHelper();
		assertNotNull(ph);
	}

	@Test(groups = AllTestSuit.test_group_cluster)
	public void testClusterEngineConf() {
		// Below are the properties on cluster engine required for its
		// functioning
		validateDirProp("cluster.tmp.directory");
	}

	void validateDirProp(String propName) {
		assertNotNull(ph);
		assertNotNull(ph.getProperty(propName));
		File tmp = new File(ph.getProperty(propName).trim());
		assertTrue(tmp.exists());
		assertTrue(tmp.isDirectory());
		assertTrue(tmp.canRead());
		assertTrue(tmp.canWrite());
	}

	void validateExecFileProp(String propName) {
		validateFileProp(propName);
		File tmp = new File(ph.getProperty(propName));
		assertTrue(tmp.canExecute());
	}

	void validateFileProp(String propName) {
		assertNotNull(ph.getProperty(propName));
		File tmp = new File(ph.getProperty(propName));
		assertTrue(tmp.exists());
		assertTrue(tmp.isFile());
		assertTrue(tmp.canRead());
	}

	@Test
	public void validateClustalConfiguration() {
		// Below are the properties on Clustal executable required for its
		// functioning
		// Executables could not be verified as full path is constructed only at
		// runtime
		assertNotNull("local.clustalw.bin.windows");
		assertNotNull("local.clustalw.bin");
		assertNotNull("cluster.clustalw.bin");
		validateFileProp("clustalw.presets.file");
		validateFileProp("clustalw.parameters.file");
		//validateFileProp("clustalw.limits.file");
	}

	@Test
	public void validateMuscleConfiguration() {
		assertNotNull("local.muscle.bin.windows");
		assertNotNull("local.muscle.bin");
		assertNotNull("cluster.muscle.bin");
		validateFileProp("muscle.presets.file");
		validateFileProp("muscle.parameters.file");
		//validateFileProp("muscle.limits.file");
	}

	@Test
	public void validateTcoffeeConfiguration() {
		assertNotNull("local.tcoffee.bin.windows");
		assertNotNull("local.tcoffee.bin");
		assertNotNull("cluster.tcoffee.bin");
		validateFileProp("tcoffee.presets.file");
		validateFileProp("tcoffee.parameters.file");
		//validateFileProp("tcoffee.limits.file");
	}

	@Test
	public void validateMafftConfiguration() {
		assertNotNull("local.mafft.bin.windows");
		assertNotNull("local.mafft.bin");
		assertNotNull("cluster.mafft.bin");
		validateFileProp("mafft.presets.file");
		validateFileProp("mafft.parameters.file");
		//validateFileProp("mafft.limits.file");
	}
}
