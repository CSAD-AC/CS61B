package hashmap;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 *  A hash table-backed Map implementation. Provides amortized constant time
 *  access to elements via get(), remove(), and put() in the best case.
 *
 *  Assumes null keys will never be inserted, and does not resize down upon remove().
 *  @author YOUR NAME HERE
 */
public class MyHashMap<K, V> implements Map61B<K, V> {

    @Override
    public void clear() {
        buckets = createTable(buckets.length);
        size = 0;
    }

    @Override
    public boolean containsKey(K key) {
        return getNode(key) != null;
    }

    @Override
    public V get(K key) {
        Node node = getNode(key);
        return node == null ? null : node.value;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void put(K key, V value) {
        int index = hash(key);
        Collection<Node> bucket = buckets[index];

        // 检查键是否已存在
        for (Node node : bucket) {
            if (node.key.equals(key)) {
                // 更新现有键的值
                node.value = value;
                return;
            }
        }

        // 添加新键值对
        bucket.add(createNode(key, value));
        size++;

        // 检查是否需要扩容
        if ((double) size / buckets.length > loadFactor) {
            resize();
        }
    }

    // 扩容方法
    private void resize() {
        Collection<Node>[] oldBuckets = buckets;
        // 桶数量翻倍
        buckets = createTable(oldBuckets.length * 2);
        int oldSize = size;
        size = 0;

        // 重新哈希所有节点
        for (Collection<Node> bucket : oldBuckets) {
            for (Node node : bucket) {
                put(node.key, node.value);
            }
        }
        size = oldSize;
    }

    @Override
    public Set<K> keySet() {
        Set<K> keys = new HashSet<>();
        for (Collection<Node> bucket : buckets) {
            for (Node node : bucket) {
                keys.add(node.key);
            }
        }
        return keys.isEmpty() ? null : keys;
    }

    @Override
    public V remove(K key) {
        int index = hash(key);
        Collection<Node> bucket = buckets[index];

        // 查找并移除节点
        Iterator<Node> iterator = bucket.iterator();
        while (iterator.hasNext()) {
            Node node = iterator.next();
            if (node.key.equals(key)) {
                iterator.remove();
                size--;
                return node.value;
            }
        }
        return null;
    }

    @Override
    public V remove(K key, V value) {
        int index = hash(key);
        Collection<Node> bucket = buckets[index];

        // 查找并移除匹配键和值的节点
        Iterator<Node> iterator = bucket.iterator();
        while (iterator.hasNext()) {
            Node node = iterator.next();
            if (node.key.equals(key) && node.value.equals(value)) {
                iterator.remove();
                size--;
                return node.value;
            }
        }
        return null;
    }

    @Override
    public Iterator<K> iterator() {
        return new MyHashMapIterator();
    }

    // 内部迭代器类
    private class MyHashMapIterator implements Iterator<K> {
        private int bucketIndex;
        private Iterator<Node> currentBucketIterator;
        private int visitedCount;

        public MyHashMapIterator() {
            bucketIndex = 0;
            visitedCount = 0;
            // 找到第一个非空桶的迭代器
            findNextNonEmptyBucket();
        }

        private void findNextNonEmptyBucket() {
            while (bucketIndex < buckets.length) {
                if (!buckets[bucketIndex].isEmpty()) {
                    currentBucketIterator = buckets[bucketIndex].iterator();
                    return;
                }
                bucketIndex++;
            }
            currentBucketIterator = null;
        }

        @Override
        public boolean hasNext() {
            return visitedCount < size;
        }

        @Override
        public K next() {
            if (!hasNext()) {
                throw new java.util.NoSuchElementException();
            }

            // 获取当前元素
            Node currentNode = currentBucketIterator.next();
            K result = currentNode.key;
            visitedCount++;

            // 如果当前桶没有更多元素，寻找下一个非空桶
            if (!currentBucketIterator.hasNext()) {
                bucketIndex++;
                findNextNonEmptyBucket();
            }

            return result;
        }
    }

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
    private int size;
    private double loadFactor;

    /** Constructors */
    public MyHashMap() {
        this(16, 0.75);
    }

    public MyHashMap(int initialSize) {
        this(initialSize, 0.75);;
    }

    /**
     * MyHashMap constructor that creates a backing array of initialSize.
     * The load factor (# items / # buckets) should always be <= loadFactor
     *
     * @param initialSize initial size of backing array
     * @param maxLoad maximum load factor
     */
    public MyHashMap(int initialSize, double maxLoad) {
        // 创建指定大小的表
        buckets = new Collection[initialSize];
        // 为每个位置创建bucket
        for (int i = 0; i < initialSize; i++) {
            buckets[i] = createBucket();
        }
        // 初始化大小为0
        this.size = 0;
        // 设置最大负载因子
        this.loadFactor = maxLoad;
    }

    /**
     * Returns a new node to be placed in a hash table bucket
     */
    private Node createNode(K key, V value) {
        // 创建新的 Node 实例
        return new Node(key, value);
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
        return new java.util.LinkedList<>();
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
        // 创建指定大小的 Collection 数组
        Collection<Node>[] table = new Collection[tableSize];
        for (int i = 0; i < tableSize; i++) {
            table[i] = createBucket();
        }
        return table;
    }

    // TODO: Implement the methods of the Map61B Interface below
    // Your code won't compile until you do so!
    // 计算键的索引位置
    private int hash(K key) {
        // 处理负的哈希值
        return (key.hashCode() & 0x7fffffff) % buckets.length;
    }

    // 查找指定键对应的节点
    private Node getNode(K key) {
        int index = hash(key);
        Collection<Node> bucket = buckets[index];

        // 遍历桶中的所有节点，查找匹配的键
        for (Node node : bucket) {
            if (node.key.equals(key)) {
                return node;
            }
        }
        return null;
    }
}
