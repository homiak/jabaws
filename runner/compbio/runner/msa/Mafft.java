/* Copyright (c) 2011 Peter Troshin
 *  
 *  JAva Bioinformatics Analysis Web Services (JABAWS) @version: 2.0     
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

package compbio.runner.msa;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;

import compbio.data.sequence.Alignment;
import compbio.data.sequence.UnknownFileFormatException;

import compbio.engine.client.PipedExecutable;
import compbio.engine.client.SkeletalExecutable;
import compbio.metadata.ResultNotAvailableException;
import compbio.runner.Util;

/**
 * 
 * @author pvtroshin
 *
 */
public class Mafft extends SkeletalExecutable<Mafft>
		implements
			PipedExecutable<Mafft> {
	/*
	 * TODO get rid of piping: Mafft now supports --out option for output file. 
     * Multi-threading supported with e.g. "thread 4" only for Linux, so JABAWS 
     * will not support it for now, it also need editing of mafft makefile  
	 */
	
	
	private static Logger log = Logger.getLogger(Mafft.class);

	private static String autoOption = "--auto";

	private final String MATRIX_PAR_NAME = "--aamatrix";

	public static final String KEY_VALUE_SEPARATOR = Util.SPACE;

	public Mafft() {
		// remove default input to prevent it to appear in the parameters list
		// that could happen if the parameters are set first
		// super.setInput("");
		addParameters(Arrays.asList("--clustalout", autoOption));
	}

	@SuppressWarnings("unchecked")
	public Alignment getResults(String workDirectory)
			throws ResultNotAvailableException {
		try {
			return Util.readClustalFile(workDirectory, getOutput());
		} catch (FileNotFoundException e) {
			log.error(e.getMessage(), e.getCause());
			throw new ResultNotAvailableException(e);
		} catch (IOException e) {
			log.error(e.getMessage(), e.getCause());
			throw new ResultNotAvailableException(e);
		} catch (UnknownFileFormatException e) {
			log.error(e.getMessage(), e.getCause());
			throw new ResultNotAvailableException(e);
		} catch (NullPointerException e) {
			log.error(e.getMessage(), e.getCause());
			throw new ResultNotAvailableException(e);
		}
	}

	@Override
	public Mafft setInput(String inFile) {
		super.setInput(inFile);
		cbuilder.setLast(inFile);
		return this;
	}

	/**
	 * Mafft input must always be the last parameter!
	 */
	@Override
	public Mafft addParameters(List<String> parameters) {
		cbuilder.addParams(parameters);
		cbuilder.removeParam(autoOption);
		return this;
	}


	
	@SuppressWarnings("unchecked")
	@Override
	public Class<Mafft> getType() {
		return (Class<Mafft>) this.getClass();
	}

	/*
	 * @Override public List<String> getParameters(
	 * compbio.runner.Executable.ExecProvider provider) { for (int i = 0; i <
	 * param.size(); i++) { String par = param.get(i); if
	 * (isMatrixParameter(par)) { String matrixName = getValue(i); if (new
	 * File(matrixName).isAbsolute()) { // Matrix can be found so no actions
	 * necessary // This method has been called already and the matrix name //
	 * is modified to contain full path // no further actions is necessary
	 * break; } String matrixPath = Util.getExecProperty("matrix.path", this);
	 * String absMatrixPath = Util.convertToAbsolute(matrixPath); param.remove(i
	 * + 1); param.remove(i); super.addParameter(MATRIX_PAR_NAME);
	 * super.addParameter(absMatrixPath + File.separator + matrixName); break; }
	 * } return super.getParameters(provider); }
	 * 
	 * boolean isMatrixParameter(String parameter) { assert
	 * !compbio.util.Util.isEmpty(parameter); if
	 * (parameter.toUpperCase().startsWith(MATRIX_PAR_NAME)) { return true; }
	 * return false; }
	 * 
	 * String getValue(int i) { return param.get(i + 1); }
	 */
}
