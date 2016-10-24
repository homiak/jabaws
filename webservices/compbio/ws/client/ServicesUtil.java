package compbio.ws.client;

import java.io.File;

import compbio.engine.client.ConfExecutable;
import compbio.engine.client.Executable;
import compbio.runner.conservation.AACon;
import compbio.runner.disorder.Disembl;
import compbio.runner.disorder.GlobPlot;
import compbio.runner.disorder.IUPred;
import compbio.runner.disorder.Jronn;
import compbio.runner.msa.ClustalO;
import compbio.runner.msa.ClustalW;
import compbio.runner.msa.Mafft;
import compbio.runner.msa.Muscle;
import compbio.runner.msa.Probcons;
import compbio.runner.msa.Tcoffee;

public class ServicesUtil {

	public static Services getServiceByRunner(Class<? extends Executable> class1) {
		assert class1 != null;
		String sname = class1.getSimpleName().toLowerCase();
		for (Services service : Services.values()) {
			if (service.toString().toLowerCase().contains(sname)) {
				return service;
			}
		}
		return null;
	}

	public static Class<? extends Executable<?>> getServiceImpl(Services service) {
		switch (service) {
			case AAConWS :
				return AACon.class;
			case ClustalOWS :
				return ClustalO.class;
			case ClustalWS :
				return ClustalW.class;
			case MafftWS :
				return Mafft.class;
			case MuscleWS :
				return Muscle.class;
			case TcoffeeWS :
				return Tcoffee.class;
			case ProbconsWS :
				return Probcons.class;
			case DisemblWS :
				return Disembl.class;
			case GlobPlotWS :
				return GlobPlot.class;
			case JronnWS :
				return Jronn.class;
			case IUPredWS :
				return IUPred.class;
			default :
				throw new RuntimeException(
						"Unknown web service implementation class for service: "
								+ service);
		}
	}

	public static Class<? extends Executable<?>> getRunnerByJobDirectory(
			File jobdir) {
		Services service = getServiceByRunnerName(getRunnerNameByJobDirectory(jobdir));
		return getServiceImpl(service);
	}

	private static String getRunnerNameByJobDirectory(File jobdir) {
		String name = jobdir.getName().split("#")[0];

		if (name.startsWith(ConfExecutable.CLUSTER_TASK_ID_PREFIX)) {
			assert ConfExecutable.CLUSTER_TASK_ID_PREFIX.length() == 1;
			name = name.substring(1);
		}
		return name;
	}

	public static Services getServiceByJobDirectory(File jobdir) {
		return getServiceByRunnerName(getRunnerNameByJobDirectory(jobdir));
	}

	private static Services getServiceByRunnerName(String name) {
		for (Services service : Services.values()) {
			String runnerName = getServiceImpl(service).getSimpleName()
					.toLowerCase();
			name = name.trim().toLowerCase();
			if (name.startsWith(runnerName)) {
				return service;
			}
		}
		return null;
	}

}
