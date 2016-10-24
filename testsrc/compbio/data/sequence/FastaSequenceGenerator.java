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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class FastaSequenceGenerator {

	enum SeqType {
		DNA, PROTEIN
	}

	enum ProteinAlphabet {
		A, R, N, D, C, E, Q, G, H, I, L, K, M, F, P, S, T, W, V
	};

	enum DNAAlphabet {
		C, T, G, A, U
	};

	enum Letters {
		q, w, e, r, t, y, u, i, o, p, a, s, d, f, g, h, j, k, l, z, x, c, v, b, n, m
	};

	final SeqType seqtype;
	final int seqNumber;
	final Random rand;

	/**
	 * 
	 * @param type
	 *            of the sequence to be generated one of DNA or PROTEIN
	 * @param seqNumber
	 *            number of sequences to be generated
	 */
	public FastaSequenceGenerator(SeqType type, int seqNumber) {
		this.seqtype = type;
		this.seqNumber = seqNumber;
		this.rand = new Random();
	}

	/**
	 * Generate a list of Fasta formatted sequences with sequence length between
	 * 0.5 to 1 of maxLenght. Name of the sequence as well as the sequence is
	 * generated randomly
	 * 
	 * @param maxSeqLength
	 *            maximum length of generated sequence
	 * @return
	 */
	public List<FastaSequence> generateFasta(int maxSeqLength) {
		List<FastaSequence> fastal = new ArrayList<FastaSequence>();
		FastaSequence seq = null;
		for (int i = 0; i < seqNumber; i++) {
			switch (this.seqtype) {
			case DNA:
				seq = new FastaSequence(generateName(), generateDna(
						maxSeqLength, getRandomNumber(0.5, 0.99)));
				break;
			case PROTEIN:
				seq = new FastaSequence(generateName(), generateProtein(
						maxSeqLength, getRandomNumber(0.5, 0.99)));
				break;
			default:
				throw new AssertionError("Cannot recognise a type!");
			}
			fastal.add(seq);
		}
		return fastal;
	}

	private String generateName() {
		Letters[] letters = Letters.values();
		int max = letters.length - 1;
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 10; i++) {
			sb.append(letters[getRandomNumber(0, max)]);
		}
		return sb.toString();
	}

	private String generateProtein(int length, double variability) {
		ProteinAlphabet[] proteinA = ProteinAlphabet.values();
		int max = proteinA.length - 1;
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < length * variability; i++) {
			sb.append(proteinA[getRandomNumber(max)]);
		}
		return sb.toString();
	}

	private String generateDna(int length, double variability) {
		if (variability == 0) {
			variability = 1;
		}
		DNAAlphabet[] dnaA = DNAAlphabet.values();
		int max = dnaA.length - 1;
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < length * variability; i++) {
			sb.append(dnaA[getRandomNumber(max)]);
		}
		return sb.toString();
	}

	/*
	 * Returns random integers in range from 0 to max
	 * 
	 * @param max
	 * 
	 * @return
	 */
	private int getRandomNumber(int max) {
		return rand.nextInt(max);
	}

	/*
	 * Returns random integers with value in range from min to max
	 * 
	 * @param min
	 * 
	 * @param max
	 * 
	 * @return
	 */
	private int getRandomNumber(int min, int max) {
		return new Long(Math.round((max - min) * rand.nextDouble() + min))
				.intValue();
	}

	private double getRandomNumber(double min, double max) {
		return (max - min) * rand.nextDouble() + min;
	}
}
