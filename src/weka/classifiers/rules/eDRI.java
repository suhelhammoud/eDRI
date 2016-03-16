/*
 *    This program is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program; if not, write to the Free Software
 *    Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

/*
 *    Prism.java
 *    Copyright (C) 1999 University of Waikato, Hamilton, New Zealand
 *
 */

package weka.classifiers.rules;

import ch.qos.logback.classic.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import weka.classifiers.Classifier;
import weka.classifiers.rules.edri.DRIOptions;
import weka.classifiers.rules.edri.DRIRule;
import weka.classifiers.rules.edri.DRITest;
import weka.classifiers.rules.edri.EDRIUtils;
import weka.core.*;
import weka.core.Capabilities.Capability;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;

import java.util.*;

/**
 * <!-- globalinfo-start -->
 * Class for building and using a eDRI rule set for classification. Can only deal with nominal attributes. Can't deal with missing values. Doesn't do any pruning.<br/>
 * <br/>
 * For more information, see <br/>
 * <br/>
 * J. F. Thabatah, S. Hammoud
 * <p>
 * <!-- globalinfo-end -->
 * <p>
 * <!-- technical-bibtex-start -->
 * BibTeX:
 * <pre>
 * &#64;article{ref here,
 *    author = {F. Thabatah},
 *    journal = { Journal Ref. here},
 *    number = {number},
 *    pages = {pagestart - pageend},
 *    title = {eDRI enhanced Dynamic Rule Induction},
 *    volume = {vol},
 *    year = {2016}
 * }
 * </pre>
 * <p>
 * <!-- technical-bibtex-end -->
 * <p>
 * <!-- options-start -->
 * Valid options are: <p/>
 * <p>
 * <pre>
 *     TODO: add later
 * </pre>
 * <p>
 * <!-- options-end -->
 *
 * @author Suhel Hammmoud (suhel.hammoud@gmail.com)
 * @version $Revision: 001 $
 */
