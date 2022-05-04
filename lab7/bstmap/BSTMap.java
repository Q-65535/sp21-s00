package bstmap;

import java.util.Iterator;
import java.util.Set;

public class BSTMap<K extends Comparable, V> implements Map61B<K, V> {

    private int size;
    private TreeNode root;

    /**
     * Inner class to store key, value and tree information
     */
    private class TreeNode {
        K key;
        V val;
        TreeNode left;
        TreeNode right;

        public TreeNode(K key, V val) {
            this.key = key;
            this.val = val;
        }
    }

    public BSTMap() {

    }

    @Override
    public void clear() {
        size = 0;
        root = null;
    }

    @Override
    public boolean containsKey(K key) {
        return containsHelper(root, key);
    }

    private boolean containsHelper(TreeNode cur, K key) {
        if (cur == null) return false;
        if (cur.key.compareTo(key) == 0) {
            return true;
        }

        boolean isFoundInLeft = containsHelper(cur.left, key);
        if (isFoundInLeft) {
            return true;
            // if left is not found, try right
        } else {
            return containsHelper(cur.right, key);
        }
    }

    @Override
    public V get(K key) {
        return getHelper(root, key);
    }

    private V getHelper(TreeNode cur, K key) {
        if (cur == null) {
            return null;
        }
        if (cur.key.compareTo(key) == 0) {
            return cur.val;
        }

        V leftFind = getHelper(cur.left, key);
        if (leftFind != null) {
            return leftFind;
            // if left is not found, try right
        } else {
            return getHelper(cur.right, key);
        }

    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void put(K key, V value) {
        root = putHelper(root, key, value);
        size++;
    }

    private TreeNode putHelper(TreeNode cur, K key, V value) {
        if (cur == null) {
            return new TreeNode(key, value);
        }
        if (cur.key.compareTo(key) == 0) {
            cur.val = value;
            return cur;
        }

        if (cur.key.compareTo(key) > 0) {
            cur.left = putHelper(cur.left, key, value);
        } else if (cur.key.compareTo(key) < 0) {
            cur.right = putHelper(cur.right, key, value);
        }
        return cur;
    }

    @Override
    public Set keySet() {
        throw new UnsupportedOperationException("keySet operation is not supported");
    }

    @Override
    public V remove(K key) {
        throw new UnsupportedOperationException("remove operation is not supported");
    }

    @Override
    public V remove(K key, V value) {
        throw new UnsupportedOperationException("remove operation is not supported");
    }

    @Override
    public Iterator iterator() {
        throw new UnsupportedOperationException("iterator is not supported");
    }

    public void printInOrder() {
        return;
    }
}
