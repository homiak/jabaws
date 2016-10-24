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

import static compbio.ws.client.Constraints.inputkey;
import static compbio.ws.client.Constraints.outputkey;
import static compbio.ws.client.Constraints.paramFile;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceException;

import compbio.data.msa.JABAService;
import compbio.data.msa.Metadata;
import compbio.data.msa.MsaWS;
import compbio.data.msa.RegistryWS;
import compbio.data.msa.SequenceAnnotation;
import compbio.data.sequence.Alignment;
import compbio.data.sequence.FastaSequence;
import compbio.data.sequence.ScoreManager;
import compbio.data.sequence.SequenceUtil;
import compbio.data.sequence.UnknownFileFormatException;
import compbio.metadata.JobSubmissionException;
import compbio.metadata.Option;
import compbio.metadata.Preset;
import compbio.metadata.ResultNotAvailableException;
import compbio.metadata.WrongParameterException;
import compbio.util.FileUtil;

/**
 * A command line client for JAva Bioinformatics Analysis Web Services
 * 
 * @author pvtroshin
 * @version 1.0
 */
public class Jws2Client {

	/*
	 * Use java.util.Logger instead of log4j logger to reduce the size of the
	 * client package
	 */
	private static final Logger log = Logger.getLogger(Jws2Client.class
			.getCanonicalName());

	/**
	 * Attempt to construct the URL object from the string
	 * 
	 * @param urlstr
	 * @return true if it succeed false otherwise
	 */
	public static boolean validURL(String urlstr) {
		try {
			if (urlstr == null || urlstr.trim().length() == 0) {
				return false;
			}
			new URL(urlstr);
		} catch (MalformedURLException e) {
			return false;
		}
		return true;
	}

	/**
	 * Connects to the service and do the job as requested, if something goes
	 * wrong reports or/and prints usage help.
	 * 
	 * @param <T>
	 *            web service type
	 * @param cmd
	 *            command line options
	 * @throws IOException
	 */
	<T> Jws2Client(String[] cmd) throws IOException {

		String hostname = CmdHelper.getHost(cmd);
		if (hostname == null) {
			System.err.println("Host name is not provided!");
			printUsage(1);
		}

		if (!validURL(hostname)) {
			System.err.println("Host name is not valid!");
			printUsage(1);
		}
		// Just list available services and quit
		boolean listServices = CmdHelper.listServices(cmd);
		if (listServices) {
			listServices(hostname);
			System.exit(0);
		}

		String serviceName = CmdHelper.getServiceName(cmd);
		if (serviceName == null) {
			System.err.println("Service name is no provided!");
			printUsage(1);
		}
		Services service = Services.getService(serviceName);
		if (service == null) {
			System.err.println("Service " + serviceName
					+ " is no recognized! Valid values are: "
					+ Arrays.toString(Services.values()));
			printUsage(1);
		}
		// Test service and quit
		boolean testService = CmdHelper.testService(cmd);
		if (testService) {
			testService(hostname, service, new PrintWriter(System.out, true));
			System.exit(0);
		}

		File inputFile = IOHelper.getFile(cmd, inputkey, true);
		File outFile = IOHelper.getFile(cmd, outputkey, false);
		File parametersFile = IOHelper.getFile(cmd, paramFile, true);
		String presetName = CmdHelper.getPresetName(cmd);

		Metadata<T> msaws = (Metadata<T>) connect(hostname, service);
		Preset<T> preset = null;
		if (presetName != null) {
			preset = MetadataHelper.getPreset(msaws, presetName);
		}
		List<Option<T>> customOptions = null;
		if (parametersFile != null) {
			List<String> prms = IOHelper.loadParameters(parametersFile);
			customOptions = MetadataHelper.processParameters(prms,
					msaws.getRunnerOptions());
		}
		Alignment alignment = null;
		if (inputFile != null) {
			Writer writer = null;
			if (outFile != null) {
				writer = IOHelper.getWriter(outFile);
			} else {
				// this stream is going to be closed later which is fine as
				// std.out will not be
				writer = new PrintWriter(System.out, true);
			}
			if (service.getServiceType() == SequenceAnnotation.class) {
				ScoreManager result = analize(inputFile,
						((SequenceAnnotation<T>) msaws), preset, customOptions);

				IOHelper.writeOut(writer, result);
			} else {
				alignment = align(inputFile, (MsaWS<T>) msaws, preset,
						customOptions);
				IOHelper.writeOut(writer, alignment);
			}
			writer.close();
		}

		boolean listParameters = CmdHelper.listParameters(cmd);
		if (listParameters) {
			System.out.println(MetadataHelper.getParametersList(msaws));
		}
		boolean listPreset = CmdHelper.listPresets(cmd);
		if (listPreset) {
			System.out.println(MetadataHelper.getPresetList(msaws));
		}
		boolean listLimits = CmdHelper.listLimits(cmd);
		if (listLimits) {
			System.out.println(MetadataHelper.getLimits(msaws));
		}
		log.fine("Disconnecting...");
		((Closeable) msaws).close();
		log.fine("Disconnected successfully!");
	}