public class eDRI
        extends Classifier
        implements OptionHandler, TechnicalInformationHandler {


    /**
     * Helper class to hold the result of ruleInstancesxxx(xxx)
     *
     * @param <K> key
     * @param <V> value
     */
    class Pair<K, V> {
        final public K key;
        final public V value;

        Pair(K key, V value) {
            this.key = key;
            this.value = value;
        }
    }

    static ch.qos.logback.classic.Logger lgLevel = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(eDRI.class);
    static Logger logger = LoggerFactory.getLogger(eDRI.class);
    static Logger freqsLogger = LoggerFactory.getLogger("freqs");


    /**
     * for serialization
     */
    static final long serialVersionUID = 1310258880025902107L;

    /**
     * Returns a string describing classifier
     *
     * @return a description suitable for
     * displaying in the explorer/experimenter gui
     */
    public String globalInfo() {
        return "Class for building and using a eDRI rule set for classification. "
                + "Can only deal with nominal attributes. Can't deal with missing values. "
                + "For more information, see \n\n"
                + getTechnicalInformation().toString();
    }

    /**
     * Returns an instance of a TechnicalInformation object, containing
     * detailed information about the technical background of this class,
     * e.g., paper reference or book this class is based on.
     *
     * @return the technical information about this class
     */
    public TechnicalInformation getTechnicalInformation() {
        TechnicalInformation result;

        result = new TechnicalInformation(Type.ARTICLE);
        result.setValue(Field.AUTHOR, "F. Thabatah, S. Hammoud");
        result.setValue(Field.YEAR, "2016");
        result.setValue(Field.TITLE, "eDRI: An algorithm for inducing modular rules");
        result.setValue(Field.JOURNAL, "Journal");
        result.setValue(Field.VOLUME, "vol");
        result.setValue(Field.NUMBER, "number");
        result.setValue(Field.PAGES, "p_start-p_end");
        return result;
    }


    /**
     * Holds algorithm configurations and OptionHandler parameters
     */
    private DRIOptions pOptions = new DRIOptions();

    /**
     * List of all rules
     */
    private List<DRIRule> m_rules = new ArrayList<>();

    /**
     * Classifies a given instance.
     *
     * @param inst the instance to be classified
     * @return the classification
     */
    public double classifyInstance(Instance inst) {

        int result = DRIRule.classifyInst(inst, m_rules);
        if (result == -1) {
            return Instance.missingValue();
        } else {
            return (double) result;
        }
    }

    @Override
    public Enumeration listOptions() {
        return pOptions.listOptions();
    }

    @Override
    public void setOptions(String[] options) throws Exception {
        pOptions.setOptions(options);
    }

    @Override
    public String[] getOptions() {
        return pOptions.getOptions();

    }

    public boolean getAddDefaultRule() {
        return pOptions.getAddDefaultRule();
    }

    public void setAddDefaultRule(boolean b) {
        pOptions.setAddDefaultRule(b);
    }

    public boolean getUseOldPrism() {
        return pOptions.getUseOldPrism();
    }

    public void setUseOldPrism(boolean b) {
        pOptions.setUseOldPrism(b);
    }

    public double getMinSupport() {
        return pOptions.getMinSupport();
    }

    public void setMinSupport(double support) {
        pOptions.setMinSupport(support);
    }

    public double getMinConfidence() {
        return pOptions.getMinConfidence();
    }

    public void setMinConfidence(double confidence) {
        pOptions.setMinConfidence(confidence);
    }

    public void setDebugLevel(SelectedTag newMethod) {
        pOptions.setDebugLevel(newMethod);
    }

    public SelectedTag getDebugLevel() {
        return pOptions.getDebugLevel();
    }

    public String debugLevelTipText() {
        return "debug level tip text";
    }

    /**
     * Returns default capabilities of the classifier.
     *
     * @return the capabilities of this classifier
     */
    public Capabilities getCapabilities() {
        Capabilities result = super.getCapabilities();
        result.disableAll();

        // attributes
        result.enable(Capability.NOMINAL_ATTRIBUTES);

        // class
        result.enable(Capability.NOMINAL_CLASS);
        result.enable(Capability.MISSING_CLASS_VALUES);

        return result;
    }

    /**
     * After running, m_rules will be refilled with new learned rules, oPtion will holds values of:
     * maxNumInstances: data.numInstances
     *
     * @param data:          training dataset
     * @param minFreqs:      minimum frequency threshold, (numInstances * minSupport)
     * @param minConfidence: confidence threshold
     * @throws Exception
     */
    public void buildClassifierEDRI(Instances data, int minFreqs, double minConfidence) throws Exception {

        List<DRIRule> rules = new ArrayList<>(data.numAttributes());
        int cl; // possible value of theClass
        Instances E = null, ruleE;
        DRIRule rule = null;

        DRITest DRITest = null;
        DRITest oldDRITest = null;
        int bestCorrect, bestCovers, attUsed;
        Enumeration enumAtt;

        // can classifier handle the data?
        getCapabilities().testWithFail(data);

        // remove instances with missing class
        data = new Instances(data);//defensive copy of the data
        data.deleteWithMissingClass();
        for (cl = 0; cl < data.numClasses(); cl++) { // for each class cl
            Attribute classAtt = data.attribute(data.classIndex());

            logger.trace("for class = {}", classAtt.value(cl));
            logger.trace("reset E from {} to {} instances",
                    E == null ? "null" : E.numInstances(),
                    data.numInstances());
            E = data; // initialize E to the instance set

            while (contains(E, cl)) { // while E contains examples in class cl
                Pair<DRIRule, Instances> result = ruleInstancesEDRI(cl, E, minFreqs, minConfidence);
                if (result == null) {
                    break;
                }
                rules.add(result.key);
                E = result.value;
            }
            logger.trace("\t switching to next class with E contains {} instances\n", E.numInstances());
        }

        if (getAddDefaultRule()) {

            DRIRule defaultRule = getDefaultRule(E);
            if (defaultRule != null) {
                rules.add(defaultRule);
                logger.trace("add defaul rule {}", defaultRule.toStr());
            }
        }
        //TODO add default class
        logger.trace("no more classes found");
        m_rules.clear();
        m_rules.addAll(rules);
    }

    /**
     * Gets the majority class of the remaining instances as DRIRule
     *
     * @param data: Remaining dataset
     * @return: DRIRule of the majority class
     */
    public DRIRule getDefaultRule(Instances data) {
        int classIndex = data.classIndex();
        int[] freqs = new int[data.attribute(classIndex).numValues()];
        for (int i = 0; i < data.numInstances(); i++) {
            int cls = (int) data.instance(i).value(classIndex);
            freqs[cls]++;
        }
        ;
        int maxVal = Integer.MIN_VALUE;
        int maxIndex = Integer.MIN_VALUE;
        for (int i = 0; i < freqs.length; i++) {
            if (freqs[i] > maxVal) {
                maxVal = freqs[i];
                maxIndex = i;
            }
        }
        return new DRIRule(data, maxIndex);
    }

    /**
     * Generates the classifier.
     *
     * @param data the data to be used
     * @throws Exception if the classifier can't built successfully
     */
    public void buildClassifier(Instances data) throws Exception {

        lgLevel.setLevel(Level.toLevel(pOptions.debugLevel()));
        pOptions.setMaxNumInstances(data.numInstances());

        if (pOptions.getUseOldPrism()) {
            buildClassifierPrism(data);
        } else {
            double minSupport = pOptions.getMinSupport();
            double minConfidence = pOptions.getMinConfidence();
            int minFreq = (int) Math.ceil(minSupport * data.numInstances());
            logger.info("Build classifier on {}, # Instances = {}, minSupport={} minFreq ={}, minConfidence={}",
                    data.relationName(), data.numInstances(), minSupport, minFreq, minConfidence);
            freqsLogger.info("Build classifier on {}, # Instances = {}, minSupport={} minFreq ={}, minConfidence={}",
                    data.relationName(), data.numInstances(), minSupport, minFreq, minConfidence);
            buildClassifierEDRI(data, minFreq, minConfidence);
        }

    }

    public void buildClassifierPrism(Instances data) throws Exception {
        List<DRIRule> rules = new ArrayList<>(data.numAttributes());

        int cl; // possible value of theClass
        Instances E = null, ruleE;
        DRIRule rule = null;

        DRITest DRITest = null;
        DRITest oldDRITest = null;
        int bestCorrect, bestCovers, attUsed;
        Enumeration enumAtt;

        // can classifier handle the data?
        getCapabilities().testWithFail(data);

        // remove instances with missing class
        data = new Instances(data);
        data.deleteWithMissingClass();
        for (cl = 0; cl < data.numClasses(); cl++) { // for each class cl
            Attribute classAtt = data.attribute(data.classIndex());

            logger.trace("for class = {}", classAtt.value(cl));
            freqsLogger.trace("for class = {}", classAtt.value(cl));

            logger.trace("reset E from {} to {} instances",
                    E == null ? "null" : E.numInstances(),
                    data.numInstances());
            E = data; // initialize E to the instance set

            while (contains(E, cl)) { // while E contains examples in class cl
                Pair<DRIRule, Instances> result = ruleInstancesPrism(cl, E);
                rules.add(result.key);
                E = result.value;
            }
            logger.trace("\t switching to next class with E contains {} instances\n", E.numInstances());
        }
        logger.trace("no more classes found");
        m_rules.clear();
        m_rules.addAll(rules);
    }


    private Pair<DRIRule, Instances> ruleInstancesEDRI(int cl, Instances e, int minFreqs, double minConfidence) throws Exception {
//        Instances data = ;
//        List<DRIRule> rules = ;
        if (e.numInstances() < minFreqs) {
            logger.trace("remaining instances = {} < {}",
                    e.numInstances(), minFreqs);
            return null;
        }
        Attribute classAtt = e.attribute(e.classIndex());
        DRIRule rule;
        Instances ruleE;
        DRITest driTest;
        int attUsed;
        int bestCovers;
        int bestCorrect;
        Enumeration enumAtt;
        logger.trace("\tE contains {} class\n", classAtt.value(cl));
        rule = new DRIRule(e, cl);
        rule.updateAndGetNotCovered(e);

        logger.trace("\tNew rule {}", rule.toStr());
        ruleE = e; // examples covered by this rule
        logger.trace("\tstart ruleE  with {} instances", ruleE.numInstances());

        while (rule.m_errors > 0 && rule.m_correct >= minFreqs) { // until the rule is perfect
            logger.trace("\t\tRule {} is not perfect", rule.id);
            driTest = new DRITest(); // make a new DRITest
            bestCorrect = bestCovers = attUsed = 0;

            // for every attribute not mentioned in the rule
            enumAtt = ruleE.enumerateAttributes();
            while (enumAtt.hasMoreElements()) {
                Attribute attr = (Attribute) enumAtt.nextElement();
                logger.trace("\t\t\tfor attr {} of class {}", attr.name(), classAtt.value(cl));
                if (isMentionedIn(attr, rule.m_text)) {
                    attUsed++;
                    logger.trace("\t\t\tSkip attr {}", attr, attr.name());
                    continue;
                }
                int M = attr.numValues();
                int[] covers = new int[M];
                int[] correct = new int[M];
                String[] attrNames = new String[M];

                for (int j = 0; j < M; j++) {
                    covers[j] = correct[j] = 0;
                    attrNames[j] = attr.value(j);
                }

                // ... calculate the counts for this class
                Enumeration enu = ruleE.enumerateInstances();
                while (enu.hasMoreElements()) {
                    Instance instance = (Instance) enu.nextElement();
                    covers[(int) instance.value(attr)]++;
                    if ((int) instance.classValue() == cl) {
                        correct[(int) instance.value(attr)]++;
                    }
                }

                freqsLogger.trace("\nAttr ({}), cover, correct", attr.name());
                for (int i = 0; i < M; i++) {
                    freqsLogger.trace("{}, {}, {}", attrNames[i], covers[i], correct[i]);
                }

                logger.trace("\t\t\t\tattr_{}  of {} Covers={}, correct {}", attr.name(), attrNames, Arrays.toString(covers), Arrays.toString(correct));

                int notCovered = -1;
                // ... for each value of this attribute, see if this DRITest is better
                for (int val = 0; val < M; val++) {
//
                    if (correct[val] < minFreqs) {
                        continue;
                    }
                    double conf = (double) correct[val] / (double) covers[val];

                    if (conf < minConfidence) {
                        continue;
                    }

                    int diff = correct[val] * bestCovers - bestCorrect * covers[val];

                    if (diff > 0 || (diff == 0 && correct[val] > bestCorrect)) {

                        // update the rule to use this DRITest
                        bestCorrect = correct[val];
                        bestCovers = covers[val];
                        driTest.m_attr = attr.index();
                        driTest.m_val = val;
//                        notCovered = bestCovers - bestCorrect;
                        rule.m_errors = bestCovers - bestCorrect;
                        rule.m_covers = bestCovers;
                        rule.m_correct = bestCorrect;
                    }
                }
            }
            if (driTest.m_attr == -1) { // Couldn't find any sensible DRITest
                logger.trace("\t\t\tCouldn't find any sensible DRITest");
                break;
            }
            logger.trace("\t\t\tAdd DRITest {} to rule {}",
                    driTest == null ? "null" : driTest.toStr(e),
                    rule == null ? "null" : rule.toStr());

            freqsLogger.trace("add item {}, to rule {}", driTest.toStr(e), rule.toStr());
            rule.addTest(driTest);

            ruleE = rule.coveredBy(ruleE);
            logger.trace("\t\t\tR_{} coveredBy {}", rule.id, ruleE.numInstances());
            if (attUsed == (e.numAttributes() - 1)) { // Used all attributes.
                logger.trace("\t\t\tused all the attributes, break loop");
                break;
            }
        }
        if (rule.m_text == null) {
            return null;
        }
        Instances result = rule.notCoveredBy(e);
        logger.trace("\tE now contains {} instances\n", result.numInstances());
        freqsLogger.trace("completed rule {}\n\n", rule.toStr());
        return new Pair<>(rule, result);
    }

    private Pair<DRIRule, Instances> ruleInstancesPrism(int cl, Instances e) throws Exception {
        Attribute classAtt = e.attribute(e.classIndex());
        DRIRule rule;
        Instances ruleE;
        DRITest DRITest;
        int attUsed;
        int bestCovers;
        int bestCorrect;
        Enumeration enumAtt;
        logger.trace("\tE contains {} class\n", classAtt.value(cl));
        rule = new DRIRule(e, cl);
        rule.updateAndGetNotCovered(e);
        logger.trace("\tNew rule {}", rule.toStr());
        ruleE = e; // examples covered by this rule
        logger.trace("\truleE {}", ruleE.numInstances());
        while (rule.m_errors != 0) { // until the rule is perfect
            DRITest = new DRITest(); // make a new DRITest
            bestCorrect = bestCovers = attUsed = 0;

            // for every attribute not mentioned in the rule
            enumAtt = ruleE.enumerateAttributes();
            while (enumAtt.hasMoreElements()) {
                Attribute attr = (Attribute) enumAtt.nextElement();
                logger.trace("\t\t\tfor attr {} of class {}", attr.name(), classAtt.value(cl));
                if (isMentionedIn(attr, rule.m_text)) {
                    attUsed++;
                    logger.trace("\t\t\tSkip attr {}", attr, attr.name());
                    continue;
                }
                int M = attr.numValues();
                int[] covers = new int[M];
                int[] correct = new int[M];
                String[] attrNames = new String[M];

                for (int j = 0; j < M; j++) {
                    covers[j] = correct[j] = 0;
                    attrNames[j] = attr.value(j);
                }

                // ... calculate the counts for this class
                Enumeration enu = ruleE.enumerateInstances();
                while (enu.hasMoreElements()) {
                    Instance instance = (Instance) enu.nextElement();
                    covers[(int) instance.value(attr)]++;
                    if ((int) instance.classValue() == cl) {
                        correct[(int) instance.value(attr)]++;
                    }
                }

                freqsLogger.trace("\nAttr ({}), cover, correct", attr.name());
                for (int i = 0; i < M; i++) {
                    freqsLogger.trace("{}, {}, {}", attrNames[i], covers[i], correct[i]);
                }


                logger.trace("\t\t\t\tattr_{}  of {} Covers={}, correct {}", attr.name(), attrNames, Arrays.toString(covers), Arrays.toString(correct));

                // ... for each value of this attribute, see if this DRITest is better
                for (int val = 0; val < M; val++) {
                    int diff = correct[val] * bestCovers - bestCorrect * covers[val];

                    // this is a ratio DRITest, correct/covers vs best correct/covers
                    if (DRITest.m_attr == -1
                            || diff > 0 || (diff == 0 && correct[val] > bestCorrect)) {

                        // update the rule to use this DRITest
                        bestCorrect = correct[val];
                        bestCovers = covers[val];
                        DRITest.m_attr = attr.index();
                        DRITest.m_val = val;
                        rule.m_errors = bestCovers - bestCorrect;
                        rule.m_covers = bestCovers;
                        rule.m_correct = bestCorrect;
                    }
                }
            }
            if (DRITest.m_attr == -1) { // Couldn't find any sensible DRITest
                logger.trace("\t\t\tCouldn't find any sensible DRITest");
                break;
            }
            logger.trace("\t\t\tAdd DRITest {} to rule {}",
                    DRITest == null ? "null" : DRITest.toStr(e),
                    rule == null ? "null" : rule.toStr());

//                    oldTest = addTest(rule, oldTest, DRITest);
            freqsLogger.trace("add item {}, to rule {}", DRITest.toStr(e), rule.toStr());
            rule.addTest(DRITest);

            ruleE = rule.coveredBy(ruleE);
            logger.trace("\t\t\tR_{} coveredBy {}", rule.id, ruleE.numInstances());
            if (attUsed == (e.numAttributes() - 1)) { // Used all attributes.
                logger.trace("\t\t\tused all the attributes, break loop");
                break;
            }
        }
        Instances result = rule.notCoveredBy(e);
        logger.trace("\tE now contains {} instances\n", result.numInstances());
        freqsLogger.trace("completed rule {}\n\n", rule.toStr());
        return new Pair<>(rule, result);
    }

    /**
     * Does E contain any examples in the class C?
     *
     * @param E the instances to be checked
     * @param C the class
     * @return true if there are any instances of class C
     * @throws Exception if something goes wrong
     */
    private static boolean contains(Instances E, int C) throws Exception {

        Enumeration enu = E.enumerateInstances();
        while (enu.hasMoreElements()) {
            if ((int) ((Instance) enu.nextElement()).classValue() == C) {
                return true;
            }
        }
        return false;
    }

    /**
     * Is this attribute mentioned in the rule?
     *
     * @param attr the attribute to be checked for
     * @param t    test contained by rule
     * @return true if the attribute is mentioned in the rule
     */
    private static boolean isMentionedIn(Attribute attr, DRITest t) {

        if (t == null) {
            return false;
        }
        if (t.m_attr == attr.index()) {
            return true;
        }
        return isMentionedIn(attr, t.m_next);
    }

    /**
     * Prints a description of the classifier.
     *
     * @return a description of the classifier as a string
     */
    public String toString() {
        int maxDigits = pOptions.getMaxNumInstances();
        if (m_rules == null) {
            return "Prism: No model built yet.";
        }

        StringBuilder sb = new StringBuilder();

        sb.append("Number of rules generated = " + m_rules.size());
        String intPattern = EDRIUtils.formatIntPattern(m_rules.size());
        sb.append("\nPrism rules ( frequency, confidence ) \n----------\n");
        for (int i = 0; i < m_rules.size(); i++) {
            DRIRule rule = m_rules.get(i);
            sb.append(String.format(intPattern + "- ", i) + rule.toString(maxDigits) + "\n");
        }

        sb.append(String.format("Avg. Rule Length = %2.2f", getAvgRuleLength(m_rules)) + "\n");
        return sb.toString();
    }

    //all based of all number of instances, remaining default rule length = 0
    private double getAvgRuleLength(List<DRIRule> rules) {
        double result = 0;
        for (DRIRule rule : rules) {
            result += rule.getLenghtWeighted();
        }
        return result / (double) pOptions.getMaxNumInstances();
    }

    /**
     * Returns the revision string.
     *
     * @return the revision
     */
    public String getRevision() {
        return RevisionUtils.extract("$Revision: 001 $");
    }

    /**
     * Main method for testing this class
     *
     * @param args the commandline parameters
     */
    public static void main(String[] args) throws Exception {
        String inFile = "/media/suhel/workspace/work/wekaprism/data/fadi.arff";
//        String command = "-t "+ inFile + " -T "+ inFile + " -no-cv";
//        runClassifier(new Prism(), args);

        Instances data = new Instances(EDRIUtils.readDataFile(inFile));
        data.setClassIndex(data.numAttributes() - 1);
        Classifier classifier = new eDRI();

        classifier.buildClassifier(data);

    }

}
