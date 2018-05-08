package jcstombe.util;

import java.util.*;

/**
 * A doubly linked list implementation of an LRU cache of objects. Any access to an entry that results in a successful
 * response (containsKey, containsValue, get, put) moves the entry to the most recently used position. When an entry
 * is added to the cache while it is full, the entry in the tail position is deleted.
 *
 * @author Josh Stomberg <jcstombe@mtu.edu>
 * <p>
 * Last Modified: May 7, 2018
 */
public class LinkedLRUCacheMap<K, V> implements Map<K, V> {

    private final Node HEAD;
    private final Node TAIL;

    private class Node {
        private Node prev, next;
        private K key;
        private V value;

        Node() {
            this(null, null, null, null);
        }

        Node(K key, V value) {
            this(key, value, null, null);
        }

        Node(K key, V value, Node prev, Node next) {
            this.key = key;
            this.value = value;
            this.prev = prev;
            this.next = next;
        }

        Node removeNodeFromChain() {
            if (prev != null) {
                prev.next = next;
            }
            if (next != null) {
                next.prev = prev;
            }
            prev = null;
            next = null;
            return this;
        }

        @Override
        public String toString() {
            return String.format("{%s : %s}",
                    (key == null) ? "null" : key.toString(),
                    (value == null) ? "null" : value.toString());
        }
    }

    private int sizeLimit, size;

    public LinkedLRUCacheMap(int cacheSize) {
        this.sizeLimit = cacheSize;
        size = 0;
        HEAD = new Node();
        TAIL = new Node();
        HEAD.next = TAIL;
        TAIL.prev = HEAD;
    }

    /**
     * preState: (HEAD) <-> (a)
     * postState: (HEAD) <-> (node) <-> (a)
     *
     * @param node the node being inserted into the cache chain
     */
    private void addAfterHead(Node node) {
        final Node a = HEAD.next;
        node.next = a;
        node.prev = HEAD;
        a.prev = node;
        HEAD.next = node;
    }

    private void moveToFront(Node node) {
        addAfterHead(node.removeNodeFromChain());
    }

    private boolean safeEquals(Object obj1, Object obj2) {
        return (obj1 == null) ? (obj2 == null) : (obj1.equals(obj2));
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return (size == 0);
    }

    @Override
    public boolean containsKey(Object key) {
        if (key == null) return false;
        // Start after sentinel node
        Node node = HEAD.next;
        while (node != TAIL) {
            if (node.key.equals(key)) {
                moveToFront(node);
                return true;
            }
            node = node.next;
        }
        return false;
    }

    @Override
    public boolean containsValue(Object value) {
        if (value == null) return false;
        // Start after sentinel node
        Node node = HEAD.next;
        while (node != TAIL) {
            if (safeEquals(node.value, value)) {
                moveToFront(node);
                return true;
            }
            node = node.next;
        }
        return false;
    }

    @Override
    public V get(Object key) {
        if (containsKey(key)) {
            return HEAD.next.value;
        } else {
            return null;
        }
    }

    @Override
    public V put(K key, V value) {
        if (key == null) throw new NullPointerException("Null Key in LinkedLRUCacheMap");
        if (containsKey(key)) {
            Node node = HEAD.next;
            V oldValue = node.value;
            node.value = value;
            return oldValue;
        } else {
            if (size == sizeLimit) {
                TAIL.prev.removeNodeFromChain();
                size--;
            }
            addAfterHead(new Node(key, value));
            size++;
            return null;
        }
    }

    @Override
    public V remove(Object key) {
        if (containsKey(key)) {
            Node node = HEAD.next.removeNodeFromChain();
            size--;
            return node.value;
        }
        return null;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        for (Map.Entry<? extends K, ? extends V> entry : m.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void clear() {
        while (HEAD.next != TAIL) {
            HEAD.next.removeNodeFromChain();
        }
    }

    @Override
    public Set<K> keySet() {
        Set<K> keys = new LinkedHashSet<>();
        // Start after sentinel node
        Node node = HEAD.next;
        while (node != TAIL) {
            keys.add(node.key);
            node = node.next;
        }
        return keys;
    }

    @Override
    public Collection<V> values() {
        Collection<V> values = new ArrayList<>();
        // Start after sentinel node
        Node node = HEAD.next;
        while (node != TAIL) {
            values.add(node.value);
            node = node.next;
        }
        return values;
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        Set<Entry<K, V>> entries = new LinkedHashSet<>();
        // Traverse in reverse order, so last used is at the end, so using putAll retains cache behavior
        Node node = TAIL.prev;
        while (node != HEAD) {
            entries.add(new AbstractMap.SimpleEntry<>(node.key, node.value));
            node = node.prev;
        }
        return entries;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("[");
        str.append(String.format("{%d/%d)", size, sizeLimit));
        Node node = HEAD.next;
        while (node != TAIL) {
            str.append(node.toString());
            if (node.next != TAIL) str.append(", ");
            node = node.next;
        }
        str.append("]");
        return str.toString();
    }
}
