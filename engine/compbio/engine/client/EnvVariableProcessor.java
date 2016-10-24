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

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import org.apache.log4j.Logger;

import compbio.util.Util;

public class EnvVariableProcessor {

	/**
	 * Special variable keys Absolute path(s) will be merged with the content of
	 * the system PATH variable
	 */
	public static final String PATH = "PATH";

	private static Logger log = Logger.getLogger(EnvVariableProcessor.class);
	private static final String PROP_NAME_VALUE_SEPARATOR = "#";
	private static final String NEXT_ENV_PROPERTY_DELIMITER = ";";

	static boolean containsMultipleVariables(String property) {
		String[] arr = property.split(NEXT_ENV_PROPERTY_DELIMITER);
		if (arr.length == 0) {
			return false;
		}
		return true;
	}

	static String[] getEnvVariableList(String property) {
		property = property.trim(); 
		if (containsMultipleVariables(property)) {
			return property.split(NEXT_ENV_PROPERTY_DELIMITER);
		}
		return new String[]{property};
	}

	static String getEnvVariableName(String property) {
		return property.split(PROP_NAME_VALUE_SEPARATOR)[0];
	}

	static String getEnvVariableValue(String property) {
		String[] vars = property.split(PROP_NAME_VALUE_SEPARATOR);
		if (vars.length > 2) {
			throw new IllegalArgumentException(
					"Must be only one value per property! Make sure the property does not contain '"
							+ PROP_NAME_VALUE_SEPARATOR + "' symbol!");
		}
		if (vars.length == 0 || vars.length==1) {
			log.warn("Environmental variable '"+property+"' does not have a value!");
			return "";
		}
		return vars[1];
	}

	static boolean isValidEnvVariableProperty(String property) {
		if (property == null) {
			return false;
		}
		return property.contains(PROP_NAME_VALUE_SEPARATOR);
	}



	/*
	 * This is a horrible hack, but it only requires to simplify configuration
	 * for users, could be removed at any time if proved to be a burden to
	 * maintanance.
	 */
	private final static String mafft_binaries = "MAFFT_BINARIES";
	private final static String fasta4mafft = "FASTA_4_MAFFT";
	private final static String iupred_path = "IUPred_PATH";

	public static Map<String, String> getEnvVariables(String property,
			Class<?> clazz) {
		Map<String, String> vars = Util.getNewHashMap();
		if (!isValidEnvVariableProperty(property)) {
			return Collections.emptyMap();
		}
		for (String evar : getEnvVariableList(property)) {
			if (!isValidEnvVariableProperty(evar)) {
				log.error(clazz.getName()
						+ " environment variable is specified by is NOT VALID! Skipping. "
						+ "Valid format is propertyName"
						+ PROP_NAME_VALUE_SEPARATOR + "propertyValue. "
						+ "Given values is: " + evar);
			}
			evar=evar.trim(); 
			String varName = getEnvVariableName(evar);
			String varValue = getEnvVariableValue(evar);
			// absolutise local paths to mafft env variables
			if (!PathValidator.isAbsolutePath(varValue)) {
				varName = varName.trim();
				if (varName.equalsIgnoreCase(mafft_binaries)
						|| varName.equalsIgnoreCase(fasta4mafft)
						|| varName.equalsIgnoreCase(iupred_path)) {
					varValue = compbio.engine.client.Util
							.convertToAbsolute(varValue);
				}
			}
			vars.put(varName, varValue);
		}

		return vars;
	}
}
