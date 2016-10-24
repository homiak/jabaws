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

package compbio.data.msa;

import java.security.InvalidParameterException;
import java.util.List;

import javax.jws.WebParam;
import javax.jws.WebService;

import compbio.data.sequence.Alignment;
import compbio.data.sequence.FastaSequence;
import compbio.metadata.JobSubmissionException;
import compbio.metadata.LimitExceededException;
import compbio.metadata.Option;
import compbio.metadata.Preset;
import compbio.metadata.ResultNotAvailableException;
import compbio.metadata.UnsupportedRuntimeException;
import compbio.metadata.WrongParameterException;

/**
 * Multiple Sequence Alignment (MSA) Web Services Interface
 * 
 * @author pvtroshin
 * 
 *         Date November 2010
 * 
 * @param <T>
 *            executable type / web service type
 */
@WebService(targetNamespace = "http://msa.data.compbio/01/01/2010/")
public interface MsaWS<T> extends JABAService, JManagement, Metadata<T> {

	/**
	 * Align a list of sequences with default settings.
	 * 
	 * Any dataset containing a greater number of sequences or when the average
	 * length of the sequences are greater then defined in the default Limit,
	 * will not be accepted for an alignment operation and
	 * JobSubmissionException will be thrown.
	 * 
	 * @param sequences
	 *            List of FastaSequence objects. The programme does not perform
	 *            any sequence validity checks. Nor does it checks whether the
	 *            sequences names are unique. It is responsibility of the caller
	 *            to make sure of this
	 * @return jobId - unique identifier for the job
	 * @throws JobSubmissionException
	 *             is thrown when the job could not be submitted due to the
	 *             following reasons: 1) The number of sequences in the
	 *             submission or their average length is greater then defined by
	 *             the default Limit. 2) Any problems on the server side e.g. it
	 *             is misconfigured or malfunction, is reported via this
	 *             exception. In the first case the information on the limit
	 *             could be obtained from an exception.
	 * @throws InvalidParameterException
	 *             thrown if input list of FASTA sequences is null or empty
	 * @throws UnsupportedRuntimeException
	 *             thrown if server OS does not support native executables for a
	 *             given web service, e.g. JABAWS is deployed on Windows and
	 *             Mafft service is called
	 * @throws LimitExceededException
	 *             is throw if the input sequences number or their average
	 *             length exceeds what is defined by the limit
	 */
	String align(
			@WebParam(name = "fastaSequences") List<FastaSequence> sequences)
			throws UnsupportedRuntimeException, LimitExceededException,
			JobSubmissionException;

	/**
	 * Align a list of sequences with options.
	 * 
	 * @see Option
	 * 
	 *      Default Limit is used to decide whether the calculation will be
	 *      permitted or denied
	 * 
	 * @param sequences
	 *            List of FastaSequence objects. The programme does not perform
	 *            any sequence validity checks. Nor does it checks whether the
	 *            sequences names are unique. It is responsibility of the caller
	 *            to validate this information
	 * @param options
	 *            A list of Options
	 * @return jobId - unique identifier for the job
	 * @throws JobSubmissionException
	 *             is thrown when the job could not be submitted due to the
	 *             following reasons: 1) The number of sequences in the
	 *             submission or their average length is greater then defined by
	 *             the default Limit. 2) Any problems on the server side e.g. it
	 *             is misconfigured or malfunction, is reported via this
	 *             exception. In the first case the information on the limit
	 *             could be obtained from an exception.
	 * @throws WrongParameterException
	 *             is throws when 1) One of the Options provided is not
	 *             supported, 2) The value of the option is defined outside the
	 *             boundaries. In both cases exception object contain the
	 *             information on the violating Option.
	 * @throws InvalidParameterException
	 *             thrown if input list of FASTA sequence is null or empty
	 * @throws UnsupportedRuntimeException
	 *             thrown if server OS does not support native executables for a
	 *             given web service, e.g. JABAWS is deployed on Windows and
	 *             Mafft service is called
	 * @throws LimitExceededException
	 *             is throw if the input sequences number or their average
	 *             length exceeds what is defined by the limit
	 */
	String customAlign(
			@WebParam(name = "fastaSequences") List<FastaSequence> sequences,
			@WebParam(name = "options") List<Option<T>> options)
			throws UnsupportedRuntimeException, LimitExceededException,
			JobSubmissionException, WrongParameterException;

	/**
	 * Align a list of sequences with preset.
	 * 
	 * Limit for a presetName is used whether the calculation will be permitted
	 * or denied. If no Limit was defined for a presetName, than default limit
	 * is used.
	 * 
	 * @param sequences
	 *            List of FastaSequence objects. The programme does not perform
	 *            any sequence validity checks. Nor does it checks whether the
	 *            sequences names are unique. It is responsibility of the caller
	 *            to validate this information
	 * @param preset
	 *            A list of Options
	 * @return String - jobId - unique identifier for the job
	 * @throws JobSubmissionException
	 *             is thrown when the job could not be submitted due to the
	 *             following reasons: 1) The number of sequences in the
	 *             submission or their average length is greater then defined by
	 *             the default Limit. 2) Any problems on the server side e.g. it
	 *             is misconfigured or malfunction, is reported via this
	 *             exception. In the first case the information on the limit
	 *             could be obtained from an exception.
	 * @throws WrongParameterException
	 *             is throws when 1) One of the Options provided is not
	 *             supported, 2) The value of the option is defined outside the
	 *             boundaries. In both cases exception object contain the
	 *             information on the violating Option.
	 * @throws InvalidParameterException
	 *             thrown if input list of FASTA sequence is null or empty
	 * @throws UnsupportedRuntimeException
	 *             thrown if server OS does not support native executables for a
	 *             given web service, e.g. JABAWS is deployed on Windows and
	 *             Mafft service is called
	 * @throws LimitExceededException
	 *             is throw if the input sequences number or average length
	 *             exceeds what is defined by the limit
	 * @see Preset
	 */
	String presetAlign(
			@WebParam(name = "fastaSequences") List<FastaSequence> sequences,
			@WebParam(name = "preset") Preset<T> preset)
			throws UnsupportedRuntimeException, LimitExceededException,
			JobSubmissionException, WrongParameterException;

	/**
	 * Return the result of the job. This method waits for the job
	 * <code>jobId</code> to complete before return.
	 * 
	 * @param jobId
	 *            a unique job identifier
	 * @return Alignment
	 * @throws ResultNotAvailableException
	 *             this exception is throw if the job execution was not
	 *             successful or the result of the execution could not be found.
	 *             (e.g. removed). Exception could also be thrown due to the
	 *             lower level problems on the server i.e. IOException,
	 *             FileNotFoundException problems as well as
	 *             UnknownFileFormatException.
	 * @throws InvalidParameterException
	 *             thrown if jobId is empty or is not recognised e.g. in invalid
	 *             format
	 */
	Alignment getResult(@WebParam(name = "jobId") String jobId)
			throws ResultNotAvailableException;

}
