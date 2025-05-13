package bstmap;

import java.util.*;

public class BSTMap<K extends Comparable<K>, V> implements Map61B<K, V> {

    private class Node {
        K key;
        V value;
        Node left, right;
        public Node(K k, V v) {
            key = k;
            value = v;
            left = right = null;
        }
    }
    private Node root;
    private int size;
    public BSTMap() {
        root = null;
        size = 0;
    }
    public BSTMap(K k, V v) {
        root = new Node(k, v);
        size = 1;
    }

    @Override
    public void clear() {
        root = null;
        size = 0;
    }

    @Override
    public boolean containsKey(K key) {
        Node current = root;
        while (current != null) {
            int compare = key.compareTo(current.key);
            if (compare == 0) {
                return true;
            } else if (compare < 0) {
                current = current.left;
            } else {
                current = current.right;
            }
        }
        return false;
    }

    @Override
    public V get(K key) {
        Node current = root;
        while (current != null) {
            int compare = key.compareTo(current.key);
            if (compare == 0) {
                return current.value;
            } else if (compare < 0) {
                current = current.left;
            } else {
                current = current.right;
            }
        }
        return null;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void put(K key, V value) {
        if(root == null) {
            root = new Node(key, value);
            size++;
            return;
        }

        Node current = root;
        Node parent = null;
        boolean isLeft = false;

        // 查找插入位置
        while (current != null) {
            int compare = key.compareTo(current.key);
            parent = current;

            if (compare == 0) {
                current.value = value;
                return;
            } else if (compare < 0) {
                current = current.left;
                isLeft = true;
            } else {
                current = current.right;
                isLeft = false;
            }
        }
        // 插入节点
        if (isLeft) {
            parent.left = new Node(key, value);
        } else {
            parent.right = new Node(key, value);
        }
        size++;
    }

    public void printInOrder() {
        ArrayList<K> keys = new ArrayList<>();
        keys = addAllKeys(root);
        Collections.sort(keys, (k1, k2) -> k1.compareTo(k2));
        for (K key : keys) {
            System.out.print(key + " ");
        }
    }


    private ArrayList<K> addAllKeys(Node node) {
        ArrayList<K> keys = new ArrayList<>();
        if (node == null) {
            return keys;
        }
        keys.add(node.key);
        keys.addAll(addAllKeys(node.left));
        keys.addAll(addAllKeys(node.right));
        return keys;
    }

    @Override
    public Set<K> keySet() {
        Set<K> keySet = new HashSet<>();
        addKeys(root, keySet);
        return keySet;
    }

    private void addKeys(Node node, Set<K> set) {
        if (node == null) return;
        set.add(node.key);          // 添加当前节点键
        addKeys(node.left, set);    // 递归左子树
        addKeys(node.right, set);   // 递归右子树
    }

    @Override
    public V remove(K key) {
        return remove(key, null);
    }

    @Override
    public V remove(K key, V value) {
        Node parent = null;
        Node current = root;
        boolean isLeftChild = false;

        // 1. 查找要删除的节点及其父节点
        while (current != null && key.compareTo(current.key) != 0) {
            parent = current;
            if (key.compareTo(current.key) < 0) {
                current = current.left;
                isLeftChild = true;
            } else {
                current = current.right;
                isLeftChild = false;
            }
        }

        if (current == null) return null; // 未找到

        //-----------只在原来逻辑上加了下面这一行-----------------//
        if (value != null && !value.equals(current.value)) return null; // 值不匹配


        V removedValue = current.value;

        // 2. 处理双子树情况
        if (current.left != null && current.right != null) {
            // 找到右子树的最小节点及其父节点
            Node successorParent = current;
            Node successor = current.right;
            while (successor.left != null) {
                successorParent = successor;
                successor = successor.left;
            }

            // 替换当前节点的键值
            current.key = successor.key;
            current.value = successor.value;

            // 将问题转化为删除 successor 节点（此时 successor 最多有一个右子树）
            if (successorParent == current) {
                successorParent.right = successor.right;
            } else {
                successorParent.left = successor.right;
            }
            size--;
            return removedValue;
        }

        // 3. 处理单子树或叶子节点
        Node replacement = (current.left != null) ? current.left : current.right;

        // 根节点处理
        if (parent == null) {
            root = replacement;
        } else {
            if (isLeftChild) {
                parent.left = replacement;
            } else {
                parent.right = replacement;
            }
        }
        size--;
        return removedValue;
    }

    @Override
    public Iterator<K> iterator() {
        return keySet().iterator();
    }



}
