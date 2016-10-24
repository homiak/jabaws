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

import javax.xml.bind.ValidationException;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import compbio.util.Util;

/**
 * The type and the lower and upper boundaries for numerical value.
 * 
 * @author pvtroshin
 * 
 * @version 1.0 November 2009
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class ValueConstrain {

	public static enum Type {
		Integer, Float
	};

	@XmlElement(required = true)
	Type type;
	// These can contain different numeric types values, thus they are strings
	String max;
	String min;

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public Number getMax() {
		if (Util.isEmpty(max)) {
			return null;
		}
		switch (type) {
		case Float:
			return Double.parseDouble(max);
		case Integer:
			return Integer.parseInt(max);
		}
		// should not happened
		throw new RuntimeException("Type is undefined! ");
	}

	public void setMax(String max) {
		this.max = max;
	}

	public Number getMin() {
		if (Util.isEmpty(min)) {
			return null;
		}
		switch (type) {
		case Float:
			return Double.parseDouble(min);
		case Integer:
			return Integer.parseInt(min);
		}
		// should not happened
		throw new RuntimeException("Type is undefined! ");
	}

	public void setMin(String min) {
		this.min = min;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		ValueConstrain constr = null;
		if (obj instanceof ValueConstrain) {
			constr = (ValueConstrain) obj;
		} else {
			return false;
		}
		if (this.type != constr.type) {
			return false;
		}
		if (this.max != null && constr.max != null) {
			if (!this.max.equals(constr.max)) {
				return false;
			}
		} else {
			return false;
		}

		if (this.min != null && constr.min != null) {
			if (!this.min.equals(constr.min)) {
				return false;
			}
		} else {
			return false;
		}

		return true;
	}

	@Override
	public String toString() {
		String value = "Type: " + this.type + "\n";
		if (this.min != null) {
			value += "Min: " + this.min + "\n";
		}
		if (this.max != null) {
			value += "Max: " + this.max + "\n";
		}
		return value;
	}

	@Override
	public int hashCode() {
		int code = type.hashCode();
		if (this.max != null) {
			code *= this.max.hashCode();
		}
		if (this.min != null) {
			code *= this.min.hashCode();
		}
		return code;
	}

	boolean hasMinValue() {
		return !Util.isEmpty(min);
	}

	boolean hasMaxValue() {
		return !Util.isEmpty(max);
	}

	/**
	 * Validate that the value is within the constrain boundaries
	 * 
	 * @param value
	 * @throws IndexOutOfBoundsException
	 *             when the value is outside the defined boundaries
	 */
	void checkValue(String value) {
		switch (type) {
		case Float:
			float val = Float.parseFloat(value);
			if (getMin() != null
					&& new Float(this.getMin().floatValue()).compareTo(val) == 1) {
				throw new IndexOutOfBoundsException("Value '" + value
						+ "' is lower that minumim value '" + getMin() + "'");
			}
			if (getMax() != null
					&& new Float(this.getMax().floatValue()).compareTo(val) == -1) {
				throw new IndexOutOfBoundsException("Value '" + value
						+ "' is greater that maximum value '" + getMax() + "'");
			}
			break;
		case Integer:
			int ival = Integer.parseInt(value);
			if (getMin() != null
					&& new Integer(this.getMin().intValue()).compareTo(ival) == 1) {
				throw new IndexOutOfBoundsException("Value '" + value
						+ "' is lower that minumim value '" + getMin() + "'");
			}
			if (getMax() != null
					&& new Integer(this.getMax().intValue()).compareTo(ival) == -1) {
				throw new IndexOutOfBoundsException("Value '" + value
						+ "' is greater that maximum value '" + getMax() + "'");
			}
			break;
		}
	}

	/**
	 * Validate the ValueConstrain object. For the ValueConstrain object the
	 * type and at least one boundary has to be defined
	 * 
	 * @throws ValidationException
	 *             - if the type or no boundaries are defined
	 */
	void validate() throws ValidationException {
		if (this.type == null) {
			throw new ValidationException(
					"Type is not defined for ValueConstrain: " + this);
		}
		if (Util.isEmpty(min) && Util.isEmpty(max)) {
			throw new ValidationException(
					"Both boundaries (min and max) is undefined for ValueConstrain: "
							+ this);
		}
	}

}
