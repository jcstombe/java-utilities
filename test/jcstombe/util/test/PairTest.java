package jcstombe.util.test;

import jcstombe.util.Pair;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Josh Stomberg <jcstombe@mtu.edu>
 * Last Modified: May 08, 2018
 */
public class PairTest {

    @Test
    public void testEquals1() {
        Pair<String, String> thing1 = new Pair<>("Hello", "World");
        Pair<String, String> thing2 = new Pair<>("Hello", "World");
        assertEquals(thing1, thing2);
    }

    @Test
    public void testEquals2() {
        Pair<String, String> thing1 = new Pair<>("Hello", "World");
        Pair<String, String> thing2 = null;
        assertNotEquals(thing1, thing2);
    }

    @Test
    public void testEquals3() {
        Pair<String, String> thing1 = new Pair<>("Hello", "World");
        Pair<String, String> thing2 = new Pair<>("Hello", "Jim");
        assertNotEquals(thing1, thing2);
    }

    @Test
    public void testHashCode1() {
        Pair<String, String> thing1 = new Pair<>("Hello", "World");
        Pair<String, String> thing2 = new Pair<>("Hello", "World");
        assertEquals(thing1.hashCode(), thing2.hashCode());
    }

    @Test(expected = NullPointerException.class)
    public void testHashCode2() {
        Pair<String, String> thing1 = new Pair<>("Hello", "World");
        Pair<String, String> thing2 = null;
        assertNotEquals(thing1.hashCode(), thing2.hashCode());
    }

    @Test
    public void testHashCode3() {
        Pair<String, String> thing1 = new Pair<>("Hello", "World");
        Pair<String, String> thing2 = new Pair<>("Hello", "Jim");
        assertNotEquals(thing1.hashCode(), thing2.hashCode());
    }
}