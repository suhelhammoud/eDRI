package weka.classifiers.rules;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import weka.core.*;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Vector;

/**
 * Created by suhel on 11/03/16.
 */
public class PrismOptions implements OptionHandler, Serializable{

    static final long serialVersionUID = 1310258880025902107L;
//    static Logger logger = LoggerFactory.getLogger(PrismOptions.class);


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
    protected String m_debugLevel= "DEBUG";
    protected int minSupport = 1;
    protected double minConfidence = 0.8;

    public SelectedTag getDebugLevel() {
        return new SelectedTag(m_debugLevel, LEVELS.toTags());

    }

    public boolean getUseOldPrism() {
        return useOldPrism;
    }

    public void setUseOldPrism(boolean useOldPrism) {
        this.useOldPrism = useOldPrism;
    }

    public int getMinSupport() {
        return minSupport;
    }

    public void setMinSupport(int minSupport) {
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
        result.addElement(new Option("minimum support", "S", 1, "-S <lower bound for minimum support >"));
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
        minSupport = Integer.parseInt(sSupport);

        useOldPrism = Utils.getFlag('P', options);

    }

    @Override
    public String[] getOptions() {
        String[] result = new String[7];
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

        return result;
    }

    public void setDebugLevel(SelectedTag newMethod) {
         m_debugLevel = newMethod.getSelectedTag().getIDStr();
    }

    public  void changeLogLevelRunTime() {
        changeLogLevelRunTime(m_debugLevel);
    }

    public static void changeLogLevelRunTime(String logLevel) {
//        Logger lg = (Logger) LoggerFactory.getLogger(PrismMod01.class);
        ((ch.qos.logback.classic.Logger)LoggerFactory.getLogger(PrismMod01.class)) .setLevel(Level.toLevel(logLevel));

    }
        public static void main(String[] args) throws Exception {

        PrismOptions opt = new PrismOptions();
        System.out.println(((Option)opt.listOptions().nextElement()).synopsis());

        String s = "-D fatal";
        opt.setOptions(s.split("\\s+"));

        String[] srtOpt = opt.getOptions();
        System.out.println(Arrays.toString(srtOpt));
    }
}
