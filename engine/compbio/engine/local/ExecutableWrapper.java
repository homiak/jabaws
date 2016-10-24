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

package compbio.engine.local;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.log4j.Logger;

import compbio.engine.client.ConfiguredExecutable;
import compbio.engine.client.PathValidator;
import compbio.engine.client.PipedExecutable;
import compbio.engine.client.Util;
import compbio.engine.client.Executable.ExecProvider;
import compbio.engine.local.StreamGobbler.OutputType;
import compbio.metadata.JobStatus;
import compbio.metadata.JobSubmissionException;
import compbio.util.FileUtil;
import compbio.util.SysPrefs;
import compbio.util.annotation.Immutable;

@Immutable
public final class ExecutableWrapper implements
	Callable<ConfiguredExecutable<?>> {

    public static final String PROC_OUT_FILE = "procOutput.txt";
    public static final String PROC_ERR_FILE = "procError.txt";

    private static ExecutorService es;

    private static final Logger log = Logger.getLogger(ExecutableWrapper.class);

    private final ConfiguredExecutable<?> confExec;

    private final ProcessBuilder pbuilder;

    public ExecutableWrapper(ConfiguredExecutable<?> executable,
	    String workDirectory) throws JobSubmissionException {
	this.confExec = executable;
	String cmd = null;
	try {
	    cmd = executable.getCommand(ExecProvider.Local);
	    PathValidator.validateExecutable(cmd);
	} catch (IllegalArgumentException e) {
	    log.error(e.getMessage(), e.getCause());
	    throw new JobSubmissionException(e);
	}
	List<String> params = executable.getParameters().getCommands();
	params.add(0, cmd);

	pbuilder = new ProcessBuilder(params);
	if (executable.getEnvironment() != null) {
	    log.debug("Setting command environment variables: "
		    + pbuilder.environment());
	    Util.mergeEnvVariables(pbuilder.environment(), executable
		    .getEnvironment());
	    log.debug("Process environment:" + pbuilder.environment());
	}
	log.debug("Setting command: " + pbuilder.command());
	PathValidator.validateDirectory(workDirectory);
	pbuilder.directory(new File(workDirectory));
	log.debug("Current working directory is "
		+ SysPrefs.getCurrentDirectory());
	log.debug("Setting working directory: " + workDirectory);
	// Initialize private executor to dump processes output if any to the
	// file system
	synchronized (log) {
	    if (es == null) {
		// Two threads are necessary for the process to write in two
		// streams error and output
		// simultaneously and hold the stream until exit. If only one
		// thread is used, the second stream may never
		// get access to the thread efficiently deadlocking the
		// proccess!
		this.es = Executors.newCachedThreadPool();
		log
			.debug("Initializing executor for local processes output dump");
		// Make sure that the executors are going to be properly closed
		Runtime.getRuntime().addShutdownHook(new Thread() {
		    @Override
		    public void run() {
			shutdownService();
		    }
		});
	    }
	}
    }

    /**
     * Stops internal executor service which captures streams of native
     * executables. This method is intended for stopping service if deployed in
     * the web application content. There is NO NEED of using this method
     * otherwise as the executor service is taken care of internally.
     */
    public static final void shutdownService() {
	if (es != null) {
	    es.shutdownNow();
	}
    }

    /**
     * It is vital that output and error streams are captured immediately for
     * this call() to succeed. Thus each instance if ExecutableWrapper has 2 its
     * own thread ready to capture the output. If executor could not execute
     * capture immediately this could lead to the call method to stale, as
     * execution could not proceed without output being captured. Every call to
     * call() method will use 2 threads
     */
    @Override
    public ConfiguredExecutable<?> call() throws IOException {
	Process proc = null;
	Future<?> errorf = null;
	Future<?> outputf = null;
	PrintStream errorStream = null;
	PrintStream outStream = null;
	try {
	    log.info("Calculation started at " + System.nanoTime());
	    Util.writeStatFile(confExec.getWorkDirectory(), JobStatus.STARTED
		    .toString());
	    // pb.redirectErrorStream(false);
	    proc = pbuilder.start();

	    /*
	     * any error message?
	     */
	    errorStream = new PrintStream(new File(pbuilder.directory()
		    + File.separator + getError()));
	    StreamGobbler errorGobbler = new StreamGobbler(proc
		    .getErrorStream(), errorStream, OutputType.ERROR);

	    // any output?
	    outStream = new PrintStream(new File(pbuilder.directory()
		    + File.separator + getOutput()));
	    StreamGobbler outputGobbler = new StreamGobbler(proc
		    .getInputStream(), outStream, OutputType.OUTPUT);

	    // kick them off
	    errorf = es.submit(errorGobbler);
	    outputf = es.submit(outputGobbler);

	    // any error???
	    int exitVal = proc.waitFor();
	    log.info("Calculation completed at " + System.nanoTime());
	    Util.writeStatFile(confExec.getWorkDirectory(), JobStatus.FINISHED
		    .toString());
	    // Let streams to write for a little more
	    errorf.get(2, TimeUnit.SECONDS);
	    outputf.get(2, TimeUnit.SECONDS);
	    // Close streams
	    errorStream.close();
	    outStream.close();
	    log.debug("Local process exit value: " + exitVal);
	} catch (ExecutionException e) {
	    // Log and ignore this is not important
	    log.trace("Native Process output threw exception: "
		    + e.getMessage());
	} catch (TimeoutException e) {
	    // Log and ignore this is not important
	    log
		    .trace("Native Process output took longer then 2s to write, aborting: "
			    + e.getMessage());
	} catch (InterruptedException e) {
	    log.error("Native Process was interrupted aborting: "
		    + e.getMessage());
	    proc.destroy();
	    errorf.cancel(true);
	    outputf.cancel(true);
	    // restore interruption status
	    Thread.currentThread().interrupt();
	} finally {
	    if (proc != null) {
		// just to make sure that we do not left anything running
		proc.destroy();
	    }
	    if (errorf != null) {
		errorf.cancel(true);
	    }
	    if (outputf != null) {
		outputf.cancel(true);
	    }
	    FileUtil.closeSilently(log, errorStream);
	    FileUtil.closeSilently(log, outStream);
	}
	return confExec;
    }

    private String getOutput() {
	if (confExec.getOutput() != null
		&& confExec.getExecutable() instanceof PipedExecutable<?>) {
	    return confExec.getOutput();
	}
	return PROC_OUT_FILE;
    }

    private String getError() {
	if (confExec.getError() != null
		&& confExec.getExecutable() instanceof PipedExecutable<?>) {
	    return confExec.getError();
	}
	return PROC_ERR_FILE;
    }

}
