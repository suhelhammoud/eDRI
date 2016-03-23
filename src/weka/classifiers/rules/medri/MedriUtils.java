package weka.classifiers.rules.medri;

//import com.google.common.base.Joiner;

import com.google.common.base.Joiner;
import com.google.common.primitives.Ints;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import weka.classifiers.rules.edri.EDRIUtils;
import weka.core.Instance;
import weka.core.Instances;

import java.io.IOException;
import java.util.*;

/**
 * Created by suhel on 17/03/16.
 */
public class MedriUtils {

    static Logger logger = LoggerFactory.getLogger(MedriUtils.class);


    public static int[] mapAttributes(Instances data) {
        int[] iattrs = new int[data.numAttributes()];
        for (int i = 0; i < iattrs.length; i++) {
            iattrs[i] = data.attribute(i).numValues();
        }
        return iattrs;
    }

    public static Pair<Set<int[]>, int[]> mapIdataAndLabels(Instances data) {
        int labelIndex = data.classIndex();
        assert labelIndex == data.numAttributes()-1;

        Set<int[]> lineData = new HashSet<>(data.numInstances());
        int[] labelsCount = new int[data.attribute(data.classIndex()).numValues()];

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
        return new Pair(lineData, labelsCount);
    }

    public static int[] toIntArray(Instance instance) {
        int[] result = new int[instance.numValues()];
        for (int i = 0; i < result.length; i++) {
            result[i] = (int)instance.value(i);
        }
        return result;
    }

    public static String formatIntPattern(int maxDigit) {
        int digits = (int) (Math.ceil(Math.log10(maxDigit+1)));
        return  "%0"+ digits +"d";
    };

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


    public static int[][][] countStep(int[] iattrs, Set<int[]> lineData, int[] avAtts) {

        int labelIndex = iattrs.length-1;
        int numLabels = iattrs[labelIndex];

        //create array of attributes, withoud the class name;
        int[][][] result = new int[iattrs.length][][];

        for (int attIndex : avAtts) {
            result[attIndex] = new int[iattrs[attIndex]][numLabels];
        }
        for (int i = 0; i < result.length; i++) {
            if (result[i] == null) result[i] = new int[0][0];
        }

        //filling with values
        for (int[] row : lineData) {
            int cls = row[labelIndex];

            for (int a : avAtts)
                result[a][row[a]][cls]++;
        }
        return result;
    }

//    public static int[][][] countStep(MedriUtils driData, Set<int[]> lineData) {
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
     * @param iattrs    holds number of item for each attribute including the class attribute
     * @param lineData line data, pruned at the end to NOT COVERED instances
     * @param label    label index
     * @return
     */
    public static IRuleLines calcStep(int[] iattrs, Set<int[]> lineData, final int label) {

        int labelIndex = iattrs.length-1;
        int numLabels = iattrs[labelIndex];

        /** Start with all attributes, does not include the label attribute*/
        Set<Integer> avAtts = new LinkedHashSet<>();
        for (int i = 0; i < labelIndex; i++) avAtts.add(i);


        IRule rule = new IRule(label);


        Set<int[]> tmpLines = lineData;
        Set<int[]> linesRemained = null;
        Set<int[]> avoidedLines = new HashSet<>(lineData.size());

       do {

            int[][][] stepCount = countStep(iattrs, tmpLines, Ints.toArray(avAtts));
            MaxIndex mx = MaxIndex.of(stepCount, rule.label);

            logger.trace("maxIndex for step = {}", mx);
            assert mx.getLabel() != MaxIndex.EMPTY;
            assert mx.getLabel() == label;


            avAtts.remove(mx.getBestAtt());
            rule.addTest(mx.getBestAtt(), mx.getBestItem());
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
        return new IRuleLines(rule, lineData);
    }



    public static List<IRule> buildClassifierPrism(int[] iattrs, int[] labelsCount, Set<int[]> lineData) {
        List<IRule> rules = new ArrayList<>();

        int labelIndex = iattrs.length-1;
        int numLabels = iattrs[labelIndex];

        for (int cls = 0; cls < numLabels; cls++) {
            logger.trace("****************************************" +
                    "\nfor class = {}", cls);
            int clsCounter = labelsCount[cls];
            logger.trace("cls {} count = {}", cls, clsCounter);
            Set<int[]> lines = new HashSet<>(lineData);//defensive copy

//            for (int i = 0; i < 4; i++) {
////            }
            while (clsCounter > 0) {
                IRuleLines lnrl = calcStep(iattrs, lines, cls);
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
        data.setClassIndex(data.numAttributes()-1);
        System.out.println(data.numInstances());
        int[] iattrs = MedriUtils.mapAttributes(data);

        Pair<Set<int[]>, int[]> linesLabels = MedriUtils.mapIdataAndLabels(data);
        Set<int[]> lineData = linesLabels.key;
        int[] labelsCount = linesLabels.value;

        logger.trace("original lines\n{}", print(lineData));
        List<IRule> rules = buildClassifierPrism(iattrs, labelsCount, lineData);

        logger.info("rules generated =\n{}", Joiner.on("\n").join(rules));



    }


}


