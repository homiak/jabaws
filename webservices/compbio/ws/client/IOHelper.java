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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import compbio.data.sequence.Alignment;
import compbio.data.sequence.ClustalAlignmentUtil;
import compbio.data.sequence.ScoreManager;

public class IOHelper {

	/**
	 * Checks -i options and return the File if one was provided, null otherwise
	 * 
	 * @param cmd
	 * @param key
	 * @param mustExist
	 * @return
	 * @throws IOException
	 */
	static File getFile(String[] cmd, String key, boolean mustExist)
			throws IOException {
		assert key != null && key.trim().length() != 0;
		for (int i = 0; i < cmd.length; i++) {
			String filename = cmd[i];
			filename = filename.trim();
			if (filename.toLowerCase().startsWith(key + pseparator)) {
				filename = filename.substring((key + pseparator).length());
				File file = new File(filename);
				if (mustExist && !file.exists()) {
					System.out.println(key + " file " + file.getAbsolutePath()
							+ " does not exist");
					return null;
				}
				if (!mustExist && !file.exists()) {
					file.createNewFile();
				}
				if (!file.canRead()) {
					System.out.println("Cannot read " + key + " file "
							+ file.getAbsolutePath());
					return null;
				}
				return file;
			}
		}
		return null;
	}

	/**
	 * Load parameters from file
	 * 
	 * @throws IOException
	 */
	static List<String> loadParameters(File paramsfile) throws IOException {
		assert paramsfile != null && paramsfile.exists();
		BufferedReader reader = new BufferedReader(new FileReader(paramsfile));
		String line = null;
		ArrayList<String> params = new ArrayList<String>();
		while ((line = reader.readLine()) != null) {
			line = line.trim();
			if (line.length() == 0)
				continue;
			params.add(line);
		}
		return params;
	}

	static Writer getWriter(File file) throws IOException {
		assert file != null && file.exists();
		return new FileWriter(file);
	}

	/**
	 * Outputs clustal formatted alignment into the file represented by the
	 * outStream
	 * 
	 * @param outStream
	 * @param align
	 *            the alignment to output
	 */
	static void writeOut(Writer writer, Alignment align) {
		try {
			ClustalAlignmentUtil.writeClustalAlignment(writer, align);
		} catch (IOException e) {
			System.err
					.println("Problems writing output file! Stack trace is below: ");
			e.printStackTrace();
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException ignored) {
					// e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Outputs AAcon results into the file represented by the outStream
	 * 
	 * @param outStream
	 * @param result
	 *            the AACon scores to output
	 */
	static void writeOut(Writer writer, ScoreManager results) {
		if (results == null) {
			return;
		}
		try {
			results.writeOut(writer);
		} catch (IOException e) {
			System.err
					.println("Problems writing output file! Stack trace is below: ");
			e.printStackTrace();
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException ignored) {
					// e.printStackTrace();
				}
			}
		}
	}

}
