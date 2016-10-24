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

package compbio.data.sequence;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import compbio.util.annotation.Immutable;

/**
 * Multiple sequence alignment.
 * 
 * Does not give any guarantees on the content of individual FastaSequece
 * records. It does not guarantee neither the uniqueness of the names of
 * sequences nor it guarantees the uniqueness of the sequences.
 * 
 * @see FastaSequence
 * @see AlignmentMetadata
 * 
 * @author pvtroshin
 * 
 * @version 1.0 September 2009
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@Immutable
public final class Alignment {

	private AlignmentMetadata metadata;
	private List<FastaSequence> sequences;

	private Alignment() {
		// This has to has a default constructor for JaxB
	}

	/**
	 * @param sequences
	 * @param program
	 * @param gapchar
	 */
	public Alignment(List<FastaSequence> sequences, Program program,
			char gapchar) {
		this.sequences = sequences;
		this.metadata = new AlignmentMetadata(Program.CLUSTAL, gapchar);
	}

	/**
	 * 
	 * @param sequences
	 * @param metadata
	 */
	public Alignment(List<FastaSequence> sequences, AlignmentMetadata metadata) {
		this.sequences = sequences;
		this.metadata = metadata;
	}

	/**
	 * 
	 * @return list of FastaSequence records
	 */
	public List<FastaSequence> getSequences() {
		return sequences;
	}

	/**
	 * 
	 * @return a number of sequence in the alignment
	 */
	public int getSize() {
		return this.sequences.size();
	}

	/**
	 * 
	 * @return AlignmentMetadata object
	 */
	public AlignmentMetadata getMetadata() {
		return metadata;
	}

	@Override
	public String toString() {
		String sseq = "";
		for (FastaSequence fs : getSequences()) {
			sseq += fs.toString() + "\n";
		}
		return sseq;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((metadata == null) ? 0 : metadata.hashCode());
		result = prime * result
				+ ((sequences == null) ? 0 : sequences.hashCode());
		return result;
	}

	/**
	 * Please note that this implementation does not take the order of sequences
	 * into account!
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Alignment)) {
			return false;
		}
		Alignment al = (Alignment) obj;
		if (this.getSize() != al.getSize()) {
			return false;
		}
		if (!this.getMetadata().equals(al.getMetadata())) {
			return false;
		}
		int outerCounter = 0;
		int matchCounter = 0;
		for (FastaSequence fs : getSequences()) {
			outerCounter++;
			for (FastaSequence fs1 : al.getSequences()) {
				if (fs.equals(fs1)) {
					matchCounter++;
					continue;
				}
			}
			// Match for at lease one element was not found!
			if (outerCounter != matchCounter) {
				return false;
			}
		}

		return true;
	}

}
