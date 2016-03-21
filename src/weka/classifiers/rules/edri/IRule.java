package weka.classifiers.rules.edri;

import com.google.common.base.MoreObjects;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by suhel on 19/03/16.
 */


public class IRule {
    public final static int EMPTY = -1;
    public final int label;
    private int[] attIndexes;
    private int[] attValues;

    private int correct;
    private int errors;
    private int covers;

    public int getErrors() {
        return errors;
    }

    public int getCorrect() {
        return correct;
    }

    public int getCovers() {
        return covers;
    }

    public double getConfidence() {
        return (double) correct / (double) covers;
    }

    public int getLenght() {
        return attIndexes.length;
    }

    private void resetCounters() {
        this.correct = 0;
        this.errors = 0;
        this.covers = 0;
    }

    public IRule(int label) {
        assert label != EMPTY;
        this.label = label;
        this.attIndexes = new int[0];
        this.attValues = new int[0];
    }

//    public IRule(MaxIndex mi) {
//        this(mi.getLabel());
//        this.correct = mi.getBestCorrect();
//        this.covers = mi.getBestCover();
//        this.errors = covers - correct;
//    }


    /**
     * reset counters,
     * keep not covered lines of the input data
     * and returen covered lines
     *
     * @param lineData
     * @return Set of not covered lines
     */
    public Set<int[]> removeAndGetCovered(Set<int[]> lineData, int resultSize) {
        Set<int[]> coveredLines = new HashSet<>(resultSize);
        resetCounters();


        for (Iterator<int[]> iter = lineData.iterator(); iter.hasNext(); ) {
            int[] line = iter.next();
            int lbl = classify(line);
            if(lbl == EMPTY){ //rule does not cover line
                coveredLines.add(line);
                iter.remove();
            }else if(lbl == line[label]){
                correct++;
            }else errors ++;
        }


        return coveredLines;
    }

    /**
     * Filter the lineData to keep only lines satisfies this rule
     * Reset correct, covers, and errors
     *
     * @param lineData
     * @return Set of not covered lines
     */
    public Set<int[]> keepCoveredBy(Set<int[]> lineData) {
        return keepCoveredBy(lineData, this);
    }

    public static Set<int[]> keepCoveredBy(Set<int[]> lineData, IRule rule) {
        rule.resetCounters();
        final int label = rule.label;

        Set<int[]> result = new HashSet<>(lineData.size());

        for (Iterator<int[]> iter = lineData.iterator(); iter.hasNext(); ) {
            int[] line = iter.next();
            int lbl = rule.classify(line);
            if (lbl == IRule.EMPTY) { //rule can not classify line
                rule.covers++;
                iter.remove();
                result.add(line);
            } else if (lbl == label) {
                rule.correct++;
            } else {
                rule.errors++;
            }
        }
        assert rule.covers == rule.correct + rule.errors;
        return result;
    }

    ;

    public void updateWith(MaxIndex maxIndex) {
        assert this.label == maxIndex.getLabel();
        this.correct = maxIndex.getBestCorrect();
        this.covers = maxIndex.getBestCover();
        this.errors = covers - correct;
    }

    private static int[] addElement(int[] a, int e) {
        a = Arrays.copyOf(a, a.length + 1);
        a[a.length - 1] = e;
        return a;
    }


    public IRule copy() {
        IRule result = new IRule(this.label);
        result.attIndexes = this.attIndexes.clone();
        result.attValues = this.attValues.clone();
        result.correct = this.correct;
        result.errors = this.errors;
        result.covers = this.covers;
        return result;
    }

    public static boolean contains(int[] arr, int att) {
        for (int i = 0; i < arr.length; i++)
            if (arr[i] == att) return true;
        return false;
    }

    public boolean addTest(int att, int val) {
        if (contains(attIndexes, att))
            return false;
        attIndexes = addElement(attIndexes, att);
        attValues = addElement(attValues, val);
        return true;
    }

    public int classify(int[] cond) {
        if (attIndexes.length == 0) return label;

        for (int index = 0; index < attIndexes.length; index++) {
            if (attValues[index] != cond[attIndexes[index]]) {
                return EMPTY;
            }
        }
        return label;
    }


    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("label", label)
                .add("index", attIndexes)
                .add("val", attValues)
                .add("correct", correct)
                .add("errors", errors)
                .add("covers", covers)
                .toString();

    }
}

class LinesIRule {
    final public IRule rule;
    final public Set<int[]> lines;

    LinesIRule(IRule rule, Set<int[]> lines) {
        this.rule = rule;
        this.lines = lines;
    }
}
