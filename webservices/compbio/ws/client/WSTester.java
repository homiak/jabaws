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
import static compbio.ws.client.Constraints.pseparator;
import static compbio.ws.client.Constraints.servicekey;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.util.Arrays;
import java.util.List;

import javax.xml.ws.WebServiceException;

import org.apache.log4j.Logger;

import compbio.data.msa.JABAService;
import compbio.data.msa.Metadata;
import compbio.data.msa.MsaWS;
import compbio.data.msa.SequenceAnnotation;
import compbio.data.sequence.Alignment;
import compbio.data.sequence.FastaSequence;
import compbio.data.sequence.ScoreManager;
import compbio.data.sequence.SequenceUtil;
import compbio.metadata.JobStatus;
import compbio.metadata.Limit;
import compbio.metadata.LimitsManager;
import compbio.metadata.Option;
import compbio.metadata.Preset;
import compbio.metadata.PresetManager;
import compbio.metadata.RunnerConfig;
import compbio.metadata.UnsupportedRuntimeException;
import compbio.util.FileUtil;
import compbio.util.Util;

/**
 * Class for testing web services
 * 
 * @author pvtroshin
 * 
 * @version 1.0 February 2010
 */
public class WSTester {

	private static Logger log = Logger.getLogger(WSTester.class);
	/**
	 * Sequences to be used as input for all WS
	 */
	public static final String fastaInput = ">Foo\n"
			+ "MTADGPRELLQLRAAVRHRPQDFVAWLMLADAELGMGDTTAGEMAVQRGLALHPGHPEAV"
			+ "\n>Bar\n"
			+ "ASDAAPEHPGIALWLHALEDAGQAEAAAAYTRAHQLLPEEPYITAQLLNAVA\n";

	public static final String fastaAlignment = ">Foo\n"
			+ "MTADGPRELLQLRAAVRHRPQDFVAWLMLADAELGMGDTTAGEMAVQRGLALHPGHPEAV--------\n"
			+ ">Bar\n"
			+ "ASDAAPEH------------PGIALWLHALE-DAGQAEAAA---AYTRAHQLLPEEPYITAQLLNAVA\n"
			+ "";

	static final List<FastaSequence> seqs = loadSeqs();

	private static final String FAILED = "FAILED";
	private static final String OK = "OK";

	private static final String UNSUPPORTED = "UNSUPPORTED";

	/**
	 * Converting input to a form accepted by WS
	 * 
	 * @return List of FastaSequence records
	 */
	private static List<FastaSequence> loadSeqs() {
		try {
			return SequenceUtil.readFasta(new ByteArrayInputStream(fastaInput
					.getBytes()));
		} catch (IOException ignored) {
			// Should not happen as a source is not a external stream
			ignored.printStackTrace();
		}
		return null;
	}

	/**
	 * Converting input to a form accepted by WS
	 * 
	 * @return List of FastaSequence records
	 */
	private static List<FastaSequence> loadAlignment() {
		try {
			return SequenceUtil.readFasta(new ByteArrayInputStream(
					fastaAlignment.getBytes()));
		} catch (IOException ignored) {
			// Should not happen as a source is not a external stream
			ignored.printStackTrace();
		}
		return null;
	}

	private final PrintWriter writer;
	private final String hostname;

	/**
	 * Construct an instance of JABAWS tester
	 * 
	 * @param hostname
	 *            - fully qualified host and context name of JABAWS e.g.
	 *            http://nanna.cluster.lifesci.dundee.ac.uk:8080/jaba
	 * @param writer
	 *            a PrintWriter instance to writer test log to.
	 */
	public WSTester(String hostname, PrintWriter writer) {
		if (Util.isEmpty(hostname)) {
			throw new NullPointerException("Hostname must be provided!");
		}
		this.hostname = hostname;
		this.writer = writer;
	}

	/**
	 * Prints usage
	 */
	static void printUsage() {
		System.out.println("Usage: <Class or Jar file name> " + hostkey
				+ pseparator + "host_and_context " + "<" + servicekey
				+ pseparator + "serviceName>");
		System.out.println();
		System.out
				.println(hostkey
						+ pseparator
						+ "<host_and_context> - a full URL to the JABAWS web server including context path e.g. http://10.31.1.159:8080/ws");
		System.out
				.println(servicekey
						+ pseparator
						+ "<ServiceName> - optional if unspecified all services are tested otherwise one of "
						+ Arrays.toString(Services.values()));
		System.out.println();

	}

