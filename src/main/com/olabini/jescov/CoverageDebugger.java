/*
 * See LICENSE file in distribution for copyright and licensing information.
 */
package com.olabini.jescov;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.debug.DebugFrame;
import org.mozilla.javascript.debug.DebuggableScript;
import org.mozilla.javascript.debug.Debugger;

public class CoverageDebugger implements Debugger {
	private final CoverageNameMapper nameMapper;
	private final CoverageRewriter coverageRewriter;
	private final Set<String> mappedSourceCode = new HashSet<String>();
	private final Set<String> currentlyMapping = new HashSet<String>();
	private final Configuration configuration;

	public CoverageDebugger(final Context context,
			final Configuration configuration) {
		this.nameMapper = new CoverageNameMapper();
		this.coverageRewriter = new CoverageRewriter(nameMapper, context);
		this.configuration = configuration;
	}

	void evaluateCoverageDependencies(final Context cx, final Scriptable scope) {
		if (configuration.isEnabled()) {
			mappedSourceCode.add(LcovDefinition.SOURCE);
			cx.evaluateString(scope, LcovDefinition.SOURCE,
					LcovDefinition.SOURCE_NAME, 0, null);
		}
	}

	@Override
	public DebugFrame getFrame(final Context cx,
			final DebuggableScript fnOrScript) {
		return null;
	}

	@Override
	public void handleCompilationDone(final Context cx,
			final DebuggableScript fnOrScript, final String source) {
		if (configuration.isEnabled()
				&& configuration.allow(fnOrScript.getSourceName())) {
			if (mappedSourceCode.add(source)) {
				final String sourceName = fnOrScript.getSourceName();
				if (currentlyMapping.add(sourceName)) {
					try {
						coverageRewriter.rewrite(fnOrScript, source);
					} finally {
						currentlyMapping.remove(sourceName);
					}
				}
			}
		}
	}

	private CoverageData coverageData;

	void generateCoverageData(final Scriptable scope) {
		if (configuration.isEnabled()) {
			final Map<String, Map<Integer, LineCoverage>> coverageResults = new HashMap<String, Map<Integer, LineCoverage>>();
			final Map<String, Collection<BranchCoverage>> coverageResults2 = new HashMap<String, Collection<BranchCoverage>>();
			final NativeArray na = (NativeArray) (((Scriptable) scope.get(
					"LCOV", scope)).get("collectedCoverageData", scope));
			for (final Object coverage : na) {
				generateLineCoverage((Scriptable) coverage, scope,
						coverageResults);
			}
			final NativeArray na2 = (NativeArray) (((Scriptable) scope.get(
					"BCOV", scope)).get("collectedCoverageData", scope));
			for (final Object coverage : na2) {
				generateBranchCoverage((Scriptable) coverage, scope,
						coverageResults2);
			}

			final Set<String> allFileNames = new HashSet<String>();
			allFileNames.addAll(coverageResults.keySet());
			allFileNames.addAll(coverageResults2.keySet());
			final List<FileCoverage> result = new ArrayList<FileCoverage>();
			for (final String fileName : allFileNames) {
				final Map<Integer, LineCoverage> lineCoverage = coverageResults
						.get(fileName);
				final Collection<BranchCoverage> branchCoverage = coverageResults2
						.get(fileName);
				result.add(new FileCoverage(fileName,
						lineCoverage == null ? Collections
								.<LineCoverage> emptySet() : lineCoverage
								.values(), branchCoverage == null ? Collections
								.<BranchCoverage> emptySet() : branchCoverage));
			}
			coverageData = new CoverageData(result);
		}
	}

	private void generateLineCoverage(final Scriptable coverage,
			final Scriptable scope,
			final Map<String, Map<Integer, LineCoverage>> coverageResults) {
		final Map<Integer, LineCoverage> lineResults = new HashMap<Integer, LineCoverage>();
		final int functionId = (int) Context.toNumber(coverage.get(
				"functionId", scope));
		final String filename = nameMapper.unmap(functionId);
		coverageResults.put(filename, lineResults);
		final List<?> availableLines = (NativeArray) coverage.get("foundLines",
				scope);
		for (final Object o : availableLines) {
			final int line = (int) Context.toNumber(o);
			final int hits = (int) Context.toNumber(coverage.get(line, scope));
			lineResults.put(line, new LineCoverage(line, hits));
		}
	}

	private void generateBranchCoverage(final Scriptable coverage,
			final Scriptable scope,
			final Map<String, Collection<BranchCoverage>> coverageResults) {
		final Collection<BranchCoverage> branchResults = new LinkedList<BranchCoverage>();
		final int functionId = (int) Context.toNumber(coverage.get(
				"functionId", scope));
		final String filename = nameMapper.unmap(functionId);
		coverageResults.put(filename, branchResults);
		final List<?> availableLines = (NativeArray) coverage.get(
				"foundBranches", scope);
		for (final Object o : availableLines) {
			final int line = (int) Context.toNumber(((List) o).get(0));
			final int branch = (int) Context.toNumber(((List) o).get(1));
			final List cov = (List) coverage.get(branch, scope);
			final int[] covResult = new int[cov.size()];
			for (int i = 0; i < cov.size(); i++) {
				covResult[i] = (int) Context.toNumber(cov.get(i));
			}
			branchResults.add(new BranchCoverage(line, branch, covResult));
		}
	}

	public CoverageData getCoverageData() {
		return coverageData;
	}
}// CoverageDebugger
