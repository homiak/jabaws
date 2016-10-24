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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.bind.ValidationException;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import compbio.util.SysPrefs;
import compbio.util.Util;

/**
 * A single value containing an option supported by the web service e.g.
 * seqType=protein. Where seqType is a optionName and protein is one of
 * possibleValues
 * 
 * @see Option
 * @see Argument
 * 
 * @author pvtroshin
 * 
 * @version 1.0 November 2009
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class Parameter<T> extends Option<T> {

	@XmlElement
	Set<String> possibleValues = new HashSet<String>();

	ValueConstrain validValue;

	private Parameter() {
		// JAXB noargs const
	}

	public Parameter(String name, String description) {
		super(name, description);
	}

	public ValueConstrain getValidValue() {
		return validValue;
	}

	public void setValidValue(ValueConstrain validValue) {
		if (validValue == null) {
			throw new NullPointerException("ValueConstrain is expected!");
		}
		this.validValue = validValue;
	}

	@Override
	public String toString() {
		String value = super.toString();
		if (validValue != null) {
			value += validValue.toString();
		}
		if (!this.possibleValues.isEmpty()) {
			Set<String> sortedPosval = new TreeSet<String>(this.possibleValues);
			value += "POSSIBLE VALUES:" + SysPrefs.newlinechar;
			for (String val : sortedPosval) {
				value += val + SysPrefs.newlinechar;
			}
		}
		value += SysPrefs.newlinechar;
		return value;
	}

	@Override
	public String toCommand(String nameValueSeparator) {
		if (nameValueSeparator == null) {
			throw new NullPointerException("Name value separator is expected!");
		}
		return getOptionName() + nameValueSeparator + getValue();
	}

	@Override
	public boolean equals(Object obj) {
		if (!super.equals(obj)) {
			return false;
		}
		Parameter<?> objp = null;
		if (obj instanceof Parameter<?>) {
			objp = (Parameter<?>) obj;
		} else {
			return false;
		}

		if (objp.possibleValues.size() != this.possibleValues.size()) {
			return false;
		}
		int matchCount = 0;
		for (String pv : objp.possibleValues) {
			if (Util.isEmpty(pv)) {
				continue;
			}
			for (String thispv : this.possibleValues) {
				if (pv.equals(thispv)) {
					matchCount++;
					break;
				}
			}
		}
		if (matchCount != objp.possibleValues.size()) {
			return false;
		}

		return true;
	}

	/**
	 * List is more convenient to work with
	 * 
	 * @return List of String
	 */
	@Override
	public List<String> getPossibleValues() {
		return new ArrayList<String>(possibleValues);
	}

	public void setPossibleValues(Set<String> possibleValues) {
		this.possibleValues = new HashSet<String>(possibleValues);
	}

	public Set<String> addPossibleValues(String... value) {
		for (String v : value) {
			this.possibleValues.add(v);
		}
		return this.possibleValues;
	}

	@Override
	public int hashCode() {
		int code = super.hashCode();
		if (possibleValues != null) {
			code += possibleValues.hashCode();
		}
		return code;
	}

	@Override
	public void setOptionNames(Set<String> optionName) {
		if (optionName.size() != 1) {
			throw new IllegalArgumentException(
					"Parameter must have a single option name! But given "
							+ optionName.size() + " names:  " + optionName);
		}
		super.setOptionNames(optionName);
	}

	@Override
	public Set<String> addOptionNames(String... value) {
		throw new UnsupportedOperationException(
				"Parameter must have only one optionName! If you setting the only name that use setOptionName instead");
	}

	public String getOptionName() {
		assert optionNames.size() == 1;
		return optionNames.iterator().next();
	}

	public void setOptionName(String optionName) {
		assert !Util.isEmpty(optionName);
		setOptionNames(Collections.singleton(optionName));
	}

	String getValue() {
		if (this.possibleValues.size() == 1) {
			return this.possibleValues.iterator().next();
		}
		return getDefaultValue();
	}

	@Override
	void validate() throws ValidationException {
		super.validate();
		if (validValue == null) {
			if (this.possibleValues.isEmpty()) {
				throw new ValidationException(
						"No possible values defined for parameter: " + this);
			}
			if (this.possibleValues.size() > 1
					&& Util.isEmpty(getDefaultValue())) {
				throw new ValidationException(
						"Multiple possible values are defined but no default value for parameter: "
								+ this);
			}
		} else {
			if (Util.isEmpty(getDefaultValue())) {
				throw new ValidationException(
						"Default value is not defined for numeric parameter! "
								+ this);
			}
			validValue.checkValue(getDefaultValue());
		}
	}

	boolean isValidValue(String value) {
		assert !Util.isEmpty(value);
		return Option.valueExist(value, getPossibleValues());
	}

	@Override
	public void setDefaultValue(String defaultVal)
			throws WrongParameterException {
		// If valid value constrain is not defined, then possible values must
		// be, and they must contain the value which is
		// about to be set!
		if (validValue == null) {
			if (getPossibleValues().isEmpty()) {
				throw new IllegalStateException(
						"Attempting to set default value for parameter: "
								+ this
								+ " Without possible values! Please define possible value first!");
			}
			if (!isValidValue(defaultVal)) {
				throw new WrongParameterException(
						"Attempting to set illegal value '" + defaultVal
								+ "' for the parameter: " + this);
			}
		} else {
			try {
				validValue.checkValue(defaultVal);
			} catch (IndexOutOfBoundsException e) {
				throw new WrongParameterException(
						"Attempting to set default value outside boundaries defined by the constraint: "
								+ validValue + "\n For parameter: " + this);
			}
		}
		this.defaultValue = defaultVal;
	}
}
