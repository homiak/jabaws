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

import java.util.List;

import javax.xml.bind.ValidationException;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import compbio.util.Util;

/**
 * Collection of presets and methods to manipulate them @see {@link Preset}
 * 
 * @author pvtroshin
 * 
 * @version 1.0 December 2009
 * @param <T>
 *            type of executable.
 */
@XmlRootElement(name = "presets")
@XmlAccessorType(XmlAccessType.FIELD)
public class PresetManager<T> {

	@XmlElement(required = true)
	String runnerClassName;

	@XmlElement(required = true)
	List<Preset<T>> preset;

	public static final String LOCAL_ENGINE_LIMIT_PRESET = "# LocalEngineExecutionLimit #";

	public List<Preset<T>> getPresets() {
		return preset;
	}

	public void setPresets(List<Preset<T>> presets) {
		this.preset = presets;
	}

	/**
	 * 
	 * @return fully qualified class name of type T
	 */
	public String getRunnerClassName() {
		return runnerClassName;
	}

	public void setRunnerClassName(String runnerClassName) {
		this.runnerClassName = runnerClassName;
	}

	/**
	 * 
	 * @param presetName
	 * @return preset by its name, null if no preset found
	 */
	public Preset<T> getPresetByName(String presetName) {
		for (Preset<T> p : preset) {
			if (p.getName().equalsIgnoreCase(presetName)) {
				return p;
			}
		}
		return null;
	}

	boolean isComposite(String value) {
		assert value != null;
		return value.contains(" ");
	}

	boolean containsValue(List<String> values, String value) {
		assert !Util.isEmpty(value);
		for (String v : values) {
			if (v.equals(value)) {
				return true;
			}
		}
		return false;
	}

	boolean isNumeric(String value) {
		assert value != null;
		try {
			Double.parseDouble(value);
			return true;
		} catch (NumberFormatException e) {
			// ignore
			return false;
		}
	}

	/**
	 * Checks whether preset option and parameter are defined in RunnerConfig
	 * object.
	 * 
	 * TODO handle parameters with values properly!
	 * 
	 * @throws ValidationException
	 *             if preset is found to be invalid.
	 * 
	 */
	public void validate(RunnerConfig<T> options) throws ValidationException {
		for (Preset<T> p : preset) {
			if (Util.isEmpty(p.name)) {
				throw new ValidationException("Preset name must not be empty!");
			}
			List<String> optionNames = p.getOptions();
			if (optionNames == null || optionNames.size() == 0) {
				throw new ValidationException(
						"At lease one option must be defined for a preset!");
			}
			for (String oName : optionNames) {
				if (isComposite(oName)) {
					String name = oName.split(" ")[0];
					Argument<T> arg = getArgument(options, name);
					String value = oName.split(" ")[1];
					// Ignore numeric values
					if (!isNumeric(value)
							&& !containsValue(arg.getPossibleValues(), value)) {
						throw new ValidationException("Value " + value
								+ " is not found in the option " + name);
					}
				} else {
					getArgument(options, oName);
				}
			}
		}
	}

	Argument<T> getArgument(RunnerConfig<T> options, String optionName)
			throws ValidationException {
		Argument<T> arg = options.getArgumentByOptionName(optionName);
		if (arg == null) {
			throw new ValidationException(
					"Option "
							+ optionName
							+ " is not found in the option list (<RunnerName>Parameter.xml file)");
		}
		return arg;
	}

	@Override
	public String toString() {
		String value = "Runner: " + this.runnerClassName;
		for (Preset<T> p : preset) {
			value += p.toString() + "\n";
		}
		return value;
	}

}
