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

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.ValidationException;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import compbio.util.SysPrefs;
import compbio.util.annotation.NotThreadSafe;

/**
 * The list of {@link Parameter}s and {@link Option}s supported by executable.
 * The lists is defined in and loaded from <ExecutableName>Parameters.xml file.
 * 
 * @author pvtroshin
 * 
 * @version 1.0 October 2009
 * @param <T>
 *            type of an Executable
 */
@XmlRootElement
@NotThreadSafe
public class RunnerConfig<T> {

	/*
	 * Please note that the order of the fields in this class is important to
	 * generate xml compliant to the hand written schema!!!
	 */

	/**
	 * The class name of a runnable e.g. T
	 */
	private String runnerClassName;
	List<Option<T>> options = new ArrayList<Option<T>>();
	String prmSeparator;
	List<Parameter<T>> parameters = new ArrayList<Parameter<T>>();

	@XmlTransient
	List<Option<T>> arguments;

	public RunnerConfig() {
		// JAXB default constructor
	}

	public RunnerConfig<T> copyAndValidateRConfig(RunnerConfig<?> runnerConf) {
		if (this.runnerClassName != runnerConf.runnerClassName) {
			throw new InvalidParameterException("Wrong runner configuration! ");
		}
		RunnerConfig<T> newrconfig = new RunnerConfig<T>();
		newrconfig.runnerClassName = runnerConf.runnerClassName;
		newrconfig.options = new ArrayList<Option<T>>(options);
		return newrconfig;
	}

	/**
	 * Returns the list of the Options supported by the executable of type T
	 * 
	 * @return list of {@link Option} supported by type T
	 * @see Option
	 */
	public List<Option<T>> getOptions() {
		return options;
	}

	/**
	 * Adds parameter to the internal parameter list
	 * 
	 * @param param
	 *            the {@link Parameter} to add
	 * @see Parameter
	 */
	public void addParameter(Parameter<T> param) {
		assert param != null;
		parameters.add(param);
	}

	/**
	 * Adds Option to the internal list of options
	 * 
	 * @param option
	 *            the {@link Option} to add
	 */
	public void addOption(Option<T> option) {
		assert option != null;
		options.add(option);
	}

	/**
	 * Returns list of {@link Parameter} and {@link Option} supported by current
	 * runner
	 * 
	 * @return list of {@link Option} and {@link Parameter} supported by type T
	 */
	@XmlTransient
	public List<Option<T>> getArguments() {
		arguments = new ArrayList<Option<T>>(options);
		arguments.addAll(parameters);
		return arguments;
	}

	/**
	 * 
	 * @return name value separator character
	 */
	public String getPrmSeparator() {
		return prmSeparator;
	}

	/**
	 * Sets name value separator character
	 * 
	 * @param prmSeparator
	 *            the separator char
	 */
	public void setPrmSeparator(String prmSeparator) {
		this.prmSeparator = prmSeparator;
	}

	/**
	 * Adds the list of options or parameters to the internal list of options
	 * 
	 * @param parameters
	 *            the list of parameters to add
	 * 
	 */
	public void setOptions(List<Option<T>> parameters) {
		this.options = parameters;
	}

	/**
	 * 
	 * @return fully qualified class name for type T
	 */
	@XmlElement(required = true)
	public String getRunnerClassName() {
		return runnerClassName;
	}

	/**
	 * Set the name of a runner class
	 * 
	 * @param runnerClassName
	 *            the name of the executable wrapping class
	 */
	public void setRunnerClassName(String runnerClassName) {
		this.runnerClassName = runnerClassName;
	}

	/**
	 * Sets the list of parameters as internal list
	 * 
	 * @param parameters
	 *            the list of parameters
	 */
	public void setParameters(List<Parameter<T>> parameters) {
		this.parameters = parameters;
	}

	/**
	 * Returns the list of parameters supported executable of type T. Where
	 * {@link Parameter} is an {@link Option} with value.
	 * 
	 * @return List of {@link Parameter} supported by type T.
	 */
	public List<Parameter<T>> getParameters() {
		return parameters;
	}

	@Override
	public String toString() {
		String value = "Runner: " + this.runnerClassName + SysPrefs.newlinechar;
		for (Option<T> par : this.getArguments()) {
			value += par;
		}
		return value;
	}

