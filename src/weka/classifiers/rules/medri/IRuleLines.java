package weka.classifiers.rules.medri;

import java.util.Collection;
import java.util.Set;

/**
 * Created by suhel on 23/03/16.
 */
public class IRuleLines {
    final public IRule rule;
    final public Collection<int[]> lines;

    IRuleLines(IRule rule, Collection<int[]> lines) {
        this.rule = rule;
        this.lines = lines;
    }
}