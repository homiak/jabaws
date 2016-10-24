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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.log4j.Logger;

import compbio.engine.conf.RunnerConfigMarshaller;
import compbio.util.Util;

/**
 * Value class for persisting ConfExecutable instances
 * 
 * @author pvtroshin
 * 
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class RunConfiguration {

	@XmlTransient
	private static Logger log = Logger.getLogger(RunConfiguration.class);

	@XmlTransient
	public static final String rconfigFile = "RunnerConfig.xml";

	/**
	 * Task ID (is a part of workDirectory but extracting it from there could be
	 * tricky)
	 */
	@XmlElement(required = true)
	String taskId;

	/**
	 * Working directory
	 */
	@XmlElement(required = true)
	String workDirectory;

	/**
	 * Parameters
	 */
	@XmlElement(required = true)
	private CommandBuilder<?> parameters;

	/**
	 * Input option
	 */
	@XmlElement(nillable = true)
	private String error;

	/**
	 * Output
	 */
	@XmlElement(nillable = true)
	private String output;

	/**
	 * Input
	 */
	@XmlElement(nillable = true)
	private String input;

	/**
	 * Runner class name
	 */
	@XmlElement(required = true)
	String runnerClassName;

	public RunConfiguration() {
		// JAXB default constructor
	}

	public RunConfiguration(ConfExecutable<?> cexec) {
		this.setParameters(cexec.getParameters());
		this.workDirectory = cexec.getWorkDirectory();
		this.taskId = cexec.getTaskId();
		this.runnerClassName = cexec.getExecutable().getClass()
				.getCanonicalName();
		this.setOutput(cexec.getOutput());
		this.setInput(cexec.getInput());
		this.setError(cexec.getError());
	}

	public static boolean write(RunConfiguration rconf) throws IOException {
		File rconfFile = new File(rconf.workDirectory, rconfigFile);
		try {
			RunnerConfigMarshaller<ConfiguredExecutable<?>> confMarch = new RunnerConfigMarshaller<ConfiguredExecutable<?>>(
					RunConfiguration.class);
			confMarch.write(rconf, new FileOutputStream(rconfFile));
		} catch (JAXBException e) {
			e.printStackTrace();
			log.error("Failed to save RunConfiguration " + rconf + " Error: "
					+ e.getMessage(), e.getCause());
		} catch (FileNotFoundException e) {
			log.error("Failed to save RunConfiguration " + rconf + " Error: "
					+ e.getMessage(), e.getCause());
		}
		return rconfFile.exists() && rconfFile.length() > 0;
	}

	public static RunConfiguration load(InputStream input) throws IOException {
		RunConfiguration rconf = null;
		try {
			RunnerConfigMarshaller<RunConfiguration> confMarch = new RunnerConfigMarshaller<RunConfiguration>(
					RunConfiguration.class);
			rconf = confMarch.read(input, RunConfiguration.class);
		} catch (JAXBException e) {
			log.error("Failed to load RunConfiguration " + rconf + " Error: "
					+ e.getMessage(), e.getCause());
		}
		return rconf;
	}

	public void setOutput(String output) {
		this.output = output;
	}

	public CommandBuilder<?> getParameters() {
		return parameters;
	}

	public void setParameters(CommandBuilder<?> parameters) {
		this.parameters = parameters;
	}

	public String getOutput() {
		return output;
	}

	public void setError(String error) {
		this.error = error;
	}

	public String getError() {
		return error;
	}

	public String getInput() {
		return input;
	}

	public void setInput(String input) {
		this.input = input;
	}

	public String getRunnerClassName() {
		return this.runnerClassName;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof RunConfiguration)) {
			return false;
		}
		RunConfiguration rconf = (RunConfiguration) obj;
		if (!strEquals(this.error, rconf.error)) {
			return false;
		}
		if (!strEquals(this.input, rconf.input)) {
			return false;
		}
		if (!strEquals(this.output, rconf.output)) {
			return false;
		}
		if (!strEquals(this.runnerClassName, rconf.runnerClassName)) {
			return false;
		}
		if (!strEquals(this.taskId, rconf.taskId)) {
			return false;
		}
		if (!strEquals(this.workDirectory, rconf.workDirectory)) {
			return false;
		}
		if (!parameters.equals(rconf.parameters)) {
			return false;
		}
		return true;
	}

	private boolean strEquals(String value, String other) {
		if (!Util.isEmpty(value) && !Util.isEmpty(other)) {
			if (!value.equals(other)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public int hashCode() {
		int code = this.taskId.hashCode() * this.runnerClassName.hashCode();
		code *= this.parameters.hashCode();
		return code;
	}

	@Override
	public String toString() {
		String value = "Class : " + this.runnerClassName + "\n";
		value += "TaskId: " + this.taskId + "\n";
		value += "Input: " + this.input + "\n";
		value += "Output: " + this.output + "\n";
		value += "Error: " + this.error + "\n";
		value += "Workdir: " + this.workDirectory + "\n";
		value += "Param: " + this.parameters + "\n";
		return value;
	}

}
