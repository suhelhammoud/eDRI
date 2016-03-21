package weka.classifiers.rules.edri;

//import com.google.common.base.Joiner;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;
import com.google.common.primitives.Ints;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import weka.core.Instance;
import weka.core.Instances;

import java.io.IOException;
import java.util.*;

/**
 * Created by suhel on 17/03/16.
 */
public class DRIData {

    static Logger logger = LoggerFactory.getLogger(DRIData.class);

    final public int labelIndex;

    /**
     * Holds the label for each line number
     */
//    final public int[] lineLabel;

    /**
     * Hold the countStep of each class label
     */
//    final public int[] labelAtt;

    /**
     * Holds the number of items in each attribute
     */
    final public int[] iattrs;
    final public int numLabels;

//    /**
//     * Holds the values of items for each row instance
//     */
//    final public Set<int[]> lineData;

    public DRIData(Instances data) {

        this.iattrs = new int[data.numAttributes()];
        this.labelIndex = iattrs.length - 1;
        this.numLabels = data.attribute(labelIndex).numValues();
        mapIAttrs(data);

//        this.labelAtt = new int[data.attribute(labelIndex).numValues()];
//      this.lineLabel = new int[data.numInstances()];
//      this.lineData = new HashSet<>(data.numInstances());
//        mapIdataAndLabels(data);
    }

    private void mapIAttrs(Instances data) {
        for (int i = 0; i < iattrs.length; i++) {
            iattrs[i] = data.attribute(i).numValues();
        }
    }

    public static Set<int[]> mapIdataAndLabels(Instances data) {
        Set<int[]> lineData = new HashSet<>(data.numInstances());

        int numAttrs = data.numAttributes();

        for (int i = 0; i < data.numInstances(); i++) {
            Instance instance = data.instance(i);
            final int[] row = new int[numAttrs];

            for (int att = 0; att < row.length; att++) {
                row[att] = (int) instance.value(att);
            }
            lineData.add(row);
        }
        return lineData;
    }

    public static StringBuilder print(Collection<int[]> c) {
        StringBuilder sb = new StringBuilder();
        for (Iterator<int[]> iter = c.iterator(); iter.hasNext(); ) {
            sb.append(Arrays.toString(iter.next()) + "\n");
        }
        return sb;
    }

    public static StringBuilder print(int[][] arr) {
        StringBuilder sb = new StringBuilder();
        if (arr == null || arr.length == 0) return sb;
        for (int i = 0; i < arr.length; i++) {
            sb.append(Ints.join(", ", arr[i]));
            sb.append("\n");
        }
        return sb;
    }

