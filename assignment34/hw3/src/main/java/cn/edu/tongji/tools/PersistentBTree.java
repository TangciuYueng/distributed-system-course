package cn.edu.tongji.tools;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.*;
import java.util.*;

public class PersistentBTree<K extends Comparable<K>, V> implements Serializable {

    @Data
    @AllArgsConstructor
    private static class Entry<K, V> implements Serializable {
        private K key;
        private V value;
    }
    @Data
    @AllArgsConstructor
    private static class SearchResult<V> {
        private boolean exist;
        private int index;
        private V value;

        public SearchResult(boolean exist, int index) {
            this.exist = exist;
            this.index = index;
        }
    }

    @Data
    private static class BTreeNode<K, V> implements Serializable {
        private List<Entry<K, V>> entries;
        private List<BTreeNode<K, V>> children;
        private boolean leaf;
        private Comparator<K> kComparator;

        public BTreeNode() {
            entries = new ArrayList<>();
            children = new ArrayList<>();
            leaf = false;
        }

        public BTreeNode(Comparator<K> kComparator) {
            this();
            this.kComparator = kComparator;
        }

        public int size() {
            return entries.size();
        }

        int compare(K key1, K key2) {
            return kComparator == null ? ((Comparable<K>) key1).compareTo(key2) : kComparator.compare(key1, key2);
        }

        public SearchResult<V> searchKey(K key) {
            int low = 0;
            int high = entries.size() - 1;
            int mid = 0;
            while (low <= high) {
                mid = (low + high) / 2; // 先这么写吧，BTree实现中，l+h不可能溢出
                Entry<K, V> entry = entries.get(mid);
                if (compare(entry.getKey(), key) == 0) // entrys.get(mid).getKey() == key
                    break;
                else if (compare(entry.getKey(), key) > 0) // entrys.get(mid).getKey() > key
                    high = mid - 1;
                else // entry.get(mid).getKey() < key
                    low = mid + 1;
            }
            boolean result = false;
            int index = 0;
            V value = null;
            if (low <= high) // 说明查找成功
            {
                result = true;
                index = mid; // index表示元素所在的位置
                value = entries.get(index).getValue();
            } else {
                result = false;
                index = low; // index表示元素应该插入的位置
            }
            return new SearchResult(result, index, value);
        }

        public void addEntry(Entry<K, V> entry) {
            entries.add(entry);
        }

        public Entry<K, V> removeEntry(int index) {
            return entries.remove(index);
        }

        public Entry<K, V> entryAt(int index) {
            return entries.get(index);
        }

        public V putEntry(Entry<K, V> entry) {
            SearchResult<V> result = searchKey(entry.getKey());
            if (result.isExist()) {
                V oldValue = entries.get(result.getIndex()).getValue();
                entries.get(result.getIndex()).setValue(entry.value);
                return oldValue;
            } else {
                insertEntry(entry, result.getIndex());
                return null;
            }
        }


        public boolean insertEntry(Entry<K, V> entry) {
            SearchResult<V> result = searchKey(entry.getKey());
            if (result.isExist())
                return false;
            else {
                insertEntry(entry, result.getIndex());
                return true;
            }
        }

        public void insertEntry(Entry<K, V> entry, int index) {
            List<Entry<K, V>> newEntries = new ArrayList<>();
            int i = 0;
            for (; i < index; ++i)
                newEntries.add(entries.get(i));
            newEntries.add(entry);
            for (; i < entries.size(); ++i)
                newEntries.add(entries.get(i));
            entries.clear();
            entries = newEntries;
        }

        public BTreeNode<K, V> childAt(int index) {
            if (isLeaf())
                throw new UnsupportedOperationException("Leaf node doesn't have children.");
            return children.get(index);
        }

        public void addChild(BTreeNode<K, V> child) {
            children.add(child);
        }

        public void removeChild(int index) {
            children.remove(index);
        }

