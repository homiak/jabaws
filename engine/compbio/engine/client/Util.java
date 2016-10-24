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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.Map;

import org.apache.log4j.Logger;

import compbio.engine.client.Executable.ExecProvider;
import compbio.engine.conf.DirectoryManager;
import compbio.engine.conf.PropertyHelperManager;
import compbio.metadata.JobStatus;
import compbio.metadata.LimitsManager;
import compbio.metadata.ResultNotAvailableException;
import compbio.util.FileUtil;
import compbio.util.PropertyHelper;
import compbio.util.SysPrefs;

public final class Util {

	private static final PropertyHelper ph = PropertyHelperManager
			.getPropertyHelper();

	private static final Logger log = Logger.getLogger(Util.class);

	public static boolean isValidJobId(final String key) {
		if (compbio.util.Util.isEmpty(key)) {
			return false;
		}
		int delIdx = key.indexOf(DirectoryManager.DELIM);
		if (delIdx < 0) {
			return false;
		}
		String id = key.substring(delIdx + DirectoryManager.DELIM.length());
		try {
			Long.parseLong(id);
		} catch (NumberFormatException e) {
			log.debug("Invalid key! " + e.getLocalizedMessage());
			return false;
		}
		return true;
	}

	public static void writeStatFile(String workDirectory,
			String fileAndEventName) {
		// never override old stat files!
		// Work directory could be null for cancelled or incomplete jobs, just
		// ignore
		if (!compbio.util.Util.isEmpty(workDirectory)) {
			writeFile(workDirectory, fileAndEventName,
					new Long(System.currentTimeMillis()).toString(), false);
		}
	}

	public static void writeFile(String workDirectory, String fileAndEventName,
			String content, boolean override) {
		File file = null;
		if (compbio.util.Util.isEmpty(workDirectory)) {
			log.debug("Calling compbio.engine.Util.writeFile() with not work directory."
					+ " Skipping writing statistics!");
			return;
		}
		assert !compbio.util.Util.isEmpty(content) : "Content expected!";
		FileWriter writer = null;
		try {
			file = new File(workDirectory, fileAndEventName);
			// Do not override existing files unless asked to do so !
			if (file.exists() && !override) {
				return;
			}
			writer = new FileWriter(file);
			writer.write(content);
			writer.close();
			log.debug("File " + fileAndEventName + " with content: " + content
					+ " has been recorder successfully! ");
		} catch (IOException e) {
			log.error("Could not record the " + fileAndEventName + " file in "
					+ workDirectory + " for local execution! Ignoring... "
					+ e.getMessage());
		} finally {
			FileUtil.closeSilently(log, writer);
		}
	}

	public static final boolean writeMarker(String workDirectory,
			JobStatus fileType) {
		if (fileType == null) {
			throw new NullPointerException("MarkerType must be provided!");
		}
		if (fileType == fileType.FINISHED || fileType == fileType.STARTED) {
			throw new IllegalArgumentException(
					"Please use Util.writeStatFile(workDirectory, fileAndEventName) to record FINISHED and STARTED statuses!");
		}
		if (!PathValidator.isValidDirectory(workDirectory)) {
			// This is OK as some task could be cancelled even before they
			// started
			log.warn("Attempting to write " + fileType
					+ " marker in the work directory " + workDirectory
					+ " is not provided or does not exist!");
			return false;
		}
		try {
			File sfile = new File(workDirectory, fileType.toString());
			if (!sfile.exists()) {
				return sfile.createNewFile();
			}
		} catch (IOException e) {
			log.error(
					"Could not record stat marker file " + fileType
							+ " into the directory " + workDirectory + " ! "
							+ e.getMessage(), e.getCause());
		}
		return false;
	}

	public static boolean isMarked(String workDirectory, JobStatus marker) {
		if (!PathValidator.isValidDirectory(workDirectory)) {
			throw new NullPointerException("Work directory " + workDirectory
					+ " is not provided or does not exist!");
		}
		return new File(workDirectory, marker.toString()).exists();
	}

	public static Map<String, String> mergeEnvVariables(
			final Map<String, String> sysEnvTobeModified,
			final Map<String, String> variables) {
		if (variables.containsKey(EnvVariableProcessor.PATH)) {
			String propPath = variables.get(EnvVariableProcessor.PATH);
			String sysPATH = sysEnvTobeModified.get(EnvVariableProcessor.PATH);
			String syspath = sysEnvTobeModified.get(EnvVariableProcessor.PATH
					.toLowerCase());
			// This version appears surprisingly often on windows machines
			boolean added = false;
			String sysPath = sysEnvTobeModified.get("Path");
			if (sysPATH != null) {
				sysEnvTobeModified.put(EnvVariableProcessor.PATH, sysPATH
						+ File.pathSeparator + propPath);
				added = true;
			}
			if (syspath != null) {
				sysEnvTobeModified.put(EnvVariableProcessor.PATH.toLowerCase(),
						syspath + File.pathSeparator + propPath);
				added = true;
			}
			if (sysPath != null) {
				sysEnvTobeModified.put("Path", sysPath + File.pathSeparator
						+ propPath);
				added = true;
			}
			// If not path variable is found, then add it
			if (!added) {
				sysEnvTobeModified.put(EnvVariableProcessor.PATH, propPath);
			}
			variables.remove(EnvVariableProcessor.PATH);
		}
		sysEnvTobeModified.putAll(variables);
		return sysEnvTobeModified;
	}

