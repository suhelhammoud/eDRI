package weka.classifiers.rules;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import weka.classifiers.Classifier;
import weka.classifiers.rules.medri.IRule;
import weka.classifiers.rules.medri.MedriOptions;
import weka.classifiers.rules.medri.MedriUtils;
import weka.classifiers.rules.medri.Pair;
import weka.core.*;

import java.util.*;

/**
 * Created by suhel on 23/03/16.
 */
public class MeDRI extends Classifier
        implements OptionHandler, TechnicalInformationHandler {

    static Logger logger = LoggerFactory.getLogger(MeDRI.class);
    static final long serialVersionUID = 1310258885525902107L;


    /**
     * Returns the revision string.
     *
     * @return the revision
     */
    public String getRevision() {
        return RevisionUtils.extract("$Revision: 001 $");
    }

    /**
     * Returns a string describing classifier
     *
     * @return a description suitable for
     * displaying in the explorer/experimenter gui
     */
    public String globalInfo() {
        return "Class for building and using a MeDRI rule set for classification. "
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

        result = new TechnicalInformation(TechnicalInformation.Type.ARTICLE);
        result.setValue(TechnicalInformation.Field.AUTHOR, "F. Thabtah, S. Hammoud");
        result.setValue(TechnicalInformation.Field.YEAR, "2016");
        result.setValue(TechnicalInformation.Field.TITLE, "MeDRI: An algorithm for inducing modular rules");
        result.setValue(TechnicalInformation.Field.JOURNAL, "Journal");
        result.setValue(TechnicalInformation.Field.VOLUME, "vol");
        result.setValue(TechnicalInformation.Field.NUMBER, "number");
        result.setValue(TechnicalInformation.Field.PAGES, "p_start-p_end");
        return result;
    }

    /**
     * Holds algorithm configurations and OptionHandler parameters
     */
    private MedriOptions moptions = new MedriOptions();

    /**
     * Holds algorithm configurations and MedriOption parameters
     */
    private List<IRule> m_rules = new ArrayList<>();

    /**
     * Classifies a given instance.
     *
     * @param inst the instance to be classified
     * @return the classification
     */
    public double classifyInstance(Instance inst) {

        for (IRule rule : m_rules) {
            int cls = rule.classify(MedriUtils.toIntArray(inst));
            if (cls != IRule.EMPTY)
                return cls;
        }
        return Instance.missingValue();
    }

    @Override
    public Enumeration listOptions() {
        return moptions.listOptions();
    }

    @Override
    public void setOptions(String[] options) throws Exception {
        moptions.setOptions(options);
    }

    @Override
    public String[] getOptions() {
        return moptions.getOptions();
    }

    public boolean getAddDefaultRule() {
        return moptions.getAddDefaultRule();
    }

    public void setAddDefaultRule(boolean b) {
        moptions.setAddDefaultRule(b);
    }



    public double getMinSupport() {
        return moptions.getMinSupport();
    }

    public void setMinSupport(double support) {
        moptions.setMinSupport(support);
    }

    public double getMinConfidence() {
        return moptions.getMinConfidence();
    }

    public void setMinConfidence(double confidence) {
        moptions.setMinConfidence(confidence);
    }

    public void setDebugLevel(SelectedTag newMethod) {
        moptions.setDebugLevel(newMethod);
    }

    public SelectedTag getDebugLevel() {
        return moptions.getDebugLevel();
    }

    public String debugLevelTipText() {
        return "debug level tip text";
    }

public void setAlgorithm(SelectedTag newMethod) {
        moptions.setAlgorithm(newMethod);
    }

    public SelectedTag getAlgorithm() {
        return moptions.getAlgorithm();
    }

    public String algorithmTipText() {
        return "Which algorithm to use, Prism, eDRI, or MeDRI ?";
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
        result.enable(Capabilities.Capability.NOMINAL_ATTRIBUTES);

        // class
        result.enable(Capabilities.Capability.NOMINAL_CLASS);
        result.enable(Capabilities.Capability.MISSING_CLASS_VALUES);

        return result;
    }



    /**
     * Generates the classifier.
     *
     * @param data the data to be used
     * @throws Exception if the classifier can't built successfully
     */
    public void buildClassifier(Instances data) throws Exception {
        logger.info("build classifer with data ={} of size={}", data.relationName(), data.numInstances());
        assert data.classIndex() == data.numAttributes() - 1;

        data.setClassIndex(data.numAttributes() - 1);
        moptions.setMaxNumInstances(data.numInstances());

        String algorithm = moptions.getAlgorithm().toString().toLowerCase();
        switch (algorithm) {
            case "prism":
                buildClassifierPrism(data);
                break;

            case "edri":
                double minSupport = moptions.getMinSupport();
                double minConfidence = moptions.getMinConfidence();
                int minFreq = (int) Math.ceil(minSupport * data.numInstances());
                logger.debug("minFreq used = {}", minFreq);
                buildClassifierEDRI(data,minFreq,minConfidence, moptions.getAddDefaultRule());
                break;

            case "medri":
                //TODO implement it
                System.out.println("run medri algorithm");
                break;

            default:
                System.err.println("Algorithm is no listed before");

        }

    }

    public void buildClassifierEDRI(Instances data, int minSupport, double minConfidence, boolean addDefaultRule) {
        int[] iattrs = MedriUtils.mapAttributes(data);

        Pair<Collection<int[]>, int[]> linesLabels = MedriUtils.mapIdataAndLabels(data);
        Collection<int[]> lineData = linesLabels.key;
        int[] labelsCount = linesLabels.value;

        logger.trace("original lines size ={}", lineData.size());
        List<IRule> rules = MedriUtils.buildClassifierEDRI(iattrs, labelsCount,
                lineData, minSupport, minConfidence, addDefaultRule);

//        logger.info("rules generated =\n{}", Joiner.on("\n").join(rules));

        m_rules.clear();
        m_rules.addAll(rules);
    }


    public void buildClassifierPrism(Instances data) {

        int[] iattrs = MedriUtils.mapAttributes(data);

        Pair<Collection<int[]>, int[]> linesLabels = MedriUtils.mapIdataAndLabels(data);
        Collection<int[]> lineData = linesLabels.key;
        int[] labelsCount = linesLabels.value;

        logger.trace("original lines size ={}", lineData.size());
        List<IRule> rules = MedriUtils.buildClassifierPrism(iattrs, labelsCount, lineData);

//        logger.info("rules generated =\n{}", Joiner.on("\n").join(rules));

        m_rules.clear();
        m_rules.addAll(rules);

    }

    /**
     * Prints a description of the classifier.
     *
     * @return a description of the classifier as a string
     */
    public String toString() {
        int maxDigits = moptions.getMaxNumInstances();
        if (m_rules == null) {
            return "Prism: No model built yet.";
        }

        StringBuilder sb = new StringBuilder();

        sb.append("Number of rules generated = " + m_rules.size());
        String intPattern = MedriUtils.formatIntPattern(m_rules.size());
        sb.append("\nPrism rules ( frequency, confidence ) \n----------\n");
        for (int i = 0; i < m_rules.size(); i++) {
            IRule rule = m_rules.get(i);
            sb.append(String.format(intPattern + " - ", (i + 1)) + rule + "\n");
        }

        sb.append(String.format("Avg. Weighted Rule Length = %2.2f", getAvgWeightedRuleLength(m_rules)) + "\n");
        sb.append(String.format("Avg. Rule Length = %2.2f", getAvgRuleLength(m_rules)) + "\n");

//        long scannedInstances = getScannedInstances();
        int numInstances = moptions.getMaxNumInstances();
//        double scannedInstancesPercent = (double)scannedInstances/(double)numInstances;
        sb.append(String.format("Num of Instances of training dataset = %,d \n", numInstances));
//        sb.append(String.format("Instances scanned to find all rules = %,d  (= %,d * %,3.2f ) \n" , scannedInstances, numInstances, scannedInstancesPercent));
        return sb.toString();
    }

    //all based of all number of instances, remaining default rule length = 0
    private double getAvgWeightedRuleLength(List<IRule> rules) {
        double result = 0;
        for (IRule rule : rules) {
            //TODO accumulate rule rule.m_correct instead of final maxNumInstances
            result += rule.getLenghtWeighted();
        }
        return result / (double) moptions.getMaxNumInstances();
    }

    private double getAvgRuleLength(List<IRule> rules) {
        double result = 0;
        for (IRule rule : rules) {
            result += rule.getLenght();
        }
        return result / (double) rules.size();
    }


}