        public void insertChild(BTreeNode<K, V> child, int index) {
            List<BTreeNode<K, V>> newChildren = new ArrayList<>();
            int i = 0;
            for (; i < index; ++i)
                newChildren.add(children.get(i));
            newChildren.add(child);
            for (; i < children.size(); ++i)
                newChildren.add(children.get(i));
            children = newChildren;
        }
    }

    private BTreeNode<K, V> root;
    private final int t;
    private int minKeySize;
    private int maxKeySize;
    private Comparator<K> kComparator;

    public PersistentBTree() {
        this(2); // 默认 t 值为 2
    }

    public PersistentBTree(int t) {
        this(t, null); // 调用第二个构造函数
    }

    public PersistentBTree(Comparator<K> kComparator) {
        this(2, kComparator); // 默认 t 值为 2
    }

    public PersistentBTree(Comparator<K> kComparator, int t) {
        this(t, kComparator); // 调用第二个构造函数
    }

    private PersistentBTree(int t, Comparator<K> kComparator) {
        this.t = t;
        this.kComparator = kComparator;

        root = new BTreeNode<>(kComparator);
        root.setLeaf(true);

        minKeySize = t - 1;
        maxKeySize = 2 * t - 1;
    }

    int compare(K key1, K key2) {
        return kComparator == null ? key1.compareTo(key2) : kComparator.compare(key1, key2);
    }

    public V search(K key) {
        return search(root, key);
    }
    private V search(BTreeNode<K, V> node, K key) {
        SearchResult<V> result = node.searchKey(key);
        if (result.isExist()) {
            return result.getValue();
        }
        else {
            if (node.isLeaf())
                return null;
            else
                return search(node.childAt(result.getIndex()), key);
        }
    }

    private void splitNode(BTreeNode<K, V> parentNode, BTreeNode<K, V> childNode, int index) {
        assert childNode.size() == maxKeySize;

        BTreeNode<K, V> siblingNode = new BTreeNode<>(kComparator);
        siblingNode.setLeaf(childNode.isLeaf());
        // 将满子节点中索引为[t, 2t - 2]的(t - 1)个项插入新的节点中
        for (int i = 0; i < minKeySize; ++i)
            siblingNode.addEntry(childNode.entryAt(t + i));
        // 提取满子节点中的中间项，其索引为(t - 1)
        Entry<K, V> entry = childNode.entryAt(t - 1);
        // 删除满子节点中索引为[t - 1, 2t - 2]的t个项
        for (int i = maxKeySize - 1; i >= t - 1; --i)
            childNode.removeEntry(i);
        if (!childNode.isLeaf()) // 如果满子节点不是叶节点，则还需要处理其子节点
        {
            // 将满子节点中索引为[t, 2t - 1]的t个子节点插入新的节点中
            for (int i = 0; i < minKeySize + 1; ++i)
                siblingNode.addChild(childNode.childAt(t + i));
            // 删除满子节点中索引为[t, 2t - 1]的t个子节点
            for (int i = maxKeySize; i >= t; --i)
                childNode.removeChild(i);
        }
        // 将entry插入父节点
        parentNode.insertEntry(entry, index);
        // 将新节点插入父节点
        parentNode.insertChild(siblingNode, index + 1);
    }

    private boolean insertNotFull(BTreeNode<K, V> node, Entry<K, V> entry) {
        assert node.size() < maxKeySize;

        if (node.isLeaf()) // 如果是叶子节点，直接插入
            return node.insertEntry(entry);
        else {
            SearchResult<V> result = node.searchKey(entry.getKey());
            if (result.isExist())
                return false;

            BTreeNode<K, V> childNode = node.childAt(result.getIndex());
            if (childNode.size() == maxKeySize) { // 如果子节点是满节点
                splitNode(node, childNode, result.getIndex());
                // 决定插入左边还是右边的子节点
                if (compare(entry.getKey(), node.entryAt(result.getIndex()).getKey()) > 0)
                    childNode = node.childAt(result.getIndex() + 1);
            }
            return insertNotFull(childNode, entry);
        }
    }

