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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;

import compbio.engine.conf.PropertyHelperManager;
import compbio.engine.conf.RunnerConfigMarshaller;
import compbio.metadata.Limit;
import compbio.metadata.LimitsManager;
import compbio.metadata.PresetManager;
import compbio.metadata.ResultNotAvailableException;
import compbio.metadata.RunnerConfig;
import compbio.metadata.UnsupportedRuntimeException;
import compbio.util.FileUtil;
import compbio.util.PropertyHelper;
import compbio.util.SysPrefs;
import compbio.util.Util;

public class ConfExecutable<T> implements ConfiguredExecutable<T> {

	private static final Logger log = Logger.getLogger(ConfExecutable.class);

	private static final PropertyHelper ph = PropertyHelperManager
			.getPropertyHelper();
	public final static String CLUSTER_TASK_ID_PREFIX = "@";

	private String workDirectory;
	private String taskDirectory;

	private ExecProvider provider;
	private Executable<T> exec;

	public ConfExecutable(Executable<T> executable, String taskDirectory) {
		this.exec = executable;
		assert !compbio.util.Util.isEmpty(taskDirectory);
		this.taskDirectory = taskDirectory;
	}

	// TODO think about appropriate exception here
	ConfExecutable(RunConfiguration rconf) {
		try {
			exec = (Executable<T>) Class.forName(rconf.runnerClassName)
					.newInstance();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		exec.loadRunConfiguration(rconf);
		setWorkDirectory(rconf.workDirectory);
		this.taskDirectory = rconf.taskId;
	}

	@Override
	public ExecProvider getExecProvider() {
		return provider;
	}

	public void setExecProvider(ExecProvider provider) {
		assert provider != null && provider != ExecProvider.Any;
		this.provider = provider;
		if (provider == Executable.ExecProvider.Cluster) {
			this.taskDirectory = CLUSTER_TASK_ID_PREFIX + taskDirectory;
		}
	}

	@Override
	public String getCommand(ExecProvider provider)
			throws UnsupportedRuntimeException {
		String command = compbio.engine.client.Util.getCommand(provider,
				exec.getClass());
		if (Util.isEmpty(command)) {
			throw new UnsupportedRuntimeException(
					"Executable "
							+ this.exec.getClass().getSimpleName()
							+ " is not supported by the current runtime environment! Current runtime environment is "
							+ (SysPrefs.isWindows
									? "Windows "
									: "Linux/Unix/Mac"));
		}
		return command;
	}

	@Override
	public ExecProvider getSupportedRuntimes() {
		return compbio.engine.client.Util.getSupportedRuntimes(exec.getClass());
	}

	@Override
	public Limit<T> getLimit(String presetName) {
		return exec.getLimit(presetName);
	}

	@Override
	public LimitsManager<T> getLimits() {
		return exec.getLimits();
	}

	@Override
	public String getTaskId() {
		return taskDirectory;
	}

	@Override
	public void setWorkDirectory(String workDirectory) {
		assert !compbio.util.Util.isEmpty(workDirectory);
		this.workDirectory = workDirectory;
	}

	@Override
	public String getWorkDirectory() {
		return this.workDirectory;
	}

	@Override
	public Executable<T> addParameters(List<String> parameters) {
		exec.addParameters(parameters);
		return exec;
	}

	@Override
	public String getOutput() {
		return exec.getOutput();
	}

	@Override
	public String getError() {
		return exec.getError();
	}

	@Override
	public List<String> getCreatedFiles() {
		return getFullPath(exec.getCreatedFiles());
	}

	List<String> getFullPath(List<String> fileNames) {
		List<String> files = new ArrayList<String>();
		for (String fileName : fileNames) {
			files.add(compbio.engine.client.Util.getFullPath(workDirectory,
					fileName));
		}
		return files;
	}

	/**
	 * Not all input paths are relative! Input path could be absolute!
	 * 
	 * @see compbio.engine.client.Executable#getInputFiles()
	 */
	@Override
	public String getInput() {
		String path = exec.getInput();
		if (PathValidator.isAbsolutePath(path)) {
			return path;
		}
		return compbio.engine.client.Util.getFullPath(workDirectory, path);
	}

	@Override
	public CommandBuilder<T> getParameters() {
		return exec.getParameters(provider);
	}

	@Override
	public CommandBuilder<T> getParameters(
			compbio.engine.client.Executable.ExecProvider provider) {
		return getParameters();
	}

	@Override
	public Executable<T> getExecutable() {
		return exec;
	}

	@Override
	public <V> V getResults() throws ResultNotAvailableException {
		return (V) exec.getResults(workDirectory);
	}

	/*
	 * This is just a pass through method (non-Javadoc)
	 * 
	 * @see compbio.runner.Executable#getResults(java.lang.String)
	 */
	@Override
	public <V> V getResults(String directory)
			throws ResultNotAvailableException {
		return (V) exec.getResults(directory);
	}

	/*
	 * This method should be executed once and result of its execution reused.
	 * If not used carefully it could slow down the system!
	 */
	public static <V> RunnerConfig<V> getRunnerOptions(
			Class<? extends Executable<V>> clazz) throws IOException {
		String parametersFile = clazz.getSimpleName().toLowerCase()
				+ ".parameters.file";
		return (RunnerConfig<V>) getRunnerConfiguration(clazz,
				RunnerConfig.class, parametersFile);
	}

	/*
	 * This method should be executed once and result of its execution reused.
	 * If not used carefully it could slow down the system!
	 */
	public static <V> PresetManager<V> getRunnerPresets(
			Class<? extends Executable<V>> clazz) throws IOException {
		String parametersFile = clazz.getSimpleName().toLowerCase()
				+ ".presets.file";
		PresetManager<V> presets = (PresetManager<V>) getRunnerConfiguration(
				clazz, PresetManager.class, parametersFile);
		return presets;
	}

	/**
	 * This method should be executed once and result of its execution reused.
	 * If not used carefully it could slow down the system!
	 * 
	 * @param <V>
	 * @param clazz
	 * @return LimitsManager instance
	 * @throws IOException
	 */
	public static <V> LimitsManager<V> getRunnerLimits(Class<V> clazz)
			throws IOException {
		String parametersFile = clazz.getSimpleName().toLowerCase()
				+ ".limits.file";
		LimitsManager<V> limits = (LimitsManager<V>) getRunnerConfiguration(
				clazz, LimitsManager.class, parametersFile);
		return limits;
	}

	static <V> Object getRunnerConfiguration(Class<V> clazz,
			Class<?> configurationHolder, String propertyName)
			throws IOException {

		Object rconf = null;
		FileInputStream confFileStream = null;
		try {
			RunnerConfigMarshaller<V> rcm = new RunnerConfigMarshaller<V>(
					configurationHolder);
			String path = ph.getProperty(propertyName);
			if (compbio.util.Util.isEmpty(path)) {
				log.debug("Configuration " + path + " is not provided");
				return null;
			}
			log.debug("Loading Configuration from " + path + " Config type:"
					+ configurationHolder);
			File confFile = new File(PropertyHelperManager.getLocalPath()
					+ path);
			confFileStream = new FileInputStream(confFile);
			rconf = rcm.read(confFileStream, configurationHolder);
			confFileStream.close();
		} catch (JAXBException e) {
			log.error(e.getLocalizedMessage(), e.getCause());
		} finally {
			FileUtil.closeSilently(log, confFileStream);
		}
		return rconf;
	}

	@Override
	public Map<String, String> getEnvironment() {
		String envProperty = ph.getProperty(exec.getClass().getSimpleName()
				.toLowerCase()
				+ ".bin.env");
		if (envProperty == null) {
			return Collections.emptyMap();
		}

		return EnvVariableProcessor.getEnvVariables(envProperty,
				this.getClass());
	}

	@Override
	public ConfiguredExecutable<?> loadRunConfiguration(RunConfiguration rconf) {
		if (rconf == null) {
			throw new NullPointerException("RunConfiguration is expected!");
		}
		return new ConfExecutable(rconf);
	}

	public static ConfiguredExecutable<?> newConfExecutable(
			RunConfiguration rconf) {
		if (rconf == null) {
			throw new NullPointerException("RunConfiguration is expected!");
		}
		return new ConfExecutable(rconf);
	}

	@Override
	public boolean saveRunConfiguration() throws IOException {
		RunConfiguration rconf = getRunConfiguration();
		return RunConfiguration.write(rconf);
	}

	public RunConfiguration getRunConfiguration() {
		/*
		 * Distinguish between dynamic settings and static settings (set in conf
		 * or class) The latter does not need saving (as if they change there
		 * were a good reason for this and it make it impossible/dangerous to
		 * rerun the task with old settings)
		 */
		// Set within Executable
		// String taskId = executable.getTaskId();

		/*
		 * All things below are handled by Executable
		 * 
		 * getInput() && getOutput()
		 * 
		 * // Handle PipedExecutables String error = executable.getError(); if
		 * (error != null) {
		 * 
		 * } String output = executable.getOutput(); if (output != null) {
		 * 
		 * } List<String> outputs = executable.getCreatedFiles(); List<String>
		 * inputs = executable.getInputFiles();
		 * 
		 * /* Environment is defined only declaratively. Map<String,String> env
		 * = executable.getEnvironment();
		 * 
		 * List<String> params = executable.getParameters();
		 */
		RunConfiguration rconf = new RunConfiguration(this);
		return rconf;
	}

	@Override
	public ConfiguredExecutable<?> loadRunConfiguration(InputStream input)
			throws IOException {
		RunConfiguration rconf = RunConfiguration.load(input);
		log.info("Loaded saved RunConfiguration " + rconf);
		return new ConfExecutable(rconf);
	}

	@Override
	public String toString() {
		String value = "Work dir: " + this.workDirectory + "\n";
		value += "TaskId: " + this.taskDirectory + "\n";
		value += "Params: " + this.getParameters() + "\n";
		value += exec.toString();
		return value;
	}

	@Override
	public String getClusterJobSettings() {
		return exec.getClusterJobSettings();
	}
}
