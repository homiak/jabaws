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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.log4j.Logger;

import compbio.metadata.Option;

/*
 * Class to replace List<String> params in the SkeletalExecutable
 * So this must be included into executable 
 * 
 * List<String> Executable.getParameter() - is OK, but 
 * when RunConfiguration is loaded CommandBuilder must be recreated from the saved list!
 * It is safer to return CommandBuilder from getParameters and persist it but needs more work.
 * Benefits of doing it: delimiter is saved, key value pairs are explicitly known, no reliance on the order for 
 * space delimited key value pairs, fully functional object    
 * 
 * TODO complete and use 
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class CommandBuilder<T> {

	private static final Logger log = Logger.getLogger(CommandBuilder.class);

	Map<String, Parameter> paramList;
	String nameValueSeparator;

	// Optionally accept order template with keys in order
	// List<String> orderTemplate
	// String keyPrefix;

	private CommandBuilder() {
		// JAXB default constructor
	}

	public CommandBuilder(String nameValueSeparator) {
		this.paramList = new LinkedHashMap<String, Parameter>();
		this.nameValueSeparator = nameValueSeparator;
	}

	public void addParams(List<String> params) {
		for (String param : params) {
			setParam(param);
		}
	}

	public void setParams(List<String> params) {
		paramList.clear();
		addParams(params);
	}

	Parameter newParameter(String param) {
		if (compbio.util.Util.isEmpty(param)) {
			throw new NullPointerException("Param must be provided!");
		}
		if (isCombinedValue(param)) {
			return new Parameter(getName(param), getValue(param));
		}
		return new Parameter(param);
	}

	boolean isCombinedValue(String param) {
		return param.contains(nameValueSeparator);
	}

	public boolean hasParam(String paramName) {
		return paramList.containsKey(paramName);
	}

	String getName(String param) {
		assert param.indexOf(nameValueSeparator) >= 0 : "The value does not look like a combined value (key+value). Value:"
				+ param;
		return param.substring(0, param.indexOf(nameValueSeparator));
	}

	String getValue(String param) {
		assert param.indexOf(nameValueSeparator) >= 0 : "The value does not look like a combined value (key+value). Value:"
				+ param;
		return param.substring(param.indexOf(nameValueSeparator) + 1);
	}

	public boolean setFirst(String param) {
		Parameter p = newParameter(param);
		p.setFirst();
		return setParam(p);
	}

	public boolean setParam(String param) {
		return setParam(newParameter(param));
	}

	boolean setParam(Parameter param) {
		boolean overriden = paramList.put(param.name, param) != null;
		if (overriden) {
			log.warn("Key " + param.name
					+ " in the command list has been overriden! ");
		}
		return overriden;
	}

	public boolean setLast(String paramName) {
		Parameter p = newParameter(paramName);
		p.setLast();
		return setParam(p);
	}

	public boolean setLast(String paramName, String paramValue) {
		if (compbio.util.Util.isEmpty(paramName)) {
			throw new NullPointerException("ParamName must be provided!");
		}
		// paramName could contain separator character as long as there is no
		// paramValue!
		// e.g. paramName could be a path to the file with spaces
		if (paramValue != null && paramName.contains(nameValueSeparator)) {
			log.warn("WARN: paramName "
					+ paramName
					+ " contains nameValue separator. Removing the separator...");
			paramName = paramName.trim().substring(0, paramName.length() - 1);
		}
		if (paramValue != null && paramName.contains(nameValueSeparator)) {
			throw new IllegalArgumentException("Parameter name '" + paramName
					+ "' contains name value separator character '"
					+ nameValueSeparator + "'!");
		}
		Parameter p = new Parameter(paramName, paramValue);
		p.setLast();
		return setParam(p);
	}

	public String getParamValue(String paramName) {
		Parameter p = paramList.get(paramName);
		return p == null ? null : p.value;
	}

	public boolean removeParam(String paramName) {
		return paramList.remove(paramName) != null;
	}

	public boolean setParam(String paramName, String paramValue) {
		return setParam(new Parameter(paramName, paramValue));
	}

	public List<String> getCommands() {
		return parameterToString(getCommandList());
	}

	public String getCommandString() {
		String command = "";
		for (Parameter p : getCommandList()) {
			command += p.toCommand(this.nameValueSeparator) + " ";
		}
		return command;
	}

	List<String> parameterToString(List<Parameter> parameters) {
		List<String> commands = new ArrayList<String>();
		for (Parameter p : parameters) {
			if (isWhiteSpaceSeparator()) {
				commands.add(p.name);
				if (p.value != null) {
					commands.add(p.value);
				}
			} else {
				commands.add(p.toCommand(this.nameValueSeparator));
			}
		}
		return commands;
	}

	/**
	 * This produces the same result as getCommands method. The only difference
	 * is that it accepts a List of Options as an input
	 * 
	 * @param arguments
	 * @return the instance of the CommandBuilder
	 */
	public static <T> CommandBuilder<T> newCommandBuilder(
			List<? extends Option<T>> arguments, String nameValueSeparator) {
		CommandBuilder<T> cbuilder = new CommandBuilder<T>(nameValueSeparator);
		for (Option<T> option : arguments) {
			String comm = option.toCommand(nameValueSeparator);
			log.trace("Setting parameter " + comm);
			cbuilder.setParam(comm);
		}
		return cbuilder;
	}

	public int size() {
		return paramList.size();
	}

	boolean isWhiteSpaceSeparator() {
		return nameValueSeparator.trim().length() == 0;
	}

	List<Parameter> getCommandList() {
		List<Parameter> commands = new ArrayList<Parameter>();
		Parameter first = null;
		Parameter last = null;
		for (Parameter param : paramList.values()) {
			if (param.isFirst()) {
				assert first == null : "Attempting to set more the one parameter as FIRST! "
						+ "Parameter which has been set is: "
						+ first
						+ "\n Parameters to be set is " + param;
				first = param;
				commands.add(0, first);
				continue;
			}
			if (param.isLast()) {
				assert last == null : "Attempting to set more the one parameter as LAST! "
						+ "Parameter which has been set is: "
						+ last
						+ "\n Parameters to be set is " + param;
				last = param;
				continue;
			}
			commands.add(param);
		}
		if (last != null) {
			commands.add(last);
		}
		return commands;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((nameValueSeparator == null) ? 0 : nameValueSeparator
						.hashCode());
		result = prime * result
				+ ((paramList == null) ? 0 : paramList.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CommandBuilder<?> other = (CommandBuilder<?>) obj;
		if (nameValueSeparator == null) {
			if (other.nameValueSeparator != null)
				return false;
		} else if (!nameValueSeparator.equals(other.nameValueSeparator))
			return false;
		if (paramList == null) {
			if (other.paramList != null)
				return false;
		}
		if (other.paramList == null && paramList != null) {
			return false;
		}
		if (paramList.size() != other.paramList.size()) {
			return false;
		}
		/*
		 * Order is important at runtime, but jaxb will not preserve the order
		 * on marshalling This is to no concern here as the order is managed
		 * internally using position
		 */
		for (Map.Entry<String, Parameter> param : paramList.entrySet()) {
			String key = param.getKey();
			Parameter p = other.paramList.get(key);
			if (p == null) {
				return false;
			}
			if (!p.equals(param.getValue())) {
				return false;
			}
		}
		return true;
	}

	@Override
	public String toString() {
		String value = "NameValueSeparator: " + nameValueSeparator + "\n";
		for (Map.Entry<String, Parameter> entry : paramList.entrySet()) {
			value += "Key: " + entry.getKey() + "\n" + " Value: "
					+ entry.getValue() + "\n";
		}
		return value;
	}

	@XmlAccessorType(XmlAccessType.FIELD)
	static class Parameter {

		@XmlTransient
		private static final int LAST = -100;
		@XmlTransient
		private static final int FIRST = -200;

		// e.g. stick it at the beginning or the end or elsewhere
		private int position = -1;
		String name;
		// String alias;
		String value;

		Parameter() {
			// JAXB default constructor
		}

		Parameter(String name) {
			this.name = name;
		}

		Parameter(String name, String value) {
			this(name);
			this.value = value;
		}

		void setLast() {
			this.position = LAST;
		}

		void setFirst() {
			this.position = FIRST;
		}

		void setAtPosition(int position) {
			throw new UnsupportedOperationException();
			// this.position = position;
		}

		boolean isLast() {
			return position == LAST;
		}

		boolean isFirst() {
			return position == FIRST;
		}

		String toCommand(String separator) {
			if (value == null) {
				return name;
			}
			return name + separator + value;
		}

		@Override
		public String toString() {
			String value = "Name: " + name + "\n";
			if (value != null) {
				value += "Value: " + value + "\n";
			}
			if (position == LAST) {
				value += "Position: LAST" + "\n";
			} else if (position == FIRST) {
				value += "Position: FIRST" + "\n";
			} else if (position != -1) {
				value += "Position:" + position + "\n";
			}
			return value;
		}

		@Override
		public int hashCode() {
			final int prime = 7;
			int result = 1;
			result = prime * result + ((name == null) ? 0 : name.hashCode());
			result = prime * result + position;
			result = prime * result + ((value == null) ? 0 : value.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Parameter other = (Parameter) obj;
			if (name == null) {
				if (other.name != null)
					return false;
			} else if (!name.equals(other.name))
				return false;
			if (position != other.position)
				return false;
			if (value == null) {
				if (other.value != null)
					return false;
			} else if (!value.equals(other.value))
				return false;
			return true;
		}

	}
}
