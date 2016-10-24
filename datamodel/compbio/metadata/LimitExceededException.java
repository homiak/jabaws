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

package compbio.metadata;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import compbio.data.sequence.FastaSequence;

/**
 * This exception is thrown if the task larger in size that the limit that
 * applies to the calculation.
 * 
 * @see Limit
 * 
 * @author pvtroshin
 * 
 * @version 1.0 February 2010
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class LimitExceededException extends JobSubmissionException {

	private static final long serialVersionUID = 15066952180013505L;

	int numberOfSequencesAllowed;
	int actualNumberofSequences;
	int aSequenceLenghtAllowed;
	int aSequenceLenghtActual;

	public LimitExceededException(String message) {
		super(message);
	}

	public static LimitExceededException newLimitExceeded(Limit<?> limit,
			List<FastaSequence> seqs) {
		String message = "Job exceed size limits, maximum number of sequences must be less than "
				+ limit.getSeqNumber() + "\n";
		if (limit.getAvgSeqLength() != 0) {
			message += "and an average sequence length must not exceed "
					+ limit.getAvgSeqLength() + "\n";
		}
		message += " However, the task contained " + seqs.size()
				+ " sequences " + "\n";
		if (limit.getAvgSeqLength() != 0) {
			message += "and an average sequence length was "
					+ Limit.getAvgSequenceLength(seqs) + "\n";
		}
		return new LimitExceededException(message);
	}

	public int getNumberOfSequencesAllowed() {
		return numberOfSequencesAllowed;
	}

	public int getActualNumberofSequences() {
		return actualNumberofSequences;
	}

	public int getSequenceLenghtAllowed() {
		return aSequenceLenghtAllowed;
	}

	public int getSequenceLenghtActual() {
		return aSequenceLenghtActual;
	}
}
