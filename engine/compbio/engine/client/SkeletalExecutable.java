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

import java.io.File;
import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;

import compbio.engine.client.CommandBuilder.Parameter;
import compbio.engine.conf.PropertyHelperManager;
import compbio.metadata.Limit;
import compbio.metadata.LimitsManager;
import compbio.util.PropertyHelper;
import compbio.util.Util;

public abstract class SkeletalExecutable<T> implements Executable<T> {

	protected static final PropertyHelper ph = PropertyHelperManager
			.getPropertyHelper();

	private static Logger log = Logger.getLogger(SkeletalExecutable.class);

	// Cache for Limits information
	private LimitsManager<T> limits;

	public static final String INPUT = "input.txt";
	public static final String OUTPUT = "result.txt";
	public static final String ERROR = "error.txt";

	protected String inputFile = INPUT;
	protected String outputFile = OUTPUT;
	protected String errorFile = ERROR;

	private boolean isInputSet = false;
	private boolean isOutputSet = false;
	private boolean isErrorSet = false;

	/**
	 * This has to allow duplicate parameters as different options may have the
	 * same value e.g. Muscle -weight1 clustalw -weight2 clustalw
	 */
	protected CommandBuilder<T> cbuilder;

	public SkeletalExecutable() {
		cbuilder = new CommandBuilder<T>(" ");
	}

	public SkeletalExecutable(String parameterKeyValueDelimiter) {
		assert parameterKeyValueDelimiter != null;
		cbuilder = new CommandBuilder<T>(parameterKeyValueDelimiter);
	}

	public SkeletalExecutable<T> setInput(String inFile) {
		if (compbio.util.Util.isEmpty(inFile)) {
			throw new IllegalArgumentException("Input file must not be NULL");
		}
		this.inputFile = inFile;
		this.isInputSet = true;
		return this;
	}

	public SkeletalExecutable<T> setOutput(String outFile) {
		if (compbio.util.Util.isEmpty(outFile)
				|| PathValidator.isAbsolutePath(outFile)) {
			throw new IllegalArgumentException(
					"Output file must not be NULL and Absolute path could not be used! Please provide the filename only. Value provided: "
							+ outFile);
		}
		this.outputFile = outFile;
		this.isOutputSet = true;
		return this;
	}

	public SkeletalExecutable<T> setError(String errFile) {
		if (compbio.util.Util.isEmpty(errFile)
				|| PathValidator.isAbsolutePath(errFile)) {
			throw new IllegalArgumentException(
					"Error file must not be NULL and Absolute path could not be used! Please provide the filename only. Value provided: "
							+ errFile);
		}
		this.errorFile = errFile;
		this.isErrorSet = true;
		return this;
	}

	@Override
	public CommandBuilder<T> getParameters(ExecProvider provider) {
		/*
		 * Prevent modification of the parameters unintentionally. This is
		 * important to preserve executable parameters intact as engine could
		 * add things into the array as it see fit. For instance
		 * ExecutableWrapper (part of local engines) add command line as the
		 * first element of an array.
		 */
		paramValueUpdater();
		return cbuilder;
	}

	@Override
	public Executable<T> addParameters(List<String> parameters) {
		cbuilder.addParams(parameters);
		return this;
	}

	public Executable<T> setParameter(String parameter) {
		cbuilder.setParam(parameter);
		return this;
	}

	/**
	 * This is a generic method of changing values of the parameters with
	 * properties
	 * 
	 * This method iterates via commands for an executable finding matches from
	 * the Executable.properties file and replacing values in CommandBuilder
	 * with a combination of value from CommandBuilder to merge path from
	 * properties
	 */
	void paramValueUpdater() {
		for (Parameter command : cbuilder.getCommandList()) {
			if (command.value == null) {
				continue;
			}
			String propertyPath = compbio.engine.client.Util.getExecProperty(
					command.name + ".path", getType());
			if (Util.isEmpty(propertyPath)) {
				continue;
			}
			if (new File(command.value).isAbsolute()) {
				// Matrix can be found so no actions necessary
				// This method has been called already and the matrix name
				// is modified to contain full path // no further actions is
				// necessary
				continue;
			}
			String absMatrixPath = compbio.engine.client.Util
					.convertToAbsolute(propertyPath);
			command.value = absMatrixPath + File.separator + command.value;
			cbuilder.setParam(command);
		}
	}

	/**
	 * This method cannot really tell whether the files has actually been
	 * created or not. It must be overridden as required.
	 * 
	 * @see compbio.engine.client.Executable#getCreatedFiles()
	 */
	@Override
	public List<String> getCreatedFiles() {
		return Arrays.asList(getOutput(), getError());
	}

