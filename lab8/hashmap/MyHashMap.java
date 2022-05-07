package hashmap;

import java.util.*;

/**
 *  A hash table-backed Map implementation. Provides amortized constant time
 *  access to elements via get(), remove(), and put() in the best case.
 *
 *  Assumes null keys will never be inserted, and does not resize down upon remove().
 *  @author YOUR NAME HERE
 */
public class MyHashMap<K, V> implements Map61B<K, V> {

    /**
     * Protected helper class to store key/value pairs
     * The protected qualifier allows subclass access
     */
    protected class Node {
        K key;
        V value;

        Node(K k, V v) {
            key = k;
            value = v;
        }
    }

    /* Instance Variables */
    private Collection<Node>[] buckets;
    /**
     * The initial size of the HashMap is 0
     */
    private HashSet<K> keySet = new HashSet<>();
    private int size = 0;
    /**
     * The initial size of this HashMap
     */
    private int initialSize = 16;
    private double loadFactor = 0.75;
    // You should probably define some more!

    /** Constructors */
    public MyHashMap() {
        buckets = new Collection[this.initialSize];
    }

    public MyHashMap(int initialSize) {
        this.initialSize = initialSize;
        buckets = new Collection[this.initialSize];
    }

    /**
     * MyHashMap constructor that creates a backing array of initialSize.
     * The load factor (# items / # buckets) should always be <= loadFactor
     *
     * @param initialSize initial size of backing array
     * @param maxLoad maximum load factor
     */
    public MyHashMap(int initialSize, double maxLoad) {
        this.initialSize = initialSize;
        this.loadFactor = maxLoad;
        buckets = new Collection[this.initialSize];
    }

    /**
     * Returns a new node to be placed in a hash table bucket
     */
    private Node createNode(K key, V value) {
        return null;
    }

    /**
     * Returns a data structure to be a hash table bucket
     *
     * The only requirements of a hash table bucket are that we can:
     *  1. Insert items (`add` method)
     *  2. Remove items (`remove` method)
     *  3. Iterate through items (`iterator` method)
     *
     * Each of these methods is supported by java.util.Collection,
     * Most data structures in Java inherit from Collection, so we
     * can use almost any data structure as our buckets.
     *
     * Override this method to use different data structures as
     * the underlying bucket type
     *
     * BE SURE TO CALL THIS FACTORY METHOD INSTEAD OF CREATING YOUR
     * OWN BUCKET DATA STRUCTURES WITH THE NEW OPERATOR!
     */
    protected Collection<Node> createBucket() {
        return new ArrayList<>();
    }

    /**
     * Returns a table to back our hash table. As per the comment
     * above, this table can be an array of Collection objects
     *
     * BE SURE TO CALL THIS FACTORY METHOD WHEN CREATING A TABLE SO
     * THAT ALL BUCKET TYPES ARE OF JAVA.UTIL.COLLECTION
     *
     * @param tableSize the size of the table to create
     */
    private Collection<Node>[] createTable(int tableSize) {
        return null;
    }

    // TODO: Implement the methods of the Map61B Interface below
    // Your code won't compile until you do so!

    @Override
    public void clear() {
        buckets = new Collection[initialSize];
        keySet.clear();
        size = 0;
    }

    @Override
    public boolean containsKey(K key) {
        return getNode(key) != null;
    }

    @Override
    public V get(K key) {
        Node node = getNode(key);
        if (node == null) {
            return null;
        } else {
            return node.value;
        }
    }

    private Node getNode(K key) {
        int position = getPosition(key);
        // if there is no bucket in the position, return false
        if (buckets[position] == null) {
            return null;
        }
        Collection<Node> bucket = buckets[position];
        Node nodeInBucket = getNodeInBucket(key, bucket);
        return nodeInBucket;
    }

    /**
     * Find the node according to the key in a given bucket
     * @param key
     * @param bucket
     * @return
     */
    private Node getNodeInBucket(K key, Collection<Node> bucket) {
        // serach for the target node
        for (Node node : bucket) {
            if (node.key.equals(key)) {
                return node;
            }
        }
        // if we can't find the target node in the bucket, return null
        return null;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void put (K key, V value) {
        if (isExceedLoadFactor()) {
            resize();
        }
        if (getNode(key) == null) {
            keySet.add(key);
            size++;
        }
        put(this.buckets, key, value);
    }

    /**
     * Put the key value pair into the given buckets
     */
    private void put(Collection[] buckets, K key, V value) {

        int position = getPosition(key);
        // if the position has no bucket, create a new one and add the node
        if (buckets[position] == null) {
            buckets[position] = createBucket();
            buckets[position].add(new Node(key, value));
            return;
        } else {
            Node nodeInBucket = getNodeInBucket(key, buckets[position]);
            // if the bucket doesn't contain the node identified by key, add new node
            if (nodeInBucket == null) {
                buckets[position].add(new Node(key, value));
            // otherwise, change the value
            } else {
                nodeInBucket.value = value;
            }
        }

    }

    /**
     * Resize the buckets
     */
    private void resize() {
        // create new buckets
        int preLen = buckets.length;
        Collection[] newBuckets = new Collection[preLen * 2];
        // put all elements in the current buckets to the new buckets
        for (K k : keySet) {
            put(newBuckets, k, get(k));
        }
        this.buckets = newBuckets;
    }

    @Override
    public Set<K> keySet() {
        return keySet;
    }

    @Override
    public V remove(K key) {
        throw new UnsupportedOperationException("remove operation is not supported!");
    }

    @Override
    public V remove(K key, V value) {
        throw new UnsupportedOperationException("remove operation is not supported!");
    }

    @Override
    public Iterator<K> iterator() {
        // just return the iterator of the keySet
        return keySet.iterator();
    }

    /**
     * Get the position based on the key
     * @param key
     * @return
     */
    private int getPosition(K key) {
        return Math.abs(key.hashCode()) % initialSize;
    }

    /**
     * Evaluate whether current load factor exceeds the maximum load factor
     * @return
     */
    private boolean isExceedLoadFactor() {
        double curFactor = (double) size / buckets.length;
        return curFactor > loadFactor;
    }

}
