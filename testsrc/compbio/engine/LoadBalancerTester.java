package compbio.engine;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import org.testng.annotations.Test;

import compbio.data.sequence.FastaSequence;
import compbio.data.sequence.SequenceUtil;
import compbio.engine.client.ConfiguredExecutable;
import compbio.engine.client.Executable.ExecProvider;
import compbio.metadata.AllTestSuit;
import compbio.metadata.JobSubmissionException;
import compbio.metadata.Limit;
import compbio.metadata.PresetManager;
import compbio.runner.msa.ClustalW;

public class LoadBalancerTester {

	@Test
	public void testLoadBalance() {
		/**
		 * This is 5 sequence x per 20000 length
		 */
		String test_input = AllTestSuit.TEST_DATA_PATH_ABSOLUTE
				+ "testlimit.fasta";
		try {
			FileInputStream fio = new FileInputStream(test_input);
			List<FastaSequence> data = SequenceUtil.readFasta(fio);
			fio.close();
			assertNotNull(data);
			assertTrue(data.size() > 0);

			ClustalW clustal = new ClustalW();
			/**
			 * ClustalW local limit is 2 sequences per 500 letters
			 */
			ConfiguredExecutable<ClustalW> confClust = Configurator
					.configureExecutable(clustal);
			ExecProvider aEngine = LoadBalancer.getEngine(confClust, data);
			Limit<ClustalW> locExec = new Limit<ClustalW>(2, 500, PresetManager.LOCAL_ENGINE_LIMIT_PRESET); 
			// For testing of production configuration uncomment
			//Limit locExec = confClust
			//		.getLimit(PresetManager.LOCAL_ENGINE_LIMIT_PRESET);
			assertTrue(locExec.getSeqNumber() <= data.size()
					|| locExec.getAvgSeqLength() * locExec.getSeqNumber() <= Limit
							.getAvgSequenceLength(data));
			// Engine will be local because LoadBalancer accessed the presets & limits 
			// directly
			// For testing of production configuration uncomment
			// assertEquals(aEngine, ExecProvider.Cluster);

		} catch (JobSubmissionException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}

	}
}
