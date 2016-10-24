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

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import compbio.metadata.JobExecutionException;
import compbio.metadata.JobSubmissionException;
import compbio.metadata.ResultNotAvailableException;

public interface ConfiguredExecutable<T> extends Executable<T>,
		PipedExecutable<T> {

	Map<String, String> getEnvironment();

	String getTaskId();

	String getCommand(ExecProvider provider) throws JobSubmissionException;

	ExecProvider getSupportedRuntimes();

	String getWorkDirectory();

	void setWorkDirectory(String workDirectory);

	<V> V getResults() throws ResultNotAvailableException;

	Executable<T> getExecutable();

	CommandBuilder<T> getParameters();

	boolean saveRunConfiguration() throws IOException;

	ConfiguredExecutable<?> loadRunConfiguration(InputStream input)
			throws IOException;

	public ExecProvider getExecProvider();

}
