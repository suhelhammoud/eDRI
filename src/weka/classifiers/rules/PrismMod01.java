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
import weka.core.*;
import weka.core.Capabilities.Capability;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.*;


/**
 <!-- globalinfo-start -->
 * Class for building and using a PRISM rule set for classification. Can only deal with nominal attributes. Can't deal with missing values. Doesn't do any pruning.<br/>
 * <br/>
 * For more information, see <br/>
 * <br/>
 * J. Cendrowska (1987). PRISM: An algorithm for inducing modular rules. International Journal of Man-Machine Studies. 27(4):349-370.
 * <p/>
 <!-- globalinfo-end -->
 *
 <!-- technical-bibtex-start -->
 * BibTeX:
 * <pre>
 * &#64;article{Cendrowska1987,
 *    author = {J. Cendrowska},
 *    journal = {International Journal of Man-Machine Studies},
 *    number = {4},
 *    pages = {349-370},
 *    title = {PRISM: An algorithm for inducing modular rules},
 *    volume = {27},
 *    year = {1987}
 * }
 * </pre>
 * <p/>
 <!-- technical-bibtex-end -->
 *
 <!-- options-start -->
 * Valid options are: <p/>
 *
 * <pre> -D
 *  If set, classifier is run in debug mode and
 *  may output additional info to the console</pre>
 *
 <!-- options-end -->
 *
 * @author Ian H. Witten (ihw@cs.waikato.ac.nz)
 * @version $Revision: 5529 $
 */
