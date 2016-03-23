package weka.classifiers.rules.medri;

/**
 * Created by suhel on 23/03/16.
 */
/**
 * Helper class
 *
 * @param <K> key
 * @param <V> value
 */
public class Pair<K, V> {
    final public K key;
    final public V value;

    Pair(K key, V value) {
        this.key = key;
        this.value = value;
    }
}
