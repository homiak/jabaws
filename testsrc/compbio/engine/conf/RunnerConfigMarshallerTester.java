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

package compbio.engine.conf;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.validation.Schema;
import javax.xml.validation.Validator;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.xml.sax.SAXException;

import compbio.metadata.AllTestSuit;
import compbio.metadata.Option;
import compbio.metadata.Parameter;
import compbio.metadata.RunnerConfig;
import compbio.metadata.ValueConstrain;
import compbio.metadata.WrongParameterException;
import compbio.runner.msa.Mafft;

public class RunnerConfigMarshallerTester {

    public static String test_input = AllTestSuit.TEST_DATA_PATH_ABSOLUTE
	    + "MafftParameters.xml";
    public static String test_schema_output = "RunnerConfigSchema.xml";
    public static String test_output = AllTestSuit.OUTPUT_DIR_ABSOLUTE
	    + "MafftParameters.out.xml";

    public static String invalidDoc = AllTestSuit.TEST_DATA_PATH_ABSOLUTE
	    + "InvalidMafftParameters.xml";

    RunnerConfig<Mafft> rconfig = null;
    Parameter<Mafft> matrixParam = null;
    RunnerConfigMarshaller<Mafft> pmarshaller = null;

    @BeforeMethod
    public void setup() {
	// write some parameters programmatically
	try {
	    rconfig = new RunnerConfig<Mafft>();
	    rconfig.setRunnerClassName(Mafft.class.getName());
	    List<Option<Mafft>> prms = new ArrayList<Option<Mafft>>();

	    Parameter<Mafft> p1 = new Parameter<Mafft>("Type",
		    "Type of the sequence (PROTEIN or DNA)");
	    // TODO publish help on a compbio web site

	    p1.setFurtherDetails(new URL("http",
		    "www.compbio.dundee.ac.uk/users/pvtroshin/ws/",
		    "Index.html"));
	    p1.addPossibleValues("PROTEIN", "DNA");
	    p1.setOptionName("-TYPE");
	    p1.setRequired(false);

	    /*
	     * -MATRIX= :Protein weight matrix=BLOSUM, PAM, GONNET, ID or
	     * filename
	     */
	    Option<Mafft> p2 = new Option<Mafft>("MATRIX",
		    "Protein weight matrix");
	    // TODO publish help on a compbio web site

	    p2.setFurtherDetails(new URL("http",
		    "www.compbio.dundee.ac.uk/users/pvtroshin/ws/",
		    "Index.html"));

	    p2.addOptionNames("-jtree"); // "-retree"
	    p2.setRequired(false);

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
	    p3.setDefaultValue("pam");
	    ValueConstrain vc = new ValueConstrain();
	    vc.setType(ValueConstrain.Type.Float);
	    vc.setMin("-10.12");
	    vc.setMax("0");
	    p3.setValidValue(vc);

	    prms.add(p1);
	    prms.add(p2);
	    prms.add(p3);
	    matrixParam = p3;
	    rconfig.setOptions(prms);

	    pmarshaller = new RunnerConfigMarshaller<Mafft>(RunnerConfig.class,
		    Parameter.class, Option.class, ValueConstrain.class);
	} catch (MalformedURLException e) {
	    e.printStackTrace();
	    fail(e.getLocalizedMessage());
	} catch (JAXBException e) {
	    e.printStackTrace();
	    fail(e.getLocalizedMessage());
	} catch (WrongParameterException e) {
	    e.printStackTrace();
	    fail(e.getLocalizedMessage());
	}

    }

    @Test()
    public void testMarshalling() {

	File outfile = new File(this.test_output);
	try {
	    pmarshaller.write(rconfig, new FileOutputStream(outfile));
	} catch (FileNotFoundException e) {
	    e.printStackTrace();
	    fail(e.getLocalizedMessage());
	} catch (JAXBException e) {
	    e.printStackTrace();
	    fail(e.getLocalizedMessage());
	} catch (IOException e) {
	    e.printStackTrace();
	    fail(e.getLocalizedMessage());
	}
	assertTrue("Output file expected, but nothing found!", outfile.exists());
	outfile.delete();
    }

