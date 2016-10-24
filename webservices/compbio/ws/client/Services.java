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

import java.net.URL;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import compbio.data.msa.JABAService;
import compbio.data.msa.MsaWS;
import compbio.data.msa.SequenceAnnotation;

/**
 * List of web services currently supported by JABAWS version 2
 * 
 */
public enum Services {
	/*
	 * Make sure this class has NO references to runners or engines as it is a
	 * part of minimal client package. Such things should go into ServicesUtil
	 */
	MafftWS, MuscleWS, ClustalWS, ClustalOWS, TcoffeeWS, ProbconsWS, AAConWS, JronnWS, DisemblWS, GlobPlotWS, IUPredWS;

	public static Services getService(String servName) {
		servName = servName.trim().toLowerCase();
		for (Services service : Services.values()) {
			if (service.toString().equalsIgnoreCase(servName)) {
				return service;
			}
		}
		return null;
	}

	Service getService(URL url, String sqname) {
		QName qname = new QName(sqname, this.toString());
		return Service.create(url, qname);
	}

	public static String toString(Set<Services> services) {
		if (services == null || services.isEmpty()) {
			return "";
		}
		String value = "";
		String delim = ", ";
		for (Services serv : services) {
			value += serv.toString() + delim;
		}
		value = value.substring(0, value.length() - delim.length());
		return value;
	}

	Class<? extends JABAService> getServiceType() {
		switch (this) {
			// deliberate leaking
			case AAConWS :
			case JronnWS :
			case DisemblWS :
			case GlobPlotWS :
			case IUPredWS :
				return SequenceAnnotation.class;

				// deliberate leaking
			case ClustalWS :
			case ClustalOWS :
			case MafftWS :
			case MuscleWS :
			case ProbconsWS :
			case TcoffeeWS :

				return MsaWS.class;
			default :
				throw new RuntimeException("Unrecognised Web Service Type "
						+ this + " - Should never happened!");
		}
	}

	JABAService getInterface(Service service) {
		assert service != null;

		QName portName = new QName(service.getServiceName().getNamespaceURI(),
				this.toString() + "Port");
		return service.getPort(portName, this.getServiceType());
	}

	public String getServiceInfo() {
		switch (this) {
			case AAConWS :
				return AACON_INFO;
			case ClustalOWS :
				return CLUSTAL_OMEGA_INFO;
			case ClustalWS :
				return CLUSTAL_INFO;
			case DisemblWS :
				return DISEMBL_INFO;
			case GlobPlotWS :
				return GLOBPLOT_INFO;
			case IUPredWS :
				return IUPRED_INFO;
			case JronnWS :
				return JRONN_INFO;
			case MafftWS :
				return MAFFT_INFO;
			case MuscleWS :
				return MUSCLE_INFO;
			case ProbconsWS :
				return PROBCONS_INFO;
			case TcoffeeWS :
				return TCOFFEE_INFO;
			default :
				throw new RuntimeException("Unrecognised Web Service Type "
						+ this + " - Should never happened!");
		}
	}

