package weka.classifiers.rules;

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
public class PrismRule
        implements Serializable, RevisionHandler {

    static AtomicInteger ID = new AtomicInteger();
    final public int id;
    /**
     * for serialization
     */
    static final long serialVersionUID = 4248784350656508583L;

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
    Test m_test;

    /**
     * Number of errors made by this rule (will end up 0)
     */
    int m_errors;

    int m_covers;
    int m_correct;

    /** The next rule in the list */
//    PrismRule m_next;

    /**
     * Constructor that takes instances and the classification.
     *
     * @param data the instances
     * @param cl   the class
     * @throws Exception if something goes wrong
     */
    public PrismRule(Instances data, int cl) throws Exception {
        this.id = ID.incrementAndGet();

//        m_instances = data;//TODO no need to assign data to m_instances
        m_classification = cl;
        m_test = null;
//        m_next = null;

        m_instances = new Instances(data, 0);
//        calcNotCovered(data);
    }

    public int updateAndGetNotCovered(Instances data) {
        m_errors = 0;
        //count not covered number
        Enumeration enu = data.enumerateInstances();
        while (enu.hasMoreElements()) {
            if ((int) ((Instance) enu.nextElement()).classValue() != m_classification) {
                m_errors++;
            }
        }
        return m_errors;
    }

    /**
     * Returns the result assigned by this rule to a given instance.
     *
     * @param inst the instance to be classified
     * @return the classification
     */
    public int resultRule(Instance inst) {

        if (m_test == null || m_test.satisfies(inst)) {
            return m_classification;
        } else {
            return -1;
        }
    }

    public void addTest(Test newTest) {
        if(m_test == null){
            m_test = newTest;
            return;
        }

        Test tempTest = m_test;
        while (tempTest.m_next != null)
            tempTest = tempTest.m_next;
        tempTest.m_next = newTest;
    }

//    /**
//     * Returns the result assigned by these rules to a given instance.
//     *
//     * @param inst the instance to be classified
//     * @return the classification
//     */
//    public int resultRules(Instance inst) {
//
//        if (resultRule(inst) != -1) {
//            return m_classification;
//        } else if (m_next != null) {
//            return m_next.resultRules(inst);
//        } else {
//            return -1;
//        }
//    }

    public static int classifyInst(Instance instance, List<PrismRule> rules) {
        for (PrismRule rule : rules) {
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
        if (m_test != null)
            sb.append("<" + m_test.toStr(m_instances) + ">");
        sb.append("]");

        return sb.toString();
    }

    ;

    /**
     * Prints the set of rules.
     *
     * @return a description of the rules as a string
     */
    public String toString() {

        try {
            StringBuffer text = new StringBuffer();
            if (m_test != null) {
                text.append("If ");
                for (Test t = m_test; t != null; t = t.m_next) {
                    if (t.m_attr == -1) {
                        text.append("?");
                    } else {
                        text.append(m_instances.attribute(t.m_attr).name() + " = " +
                                m_instances.attribute(t.m_attr).value(t.m_val));
                    }
                    if (t.m_next != null) {
                        text.append("\n   and ");
                    }
                }
                text.append(" then ");
            }
            text.append(m_instances.classAttribute().value(m_classification) + "\n");
//            if (m_next != null) {
//                text.append(m_next.toString());
//            }
            return text.toString();
        } catch (Exception e) {
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
        if (getConfidence() >= minConf && m_correct >= minSupport) return true;
//        if(m_correct >= minSupport) return  true;
        return false;
    }

    private double getConfidence() {
        return (double)m_correct/(double)m_covers;
    }
}
