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

package compbio.engine;

import static org.testng.Assert.fail;

import org.testng.annotations.Test;

import compbio.engine.client.ConfiguredExecutable;
import compbio.metadata.JobSubmissionException;
import compbio.runner.msa.ClustalW;

public class EngineConfiguratorTester {

	@Test
	public void testGetAsyncEngine() {

		ClustalW clustal = new ClustalW();

		try {
			SyncExecutor sEngine = Configurator.getSyncEngine(Configurator
					.configureExecutable(clustal));
		} catch (JobSubmissionException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	@Test
	public void testGetSyncEngine() {
		ClustalW clustal = new ClustalW();
		try {
			ConfiguredExecutable<ClustalW> confClust = Configurator
					.configureExecutable(clustal);
			AsyncExecutor aEngine = Configurator.getAsyncEngine(confClust);
		} catch (JobSubmissionException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

}