	public static final String AACON_INFO = new ServiceInfo(AAConWS,
			"in preparation", "1.0", "http://www.compbio.dundee.ac.uk/aacon")
			.toString();
	public static final String CLUSTAL_INFO = new ServiceInfo(
			ClustalWS,
			"Larkin MA, Blackshields G, Brown NP, Chenna R, McGettigan PA, McWilliam H, Valentin F, Wallace IM, Wilm A, Lopez R, Thompson JD, Gibson TJ, Higgins DG.\r\n"
					+ "(2007). Clustal W and Clustal X version 2.0. Bioinformatics, 23, 2947-2948. ",
			"2.0.12", "http://www.clustal.org/clustal2/").toString();
	public static final String CLUSTAL_OMEGA_INFO = new ServiceInfo(
			ClustalOWS,
			"Fast, scalable generation of high quality protein multiple sequence alignments using Clustal Omega\r\n"
					+ "Fabian Sievers, Andreas Wilm, David Dineen, Toby J. Gibson, Kevin Karplus, Weizhong Li, Rodrigo Lopez, Hamish McWilliam, Michael Remmert, Johannes Söding, Julie D. Thompson, Desmond G. Higgins",
			"1.0.2", "http://www.clustal.org/omega").toString();
	public static final String DISEMBL_INFO = new ServiceInfo(
			DisemblWS,
			"R. Linding, L.J. Jensen, F. Diella, P. Bork, T.J. Gibson and R.B. Russell\r\n"
					+ "Protein disorder prediction: implications for structural proteomics\r\n"
					+ "Structure Vol 11, Issue 11, 4 November 2003", "1.5",
			"http://dis.embl.de/").toString();
	public static final String GLOBPLOT_INFO = new ServiceInfo(
			GlobPlotWS,
			"Rune Linding, Robert B. Russell, Victor Neduva and Toby J. Gibson "
					+ "'GlobPlot: exploring protein sequences for globularity and disorder.' Nucl. Acids Res. (2003) 31 (13): 3701-3708. doi: 10.1093/nar/gkg519\r\n",
			"2.3", "http://globplot.embl.de/").toString();
	public static final String IUPRED_INFO = new ServiceInfo(
			IUPredWS,
			"The Pairwise Energy Content Estimated from Amino Acid Composition Discriminates between Folded and Intrinsically Unstructured Proteins\r\n"
					+ "Zsuzsanna Dosztányi, Veronika Csizmók, Péter Tompa and István Simon\r\n"
					+ "J. Mol. Biol. (2005) 347, 827-839.", "1.0",
			"http://iupred.enzim.hu/").toString();
	public static final String TCOFFEE_INFO = new ServiceInfo(TcoffeeWS,
			"T-Coffee: A novel method for multiple sequence alignments  "
					+ "Notredame, Higgins, Heringa, JMB, 302 (205-217) 2000",
			"8.99", "http://tcoffee.crg.cat/apps/tcoffee/index.html")
			.toString();
	public static final String MUSCLE_INFO = new ServiceInfo(
			MuscleWS,
			"Edgar, R.C. (2004) MUSCLE: multiple sequence alignment with high accuracy and high throughput.Nucleic Acids Res. 32(5):1792-1797.\r\n"
					+ "doi:10.1093/nar/gkh340", "3.8.31",
			"http://www.drive5.com/muscle/").toString();
	public static final String PROBCONS_INFO = new ServiceInfo(
			ProbconsWS,
			"Do, C.B., Mahabhashyam, M.S.P., Brudno, M., and Batzoglou, S. 2005. PROBCONS: "
					+ "Probabilistic Consistency-based Multiple Sequence Alignment. Genome Research 15: 330-340. ",
			"1.12", "http://probcons.stanford.edu/").toString();;
	public static final String JRONN_INFO = new ServiceInfo(
			JronnWS,
			"unpublished, original algorithm Yang,Z.R., Thomson,R., McMeil,P. and Esnouf,R.M. (2005) "
					+ "RONN: the bio-basis function neural network technique applied to the "
					+ "dectection of natively disordered regions in proteins Bioinformatics 21: 3369-3376\r\n",
			"1.0", "http://www.compbio.dundee.ac.uk/jabaws/").toString();;
	public static final String MAFFT_INFO = new ServiceInfo(
			MafftWS,
			"Katoh, Toh 2010 (Bioinformatics 26:1899-1900)\r\n"
					+ "Parallelization of the MAFFT multiple sequence alignment program. ",
			"6.8.57", "http://mafft.cbrc.jp/alignment/software/").toString();;

	@XmlAccessorType(XmlAccessType.FIELD)
	static class ServiceInfo {
		Services service;
		String reference;
		String version;
		String moreinfo;
		final static String jabaws_version = "2.0";
		final static String line_delimiter = "\n";

		private ServiceInfo() {
			// Default constructor for JAXB
		}
		private ServiceInfo(Services service, String reference, String version,
				String moreinfo) {
			this.service = service;
			this.reference = reference;
			this.version = version;
			this.moreinfo = moreinfo;
		}

		@Override
		public String toString() {
			String value = "SERVICE: " + service + " version " + version
					+ line_delimiter;
			value += "JABAWS v. " + jabaws_version + line_delimiter;
			value += "REFERENCES: " + reference + line_delimiter;
			value += "MORE INFORMATION: " + moreinfo + line_delimiter;
			return value;
		}
	}

	public static void main(String[] args) {
		System.out.println(MUSCLE_INFO);
	}
}