	/*
	 * Cast is safe as runnerClassNames equality checked (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		RunnerConfig<?> rconf = null;
		if (obj instanceof RunnerConfig) {
			rconf = (RunnerConfig) obj;
		}
		if (!rconf.runnerClassName.equals(this.runnerClassName)) {
			return false;
		}
		if (this.options.size() != rconf.options.size()) {
			return false;
		}
		if (this.parameters.size() != rconf.parameters.size()) {
			return false;
		}
		if (!this.prmSeparator.equals(rconf.prmSeparator)) {
			return false;
		}
		// Size of option list is the same at that point
		for (Option<T> op : options) {
			Option<T> roption = (Option<T>) rconf.getArgument(op.getName());
			if (roption == null) {
				return false;
			}
			if (!op.equals(roption)) {
				return false;
			}
		}
		// Size of parameters list is the same at that point
		for (Parameter<T> par : parameters) {
			Parameter<T> rpar = (Parameter<T>) rconf.getArgument(par.getName());
			if (rpar == null) {
				return false;
			}
			if (!par.equals(rpar)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Returns the argument by its name if found, NULL otherwise. Where the
	 * Argument is a common interface for {@link Option} and {@link Parameter}
	 * therefore this method can return either. If you need to retrieve the
	 * Option by its optionNames use @link
	 * {@link RunnerConfig#getArgumentByOptionName(String)} method. The
	 * difference between option name and optionName is explained by the
	 * following example:
	 * 
	 * <pre>
	 * <name>Sequence type</name>
	 *         <description>
	 *         --nuc - Assume the sequences are nucleotide.
	 *         --amino - Assume the sequences are amino acid. </description>
	 *         <optionNames>--amino</optionNames>
	 *         <optionNames>--nuc</optionNames>
	 *         <optionNames>--auto</optionNames>
	 * </pre>
	 * 
	 * In the example, the "Sequence type" is a name whereas --amino, --nuc and
	 * --auto are all optionNames. This dichotomy only manifests in
	 * <code>Option</code> never in <code>Parameters</code> as the latter can
	 * only have single <optioNames> element
	 * 
	 * @param name
	 *            the Parameter of Option name
	 * @return {@link Argument}
	 */
	public Option<T> getArgument(String name) {
		for (Option<T> par : getArguments()) {
			if (par.getName().equalsIgnoreCase(name)) {
				return par;
			}
		}
		return null;
	}

	/**
	 * Removes the argument {@link Argument} if found. Where Argument is either
	 * {@link Option} or {@link Parameter}.
	 * 
	 * @param name
	 *            of the argument
	 * @return true if argument was removed, false otherwise
	 */
	@SuppressWarnings("unchecked")
	// Just use raw type in instanceof this is safe
	public boolean removeArgument(String name) {
		Option<T> par = getArgument(name);
		if (par != null) {
			if (par instanceof Parameter) {
				parameters.remove(par);
				return true;
			} else {
				this.options.remove(par);
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns the argument by option name, NULL if the argument is not found
	 * 
	 * @param optionName
	 *            - the optionName. This is not the same as an Option name.
	 * 
	 *            For example:
	 * 
	 *            <pre>
	 *            <name>Output sequences order</name>
	 *         		  <description>--inputorder - Output order: same as input. 
	 *         		   --reorder - Output order: aligned. Default: same as input</description>
	 *         		  <optionNames>--inputorder</optionNames>
	 *         		  <optionNames>--reorder</optionNames>
	 * </pre>
	 * 
	 *            The name of the option in the example is
	 *            "Output sequences order" whereas optionNames are
	 *            "--inputorder" and "--reorder". If you need to retrieve the
	 *            Option or Parameter by its names use
	 *            {@link RunnerConfig#getArgument(String)} method
	 * @return Option
	 */
	public Option<T> getArgumentByOptionName(String optionName) {
		for (Option<T> opt : getArguments()) {
			for (String val : opt.getOptionNames()) {
				if (val.equalsIgnoreCase(optionName)) {
					return opt;
				}
			}
		}

		return null;
	}

	/**
	 * Removes the argument which can be a Parameter or an Option instance by
	 * the value in <optionNames> element of the runner configuration
	 * descriptor.
	 * 
	 * @param optionName
	 *            the optionName of the option, do not confuse with the name!
	 * @return true if argument with optionName exists and was removed, false
	 *         otherwise
	 * @see RunnerConfig#getArgumentByOptionName(String) for destinctions
	 *      between optionNames and the name of the Option
	 */
	@SuppressWarnings("unchecked")
	// Just use raw type in instanceof this is safe
	public boolean removeArgumentByOptionName(String optionName) {
		Option<T> par = getArgumentByOptionName(optionName);
		if (par != null) {
			if (par instanceof Parameter) {
				this.parameters.remove(par);
				return true;
			} else {
				this.options.remove(par);
				return true;
			}
		}
		return false;
	}

	/**
	 * Validate the value of the argument. Checks whether the argument value is
	 * in the valid values range.
	 * 
	 * @throws ValidationException
	 *             if any of the arguments found invalid which is when
	 *             <dl>
	 *             <li>Parameter value outside {@link ValueConstrain} boundary</li>
	 *             <li>Parameter name is not listed in possible values</li>
	 *             </dl>
	 */
	public void validate() throws ValidationException {
		for (Option<?> option : getArguments()) {
			option.validate();
		}
	}
}
