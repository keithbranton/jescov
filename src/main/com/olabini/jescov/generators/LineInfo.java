/*
 * See LICENSE file in distribution for copyright and licensing information.
 */
package com.olabini.jescov.generators;

import java.util.Collection;

import org.apache.commons.lang.StringEscapeUtils;

public class LineInfo {
	private final int lineNumber;
	private final String code;
	private final int hits;
	private final Collection<int[]> branches;

	public LineInfo(final int lineNumber, final String code, final int hits,
			final Collection<int[]> branches) {
		this.lineNumber = lineNumber;
		this.code = code;
		this.hits = hits;
		this.branches = branches;
	}

	public boolean getCanBeCovered() {
		return hits != -1;
	}

	public boolean isCompletelyCovered() {
		return hits > 0 && allBranchesCovered();
	}

	public boolean isBranchCoverage() {
		return branches.size() > 0;
	}

	public String getBranchDescription() {
		if (branches.size() > 1) {
			return blendedCoverage() + " " + separateCoveragePercentage();
		} else {
			return blendedCoverage();
		}
	}

	private String separateCoveragePercentage() {
		final StringBuilder sb = new StringBuilder();
		sb.append("[each condition: ");
		String sep = "";
		for (final int[] branch : branches) {
			int valid = 0;
			int covered = 0;
			for (final int hits : branch) {
				valid++;
				if (hits > 0) {
					covered++;
				}
			}
			sb.append(sep).append(percentage(covered, valid));
			sep = ", ";
		}
		sb.append("].");
		return sb.toString();
	}

	private String blendedCoverage() {
		int valid = 0;
		int covered = 0;
		for (final int[] branch : branches) {
			for (final int hits : branch) {
				valid++;
				if (hits > 0) {
					covered++;
				}
			}
		}

		return percentage(covered, valid) + " (" + covered + "/" + valid + ")";
	}

	private String percentage(final int covered, final int valid) {
		final int percentage = (int) ((((double) covered) / ((double) valid)) * 100);
		return "" + percentage + "%";
	}

	public int getLineNumber() {
		return this.lineNumber;
	}

	public String getCode() {
		return StringEscapeUtils.escapeXml(this.code);
	}

	public int getHits() {
		return this.hits;
	}

	private boolean allBranchesCovered() {
		for (final int[] branch : branches) {
			for (final int hits : branch) {
				if (hits == 0) {
					return false;
				}
			}
		}
		return true;
	}
}
