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
import static compbio.ws.client.Constraints.hostkey;
import static compbio.ws.client.Constraints.limitList;
import static compbio.ws.client.Constraints.listServices;
import static compbio.ws.client.Constraints.paramList;
import static compbio.ws.client.Constraints.presetList;
import static compbio.ws.client.Constraints.presetkey;
import static compbio.ws.client.Constraints.pseparator;
import static compbio.ws.client.Constraints.servicekey;
import static compbio.ws.client.Constraints.testKey;

class CmdHelper {

	/**
	 * Check whether presetList is set in the command line
	 * 
	 * @param cmd
	 *            command line options
	 * @return true if presetList is found, false otherwise
	 */
	static boolean listPresets(String[] cmd) {
		return keyFound(cmd, presetList);
	}

	/**
	 * Checks whether limitList parameter is in the command line
	 * 
	 * @param cmd
	 *            - command line options
	 * @return true if it is, false otherwise
	 */
	static boolean listLimits(String[] cmd) {
		return keyFound(cmd, limitList);
	}

	/**
	 * list available services
	 * 
	 * @param cmd
	 * @return
	 */
	static boolean listServices(String[] cmd) {
		return keyFound(cmd, listServices);
	}

	/**
	 * tests service
	 * 
	 * @param cmd
	 * @return
	 */
	static boolean testService(String[] cmd) {
		return keyFound(cmd, testKey);
	}

	/**
	 * Checks whether the key is in the command line
	 * 
	 * @param cmd
	 * @param key
	 * @return true if it is, false otherwise
	 */
	static boolean keyFound(String[] cmd, String key) {
		assert cmd != null && cmd.length > 0;
		assert key != null;
		for (int i = 0; i < cmd.length; i++) {
			String listPresets = cmd[i];
			if (listPresets.trim().equalsIgnoreCase(key)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Extracts preset name from the command line is any
	 * 
	 * @param cmd
	 *            command line options
	 * @return presetName or null if no presets is defined
	 */
	static String getPresetName(String[] cmd) {
		String preset = null;
		for (int i = 0; i < cmd.length; i++) {
			String presetPrm = cmd[i];
			if (presetPrm.trim().toLowerCase()
					.startsWith(presetkey + pseparator)) {
				preset = presetPrm.substring(presetPrm.indexOf(pseparator) + 1);
				break;
			}
		}
		return preset;
	}

	/**
	 * Extracts service name from the command line
	 * 
	 * @param cmd
	 *            command line options
	 * @return service name or null if it is not defined
	 */
	public static String getServiceName(String[] cmd) {
		for (int i = 0; i < cmd.length; i++) {
			String serv = cmd[i];
			if (serv.trim().toLowerCase().startsWith(servicekey + pseparator)) {
				return serv.substring(serv.indexOf(pseparator) + 1);
			}
		}
		return null;
	}

	/**
	 * Extracts host name from the command line
	 * 
	 * @param cmd
	 *            command line options
	 * @return host name or null if it is not defined
	 */
	public static String getHost(String[] cmd) {
		for (int i = 0; i < cmd.length; i++) {
			String host = cmd[i];
			if (host.trim().toLowerCase().startsWith(hostkey + pseparator)) {
				return host.substring(host.indexOf(pseparator) + 1);
			}
		}
		return null;
	}

	/**
	 * Searches the command line keys in the array of parameters
	 * 
	 * @param cmd
	 *            command line options
	 * @return true is the list of Parameters is requested, false otherwise
	 */
	static boolean listParameters(String[] cmd) {
		return keyFound(cmd, paramList);
	}

}