	/**
	 * Calls alignment with preset
	 * 
	 * @param <T>
	 * @param msaws
	 * @param presets
	 *            list of the Preset
	 * @throws UnsupportedRuntimeException
	 */
	private <T> boolean presetAlign(MsaWS<T> msaws, List<Preset<T>> presets)
			throws UnsupportedRuntimeException {
		boolean succeed = false;
		for (Preset<T> preset : presets) {
			writer.print("Aligning with preset '" + preset.getName() + "'... ");
			Alignment al = null;
			try {
				String taskId = msaws.presetAlign(seqs, preset);
				al = msaws.getResult(taskId);
				if (al != null) {
					writer.println(OK);
				}
				succeed = true;
			} catch (Exception e) {
				if (e instanceof UnsupportedRuntimeException) {
					// If executable is not supported than none of the presets
					// are
					// going to work
					throw (UnsupportedRuntimeException) e;
				} else {
					reportException(e);
				}
				continue;
			}
		}
		return succeed;
	}

	private <T> boolean testMsaWS(MsaWS<T> msaws) throws Exception {
		assert msaws != null;

		boolean succeed = testDefaultAlignment(msaws);
		// If exception above is thrown than the tests below is not run

		PresetManager<T> pmanager = msaws.getPresets();
		if (pmanager != null && pmanager.getPresets().size() > 0) {
			writer.println("Testing alignment with presets:");
			List<Preset<T>> plist = pmanager.getPresets();
			succeed = !succeed ? presetAlign(msaws, plist) : succeed;
		}
		testMetadata(msaws);
		return succeed;
	}
	/**
	 * Call most of web services functions and check the output
	 * 
	 * @param <T>
	 *            web service type
	 * @param msaws
	 * @throws UnsupportedRuntimeException
	 *             is thrown if the connection to a web service was made, but
	 *             the web service is not functional. e.g. when native
	 *             executable does not exists for a server platform
	 */
	@SuppressWarnings("unchecked")
	private <T> boolean checkService(JABAService wservice, Services service) {
		try {
			if (wservice == null) {
				throw new NullPointerException(
						"JABAService instance must be provided!");
			}

			if (wservice instanceof MsaWS) {
				return testMsaWS((MsaWS<T>) wservice);
			} else if (wservice instanceof SequenceAnnotation) {
				return testSequenceAnnotationWS(
						(SequenceAnnotation<T>) wservice, service);
			} else {
				throw new UnsupportedOperationException("The service: "
						+ wservice.getClass() + " is not supported! ");
			}
		} catch (Exception e) {
			reportException(e);
			return false;
		}
	}

	private <T> boolean testSequenceAnnotationWS(
			SequenceAnnotation<T> wservice, Services service) throws Exception {
		writer.print("Calling analyse.........");

		List<FastaSequence> input = loadSeqs();
		if (service == Services.AAConWS) {
			input = loadAlignment();
		}
		boolean success = testDefaultAnalyse(input, wservice, null, null);

		PresetManager<T> presetman = wservice.getPresets();
		if (presetman != null) {
			List<Preset<T>> presets = presetman.getPresets();
			if (presets != null && !presets.isEmpty()) {
				Preset<T> preset = presets.get(0);
				writer.print("Calling analyse with Preset.........");
				success = testDefaultAnalyse(input, wservice, preset, null);
			}
		}
		testMetadata(wservice);
		return success;
	}

	private <T> boolean testDefaultAnalyse(List<FastaSequence> fastalist,
			SequenceAnnotation<T> wsproxy, Preset<T> preset,
			List<Option<T>> customOptions) throws Exception {

		ScoreManager scores = null;

		String jobId = null;
		if (customOptions != null) {
			jobId = wsproxy.customAnalize(fastalist, customOptions);
		} else if (preset != null) {
			jobId = wsproxy.presetAnalize(fastalist, preset);
		} else {
			jobId = wsproxy.analize(fastalist);
		}
		Thread.sleep(1000);
		scores = wsproxy.getAnnotation(jobId);
		if (scores != null) {
			writer.println(OK);
		}

		return scores != null;
	}

	private void reportException(Exception e) {
		writer.println(FAILED);
		writer.println("Exception while waiting for results. "
				+ "Exception details are below:");
		writer.println(e.getLocalizedMessage());
		e.printStackTrace(writer);
	}

