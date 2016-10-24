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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;

@XmlAccessorType(XmlAccessType.FIELD)
public class ScoreManager {

	@XmlTransient
	public static final String SINGLE_ENTRY_KEY = "Alignment";

	private List<ScoreHolder> seqScores;

	private ScoreManager() {
		// Default JAXB constructor
	}

	private ScoreManager(String id, Set<Score> data) {
		seqScores = new ArrayList<ScoreManager.ScoreHolder>();
		seqScores.add(new ScoreHolder(id, data));
	}

	private ScoreManager(Map<String, Set<Score>> data) {
		List<ScoreHolder> seqScores = new ArrayList<ScoreHolder>();
		for (Map.Entry<String, Set<Score>> singleSeqScores : data.entrySet()) {
			seqScores.add(new ScoreHolder(singleSeqScores.getKey(),
					singleSeqScores.getValue()));
		}
		this.seqScores = seqScores;
	}

	public static ScoreManager newInstance(Map<String, Set<Score>> data) {
		return new ScoreManager(data);
	}

	public static ScoreManager newInstanceSingleScore(
			Map<String, Score> seqScoresMap) {
		Map<String, Set<Score>> multipleScoresMap = new TreeMap<String, Set<Score>>();
		for (Map.Entry<String, Score> seqScore : seqScoresMap.entrySet()) {
			Set<Score> scores = new TreeSet<Score>();
			scores.add(seqScore.getValue());
			multipleScoresMap.put(seqScore.getKey(), scores);
		}
		return new ScoreManager(multipleScoresMap);
	}

	public static ScoreManager newInstanceSingleSequence(Set<Score> data) {
		return new ScoreManager(ScoreManager.SINGLE_ENTRY_KEY,
				new TreeSet(data));
	}

	public Map<String, TreeSet<Score>> asMap() {
		Map<String, TreeSet<Score>> seqScoresMap = new TreeMap<String, TreeSet<Score>>();
		for (ScoreHolder sch : this.seqScores) {
			TreeSet<Score> oldValue = seqScoresMap.put(sch.id, new TreeSet(
					sch.scores));
			if (oldValue != null) {
				throw new IllegalStateException(
						"Cannot represent this ScoreManager instance "
								+ "as a Map as it contains duplicated keys: "
								+ sch.id);
			}
		}
		return seqScoresMap;
	}

	public Set<Score> asSet() {
		if (seqScores.size() == 0 || seqScores.size() > 1) {
			throw new IllegalStateException(
					"This ScoreManager has no or multiple sequence entries and thus "
							+ "cannot be represented as a Set. Number of entries are: "
							+ seqScores.size());
		}
		ScoreHolder sch = seqScores.get(0);
		return sch.scores;
	}

	public int getNumberOfSeq() {
		return seqScores.size();
	}

	public ScoreHolder getAnnotationForSequence(String seqId) {
		for (ScoreHolder sch : seqScores) {
			if (sch.id.equals(seqId)) {
				return sch;
			}
		}
		return null;
	}

	public void writeOut(Writer outStream) throws IOException {
		assert outStream != null : "Output steam is not defined!";
		if (seqScores == null) {
			return;
		}
		for (ScoreHolder oneSeqScores : seqScores) {
			if (oneSeqScores == null)
				continue;
			oneSeqScores.writeOut(outStream);
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((seqScores == null) ? 0 : seqScores.hashCode());
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
		ScoreManager other = (ScoreManager) obj;
		if (seqScores == null) {
			if (other.seqScores != null)
				return false;
		} else if (!seqScores.equals(other.seqScores))
			return false;
		return true;
	}

	@XmlAccessorType(XmlAccessType.FIELD)
	public static class ScoreHolder {

		public String id;
		public TreeSet<Score> scores;

		private ScoreHolder() {
			// JAXB Default constructor should not be used otherwise
		}

		ScoreHolder(String id, Set<Score> scores) {
			this.id = id;
			this.scores = new TreeSet<Score>(scores);
		}

		public void writeOut(Writer writer) throws IOException {
			writer.write(">" + id + "\n");
			Score.write(scores, writer);
		}

		public Score getScoreByMethod(Enum<?> method) {
			for (Score sc : scores) {
				if (method.toString().equals(sc.getMethod())) {
					return sc;
				}
			}
			return null;
		}
		public Score getScoreByMethod(String method) {
			for (Score sc : scores) {
				if (method.toString().equals(sc.getMethod())) {
					return sc;
				}
			}
			return null;
		}
		public int getNumberOfScores() {
			return scores.size();
		}

		@Override
		public int hashCode() {
			final int prime = 17;
			int result = 1;
			result = prime * result + ((id == null) ? 0 : id.hashCode());
			result = prime * result
					+ ((scores == null) ? 0 : scores.hashCode());
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
			ScoreHolder other = (ScoreHolder) obj;
			if (id == null) {
				if (other.id != null)
					return false;
			} else if (!id.equals(other.id))
				return false;
			if (scores == null) {
				if (other.scores != null)
					return false;
			} else if (!scores.equals(other.scores))
				return false;
			return true;
		}

	}

}
