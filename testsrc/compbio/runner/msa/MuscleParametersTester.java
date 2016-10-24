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
import compbio.runner.msa.Muscle;
import compbio.util.FileUtil;
import compbio.util.Util;

public class MuscleParametersTester {

	static final String muscleConfigFile = AllTestSuit.TEST_DATA_PATH
			+ "MuscleParameters.xml";
	public static String test_outfile = "TO1381.muscle.out";
	public static String cluster_test_outfile = "TO1381.muscle.cluster.out";
	public static final String input = AllTestSuit.TEST_DATA_PATH
			+ "MusclePresets.xml";

	private static Logger log = Logger
			.getLogger(AllTestSuit.RUNNER_TEST_LOGGER);
	static {
		log.setLevel(Level.INFO);
	}

	RunnerConfig<Muscle> muscleConfig = null;
	OptionCombinator muscleOpc = null;
	PresetManager<Muscle> presets = null;

	@BeforeMethod(groups = { AllTestSuit.test_group_runner })
	public void setup() {
		try {
			RunnerConfigMarshaller<Muscle> clustalmarsh = new RunnerConfigMarshaller<Muscle>(
					RunnerConfig.class);
			muscleConfig = clustalmarsh.read(new FileInputStream(new File(
					muscleConfigFile)), RunnerConfig.class);
			muscleOpc = new OptionCombinator(muscleConfig);

			// Load presets
			RunnerConfigMarshaller<Muscle> rconfigPresets = new RunnerConfigMarshaller<Muscle>(
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

	@Test(groups = { AllTestSuit.test_group_runner })
	public void testDefaultParameters() {
		Muscle muscle = new Muscle();
		muscle.setInput(AllTestSuit.test_input).setOutput(test_outfile);

		try {
			// For local execution use relavive
			ConfiguredExecutable<Muscle> confMuscle = Configurator
					.configureExecutable(muscle);
			LocalRunner lr = new LocalRunner(confMuscle);
			lr.executeJob();
			ConfiguredExecutable<?> al = lr.waitForResult();
			assertNotNull(al.getResults());
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

	@Test
	public void testConfiguration() {
		try {
			muscleConfig.validate();
		} catch (ValidationException e) {
			e.printStackTrace();
			fail(e.getLocalizedMessage());
		} catch (IllegalStateException e) {
			e.printStackTrace();
			fail(e.getLocalizedMessage());
		}
	}

	@Test(groups = { AllTestSuit.test_group_runner })
	public void testOptions() {
		test(muscleOpc.getAllOptions());
	}

	@Test(groups = { AllTestSuit.test_group_runner })
	public void testParameters() {
		List<Parameter<?>> params = muscleOpc.getAllParameters();
		Collections.shuffle(params);
		test(params);
	}

	@Test(groups = { AllTestSuit.test_group_runner })
	public void testArguments() {
		List<Option<?>> options = new ArrayList<Option<?>>(muscleConfig
				.getOptions());
		options.addAll(muscleOpc.getAllParameters());
		Collections.shuffle(options);
		test(options);
	}

	@Test(groups = { AllTestSuit.test_group_runner })
	public void testConstrainedParametersMinValues() {
		Map<Parameter<?>, String> params = muscleOpc
				.getAllConstrainedParametersWithBorderValues(true);
		test(params);
	}

	@Test(groups = { AllTestSuit.test_group_runner })
	public void testConstrainedParametersMaxValues() {
		Map<Parameter<?>, String> params = muscleOpc
				.getAllConstrainedParametersWithBorderValues(false);
		test(params);
	}

	@Test(groups = { AllTestSuit.test_group_runner })
	public void testConstrainedParametersRandomValues() {
		for (int i = 0; i < 20; i++) {
			Map<Parameter<?>, String> params = muscleOpc
					.getAllConstrainedParametersWithRandomValues();
			List<Parameter<?>> paramList = new ArrayList<Parameter<?>>(params
					.keySet());
			Collections.shuffle(paramList);
			List<Parameter<?>> subList = paramList.subList(0, Util
					.getRandomNumber(1, paramList.size()));
			List<String> args = muscleOpc.parametersToCommandString(subList,
					params);
			singleTest(args);
		}
	}

	@Test(groups = { AllTestSuit.test_group_runner })
	public void testPresets() {
		for (Preset<Muscle> p : presets.getPresets()) {
			singleTest(p.getOptions());
		}
	}

	void test(Map<Parameter<?>, String> paramValue) {
		List<Parameter<?>> paramList = new ArrayList<Parameter<?>>(paramValue
				.keySet());
		for (int i = 0; i < paramValue.size(); i++) {
			List<String> args = muscleOpc.parametersToCommandString(paramList,
					paramValue);
			singleTest(args);
			Collections.shuffle(paramList);
		}
		log.info("NUMBER OF COBINATION TESTED: " + paramValue.size());
	}

	void test(List<? extends Option<?>> params) {
		for (int i = 0; i < params.size(); i++) {
			List<String> args = muscleOpc.argumentsToCommandString(params);
			singleTest(args);
			Collections.shuffle(params);
		}
		log.info("NUMBER OF COBINATION TESTED: " + params.size());
	}

	void singleTest(List<String> params) {
		try {
			log.info("Using arguments: " + params);
			Muscle muscle = new Muscle();
			muscle.setInput(AllTestSuit.test_input).setOutput(test_outfile);

			// For local execution use relative
			ConfiguredExecutable<Muscle> confMuscle = Configurator
					.configureExecutable(muscle, ExecProvider.Local);
			System.out.println("Using params:" + params);
			// Add options to the executable
			confMuscle.addParameters(params);

			LocalRunner lr = new LocalRunner(confMuscle);
			lr.executeJob();
			ConfiguredExecutable<?> al = lr.waitForResult();
			assertNotNull(al.getResults());
			File errors = new File(confMuscle.getWorkDirectory(),
					ExecutableWrapper.PROC_ERR_FILE);
			if (errors.length() != 0) {
				log.error("PROBLEMS:\n " + FileUtil.readFileToString(errors));
			}
			assertTrue(errors.length() == 0, "Run with arguments : " + params
					+ " FAILED!");

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

}
