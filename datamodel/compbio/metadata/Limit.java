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
import javax.xml.bind.annotation.XmlAttribute;

import compbio.data.sequence.FastaSequence;
import compbio.util.SysPrefs;

/**
 * A value object containing a maximum number of sequences and a maximum average
 * sequence length for a preset. Also contains static method for determining the
 * number of sequence and their average length in the List<FastaSequence>
 * 
 * 
 * @author pvtroshin
 * 
 * @version 1.0 January 2010
 * 
 * @param <T>
 *            the type of an executable for which this limit is defined.
 * 
 * @see LimitsManager
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class Limit<T> {

	// Allowed to be null
	private String preset;
	// Cannot be 0 or below
	private int seqNumber;
	// Can be 0 - i.e. undefined
	private int seqLength;

	@XmlAttribute
	boolean isDefault;

	private Limit() {
		// JAXB default constructor
	}

	/**
	 * Instantiate the limit
	 * 
	 * @param seqNumber
	 *            the maximum number of sequences allowed for calculation.
	 *            Required
	 * @param seqLength
	 *            the average length of the sequence, optional
	 * @param preset
	 *            the name of preset if any, optional
	 * @throws IllegalArgumentException
	 *             if the seqNumber is not supplied or the seqLength is negative
	 */
	public Limit(int seqNumber, int seqLength, String preset) {
		if (seqNumber <= 0) {
			throw new IllegalArgumentException(
					"seqNumber - a maximum number of sequences to align must be greater than 0. Value given:"
							+ seqNumber);
		}
		if (seqLength < 0) {
			throw new IllegalArgumentException(
					"seqLength - an average sequence length must be greater than 0. Value given:"
							+ seqLength);
		}
		this.seqNumber = seqNumber;
		this.seqLength = seqLength;
		this.preset = preset;
		this.isDefault = false;
	}

	public Limit(int seqNumber, int seqLength, String preset, boolean isDefault) {
		this(seqNumber, seqNumber, preset);
		this.isDefault = isDefault;
	}

	public String getPreset() {
		return preset;
	}

	/**
	 * 
	 * @return the allowed average sequence length
	 */
	public int getAvgSeqLength() {
		return seqLength;
	}

	/**
	 * 
	 * @return the maximum number of sequences allowed
	 */
	public int getSeqNumber() {
		return seqNumber;
	}

	/**
	 * 
	 * @return true is this is a default limit to be used, false otherwise
	 */
	public boolean isDefault() {
		return isDefault;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((preset == null) ? 0 : preset.hashCode());
		result = prime * result + seqLength;
		result = prime * result + seqNumber;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Limit other = (Limit) obj;
		if (preset == null) {
			if (other.preset != null)
				return false;
		} else if (!preset.equals(other.preset))
			return false;
		if (seqLength != other.seqLength)
			return false;
		if (seqNumber != other.seqNumber)
			return false;
		return true;
	}

	@Override
	public String toString() {
		String value = "";
		if (isDefault) {
			value = "Default Limit" + SysPrefs.newlinechar;
		} else {
			value = "Limit for Preset '" + preset + "'" + SysPrefs.newlinechar;
		}
		value += "Maximum sequence number=" + seqNumber + SysPrefs.newlinechar;
		value += "Average sequence length=" + seqLength + SysPrefs.newlinechar;
		value += SysPrefs.newlinechar;
		return value;
	}

	/*
	 * Calculates total number of letters allowed
	 */
	long numberOfLetters() {
		return this.seqNumber * this.seqLength;
	}

	/**
	 * Checks if the number of sequences or their average length in the dataset
	 * exceeds this limit.
	 * 
	 * @param data
	 *            the dataset to measure
	 * @return true if a limit is exceeded (what is the dataset is larger then
	 *         the limit), false otherwise. First check the number of sequences
	 *         in the dataset and if it exceeds the limit return true
	 *         irrespective of the average length. If the number of sequences in
	 *         the dataset is less than the limit and average length is defined,
	 *         then check whether the total number of letters (number of
	 *         sequence multiplied by the average sequence length) is greater
	 *         then the total number of letters in the dataset. Returns true if
	 *         the total number of letters in the dataset is greater than the
	 *         limit, false otherwise.
	 */
	public boolean isExceeded(List<FastaSequence> data) {
		if (data == null) {
			throw new NullPointerException(
					"List of fasta sequences is expected!");
		}
		if (data.size() > this.seqNumber) {
			return true;
		}
		if (this.seqLength != 0 && data.size() > 0) {
			if ((long) getAvgSequenceLength(data) * data.size() > numberOfLetters()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Calculates an average sequence length of the dataset
	 * 
	 * @param data
	 * @return an average sequence length in the input dataset
	 */
	public static int getAvgSequenceLength(List<FastaSequence> data) {
		long length = 0;
		for (FastaSequence seq : data) {
			length += seq.getLength();
		}
		return (int) (length / data.size());
	}

	void validate() {
		if (this.seqNumber < 1) {
			throw new AssertionError(
					"Maximum number of sequences must be defined and be positive! Set value is: "
							+ this.seqNumber);
		}
		if (this.seqLength != 0 && this.seqLength < 1) {
			throw new AssertionError(
					"Average sequence length must be positive! Set value is: "
							+ this.seqLength);
		}
	}
}
