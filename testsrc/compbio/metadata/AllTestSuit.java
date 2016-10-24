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

import java.io.File;

import compbio.runner.Util;
import compbio.runner.msa.ClustalW;
import compbio.runner.msa.Muscle;
import compbio.runner.msa.Tcoffee;
import compbio.util.SysPrefs;

public class AllTestSuit {

	public static final PresetManager<ClustalW> CLUSTAL_PRESETS = Util
			.getPresets(ClustalW.class);
	public static final RunnerConfig<ClustalW> CLUSTAL_PARAMETERS = Util
			.getSupportedOptions(ClustalW.class);
	public static final LimitsManager<ClustalW> CLUSTAL_LIMITS = compbio.engine.client.Util
			.getLimits(new ClustalW().getType());

	public static final PresetManager<Tcoffee> TCOFFEE_PRESETS = Util
			.getPresets(Tcoffee.class);
	public static final RunnerConfig<Tcoffee> TCOFFEE_PARAMETERS = Util
			.getSupportedOptions(Tcoffee.class);
	public static final LimitsManager<Tcoffee> TCOFFEE_LIMITS = compbio.engine.client.Util
			.getLimits(new Tcoffee().getType());

	public static final PresetManager<Muscle> MUSCLE_PRESETS = Util
			.getPresets(Muscle.class);
	public static final RunnerConfig<Muscle> MUSCLE_PARAMETERS = Util
			.getSupportedOptions(Muscle.class);
	public static final LimitsManager<Muscle> MUSCLE_LIMITS = compbio.engine.client.Util
			.getLimits(new Muscle().getType());

	public final static String test_group_cluster = "cluster";
	public final static String test_group_runner = "runner";
	public final static String test_group_non_windows = "non_windows";
	public final static String test_group_windows_only = "windows_only";
	public final static String test_group_engine = "engine";
	public final static String test_group_long = "performance";
	public final static String test_group_webservices = "webservices";

	/*
	 * For this to work execution must start from the project directory!
	 */
	public static final String CURRENT_DIRECTORY = SysPrefs
			.getCurrentDirectory() + File.separator;

	public static final String TEST_DATA_PATH = "testsrc" + File.separator
			+ "testdata" + File.separator;

	public static final String TEST_DATA_PATH_ABSOLUTE = AllTestSuit.CURRENT_DIRECTORY
			+ TEST_DATA_PATH;

	// For cluster execution paths MUST BE ABSOLUTE as cluster hosts will not be
	// able to access the task otherwise
	public static final String OUTPUT_DIR_ABSOLUTE = AllTestSuit.CURRENT_DIRECTORY
			+ File.separator + "local_jobsout" + File.separator;

	public static final String RUNNER_TEST_LOGGER = "RunnerLogger";

	public static final String test_input = AllTestSuit.TEST_DATA_PATH_ABSOLUTE
			+ "TO1381.fasta";

	public static final String test_alignment_input = AllTestSuit.TEST_DATA_PATH_ABSOLUTE
			+ "TO1381.fasta.aln";

	public static final String test_input_real = AllTestSuit.TEST_DATA_PATH_ABSOLUTE
			+ "50x500Protein.fasta";

	public static final String test_input_dna = AllTestSuit.TEST_DATA_PATH_ABSOLUTE
			+ "3dnaseqs.fasta";

	public static final String test_input_large = AllTestSuit.TEST_DATA_PATH_ABSOLUTE
			+ "1000x3000Dna.fasta";

}
