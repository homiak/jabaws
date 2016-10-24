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
import compbio.runner.msa.Probcons;

@WebService(endpointInterface = "compbio.data.msa.MsaWS", targetNamespace = JABAService.SERVICE_NAMESPACE, serviceName = "ProbconsWS")
public class ProbconsWS implements MsaWS<Probcons> {

	private static Logger log = Logger.getLogger(ProbconsWS.class);

	private static final RunnerConfig<Probcons> probconsOptions = Util
			.getSupportedOptions(Probcons.class);

	private static final LimitsManager<Probcons> limitMan = compbio.engine.client.Util
			.getLimits(new Probcons().getType());

	@Override
	public String align(List<FastaSequence> sequences)
			throws JobSubmissionException {
		WSUtil.validateFastaInput(sequences);
		ConfiguredExecutable<Probcons> confProbcons = init(sequences);
		return WSUtil
				.align(sequences, confProbcons, log, "align", getLimit(""));
	}

	ConfiguredExecutable<Probcons> init(List<FastaSequence> dataSet)
			throws JobSubmissionException {
		Probcons probcons = new Probcons();
		probcons.setInput(SkeletalExecutable.INPUT)
				.setOutput(SkeletalExecutable.OUTPUT)
				.setError(SkeletalExecutable.ERROR);
		return Configurator.configureExecutable(probcons, dataSet);
	}

	@Override
	public String customAlign(List<FastaSequence> sequences,
			List<Option<Probcons>> options) throws JobSubmissionException,
			WrongParameterException {
		WSUtil.validateFastaInput(sequences);
		ConfiguredExecutable<Probcons> confProbcons = init(sequences);
		List<String> params = WSUtil.getCommands(options,
				Probcons.KEY_VALUE_SEPARATOR);
		log.info("Setting parameters:" + params);
		confProbcons.addParameters(params);
		return WSUtil.align(sequences, confProbcons, log, "customAlign",
				getLimit(""));
	}

	@Override
	public String presetAlign(List<FastaSequence> sequences,
			Preset<Probcons> preset) throws JobSubmissionException,
			WrongParameterException {
		WSUtil.validateFastaInput(sequences);
		if (preset == null) {
			throw new WrongParameterException("Preset must be provided!");
		}
		ConfiguredExecutable<Probcons> confProbcons = init(sequences);
		confProbcons.addParameters(preset.getOptions());
		Limit<Probcons> limit = getLimit(preset.getName());
		return WSUtil.align(sequences, confProbcons, log, "presetAlign", limit);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Alignment getResult(String jobId) throws ResultNotAvailableException {
		WSUtil.validateJobId(jobId);
		AsyncExecutor asyncEngine = Configurator.getAsyncEngine(jobId);
		ConfiguredExecutable<Probcons> probcons = (ConfiguredExecutable<Probcons>) asyncEngine
				.getResults(jobId);
		Alignment al = probcons.getResults();
		// log(jobId, "getResults");
		return al;
	}

	@Override
	public Limit<Probcons> getLimit(String presetName) {
		if (limitMan == null) {
			// Limit is not defined
			return null;
		}
		return limitMan.getLimitByName(presetName);
	}

	@Override
	public LimitsManager<Probcons> getLimits() {
		return limitMan;
	}

	@Override
	public ChunkHolder pullExecStatistics(String jobId, long position) {
		WSUtil.validateJobId(jobId);
		// TODO check if output is the one to return
		String file = Configurator.getWorkDirectory(jobId) + File.separator
				+ new Probcons().getError();
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
	public PresetManager<Probcons> getPresets() {
		return null;
	}

	@Override
	public RunnerConfig<Probcons> getRunnerOptions() {
		return probconsOptions;
	}

}
