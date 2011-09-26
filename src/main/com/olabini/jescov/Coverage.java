package com.olabini.jescov;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

public class Coverage {
    private final Context context;
    private final Scriptable scope;
    private final CoverageDebugger coverageDebugger;

    Coverage(Context context, Scriptable scope) {
        this.context = context;
        this.scope = scope;
        this.coverageDebugger = new CoverageDebugger(nameMapper, coverageRewriter);
    }

    public static Coverage on(Context ctx, Scriptable scope) {
        Coverage c = new Coverage(ctx, scope);
        c.initialize();
        return c;
    }

    private void initialize() {
        context.setOptimizationLevel(-1);
        context.setDebugger(coverageDebugger, null);
        coverageDebugger.evaluateCoverageDependencies(context, scope);
    }

    public void done() {
        coverageDebugger.reportCoverage(scope);
    }
}
