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

/**
 * Enumeration defining two constraints for SMERFS columns score calculation.
 * MAX_SCORE gives the highest core of all the windows the column belongs to.
 * MID_SCORE gives the window score to the column in the middle.
 * 
 * @author Agnieszka Golicz & Peter Troshin
 */
public enum SMERFSConstraints {

	MAX_SCORE, MID_SCORE;

	/**
	 * Default column scoring schema
	 */
	public static final SMERFSConstraints DEFAULT_COLUMN_SCORE = SMERFSConstraints.MID_SCORE;

	/**
	 * Default window size value for SMERFS algorithm
	 */
	public static final int DEFAULT_WINDOW_SIZE = 7;

	/**
	 * Default gap threshold value for SMERFS algorithm
	 */
	public static final double DEFAULT_GAP_THRESHOLD = 0.1;

	public static SMERFSConstraints getSMERFSColumnScore(String score) {

		score = score.trim().toLowerCase();
		if (score.equalsIgnoreCase(SMERFSConstraints.MAX_SCORE.toString())) {
			return SMERFSConstraints.MAX_SCORE;
		}
		if (score.equalsIgnoreCase(SMERFSConstraints.MID_SCORE.toString())) {
			return SMERFSConstraints.MID_SCORE;
		}
		return null;
	}
}
