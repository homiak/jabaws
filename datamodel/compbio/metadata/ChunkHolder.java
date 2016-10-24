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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

/**
 * Represents a chunk of a string data together with the position in a file for
 * the next read operation.
 * 
 * @author pvtroshin
 * 
 * @version 1.0 December 2009
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class ChunkHolder {

	String chunk;
	long position;

	private ChunkHolder() {
		// JaxB default constructor
		// should not be used otherwise
	}

	public ChunkHolder(String chunk, long position) {
		if (position < 0) {
			throw new IndexOutOfBoundsException(
					"Position in a file could not be negative! Given value: "
							+ position);
		}
		if (chunk == null) {
			throw new NullPointerException("Chunk must not be NULL!");
		}
		this.chunk = chunk;
		this.position = position;
	}

	public String getChunk() {
		return chunk;
	}

	public long getNextPosition() {
		return position;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		ChunkHolder ch = null;
		if (!(obj instanceof ChunkHolder)) {
			ch = (ChunkHolder) obj;
		}
		if (this.position != ch.position) {
			return false;
		}
		if (!this.chunk.equals(ch.chunk)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		String value = "Position: " + position + "\n";
		value += "Chunk size: " + chunk.length() + "\n";
		value += "Chunk: " + chunk + "\n";
		return value;
	}

	@Override
	public int hashCode() {
		return new Long(position + chunk.hashCode()).intValue();
	}
}
