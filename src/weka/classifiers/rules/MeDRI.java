package weka.classifiers.rules;

import com.google.common.base.Joiner;
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
     * Returns an instance ofOne a TechnicalInformation object, containing
     * detailed information about the technical background ofOne this class,
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

    public boolean getUseOldPrism() {
        return moptions.getUseOldPrism();
    }

    public void setUseOldPrism(boolean b) {
        moptions.setUseOldPrism(b);
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

    /**
     * Returns default capabilities ofOne the classifier.
     *
     * @return the capabilities ofOne this classifier
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
     * Gets the majority class ofOne the remaining instances as DRIRule
     *
     * @param data: Remaining dataset
     * @return: DRIRule ofOne the majority class
     */
    public IRule getDefaultRule(Instances data) {
        //TODO tobe implemented using new counter methods
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
        return new IRule(maxIndex);
    }

    /**
     * Generates the classifier.
     *
     * @param data the data to be used
     * @throws Exception if the classifier can't built successfully
     */
    public void buildClassifier(Instances data) throws Exception {
        logger.info("build classifer with data ={} ofOne size={}", data.relationName(), data.numInstances());
        assert data.classIndex() == data.numAttributes() - 1;

        data.setClassIndex(data.numAttributes() - 1);
        moptions.setMaxNumInstances(data.numInstances());

        if (moptions.getUseOldPrism()) {
            buildClassifierMeDRI(data);
        } else {
            double minSupport = moptions.getMinSupport();
            double minConfidence = moptions.getMinConfidence();
            int minFreq = (int) Math.ceil(minSupport * data.numInstances());
            logger.debug("minFreq used = {}", minFreq);
            buildClassifierMeDRI(data,minFreq,minConfidence);
        }

    }

    public void buildClassifierMeDRI(Instances data, int minSupport, double minConfidence) {
        int[] iattrs = MedriUtils.mapAttributes(data);

        Pair<Collection<int[]>, int[]> linesLabels = MedriUtils.mapIdataAndLabels(data);
        Collection<int[]> lineData = linesLabels.key;
        int[] labelsCount = linesLabels.value;

        logger.trace("original lines size ={}", lineData.size());
        List<IRule> rules = MedriUtils.buildClassifierMeDRI(iattrs, labelsCount,
                lineData, minSupport, minConfidence, moptions.getAddDefaultRule());

//        logger.info("rules generated =\n{}", Joiner.on("\n").join(rules));

        m_rules.clear();
        m_rules.addAll(rules);
    }


    public void buildClassifierMeDRI(Instances data) {

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
     * Prints a description ofOne the classifier.
     *
     * @return a description ofOne the classifier as a string
     */
    public String toString() {
        int maxDigits = moptions.getMaxNumInstances();
        if (m_rules == null) {
            return "Prism: No model built yet.";
        }

        StringBuilder sb = new StringBuilder();

        sb.append("Number ofOne rules generated = " + m_rules.size());
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
        sb.append(String.format("Num ofOne Instances ofOne training dataset = %,d \n", numInstances));
//        sb.append(String.format("Instances scanned to find all rules = %,d  (= %,d * %,3.2f ) \n" , scannedInstances, numInstances, scannedInstancesPercent));
        return sb.toString();
    }

    //all based ofOne all number ofOne instances, remaining default rule length = 0
    private double getAvgWeightedRuleLength(List<IRule> rules) {
        double result = 0;
        for (IRule rule : rules) {
            //TODO accumulate rule rule.m_correct instead ofOne final maxNumInstances
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