    public boolean insert(K key, V value) {
        if (root.size() == maxKeySize) // 如果根节点满了，则B树长高
        {
            BTreeNode<K, V> newRoot = new BTreeNode<>(kComparator);
            newRoot.setLeaf(false);
            newRoot.addChild(root);
            splitNode(newRoot, root, 0);
            root = newRoot;
        }
        return insertNotFull(root, new Entry<>(key, value));
    }

    private V putNotFull(BTreeNode<K, V> node, Entry<K, V> entry) {
        assert node.size() < maxKeySize;

        if (node.isLeaf()) // 如果是叶子节点，直接插入
            return node.putEntry(entry);
        else {
            /* 找到entry在给定节点应该插入的位置，那么entry应该插入
             * 该位置对应的子树中
             */
            SearchResult<V> result = node.searchKey(entry.getKey());
            // 如果存在，则更新
            if (result.isExist())
                return node.putEntry(entry);
            BTreeNode<K, V> childNode = node.childAt(result.getIndex());
            if (childNode.size() == 2 * t - 1) // 如果子节点是满节点
            {
                // 则先分裂
                splitNode(node, childNode, result.getIndex());
                /* 如果给定entry的键大于分裂之后新生成项的键，则需要插入该新项的右边，
                 * 否则左边。
                 */
                if (compare(entry.getKey(), node.entryAt(result.getIndex()).getKey()) > 0)
                    childNode = node.childAt(result.getIndex() + 1);
            }
            return putNotFull(childNode, entry);
        }
    }

    public V put(K key, V value) {
        if (root.size() == maxKeySize) // 如果根节点满了，则B树长高
        {
            BTreeNode<K, V> newRoot = new BTreeNode<>(kComparator);
            newRoot.setLeaf(false);
            newRoot.addChild(root);
            splitNode(newRoot, root, 0);
            root = newRoot;
        }
        return putNotFull(root, new Entry<K, V>(key, value));
    }

    public void output() {
        Queue<BTreeNode<K, V>> queue = new LinkedList<>();
        queue.offer(root);
        while (!queue.isEmpty()) {
            BTreeNode<K, V> node = queue.poll();
            for (int i = 0; i < node.size(); ++i)
                System.out.print(node.entryAt(i) + " ");
            System.out.println();
            if (!node.isLeaf()) {
                for (int i = 0; i <= node.size(); ++i)
                    queue.offer(node.childAt(i));
            }
        }
    }

    public void saveToFile(String fileName) throws IOException {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(fileName))) {
            out.writeObject(this);
        }
    }

    public static <K extends Comparable<K>, V> PersistentBTree<K, V> loadFromFile(String fileName) throws IOException, ClassNotFoundException {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(fileName))) {
            return (PersistentBTree<K, V>) in.readObject();
        }
    }

    public static void main(String[] args) {
        Random random = new Random();
        PersistentBTree<Integer, Integer> btree = new PersistentBTree<>(3);
        List<Integer> save = new ArrayList<>();
        for (int i = 0; i < 10; ++i) {
            int r = random.nextInt(100);
            save.add(r);
            System.out.print(r + "  ");
            btree.insert(r, r);
        }
        System.out.println();

        System.out.println("----------------------");
        btree.output();

        String fileName = "btree_data.ser";
        try {
            btree.saveToFile(fileName);
            System.out.println("BTree saved to file: " + fileName);
        } catch (IOException e) {
            System.err.println("Failed to save BTree to file: " + e.getMessage());
        }

        // 从文件加载B树对象
        try {
            PersistentBTree<Integer, String> loadedBTree = PersistentBTree.loadFromFile(fileName);
            System.out.println("BTree loaded from file:");
            loadedBTree.output(); // 输出加载后的B树内容
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Failed to load BTree from file: " + e.getMessage());
        }
    }
}