public class PrismMod01
        extends Classifier
        implements OptionHandler, TechnicalInformationHandler {


    PrismOptions pOptions = new PrismOptions();
    static ch.qos.logback.classic.Logger lgLevel = (ch.qos.logback.classic.Logger)LoggerFactory.getLogger(PrismMod01.class);
    static Logger logger = LoggerFactory.getLogger(PrismMod01.class);


//    protected String m_debugLevel = "warn";

     /** for serialization */
    static final long serialVersionUID = 1310258880025902107L;

    /**
     * Returns a string describing classifier
     * @return a description suitable for
     * displaying in the explorer/experimenter gui
     */
    public String globalInfo() {
        return "Class for building and using a PRISM rule set for classification. "
                + "Can only deal with nominal attributes. Can't deal with missing values. "
                + "Doesn't do any pruning.\n\n"
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
        TechnicalInformation 	result;

        result = new TechnicalInformation(Type.ARTICLE);
        result.setValue(Field.AUTHOR, "J. Cendrowska");
        result.setValue(Field.YEAR, "1987");
        result.setValue(Field.TITLE, "PRISM: An algorithm for inducing modular rules");
        result.setValue(Field.JOURNAL, "International Journal of Man-Machine Studies");
        result.setValue(Field.VOLUME, "27");
        result.setValue(Field.NUMBER, "4");
        result.setValue(Field.PAGES, "349-370");



        return result;
    }




    /** The first rule in the list of rules */
    private List<PrismRule> m_rules = new ArrayList<>();

    /**
     * Classifies a given instance.
     *
     * @param inst the instance to be classified
     * @return the classification
     */
    public double classifyInstance(Instance inst) {

        int result = PrismRule.classifyInst(inst, m_rules);
        if (result == -1) {
            return Instance.missingValue();
        } else {
            return (double)result;
        }
    }

    @Override
    public Enumeration listOptions() {
        return  pOptions.listOptions();
//        return pOptions.listOptions();
    }

    @Override
    public void setOptions(String[] options) throws Exception {
        pOptions.setOptions(options);
//        m_debugLevel = Utils.getOption("D", options);

//        pOptions.setOptions(options);
    }

    @Override
    public String[] getOptions() {
        return pOptions.getOptions();

//        return pOptions.getOptions();
    }

    public int getMinSupport() {
        return pOptions.getMinSupport();
    }

    public void setMinSupport(int support) {
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
//        pOptions.setM_debugLevel(newMethod.getSelectedTag().getIDStr());
//        m_debugLevel = newMethod.getSelectedTag().getIDStr();
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
     * @return      the capabilities of this classifier
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
     * Generates the classifier.
     *
     * @param data the data to be used
     * @exception Exception if the classifier can't built successfully
     */
    public void buildClassifier(Instances data) throws Exception {
        lgLevel.setLevel(Level.toLevel(pOptions.m_debugLevel));
        List<PrismRule> rules = new ArrayList<>(data.numAttributes());

        int cl; // possible value of theClass
        Instances E = null, ruleE;
        PrismRule rule = null;

        Test test = null;
        Test oldTest = null;
        int bestCorrect, bestCovers, attUsed;
        Enumeration enumAtt;

        // can classifier handle the data?
        getCapabilities().testWithFail(data);

        // remove instances with missing class
        data = new Instances(data);
        data.deleteWithMissingClass();
        for (cl = 0; cl < data.numClasses(); cl++) { // for each class cl
            Attribute classAtt = data.attribute(data.classIndex());

            logger.debug("for class = {}", classAtt.value(cl));
            logger.debug("reset E from {} to {} instances",
                    E == null ? "null": E.numInstances(),
                    data.numInstances());
            E = data; // initialize E to the instance set

            while (contains(E, cl)) { // while E contains examples in class cl
                Pair<PrismRule, Instances> result = ruleInstances(cl, E);
                rules.add(result.key);
                E = result.value;
            }

            logger.debug("\t switching to next class with E contains {} instances\n", E.numInstances());

        }
        logger.debug("no more classes found");
        m_rules.clear();
        m_rules.addAll(rules);
    }

    class Pair<K, V>{
        final public  K key;
        final public V value;

        Pair(K key, V value) {
            this.key = key;
            this.value = value;
        }
    }

    private Pair<PrismRule, Instances> ruleInstances(int cl, Instances e) throws Exception {
//        Instances data = ;
//        List<PrismRule> rules = ;
        Attribute classAtt = e.attribute(e.classIndex());
        PrismRule rule;
        Instances ruleE;
        Test test;
        int attUsed;
        int bestCovers;
        int bestCorrect;
        Enumeration enumAtt;
        logger.debug("\tE contains {} class\n", classAtt.value(cl));
        rule = new PrismRule(e, cl);
//        rules.add(rule);
        logger.debug("\tNew rule {}", rule.toStr());
        ruleE = e; // examples covered by this rule
        logger.info("\truleE {}", ruleE.numInstances());
        while (rule.m_errors != 0) { // until the rule is perfect
            test = new Test(); // make a new test
            bestCorrect = bestCovers = attUsed = 0;

            // for every attribute not mentioned in the rule
            enumAtt = ruleE.enumerateAttributes();
            while (enumAtt.hasMoreElements()) {
                Attribute attr = (Attribute) enumAtt.nextElement();
                logger.debug("\t\t\tfor attr {} of class {}", attr.name(), classAtt.value(cl));
                if (isMentionedIn(attr, rule.m_test)) {
                    attUsed++;
                    logger.debug("\t\t\tSkip attr {}", attr, attr.name());
                    continue;
                }
                int M = attr.numValues();
                int[] covers = new int [M];
                int[] correct = new int [M];
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


                logger.debug("\t\t\t\tattr_{}  of {} Covers={}, correct {}", attr.name(), attrNames, Arrays.toString(covers), Arrays.toString(correct));

                // ... for each value of this attribute, see if this test is better
                for (int val = 0; val < M; val ++) {
                    int diff = correct[val] * bestCovers - bestCorrect * covers[val];

                    // this is a ratio test, correct/covers vs best correct/covers
                    if (test.m_attr == -1
                            || diff > 0 || (diff == 0 && correct[val] > bestCorrect)) {

                        // update the rule to use this test
                        bestCorrect = correct[val];
                        bestCovers = covers[val];
                        test.m_attr = attr.index();
                        test.m_val = val;
                        rule.m_errors = bestCovers - bestCorrect;
                    }
                }

            }
            if (test.m_attr == -1) { // Couldn't find any sensible test
                logger.debug("\t\t\tCouldn't find any sensible test");
                break;
            }
            logger.debug("\t\t\tAdd test {} to rule {}",
                    test == null? "null": test.toStr(e),
                    rule == null ? "null": rule.toStr());

//                    oldTest = addTest(rule, oldTest, test);
            rule.addTest(test);

            ruleE = rule.coveredBy(ruleE);
            logger.debug("\t\t\tR_{} coveredBy {}", rule.id, ruleE.numInstances());
            if (attUsed == (e.numAttributes() - 1)) { // Used all attributes.
                logger.debug("\t\t\tused all the attributes, break loop");
                break;
            }
        }
        Instances result = rule.notCoveredBy(e);
        logger.debug("\tE now contains {} instances\n", result.numInstances());
        return new Pair<>(rule, result);
    }


    /**
     * Add a test to this rule.
     *
     * @param rule the rule to which test is to be added
     * @param lastTest the rule's last test
     * @param newTest the test to be added
     * @return the new last test of the rule
     */
    private Test addTest(PrismRule rule, Test lastTest, Test newTest) {

        if (rule.m_test == null) {
            rule.m_test = newTest;
        } else {
            lastTest.m_next = newTest;
        }
        return newTest;
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
     * @param t test contained by rule
     * @return true if the attribute is mentioned in the rule
     */
    private static boolean isMentionedIn(Attribute attr, Test t) {

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

        if (m_rules == null) {
            return "Prism: No model built yet.";
        }
        return "Prism rules\n----------\n" + m_rules.toString();
    }

    /**
     * Returns the revision string.
     *
     * @return		the revision
     */
    public String getRevision() {
        return RevisionUtils.extract("$Revision: 5529 $");
    }

    /**
     * Main method for testing this class
     *
     * @param args the commandline parameters
     */
    public static void main(String[] args) throws Exception{
        String inFile = "/media/suhel/workspace/work/wekaprism/data/fadi.arff";
//        String command = "-t "+ inFile + " -T "+ inFile + " -no-cv";
//        runClassifier(new Prism(), args);

        Instances data = new Instances(readDataFile(inFile));
        data.setClassIndex(data.numAttributes()-1);
        Classifier classifier = new PrismMod01();
        classifier.buildClassifier(data);

    }


    public static BufferedReader readDataFile(String filename) {
        BufferedReader inputReader = null;

        try {
            inputReader = new BufferedReader(new FileReader(filename));
        } catch (FileNotFoundException ex) {
            System.err.println("File not found: " + filename);
        }

        return inputReader;
    }
}
