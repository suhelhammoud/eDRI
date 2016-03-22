package weka.classifiers.rules.edri;

//import com.google.common.base.Joiner;

import com.google.common.base.Joiner;
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
    final public int[] labelsCount;
//    /**
//     * Holds the values of items for each row instance
//     */
//    final public Set<int[]> lineData;

    public DRIData(Instances data) {

        this.iattrs = new int[data.numAttributes()];
        this.labelIndex = iattrs.length - 1;
        this.numLabels = data.attribute(labelIndex).numValues();
        this.labelsCount = new int[numLabels];
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

    public Set<int[]> mapIdataAndLabels(Instances data) {
        Set<int[]> lineData = new HashSet<>(data.numInstances());

        int numAttrs = data.numAttributes();
        for (int i = 0; i < data.numInstances(); i++) {
            Instance instance = data.instance(i);
            final int[] row = new int[numAttrs];

            for (int att = 0; att < row.length; att++) {
                row[att] = (int) instance.value(att);
            }
            labelsCount[row[labelIndex]]++;
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


    public static int[][][] countStep(DRIData dd, Set<int[]> lineData, int[] avAtts) {

        //create array of attributes, withoud the class name;
        int[][][] result = new int[dd.iattrs.length][][];

        for (int attIndex : avAtts) {
            result[attIndex] = new int[dd.iattrs[attIndex]][dd.numLabels];
        }
        for (int i = 0; i < result.length; i++) {
            if (result[i] == null) result[i] = new int[0][0];
        }

        //filling with values
        for (int[] row : lineData) {
            int cls = row[dd.labelIndex];

            for (int a : avAtts)
                result[a][row[a]][cls]++;
        }
        return result;
    }

//    public static int[][][] countStep(DRIData driData, Set<int[]> lineData) {
//
//        //create array of attributes, withoud the class name;
//        int[][][] result = new int[driData.iattrs.length][][];
//
//        //init counters
//        for (int attIndex = 0; attIndex < result.length; attIndex++) {
//            //create array of elements for each attribute
//            result[attIndex] = new int[driData.iattrs[attIndex]][driData.numLabels];
//        }
//
//        //filling with values
//        for (int[] row : lineData) {
//            int cls = row[driData.labelIndex];
//            for (int a = 0; a < row.length; a++) {
//                result[a][row[a]][cls]++;
//            }
//        }
//        return result;
//    }


    public static Set<int[]> splitAndGetCovered(Set<int[]> lineData, IRule rule, int resultSize) {
        Set<int[]> coveredLines = new HashSet<>(resultSize);
        final int lblInLine = lineData.iterator().next().length - 1;
        for (Iterator<int[]> iter = lineData.iterator(); iter.hasNext(); ) {
            int[] line = iter.next();

            if (rule.classify(line) == IRule.EMPTY)
                continue; //not Classified keep it
            coveredLines.add(line);
            iter.remove();
        }
        assert coveredLines.size() == resultSize;
        return coveredLines;
    }

    /**
     * @param dd       (with label index and iattrs length values
     * @param lineData line data, pruned at the end to NOT COVERED instances
     * @param label    label index
     * @return
     */
    public static LinesIRule calcStep(DRIData dd, Set<int[]> lineData, final int label) {

        /** Start with all attributes*/
        Set<Integer> avAtts = new LinkedHashSet<>();
        for (int i = 0; i < dd.labelIndex; i++) avAtts.add(i);


        IRule rule = new IRule(label);


        Set<int[]> tmpLines = lineData;
        Set<int[]> linesRemained = null;
        Set<int[]> avoidedLines = new HashSet<>(lineData.size());

       do {

            int[][][] stepCount = countStep(dd, tmpLines, Ints.toArray(avAtts));
            MaxIndex mx = MaxIndex.of(stepCount, rule.label);

            logger.trace("maxIndex for step = {}", mx);
            assert mx.getLabel() != MaxIndex.EMPTY;
            assert mx.getLabel() == label;


            avAtts.remove(mx.getAtt());
            rule.addTest(mx.getAtt(), mx.getItem());
            rule.updateWith(mx);

            Set<int[]> coveredLines  = splitAndGetCovered(tmpLines,rule, mx.bestCover);
           avoidedLines.addAll(tmpLines);
           linesRemained = tmpLines;
            logger.trace("tmpLines =\n{}", print(tmpLines));
            logger.trace("coveredLines =\n{}", print(coveredLines));

            logger.trace("switch tmpLines of size= {}, into coveredLines of size= {}", tmpLines.size(), coveredLines.size());
//            if (linesRemained == null) {
//
//            }
            tmpLines = coveredLines;

        }  while (rule.getErrors() > 0 && avAtts.size() > 0);

        lineData.addAll(avoidedLines);
        logger.trace("exit calc steps with final rule ={}",rule);
        return new LinesIRule(rule, lineData);
    }



    public static List<IRule> buildClassifierPrism(DRIData dd, Set<int[]> lineData) {
        List<IRule> rules = new ArrayList<>();

        for (int cls = 0; cls < dd.numLabels; cls++) {
            logger.trace("****************************************" +
                    "\nfor class = {}", cls);
            int clsCounter = dd.labelsCount[cls];
            logger.trace("cls {} count = {}", cls, clsCounter);
            Set<int[]> lines = new HashSet<>(lineData);//defensive copy

//            for (int i = 0; i < 4; i++) {
////            }
            while (clsCounter > 0) {
                LinesIRule lnrl = calcStep(dd, lines, cls);
                logger.trace("rule {}", lnrl.rule);
                logger.trace("remaining lines={}\n{}", lnrl.lines.size(),print(lnrl.lines));

                lines = lnrl.lines;
                clsCounter -= lnrl.rule.getCorrect();
                logger.trace("took {} , remains {} instances",
                        lnrl.rule.getCorrect(), clsCounter);
                rules.add(lnrl.rule);
            }
        }

        return rules;
    }

    public static void main(String[] args) throws IOException {
        logger.info("test logger");

//        String inFile = "/media/suhel/workspace/work/wekaprism/data/fadi.arff";
        String inFile = "/media/suhel/workspace/work/wekaprism/data/cl.arff";

        Instances data = new Instances(EDRIUtils.readDataFile(inFile));
        System.out.println(data.numInstances());
        DRIData dd = new DRIData(data);

        Set<int[]> lineData = dd.mapIdataAndLabels(data);
        logger.trace("original lines\n{}", print(lineData));
        List<IRule> rules = buildClassifierPrism(dd, lineData);

        logger.info("rules generated =\n{}", Joiner.on("\n").join(rules));
        if (true) {
            return;
        }
        Set<int[]> notCovered = new HashSet<>(lineData);
//        List<IRule> rules = new ArrayList<>();
        LinesIRule lnrl;

        logger.trace("lineLabel {}", lineData.size());
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


