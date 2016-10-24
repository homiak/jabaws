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

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlTransient;

import compbio.util.SysPrefs;

/**
 * Collection of Options and Parameters with their values
 * 
 * @see Option
 * @see Parameter
 * 
 * @author pvtroshin
 * 
 * @version 1.0 December 2009
 * @param <T>
 *            executable type
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class Preset<T> {

	@XmlTransient
	private static final String SPACE = " ";

	@XmlElement(required = true, nillable = false)
	// @XmlID - ? require no spaces (!)
	String name;

	String description;

	@XmlElement(required = true, nillable = false)
	@XmlElementWrapper(name = "optlist")
	List<String> option;

	public void setOptions(List<String> option) {
		this.option = option;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return a List of Options as a String
	 */
	public List<String> getOptions() {
		return new ArrayList<String>(option);
	}

	/**
	 * 
	 * @return - name of the Preset
	 */
	public String getName() {
		return name;
	}

	/**
	 * 
	 * @return - a long description of the Preset
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Converts list of options as String to type Option
	 * 
	 * @param rconfig
	 * @return List of Options
	 * @throws WrongParameterException
	 *             if the value of the parameter is invalid @see
	 *             {@link Parameter}
	 */
	public List<Option<T>> getArguments(RunnerConfig<T> rconfig)
			throws WrongParameterException {
		List<Option<T>> options = new ArrayList<Option<T>>();
		for (String optionName : option) {
			optionName = optionName.trim();
			String oname = getName(optionName);
			Option<T> option = rconfig.getArgumentByOptionName(oname);
			if (option != null) {
				// Set default value to the preset value
				if (containValue(optionName)) {
					// extract and set value to the parameter
					option.setDefaultValue(getValue(optionName));
				} else {
					// set value to the option as default, as this could be a
					// multi-option value
					option.setDefaultValue(oname);
				}
				options.add(option);
			}
		}
		return options;
	}

	boolean containValue(String option) {
		if (option.trim().contains(SPACE)) {
			return true;
		}
		return false;
	}

	String getName(String option) {
		option = option.trim();
		if (containValue(option)) {
			return option.substring(0, option.indexOf(SPACE)).trim();
		}
		return option;
	}

	String getValue(String option) {
		assert containValue(option);
		option = option.trim();
		return option.substring(option.indexOf(SPACE) + 1).trim();
	}

	@Override
	public String toString() {
		String value = "Preset name: '" + name + "'" + SysPrefs.newlinechar;
		value += "Description: " + description + SysPrefs.newlinechar;
		value += "Options: " + SysPrefs.newlinechar;
		for (String oname : this.option) {
			value += oname + SysPrefs.newlinechar;
		}
		value += SysPrefs.newlinechar;
		return value;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((description == null) ? 0 : description.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((option == null) ? 0 : option.hashCode());
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
		Preset other = (Preset) obj;
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (option == null) {
			if (other.option != null)
				return false;
		} else if (!option.equals(other.option))
			return false;
		return true;
	}

}
