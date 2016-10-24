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
package compbio.metadata;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.bind.ValidationException;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import compbio.engine.conf.RunnerConfigMarshaller;
import compbio.runner.msa.Mafft;

public class RunnerConfigTester {

	public static String test_input = AllTestSuit.TEST_DATA_PATH_ABSOLUTE
			+ "MafftParameters.xml";

	RunnerConfig<Mafft> rconfig = null;

	@BeforeMethod
	public void setup() {
		try {
			rconfig = new RunnerConfig<Mafft>();
			rconfig.setRunnerClassName(Mafft.class.getName());
			List<Option<Mafft>> prms = new ArrayList<Option<Mafft>>();

			RunnerConfigMarshaller<Mafft> pmarshaller = new RunnerConfigMarshaller<Mafft>(
					RunnerConfig.class, Parameter.class, Option.class,
					ValueConstrain.class);
		} catch (JAXBException e) {
			e.printStackTrace();
			fail(e.getLocalizedMessage());
		}
	}

	@Test
	public void testValidate() {
		try {
			rconfig.validate();
		} catch (ValidationException e) {
			e.printStackTrace();
			fail(e.getLocalizedMessage());
		} catch (IllegalStateException e) {
			e.printStackTrace();
			fail(e.getLocalizedMessage());
		}
	}

