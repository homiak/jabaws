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

import java.util.List;

import compbio.metadata.Limit;
import compbio.metadata.LimitsManager;
import compbio.metadata.ResultNotAvailableException;

/**
 * Interface to a native executable.
 * 
 * @author pvtroshin
 * 
 * @param <T>
 */
public interface Executable<T> {

	enum ExecProvider {
		Local, Cluster, Any
	};

	/**
	 * Adds parameter to the list of parameters for a native executable
	 * 
	 * @param parameters
	 * @return this Executable
	 */
	Executable<T> addParameters(List<String> parameters);

	// TODO remove absolute
	@Deprecated
	List<String> getCreatedFiles();

	String getInput();

	String getOutput();

	String getError();

	CommandBuilder<T> getParameters(ExecProvider provider);

	<V> V getResults(String directory) throws ResultNotAvailableException;

	Executable<?> loadRunConfiguration(RunConfiguration rconfig);

	Limit<T> getLimit(String presetName);

	LimitsManager<T> getLimits();

	String getClusterJobSettings();

}
