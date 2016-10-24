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
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.xml.sax.SAXException;

import compbio.engine.conf.RunnerConfigMarshaller;
import compbio.runner.msa.Mafft;

public class OptionMarshallerTester {

    public static String test_input = AllTestSuit.TEST_DATA_PATH_ABSOLUTE
	    + "MafftParameters.xml";
    public static String test_schema_output = "NextGenMafftOptionsSchema.xml";
    public static String test_output = "MafftOptions.xml.out";
    public static String reWrittenInput = AllTestSuit.OUTPUT_DIR_ABSOLUTE
	    + "rewrittenMafftParams.xml";

    RunnerConfig<Mafft> rconfig = null;
    Option<Mafft> matrixParam = null;

    @BeforeMethod()
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

	    p2.addOptionNames("-jtree");
	    p2.addOptionNames("-jfasta");
	    p2.setRequired(false);

	    Parameter<Mafft> p3 = new Parameter<Mafft>("MATRIX2",
		    "Protein weight matrix");
	    // TODO publish help on a compbio web site
	    p3.setFurtherDetails(new URL("http",
		    "www.compbio.dundee.ac.uk/users/pvtroshin/ws/",
		    "Index.html"));

	    p3.addPossibleValues("BLOSUM", "PAM", "GONNET", "ID");
	    // This attribute is required by strict schema
	    p3.setOptionName("-MATRIX");
	    p3.setRequired(true);
	    p3.setDefaultValue("id");
	    ValueConstrain vc = new ValueConstrain();
	    vc.setType(ValueConstrain.Type.Float);
	    vc.setMin("-10.12");
	    vc.setMax("0");
	    p3.setValidValue(vc);

	    prms.add(p1);
	    prms.add(p2);
	    prms.add(p3);
	    matrixParam = p2;
	    rconfig.setOptions(prms);

	} catch (MalformedURLException e) {
	    e.printStackTrace();
	    fail(e.getLocalizedMessage());
	} catch (WrongParameterException e) {
	    e.printStackTrace();
	    fail(e.getLocalizedMessage());
	}
    }

    @Test(expectedExceptions = { javax.xml.bind.MarshalException.class })
    public void testMarshalling() throws JAXBException {

	File outfile = new File(AllTestSuit.OUTPUT_DIR_ABSOLUTE, test_output);
	try {
	    RunnerConfigMarshaller<Mafft> rmarsh = new RunnerConfigMarshaller<Mafft>(
		    RunnerConfig.class);

	    // This throws an exception
	    // I am not sure why
	    rmarsh.writeAndValidate(rconfig,
		    AllTestSuit.TEST_DATA_PATH_ABSOLUTE + File.separator
			    + "RunnerConfigSchema.xsd", new FileOutputStream(
			    outfile));

	} catch (FileNotFoundException e) {
	    e.printStackTrace();
	    fail(e.getLocalizedMessage());
	} catch (IOException e) {
	    e.printStackTrace();
	    fail(e.getLocalizedMessage());
	} catch (SAXException e) {
	    e.printStackTrace();
	    fail(e.getLocalizedMessage());
	}
	assertTrue("Output file expected, but nothing found!", outfile.exists());
	// outfile.delete();
    }

    @Test()
    public void testUnMarshalling() {
	try {
	    File input = new File(this.test_input);
	    assertTrue(input.exists());
	    JAXBContext ctx = JAXBContext.newInstance(RunnerConfig.class);
	    Unmarshaller um = ctx.createUnmarshaller();
	    JAXBElement<RunnerConfig> rconfig = um.unmarshal(new StreamSource(
		    input), RunnerConfig.class);
	    RunnerConfig<Mafft> runner = rconfig.getValue();
	    assertNotNull(runner);
	    System.out.println(runner);
	    assertFalse(runner.options.isEmpty());
	    assertFalse(runner.parameters.isEmpty());
	    assertEquals(7, runner.options.size());
	    assertEquals(8, runner.parameters.size());
	    Option<Mafft> stypeOption = runner.getArgument("Sequence type");
	    System.out.println(stypeOption);
	    assertNotNull(stypeOption);
	    assertFalse(stypeOption.isRequired);
	    assertEquals("--auto", stypeOption.defaultValue);
	    assertEquals(2, stypeOption.optionNames.size());

	    assertEquals(" ", runner.getPrmSeparator());
	    Option<Mafft> guidetrOption = runner
		    .getArgument("Guide tree rebuild");
	    Parameter<Mafft> guidetr = (Parameter<Mafft>) guidetrOption;
	    ValueConstrain constraint = guidetr.getValidValue();
	    assertEquals("Integer", constraint.type.toString());
	    assertEquals(1, constraint.getMin());
	    assertEquals(100, constraint.getMax());

	    RunnerConfigMarshaller<Mafft> rmarsh = new RunnerConfigMarshaller<Mafft>(
		    RunnerConfig.class);
	    // Now see if we can write a valid document back discard the actual
	    // output only validation is important here
	    rmarsh.write(rconfig,
		    new FileOutputStream(new File(reWrittenInput)));

	    RunnerConfig<Mafft> rc = rmarsh.readAndValidate(
		    new FileInputStream(new File(reWrittenInput)),
		    RunnerConfig.class);
	    assertEquals(runner, rc);

	} catch (JAXBException e) {
	    e.printStackTrace();
	    fail(e.getLocalizedMessage());
	} catch (IOException e) {
	    e.printStackTrace();
	    fail(e.getLocalizedMessage());
	} catch (SAXException e) {
	    e.printStackTrace();
	    fail(e.getLocalizedMessage());
	}
    }
}