	public static String convertToAbsolute(String relativePath) {
		// If specified path is relative, than make it absolute
		String absolute = relativePath;
		if (!PathValidator.isAbsolutePath(relativePath)) {
			absolute = PropertyHelperManager.getLocalPath() + relativePath;
			Util.log.trace("Changing local path in enviromental variable to absolute: FROM "
					+ relativePath + " TO " + absolute);
		}
		return absolute;
	}

	public static String getExecProperty(String propertySpec, Executable<?> exec) {
		assert !compbio.util.Util.isEmpty(propertySpec);
		assert exec != null;
		return Util.getExecProperty(propertySpec, exec.getClass());
	}

	public static String getExecProperty(String propertySpec, Class<?> clazz) {
		assert !compbio.util.Util.isEmpty(propertySpec);
		assert clazz != null;
		String property = clazz.getSimpleName().toLowerCase() + "."
				+ propertySpec.toLowerCase();
		log.trace("Processing property: " + property);
		return ph.getProperty(property);
	}

	public static String getFullPath(String workDirectory, String fileName) {
		assert !compbio.util.Util.isEmpty(fileName) : "Filename must be provided! ";
		assert !compbio.util.Util.isEmpty(workDirectory) : "Workdirectory must be provided! ";
		return workDirectory + File.separator + fileName;
	}

	public static String getCommand(ExecProvider provider, Class<?> clazz) {
		if (provider == ExecProvider.Any) {
			throw new IllegalArgumentException(
					"A particular execution environment must be chosen");
		}
		String execCommandName = clazz.getSimpleName().toLowerCase();
		String bin = "";
		if (provider == ExecProvider.Local) {
			if (SysPrefs.isWindows) {
				bin = ph.getProperty("local." + execCommandName
						+ ".bin.windows");
			} else {
				bin = ph.getProperty("local." + execCommandName + ".bin");
			}
			// For executable Jar files the location of Java executable is not
			// required for local execution. If it is not provided, JABAWS will
			// attempt to use Java from JAVA_HOME env variable
			if (isJavaLibrary(clazz)) {
				if (compbio.util.Util.isEmpty(bin)) {
					bin = getJava();
				}
			}
			// If path to executable defined in the properties is not absolute,
			// then make it so
			// as setting working directory of ProcessBuilder will make it
			// impossible
			// to find an executable otherwise
			if (!compbio.util.Util.isEmpty(bin)
					&& !PathValidator.isAbsolutePath(bin)) {
				bin = bin.trim();
				if (bin.equalsIgnoreCase("java")
						|| bin.equalsIgnoreCase("java.exe")) {
					// do not make path absolute to the java executable if
					// relative path is provided. Java executable is not a part
					// of JABAWS distribution!
				} else {
					bin = PropertyHelperManager.getLocalPath() + bin;
				}
			}
		} else {
			bin = ph.getProperty("cluster." + execCommandName + ".bin");
		}
		// Could have done: Set executable flag if not set
		// but - do not because in some cases more than one file must be made
		// executable!
		/*
		 * if (!compbio.util.Util.isEmpty(bin)) { File command = new File(bin);
		 * if (!command.canExecute()) { log.debug(
		 * "The command line binary is not executable! (just unpacked from war file? )"
		 * ); log.debug("Attempting to set executable flag for command: " +
		 * bin); command.setExecutable(true, false); } }
		 */
		log.debug("Using executable: " + bin);
		return bin; // File.separator
	}
	/**
	 * Returns true of executableName.jar.file property has some value in the
	 * Executable.properties file, false otherwise.
	 * 
	 * @param clazz
	 * @return
	 */
	public static boolean isJavaLibrary(Class<?> clazz) {
		String execCommandName = clazz.getSimpleName().toLowerCase();
		String java_lib = ph.getProperty(execCommandName + ".jar.file");
		return !compbio.util.Util.isEmpty(java_lib);
	}

