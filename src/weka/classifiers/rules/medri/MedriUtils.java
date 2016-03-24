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

    public static Pair<Collection<int[]>, int[]> mapIdataAndLabels(Instances data) {
        int labelIndex = data.classIndex();
        assert labelIndex == data.numAttributes() - 1;

        Collection<int[]> lineData = new ArrayList<>(data.numInstances());
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
            result[i] = (int) instance.value(i);
        }
        return result;
    }

    public static String formatIntPattern(int maxDigit) {
        int digits = (int) (Math.ceil(Math.log10(maxDigit + 1)));
        return "%0" + digits + "d";
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


    public static int[][][] countStep(int[] iattrs, Collection<int[]> lineData, int[] avAtts) {

        int labelIndex = iattrs.length - 1;
        int numLabels = iattrs[labelIndex];

        //create array ofOne attributes, withoud the class name;
        int[][][] result = new int[iattrs.length][][];

        for (int attIndex : avAtts) {
            result[attIndex] = new int[iattrs[attIndex]][numLabels];
        }
        //fill remaining attributes with empty arrays
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
//        //create array ofOne attributes, withoud the class name;
//        int[][][] result = new int[driData.iattrs.length][][];
//
//        //init counters
//        for (int attIndex = 0; attIndex < result.length; attIndex++) {
//            //create array ofOne elements for each attribute
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



    /**
     *
     * @param lineData
     * @param rule
     * @param resultSize bestCover ofOne the last MaxIndex
     * @return Pair<coveredLines, notCoveredLines>
     */
    public static Pair<Collection<int[]>, Collection<int[]>> splitAndGetCovered(
            Collection<int[]> lineData, IRule rule, int resultSize) {

//        assert lineData.size() > resultSize;

        Collection<int[]> coveredLines = new ArrayList<>(resultSize);
        Collection<int[]> notCoveredLines = new ArrayList<>(lineData.size() - resultSize);

        for (Iterator<int[]> iter = lineData.iterator(); iter.hasNext(); ) {
            int[] line = iter.next();

            if (rule.classify(line) == IRule.EMPTY) {
                notCoveredLines.add(line);
            } else {
                coveredLines.add(line);
            }
        }
        assert coveredLines.size() == resultSize;
        assert coveredLines.size() + notCoveredLines.size() == lineData.size();
        return new Pair(coveredLines, notCoveredLines);
    }

    /**
     * @param iattrs   holds number ofOne item for each attribute including the class attribute
     * @param lineData line data, pruned at the end to NOT COVERED instances
     * @param label    label index
     * @return
     */
    public static IRuleLines calcStep(int[] iattrs, Collection<int[]> lineData, final int label) {

        int labelIndex = iattrs.length - 1;
        int numLabels = iattrs[labelIndex];

        /** Start with all attributes, does not include the label attribute*/
        Set<Integer> avAtts = new LinkedHashSet<>();
        for (int i = 0; i < labelIndex; i++) avAtts.add(i);
        IRule rule = new IRule(label);


        Collection<int[]> entryLines = lineData;
        Collection<int[]> notCoveredLines = new ArrayList<>(lineData.size());

        do {

            int[][][] stepCount = countStep(iattrs, entryLines, Ints.toArray(avAtts));
            MaxIndex mx = MaxIndex.ofOne(stepCount, rule.label);

            assert mx.getLabel() != MaxIndex.EMPTY;
            assert mx.getLabel() == label;


            avAtts.remove(mx.getBestAtt());
            rule.addTest(mx.getBestAtt(), mx.getBestItem());
            rule.updateWith(mx);

            Pair<Collection<int[]>, Collection<int[]>> splitResult = splitAndGetCovered(entryLines, rule, mx.bestCover);
            notCoveredLines.addAll(splitResult.value);

            entryLines = splitResult.key;

        } while (rule.getErrors() > 0 && avAtts.size() > 0);

        return new IRuleLines(rule, notCoveredLines);
    }


    /**
     * @param iattrs   holds number ofOne item for each attribute including the class attribute
     * @param lineData line data, pruned at the end to NOT COVERED instances
     * @param label    label index
     * @return
     */
    public static IRuleLines calcStep(int[] iattrs, Collection<int[]> lineData, final int label,
                                      int minFreq, double minConfidence) {

        if(lineData.size() < minFreq) return null;

        int labelIndex = iattrs.length - 1;
        int numLabels = iattrs[labelIndex];

        /** Start with all attributes, does not include the label attribute*/
        Set<Integer> avAtts = new LinkedHashSet<>();
        for (int i = 0; i < labelIndex; i++) avAtts.add(i);
        IRule rule = new IRule(label);


        Collection<int[]> entryLines = lineData;
        Collection<int[]> notCoveredLines = new ArrayList<>(lineData.size());

        do {

            int[][][] stepCount = countStep(iattrs, entryLines, Ints.toArray(avAtts));
            MaxIndex mx = MaxIndex.ofSupportConfidence(stepCount,
                    rule.label, minFreq, minConfidence);

            if(mx.getLabel() == MaxIndex.EMPTY){
                break;
            }

            assert mx.getLabel() != MaxIndex.EMPTY;
            assert mx.getLabel() == label;
            assert mx.getBestAtt() >= 0;
            assert mx.getBestItem() >= 0;

            avAtts.remove(mx.getBestAtt());
            rule.addTest(mx.getBestAtt(), mx.getBestItem());
            rule.updateWith(mx);

            Pair<Collection<int[]>, Collection<int[]>> splitResult = splitAndGetCovered(entryLines, rule, mx.bestCover);
            notCoveredLines.addAll(splitResult.value);

            entryLines = splitResult.key;

        } while (rule.getErrors() > 0 && avAtts.size() > 0 && rule.getCorrect() >= minFreq);

        if (rule.getLenght() == 0) {//TODO more inspection is needed here
            return null;
        }

        return new IRuleLines(rule, notCoveredLines);
    }


    public static List<IRule> buildClassifierMeDRI(int[] iattrs, int[] labelsCount, Collection<int[]> lineData,
                                                   int minFreq, double minConfidence, boolean addDefaultRule) {
        List<IRule> rules = new ArrayList<>();

        int labelIndex = iattrs.length - 1;
        int numLabels = iattrs[labelIndex];

        Collection<int[]> remainingLines = null;
        for (int cls = 0; cls < numLabels; cls++) {
            logger.trace("****************************************" +
                    "\nfor class = {}", cls);
            int clsCounter = labelsCount[cls];
            logger.trace("cls {} count = {}", cls, clsCounter);
            Collection<int[]> lines = lineData;//new ArrayList<>(lineData);//defensive copy


            while (clsCounter > 0) {
                IRuleLines lnrl = calcStep(iattrs, lines, cls, minFreq, minConfidence);
                if(lnrl == null) break; // stop adding rules for current class. break out to the new class


                logger.trace("rule {}", lnrl.rule);
                logger.trace("remaining lines={}", lnrl.lines.size());

                lines = lnrl.lines;
                remainingLines = lines;
                clsCounter -= lnrl.rule.getCorrect();
                logger.trace("took {} , remains {} instances",
                        lnrl.rule.getCorrect(), clsCounter);
                rules.add(lnrl.rule);
            }
        }
        if (addDefaultRule) {
            if (remainingLines != null && remainingLines.size() > 0) {
                IRule rule = getDefaultRule(remainingLines, labelIndex, numLabels);
                rules.add(rule);
            }
        }

        //TODO check to add defaultRule
        assert rules.size() > 0;
        return rules;
    }

    private static IRule getDefaultRule(Collection<int[]> lines, int labelIndex, int numLabels) {
        int[] freqs = new int[numLabels];
        for (int[] line : lines) {
            freqs[line[labelIndex]]++;
        }

        int maxVal = Integer.MIN_VALUE;
        int maxIndex = Integer.MIN_VALUE;
        for (int i = 0; i < freqs.length; i++) {
            if (freqs[i] > maxVal) {
                maxVal = freqs[i];
                maxIndex = i;
            }
        }
        IRule rule = new IRule(maxIndex, maxVal, MaxIndex.sum(freqs));

        return rule ;
    }

    public static List<IRule> buildClassifierPrism(int[] iattrs, int[] labelsCount, Collection<int[]> lineData) {
        List<IRule> rules = new ArrayList<>();

        int labelIndex = iattrs.length - 1;
        int numLabels = iattrs[labelIndex];

        for (int cls = 0; cls < numLabels; cls++) {
            logger.trace("****************************************" +
                    "\nfor class = {}", cls);
            int clsCounter = labelsCount[cls];
            logger.trace("cls {} count = {}", cls, clsCounter);
            Collection<int[]> lines = lineData;//new ArrayList<>(lineData);//defensive copy

            while (clsCounter > 0) {
                IRuleLines lnrl = calcStep(iattrs, lines, cls);
                logger.trace("rule {}", lnrl.rule);
                logger.trace("remaining lines={}", lnrl.lines.size());

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
        data.setClassIndex(data.numAttributes() - 1);
        System.out.println(data.numInstances());
        int[] iattrs = MedriUtils.mapAttributes(data);

        Pair<Collection<int[]>, int[]> linesLabels = MedriUtils.mapIdataAndLabels(data);
        Collection<int[]> lineData = linesLabels.key;
        int[] labelsCount = linesLabels.value;

        logger.trace("original lines size = {}", lineData.size());
        List<IRule> rules = buildClassifierPrism(iattrs, labelsCount, lineData);

        logger.info("rules generated =\n{}", Joiner.on("\n").join(rules));


    }



}


