package weka.classifiers.rules.edri;

import weka.core.Instance;
import weka.core.Instances;
import weka.core.RevisionHandler;
import weka.core.RevisionUtils;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Class for storing a PRISM ruleset, i.e. a list of rules
 */
public class DRIRule
        implements Serializable, RevisionHandler {

    static AtomicInteger ID = new AtomicInteger();
    final public int id;
    /**
     * for serialization
     */
    static final long serialVersionUID = 424878435065650583L;

    /**
     * The classification
     */
    final int m_classification;

    /**
     * The instance
     */
    final Instances m_instances;

    /**
     * First test of this rule
     */
    public DRITest m_text;

    /**
     * Number of errors made by this rule (will end up 0)
     */
    public int m_errors;

    public int m_covers;
    public int m_correct;

    /**
     * Constructor that takes instances and the classification.
     *
     * @param data the instances
     * @param cl   the class
     * @throws Exception if something goes wrong
     */
    public DRIRule(Instances data, int cl)  {
        this.id = ID.incrementAndGet();

//        m_instances = data;//TODO no need to assign data to m_instances
        m_classification = cl;
        m_text = null;
//        m_next = null;

        m_instances = new Instances(data, 0);
//        calcNotCovered(data);
    }

    public int updateAndGetNotCovered(Instances data) {
        m_errors = 0;
        //count not covered number
        Enumeration enu = data.enumerateInstances();
        while (enu.hasMoreElements()) {
            if ((int) ((Instance) enu.nextElement()).classValue() == m_classification) {
                m_correct++;
            } else {
                m_errors++;
            }
        }
        m_covers = m_correct + m_errors;
        return m_errors;
    }

    /**
     * Returns the result assigned by this rule to a given instance.
     *
     * @param inst the instance to be classified
     * @return the classification
     */
    public int resultRule(Instance inst) {

        if (m_text == null || m_text.satisfies(inst)) {
            return m_classification;
        } else {
            return -1;
        }
    }

    public void addTest(DRITest newDRITest) {
        if(m_text == null){
            m_text = newDRITest;
            return;
        }

        DRITest tempDRITest = m_text;
        while (tempDRITest.m_next != null)
            tempDRITest = tempDRITest.m_next;
        tempDRITest.m_next = newDRITest;
    }


    public static int classifyInst(Instance instance, List<DRIRule> rules) {
        for (DRIRule rule : rules) {
            if (rule.resultRule(instance) != -1) {
                return rule.m_classification;
            }
        }
        return -1;
    }

    /**
     * Returns the set of instances that are covered by this rule.
     *
     * @param data the instances to be checked
     * @return the instances covered
     */
    public Instances coveredBy(Instances data) {

        Instances result = new Instances(data, data.numInstances());
        Enumeration enu = data.enumerateInstances();
        while (enu.hasMoreElements()) {
            Instance i = (Instance) enu.nextElement();
            if (resultRule(i) != -1) {
                result.add(i);
            }
        }
        result.compactify();
        return result;
    }

    /**
     * Returns the set of instances that are not covered by this rule.
     *
     * @param data the instances to be checked
     * @return the instances not covered
     */
    public Instances notCoveredBy(Instances data) {

        Instances r = new Instances(data, data.numInstances());
        Enumeration enu = data.enumerateInstances();
        while (enu.hasMoreElements()) {
            Instance i = (Instance) enu.nextElement();
            if (resultRule(i) == -1) {
                r.add(i);
            }
        }
        r.compactify();
        return r;
    }

    public String toStr() {
        StringBuilder sb = new StringBuilder("R_" + id + "[");
        int classIndex = m_instances.classIndex();
        String label = m_instances.attribute(classIndex).value(m_classification);
        sb.append("cls=" + label + ", inst = " + m_instances.numInstances() + ",");
        sb.append("err = " + m_errors);
        if (m_text != null)
            sb.append("<" + m_text.toStr(m_instances) + ">");
        sb.append("]");

        return sb.toString();
    }



    /**
     * Prints the set of rules.
     *
     * @return a description of the rules as a string
     */
    public String toString(int maxDigits) {
        int digits = (int) (Math.ceil(Math.log10(maxDigits)));

        String pattern = "( "+ EDRIUtils.formatIntPattern(digits)+", %.2f ) ";
//        System.out.println(pattern);
        try {
            StringBuffer text = new StringBuffer();

            if (m_text != null) {
                text.append(String.format(pattern, m_correct, getConfidence()));
                text.append("If (");
                for (DRITest t = m_text; t != null; t = t.m_next) {
                    if (t.m_attr == -1) {
                        text.append("?");
                    } else {
                        text.append(m_instances.attribute(t.m_attr).name() + " = " +
                                m_instances.attribute(t.m_attr).value(t.m_val));
                    }
                    if (t.m_next != null) {
                        text.append(" , ");
                    }
                }
                text.append(") then ");
            }
            text.append(m_instances.classAttribute().value(m_classification) );
            return text.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "Can't print Prism classifier!";
        }
    }

    /**
     * Returns the revision string.
     *
     * @return the revision
     */
    public String getRevision() {
        return RevisionUtils.extract("$Revision: 5529 $");
    }

    public boolean isPerfect(int minSupport, double minConf) {
        if (m_errors == 0) return true;
        if (m_correct >= minSupport) return true;
        if (getConfidence() >= minConf && m_correct >= minSupport) return true;
//        if(m_correct >= minSupport) return  true;
        return false;
    }

    private double getConfidence() {
        return (double)m_correct/(double)m_covers;
    }

    public double getLenghtWeighted() {
        return this.m_correct * this.getLength();
    }

    private int getLength() {
        int result = 0;
        DRITest test = m_text;
        while( test  != null){
            result++;
            test = test.m_next;
        }
        return result;
    }
}
