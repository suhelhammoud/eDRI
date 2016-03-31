package weka.classifiers.rules.medri;

import java.util.Collection;
import java.util.Set;

/**
 * Created by suhel on 23/03/16.
 */
public class IRuleLines {
    final public IRule rule;
    final public Collection<int[]> lines;
    final public long scannedInstances;

    public IRuleLines(IRule rule, Collection<int[]> lines, long scannedInstances) {
        this.rule = rule;
        this.lines = lines;
        this.scannedInstances = scannedInstances;
    }

    public IRuleLines(IRule rule, Collection<int[]> lines) {
        this(rule, lines, 0);

    }
}