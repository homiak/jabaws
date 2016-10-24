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
package compbio.data.msa;

import javax.jws.WebParam;

import compbio.metadata.Limit;
import compbio.metadata.LimitsManager;
import compbio.metadata.PresetManager;
import compbio.metadata.RunnerConfig;

public interface Metadata<T> {

	/**
	 * Get options supported by a web service
	 * 
	 * @return RunnerConfig the list of options and parameters supported by a
	 *         web service.
	 */
	RunnerConfig<T> getRunnerOptions();

	/**
	 * Get presets supported by a web service
	 * 
	 * @return PresetManager the object contains information about presets
	 *         supported by a web service
	 */
	PresetManager<T> getPresets();

	/**
	 * Get a Limit for a preset.
	 * 
	 * @param presetName
	 *            the name of the preset. if no name is provided, then the
	 *            default preset is returned. If no limit for a particular
	 *            preset is defined then the default preset is returned
	 * @return Limit
	 */
	Limit<T> getLimit(@WebParam(name = "presetName") String presetName);

	/**
	 * List Limits supported by a web service.
	 * 
	 * @return LimitManager
	 */
	LimitsManager<T> getLimits();

}