	/**
	 * Asks registry to test the service on the host hostname
	 * 
	 * @param hostname
	 * @param service
	 * @param writer
	 * @throws ConnectException
	 * @throws WebServiceException
	 */
	public static void testService(String hostname, Services service,
			PrintWriter writer) throws ConnectException, WebServiceException {
		RegistryWS registry = connectToRegistry(hostname);
		if (registry != null) {
			String message = registry.testService(service);
			writer.println("Service " + service + " testing results: ");
			writer.println(message);
			FileUtil.closeSilently(((Closeable) registry));
		}
		writer.flush();
	}

	public static Set<Services> getServices(String hostname)
			throws WebServiceException, ConnectException {
		RegistryWS registry = connectToRegistry(hostname);
		Set<Services> services = Collections.EMPTY_SET;
		if (registry != null) {
			services = registry.getSupportedServices();
			FileUtil.closeSilently(((Closeable) registry));
		}
		return services;
	}

	private static void listServices(String hostname)
			throws WebServiceException, IOException {
		Set<Services> services = Jws2Client.getServices(hostname);
		if (!services.isEmpty()) {
			System.out.println("Supported services are: "
					+ Services.toString(services));
		} else {
			System.out.println("Failed to connect to the registry! ");
		}
	}

	/**
	 * Calculate conservation for sequences loaded from the file
	 * 
	 * @param wsproxy
	 *            a web service proxy
	 * @param file
	 *            the file to read the results from
	 * @param preset
	 *            Preset to use optional
	 * @param customOptions
	 *            the list of options
	 * @return Set<Score> the conservation scores
	 * @throws UnknownFileFormatException
	 */
	static <T> ScoreManager analize(List<FastaSequence> fastalist,
			SequenceAnnotation<T> wsproxy, Preset<T> preset,
			List<Option<T>> customOptions) {

		ScoreManager scores = null;
		try {
			String jobId = null;
			if (customOptions != null && preset != null) {
				System.out
						.println("WARN: Parameters (-f) are defined together with a preset (-r) ignoring preset!");
			}
			if (customOptions != null) {
				jobId = wsproxy.customAnalize(fastalist, customOptions);
			} else if (preset != null) {
				jobId = wsproxy.presetAnalize(fastalist, preset);
			} else {
				jobId = wsproxy.analize(fastalist);
			}
			System.out.println("\n\ncalling analise.........");
			Thread.sleep(1000);
			scores = wsproxy.getAnnotation(jobId);

		} catch (JobSubmissionException e) {
			System.err
					.println("Exception while submitting job to a web server. "
							+ "Exception details are below:");
			e.printStackTrace();
		} catch (ResultNotAvailableException e) {
			System.err.println("Exception while waiting for results. "
					+ "Exception details are below:");
			e.printStackTrace();
		} catch (InterruptedException e) {
			// ignore and propagate an interruption
			Thread.currentThread().interrupt();
			System.err.println("Exception while waiting for results. "
					+ "Exception details are below:");
			e.printStackTrace();
		} catch (WrongParameterException e) {
			System.err
					.println("Exception while parsing the web method input parameters. "
							+ "Exception details are below:");
			e.printStackTrace();
		}
		return scores;

	}

