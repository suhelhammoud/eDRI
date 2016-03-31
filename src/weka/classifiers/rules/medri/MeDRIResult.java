package weka.classifiers.rules.medri;

import java.util.List;

/**
 * Created by suhel on 31/03/16.
 */
public class MeDRIResult {

    private long scannedInstances = 0;
    private List<IRule> rules;

    public void setScannedInstances(long scannedInstances) {
        this.scannedInstances = scannedInstances;
    }

    public long getScannedInstances() {
        return scannedInstances;
    }

    public List<IRule> getRules() {
        return rules;
    }

    public void setRules(List<IRule> rules) {
        this.rules = rules;
    }
}
