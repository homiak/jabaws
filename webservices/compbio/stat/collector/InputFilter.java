package compbio.stat.collector;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.apache.log4j.Logger;

import compbio.util.FileUtil;
import compbio.util.Util;
import compbio.ws.client.WSTester;

public class InputFilter {

	private static final Logger log = Logger.getLogger(InputFilter.class);
	/**
	 * Accepts input as valid unless it is a test input
	 * 
	 * @param input
	 * @return
	 */
	static boolean accept(File input) {
		if (input == null)
			return true;
		assert input.isFile() : "Input file is not a file! " + input;
		String[] fastainput = WSTester.fastaInput.split("\n");
		assert fastainput.length == 4;
		String[] aligninput = WSTester.fastaAlignment.split("\n");
		assert aligninput.length == 4;
		// We do now know the type of the input here so must compare with both
		// references
		boolean isReference = compareLines(input, fastainput);
		if (!isReference) {
			isReference = compareLines(input, aligninput);
		}
		// only accept genuine input
		return !isReference;
	}

	private static boolean compareLines(File input, String[] reference) {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(input));
			// only compare first four lines of the file with reference
			// because the reference length is only 4 lines
			for (int i = 0; i < 4; i++) {
				String line = reader.readLine();
				if (Util.isEmpty(line)) {
					return false;
				}
				line = line.trim();
				if (!line.equals(reference[i].trim())) {
					return false;
				}
			}
			reader.close();
		} catch (IOException ioe) {
			log.warn(ioe, ioe.getCause());
		} finally {
			FileUtil.closeSilently(reader);
		}
		return true;
	}
}
