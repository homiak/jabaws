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
package compbio.ws.server;

import java.io.File;

import javax.jws.WebService;

import org.apache.log4j.Logger;

import compbio.data.msa.JABAService;
import compbio.data.msa.SequenceAnnotation;
import compbio.engine.Configurator;
import compbio.metadata.ChunkHolder;
import compbio.runner.conservation.AACon;
import compbio.runner.disorder.Jronn;

@WebService(endpointInterface = "compbio.data.msa.SequenceAnnotation", targetNamespace = JABAService.V2_SERVICE_NAMESPACE, serviceName = "JronnWS")
public class JronnWS extends SequenceAnnotationService<Jronn>
		implements
			SequenceAnnotation<Jronn> {

	private static Logger log = Logger.getLogger(JronnWS.class);

	public JronnWS() {
		super(new Jronn(), log);
	}

	@Override
	public ChunkHolder pullExecStatistics(String jobId, long position) {
		WSUtil.validateJobId(jobId);
		String file = Configurator.getWorkDirectory(jobId) + File.separator
				+ AACon.getStatFile();
		return WSUtil.pullFile(file, position);
	}

}
