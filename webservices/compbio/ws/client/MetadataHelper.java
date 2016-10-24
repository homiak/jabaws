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
package compbio.ws.client;

import static compbio.ws.client.Constraints.pseparator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import compbio.data.msa.Metadata;
import compbio.metadata.Limit;
import compbio.metadata.LimitsManager;
import compbio.metadata.Option;
import compbio.metadata.Preset;
import compbio.metadata.PresetManager;
import compbio.metadata.RunnerConfig;
import compbio.metadata.WrongParameterException;

public class MetadataHelper {

	/**
	 * Returns a list of options supported by web service
	 * 
	 * @param <T>
	 *            web service type
	 * @param msaws
	 *            web service proxy
	 * @return List of options supported by a web service
	 */
	static <T> List<Option<T>> getParametersList(Metadata<T> msaws) {
		assert msaws != null;
		RunnerConfig<T> config = msaws.getRunnerOptions();
		if (config == null) {
			return Collections.emptyList();
		}
		return config.getArguments();
	}

	/**
	 * Returns an objects from which the list of presets supported by web
	 * service <T> can be obtained
	 * 
	 * @param <T>
	 *            web service type
	 * @param msaws
	 *            web service proxy
	 * @return PresetManager, object which operates on presets
	 */
	static <T> PresetManager<T> getPresetList(Metadata<T> msaws) {
		assert msaws != null;
		PresetManager<T> presetman = msaws.getPresets();
		return presetman;
	}

	/**
	 * Returns a list of limits supported by web service Each limit correspond
	 * to a particular preset.
	 * 
	 * @param <T>
	 *            web service type
	 * @param msaws
	 *            web service proxy
	 * @return List of limits supported by a web service
	 */
	static <T> List<Limit<T>> getLimits(Metadata<T> msaws) {
		assert msaws != null;
		LimitsManager<T> lmanger = msaws.getLimits();

		return lmanger != null ? lmanger.getLimits() : null;
	}

	/**
	 * Returns {@code Preset} by its name
	 * 
	 * @see Preset
	 * @param <T>
	 * @param msaws
	 * @param presetName
	 * @return Return a Preset by its optionName
	 */
	static <T> Preset<T> getPreset(Metadata<T> msaws, String presetName) {
		assert presetName != null;
		PresetManager<T> presets = MetadataHelper.getPresetList(msaws);
		if (presets == null) {
			System.out
					.println("No presets are supported by the service! Ignoring -r directive!");
			return null;
		}
		Preset<T> pre = presets.getPresetByName(presetName);
		if (pre == null) {
			System.out.println("Cannot find preset: " + presetName
					+ " WARN: ignoring -r directive!");
		}
		return pre;
	}

	/**
	 * Converts options supplied via parameters file into {@code Option} objects
	 * 
	 * @param <T>
	 *            web service type
	 * @param params
	 * @param options
	 * @return List of Options of type T
	 */
	static <T> List<Option<T>> processParameters(List<String> params,
			RunnerConfig<T> options) {
		List<Option<T>> chosenOptions = new ArrayList<Option<T>>();
		for (String param : params) {
			String oname = null;
			if (isParameter(param)) {
				oname = getParamName(param);
			} else {
				oname = param;
			}
			Option<T> o = options.getArgumentByOptionName(oname);
			if (o == null) {
				System.out.println("WARN ignoring unsuppoted parameter: "
						+ oname);
				continue;
			}
			if (isParameter(param)) {
				try {
					o.setValue(getParamValue(param));
				} catch (WrongParameterException e) {
					System.out
							.println("Problem setting value for the parameter: "
									+ param);
					e.printStackTrace();
				}
			}
			chosenOptions.add(o);
		}
		return chosenOptions;
	}

	static String getParamName(String fullName) {
		assert isParameter(fullName);
		return fullName.substring(0, fullName.indexOf(pseparator));
	}

	static String getParamValue(String fullName) {
		assert isParameter(fullName);
		return fullName.substring(fullName.indexOf(pseparator) + 1);
	}

	static boolean isParameter(String param) {
		return param.contains(pseparator);
	}

}