	@Override
	public String getInput() {
		return inputFile;
	}

	protected boolean isInputSet() {
		return isInputSet;
	}

	protected boolean isOutputSet() {
		return isOutputSet;
	}

	protected boolean isErrorSet() {
		return isErrorSet;
	}

	@Override
	public String getOutput() {
		return outputFile;
	}

	@Override
	public String getError() {
		return errorFile;
	}

	@Override
	public String toString() {
		String value = "Input: " + this.getInput() + "\n";
		value += "Output: " + this.getOutput() + "\n";
		value += "Error: " + this.getError() + "\n";
		value += "Class: " + this.getClass() + "\n";
		value += "Params: " + cbuilder + "\n";
		return value;
	}

	@Override
	public Executable<?> loadRunConfiguration(RunConfiguration rconfig) {
		if (!compbio.util.Util.isEmpty(rconfig.getOutput())) {
			setOutput(rconfig.getOutput());
		}
		if (!compbio.util.Util.isEmpty(rconfig.getError())) {
			setError(rconfig.getError());
		}
		if (!compbio.util.Util.isEmpty(rconfig.getInput())) {
			setInput(rconfig.getInput());
		}
		this.cbuilder = (CommandBuilder<T>) rconfig.getParameters();
		return this;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof SkeletalExecutable<?>)) {
			return false;
		}
		SkeletalExecutable<?> exec = (SkeletalExecutable<?>) obj;
		if (!Util.isEmpty(this.inputFile) && !Util.isEmpty(exec.inputFile)) {
			if (!this.inputFile.equals(exec.inputFile)) {
				return false;
			}
		}
		if (!Util.isEmpty(this.outputFile) && !Util.isEmpty(exec.outputFile)) {
			if (!this.outputFile.equals(exec.outputFile)) {
				return false;
			}
		}
		if (!Util.isEmpty(this.errorFile) && !Util.isEmpty(exec.errorFile)) {
			if (!this.errorFile.equals(exec.errorFile)) {
				return false;
			}
		}
		if (!this.cbuilder.equals(exec.cbuilder)) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		int code = inputFile.hashCode();
		code += outputFile.hashCode();
		code += errorFile.hashCode();
		code *= this.cbuilder.hashCode();
		return code;
	}

	@Override
	public String getClusterJobSettings() {
		String settings = ph.getProperty(getType().getSimpleName()
				.toLowerCase() + ".cluster.settings");
		return settings == null ? "" : settings;
	}

	/**
	 * 
	 * @return number of cpus to use on the cluster or 0 if the value is
	 *         undefined
	 */
	public static int getClusterCpuNum(Class<? extends Executable<?>> type) {
		int cpus = 0;
		String cpuNum = ph.getProperty(type.getSimpleName().toLowerCase()
				+ ".cluster.cpunum");
		if (compbio.util.Util.isEmpty(cpuNum)) {
			return 0;
		}
		try {
			cpus = Integer.parseInt(cpuNum);
		} catch (NumberFormatException e) {
			// safe to ignore
			log.debug("Number of cpus to use for cluster execution is defined but could not be parsed as integer! Given value is: "
					+ cpuNum);
			return 0;
		}
		if (cpus < 1 || cpus > 100) {
			throw new InvalidParameterException(
					"Number of cpu for cluster execution must be within 1 and 100! "
							+ "Look at the value of 'tcoffee.cluster.cpunum' property. Given value is "
							+ cpus);
		}
		return cpus;
	}

	@Override
	public synchronized Limit<T> getLimit(String presetName) {
		// Assume this function is called for the first time and thus need
		// initialization
		if (limits == null) {
			limits = getLimits();
		}
		// Either the initialization failed or limits were not configured.
		if (limits == null) {
			return null;
		}

		Limit<T> limit = null;
		if (limits != null) {
			// this returns default limit if preset is undefined!
			limit = limits.getLimitByName(presetName);
		}
		// If limit is not defined for a particular preset, then return default
		// limit
		if (limit == null) {
			log.debug("Limit for the preset " + presetName
					+ " is not found. Using default");
			limit = limits.getDefaultLimit();
		}
		return limit;
	}

	@Override
	public LimitsManager<T> getLimits() {
		synchronized (SkeletalExecutable.class) {
			if (limits == null) {
				limits = compbio.engine.client.Util.getLimits(this.getType());
			}
		}
		return limits;
	}

	/**
	 * 
	 * @return subclasses must return their type
	 */
	public abstract Class<T> getType();

}
