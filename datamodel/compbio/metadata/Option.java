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

import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.bind.ValidationException;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import compbio.util.SysPrefs;
import compbio.util.Util;

/**
 * Command line option/flag or multiple exclusive options with no value. Example
 * -protein, -dna, -auto
 * 
 * @author pvtroshin
 * 
 * @version 1.0 October 2009
 * @param <T>
 *            type of executable
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class Option<T> implements Argument<T> {

	@XmlElement(required = true)
	protected String description;

	@XmlElement(required = true)
	Set<String> optionNames = new HashSet<String>();

	@XmlElement(required = true)
	protected String name;

	@XmlAttribute
	protected boolean isRequired;
	@XmlElement
	protected URL furtherDetails;
	@XmlElement
	protected String defaultValue;

	Option() {
		// Has to have no arg constructor for JAXB
	}

	public Option(String name, String description) {
		this.name = name;
		this.description = description;
	}

	/**
	 * Human readable name of the option
	 */
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * A long description of the Option
	 */
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * The URL where further details about the option can be found
	 */
	public URL getFurtherDetails() {
		return furtherDetails;
	}

	public void setFurtherDetails(URL furtherDetails) {
		this.furtherDetails = furtherDetails;
	}

	/**
	 * A default value of the option. Defaults to command line argument name
	 * e.g. -auto
	 */
	public String getDefaultValue() {
		return defaultValue;
	}

	/**
	 * Sets one of the values defined in optionList as default. Attempting set
	 * the value not listed there will result in WrongParameter exception
	 * 
	 * @param defaultVal
	 * @throws WrongParameterException
	 *             is thrown if the defaultValue is not found in optionList
	 */
	public void setDefaultValue(String defaultVal)
			throws WrongParameterException {
		if (optionNames.isEmpty()) {
			throw new IllegalStateException("Please define optionNames first!");
		}
		if (!valueExist(defaultVal, getOptionNames())) {
			throw new WrongParameterException(
					"Attempting to set illegal defaultValue '" + defaultVal
							+ "' which is not defined optionNames for option: "
							+ this);
		}
		this.defaultValue = defaultVal;
	}

	static boolean valueExist(String testValue, List<String> values) {
		assert !Util.isEmpty(testValue);
		for (String val : values) {
			if (testValue.equalsIgnoreCase(val)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Flag that indicated that this option must be specified in the command
	 * line for an executable to run
	 * 
	 * @return true is the option is required, false otherwise
	 */
	public boolean isRequired() {
		return isRequired;
	}

	public void setRequired(boolean isRequired) {
		this.isRequired = isRequired;
	}

	/**
	 * 
	 * @return List of option names
	 */
	public List<String> getOptionNames() {
		return new ArrayList<String>(optionNames);
	}

	public void setOptionNames(Set<String> optionNames) {
		this.optionNames = new HashSet<String>(optionNames);
	}

	/**
	 * Adds an option to the optionName list
	 * 
	 * @param value
	 * @return modified optionName list
	 */
	public Set<String> addOptionNames(String... value) {
		for (String v : value) {
			boolean added = this.optionNames.add(v);
			assert added : "Duplicated optionName is detected!";
		}
		return this.optionNames;
	}

	@Override
	public String toString() {
		String value = "Option name: " + this.name + SysPrefs.newlinechar;
		value += "Description: " + this.description + SysPrefs.newlinechar;
		if (!Util.isEmpty(defaultValue)) {
			value += "Default value: " + this.defaultValue
					+ SysPrefs.newlinechar;
		}
		value += "URL: " + this.furtherDetails + SysPrefs.newlinechar;
		value += "Is required: " + this.isRequired + SysPrefs.newlinechar;
		if (!this.optionNames.isEmpty()) {
			Set<String> sortedPosval = new TreeSet<String>(this.optionNames);
			value += "Option Names: " + SysPrefs.newlinechar;
			for (String val : sortedPosval) {
				value += val + SysPrefs.newlinechar;
			}
		}
		return value;
	}

	/**
	 * Convert the option to the command string.
	 * 
	 * @return If only one optionName is defined, than it is returned, if many
	 *         option names are defined, then the defaultValue is returned.
	 *         Option must have a default value if there are many optionNames to
	 *         be valid.
	 */
	public String toCommand(String nameValueSeparator) {
		if (optionNames.size() == 1) {
			return optionNames.iterator().next();
		}
		return getDefaultValue();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		Option<?> objArg = null;
		if (obj instanceof Option<?>) {
			objArg = (Option<?>) obj;
		} else {
			return false;
		}
		if (!Util.isEmpty(objArg.name) && !Util.isEmpty(name)) {
			if (!objArg.name.equals(this.name)) {
				return false;
			}
		}
		if (!Util.isEmpty(objArg.description) && !Util.isEmpty(description)) {
			if (!objArg.description.equals(this.description)) {
				return false;
			}
		}
		if (objArg.isRequired != this.isRequired) {
			return false;
		}
		if (!Util.isEmpty(objArg.defaultValue) && !Util.isEmpty(defaultValue)) {
			if (!objArg.defaultValue.equals(this.defaultValue)) {
				return false;
			}
		}
		if (objArg.optionNames.size() != this.optionNames.size()) {
			return false;
		}
		int matchCount = 0;
		for (String oname : objArg.optionNames) {
			if (Util.isEmpty(oname)) {
				continue;
			}
			for (String thisoname : this.optionNames) {
				if (oname.equals(thisoname)) {
					matchCount++;
					break;
				}
			}
		}
		if (matchCount != objArg.optionNames.size()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		int code = this.name.hashCode() * this.description.hashCode();
		if (this.isRequired) {
			code += this.furtherDetails.hashCode() * 3;
		} else {
			if (defaultValue != null) {
				code += this.defaultValue.hashCode() * 2;
			}
		}
		if (this.description != null) {
			code += this.description.hashCode() * 4;
		}

		return code;
	}

	/**
	 * List of possible optionNames
	 */
	@Override
	public List<String> getPossibleValues() {
		return new ArrayList<String>(optionNames);
	}

	@Override
	public void setValue(String dValue) throws WrongParameterException {
		this.setDefaultValue(dValue);
	}

	/**
	 * Validate the option
	 * 
	 * @throws ValidationException
	 *             is the option is invalid. This happens if option does not
	 *             have a default value but have multiple option names, or no
	 *             option names is defined
	 */
	void validate() throws ValidationException {
		if (optionNames == null) {
			throw new ValidationException(
					"Option names are not defined for option: " + this);
		}
		if (optionNames.size() > 1 && Util.isEmpty(getDefaultValue())) {
			throw new ValidationException(
					"Default value is required as multiple optionNames are defined for option: "
							+ this);
		}
		if (Util.isEmpty(name)) {
			throw new ValidationException("No name is defined for option: "
					+ this);
		}
	}
}