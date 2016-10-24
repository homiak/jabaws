package compbio.data.sequence;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import compbio.metadata.AllTestSuit;

public class FastaReaderTester {

	static FastaSequence s0 = new FastaSequence(
			"zedpshvyzg",
			"GCQDKNNIAELNEIMGTTRSPSDWQHMKGASPRAEIGLTGKKDSWWRHCCSKEFNKTPPPIHPDMKRWGWMWNRENFEKFLIDNFLNPPCPRLMLTKGTWWRHEDLCHEIFWSTLRWLCLGNQSFSAMIWGHLCECHRMIWWESNEHMFWLKFRRALKKMNSNGPCMGPDNREWMITNRMGKEFCGPAFAGDCQSCWRKCHKTNKICFNEKKGTPTKIDHEQKDIMDILKDIDNHRNWKQCQLWLLTSKSTDQESTTMLTWSTWRDFFIIIKQPFDHKCRGALDANGDFQIAAELKWPAPMIILRQNQKTMHDKSCHHFFTNRCPLMHTTRANDKQCSWHTRKQFICQQDFTTWQHRPDTHRILPSWCMSTRRKNHIKNTPALAFSTCEMGDLPNGWAPGTIILQRQFTQAIKLPQETTGWPRCDPKFDHWNMSKWLRQLLGRDDEMIPPQCD");

	static FastaSequence s1 = new FastaSequence(
			"xovkactesa",
			"CPLSKWWNRRAFLSHTANHWMILMTWEGPHDGESKMRIAMMKWSPCKPTMSHFRCGLDAWAEPIRQIACESTFRM"
					+ "FCTTPRPIHKLTEMWGHMNGWTGAFCRQLECEWMMPPRHPHPCTSTFNNNKKRLIGQIPNEGKQLFINFQKPQHG"
					+ "FSESDIWIWKDNPTAWHEGLTIAGIGDGQHCWNWMPMPWSGAPTSNALIEFWTWLGMIGTRCKTQGMWWDAMNHH"
					+ "DQFELSANAHIAAHHMEKKMILKPDDRNLGDDTWMPPGKIWMRMFAKNTNACWPEGCRDDNEEDDCGTHNLHRMC");
	static FastaSequence s2 = new FastaSequence(
			"ntazzewyvv",
			"CGCKIF D D NMKDNNRHG TDIKKHGFMH IRHPE KRDDC FDNHCIMPKHRRWGLWD"
					+ "EASINM	AQQWRSLPPSRIMKLNG	HGCDCMHSHMEAD	DTKQSGIKGTFWNG	HDAQWLCRWG"
					+ "EFITEA	WWGRWGAITFFHAH	ENKNEIQECSDQNLKE	SRTTCEIID   TCHLFTRHLDGW"
					+ " RCEKCQANATHMTW ACTKSCAEQW  FCAKELMMN    "
					+ "W        KQMGWRCKIFRKLFRDNCWID  FELPWWPICFCCKGLSTKSHSAHDGDQCRRW    WPDCARDWLGPGIRGEF   "
					+ "FCTHICQQLQRNFWCGCFRWNIEKRMFEIFDDNMAAHWKKCMHFKFLIRIHRHGPITMKMTWCRSGCCFGKTRRLPDSSFISAFLDPKHHRDGSGMMMWSSEMRSCAIPDPQQAWNQGKWIGQIKDWNICFAWPIRENQQCWATPHEMPSGFHFILEKWDALAHPHMHIRQKKCWAWAFLSLMSSTHSDMATFQWAIPGHNIWSNWDNIICGWPRI");
	static FastaSequence s3 = new FastaSequence(" 12 d t y wi 		k	jbke  	",
			"  KLSHHDCD" + "   N" + "    H" + "    HSKCTEPHCGNSHQML\n\rHRDP"
					+ "    CCDQCQSWEAENWCASMRKAILF");
	static FastaSequence s4 = new FastaSequence(" 12 d t>y wi->foo 		k	jbke  	",
			"  KLSHHDCD" + "   N" + "    H" + "    HSKCTEPHCGNSHQML\n\rHRDP"
					+ "    CCDQCQSWEAENWCASMRKAILF");
	@Test()
	public void test() {

		List<FastaSequence> old_seqs = null;
		final List<FastaSequence> list = new ArrayList<FastaSequence>();
		try {
			old_seqs = SequenceUtil.readFasta(new FileInputStream(
					AllTestSuit.TEST_DATA_PATH + "complicated.fasta"));
			final FastaReader fr = new FastaReader(AllTestSuit.TEST_DATA_PATH
					+ "complicated.fasta");

			while (fr.hasNext()) {
				final FastaSequence fs = fr.next();
				list.add(fs);
			}
		} catch (final FileNotFoundException e) {
			e.printStackTrace();
			Assert.fail(e.getLocalizedMessage());
		} catch (final IOException e) {
			e.printStackTrace();
			Assert.fail(e.getLocalizedMessage());
		}
		System.out.println("OLD: " + old_seqs);
		System.out.println("NEW: " + list);
		Assert.assertEquals(old_seqs.size(), list.size());
		Assert.assertEquals(old_seqs.get(0), list.get(0));
		Assert.assertEquals(old_seqs.get(1), list.get(1));
		// Assert.assertEquals(old_seqs.get(2), list.get(2));
		// Assert.assertEquals(seqs.get(3), list.get(3));

		Assert.assertEquals(FastaReaderTester.s0, list.get(0));
		Assert.assertEquals(FastaReaderTester.s1, list.get(1));
		Assert.assertEquals(FastaReaderTester.s2, list.get(2));
		Assert.assertEquals(FastaReaderTester.s3, list.get(3));
		Assert.assertEquals(FastaReaderTester.s4, list.get(4));

	}
}
