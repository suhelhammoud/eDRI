package weka.classifiers.rules.medri;

import com.google.common.base.MoreObjects;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by suhel on 19/03/16.
 */


public class IRule implements Serializable{
    static final long serialVersionUID = 424878435065750583L;

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

    public IRule(int label, int correct, int covers) {
        this(label);
        this.correct = correct;
        this.covers = covers;
    }

    public IRule(int label) {
        assert label != EMPTY;
        this.label = label;
        this.attIndexes = new int[0];
        this.attValues = new int[0];
    }



//    /**
//     * Filter the lineData to keep only lines satisfies this rule
//     * Reset correct, covers, and errors
//     *
//     * @param lineData
//     * @return Set ofOne not covered lines
//     */
//    public Set<int[]> keepCoveredBy(Set<int[]> lineData) {
//        return keepCoveredBy(lineData, this);
//    }
//
//    public static Set<int[]> keepCoveredBy(Set<int[]> lineData, IRule rule) {
//        rule.resetCounters();
//        final int label = rule.label;
//
//        Set<int[]> result = new HashSet<>(lineData.size());
//
//        for (Iterator<int[]> iter = lineData.iterator(); iter.hasNext(); ) {
//            int[] line = iter.next();
//            int lbl = rule.classify(line);
//            if (lbl == IRule.EMPTY) { //rule can not classify line
//                rule.covers++;
//                iter.remove();
//                result.add(line);
//            } else if (lbl == label) {
//                rule.correct++;
//            } else {
//                rule.errors++;
//            }
//        }
//        assert rule.covers == rule.correct + rule.errors;
//        return result;
//    }



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


    public double getLenghtWeighted() {
        return this.correct * this.getLenght();
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

    public static void main(String[] args) {
        System.out.println("done");
    }
}