	private <T> void testMetadata(Metadata<T> msaws)
			throws UnsupportedRuntimeException {

		writer.print("Querying presets...");
		PresetManager<T> pmanager = msaws.getPresets();
		if (pmanager != null && pmanager.getPresets().size() > 0) {
			writer.println(OK);
		} else {
			writer.println(UNSUPPORTED);
		}

		writer.print("Querying Parameters...");
		RunnerConfig<T> options = msaws.getRunnerOptions();
		if (options != null && options.getArguments().size() > 0) {
			writer.println(OK);
		} else {
			writer.println(UNSUPPORTED);
		}

		writer.print("Querying Limits...");
		LimitsManager<T> limits = msaws.getLimits();
		if (limits != null && limits.getLimits().size() > 0) {
			writer.println(OK);
			// writer.println("Limits details: \n" + limits.toString());
		} else {
			writer.println(UNSUPPORTED);
		}

		writer.print("Querying Local Engine Limits...");
		Limit<T> localLimit = msaws
				.getLimit(PresetManager.LOCAL_ENGINE_LIMIT_PRESET);
		if (localLimit != null) {
			writer.println(OK);
		} else {
			writer.println(UNSUPPORTED);
		}
	}

	/**
	 * Align using default settings
	 * 
	 * @param <T>
	 * @param msaws
	 * @throws UnsupportedRuntimeException
	 */
	private <T> boolean testDefaultAlignment(MsaWS<T> msaws) throws Exception {
		writer.print("Testing alignment with default parameters:");
		Alignment al = null;
		boolean succeed = false;

		String taskId = msaws.align(seqs);
		writer.print("\nQuerying job status...");
		JobStatus status = msaws.getJobStatus(taskId);
		while (status != JobStatus.FINISHED) {
			Thread.sleep(1000);
			status = msaws.getJobStatus(taskId);
		}
		writer.println(OK);
		writer.print("Retrieving results...");
		al = msaws.getResult(taskId);
		succeed = true;
		if (al != null) {
			writer.println(OK);
		}
		return succeed;
	}
	/**
	 * Test JWS2 web services
	 * 
	 * @param <T>
	 *            web service type
	 * @param args
	 *            -h=<Your web application server host name, port and JWS2
	 *            context path>
	 * 
	 *            -s=<ServiceName> which is optional. If service name is not
	 *            provided then all known JWS2 web services are tested
	 * @throws IOException
	 */
	public static <T> void main(String[] args) throws IOException {

		if (args == null || args.length < 1) {
			WSTester.printUsage();
			System.exit(0);
		}
		String host = CmdHelper.getHost(args);
		String serviceName = CmdHelper.getServiceName(args);
		if (!Jws2Client.validURL(host)) {
			System.err
					.println("<host_and_context> parameter is not provided or is incorrect!");
			System.exit(1);
		}
		WSTester tester = new WSTester(host, new PrintWriter(System.out, true));

		if (serviceName != null) {
			Services service = Services.getService(serviceName);
			if (service == null) {
				tester.writer.println("Service '" + serviceName
						+ "' is not supported. Valid values are: "
						+ Arrays.toString(Services.values()));
				tester.writer.println();
				printUsage();
				System.exit(1);
			}
			tester.checkService(service);
			System.exit(0);
		}

		tester.writer
				.println("<ServiceName> is not provided checking all known services...");

		for (Services serv : Services.values()) {
			tester.writer.println();
			tester.checkService(serv);
		}

	}

	/**
	 * Test JABA web service
	 * 
	 * @param service
	 *            the service to test
	 * @return true if the service works as expected, false otherwise
	 * @throws WebServiceException
	 * @throws ConnectException
	 */
	public boolean checkService(Services service) throws ConnectException,
			WebServiceException {
		JABAService ws = Jws2Client.connect(hostname, service);
		if (ws == null) {
			writer.println("Cannot estabilish the connection to host "
					+ hostname + " with service " + service.toString());
			return false;
		}
		boolean succeed = false;
		try {
			writer.println("Checking service " + service.toString());
			succeed = checkService(ws, service);
		} finally {
			FileUtil.closeSilently(((Closeable) ws));
		}
		reportResults(service, succeed);
		return succeed;
	}

	private void reportResults(Services serv, boolean succeed) {
		if (succeed) {
			writer.println("Check is completed. The Service " + serv
					+ " IS WORKING\n");
		} else {
			writer.println("Check is aborted. The Service " + serv
					+ " HAS SOME PROBLEMS\n");
		}
	}
}
