package weka.classifiers.rules;

import weka.core.Instance;
import weka.core.Instances;
import weka.core.RevisionHandler;
import weka.core.RevisionUtils;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Class for storing a list of attribute-value tests
 */
public class Test
        implements Serializable, RevisionHandler {

    final private static AtomicInteger ID = new AtomicInteger() ;
    final public int id;

    public Test() {
        this.id = ID.incrementAndGet();
    }
    /** for serialization */
    static final long serialVersionUID = -8925333011350280799L;

    /** Attribute to test */
    int m_attr = -1;

    /** The attribute's value */
    int m_val;

    /** The next test in the rule */
    Test m_next = null;

    /**
     * Returns whether a given instance satisfies this test.
     *
     * @param inst the instance to be tested
     * @return true if the instance satisfies the test
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
        return RevisionUtils.extract("$Revision: 5529 $");
    }


    /**
     * Used by Suhel for logging purposes
     * @return
     */
    public String toStr() {
        StringBuilder sb = new StringBuilder("T_"+id);
        sb.append("(att_"+m_attr+ " = "+m_val+")");
        Test ntest = m_next;
        while (ntest != null) {
            sb.append(" -> " + ntest.toStr());
            ntest = ntest.m_next;
        }

        return  sb.toString();
    };

    public String toStr(Instances data) {
        StringBuilder sb = new StringBuilder("T_"+id);
        String at_name = data.attribute(m_attr).name();
        String at_val = data.attribute(m_attr).value(m_val);
        sb.append("(att_"+at_name+ " = "+at_val+")");
        Test ntest = m_next;
        while (ntest != null) {
            sb.append(" -> " + ntest.toStr(data));
            ntest = ntest.m_next;
        }

        return  sb.toString();
    };
}