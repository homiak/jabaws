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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.xml.sax.SAXException;

import compbio.engine.conf.RunnerConfigMarshaller;
import compbio.runner.msa.Mafft;

public class PresetTester {

    public static final String input = AllTestSuit.TEST_DATA_PATH
	    + "MafftPresets.xml";
    public static String test_input = AllTestSuit.TEST_DATA_PATH_ABSOLUTE
	    + "MafftParameters.xml";

    PresetManager<Mafft> presets = null;
    RunnerConfig<Mafft> rconfParams = null;

    @BeforeTest(enabled = true)
    public void loadPresets() {
	try {
	    // Load Preset definitions
	    RunnerConfigMarshaller<Mafft> rconfigPresets = new RunnerConfigMarshaller<Mafft>(
		    PresetManager.class);
	    File infile = new File(input);
	    assertTrue(infile.exists());
	    presets = rconfigPresets.read(new FileInputStream(infile),
		    PresetManager.class);
	    assertNotNull(presets);
	    assertFalse(presets.preset.isEmpty());

	    // Load Parameters definitions
	    File input = new File(this.test_input);
	    assertTrue(input.exists());
	    JAXBContext ctx = JAXBContext.newInstance(RunnerConfig.class);
	    Unmarshaller um = ctx.createUnmarshaller();
	    JAXBElement<RunnerConfig> rconfigParams = um.unmarshal(
		    new StreamSource(input), RunnerConfig.class);
	    rconfParams = rconfigParams.getValue();
	    assertNotNull(rconfParams);

	} catch (JAXBException e) {
	    e.printStackTrace();
	    fail(e.getMessage());
	} catch (FileNotFoundException e) {
	    e.printStackTrace();
	    fail(e.getMessage());
	}
    }

    @Test
    public void marshallPreset() {
	try {
	    RunnerConfigMarshaller<Mafft> rconfig = new RunnerConfigMarshaller<Mafft>(
		    PresetManager.class);
	    PresetManager<Mafft> pman = getPresets();
	    assertNotNull(pman);
	    rconfig.readAndValidate(new FileInputStream(new File(input)),
		    PresetManager.class);
	} catch (JAXBException e) {
	    e.printStackTrace();
	    fail(e.getMessage());
	} catch (IOException e) {
	    e.printStackTrace();
	    fail(e.getMessage());
	} catch (SAXException e) {
	    e.printStackTrace();
	    fail(e.getMessage());
	}
    }

    @Test
    public void validatePresets() {
	assertNotNull(presets);
	assertEquals(presets.getPresets().size(), 6);
	try {
	    for (Preset<Mafft> pr : presets.getPresets()) {
		List<Option<Mafft>> options;
		options = pr.getArguments(rconfParams);
		assertFalse(options.isEmpty());
		if (pr.name.equals("L-INS-i (Accuracy-oriented)")) {
		    assertEquals(options.size(), 2);
		    Option<Mafft> o = options.get(0);
		    if (o.name.equals("Pairwise alignment computation method")) {
			List<String> onames = o.getOptionNames();
			boolean match = false;
			for (String oname : onames) {
			    if (oname.equals("--localpair")) {
				match = true;
				break;
			    }
			}
			assertTrue(match);
		    }
		}
		if (pr.name.equals("NW-NS-PartTree-1 (Speed oriented)")) {
		    assertEquals(options.size(), 4);
		}
	    }
	} catch (WrongParameterException e) {
	    e.printStackTrace();
	    fail(e.getMessage());
	}
    }

    @Test
    public void testPresetWithMuptiOptions() {
	assertNotNull(presets);
	assertEquals(presets.getPresets().size(), 6);
	try {
	    for (Preset<Mafft> pr : presets.getPresets()) {
		List<Option<Mafft>> options;
		options = pr.getArguments(rconfParams);
		assertFalse(options.isEmpty());
		if (pr.name.equals("E-INS-i (Accuracy-oriented)")) {
		    assertEquals(options.size(), 3);
		    Option<Mafft> o = options.get(0);
		    if (o.name.equals("Pairwise alignment computation method")) {
			List<String> onames = o.getOptionNames();
			boolean match = false;
			for (String oname : onames) {
			    if (oname.equals("--genafpair")) {
				match = true;
				break;
			    }
			}
			assertTrue(match);
		    }
		}
		if (pr.name.equals("NW-NS-PartTree-1 (Speed oriented)")) {
		    assertEquals(options.size(), 4);
		}
	    }
	} catch (WrongParameterException e) {
	    e.printStackTrace();
	    fail(e.getMessage());
	}
    }

    private static PresetManager<Mafft> getPresets() {
	Preset<Mafft> preset1 = new Preset<Mafft>();
	preset1.name = "L-INS-i (Accuracy-oriented)";
	preset1.description = "dsfjkg fdjksghkjsgdfh jksdfg sdfgkjhsdfgk kjsdfg ";

	List<String> optionNames = new ArrayList<String>();
	optionNames.add("--localpair");
	optionNames.add("--maxiterate 1000");
	preset1.option = optionNames;
	PresetManager<Mafft> prman = new PresetManager<Mafft>();
	prman.preset = Collections.singletonList(preset1);
	prman.runnerClassName = Mafft.class.getCanonicalName();

	return prman;
    }
}
