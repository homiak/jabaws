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

/**
 * This is a marker interface, contains no methods
 * 
 * @author pvtroshin
 * 
 */
public interface JABAService {

	// JABAWS version 1.0 service name
	static final String SERVICE_NAMESPACE = "http://msa.data.compbio/01/01/2010/";
	// JABAWS version 2.0 service name
	static final String V2_SERVICE_NAMESPACE = "http://msa.data.compbio/01/12/2010/";

}