	@Test(expectedExceptions = WrongParameterException.class)
	public void testCreateParameter() throws WrongParameterException {
		try {
			Parameter<Mafft> p3 = new Parameter<Mafft>("Matrix1",
					"Protein weight matrix");
			// TODO publish help on a compbio web site
			p3.setFurtherDetails(new URL("http",
					"www.compbio.dundee.ac.uk/users/pvtroshin/ws/",
					"Index.html"));

			p3.addPossibleValues("BLOSUM", "PAM", "GONNET", "ID");
			// This attribute is required by strict schema
			p3.setOptionName("--AAMATRIX");
			p3.setRequired(true);
			// THIS LINE IS CAUSING EXCEPTION AS DEFAULT VALUE MUST BE DEFINED
			// IN WITHIN POSSIBLE VALUES
			p3.setDefaultValue("pam22");
			String com = p3.toCommand(" ");
			System.out.println("AAAAAAAAAAAAAA!" + com);
		} catch (MalformedURLException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	@Test()
	public void testParameterToCommand() throws WrongParameterException {
		try {
			Parameter<Mafft> p3 = new Parameter<Mafft>("Matrix1",
					"Protein weight matrix");
			// TODO publish help on a compbio web site
			p3.setFurtherDetails(new URL("http",
					"www.compbio.dundee.ac.uk/users/pvtroshin/ws/",
					"Index.html"));

			p3.addPossibleValues("BLOSUM", "PAM", "GONNET", "ID");
			// This attribute is required by strict schema
			p3.setOptionName("--AAMATRIX");
			p3.setRequired(true);
			// THIS LINE IS CAUSING EXCEPTION AS DEFAULT VALUE MUST BE DEFINED
			// IN WITHIN POSSIBLE VALUES
			p3.setDefaultValue("PAM");
			String com = p3.toCommand("=");
			assertTrue(com.startsWith("--AAMATRIX"));
			assertTrue(com.endsWith("PAM"));
			assertTrue(com.contains("="));
			p3.setDefaultValue("ID");
			com = p3.toCommand("=");
			assertFalse(com.endsWith("PAM"));
			assertFalse(com.contains("PAM"));

		} catch (MalformedURLException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	@Test(expectedExceptions = ValidationException.class)
	public void testOptionNoDefaultValidate() throws ValidationException {
		try {
			Option<Mafft> p3 = new Option<Mafft>("Matrix1",
					"Protein weight matrix");
			// TODO publish help on a compbio web site
			p3.setFurtherDetails(new URL("http",
					"www.compbio.dundee.ac.uk/users/pvtroshin/ws/",
					"Index.html"));

			p3.setOptionNames(new HashSet(Arrays.asList("--AAMATRIX",
					"--ABMAT", "--BBBB")));
			p3.setRequired(true);
			// THIS LINE IS CAUSING EXCEPTION AS DEFAULT VALUE MUST BE DEFINED
			// IN WITHIN POSSIBLE VALUES
			p3.validate();

		} catch (MalformedURLException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	@Test(expectedExceptions = WrongParameterException.class)
	public void testOptionSetInvalidValue() throws WrongParameterException {
		try {
			Option<Mafft> p3 = new Option<Mafft>("Matrix1",
					"Protein weight matrix");
			// TODO publish help on a compbio web site
			p3.setFurtherDetails(new URL("http",
					"www.compbio.dundee.ac.uk/users/pvtroshin/ws/",
					"Index.html"));

			p3.setOptionNames(new HashSet(Arrays.asList("--AAMATRIX",
					"--ABMAT", "--BBBB")));
			p3.setRequired(true);
			// THIS LINE IS CAUSING EXCEPTION AS DEFAULT VALUE MUST BE DEFINED
			// IN WITHIN POSSIBLE VALUES
			p3.setDefaultValue("AAA");

		} catch (MalformedURLException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	@Test()
	public void testOptionToCommand() {
		try {
			Option<Mafft> p3 = new Option<Mafft>("Matrix1",
					"Protein weight matrix");
			// TODO publish help on a compbio web site
			p3.setFurtherDetails(new URL("http",
					"www.compbio.dundee.ac.uk/users/pvtroshin/ws/",
					"Index.html"));

			p3.setOptionNames(new HashSet(Arrays.asList("--AAMATRIX",
					"--ABMAT", "--BBBB")));
			p3.setRequired(true);
			// THIS LINE IS CAUSING EXCEPTION AS DEFAULT VALUE MUST BE DEFINED
			// IN WITHIN POSSIBLE VALUES
			p3.setDefaultValue("--BBBB");
			p3.validate();
			String com = p3.toCommand("=");
			assertEquals("--BBBB", com);
			p3.setDefaultValue("--ABMAT");
			com = p3.toCommand("=");
			assertEquals("--ABMAT", com);
		} catch (MalformedURLException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (ValidationException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (WrongParameterException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	@Test(expectedExceptions = IllegalStateException.class)
	public void testCreateNumParameterWithoutValidValue() {
		try {
			Parameter<Mafft> p4 = new Parameter<Mafft>("Matrix",
					"DNA weight matrix");
			// This is causing exception is ValidValue constrain is not defined
			// for
			// numeric value
			p4.setDefaultValue("5");
		} catch (WrongParameterException e) {
			e.printStackTrace();
			fail(e.getLocalizedMessage());
		}

	}

	@Test()
	public void testCreateParameterWithValidValueConstrain() {

		Parameter<Mafft> p4 = new Parameter<Mafft>("Matrix",
				"DNA weight matrix");
		ValueConstrain vc = new ValueConstrain();
		vc.setType(ValueConstrain.Type.Float);
		vc.setMin("0");
		vc.setMax("10");
		p4.setValidValue(vc);
		try {
			p4.setDefaultValue("5");
		} catch (WrongParameterException e) {
			e.printStackTrace();
			fail(e.getLocalizedMessage());
		}
	}

	@Test(expectedExceptions = WrongParameterException.class)
	public void testValidateLowerBoundaryConstrainCheck()
			throws WrongParameterException {
		try {
			Parameter<Mafft> p3 = new Parameter<Mafft>("Matrix1",
					"Protein weight matrix");
			// TODO publish help on a compbio web site
			p3.setFurtherDetails(new URL("http",
					"www.compbio.dundee.ac.uk/users/pvtroshin/ws/",
					"Index.html"));
			// This attribute is required by strict schema
			p3.setOptionName("--AAMATRIX");
			p3.setRequired(true);

			ValueConstrain vc = new ValueConstrain();
			vc.setType(ValueConstrain.Type.Float);
			vc.setMin("-10.12");
			vc.setMax("0");
			p3.setValidValue(vc);
			// THIS IS CAUSING EXCEPTION
			p3.setDefaultValue("-11.0");

		} catch (MalformedURLException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	@Test(expectedExceptions = WrongParameterException.class)
	public void testValidateUpperBoundaryConstrainCheck()
			throws WrongParameterException {
		try {
			Parameter<Mafft> p3 = new Parameter<Mafft>("Matrix1",
					"Protein weight matrix");
			// TODO publish help on a compbio web site
			p3.setFurtherDetails(new URL("http",
					"www.compbio.dundee.ac.uk/users/pvtroshin/ws/",
					"Index.html"));
			// This attribute is required by strict schema
			p3.setOptionName("--AAMATRIX");
			p3.setRequired(true);

			ValueConstrain vc = new ValueConstrain();
			vc.setType(ValueConstrain.Type.Float);
			vc.setMin("-10.12");
			vc.setMax("0");
			p3.setValidValue(vc);
			// THIS IS CAUSING EXCEPTION
			p3.setDefaultValue("1");

		} catch (MalformedURLException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	@Test()
	public void testValidateBoundaryConstrainCheck() {
		try {
			Parameter<Mafft> p3 = new Parameter<Mafft>("Matrix1",
					"Protein weight matrix");
			// TODO publish help on a compbio web site
			p3.setFurtherDetails(new URL("http",
					"www.compbio.dundee.ac.uk/users/pvtroshin/ws/",
					"Index.html"));
			// This attribute is required by strict schema
			p3.setOptionName("--AAMATRIX");
			p3.setRequired(true);

			ValueConstrain vc = new ValueConstrain();
			vc.setType(ValueConstrain.Type.Float);
			vc.setMin("-10.12");
			p3.setValidValue(vc);
			// Max value boundary is not defined so 1 is valid
			p3.setDefaultValue("1");
			p3.validate();
		} catch (MalformedURLException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (WrongParameterException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (ValidationException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	@Test(expectedExceptions = ValidationException.class)
	public void testValidateValueConstrain() throws ValidationException {
		ValueConstrain vc = new ValueConstrain();
		vc.setType(ValueConstrain.Type.Float);
		// NO BOUNDARIES DEFINED
		vc.validate();
	}
}
