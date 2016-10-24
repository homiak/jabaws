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

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
import compbio.engine.SyncExecutor;
import compbio.engine.client.ConfiguredExecutable;
import compbio.engine.client.Executable.ExecProvider;
import compbio.engine.conf.RunnerConfigMarshaller;
import compbio.metadata.AllTestSuit;
import compbio.metadata.JobExecutionException;
import compbio.metadata.JobSubmissionException;
import compbio.metadata.Option;
import compbio.metadata.Parameter;
import compbio.metadata.PresetManager;
import compbio.metadata.ResultNotAvailableException;
import compbio.metadata.RunnerConfig;
import compbio.runner.OptionCombinator;
import compbio.runner.msa.Probcons;
import compbio.util.Util;

public class ProbconsParametersTester {

	static final String probconsConfigFile = AllTestSuit.TEST_DATA_PATH
			+ "ProbconsParameters.xml";

	public static String test_outfile = "TO1381.probcons.out";

	private static Logger log = Logger
			.getLogger(AllTestSuit.RUNNER_TEST_LOGGER);
	static {
		log.setLevel(Level.INFO);
	}

	PresetManager<Probcons> presets = null;
	RunnerConfig<Probcons> probconsConfig = null;
	OptionCombinator probconsOpc = null;

	@BeforeMethod(groups = { AllTestSuit.test_group_runner,
			AllTestSuit.test_group_non_windows })
	public void setup() {
		try {
			// Load parameters
			RunnerConfigMarshaller<Probcons> mafftmarsh = new RunnerConfigMarshaller<Probcons>(
					RunnerConfig.class);
			probconsConfig = mafftmarsh.read(new FileInputStream(new File(
					probconsConfigFile)), RunnerConfig.class);
			probconsOpc = new OptionCombinator(probconsConfig);

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
			this.probconsConfig.validate();
		} catch (ValidationException e) {
			e.printStackTrace();
			fail(e.getLocalizedMessage());
		} catch (IllegalStateException e) {
			e.printStackTrace();
			fail(e.getLocalizedMessage());
		}
	}

	@Test(groups = { AllTestSuit.test_group_runner,
			AllTestSuit.test_group_non_windows })
	public void testDefaultParameters() {
		Probcons mafft = new Probcons();
		mafft.setInput(AllTestSuit.test_input).setOutput(test_outfile);

		try {
			// For local execution use relavive
			ConfiguredExecutable<Probcons> confMafft = Configurator
					.configureExecutable(mafft, ExecProvider.Cluster);
			SyncExecutor sexecutor = Configurator.getSyncEngine(confMafft);
			sexecutor.executeJob();
			confMafft = (ConfiguredExecutable<Probcons>) sexecutor
					.waitForResult();
			assertNotNull(confMafft.getResults());
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

	@Test(groups = { AllTestSuit.test_group_runner,
			AllTestSuit.test_group_non_windows })
	public void testOptions() {
		test(probconsOpc.getAllOptions());
	}

	@Test(groups = { AllTestSuit.test_group_runner,
			AllTestSuit.test_group_non_windows })
	public void testParameters() {
		List<Parameter<?>> params = probconsOpc.getAllParameters();
		Collections.shuffle(params);
		test(params);
	}

	@Test(groups = { AllTestSuit.test_group_runner,
			AllTestSuit.test_group_non_windows })
	public void testArguments() {
		List<Option<?>> options = new ArrayList<Option<?>>(probconsConfig
				.getOptions());
		options.addAll(probconsOpc.getAllParameters());
		Collections.shuffle(options);
		test(options);
	}

	@Test(groups = { AllTestSuit.test_group_runner,
			AllTestSuit.test_group_non_windows })
	public void testConstrainedParametersMinValues() {
		Map<Parameter<?>, String> params = probconsOpc
				.getAllConstrainedParametersWithBorderValues(true);
		test(params);
	}

	@Test(groups = { AllTestSuit.test_group_runner,
			AllTestSuit.test_group_non_windows })
	public void testConstrainedParametersMaxValues() {
		Map<Parameter<?>, String> params = probconsOpc
				.getAllConstrainedParametersWithBorderValues(false);
		test(params);
	}

	@Test(groups = { AllTestSuit.test_group_runner,
			AllTestSuit.test_group_non_windows })
	public void testConstrainedParametersRandomValues() {
		for (int i = 0; i < 20; i++) {
			Map<Parameter<?>, String> params = probconsOpc
					.getAllConstrainedParametersWithRandomValues();
			List<Parameter<?>> paramList = new ArrayList<Parameter<?>>(params
					.keySet());
			Collections.shuffle(paramList);
			List<Parameter<?>> subList = paramList.subList(0, Util
					.getRandomNumber(1, paramList.size()));
			List<String> args = probconsOpc.parametersToCommandString(subList,
					params);
			singleTest(args);
		}
	}

	void test(Map<Parameter<?>, String> paramValue) {
		List<Parameter<?>> paramList = new ArrayList<Parameter<?>>(paramValue
				.keySet());
		for (int i = 0; i < paramValue.size(); i++) {
			List<String> args = probconsOpc.parametersToCommandString(
					paramList, paramValue);
			singleTest(args);
			Collections.shuffle(paramList);
		}
		log.info("NUMBER OF COBINATION TESTED: " + paramValue.size());
	}

	void test(List<? extends Option<?>> params) {
		for (int i = 0; i < params.size(); i++) {
			List<String> args = probconsOpc.argumentsToCommandString(params);
			singleTest(args);
			Collections.shuffle(params);
		}
		log.info("NUMBER OF COBINATION TESTED: " + params.size());
	}

	void singleTest(List<String> params) {
		try {
			log.info("Using arguments: " + params);
			Probcons mafft = new Probcons();
			mafft.setInput(AllTestSuit.test_input).setOutput(test_outfile);

			// For local execution use relative
			ConfiguredExecutable<Probcons> confMafft = Configurator
					.configureExecutable(mafft, ExecProvider.Local);
			// Add options to the executable
			confMafft.addParameters(params);

			SyncExecutor sexecutor = Configurator.getSyncEngine(confMafft);
			sexecutor.executeJob();
			ConfiguredExecutable<?> al = sexecutor.waitForResult();
			assertNotNull(al.getResults());
			/*
			 * TODO File errors = new File(confMafft.getWorkDirectory(),
			 * ExecutableWrapper.PROC_ERR_FILE); if (errors.length() != 0) {
			 * log.error("PROBLEMS:\n " + FileUtil.readFileToString(errors)); }
			 * assertTrue("Run with arguments : " + params + " FAILED!", errors
			 * .length() == 0);
			 */
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

}
