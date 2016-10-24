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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.xml.bind.ValidationException;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import compbio.engine.Configurator;
import compbio.engine.SyncExecutor;
import compbio.engine.client.ConfiguredExecutable;
import compbio.engine.client.Executable.ExecProvider;
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
import compbio.runner.msa.Tcoffee;
import compbio.util.Util;

public class TcoffeeParametersTester {

	public static String test_outfile = "TO1381.tcoffee.out";

	private static Logger log = Logger
			.getLogger(AllTestSuit.RUNNER_TEST_LOGGER);
	static {
		log.setLevel(Level.INFO);
	}

	RunnerConfig<Tcoffee> tcoffeeConfig = AllTestSuit.TCOFFEE_PARAMETERS;
	OptionCombinator tcoffeeOpc = null;
	PresetManager<Tcoffee> presets = AllTestSuit.TCOFFEE_PRESETS;

	@BeforeMethod(groups = { AllTestSuit.test_group_runner,
			AllTestSuit.test_group_non_windows })
	public void setup() {
		tcoffeeOpc = new OptionCombinator(tcoffeeConfig);
	}

	@Test(groups = { AllTestSuit.test_group_runner,
			AllTestSuit.test_group_non_windows })
	public void testDefaultParameters() {
		Tcoffee tcoffee = new Tcoffee();
		tcoffee.setInput(AllTestSuit.test_input).setOutput(test_outfile);

		try {
			// For local execution use relavive
			ConfiguredExecutable<Tcoffee> confTcoffee = Configurator
					.configureExecutable(tcoffee, ExecProvider.Cluster);
			SyncExecutor sexecutor = Configurator.getSyncEngine(confTcoffee);
			sexecutor.executeJob();
			ConfiguredExecutable<?> al = sexecutor.waitForResult();
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
			this.tcoffeeConfig.validate();
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
	public void testOptions() {
		test(tcoffeeOpc.getAllOptions());
	}

	@Test(groups = { AllTestSuit.test_group_runner,
			AllTestSuit.test_group_non_windows })
	public void testParameters() {
		List<Parameter<?>> params = tcoffeeOpc.getAllParameters();
		Collections.shuffle(params);
		test(params);
	}

	@Test(groups = { AllTestSuit.test_group_runner,
			AllTestSuit.test_group_non_windows })
	public void testArguments() {
		List<Option<?>> options = new ArrayList<Option<?>>(tcoffeeConfig
				.getOptions());
		options.addAll(tcoffeeOpc.getAllParameters());
		Collections.shuffle(options);
		test(options);
	}

	@Test(groups = { AllTestSuit.test_group_runner,
			AllTestSuit.test_group_non_windows })
	public void testConstrainedParametersMinValues() {
		Map<Parameter<?>, String> params = tcoffeeOpc
				.getAllConstrainedParametersWithBorderValues(true);
		test(params);
	}

	@Test(groups = { AllTestSuit.test_group_runner,
			AllTestSuit.test_group_non_windows })
	public void testConstrainedParametersMaxValues() {
		Map<Parameter<?>, String> params = tcoffeeOpc
				.getAllConstrainedParametersWithBorderValues(false);
		test(params);
	}

	@Test(groups = { AllTestSuit.test_group_runner,
			AllTestSuit.test_group_non_windows })
	public void testConstrainedParametersRandomValues() {
		for (int i = 0; i < 20; i++) {
			Map<Parameter<?>, String> params = tcoffeeOpc
					.getAllConstrainedParametersWithRandomValues();
			List<Parameter<?>> paramList = new ArrayList<Parameter<?>>(params
					.keySet());
			Collections.shuffle(paramList);
			List<Parameter<?>> subList = paramList.subList(0, Util
					.getRandomNumber(1, paramList.size()));
			List<String> args = tcoffeeOpc.parametersToCommandString(subList,
					params);
			singleTest(args);
		}
	}

	// TODO fix this!
	// This is due to incorrect installation of Blast with tcoffee mode
	// expresso.
	// It is either have to be fixed or removed
	@Test(groups = { AllTestSuit.test_group_runner,
			AllTestSuit.test_group_non_windows })
	public void testPresets() {
		for (Preset<Tcoffee> p : presets.getPresets()) {
			singleTest(p.getOptions());
		}
	}

	void test(Map<Parameter<?>, String> paramValue) {
		List<Parameter<?>> paramList = new ArrayList<Parameter<?>>(paramValue
				.keySet());
		for (int i = 0; i < paramValue.size(); i++) {
			List<String> args = tcoffeeOpc.parametersToCommandString(paramList,
					paramValue);
			singleTest(args);
			Collections.shuffle(paramList);
		}
		log.info("NUMBER OF COBINATION TESTED: " + paramValue.size());
	}

	void test(List<? extends Option<?>> params) {
		for (int i = 0; i < params.size(); i++) {
			List<String> args = tcoffeeOpc.argumentsToCommandString(params);
			singleTest(args);
			Collections.shuffle(params);
		}
		log.info("NUMBER OF COBINATION TESTED: " + params.size());
	}

	void singleTest(List<String> params) {
		try {
			log.info("Using arguments: " + params);
			String input = AllTestSuit.test_input;
			if (params.contains("-mode rcoffee")) {
				input = AllTestSuit.test_input_dna;
			}
			Tcoffee tcoffee = new Tcoffee();
			tcoffee.setInput(input).setOutput(test_outfile);

			// For local execution use relative
			ConfiguredExecutable<Tcoffee> confTcoffee = Configurator
					.configureExecutable(tcoffee, ExecProvider.Local);
			// Add options to the executable
			confTcoffee.addParameters(params);

			SyncExecutor sexecutor = Configurator.getSyncEngine(confTcoffee,
					ExecProvider.Local);
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
