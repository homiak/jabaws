/* Copyright (c) 2011 Peter Troshin
 *  
 *  JAva Bioinformatics Analysis Web Services (JABAWS) @version: 2.0     
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

package compbio.runner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.log4j.Logger;

import compbio.engine.client.Executable;
import compbio.metadata.Option;
import compbio.metadata.Parameter;
import compbio.metadata.RunnerConfig;
import compbio.metadata.ValueConstrain;
import compbio.util.Util;
import compbio.util.annotation.Immutable;

/**
 * This class solve the following problems. Given the RunnerConfig
 * 
 * 1) generate a valid option string with all options and parameters in it
 * 
 * 2) Permute all possible combinations of options order and parameters values
 * 
 * 
 * @author pvtroshin
 * 
 */
@Immutable
public final class OptionCombinator {

    private static Logger log = Logger.getLogger(OptionCombinator.class);
    private static final String MINVALUE = "-100";
    private static final String MAXVALUE = "100";

    private static final Random rand = new Random();
    private final RunnerConfig<? extends Executable<?>> rconfig;

    public OptionCombinator(RunnerConfig<? extends Executable<?>> rconfig) {
	if (rconfig == null) {
	    throw new IllegalArgumentException("RunnerConfig must be provided!");
	}
	this.rconfig = rconfig;
    }

    public List<String> optionsToCommandString(List<Option<?>> options) {
	List<String> strOptions = new ArrayList<String>();
	for (Option<?> option : options) {
	    strOptions.add(optionToCommandString(option));
	}
	return strOptions;
    }

    public List<String> getOptionsAtRandom() {
	return optionsToCommandString(getAllOptions());
    }

    public List<Option<?>> getAllOptions() {
	return new ArrayList<Option<?>>(rconfig.getOptions());
    }

    static String optionToCommandString(Option<?> option) {
	int size = option.getOptionNames().size();
	int idx = rand.nextInt(size);
	return option.getOptionNames().get(idx);
    }

    public List<Parameter<?>> getAllParameters() {
	List<Parameter<?>> args = new ArrayList<Parameter<?>>();
	for (Parameter<?> prm : rconfig.getParameters()) {
	    // Make sure there is a default value linked to the parameter to use
	    // later
	    // prm.setDefaultValue(getValue(prm));
	    args.add(prm);
	}
	return args;
    }

    public List<String> argumentsToCommandString(
	    List<? extends Option<?>> arguments) {
	return argumentsToCommandString(arguments, rconfig);
    }

    public static List<String> argumentsToCommandString(
	    List<? extends Option<?>> arguments,
	    RunnerConfig<? extends Executable<?>> rconfig) {

	if (arguments == null || rconfig == null) {
	    throw new NullPointerException(
		    "Arguments and RunnerConfig objects must be provided!");
	}

	List<String> command = new ArrayList<String>();
	for (Option<?> option : arguments) {

	    if (option instanceof Parameter<?>) {
		Parameter<?> prm = (Parameter<?>) option;
		command.add(prm.getOptionName() + rconfig.getPrmSeparator()
			+ getValue(prm));
		log.trace("Setting parameter " + prm);
	    } else {
		log.trace("Setting option " + option);
		command.add(optionToCommandString(option));
	    }
	}
	return command;
    }

    public List<String> parametersToCommandString(
	    List<Parameter<?>> orderedList, Map<Parameter<?>, String> prmValue) {
	List<String> args = new ArrayList<String>();
	for (Parameter<?> param : orderedList) {
	    args.add(param.getOptionName() + rconfig.getPrmSeparator()
		    + prmValue.get(param));
	}
	return args;
    }

    public Map<Parameter<?>, String> getAllConstrainedParametersWithBorderValues(
	    boolean minValue) {
	Map<Parameter<?>, String> paramValue = new HashMap<Parameter<?>, String>();
	for (Parameter<?> prm : rconfig.getParameters()) {
	    ValueConstrain vc = prm.getValidValue();
	    if (vc == null) {
		continue;
	    }
	    String value = "";
	    if (minValue) {
		value = getLowBorderValue(prm);
	    } else {
		value = getUpperBorderValue(prm);
	    }
	    paramValue.put(prm, value);
	}
	return paramValue;
    }

    public Map<Parameter<?>, String> getAllConstrainedParametersWithRandomValues() {
	Map<Parameter<?>, String> paramValue = new HashMap<Parameter<?>, String>();
	for (Parameter<?> prm : rconfig.getParameters()) {
	    ValueConstrain vc = prm.getValidValue();
	    if (vc == null) {
		continue;
	    }
	    paramValue.put(prm, getRandomValue(prm));
	}
	return paramValue;
    }

    String getLowBorderValue(Parameter<?> param) {
	assert param != null;
	ValueConstrain vc = param.getValidValue();
	Number minVal = vc.getMin();
	return minVal == null ? MINVALUE : minVal.toString();
    }

    String getUpperBorderValue(Parameter<?> param) {
	assert param != null;
	ValueConstrain vc = param.getValidValue();
	Number maxVal = vc.getMax();
	return maxVal == null ? MAXVALUE : maxVal.toString();
    }

    String getRandomValue(Parameter<?> param) {
	assert param != null;
	String low = getLowBorderValue(param);
	String high = getUpperBorderValue(param);
	ValueConstrain vc = param.getValidValue();

	if (vc.getType() == ValueConstrain.Type.Float) {
	    return new Double(Util.getRandomNumber(Double.parseDouble(low),
		    Double.parseDouble(high))).toString();
	}
	return new Integer(Util.getRandomNumber(Integer.parseInt(low), Integer
		.parseInt(high))).toString();
    }

    static String getValue(Parameter<?> param) {
	assert param != null;
	if (param.getDefaultValue() != null) {
	    log.trace("Returning default value: " + param.getDefaultValue());
	    return param.getDefaultValue();
	} else {
	    // Some parameters do not have default values e.g. seq type - nuc or
	    // protein
	    List<String> passValues = param.getPossibleValues();
	    int idx = rand.nextInt(passValues.size());
	    log.trace("Returning random value: " + passValues.get(idx));
	    return passValues.get(idx);
	}
    }
}
