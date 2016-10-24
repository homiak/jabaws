package compbio.ws.server;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.log4j.Logger;

import compbio.engine.client.Executable.ExecProvider;
import compbio.engine.client.Util;
import compbio.engine.conf.PropertyHelperManager;
import compbio.runner.disorder.IUPred;
import compbio.runner.msa.Muscle;
import compbio.util.SysPrefs;

/**
 * Run setexecflag.sh script if executable flag is not set
 * 
 * @author pvtroshin
 * 
 */
public class SetExecutableFlag implements ServletContextListener {

	private final Logger log = Logger.getLogger(SetExecutableFlag.class);

	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		// do nothing
	}

	/**
	 * This listener is designed to run only once when the web application is
	 * deployed to set executable flag for binaries.
	 * 
	 * @param arg0
	 *            - ignored
	 */
	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		// Assume at least one of these is configured
		// Do not even bother with cluster execution, sysadmins set the flag
		// themselves
		String command = Util.getCommand(ExecProvider.Local, Muscle.class);
		if (compbio.util.Util.isEmpty(command)) {
			command = Util.getCommand(ExecProvider.Local, IUPred.class);
		}
		boolean isExec = true;
		if (!compbio.util.Util.isEmpty(command)) {
			File file = new File(command);
			isExec = file.canExecute();
		}

		String workDir = PropertyHelperManager.getLocalPath() + "binaries/src";
		String script = "setexecflag.sh";

		// Run only one once if not on Windows
		if (!SysPrefs.isWindows && !isExec) {

			// verify script exist
			File scriptFile = new File(workDir, script);
			if (!scriptFile.exists()) {
				log.debug("Setexecflag.sh script is NOT found in "
						+ scriptFile.getAbsolutePath());
				return;
			} else {
				scriptFile.setExecutable(true);
				log.debug("Setexecflag.sh script is found");
			}

			try {
				log.debug("Executable flag is NOT set for the binaries - attemping to set it");
				ProcessBuilder pb = new ProcessBuilder(scriptFile.getAbsolutePath());
				pb.directory(new File(workDir));
				Process process = pb.start();
				process.waitFor();
				process.destroy();
			} catch (IOException e) {
				log.debug("Failed to execute set executable flag script due to IOException! Please run it manually!");
				log.debug(e.getMessage(), e);
			} catch (InterruptedException e) {
				log.debug("Failed to execute set executable flag script due to Interruption! Please run it manually!");
			}
		} else {
			log.debug("Executable flag is already set for the binaries.");
		}
	}
}
