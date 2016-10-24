package compbio.ws.client;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ConnectException;
import java.util.List;

import javax.xml.ws.WebServiceException;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import compbio.data.msa.JABAService;
import compbio.data.msa.SequenceAnnotation;
import compbio.data.sequence.ConservationMethod;
import compbio.data.sequence.FastaSequence;
import compbio.data.sequence.ScoreManager;
import compbio.data.sequence.SequenceUtil;
import compbio.metadata.AllTestSuit;
import compbio.metadata.JobSubmissionException;
import compbio.metadata.LimitExceededException;
import compbio.metadata.PresetManager;
import compbio.metadata.ResultNotAvailableException;
import compbio.metadata.RunnerConfig;
import compbio.metadata.UnsupportedRuntimeException;
import compbio.metadata.WrongParameterException;
import compbio.runner.conservation.AACon;
import compbio.util.SysPrefs;

public class TestAAConWS {

	SequenceAnnotation<AACon> msaws;

	@BeforeTest(groups = {AllTestSuit.test_group_webservices})
	void initConnection() {
		/*
		 * URL url = null; try { url = new
		 * URL("http://localhost:8080/jabaws/AAConWS?wsdl"); } catch
		 * (MalformedURLException e) { e.printStackTrace();
		 * fail(e.getLocalizedMessage()); } String namespace =
		 * "http://msa.data.compbio/01/12/2010/"; QName qname = new
		 * QName(namespace, "AAConWS"); Service serv = Service.create(url,
		 * qname); msaws = serv.getPort(new QName(namespace, "AAConWSPort"),
		 * Annotation.class);
		 */try {
			JABAService client = Jws2Client.connect(
					"http://localhost:8080/jabaws", Services.AAConWS);
			msaws = (SequenceAnnotation<AACon>) client;
		} catch (ConnectException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (WebServiceException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	@Test(groups = {AllTestSuit.test_group_webservices})
	public void testAnalize() throws FileNotFoundException, IOException {

		/*
		 * MsaWS msaws = serv.getPort(new QName(
		 * "http://msa.data.compbio/01/01/2010/", "ClustalWSPort"),
		 * MsaWS.class);
		 */
		// Annotation<AACon> msaws = serv.getPort(new QName(namespace,
		// "ClustalWSPort"), Annotation.class);

		// List<FastaSequence> fsl = SequenceUtil.readFasta(new FileInputStream(
		// AAConTester.test_alignment_input));

		String CURRENT_DIRECTORY = SysPrefs.getCurrentDirectory()
				+ File.separator;

		List<FastaSequence> fsl = SequenceUtil.readFasta(new FileInputStream(
				CURRENT_DIRECTORY + "testsrc" + File.separator + "testdata"
						+ File.separator + "TO1381.fasta.aln"));

		try {
			System.out.println("Pres: " + msaws.getPresets().getPresets());
			String jobId = msaws.analize(fsl);
			System.out.println("J: " + jobId);
			ScoreManager result = msaws.getAnnotation(jobId);
			assertNotNull(result);
			assertEquals(result.asSet().size(), 1);

			assertEquals(result.asSet().iterator().next().getMethod(),
					ConservationMethod.SHENKIN.toString());
			List<Float> scores = result.asSet().iterator().next().getScores();
			assertNotNull(scores);
			assertEquals(scores.size(), 568);

			// Using presets
			PresetManager<AACon> presets = msaws.getPresets();
			jobId = msaws.presetAnalize(fsl,
					presets.getPresetByName("Quick conservation"));
			result = msaws.getAnnotation(jobId);
			assertNotNull(result);
			assertEquals(result.asSet().size(), 13);

			jobId = msaws.presetAnalize(fsl,
					presets.getPresetByName("Slow conservation"));
			result = msaws.getAnnotation(jobId);
			assertNotNull(result);
			assertEquals(result.asSet().size(), 5);

			jobId = msaws.presetAnalize(fsl,
					presets.getPresetByName("Complete conservation"));
			result = msaws.getAnnotation(jobId);
			assertNotNull(result);
			assertEquals(result.asSet().size(), 18);

		} catch (UnsupportedRuntimeException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (LimitExceededException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (JobSubmissionException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (ResultNotAvailableException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (WrongParameterException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}

	}
	@Test(groups = {AllTestSuit.test_group_webservices})
	public void testPresetAnalize() throws FileNotFoundException, IOException {

		String CURRENT_DIRECTORY = SysPrefs.getCurrentDirectory()
				+ File.separator;

		List<FastaSequence> fsl = SequenceUtil.readFasta(new FileInputStream(
				CURRENT_DIRECTORY + "testsrc" + File.separator + "testdata"
						+ File.separator + "TO1381.fasta.aln"));

		try {
			System.out.println("Pres: " + msaws.getPresets().getPresets());

			// Using presets
			PresetManager<AACon> presets = msaws.getPresets();
			String jobId = msaws.presetAnalize(fsl,
					presets.getPresetByName("Quick conservation"));
			ScoreManager result = msaws.getAnnotation(jobId);
			assertNotNull(result);
			assertEquals(result.asSet().size(), 13);

			jobId = msaws.presetAnalize(fsl,
					presets.getPresetByName("Slow conservation"));
			result = msaws.getAnnotation(jobId);
			assertNotNull(result);
			assertEquals(result.asSet().size(), 5);

			jobId = msaws.presetAnalize(fsl,
					presets.getPresetByName("Complete conservation"));
			result = msaws.getAnnotation(jobId);
			assertNotNull(result);
			assertEquals(result.asSet().size(), 18);

		} catch (UnsupportedRuntimeException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (LimitExceededException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (JobSubmissionException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (ResultNotAvailableException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (WrongParameterException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}

	}

	@Test(groups = {AllTestSuit.test_group_webservices})
	public void testCustomAnalize() throws FileNotFoundException, IOException {

		String CURRENT_DIRECTORY = SysPrefs.getCurrentDirectory()
				+ File.separator;

		List<FastaSequence> fsl = SequenceUtil.readFasta(new FileInputStream(
				CURRENT_DIRECTORY + "testsrc" + File.separator + "testdata"
						+ File.separator + "TO1381.fasta.aln"));

		// Using options
		RunnerConfig<AACon> options = msaws.getRunnerOptions();
		// System.out.println(options.getArguments());

		try {
			options.getArgument("Calculation method").setDefaultValue("SMERFS");
			// options.getArgument("SMERFS Column Scoring Method")
			// .setDefaultValue("MAX_SCORE");
			// options.getArgument("SMERFS Gap Threshhold").setDefaultValue("1");
			String jobId = msaws.customAnalize(fsl, options.getArguments());
			ScoreManager result = msaws.getAnnotation(jobId);
			assertNotNull(result);
			assertEquals(result.asSet().size(), 1);
			assertEquals(result.asSet().iterator().next().getScores().get(0),
					0.698f);

			options.getArgument("Calculation method").setDefaultValue("SMERFS");
			options.removeArgument("Normalize");
			System.out.println(options);
			jobId = msaws.customAnalize(fsl, options.getArguments());
			result = msaws.getAnnotation(jobId);
			assertNotNull(result);
			assertEquals(result.asSet().size(), 1);
			assertEquals(result.asSet().iterator().next().getScores().get(0),
					0.401f);

		} catch (WrongParameterException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (UnsupportedRuntimeException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (LimitExceededException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (JobSubmissionException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (ResultNotAvailableException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
}
