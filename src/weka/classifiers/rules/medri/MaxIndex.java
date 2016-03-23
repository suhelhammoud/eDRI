package weka.classifiers.rules.medri;

import com.google.common.base.MoreObjects;

/**
 * Created by suhel on 21/03/16.
 */

public class MaxIndex {
    public final static int EMPTY = -1;
    private int bestCorrect = -1;//Should start with negative value
    int bestCover = 0;
    private int bestAtt = EMPTY, bestItem = EMPTY, label = EMPTY;

    public int getBestAtt() {
        return bestAtt;
    }

    public int getBestItem() {
        return bestItem;
    }

    public int getLabel() {
        return label;
    }

    public int getBestCover() {
        return bestCover;
    }

    public int getBestCorrect() {
        return bestCorrect;
    }

    public MaxIndex copy() {
        MaxIndex result = new MaxIndex();
        result.bestAtt = this.bestAtt;
        result.bestItem = this.bestItem;
        result.label = this.label;
        result.bestCorrect = this.bestCorrect;
        result.bestCover = this.bestCover;

        return result;
    }

    public static MaxIndex of(int[][][] count) {
        MaxIndex mi = new MaxIndex();
        for (int at = 0; at < count.length; at++) {
            for (int itm = 0; itm < count[at].length; itm++) {
                mi.max(count[at][itm], at, itm);
            }
        }
        return mi;
    }

    public static MaxIndex of(int[][][] count, int label) {
        MaxIndex mi = new MaxIndex();
        for (int at = 0; at < count.length; at++) {
            for (int itm = 0; itm < count[at].length; itm++) {
                mi.maxOne(count[at][itm], at, itm, label);
            }
        }
        return mi;
    }

    public boolean maxOne(int[] itemLabels, int attIndex, int itemIndex, int label) {
        int sum = sum(itemLabels);
        boolean changed = false;
        int diff = itemLabels[label] * bestCover - bestCorrect * sum;
        if (diff > 0 || diff == 0 && itemLabels[label] > bestCorrect) {
            this.bestAtt = attIndex;
            this.bestItem = itemIndex;
            this.label = label;
            this.bestCorrect = itemLabels[label];
            this.bestCover = sum;
            changed = true;
        }
        return changed;

    }

    public boolean max(int[] itemLabels, int attIndex, int itemIndex) {
        int sum = sum(itemLabels);
        boolean changed = false;
        for (int i = 0; i < itemLabels.length; i++) {
            int diff = itemLabels[i] * bestCover - bestCorrect * sum;
            if (diff > 0 || diff == 0 && itemLabels[i] > bestCorrect) {
                this.bestAtt = attIndex;
                this.bestItem = itemIndex;
                this.label = i;
                this.bestCorrect = itemLabels[i];
                this.bestCover = sum;
                changed = true;
            }
        }
        return changed;
    }

    @Override
    public String toString() {

        return MoreObjects.toStringHelper(this)
                .add("bestAtt", bestAtt)
                .add("bestItem", bestItem)
                .add("lbl", label)
                .add("correct", bestCorrect)
                .add("cover", bestCover)
                .toString();
    }

    public static int sum(int[] a) {
        int result = 0;
        for (int i : a) result += i;
        return result;
    }
}
