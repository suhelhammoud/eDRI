package weka.classifiers.rules.edri;

import weka.core.Instance;
import weka.core.Instances;
import weka.core.RevisionHandler;
import weka.core.RevisionUtils;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Class for storing a list of attribute-value tests
 */
public class DRITest
        implements Serializable, RevisionHandler {

    final private static AtomicInteger ID = new AtomicInteger() ;
    final public int id;

    public DRITest() {
        this.id = ID.incrementAndGet();
    }
    /** for serialization */
    static final long serialVersionUID = -8925356011350280799L;

    /** Attribute to test */
    public int m_attr = -1;

    /** The attribute's value */
    public int m_val;

    /** The next test in the rule */
    public DRITest m_next = null;

    /**
     * Returns whether a given instance classify this test.
     *
     * @param inst the instance to be tested
     * @return true if the instance classify the test
     */
    boolean satisfies(Instance inst) {

        if ((int) inst.value(m_attr) == m_val) {
            if (m_next == null) {
                return true;
            } else {
                return m_next.satisfies(inst);
            }
        }
        return false;
    }

    /**
     * Returns the revision string.
     *
     * @return		the revision
     */
    public String getRevision() {
        return RevisionUtils.extract("$Revision: 001 $");
    }


    public String toStr(Instances data) {
        StringBuilder sb = new StringBuilder("T_"+id);
        String at_name = data.attribute(m_attr).name();
        String at_val = data.attribute(m_attr).value(m_val);
        sb.append("(att_"+at_name+ " = "+at_val+")");
        DRITest ntest = m_next;
        while (ntest != null) {
            sb.append(" -> " + ntest.toStr(data));
            ntest = ntest.m_next;
        }

        return  sb.toString();
    };

    public static void main(String[] args) {
        String p = EDRIUtils.formatIntPattern(300);
        System.out.printf("p");
        System.out.printf(String.format(p, 4));
    }

}