/* Copyright (c) 2011 Peter Troshin
 *  
 *  JAva Bioinformatics Analysis Web Services (JABAWS) @version: 2.0     
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

package compbio.ws.server;

import java.io.File;
import java.util.List;

import javax.jws.WebService;

import org.apache.log4j.Logger;

import compbio.data.msa.JABAService;
import compbio.data.msa.MsaWS;
import compbio.data.sequence.Alignment;
import compbio.data.sequence.FastaSequence;
import compbio.engine.AsyncExecutor;
import compbio.engine.Configurator;
import compbio.engine.client.ConfiguredExecutable;
import compbio.engine.client.SkeletalExecutable;
import compbio.metadata.ChunkHolder;
import compbio.metadata.JobStatus;
import compbio.metadata.JobSubmissionException;
import compbio.metadata.Limit;
import compbio.metadata.LimitsManager;
import compbio.metadata.Option;
import compbio.metadata.Preset;
import compbio.metadata.PresetManager;
import compbio.metadata.ResultNotAvailableException;
import compbio.metadata.RunnerConfig;
import compbio.metadata.WrongParameterException;
import compbio.runner.Util;
import compbio.runner.msa.Muscle;

@WebService(endpointInterface = "compbio.data.msa.MsaWS", targetNamespace = JABAService.SERVICE_NAMESPACE, serviceName = "MuscleWS")
public class MuscleWS implements MsaWS<Muscle> {

	private static Logger log = Logger.getLogger(MuscleWS.class);

	private static final RunnerConfig<Muscle> muscleOptions = Util
			.getSupportedOptions(Muscle.class);

	private static final PresetManager<Muscle> musclePresets = Util
			.getPresets(Muscle.class);

	private static final LimitsManager<Muscle> limitMan = compbio.engine.client.Util
			.getLimits(new Muscle().getType());

	@Override
	public String align(List<FastaSequence> sequences)
			throws JobSubmissionException {
		WSUtil.validateFastaInput(sequences);
		ConfiguredExecutable<Muscle> confMuscle = init(sequences);
		return WSUtil.align(sequences, confMuscle, log, "align", getLimit(""));
	}

	ConfiguredExecutable<Muscle> init(List<FastaSequence> sequences)
			throws JobSubmissionException {
		Muscle muscle = new Muscle();
		muscle.setInput(SkeletalExecutable.INPUT)
				.setOutput(SkeletalExecutable.OUTPUT)
				.setError(SkeletalExecutable.ERROR);
		return Configurator.configureExecutable(muscle, sequences);
	}

	@Override
	public String customAlign(List<FastaSequence> sequences,
			List<Option<Muscle>> options) throws JobSubmissionException,
			WrongParameterException {
		WSUtil.validateFastaInput(sequences);
		ConfiguredExecutable<Muscle> confMuscle = init(sequences);
		// Could not do that! Space separated values
		// will all be treated as keys! thus duplicates removed
		// String params = cbuilder.getCommand();
		List<String> params = WSUtil.getCommands(options,
				Muscle.KEY_VALUE_SEPARATOR);
		confMuscle.addParameters(params);
		return WSUtil.align(sequences, confMuscle, log, "customAlign",
				getLimit(""));
	}

	@Override
	public String presetAlign(List<FastaSequence> sequences,
			Preset<Muscle> preset) throws JobSubmissionException,
			WrongParameterException {
		WSUtil.validateFastaInput(sequences);
		if (preset == null) {
			throw new WrongParameterException("Preset must be provided!");
		}
		ConfiguredExecutable<Muscle> confMuscle = init(sequences);
		confMuscle.addParameters(preset.getOptions());
		Limit<Muscle> limit = getLimit(preset.getName());
		return WSUtil.align(sequences, confMuscle, log, "presetAlign", limit);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Alignment getResult(String jobId) throws ResultNotAvailableException {
		WSUtil.validateJobId(jobId);
		AsyncExecutor asyncEngine = Configurator.getAsyncEngine(jobId);
		ConfiguredExecutable<Muscle> muscle = (ConfiguredExecutable<Muscle>) asyncEngine
				.getResults(jobId);
		Alignment al = muscle.getResults();
		// log(jobId, "getResults");
		return al;
	}

	@Override
	public Limit<Muscle> getLimit(String presetName) {
		if (limitMan == null) {
			// No limit is configured
			return null;
		}
		return limitMan.getLimitByName(presetName);
	}

	@Override
	public LimitsManager<Muscle> getLimits() {
		return limitMan;
	}

	@Override
	public ChunkHolder pullExecStatistics(String jobId, long position) {
		WSUtil.validateJobId(jobId);
		String file = Configurator.getWorkDirectory(jobId) + File.separator
				+ Muscle.getStatFile();
		return WSUtil.pullFile(file, position);
	}

	@Override
	public boolean cancelJob(String jobId) {
		WSUtil.validateJobId(jobId);
		return WSUtil.cancelJob(jobId);
	}

	@Override
	public JobStatus getJobStatus(String jobId) {
		WSUtil.validateJobId(jobId);
		return WSUtil.getJobStatus(jobId);
	}

	@Override
	public PresetManager<Muscle> getPresets() {
		return musclePresets;
	}

	@Override
	public RunnerConfig<Muscle> getRunnerOptions() {
		return muscleOptions;
	}

}
