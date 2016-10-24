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

import javax.jws.WebService;
import javax.xml.ws.Endpoint;

/**
 * This class publish a web service. This is not a production implementation.
 * Should be used only for during development and testing
 * 
 * @author pvtroshin
 * @date October 2009
 */
@Deprecated
public class SimpleWSPublisher {

	public SimpleWSPublisher(Object exec, String context) {

	}

	public static void main(String[] args) {
		if (args.length != 2) {
			System.out.println("Usage: ");
			System.out.println("SimpleWSPublisher context class");
			System.out
					.println("where context is a context name, the name after the address by "
							+ "which web service can be called e.g. Clustal "
							+ " and class is a web service implementation class");

		}
		String context = args[0];
		String clazzName = args[1];
		try {
			Class<?> clazz = Class.forName(clazzName);
			if (!clazz.isAnnotationPresent(WebService.class)) {
				System.out.println("Can only start web services. "
						+ "Please check whether the class provided is "
						+ "annotated with Webservice annotation");
				System.exit(1);
			}
			Endpoint.publish("http://127.0.0.1:7979/" + context,
					clazz.newInstance());
		} catch (InstantiationException e) {
			e.printStackTrace();
			System.exit(1);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			System.exit(1);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			System.exit(1);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
}
