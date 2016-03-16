package weka.classifiers.rules.edri;

import ch.qos.logback.classic.Level;
import org.slf4j.LoggerFactory;
import weka.classifiers.rules.eDRI;
import weka.core.*;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.Vector;

/**
 * Created by suhel on 11/03/16.
 */
public class DRIOptions implements OptionHandler, Serializable{

    static final long serialVersionUID = 1310258885025902107L;

    private boolean addDefaultRule = false;

    public boolean getAddDefaultRule() {
        return addDefaultRule;
    }

    public void setAddDefaultRule(boolean addDefaultRule) {
        this.addDefaultRule = addDefaultRule;
    }

    //    static Logger logger = LoggerFactory.getLogger(DRIOptions.class);


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

    protected boolean useOldPrism = true;
    protected String m_debugLevel= "info";

    public String debugLevel() {
        return m_debugLevel;
    }
    protected double minSupport = 0.02;

    protected double minConfidence = 0.0;

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

    public boolean getUseOldPrism() {
        return useOldPrism;
    }

    public void setUseOldPrism(boolean useOldPrism) {
        this.useOldPrism = useOldPrism;
    }

    public double getMinSupport() {
        return minSupport;
    }

    public void setMinSupport(double minSupport) {
        this.minSupport = minSupport;
    }


    public double getMinConfidence() {
        return minConfidence;
    }

    public void setMinConfidence(double minConfidence) {
        this.minConfidence = minConfidence;
    }






    @Override
    public Enumeration listOptions() {
        Vector<Option> result = new Vector<>(1);
        result.addElement(new Option("Old Prism algorithm", "P", 0, "-P"));
        result.addElement(new Option("Add Default Rule?", "R", 0, "-R"));
        result.addElement(new Option("minimum support", "S", 1, "-S <lower bound for minimum support >"));
//        result.addElement(new Option("minimum support", "s", 1, "-s <lower bound for minimum support >"));
        result.addElement(new Option("minimum confidence", "C", 1, "-C <minimum confidence of a rule >"));
        result.addElement(new Option("descritption", "D", 1, "-D < off | trace | debug | info | warn | error | fatal >"));
        return result.elements();

    }

    @Override
    public void setOptions(String[] options) throws Exception {
        String  optionString = Utils.getOption('D', options);
        m_debugLevel = LEVELS.valueOf(optionString).name();

        String sConfidence = Utils.getOption('C', options);
        minConfidence = Double.parseDouble(sConfidence);
        String sSupport = Utils.getOption('S', options);
        minSupport = Double.parseDouble(sSupport);

        useOldPrism = Utils.getFlag('P', options);
        addDefaultRule = Utils.getFlag('R', options);

    }

    @Override
    public String[] getOptions() {
        String[] result = new String[8];
        int currentIndex = 0;
        result[currentIndex++] = "-D";
        result[currentIndex++] = m_debugLevel;

        result[currentIndex++] = "-S";
        result[currentIndex++] = ""+minSupport;

        result[currentIndex++] = "-C";
        result[currentIndex++] = ""+ minConfidence;

        if(useOldPrism)
            result[currentIndex++] = "-P";
        else
            result[currentIndex++] = "";

        if(addDefaultRule)
            result[currentIndex++] = "-R";
        else
            result[currentIndex++] = "";

        return result;
    }

    public void setDebugLevel(SelectedTag newMethod) {
         m_debugLevel = newMethod.getSelectedTag().getIDStr();
    }

    public  void changeLogLevelRunTime() {
        changeLogLevelRunTime(m_debugLevel);
    }

    public static void changeLogLevelRunTime(String logLevel) {
//        Logger lg = (Logger) LoggerFactory.getLogger(eDRI.class);
        ((ch.qos.logback.classic.Logger)LoggerFactory.getLogger(eDRI.class)) .setLevel(Level.toLevel(logLevel));

    }

}
