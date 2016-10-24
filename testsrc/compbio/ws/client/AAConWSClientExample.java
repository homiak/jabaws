package compbio.ws.client;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.List;

import compbio.data.msa.SequenceAnnotation;
import compbio.data.sequence.FastaSequence;
import compbio.data.sequence.ScoreManager;
import compbio.data.sequence.SequenceUtil;
import compbio.metadata.JobSubmissionException;
import compbio.metadata.Preset;
import compbio.metadata.PresetManager;
import compbio.metadata.ResultNotAvailableException;
import compbio.metadata.UnsupportedRuntimeException;
import compbio.metadata.WrongParameterException;
import compbio.runner.conservation.AACon;

/**
 * AAConWS client example
 */
public class AAConWSClientExample {

	/*
	 * Input sequences. For the simplicity keep them in the class
	 */
	static final String input = ">Foo      \r\n"
			+ "MTADGPRELLQLRAAVRHRPQDFVAWLMLADAELGMGDTTAGEMAVQRGLALHPGHPEAV\r\n"
			+ "ARLGRVRWTQQRHAEAAVLLQQASDAAPEHPGIALWLGHALEDAGQAEAAAAAYTRAHQL\r\n"
			+ "LPEEPYITAQLLNWRRRLCDWRALDVLSAQVRAAVAQGVGAVEPFAFLSEDASAAEQLAC\r\n"
			+ "ARTRAQAIAASVRPLAPTRVRSKGPLRVGFVSNGFGAHPTGLLTVALFEALQRRQPDLQM\r\n"
			+ "HLFATSGDDGSTLRTRLAQASTLHDVTALGHLATAKHIRHHGIDLLFDLRGWGGGGRPEV\r\n"
			+ "FALRPAPVQVNWLAYPGTSGAPWMDYVLGDAFALPPALEPFYSEHVLRLQGAFQPSDTSR\r\n"
			+ "VVAEPPSRTQCGLPEQGVVLCCFNNSYKLNPQSMARMLAVLREVPDSVLWLLSGPGEADA\r\n"
			+ "RLRAFAHAQGVDAQRLVFMPKLPHPQYLARYRHADLFLDTHPYNAHTTASDALWTGCPVL\r\n"
			+ "TTPGETFAARVAGSLNHHLGLDEMNVADDAAFVAKAVALASDPAALTALHARVDVLRRES\r\n"
			+ "GVFEMDGFADDFGALLQALARRHGWLGI\r\n"
			+ "\r\n"
			+ ">Bar                    \r\n"
			+ "-----------------------------------MGDTTAGEMAVQRGLALH-------\r\n"
			+ "---------QQRHAEAAVLLQQASDAAPEHPGIALWL-HALEDAGQAEAAAA-YTRAHQL\r\n"
			+ "LPEEPYITAQLLN--------------------AVAQGVGAVEPFAFLSEDASAAE----\r\n"
			+ "----------SVRPLAPTRVRSKGPLRVGFVSNGFGAHPTGLLTVALFEALQRRQPDLQM\r\n"
			+ "HLFATSGDDGSTLRTRLAQASTLHDVTALGHLATAKHIRHHGIDLLFDLRGWGGGGRPEV\r\n"
			+ "FALRPAPVQVNWLAYPGTSGAPWMDYVLGDAFALPPALEPFYSEHVLRLQGAFQPSDTSR\r\n"
			+ "VVAEPPSRTQCGLPEQGVVLCCFNNSYKLNPQSMARMLAVLREVPDSVLWLLSGPGEADA\r\n"
			+ "RLRAFAHAQGVDAQRLVFMPKLPHPQYLARYRHADLFLDTHPYNAHTTASDALWTGCPVL\r\n"
			+ "TTPGETFAARVAGSLNHHLGLDEMNVADDAAFVAKAVALASDPAALTALHARVDVLRRES\r\n"
			+ "GVFEMDGFADDFGALLQALARRHGWLGI\r\n"
			+ "\r\n"
			+ ">Noname             \r\n"
			+ "-MTADGPRELLQLRAAVRHRPQDVAWLMLADAELGMGDTTAGEMAVQRGLALHPGHPEAV\r\n"
			+ "ARLGRVRWTQQRHAEAAVLLQQASDAAPEHPGIALWLGHALED--------------HQL\r\n"
			+ "LPEEPYITAQLDVLSAQVR-------------AAVAQGVGAVEPFAFLSEDASAAEQLAC\r\n"
			+ "ARTRAQAIAASVRPLAPTRVRSKGPLRVGFVSNGFGAHPTGLLTVALFEALQRRQPDLQM\r\n"
			+ "HLFATSGDDGSTLRTRLAQASTLHDVTALGHLATAKHIRHHGIDLLFDLRGWGGGGRPEV\r\n"
			+ "FALRPAPVQVNWLAYPGTSGAPWMDYVLGDAFALPPALEPFYSEHVLRLQGAFQPSDTSR\r\n"
			+ "VVAEPPSRTQCGLPEQGVVLCCFNNSYKLNPQSMARMLAVLREVPDSVLWLLSGPGEADA\r\n"
			+ "RLRAFAHAQGVDAQRLVFMPKLPHPQYLARYRHADLFLDTHPYNAHTTASDALWTGCPVL\r\n"
			+ "TTPGETFAARVAGSLNHHLGLDEMNVADDAAFVAKAVALASDPAALTALHARVDVLRRES\r\n"
			+ "I---------------------------";

	public static void main(String[] args) throws UnsupportedRuntimeException,
			JobSubmissionException, WrongParameterException,
			FileNotFoundException, IOException, ResultNotAvailableException,
			InterruptedException {

		/*
		 * Annotation interface for AAConWS web service instance
		 */
		SequenceAnnotation<AACon> client = (SequenceAnnotation<AACon>) Jws2Client
				.connect("http://www.compbio.dundee.ac.uk/aacon",
						Services.AAConWS);

		/* Get the list of available presets */
		PresetManager presetman = client.getPresets();

		/* Get the Preset object by preset name */
		Preset preset = presetman.getPresetByName("Complete conservation");

		/*
		 * Load sequences in FASTA format from the file You can use something
		 * like new FileInputStream() to load sequence from the file
		 */
		List<FastaSequence> fastalist = SequenceUtil
				.readFasta(new ByteArrayInputStream(input.getBytes()));

		/*
		 * Submit loaded sequences for an alignment using preset. The job
		 * identifier is returned by this method, you can retrieve the results
		 * with it sometime later.
		 */
		String jobId = client.presetAnalize(fastalist, preset);

		/* This method will block for the duration of the calculation */
		ScoreManager result = client.getAnnotation(jobId);

		/*
		 * This is a better way of obtaining results, it does not involve
		 * holding the connection open for the duration of the calculation,
		 * Besides, as the University of Dundee public server will reset the
		 * connection after 10 minutes of idling, this is the only way to obtain
		 * the results of long running task from our public server.
		 */
		// while (client.getJobStatus(jobId) != JobStatus.FINISHED) {
		// Thread.sleep(1000); // wait a second, then recheck the status
		// }

		/* Output the alignment to standard out */
		Writer writer = new PrintWriter(System.out, true);
		IOHelper.writeOut(writer, result);
		writer.close();
		// Score.write(result, System.out);

		/* Alternatively, you can record retrieved alignment into the file */
		// FileOutputStream out = new FileOutputStream("result.txt");
		// Score.write(result, out);
		// out.close();
	}
}
