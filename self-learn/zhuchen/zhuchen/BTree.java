package zhuchen;

public class BTree {
    private static final int M = 2;         // B-树的阶数
    private Node root;                      // 根节点

    // 内部节点类
    private static class Node {
        int num;                            // 当前键的数量
        int[] keys = new int[2 * M - 1];    // 键
        Node[] children = new Node[2 * M];  // 子节点
        boolean isLeaf;                     // 是否为叶子节点

        Node(boolean isLeaf) {
            this.isLeaf = isLeaf;
        }
    }

    public BTree() {
        root = new Node(true); // 初始为空树
    }

    public void insert(int key) {
        // 处理根节点分裂
        if (root.num == 2 * M - 1) {
            Node newRoot = new Node(false);
            newRoot.children[0] = root;
            splitChild(newRoot, 0);
            root = newRoot; // 更新根节点
        }
        insertNonFull(root, key);
    }

    // 插入辅助方法（节点未满时）
    private void insertNonFull(Node node, int key) {
        int i = node.num - 1;

        if (node.isLeaf) {
            // 叶子节点直接插入
            while (i >= 0 && key < node.keys[i]) {
                node.keys[i + 1] = node.keys[i];
                i--;
            }
            node.keys[i + 1] = key;
            node.num++;
        } else {
            // 找到子节点位置
            while (i >= 0 && key < node.keys[i]) {
                i--;
            }
            i++;

            // 检查子节点是否需要分裂
            if (node.children[i].num == 2 * M - 1) {
                splitChild(node, i);
                if (key > node.keys[i]) {
                    i++;
                }
            }
            insertNonFull(node.children[i], key);
        }
    }

    // 分裂子节点
    private void splitChild(Node parent, int childIndex) {
        // child为分裂后的左子树,  newChild为分裂后的右子树
        Node child = parent.children[childIndex];
        Node newChild = new Node(child.isLeaf);
        newChild.num = M - 1;

        // 复制后半部分键
        for (int j = 0; j < M - 1; j++) {
            newChild.keys[j] = child.keys[j + M];
        }

        // 复制后半部分子节点（若非叶子）
        if (!child.isLeaf) {
            for (int j = 0; j < M; j++) {
                newChild.children[j] = child.children[j + M];
            }
        }
        child.num = M - 1;

        // 调整父节点的键和子节点指针
        for (int j = parent.num; j > childIndex; j--) {
            parent.keys[j] = parent.keys[j - 1];
        }
        parent.keys[childIndex] = child.keys[M - 1];
        parent.num++;

        for (int j = parent.num; j > childIndex + 1; j--) {
            parent.children[j] = parent.children[j - 1];
        }
        parent.children[childIndex + 1] = newChild;
    }

    // 打印树结构（测试用）
    public void print() {
        print(root, 0);
    }

    private void print(Node node, int indent) {
        for (int i = 0; i < indent; i++) System.out.print("  ");
        System.out.print("[");
        for (int i = 0; i < node.num; i++) {
            if (i > 0) System.out.print(", ");
            System.out.print(node.keys[i]);
        }
        System.out.println("]");

        if (!node.isLeaf) {
            for (int i = 0; i <= node.num; i++) {
                if (node.children[i] != null) {
                    print(node.children[i], indent + 1);
                }
            }
        }
    }

    public static void main(String[] args) {
        BTree tree = new BTree();
        int[] keys = {10, 20, 5, 6, 12, 30, 7, 17};

        for (int key : keys) {
            System.out.println("Insert " + key);
            tree.insert(key);
            tree.print();
            System.out.println("-----------------");
        }
    }
}