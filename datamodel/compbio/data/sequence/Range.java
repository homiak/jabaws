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
package compbio.data.sequence;

public class Range implements Comparable<Range> {

	public final int from;
	public final int to;

	private Range() {
		// JAXB default constructor should not be used
		from = 0;
		to = from;
	}

	public Range(int from, int to) {
		this.from = from;
		this.to = to;
	}

	public Range(String[] twoElementAr) {
		if (twoElementAr == null || twoElementAr.length != 2) {
			throw new IllegalArgumentException();
		}
		this.from = Integer.parseInt(twoElementAr[0].trim());
		this.to = Integer.parseInt(twoElementAr[1].trim());
	}

	@Override
	public String toString() {
		return from + "-" + to;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + from;
		result = prime * result + to;
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
		Range other = (Range) obj;
		if (from != other.from)
			return false;
		if (to != other.to)
			return false;
		return true;
	}

	@Override
	public int compareTo(Range o) {
		if (o == null)
			return 1;
		return new Integer(this.from).compareTo(new Integer(o.from));
	}

}
