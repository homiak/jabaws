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

class Constraints {

	final static String pseparator = "=";

	// Parameters for required command line options
	final static String hostkey = "-h";
	final static String servicekey = "-s";

	final static String listServices = "-list_services";
	final static String testKey = "-test";

	// Actions
	final static String inputkey = "-i";

	final static String paramList = "-parameters";
	final static String presetList = "-presets";
	final static String limitList = "-limits";

	// Options
	final static String paramFile = "-f";
	final static String outputkey = "-o";
	final static String parameterkey = "-p";
	final static String presetkey = "-r";

	final static String help_text = "\r\n" + 
			"JABAWS2 client August 2011 http://www.compbio.dundee.ac.uk/jabaws \r\n" + 
			" \r\n" + 
			"Usage: <Class or Jar file name> -h=host_and_context <-s=serviceName> ACTION [OPTIONS] \r\n" + 
			"\r\n" + 
			"-h=<host_context>  - a full URL to the JABAWS web server including context \r\n" + 
			"                     path e.g. http://10.31.1.159:8080/ws\r\n" + 
			"-s=<ServiceName>   - one of [MafftWS, MuscleWS, ClustalWS, ClustalOWS, TcoffeeWS, \r\n" + 
			"					 ProbconsWS, AAConWS, JronnWS, DisemblWS, GlobPlotWS, IUPredWS]\r\n" + 
			"                     <serviceName> is required for all ACTIONS but -list_services\r\n" + 
			"\r\n" + 
			"ACTIONS: \r\n" + 
			"-list_services    - list available services\r\n" + 
			"-test             - test service \r\n" + 
			"-i=<inputFile>    - full path to fasta formatted sequence file, from which to align \r\n" + 
			"                    sequences\r\n" + 
			"-parameters       - lists parameters supported by web service\r\n" + 
			"-presets          - lists presets supported by web service\r\n" + 
			"-limits           - lists web services limits\r\n" + 
			"\r\n" + 
			"Please note that if input file is specified other actions are ignored\r\n" + 
			"\r\n" + 
			"OPTIONS (only for use with -i action):\r\n" + 
			"-r=<presetName>   - name of the preset to use\r\n" + 
			"-o=<outputFile>   - full path to the file where to write an alignment\r\n" + 
			"-f=<PrmInputFile> - the name of the file with the list of parameters to use.\r\n" + 
			"\r\n" + 
			"Please note that -r and -f options cannot be used together. Alignment is done with \r\n" + 
			"either preset or a parameters from the file, but not both!\r\n" + 
			"\r\n" + 
			"EXAMPLES: \r\n" + 
			"\r\n" + 
			"1) List all available services on the host \r\n" + 
			"\r\n" + 
			"Jws2Client -h=http://www.compbio.dundee.ac.uk/jabaws -list_services\r\n" + 
			"\r\n" + 
			"2) Test Clustal web service \r\n" + 
			"\r\n" + 
			"Jws2Client -h=http://www.compbio.dundee.ac.uk/jabaws -s=ClustalWS -test \r\n" + 
			"\r\n" + 
			"3) Align sequence from file input.txt with Probcons. Record resulting alignment \r\n" + 
			"into the output.txt \r\n" + 
			"\r\n" + 
			"Jws2Client -h=http://www.compbio.dundee.ac.uk/jabaws -s=ProbconsWS -i=input.txt -o=output.txt\r\n" + 
			"\r\n" + 
			"4) Calculate disorder with Disembl take input from input.txt, output results to \r\n" + 
			"the console \r\n" + 
			"\r\n" + 
			"Jws2Client -h=http://www.compbio.dundee.ac.uk/jabaws -s=DisemblWS -i=input.txt \r\n" + 
			"\r\n" + 
			"5) List all parameters available for AAconWS service \r\n" + 
			"\r\n" + 
			"Jws2Client -h=http://www.compbio.dundee.ac.uk/jabaws -s=AAconWS -parameters\r\n" + 
			"\r\n" + 
			"6) Calculate conservation with AAConWS using LANDGRAF method, for Clustal alignment \r\n" + 
			"from input.txt and report  the scores to the console \r\n" + 
			"\r\n" + 
			"Jws2Client -h=http://www.compbio.dundee.ac.uk/jabaws -s=AAconWS -i=input.txt -f=prm.txt \r\n" + 
			"\r\n" + 
			"Where the content of prm.txt file is -m=LANDGRAF\r\n" + 
			"The list of the supported parameters can be obtained as shown in the example 5. \r\n" + 
			"\r\n" + 
			"Citation: Peter V. Troshin, James B. Procter and Geoffrey J. Barton - \"Java \r\n" + 
			"Bioinformatics Analysis Web Services for Multiple Sequence Alignment - \r\n" + 
			"JABAWS:MSA\" Bioinformatics 2011; doi: 10.1093/bioinformatics/btr304.\r\n" + 
			"";

}