    public static StringBuilder print(int[][][] d) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < d.length; i++) {
            sb.append("********** " + i + " *********\n");
            sb.append(print(d[i]));
        }
        return sb;
    }


    public static int[][][] countStep(DRIData driData, Set<int[]> lineData, int[] avAtts) {

        //create array of attributes, withoud the class name;
        int[][][] result = new int[driData.iattrs.length][][];

        for (int attIndex : avAtts) {
            result[attIndex] = new int[driData.iattrs[attIndex]][driData.numLabels];
        }
        for (int i = 0; i < result.length; i++) {
            if (result[i] == null) result[i] = new int[0][0];
        }

        //filling with values
        for (int[] row : lineData) {
            int cls = row[driData.labelIndex];
            for (int a : avAtts)
                result[a][row[a]][cls]++;
        }
        return result;
    }

    public static int[][][] countStep(DRIData driData, Set<int[]> lineData) {

        //create array of attributes, withoud the class name;
        int[][][] result = new int[driData.iattrs.length][][];

        //init counters
        for (int attIndex = 0; attIndex < result.length; attIndex++) {
            //create array of elements for each attribute
            result[attIndex] = new int[driData.iattrs[attIndex]][driData.numLabels];
        }

        //filling with values
        for (int[] row : lineData) {
            int cls = row[driData.labelIndex];
            for (int a = 0; a < row.length; a++) {
                result[a][row[a]][cls]++;
            }
        }
        return result;
    }


    /**
     *
     * @param dd (with label index and iattrs length values
     * @param lineData line data, pruned at the end to not covered instances
     * @param label label index
     * @return
     */
    public static LinesIRule calcStep(DRIData dd, Set<int[]> lineData, int label ) {

        /** Start with all attributes*/
        Set<Integer> avAtts = new LinkedHashSet<>();
        for (int i = 0; i < dd.labelIndex; i++) avAtts.add(i);

        logger.trace("Start with avAtts = {}", avAtts );
        /** count the class counts for each element in each attributes*/
        int[][][] count = countStep(dd, lineData, Ints.toArray(avAtts));

        /** create MaxIndex instance either general or based on */
        MaxIndex mi = label == MaxIndex.EMPTY ?
                MaxIndex.of(count)
                : MaxIndex.of(count, label) ;

        logger.trace("maxIndex for step = {}", mi);
        assert mi.getLabel() != MaxIndex.EMPTY;


        IRule rule = new IRule(mi.getLabel());

        rule.addTest(mi.getAtt(), mi.getItem());
        /** Done with max attributes element, remove it  */
        avAtts.remove(mi.getAtt());

        Set<int[]> notCovered = rule.keepCoveredBy(lineData);
        logger.trace("lines kept = {}, lines not coverd= {}", lineData.size(), notCovered.size());
        logger.trace(" rule getErrors {}", rule.getErrors());
        rule.updateWith(mi);//TODO check if remvoed
        logger.trace(" rule getErrors after updates {}", rule.getErrors());

        /** Keep looping while current set of lines not completed*/
        while (rule.getErrors() > 0 || avAtts.size() == 0) {
            /** count the class counts for remaining attributs */
            int[][][] sepCount = countStep(dd, lineData, Ints.toArray(avAtts));

            MaxIndex mx = MaxIndex.of(sepCount, rule.label);
            if (mx.getLabel() == MaxIndex.EMPTY) {
                logger.error("can't find sensible test on rule {}", rule.toString());
                break;
            }

            avAtts.remove(mx.getAtt());

            rule.addTest(mx.getAtt(), mx.getItem());

            Set<int[]> remains = rule.keepCoveredBy(lineData);
            //rule.updateWith(mx);//TODO check deleting updateWith

            notCovered.addAll(remains);

        }

         return new LinesIRule(rule, notCovered);
    }

    public static MaxIndex max(int[][][] count) {
        MaxIndex mi = new MaxIndex();
        for (int at = 0; at < count.length - 1; at++) {
            for (int itm = 0; itm < count[at].length; itm++) {
                mi.max(count[at][itm], at, itm);
            }
        }
        return mi;
    }


    public static void main(String[] args) throws IOException {
        logger.info("test logger");

        String inFile = "/media/suhel/workspace/work/wekaprism/data/fadi.arff";

        Instances data = new Instances(EDRIUtils.readDataFile(inFile));
        System.out.println(data.numInstances());
        DRIData dd = new DRIData(data);

        Set<int[]> lineLabel = mapIdataAndLabels(data);
        Set<int[]> notCovered = new HashSet<>(lineLabel);
        List<IRule> rules = new ArrayList<>();
        LinesIRule lnrl;

        logger.trace("lineLabel {}", lineLabel.size());
        logger.trace("not covered {}", notCovered.size());

        int label = 0;
        logger.trace("label used = {}", label);

        logger.trace("Start Rule One *****************");
        logger.trace("lines size = {}", notCovered.size());
        lnrl = calcStep(dd, notCovered, label);
        logger.trace("rule 1 = {}", lnrl.rule.toString());
        rules.add(lnrl.rule);
//        notCovered.addAll(lnrl.lines);
        logger.trace("not covered size = {}", notCovered.size());


        logger.trace("Start Rule Two *****************");
        logger.trace("lines size = {}", notCovered.size());
        lnrl = calcStep(dd, notCovered, label);
        logger.trace("rule 1 = {}", lnrl.rule.toString());
        rules.add(lnrl.rule);
        notCovered.addAll(lnrl.lines);
        logger.trace("not covered size = {}", notCovered.size());


        logger.trace("Start Rule Three *****************");
        logger.trace("lines size = {}", notCovered.size());
        lnrl = calcStep(dd, notCovered, label);
        logger.trace("rule 1 = {}", lnrl.rule.toString());
        rules.add(lnrl.rule);
        notCovered.addAll(lnrl.lines);
        logger.trace("not covered size = {}", notCovered.size());




    }


}


