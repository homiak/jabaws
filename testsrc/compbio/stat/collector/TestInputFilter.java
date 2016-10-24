package compbio.stat.collector;

import java.io.File;
import java.io.IOException;

import org.testng.Assert;
import org.testng.annotations.Test;

import compbio.metadata.AllTestSuit;

public class TestInputFilter {

	final static String FASTA_INPUT = AllTestSuit.TEST_DATA_PATH_ABSOLUTE
			+ "TO1381.fasta";
	final static String ALN_INPUT = AllTestSuit.TEST_DATA_PATH_ABSOLUTE
			+ "TO1381L.aln";
	final static String TEST_FASTA_INPUT = AllTestSuit.TEST_DATA_PATH_ABSOLUTE
			+ "test_input.fasta";
	final static String TEST_ALIGNMENT_INPUT = AllTestSuit.TEST_DATA_PATH_ABSOLUTE
			+ "test_input.aln";
	@Test
	public void TestInputFilter() {
		InputFilter ifilter = new InputFilter();
		try {
			// Makes sure real files are accepted
			Assert.assertTrue(ifilter.accept(new File(FASTA_INPUT)));
			Assert.assertTrue(ifilter.accept(new File(ALN_INPUT)));

			// .. and test files are not
			Assert.assertFalse(ifilter.accept(new File(TEST_ALIGNMENT_INPUT)));
			Assert.assertFalse(ifilter.accept(new File(TEST_FASTA_INPUT)));
			// does not matter if the file is empty, it is still not a test
			// file!
			Assert.assertTrue(ifilter.accept(File.createTempFile("aaa", "bbb")));
		} catch (IOException e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
	}
}
