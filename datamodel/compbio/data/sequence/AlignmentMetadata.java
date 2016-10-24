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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import compbio.util.annotation.Immutable;

/**
 * Alignment metadata e.g. method/program being used to generate the alignment
 * and its parameters
 * 
 * @author pvtroshin
 * 
 * @version 1.0 September 2009
 */
@Immutable
@XmlAccessorType(XmlAccessType.FIELD)
public class AlignmentMetadata {

	private Program program;
	private char gapchar;

	private AlignmentMetadata() {
		// Default no args constructor required for JAXB
	}

	public AlignmentMetadata(Program program, char gapchar) {
		this.program = program;
		this.gapchar = gapchar;
	}

	public Program getProgram() {
		return program;
	}

	public char getGapchar() {
		return gapchar;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof AlignmentMetadata)) {
			return false;
		}
		AlignmentMetadata alm = (AlignmentMetadata) obj;
		if (alm.getProgram() != this.getProgram()) {
			return false;
		}
		if (alm.getGapchar() != this.getGapchar()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		return getProgram().hashCode() * getGapchar() * 13;
	}

}