	/**
	 * Calculate conservation for sequences loaded from the file
	 * 
	 * @param wsproxy
	 *            a web service proxy
	 * @param file
	 *            the file to read the results from
	 * @param preset
	 *            Preset to use optional
	 * @param customOptions
	 *            the list of options
	 * @return Set<Score> the conservation scores
	 * @throws IOException
	 * @throws UnknownFileFormatException
	 */
	static <T> ScoreManager analize(File file, SequenceAnnotation<T> wsproxy,
			Preset<T> preset, List<Option<T>> customOptions) {
		List<FastaSequence> fastalist = null;
		try {
			fastalist = SequenceUtil.openInputStream(file.getAbsolutePath());
			assert !fastalist.isEmpty() : "Input is empty!";
		} catch (IOException e) {
			System.err
					.println("Exception while reading the input file. "
							+ "Check that the input file contains a list of fasta formatted sequences! "
							+ "Exception details are below:");
			e.printStackTrace();
		} catch (UnknownFileFormatException e) {
			System.err
					.println("Exception while attempting to read the input file "
							+ "Exception details are below:");
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		return analize(fastalist, wsproxy, preset, customOptions);
	}
	/**
	 * Connects to a web service by the host and the service name web service
	 * type
	 * 
	 * @param host
	 *            the fully qualified name of JABAWS server including JABAWS
	 *            context name e.g
	 *            http://nanna.cluster.lifesci.dundee.ac.uk:8080/jaba
	 * @param service
	 *            the name of the JABAWS service to connect to
	 * @return JABAService<T>
	 * @throws WebServiceException
	 * @throws ConnectException
	 *             if fails to connect to the service on the host
	 */
	public static JABAService connect(String host, Services service)
			throws WebServiceException, ConnectException {
		URL url = null;
		log.log(Level.FINE, "Attempting to connect...");
		try {
			url = new URL(host + "/" + service.toString() + "?wsdl");
		} catch (MalformedURLException e) {
			e.printStackTrace();
			// ignore as the host name is already verified
		}
		Service serv = null;
		try {
			serv = service.getService(url, JABAService.SERVICE_NAMESPACE);
		} catch (WebServiceException wse) {
			System.out.println("Connecting to JABAWS version 2 service");
			if (isV2service(wse)) {
				serv = service
						.getService(url, JABAService.V2_SERVICE_NAMESPACE);
			}
		}
		if (serv == null) {
			throw new ConnectException("Could not connect to " + url
					+ " the server is down?");
		}
		JABAService serviceIF = service.getInterface(serv);
		log.log(Level.INFO, "Connected successfully!");

		return serviceIF;
	}

	static boolean isV2service(WebServiceException wse) {
		String message = wse.getMessage();
		int idx = message.indexOf("not a valid service");
		if (idx > 0) {
			if (message.substring(idx).contains(
					JABAService.V2_SERVICE_NAMESPACE)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Get a connection of JABAWS registry
	 * 
	 * @param host
	 *            the fully qualified name of JABAWS server including JABAWS
	 *            context name e.g
	 *            http://nanna.cluster.lifesci.dundee.ac.uk:8080/jaba
	 * @return compbio.data.msa.RegistryWS - instance of a RegistryWS web
	 *         service
	 * @throws WebServiceException
	 * @throws ConnectException
	 */
	public static compbio.data.msa.RegistryWS connectToRegistry(String host)
			throws WebServiceException, ConnectException {
		URL url = null;
		String service = "RegistryWS";
		log.log(Level.FINE, "Attempting to connect...");

		try {
			url = new URL(host + "/" + service + "?wsdl");
		} catch (MalformedURLException e) {
			e.printStackTrace();
			// ignore as the host name is already verified
		}
		QName qname = new QName(JABAService.V2_SERVICE_NAMESPACE, service);
		Service serv = Service.create(url, qname);

		if (serv == null) {
			throw new ConnectException("Could not connect to " + url
					+ " the server is down?");
		}

		QName portName = new QName(serv.getServiceName().getNamespaceURI(),
				service + "Port");
		compbio.data.msa.RegistryWS serviceIF = serv.getPort(portName,
				compbio.data.msa.RegistryWS.class);

		log.log(Level.INFO, "Connected to " + service + " successfully!");

		return serviceIF;
	}

	/**
	 * Align sequences from the file using MsaWS
	 * 
	 * @param <T>
	 *            web service type e.g. Clustal
	 * @param file
	 *            to write the resulting alignment to
	 * @param msaws
	 *            MsaWS required
	 * @param preset
	 *            Preset to use optional
	 * @param customOptions
	 *            file which contains new line separated list of options
	 * @return Alignment
	 */
	static <T> Alignment align(File file, MsaWS<T> msaws, Preset<T> preset,
			List<Option<T>> customOptions) {
		FileInputStream instream = null;
		List<FastaSequence> fastalist = null;
		Alignment alignment = null;
		try {
			instream = new FileInputStream(file);
			fastalist = SequenceUtil.readFasta(instream);
			instream.close();
			String jobId = null;
			if (customOptions != null && preset != null) {
				System.out
						.println("WARN: Parameters (-f) are defined together with a preset (-r) ignoring preset!");
			}
			if (customOptions != null) {
				jobId = msaws.customAlign(fastalist, customOptions);
			} else if (preset != null) {
				jobId = msaws.presetAlign(fastalist, preset);
			} else {
				jobId = msaws.align(fastalist);
			}
			System.out.println("\n\ncalling align.........");
			Thread.sleep(1000);
			alignment = msaws.getResult(jobId);

		} catch (IOException e) {
			System.err
					.println("Exception while reading the input file. "
							+ "Check that the input file contains a list of fasta formatted sequences! "
							+ "Exception details are below:");
			e.printStackTrace();
		} catch (JobSubmissionException e) {
			System.err
					.println("Exception while submitting job to a web server. "
							+ "Exception details are below:");
			e.printStackTrace();
		} catch (ResultNotAvailableException e) {
			System.err.println("Exception while waiting for results. "
					+ "Exception details are below:");
			e.printStackTrace();
		} catch (InterruptedException ignored) {
			// ignore and propagate an interruption
			Thread.currentThread().interrupt();
		} catch (WrongParameterException e) {
			e.printStackTrace();
		} finally {
			if (instream != null) {
				try {
					instream.close();
				} catch (IOException ignored) {
					// ignore
				}
			}
		}
		return alignment;
	}

	/**
	 * Prints Jws2Client usage information to standard out
	 * 
	 * @param exitStatus
	 */
	static void printUsage(int exitStatus) {
		System.out.println(Constraints.help_text);
		System.exit(exitStatus);
	}

	/**
	 * Starts command line client, if no parameter are supported print help. Two
	 * parameters are required for successful call the JWS2 host name and a
	 * service name.
	 * 
	 * @param args
	 *            Usage: <Class or Jar file name> -h=host_and_context
	 *            -s=serviceName ACTION [OPTIONS]
	 * 
	 *            -h=<host_and_context> - a full URL to the JWS2 web server
	 *            including context path e.g. http://10.31.1.159:8080/ws
	 * 
	 *            -s=<ServiceName> - one of [MafftWS, MuscleWS, ClustalWS,
	 *            TcoffeeWS, ProbconsWS] ACTIONS:
	 * 
	 *            -i=<inputFile> - full path to fasta formatted sequence file,
	 *            from which to align sequences
	 * 
	 *            -parameters - lists parameters supported by web service
	 * 
	 *            -presets - lists presets supported by web service
	 * 
	 *            -limits - lists web services limits Please note that if input
	 *            file is specified other actions are ignored
	 * 
	 *            OPTIONS: (only for use with -i action):
	 * 
	 *            -r=<presetName> - name of the preset to use
	 * 
	 *            -o=<outputFile> - full path to the file where to write an
	 *            alignment -f=<parameterInputFile> - the name of the file with
	 *            the list of parameters to use. Please note that -r and -f
	 *            options cannot be used together. Alignment is done with either
	 *            preset or a parameters from the file, but not both!
	 * 
	 */
	public static void main(String[] args) {

		if (args == null) {
			printUsage(1);
		}
		if (args.length < 2) {
			System.err.println("Host and service names are required!");
			printUsage(1);
		}

		try {
			new Jws2Client(args);
		} catch (IOException e) {
			log.log(Level.SEVERE, "IOException in client! " + e.getMessage(),
					e.getCause());
			System.err.println("Cannot write output file! Stack trace: ");
			e.printStackTrace();
		}
	}
}
