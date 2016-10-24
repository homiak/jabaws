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

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;
import javax.xml.bind.ValidationException;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import compbio.engine.Configurator;
import compbio.engine.client.ConfiguredExecutable;
import compbio.engine.client.Executable.ExecProvider;
import compbio.engine.conf.RunnerConfigMarshaller;
import compbio.engine.local.ExecutableWrapper;
import compbio.engine.local.LocalRunner;
import compbio.metadata.AllTestSuit;
import compbio.metadata.JobExecutionException;
import compbio.metadata.JobSubmissionException;
import compbio.metadata.Option;
import compbio.metadata.Parameter;
import compbio.metadata.Preset;
import compbio.metadata.PresetManager;
import compbio.metadata.ResultNotAvailableException;
import compbio.metadata.RunnerConfig;
import compbio.runner.OptionCombinator;
import compbio.runner.msa.ClustalW;
import compbio.util.FileUtil;

@Test(sequential = true)
public class ClustalWParametersTester {

	static final String clustalConfigFile = AllTestSuit.TEST_DATA_PATH
			+ "ClustalParameters.xml";

	public static String test_outfile = "TO1381.clustal.out";
	public static String test_error = "TO1381.clustal.error";
	public static String cluster_test_outfile = "TO1381.clustal.cluster.out";
	public static final String input = AllTestSuit.TEST_DATA_PATH
			+ "ClustalPresets.xml";

	private static Logger log = Logger
			.getLogger(AllTestSuit.RUNNER_TEST_LOGGER);
	static {
		log.setLevel(Level.INFO);
	}

	RunnerConfig<ClustalW> clustalConfig = null;
	OptionCombinator clustalOpc = null;
	PresetManager<ClustalW> presets = null;

	@BeforeMethod(groups = { AllTestSuit.test_group_runner })
	public void setup() {
		try {
			RunnerConfigMarshaller<ClustalW> clustalmarsh = new RunnerConfigMarshaller<ClustalW>(
					RunnerConfig.class);
			clustalConfig = clustalmarsh.read(new FileInputStream(new File(
					clustalConfigFile)), RunnerConfig.class);
			clustalOpc = new OptionCombinator(clustalConfig);

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
	public void testConfiguration() {
		try {
			this.clustalConfig.validate();
		} catch (ValidationException e) {
			e.printStackTrace();
			fail(e.getLocalizedMessage());
		} catch (IllegalStateException e) {
			e.printStackTrace();
			fail(e.getLocalizedMessage());
		}
	}

	@Test(groups = { AllTestSuit.test_group_runner })
	public void testDefaultParameters() {
		ClustalW clustal = new ClustalW();
		clustal.setInput(AllTestSuit.test_input).setOutput(test_outfile);

		try {
			// For local execution use relavive
			ConfiguredExecutable<ClustalW> confClustal = Configurator
					.configureExecutable(clustal);
			LocalRunner lr = new LocalRunner(confClustal);
			lr.executeJob();
			confClustal = (ConfiguredExecutable<ClustalW>) lr.waitForResult();
			assertNotNull(confClustal.getResults());
		} catch (JobSubmissionException e) {
			e.printStackTrace();
			fail(e.getLocalizedMessage());
		} catch (JobExecutionException e) {
			e.printStackTrace();
			fail(e.getLocalizedMessage());
		} catch (ResultNotAvailableException e) {
			e.printStackTrace();
			fail(e.getLocalizedMessage());
		}
	}

	@Test(groups = { AllTestSuit.test_group_runner })
	public void testOptions() {
		test(clustalOpc.getAllOptions());
	}

	@Test(groups = { AllTestSuit.test_group_runner })
	public void testParameters() {
		List<Parameter<?>> params = clustalOpc.getAllParameters();
		Collections.shuffle(params);
		test(params);
	}

	@Test(groups = { AllTestSuit.test_group_runner })
	public void testArguments() {
		List<Option<?>> options = clustalOpc.getAllOptions();
		options.addAll(clustalOpc.getAllParameters());
		Collections.shuffle(options);
		test(options);
	}

	@Test(groups = { AllTestSuit.test_group_runner })
	public void testConstrainedParametersMinValues() {
		Map<Parameter<?>, String> params = clustalOpc
				.getAllConstrainedParametersWithBorderValues(true);
		test(params);
	}

	@Test(groups = { AllTestSuit.test_group_runner })
	public void testConstrainedParametersMaxValues() {
		Map<Parameter<?>, String> params = clustalOpc
				.getAllConstrainedParametersWithBorderValues(false);
		test(params);
	}

	@Test(groups = { AllTestSuit.test_group_runner })
	public void testConstrainedParametersRandomValues() {
		for (int i = 0; i < 5; i++) {
			Map<Parameter<?>, String> params = clustalOpc
					.getAllConstrainedParametersWithRandomValues();
			List<Parameter<?>> paramList = new ArrayList<Parameter<?>>(params
					.keySet());
			Collections.shuffle(paramList);
			List<String> args = clustalOpc.parametersToCommandString(paramList,
					params);
			singleTest(args);
		}
	}

	void test(Map<Parameter<?>, String> paramValue) {
		List<Parameter<?>> paramList = new ArrayList<Parameter<?>>(paramValue
				.keySet());
		for (int i = 0; i < paramValue.size(); i++) {
			List<String> args = clustalOpc.parametersToCommandString(paramList,
					paramValue);
			singleTest(args);
			Collections.shuffle(paramList);
		}
		log.info("NUMBER OF COBINATION TESTED: " + paramValue.size());
	}

	void test(List<? extends Option<?>> params) {
		for (int i = 0; i < params.size(); i++) {
			List<String> args = clustalOpc.argumentsToCommandString(params);
			singleTest(args);
			Collections.shuffle(params);
		}
		log.info("NUMBER OF COBINATION TESTED: " + params.size());
	}

	void singleTest(List<String> params) {
		try {
			log.info("Using arguments: " + params);
			ClustalW clustal = new ClustalW();
			clustal.setInput(AllTestSuit.test_input).setOutput(test_outfile);

			// For local execution use relative
			ConfiguredExecutable<ClustalW> confClustal = Configurator
					.configureExecutable(clustal, ExecProvider.Local);
			// Add options to the executable
			confClustal.addParameters(params);
			LocalRunner lr = new LocalRunner(confClustal);
			lr.executeJob();
			confClustal = (ConfiguredExecutable<ClustalW>) lr.waitForResult();
			assertNotNull(confClustal.getResults());
			File errors = new File(confClustal.getWorkDirectory(),
					ExecutableWrapper.PROC_ERR_FILE);
			if (errors.length() != 0) {
				log.error("PROBLEMS:\n " + FileUtil.readFileToString(errors));
			}
			assertTrue(errors.length() == 0, "Run with arguments : " + params
					+ " FAILED!");
			Collections.shuffle(params);
		} catch (JobSubmissionException e) {
			e.printStackTrace();
			fail(e.getLocalizedMessage());
		} catch (JobExecutionException e) {
			e.printStackTrace();
			fail(e.getLocalizedMessage());
		} catch (IOException e) {
			e.printStackTrace();
			fail(e.getLocalizedMessage());
		} catch (ResultNotAvailableException e) {
			e.printStackTrace();
			fail(e.getLocalizedMessage());
		}
	}

	@Test(groups = { AllTestSuit.test_group_runner })
	public void testPresets() {
		for (Preset<ClustalW> p : presets.getPresets()) {
			singleTest(p.getOptions());
		}
	}

}
