package weka.classifiers.rules;

import weka.core.*;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Vector;

/**
 * Created by suhel on 11/03/16.
 */
public class PrismOptions implements OptionHandler, Serializable{


    public SelectedTag getDebugLevel() {
        return new SelectedTag(m_debugLevel, LEVELS.toTags());

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


    protected String m_debugLevel= "DEBUG";
    protected int minSupport = 0;

    public int getMinSupport() {
        return minSupport;
    }

    public void setMinSupport(int minSupport) {
        this.minSupport = minSupport;
    }

    protected double minConfidence = 0.4;

    public double getMinConfidence() {
        return minConfidence;
    }

    public void setMinConfidence(double minConfidence) {
        this.minConfidence = minConfidence;
    }


    public String getM_debugLevel() {
        return m_debugLevel;
    }

    public void setM_debugLevel(String m_debugLevel) {
        this.m_debugLevel = m_debugLevel;
    }



    @Override
    public Enumeration listOptions() {
        Vector<Option> result = new Vector<>(1);
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

    }

    @Override
    public String[] getOptions() {
        String[] result = new String[6];
        int currentIndex = 0;
        result[currentIndex++] = "-D";
        result[currentIndex++] = m_debugLevel;

        result[currentIndex++] = "-S";
        result[currentIndex++] = ""+minSupport;

        result[currentIndex++] = "-C";
        result[currentIndex++] = ""+ minConfidence;


        return result;
    }

    public void setDebugLevel(SelectedTag newMethod) {
        setM_debugLevel(newMethod.getSelectedTag().getIDStr());
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