    @Test()
    public void testUnMarshalling() {

	File outfile = new File(this.test_output);
	try {
	    pmarshaller.write(rconfig, new FileOutputStream(outfile));

	    RunnerConfig<?> rconfig = pmarshaller.read(new FileInputStream(
		    outfile), RunnerConfig.class, Parameter.class,
		    Option.class, ValueConstrain.class);
	    assertNotNull(rconfig);
	    assertEquals(rconfig.getParameters().size(), this.rconfig
		    .getParameters().size());
	    assertEquals(rconfig.getRunnerClassName(), this.rconfig
		    .getRunnerClassName());
	    assertTrue(matrixParam.equals(rconfig.getArgument("MATRIX1")));
	    assertFalse(matrixParam.equals(rconfig.getArgument("Type")));
	} catch (FileNotFoundException e) {
	    e.printStackTrace();
	    fail(e.getLocalizedMessage());
	} catch (JAXBException e) {
	    e.printStackTrace();
	    fail(e.getLocalizedMessage());
	} catch (IOException e) {
	    e.printStackTrace();
	    fail(e.getLocalizedMessage());
	}
	// outfile.delete();
    }

    @Test()
    public void testValidation() {
	try {
	    System.out.println("CCCC " + rconfig);
	    // write schema
	    pmarshaller.generateSchema(AllTestSuit.OUTPUT_DIR_ABSOLUTE,
		    test_schema_output);

	    File schemafile = new File(AllTestSuit.OUTPUT_DIR_ABSOLUTE,
		    test_schema_output);
	    assertTrue(schemafile.exists());
	    // document is NOT valid even against a loose schema as elements in
	    // java are annotated as required
	    Validator looseValidator = RunnerConfigMarshaller
		    .getValidator(schemafile.getAbsolutePath());

	    // write output xml file
	    File outfile = new File(this.test_output);
	    pmarshaller.write(rconfig, new FileOutputStream(outfile));

	    assertTrue("Invalid output is NOT expected", RunnerConfigMarshaller
		    .validate(looseValidator, test_output));

	    Schema strictSchema = RunnerConfigMarshaller
		    .getSchema(AllTestSuit.TEST_DATA_PATH_ABSOLUTE
			    + File.separator + "RunnerConfigSchema.xsd");

	    Validator strictVal = RunnerConfigMarshaller
		    .getValidator(strictSchema);

	    // document is invalid against strict schema
	    assertFalse("Invalid output is expected", RunnerConfigMarshaller
		    .validate(strictVal, invalidDoc));

	    // schemafile.delete();
	    // outfile.delete();

	} catch (MalformedURLException e) {
	    e.printStackTrace();
	    fail(e.getLocalizedMessage());
	} catch (JAXBException e) {
	    e.printStackTrace();
	    fail(e.getLocalizedMessage());
	} catch (IOException e) {
	    e.printStackTrace();
	    fail(e.getLocalizedMessage());
	} catch (SAXException e) {
	    e.printStackTrace();
	    fail(e.getMessage());
	}

    }

    @Test(expectedExceptions = JAXBException.class)
    public void testValidationOnMarshalling() throws SAXException,
	    JAXBException {
	// This is not valid parameter
	Parameter<Mafft> p = new Parameter<Mafft>("MATRIXXX",
		"Protein weight matrix");
	// This attribute is required by strict schema
	// p.setOptionName("-M");
	p.setRequired(true);
	rconfig.addParameter(p);
	try {

	    // strict schema invalidate this document and throw an exception
	    // just discard the output
	    pmarshaller.writeAndValidate(rconfig,
		    AllTestSuit.TEST_DATA_PATH_ABSOLUTE + File.separator
			    + "RunnerConfigSchema.xsd",
		    new ByteArrayOutputStream());

	    fail("Exception has been thrown before this place in unreachable");

	} catch (MalformedURLException e) {
	    e.printStackTrace();
	    fail(e.getLocalizedMessage());
	} catch (IOException e) {
	    e.printStackTrace();
	    fail(e.getLocalizedMessage());
	}
    }

    @Test()
    public void testSchemaFromCodeGeneration() {
	try {

	    pmarshaller.generateSchema(AllTestSuit.OUTPUT_DIR_ABSOLUTE,
		    test_schema_output);
	} catch (JAXBException e) {
	    e.printStackTrace();
	    fail(e.getLocalizedMessage());
	} catch (IOException e) {
	    e.printStackTrace();
	    fail(e.getLocalizedMessage());
	}
	File schemafile = new File(AllTestSuit.OUTPUT_DIR_ABSOLUTE,
		test_schema_output);
	assertTrue("Schema file expected but not found", schemafile.exists());
	assertTrue("Schema file seems to be empty", schemafile.length() > 50);
	// schemafile.delete();
    }

}
