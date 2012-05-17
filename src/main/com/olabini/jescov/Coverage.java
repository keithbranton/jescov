package com.olabini.jescov;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

public class Coverage {
	private final Context context;
	private final Scriptable scope;
	private final CoverageDebugger coverageDebugger;

	Coverage(final Context context, final Scriptable scope,
			final Configuration configuration) {
		this.context = context;
		this.scope = scope;
		this.coverageDebugger = new CoverageDebugger(context, configuration);
	}

	public static Coverage on(final Context ctx, final Scriptable scope,
			final Configuration configuration) {
		final Coverage c = new Coverage(ctx, scope, configuration);
		c.initialize();
		return c;
	}

	private void initialize() {
		context.setOptimizationLevel(-1);
		context.setDebugger(coverageDebugger, null);
		coverageDebugger.evaluateCoverageDependencies(context, scope);
	}

	public void done() {
		coverageDebugger.generateCoverageData(scope);
	}

	public CoverageData getCoverageData() {
		return coverageDebugger.getCoverageData();
	}
}
