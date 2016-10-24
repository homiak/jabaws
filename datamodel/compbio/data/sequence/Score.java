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

import java.io.IOException;
import java.io.Writer;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.TreeSet;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import compbio.util.annotation.Immutable;

/**
 * A value class for AACon annotation results storage. The objects of this type
 * are immutable
 * 
 * @author pvtroshin
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@Immutable
public class Score implements Comparable<Score> {

	static final NumberFormat NUMBER_FORMAT = NumberFormat
			.getNumberInstance(Locale.UK);
	static {
		NUMBER_FORMAT.setGroupingUsed(false);
		NUMBER_FORMAT.setMaximumFractionDigits(3);
	}
	// This should be Enum<?> but JAXB cannot serialize it.
	private final String method;

	private TreeSet<Range> ranges = new TreeSet<Range>();

	private ArrayList<Float> scores = new ArrayList<Float>(0);

	private Score() {
		// JaXB default constructor
		method = "";
	}

	/**
	 * Instantiate the Score
	 * 
	 * @param method
	 *            the ConservationMethod with which {@code scores} were
	 *            calculated
	 * @param scores
	 *            the actual conservation values for each column of the
	 *            alignment
	 */
	public Score(Enum<?> method, ArrayList<Float> scores) {
		this.method = method.toString();
		this.scores = new ArrayList<Float>(scores);
	}

	/**
	 * @param method
	 *            the ConservationMethod with which {@code scores} were
	 *            calculated
	 * @param scores
	 *            the actual conservation values for each column of the
	 *            alignment
	 * @param ranges
	 *            The set of ranges i.e. parts of the sequence with specific
	 *            function, usually can be calculated based on scores
	 */
	public Score(Enum<?> method, ArrayList<Float> scores, TreeSet<Range> ranges) {
		this.method = method.toString();
		this.ranges = ranges;
		this.scores = scores;
	}

	public Score(Enum<?> method, TreeSet<Range> ranges) {
		this.method = method.toString();
		this.ranges = ranges;
	}

	public Score(Enum<?> method, float[] scores) {
		this.method = method.toString();
		this.scores = toList(scores);
	}

	private ArrayList<Float> toList(float[] values) {
		ArrayList<Float> vlist = new ArrayList<Float>();
		for (float v : values) {
			vlist.add(new Float(v));
		}
		return vlist;
	}
	/**
	 * Returns the ConservationMethod
	 * 
	 * @return the ConservationMethod
	 */
	public String getMethod() {
		return method;
	}

	/**
	 * The column scores for the alignment
	 * 
	 * @return the column scores for the alignment
	 */
	public ArrayList<Float> getScores() {
		return scores;
	}

	/**
	 * Return Ranges if any Collections.EMPTY_SET otherwise
	 * 
	 * @return ordered set of Range
	 */
	public TreeSet<Range> getRanges() {
		return ranges;
	}

	public void setRanges(TreeSet<Range> ranges) {
		this.ranges = ranges;
	}

	@Override
	public String toString() {
		return "Score [method=" + method + ", ranges=" + ranges + ", scores="
				+ scores + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 7;
		int result = 1;
		result = prime * result + ((method == null) ? 0 : method.hashCode());
		result = prime * result + ((ranges == null) ? 0 : ranges.hashCode());
		result = prime * result + ((scores == null) ? 0 : scores.hashCode());
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
		Score other = (Score) obj;
		if (method == null) {
			if (other.method != null)
				return false;
		} else if (!method.equals(other.method))
			return false;
		if (ranges == null) {
			if (other.ranges != null)
				return false;
		} else if (!ranges.equals(other.ranges))
			return false;
		if (scores == null) {
			if (other.scores != null)
				return false;
		} else if (!scores.equals(other.scores))
			return false;
		return true;
	}

	/**
	 * Outputs the List of Score objects into the Output stream. The output
	 * format is as follows:
	 * 
	 * <pre>
	 * {@code
	 * #MethodName [comma separated list of ranges] <space separated list of values>
	 * 	  
	 * For example:
	 * 	 
	 * #KABAT 0.2 0.3 0.2 0 0.645 0.333 1 1 0 0
	 * #SMERFS 0.645 0.333 1 1 0 0 0.2 0.3 0.2 0
	 * #COILS 22-33, 44-56 0.121 3.212
	 * }
	 * </pre>
	 * 
	 * The maximum precision for values is 3 digits, but can be less.
	 * 
	 * @param scores
	 *            the list of scores to output
	 * @param writer
	 * @throws IOException
	 *             if the OutputStream cannot be written into
	 * @throws NullPointerException
	 *             if the output stream is null
	 */
	public static void write(TreeSet<Score> scores, Writer writer)
			throws IOException {
		if (writer == null) {
			throw new NullPointerException("Writer must be provided!");
		}
		for (Score score : scores) {
			writer.write("#" + score.method);
			int count = score.ranges.size();
			for (Range range : score.ranges) {
				count--;
				writer.write(" "+range.toString());
				if (count != 0) {
					writer.write(",");
				}
			}
			for (Float scoreVal : score.scores) {
				writer.write(" "+NUMBER_FORMAT.format(scoreVal));
			}
			writer.write("\n");
			writer.flush();
		}
		writer.flush();
	}

	@Override
	public int compareTo(Score o) {
		return this.method.compareTo(o.method);
	}
}
