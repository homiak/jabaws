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

package compbio.engine;

import java.util.List;

import org.apache.log4j.Logger;

import compbio.data.sequence.FastaSequence;
import compbio.engine.client.Executable;
import compbio.engine.local.LocalExecutorService;
import compbio.metadata.Limit;
import compbio.metadata.PresetManager;

/**
 * This class decides where to execute the job. If the local engine is enabled
 * in the configuration file and it has free threads and the size of the tasks
 * permits the local execution, then the local execution will be favoured.
 * 
 * @author pvtroshin
 * @version 1.0 March 2009
 */
public class LoadBalancer {

	private static Logger log = Logger.getLogger(LoadBalancer.class);

	private LoadBalancer() {
	}

	public static Executable.ExecProvider getEngine(Executable<?> executable) {
		if (LocalExecutorService.getExecutor().canAcceptMoreWork()) {
			log.debug("LOCAL engine HAS FREE threads will execute ... ");
			return Executable.ExecProvider.Local;
		}
		log.debug("NO free threads on the LOCAL engine! Targeting for CLUSTER execution... ");
		return Executable.ExecProvider.Cluster;
	}

	public static <T, V> Executable.ExecProvider getEngine(
			Executable<V> executable, List<FastaSequence> dataSet) {

		// If data set is deemed too big for local execution, than give a
		// cluster engine
		// If limit is not defined then defaults to executing on the cluster
		Limit<V> limit = executable
				.getLimit(PresetManager.LOCAL_ENGINE_LIMIT_PRESET);
		log.trace("Inspecting whether the job can be executed locally using limit: "
				+ limit);
		if (limit == null || limit.isExceeded(dataSet)) {
			log.debug("Job EXCEEDS LOCAL execution LIMIT targeting for cluster execution! ");
			return Executable.ExecProvider.Cluster;
		}
		log.debug("Job FITS into the LOCAL execution limit consulting load balancer... ");
		// Even if the data satisfies criteria for local execution it may still
		// be executed on the cluster as the maximum capacity for local engine
		// may be reached.
		return getEngine(executable);
	}

}
