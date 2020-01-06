package application;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;


/**
 * Implementation of a B+ tree to allow efficient access to many different indexes of a large data
 * set. BPTree objects are created for each type of index needed by the program. BPTrees provide an
 * efficient range search as compared to other types of data structures due to the ability to
 * perform log_m N lookups and linear in-order traversals of the data items.
 * 
 * @author sapan (sapan@cs.wisc.edu) xiaoshan Li (xli842@wisc.edu)
 *
 * @param <K> key - expect a string that is the type of id for each item
 * @param <V> value - expect a user-defined type that stores all data for a food item
 */
public class BPTree<K extends Comparable<K>, V> implements BPTreeADT<K, V> {

    // Root of the tree
    private Node root;

    // Branching factor is the number of children nodes
    // for internal nodes of the tree
    private int branchingFactor;


    /**
     * Public constructor
     * 
     * @param branchingFactor
     */
    public BPTree(int branchingFactor) {
        if (branchingFactor <= 2) {
            throw new IllegalArgumentException("Illegal branching factor: " + branchingFactor);
        }
        // TODO : Complete
        this.root = new LeafNode();
        this.branchingFactor = branchingFactor;
    }

    /*
     * (non-Javadoc)
     * 
     * @see BPTreeADT#insert(java.lang.Object, java.lang.Object)
     */
    @Override
    public void insert(K key, V value) {
        root.insert(key, value);
        InternalNode result = (InternalNode) root.split();

        if (result != null) {
            // Create a new root pointing to them after the old root was split to two parts
            InternalNode _root = new InternalNode();
            _root.keys = result.keys;
            _root.children = result.children;
            root = _root;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see BPTreeADT#rangeSearch(java.lang.Object, java.lang.String)
     */
    @Override
    public List<V> rangeSearch(K key, String comparator) {
        // range search for searching
        if (!comparator.contentEquals(">=") && !comparator.contentEquals("==")
                        && !comparator.contentEquals("<=")) {
            return new ArrayList<V>();
        } else {
            return root.rangeSearch(key, comparator);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        Queue<List<Node>> queue = new LinkedList<List<Node>>();
        queue.add(Arrays.asList(root));
        StringBuilder sb = new StringBuilder();
        while (!queue.isEmpty()) {
            Queue<List<Node>> nextQueue = new LinkedList<List<Node>>();
            while (!queue.isEmpty()) {
                List<Node> nodes = queue.remove();
                sb.append('{');
                Iterator<Node> it = nodes.iterator();
                while (it.hasNext()) {
                    Node node = it.next();
                    sb.append(node.toString());
                    if (it.hasNext())
                        sb.append(", ");
                    if (node instanceof BPTree.InternalNode)
                        nextQueue.add(((InternalNode) node).children);
                }
                sb.append('}');
                if (!queue.isEmpty())
                    sb.append(", ");
                else {
                    sb.append('\n');
                }
            }
            queue = nextQueue;
        }
        return sb.toString();
    }

    /**
     * This abstract class represents any type of node in the tree This class is a super class of
     * the LeafNode and InternalNode types.
     * 
     * @author sapan
     */
    private abstract class Node {

        // List of keys
        List<K> keys;

        /**
         * Package constructor
         */
        Node() {
            keys = new ArrayList<K>();
        }

        /**
         * Inserts key and value in the appropriate leaf node and balances the tree if required by
         * splitting
         * 
         * @param key
         * @param value
         */
        abstract void insert(K key, V value);

        /**
         * Gets the first leaf key of the tree
         * 
         * @return key
         */
        abstract K getFirstLeafKey();

        /**
         * Gets the new sibling created after splitting the node
         * 
         * @return Node
         */
        abstract Node split();

        /*
         * (non-Javadoc)
         * 
         * @see BPTree#rangeSearch(java.lang.Object, java.lang.String)
         */
        abstract List<V> rangeSearch(K key, String comparator);

        /**
         * 
         * @return boolean
         */
        abstract boolean isOverflow();

        public String toString() {
            return keys.toString();
        }

    } // End of abstract class Node

    /**
     * This class represents an internal node of the tree. This class is a concrete sub class of the
     * abstract Node class and provides implementation of the operations required for internal
     * (non-leaf) nodes.
     * 
     * @author sapan
     */
    private class InternalNode extends Node {

        // List of children nodes
        List<Node> children;

        /**
         * Package constructor
         */
        InternalNode() {
            super();
            children = new ArrayList<Node>();
        }

        /**
         * (non-Javadoc)
         * 
         * @see BPTree.Node#getFirstLeafKey()
         */
        K getFirstLeafKey() {
            return children.get(0).getFirstLeafKey();
        }

        /**
         * (non-Javadoc)
         * 
         * @see BPTree.Node#isOverflow()
         */
        boolean isOverflow() {
            return (keys.size() >= branchingFactor);
        }

        /**
         * (non-Javadoc)
         * 
         * @see BPTree.Node#insert(java.lang.Comparable, java.lang.Object)
         */
        void insert(K key, V value) {
            if (children != null) {
                // find the position to insert key
                int position = keys.size();
                for (int k = 0; k < keys.size(); k++) {
                    if (key.compareTo(keys.get(k)) <= 0) {
                        position = k;
                        break;
                    }
                }
                // insert the key and value at the leaf node through a recursive way
                children.get(position).insert(key, value);
                // find whether the tree split or not
                InternalNode newNode = (InternalNode) children.get(position).split();
                // if yes, split the tree and add the key of the split node to upper level
                if (newNode != null) {
                    children.remove(position);
                    children.addAll(position, newNode.children);
                    // find the position to add key into upper level
                    int childPosition = keys.size();
                    for (int j = 0; j < keys.size(); j++) {
                        if (keys.get(j).compareTo(newNode.keys.get(0)) >= 0) {
                            childPosition = j;
                            break;
                        }
                    }
                    // add the key to upper level
                    if (keys.size() == childPosition) {
                        keys.add(newNode.keys.get(0));
                    } else {
                        keys.add(childPosition, newNode.keys.get(0));
                    }
                }
            }
        }

        /**
         * (non-Javadoc)
         * 
         * @see BPTree.Node#split()
         */
        Node split() {
            if (isOverflow()) {
                // find the split position
                int mid = branchingFactor / 2;
                if (branchingFactor % 2 == 0) {
                    mid = branchingFactor / 2 + 1;
                } else {
                    mid = (branchingFactor + 1) / 2;
                }
                // split and create a new internal node as the parent of previous nodes
                InternalNode newNode = new InternalNode();
                InternalNode sibling = new InternalNode();
                newNode.keys.add(keys.get(mid - 1));
                sibling.keys.addAll(keys.subList(0, mid - 1));
                sibling.children.addAll(children.subList(0, mid));
                for (int i = 0; i < mid; i++) {
                    this.keys.remove(0);
                    this.children.remove(0);
                }
                // add preivious nodes as children to the parent node
                newNode.children.add(sibling);
                newNode.children.add(this);
                return newNode;
            } else {
                return null;
            }
        }

        /**
         * (non-Javadoc)
         * 
         * @see BPTree.Node#rangeSearch(java.lang.Comparable, java.lang.String)
         */
        List<V> rangeSearch(K key, String comparator) {
            // recursively find the key in leaf node
            int position = keys.size();
            for (int k = 0; k < keys.size(); k++) {
                if (key.compareTo(keys.get(k)) <= 0) {
                    position = k;
                    break;
                }
            }
            return children.get(position).rangeSearch(key, comparator);
        }

    } // End of class InternalNode


    /**
     * This class represents a leaf node of the tree. This class is a concrete sub class of the
     * abstract Node class and provides implementation of the operations that required for leaf
     * nodes.
     * 
     * @author sapan
     */
    private class LeafNode extends Node {

        // List of values
        List<V> values;

        // Reference to the next leaf node
        LeafNode next;

        // Reference to the previous leaf node
        LeafNode previous;

        /**
         * Package constructor
         */
        LeafNode() {
            super();
            values = new ArrayList<V>();
            next = null;
            previous = null;
        }


        /**
         * (non-Javadoc)
         * 
         * @see BPTree.Node#getFirstLeafKey()
         */
        K getFirstLeafKey() {
            return keys.get(0);
        }

        /**
         * (non-Javadoc)
         * 
         * @see BPTree.Node#isOverflow()
         */
        boolean isOverflow() {
            return (keys.size() >= branchingFactor);
        }

        /**
         * (non-Javadoc)
         * 
         * @see BPTree.Node#insert(Comparable, Object)
         */
        void insert(K key, V value) {
            // find the position to insert key and value
            int position = keys.size();
            for (int i = 0; i < keys.size(); i++) {
                if (keys.get(i).compareTo(key) > 0) {
                    position = i;
                    break;
                } else if (keys.get(i).compareTo(key) == 0) {
                    position = i;
                    break;
                }
            }
            // add key and value
            if (keys.size() == position) {
                keys.add(key);
                values.add(value);
            } else {
                keys.add(position, key);
                values.add(position, value);
            }
        }

        /**
         * (non-Javadoc)
         * 
         * @see BPTree.Node#split()
         */
        Node split() {
            if (isOverflow()) {
                // find the split position
                int mid = branchingFactor / 2;
                if (branchingFactor % 2 == 0) {
                    mid = branchingFactor / 2 + 1;
                } else {
                    mid = (branchingFactor + 1) / 2;
                }
                // create a new internal node as the parent of previous nodes
                InternalNode newNode = new InternalNode();
                LeafNode sibling = new LeafNode();
                newNode.keys.add(keys.get(mid - 1));
                sibling.keys.addAll(keys.subList(0, mid - 1));
                sibling.values.addAll(values.subList(0, mid - 1));

                for (int i = 0; i < mid - 1; i++) {
                    this.keys.remove(0);
                    this.values.remove(0);
                }
                // add chilren to parent node
                newNode.children.add(sibling);
                newNode.children.add(this);
                sibling.previous = this.previous;
                if (this.previous != null) {
                    this.previous.next = sibling;
                }
                // set the linked list
                this.previous = sibling;
                sibling.next = this;
                return newNode;
            } else {
                return null;
            }
        }

        /**
         * (non-Javadoc)
         * 
         * @see BPTree.Node#rangeSearch(Comparable, String)
         */
        List<V> rangeSearch(K key, String comparator) {

            ArrayList<V> returnList = new ArrayList<V>();
            // if the comparator is large or equal
            if (comparator.contentEquals(">=")) {
                for (int i = 0; i < keys.size(); i++) {
                    if (keys.get(i).compareTo(key) >= 0) {
                        returnList.add(values.get(i));
                    }
                }

                // check the previous or next and the find the value with key that is larger
                // or equal to the the search key
                LeafNode current = this;
                LeafNode checkPrevious = this;
                while (current.next != null) {
                    for (int j = 0; j < current.next.keys.size(); j++) {
                        if (current.next.keys.get(j).compareTo(key) >= 0) {
                            returnList.add(current.next.values.get(j));
                        }
                    }
                    current = current.next;
                }

                while (checkPrevious.previous != null) {
                    for (int j = checkPrevious.previous.keys.size() - 1; j >= 0; j--) {
                        if (checkPrevious.previous.keys.get(j).compareTo(key) >= 0) {
                            returnList.add(0, checkPrevious.previous.values.get(j));
                        }
                    }
                    checkPrevious = checkPrevious.previous;
                }

                // if the comparator is equal
            } else if (comparator.contentEquals("==")) {

                if (keys.contains(key)) {
                    for (int i = 0; i < keys.size(); i++) {
                        if (keys.get(i).equals(key)) {
                            returnList.add(values.get(i));
                        }
                    }
                }
                // check the previous or next and the find the value with key that is
                // equal to the the search key
                LeafNode current = this;
                LeafNode checkPrevious = this;
                while (current.next != null) {
                    for (int j = 0; j < current.next.keys.size(); j++) {
                        if (current.next.keys.get(j).compareTo(key) == 0) {
                            returnList.add(current.next.values.get(j));
                        }
                    }
                    current = current.next;
                }

                while (checkPrevious.previous != null) {
                    for (int j = checkPrevious.previous.keys.size() - 1; j >= 0; j--) {
                        if (checkPrevious.previous.keys.get(j).compareTo(key) == 0) {
                            returnList.add(0, checkPrevious.previous.values.get(j));
                        }
                    }
                    checkPrevious = checkPrevious.previous;
                }

                // if the comparator is less or equal
            } else if (comparator.contentEquals("<=")) {

                for (int i = 0; i < keys.size(); i++) {
                    if (keys.get(i).compareTo(key) <= 0) {
                        returnList.add(values.get(i));
                    }
                }

                // check the previous or next and the find the value with key that is smaller
                // or equal to the the search key
                LeafNode current = this;
                LeafNode checkPrevious = this;
                while (current.next != null) {
                    for (int j = 0; j < current.next.keys.size(); j++) {
                        if (current.next.keys.get(j).compareTo(key) <= 0) {
                            returnList.add(current.next.values.get(j));
                        }
                    }
                    current = current.next;
                }

                while (checkPrevious.previous != null) {
                    for (int j = checkPrevious.previous.keys.size() - 1; j >= 0; j--) {
                        if (checkPrevious.previous.keys.get(j).compareTo(key) <= 0) {
                            returnList.add(0, checkPrevious.previous.values.get(j));
                        }
                    }
                    checkPrevious = checkPrevious.previous;
                }
            }
            return returnList;
        }

    } // End of class LeafNode


    /**
     * Contains a basic test scenario for a BPTree instance. It shows a simple example of the use of
     * this class and its related types.
     * 
     * @param args
     */
    public static void main(String[] args) {

    }

} // End of class BPTree
