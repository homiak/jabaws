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
package compbio.engine.local;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import org.apache.log4j.Logger;

import compbio.util.SysPrefs;
import compbio.util.annotation.ThreadSafe;

@ThreadSafe()
public class StreamGobbler implements Runnable {

	enum OutputType {
		OUTPUT, ERROR
	};

	private static final Logger log = Logger.getLogger(StreamGobbler.class);

	final private InputStream is;
	final private OutputStream os;
	final private OutputType type;

	StreamGobbler(InputStream is, OutputStream os, OutputType type) {
		this.is = is;
		this.os = os;
		this.type = type;
	}

	public void run() {

		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os));
		String line = null;
		try {
			while ((line = br.readLine()) != null && !Thread.interrupted()) {
				log.trace(type + ">" + line);
				writer.write(line + SysPrefs.newlinechar);
			}
			br.close();
			writer.close();
		} catch (IOException ioe) {
			log.error(ioe.getMessage(), ioe.getCause());
		} finally {
			closeSilently(br);
			closeSilently(writer);
		}
	}

	private static void closeSilently(Closeable stream) {
		if (stream != null) {
			try {
				stream.close();
			} catch (IOException e) {
				log.error(e.getLocalizedMessage(), e.getCause());
			}
		}
	}
}