	/**
	 * Returns the absolute path to the Java executable from JAVA_HOME
	 * 
	 * @return returns the absolute path to the Java executable from JAVA_HOME
	 */
	public static String getJava() {
		String javahome = System.getProperty("java.home");
		if (compbio.util.Util.isEmpty(javahome)) {
			javahome = System.getenv("JAVA_HOME");
		}
		if (compbio.util.Util.isEmpty(javahome)) {
			log.warn("Cannot find Java in java.home system property "
					+ "or JAVA_HOME environment variable! ");
			return null;
		}
		File jh = new File(javahome);
		if (jh.exists() && jh.isDirectory()) {
			String java = javahome + File.separator + "bin" + File.separator
					+ "java";
			if (SysPrefs.isWindows) {
				java += ".exe";
			}
			File jexe = new File(java);
			if (jexe.exists() && jexe.isFile() && jexe.canExecute()) {
				log.info("Using Java from: " + jexe.getAbsolutePath());
				return jexe.getAbsolutePath();
			} else {
				log.warn("Cannot find java executable in the JAVA_HOME!");
			}
		} else {
			log.warn("JAVA_HOME does not seems to point to a valid directory! Value: "
					+ javahome);
		}
		return null;
	}

	public static ExecProvider getSupportedRuntimes(Class<?> clazz) {
		boolean localRuntimeSupport = false;
		boolean clusterRuntimeSupport = false;
		String executableName = clazz.getSimpleName().toLowerCase();
		String localRuntime1 = ph.getProperty("local." + executableName
				+ ".bin.windows");
		String localRuntime2 = ph.getProperty("local." + executableName
				+ ".bin");
		if (!compbio.util.Util.isEmpty(localRuntime1)
				|| !compbio.util.Util.isEmpty(localRuntime2)) {
			localRuntimeSupport = true;
		} else {
			localRuntimeSupport = isJavaLibrary(clazz) && getJava() != null;
		}

		String clusterRuntime = ph.getProperty("cluster." + executableName
				+ ".bin");
		if (!compbio.util.Util.isEmpty(clusterRuntime)) {
			clusterRuntimeSupport = true;
		}
		if (localRuntimeSupport && clusterRuntimeSupport) {
			return ExecProvider.Any;
		} else if (localRuntimeSupport) {
			return ExecProvider.Local;
		} else if (clusterRuntimeSupport) {
			return ExecProvider.Cluster;
		}
		// Means executable cannot be executed -> is improperly configured
		// should be ignored
		throw new InvalidParameterException(
				"Executable is not provided for any runtime environments");
	}
	public static ConfiguredExecutable<?> loadExecutable(String taskId)
			throws ResultNotAvailableException {
		String workDir = compbio.engine.Configurator.getWorkDirectory(taskId);
		// The results for this job has been collected once, or the JVM may
		// have been restarted,
		// so that the job is not in the job list
		// ->load a ConfiguredExercutable from saved run and return it
		FileInputStream fileInStream = null;
		ConfiguredExecutable<?> exec = null;
		try {
			fileInStream = new FileInputStream(workDir + File.separator
					+ RunConfiguration.rconfigFile);
			RunConfiguration rconf = RunConfiguration.load(fileInStream);
			exec = ConfExecutable.newConfExecutable(rconf);
			fileInStream.close();
		} catch (FileNotFoundException e) {
			log.error(
					"Could not find run configuration to load!"
							+ e.getLocalizedMessage(), e.getCause());
			throw new ResultNotAvailableException(
					"Could not find run configuration to load!"
							+ e.getMessage(), e.getCause());
		} catch (IOException e) {
			log.error(
					"IO Exception while reading run configuration file!"
							+ e.getLocalizedMessage(), e.getCause());
			throw new ResultNotAvailableException(
					"Could not load run configuration!" + e.getMessage(),
					e.getCause());
		} finally {
			FileUtil.closeSilently(log, fileInStream);
		}
		return exec;
	}

	/**
	 * For now just assume that all parameters which came in needs setting it
	 * will be a client responsibility to prepare RunnerConfig object then
	 * 
	 * @param rconfig
	 * @return
	 * 
	 *         public static List<String> toOptionString(RunnerConfig<?>
	 *         rconfig) { String option = ""; List<String> options = new
	 *         ArrayList<String>(); for (Parameter<?> par :
	 *         rconfig.getParameters()) { if (par.getPossibleValues().isEmpty())
	 *         { option = par.getOptionName(); } else { option =
	 *         par.getOptionName() + "=" + par.getPossibleValues().get(0); } //
	 *         separate options options.add(option); } return options; }
	 */

	public static <T> LimitsManager<T> getLimits(Class<T> clazz) {
		LimitsManager<T> limits = null;
		try {
			limits = ConfExecutable.getRunnerLimits(clazz);
		} catch (FileNotFoundException e) {
			Util.log.warn("No limits are found for " + clazz + " executable! "
					+ e.getLocalizedMessage(), e.getCause());
			// its ok, limit may not be initialized
		} catch (IOException e) {
			Util.log.warn("IO exception while attempting to read limits for "
					+ clazz + " executable! " + e.getLocalizedMessage(),
					e.getCause());
		}
		return limits;
	}

}
