package weka.classifiers.rules;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import weka.classifiers.Classifier;
import weka.classifiers.rules.medri.*;
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


    public double getMinFrequency() {
        return moptions.getMinFrequency();
    }

    public void setMinFrequency(double support) {
        moptions.setMinFrequency(support);
    }

    public double getMinRuleStrength() {
        return moptions.getMinRuleStrength();
    }

    public void setMinRuleStrength(double confidence) {
        moptions.setMinRuleStrength(confidence);
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
        moptions.setInstancesCopy(data);

        moptions.resetScannedInstances(0);

        String algorithm = moptions.getAlgorithm().toString().toLowerCase();
        switch (algorithm) {
            case "prism":
                buildClassifierPrism(data, moptions.getAddDefaultRule());
                break;

            case "edri": {
                double minSupport = moptions.getMinFrequency();
                double minConfidence = moptions.getMinRuleStrength();
                int minFreq = (int) Math.ceil(minSupport * data.numInstances()+ 1.e-6);
                logger.debug("minFreq used = {}", minFreq);
                buildClassifierEDRI(data, minFreq, minConfidence, moptions.getAddDefaultRule());
            }
            break;

            case "medri": {
                double minSupport = moptions.getMinFrequency();
                double minConfidence = moptions.getMinRuleStrength();
                int minFreq = (int) Math.ceil(minSupport * data.numInstances() + 1.e-6);
                logger.debug("minFreq used = {}", minFreq);
                buildClassifierMeDRI(data, minFreq, minConfidence, moptions.getAddDefaultRule());

            }
            break;

            default:
                System.err.println("Algorithm is no listed before");

        }

    }



    public MeDRIResult buildClassifierMeDRI(Instances data, int minSupport, double minConfidence, boolean addDefaultRule) {
        int[] iattrs = MedriUtils.mapAttributes(data);

        Pair<Collection<int[]>, int[]> linesLabels = MedriUtils.mapIdataAndLabels(data);
        Collection<int[]> lineData = linesLabels.key;
        int[] labelsCount = linesLabels.value;

        logger.trace("original lines size ={}", lineData.size());
       MeDRIResult result = MedriUtils.buildClassifierMeDRI(iattrs, labelsCount,
                lineData, minSupport, minConfidence, addDefaultRule);

        m_rules.clear();
        m_rules.addAll(result.getRules());
        moptions.resetScannedInstances(result.getScannedInstances());
        return result;
    }

    public MeDRIResult buildClassifierEDRI(Instances data, int minSupport, double minConfidence, boolean addDefaultRule) {
        int[] iattrs = MedriUtils.mapAttributes(data);

        Pair<Collection<int[]>, int[]> linesLabels = MedriUtils.mapIdataAndLabels(data);
        Collection<int[]> lineData = linesLabels.key;
        int[] labelsCount = linesLabels.value;

        logger.trace("original lines size ={}", lineData.size());
        MeDRIResult result = MedriUtils.buildClassifierEDRI(iattrs, labelsCount,
                lineData, minSupport, minConfidence, addDefaultRule);

        m_rules.clear();
        m_rules.addAll(result.getRules());
        moptions.resetScannedInstances(result.getScannedInstances());
        return result;
    }


    public MeDRIResult buildClassifierPrism(Instances data, boolean addDefaultRule) {

        int[] iattrs = MedriUtils.mapAttributes(data);

        Pair<Collection<int[]>, int[]> linesLabels = MedriUtils.mapIdataAndLabels(data);
        Collection<int[]> lineData = linesLabels.key;
        int[] labelsCount = linesLabels.value;

        logger.trace("original lines size ={}", lineData.size());
        MeDRIResult result = MedriUtils.buildClassifierPrism(iattrs, labelsCount, lineData, addDefaultRule);

        m_rules.clear();
        m_rules.addAll(result.getRules());
        moptions.resetScannedInstances(result.getScannedInstances());
        return result;
    }

    public String toString(Instances data, int maxDigit) {
        if (m_rules == null) {
            return getAlgorithm() + "No model built yet.";
        }

        StringBuilder sb = new StringBuilder();

        sb.append(String.format("Classifier = %s , add default rule = %s", getAlgorithm().toString(), String.valueOf(getAddDefaultRule())));
        if (!getAlgorithm().toString().toUpperCase().equals("PRISM")) {
            sb.append(String.format(" min freq = %.4f, min strength = %.2f", getMinFrequency(), getMinRuleStrength()));
        }
        sb.append("\nNumber of rules generated = " + m_rules.size());
        String intPattern = MedriUtils.formatIntPattern(m_rules.size());
        sb.append("\n" + getAlgorithm() +" rules ( frequency, strength ) \n----------\n");
        for (int i = 0; i < m_rules.size(); i++) {
            IRule rule = m_rules.get(i);
            sb.append(String.format(intPattern + " - ", (i + 1)) + rule.toString(data, maxDigit) + "\n");
        }

        sb.append(String.format("\nClassifier = %s , add default rule = %s", getAlgorithm().toString(), String.valueOf(getAddDefaultRule())));
        if (!getAlgorithm().toString().toUpperCase().equals("PRISM")) {
            sb.append(String.format(" min freq = %.4f, min strenght = %.2f\n ", getMinFrequency(), getMinRuleStrength()));
        } else sb.append("\n");

        sb.append(String.format("Avg. Weighted Rule Length = %2.2f", getAvgWeightedRuleLength(m_rules)) + "\n");
        sb.append(String.format("Avg. Rule Length = %2.2f", getAvgRuleLength(m_rules)) + "\n");

        int numInstances = moptions.getMaxNumInstances();
        sb.append(String.format("Num of Instances of training dataset = %,d \n", numInstances));
        Pair<Integer, Integer> pnp = getInstancesOfPerfectRules(m_rules);
        sb.append(String.format("# Instances covered with perfect rules = %,d instances  ( %.2f %% )  \n", (int)pnp.key, (double)pnp.key/pnp.value));

        Pair<Integer, Integer> pnprules = perfectRules(m_rules);
        sb.append(String.format("# perfect rules = %,d , # not perfect rules = %,d \n", pnprules.key, pnprules.value));

        long scannedInstances = moptions.getScannedInstances();
        double scannedInstancesPercent = (double)scannedInstances/(double)numInstances;


        sb.append(String.format("Instances scanned to find all rules = %,d  (= %,d * %,3.2f ) \n" , scannedInstances, numInstances, scannedInstancesPercent));
        return sb.toString();
    }

    private static Pair<Integer, Integer> perfectRules(List<IRule> rules) {
        int perfectRules = 0;
        int notPerfectRules = 0;

        for (IRule rule : rules) {
            if (1 - rule.getConfidence() < 1e-6) {
                perfectRules++;
            }else notPerfectRules ++;
        }
        return new Pair(perfectRules, notPerfectRules);
    }

    private static Pair<Integer, Integer> getInstancesOfPerfectRules(List<IRule> rules) {
        int perfectRules = 0;
        int totals = 0;

        for (IRule rule : rules) {
            int correct = rule.getCorrect();
            totals += correct;
            if (1 - rule.getConfidence() < 1e-6) {
                perfectRules+=  correct;
            }
        }
//        perfectRules = perfectRules/totals;
//        notPerfectRules = notPerfectRules/totals;
        return new Pair(perfectRules, totals );
    }


    /**
     * Prints a description of the classifier.
     *
     * @return a description of the classifier as a string
     */
    public String toString() {
        int maxDigits = moptions.getMaxNumInstances();
        Instances data = moptions.getInstances();
        return toString(data, maxDigits);
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
