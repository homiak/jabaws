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
import javax.xml.bind.annotation.XmlRootElement;

import compbio.util.Util;

/**
 * A collection of Limits
 * 
 * @see Limit
 * @author pvtroshin
 * 
 * @version 1.0 January 2010
 * @param <T>
 *            executable type
 */
@XmlRootElement(name = "limits")
@XmlAccessorType(XmlAccessType.FIELD)
public class LimitsManager<T> {

	String runnerClassName;
	List<Limit<T>> limit;

	/**
	 * 
	 * @return all limits defined for an executable T
	 */
	public List<Limit<T>> getLimits() {
		return limit;
	}

	@Override
	public String toString() {
		if (limit == null) {
			return "";
		}
		String value = "";
		for (Limit<T> lim : limit) {
			value += lim.toString();
		}
		return value;
	}

	/**
	 * 
	 * @param presetName
	 * @return Limit defined for the executable T and presetName. If no limit is
	 *         defined for the presetName then default Limit is returned. If
	 *         presetName is empty or null than the default Limit will be
	 *         returned. If not limit defined for the type T than NULL will be
	 *         returned
	 */
	public Limit<T> getLimitByName(String presetName) {
		if (limit == null) {
			return null;
		}
		if (Util.isEmpty(presetName)) {
			// return default limit
			return getDefaultLimit();
		}
		for (Limit<T> lim : limit) {
			String preset = lim.getPreset();
			if (preset == null) {
				continue;
			}
			if (preset.equalsIgnoreCase(presetName)) {
				return lim;
			}
		}
		return null;
	}

	/**
	 * 
	 * @return the default Limit for an executable type T
	 */
	public Limit<T> getDefaultLimit() {
		for (Limit<T> lim : limit) {
			if (lim.isDefault) {
				return lim;
			}
		}
		return null;
	}

	/**
	 * Validate Limits
	 * 
	 * @see Limit
	 * @see Preset
	 * @param presets
	 * @throws ValidationException
	 *             if any of the Limit defined is found to be invalid. That is
	 *             when
	 * 
	 *             1) No default limit is defined
	 * 
	 *             2) More than 1 default limit is defined
	 * 
	 *             3) Limit's preset name does not match any presets for type T
	 */
	public void validate(PresetManager<T> presets) throws ValidationException {
		int defaults = 0;
		for (Limit<T> lim : limit) {
			if (lim.isDefault) {
				defaults++;
			}
		}
		if (defaults == 0) {
			throw new ValidationException("Default limit is not set!");
		}
		if (defaults > 1) {
			throw new ValidationException(
					"Default limit is set more than once!");
		}
		if (presets != null) {
			for (Limit<T> lim : limit) {
				lim.validate();
				String presetName = lim.getPreset();
				// skip "special" preset
				if (presetName != null
						&& !presetName
								.equals(PresetManager.LOCAL_ENGINE_LIMIT_PRESET)) {
					Preset<T> preset = presets.getPresetByName(presetName);
					if (preset == null) {
						throw new ValidationException("Preset " + presetName
								+ " is not found!");
					}
				}
			}
		}
	}

}
