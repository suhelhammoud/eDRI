package weka.classifiers.rules.medri;

import ch.qos.logback.classic.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import weka.classifiers.rules.eDRI;
import weka.core.*;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.Vector;

/**
 * Created by suhel on 11/03/16.
 */
public class MedriOptions implements OptionHandler, Serializable{

    static final long serialVersionUID = 1310258885025902127L;
    static Logger logger = LoggerFactory.getLogger(MedriOptions.class);

    private boolean addDefaultRule = false;

    public boolean getAddDefaultRule() {
        return addDefaultRule;
    }

    public void setAddDefaultRule(boolean addDefaultRule) {
        this.addDefaultRule = addDefaultRule;
    }



    enum LEVELS {off, trace, debug, info, warn, error, fatal;
        public static Tag[] toTags(){
            LEVELS[] levels = values();
            Tag[] result = new Tag[levels.length];
            for (int i = 0; i < levels.length; i++) {
                result[i] = new Tag(i, levels[i].name(), levels[i].name());
            }
            return result;
        };
    }


    enum ALGORITHMS {Prism, eDRI, MeDRI;
        public static Tag[] toTags(){
            ALGORITHMS[] levels = values();
            Tag[] result = new Tag[levels.length];
            for (int i = 0; i < levels.length; i++) {
                result[i] = new Tag(i, levels[i].name(), levels[i].name());
            }
            return result;
        };
    }

    protected String m_debugLevel= "info";
    protected String m_algorithm= "Prism";


    public String debugLevel() {
        return m_debugLevel;
    }
    protected double minFrequency = 0.01;

    protected double minRuleStrength = 0.5;

    protected int maxNumInstances = 0;

    public int getMaxNumInstances() {
        return maxNumInstances;
    }

    public void setMaxNumInstances(int maxNumInstances) {
        this.maxNumInstances = maxNumInstances;
    }

    public SelectedTag getDebugLevel() {
        return new SelectedTag(m_debugLevel, LEVELS.toTags());

    }

    public SelectedTag getAlgorithm() {
        return new SelectedTag(m_algorithm, ALGORITHMS.toTags());

    }


    public double getMinFrequency() {
        return minFrequency;
    }

    public void setMinFrequency(double minFrequency) {
        this.minFrequency = Math.abs(minFrequency);
    }


    public double getMinRuleStrength() {
        return minRuleStrength;
    }

    public void setMinRuleStrength(double minRuleStrength) {
        this.minRuleStrength = Math.abs(minRuleStrength);
    }



    Instances instances;

    public void setInstancesCopy(Instances data) {
        instances = new Instances(data, 0);
    }

    public Instances getInstances() {
        return instances;
    }

    private long scannedInstances = 0;

    public void resetScannedInstances(long init) {
        scannedInstances = init;
    }

    public long increaseScannedInstances(long a) {
        scannedInstances += a;
        return scannedInstances;
    }
    public long increaseScannedInstances() {
        scannedInstances++;
        return scannedInstances;
    }

    public long getScannedInstances() {
        return scannedInstances;
    }


    @Override
    public Enumeration listOptions() {
        Vector<Option> result = new Vector<>(1);
        result.addElement(new Option("Add Default Rule?", "R", 0, "-R"));
        result.addElement(new Option("minimum support", "S", 1, "-S <lower bound for minimum support >"));
        result.addElement(new Option("minimum confidence", "C", 1, "-C <minimum confidence ofOne a rule >"));
        result.addElement(new Option("descritption", "D", 1, "-D < off | trace | debug | info | warn | error | fatal >"));
        result.addElement(new Option("descritption", "A", 1, "-A < Prism | eDRI | MeDRI >"));
        return result.elements();

    }

    @Override
    public void setOptions(String[] options) throws Exception {
        String  optionString = Utils.getOption('D', options);
        m_debugLevel = LEVELS.valueOf(optionString).name();

        String  algoString = Utils.getOption('A', options);
        m_algorithm = ALGORITHMS.valueOf(algoString).name();


        String sConfidence = Utils.getOption('C', options);
        minRuleStrength = Double.parseDouble(sConfidence);
        String sSupport = Utils.getOption('S', options);
        minFrequency = Double.parseDouble(sSupport);

        addDefaultRule = Utils.getFlag('R', options);

    }

    @Override
    public String[] getOptions() {
        String[] result = new String[9];
        int currentIndex = 0;
        result[currentIndex++] = "-D";
        result[currentIndex++] = m_debugLevel;

        result[currentIndex++] = "-A";
        result[currentIndex++] = m_algorithm;

        result[currentIndex++] = "-S";
        result[currentIndex++] = ""+ minFrequency;

        result[currentIndex++] = "-C";
        result[currentIndex++] = ""+ minRuleStrength;

        if(addDefaultRule)
            result[currentIndex++] = "-R";
        else
            result[currentIndex++] = "";

        return result;
    }

    public void setDebugLevel(SelectedTag newMethod) {
         m_debugLevel = newMethod.getSelectedTag().getIDStr();
    }

    public void setAlgorithm(SelectedTag algo) {
         m_algorithm = algo.getSelectedTag().getIDStr();
    }


    public  void changeLogLevelRunTime() {
        changeLogLevelRunTime(m_debugLevel);
    }


    public static void changeLogLevelRunTime(String logLevel) {
//        Logger lg = (Logger) LoggerFactory.getLogger(eDRI.class);
        ((ch.qos.logback.classic.Logger)LoggerFactory.getLogger(eDRI.class)) .setLevel(Level.toLevel(logLevel));

    }

    public String printRunConf() {
        StringBuilder sb = new StringBuilder();
        return "";
    }

}
