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

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

import compbio.data.sequence.FastaSequence;
import compbio.data.sequence.ScoreManager;
import compbio.metadata.JobSubmissionException;
import compbio.metadata.LimitExceededException;
import compbio.metadata.Option;
import compbio.metadata.Preset;
import compbio.metadata.ResultNotAvailableException;
import compbio.metadata.UnsupportedRuntimeException;
import compbio.metadata.WrongParameterException;

/**
 * Interface for tools that results to one or more annotation to sequence(s)
 * 
 * Single, multiple sequences their groups or alignments can be annotated
 * 
 * @author Peter Troshin
 * 
 * @param <T>
 *            executable type / web service type
 * 
 * @version 1.0 November 2010
 */
@WebService(targetNamespace = JABAService.V2_SERVICE_NAMESPACE)
public interface SequenceAnnotation<T>
		extends
			JABAService,
			JManagement,
			Metadata<T> {

	/**
	 * 
	 * Analyse the sequences. The actual analysis algorithm is defined by the
	 * type T.
	 * 
	 * Any dataset containing a greater number of sequences or the average
	 * length of the sequences are greater then defined in the default Limit
	 * will not be accepted for an alignment operation and
	 * JobSubmissionException will be thrown.
	 * 
	 * @param sequences
	 *            List of FastaSequence objects. The programme does not perform
	 *            any sequence validity checks. Nor does it checks whether the
	 *            sequences names are unique. It is responsibility of the caller
	 *            to validate this information
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
	 *             thrown if input list of fasta sequence is null or empty
	 * @throws UnsupportedRuntimeException
	 *             thrown if server OS does not support native executables for a
	 *             given web service, e.g. JABAWS is deployed on Windows and
	 *             Mafft service is called
	 * @throws LimitExceededException
	 *             is throw if the input sequences number or average length
	 *             exceeds what is defined by the limit
	 */
	@WebMethod
	String analize(
			@WebParam(name = "fastaSequences") List<FastaSequence> sequences)
			throws UnsupportedRuntimeException, LimitExceededException,
			JobSubmissionException;

	/**
	 * Analyse the sequences according to custom settings defined in options
	 * list. The actual analysis algorithm is defined by the type T. Default
	 * Limit is used to decide whether the calculation will be permitted or
	 * denied
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
	 *             thrown if input list of fasta sequence is null or empty
	 * @throws UnsupportedRuntimeException
	 *             thrown if server OS does not support native executables for a
	 *             given web service, e.g. JABAWS is deployed on Windows and
	 *             Mafft service is called
	 * @throws LimitExceededException
	 *             is throw if the input sequences number or average length
	 *             exceeds what is defined by the limit
	 * @see Option
	 */
	@WebMethod
	String customAnalize(
			@WebParam(name = "fastaSequences") List<FastaSequence> sequences,
			@WebParam(name = "options") List<Option<T>> options)
			throws UnsupportedRuntimeException, LimitExceededException,
			JobSubmissionException, WrongParameterException;

	/**
	 * Analyse the sequences according to the preset settings. The actual
	 * analysis algorithm is defined by the type T.
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
	 *             thrown if input list of fasta sequence is null or empty
	 * @throws UnsupportedRuntimeException
	 *             thrown if server OS does not support native executables for a
	 *             given web service, e.g. JABAWS is deployed on Windows and
	 *             Mafft service is called
	 * @throws LimitExceededException
	 *             is throw if the input sequences number or average length
	 *             exceeds what is defined by the limit
	 */
	@WebMethod
	String presetAnalize(
			@WebParam(name = "fastaSequences") List<FastaSequence> sequences,
			@WebParam(name = "preset") Preset<T> preset)
			throws UnsupportedRuntimeException, LimitExceededException,
			JobSubmissionException, WrongParameterException;

	/**
	 * Return the result of the job.
	 * 
	 * @param jobId
	 *            a unique job identifier
	 * @return the Map with the sequence names, sequence group names or the word
	 *         'Alignment' in case of alignments and values the represented by a
	 *         Set of Score objects. The alignment can be represented in as
	 *         little as one key->value pair in this map, the list of sequences
	 *         will be represented by multiple key->value mappings. If multiple
	 *         annotations were calculated, then they are represented as a Set
	 *         of Scores.
	 * @throws ResultNotAvailableException
	 *             this exception is throw if the job execution was not
	 *             successful or the result of the execution could not be found.
	 *             (e.g. removed). Exception could also be thrown is dues to the
	 *             lower level problems on the server i.e. IOException,
	 *             FileNotFoundException problems as well as
	 *             UnknownFileFormatException.
	 * @throws InvalidParameterException
	 *             thrown if jobId is empty or cannot be recognised e.g. in
	 *             invalid format
	 * 
	 */
	@WebMethod
	ScoreManager getAnnotation(@WebParam(name = "jobId") String jobId)
			throws ResultNotAvailableException;
	/*
	 * The method should really return Map and Set, but unfortunately JAXB
	 * cannot serialize interfaces, has a concrete implementation is used Could
	 * also specify the generic Set e.g. ? extends Set. But this would require
	 * the client cast or operate with generic Set. Keep it simple for now.
	 */
}
