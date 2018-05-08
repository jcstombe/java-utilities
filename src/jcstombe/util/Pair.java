package jcstombe.util;

/**
 * A class providing basic immutable tuple functionality in Java
 *
 * @param <T1> Type of the first thing
 * @param <T2> Type of the second thing
 * @author Josh Stomberg <jcstombe@mtu.edu>
 * <p>
 * Last Modified: May 7, 2018
 */
public class Pair<T1, T2> {

    public final T1 first;
    public final T2 second;

    public Pair(T1 thing1, T2 thing2) {
        first = thing1;
        second = thing2;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Pair) {
            Pair other = (Pair) obj;
            return first.equals(other.first) && second.equals(other.second);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Integer.rotateLeft(first.hashCode(), 7) ^ second.hashCode();
    }
}
