package zhuchen;

public class BTree {
    private static final int M = 2;         // B-树的阶数
    private Node root;                      // 根节点

    // 内部节点类
    private static class Node {
        int num;                            // 当前键的数量
        int[] keys = new int[2 * M - 1];    // 键数组
        Node[] children = new Node[2 * M];  // 子节点数组
        boolean isLeaf;                     // 是否为叶子节点

        Node(boolean isLeaf) {
            this.isLeaf = isLeaf;
        }
    }

    public BTree() {
        root = new Node(true);
    }

    // 公开删除接口
    public void delete(int key) {
        delete(root, key);

        // 根节点可能变为空
        if (root.num == 0 && !root.isLeaf) {
            root = root.children[0];
        }
    }

    // 递归删除核心方法
    private void delete(Node node, int key) {
        int idx = findKeyIndex(node, key);

        // Case 1: 键在当前节点中
        if (idx < node.num && node.keys[idx] == key) {
            if (node.isLeaf) {
                removeFromLeaf(node, idx);
            } else {
                removeFromInternal(node, idx);
            }
        } else {
            // Case 2: 键在子树中
            if (node.isLeaf) {
                return; // 键不存在
            }

            // 确保子节点有足够键
            if (node.children[idx].num < M) {
                fillChild(node, idx);
            }

            // 递归删除
            if (idx > node.num) {
                delete(node.children[idx-1], key);
            } else {
                delete(node.children[idx], key);
            }
        }
    }

    // 从叶子节点删除
    private void removeFromLeaf(Node node, int idx) {
        for (int i = idx+1; i < node.num; ++i) {
            node.keys[i-1] = node.keys[i];
        }
        node.num--;
    }

    // 从内部节点删除
    private void removeFromInternal(Node node, int idx) {
        int key = node.keys[idx];

        // Case 3a: 左子节点有足够键
        if (node.children[idx].num >= M) {
            Node pred = getPredecessor(node.children[idx]);
            node.keys[idx] = pred.keys[pred.num-1];
            delete(node.children[idx], node.keys[idx]);
        }
        // Case 3b: 右子节点有足够键
        else if (node.children[idx+1].num >= M) {
            Node succ = getSuccessor(node.children[idx+1]);
            node.keys[idx] = succ.keys[0];
            delete(node.children[idx+1], node.keys[idx]);
        }
        // Case 3c: 合并子节点
        else {
            mergeNodes(node, idx);
            delete(node.children[idx], key);
        }
    }

    // 获取前驱节点（左子树最大值）
    private Node getPredecessor(Node node) {
        while (!node.isLeaf) {
            node = node.children[node.num];
        }
        return node;
    }

    // 获取后继节点（右子树最小值）
    private Node getSuccessor(Node node) {
        while (!node.isLeaf) {
            node = node.children[0];
        }
        return node;
    }

    // 合并节点操作
    private void mergeNodes(Node parent, int idx) {
        Node child = parent.children[idx];
        Node sibling = parent.children[idx+1];

        // 将父节点键下移
        child.keys[child.num] = parent.keys[idx];

        // 合并兄弟节点键
        for (int i = 0; i < sibling.num; ++i) {
            child.keys[child.num+1+i] = sibling.keys[i];
        }

        // 合并子节点指针（若非叶子）
        if (!child.isLeaf) {
            for (int i = 0; i <= sibling.num; ++i) {
                child.children[child.num+1+i] = sibling.children[i];
            }
        }

        // 调整父节点键
        for (int i = idx+1; i < parent.num; ++i) {
            parent.keys[i-1] = parent.keys[i];
        }

        // 调整父节点子指针
        for (int i = idx+2; i <= parent.num; ++i) {
            parent.children[i-1] = parent.children[i];
        }

        child.num += sibling.num + 1;
        parent.num--;
    }

    // 填充不足的子节点
    private void fillChild(Node parent, int idx) {
        // Case 2a: 向左兄弟借键
        if (idx != 0 && parent.children[idx-1].num >= M) {
            borrowFromLeft(parent, idx);
        }
        // Case 2b: 向右兄弟借键
        else if (idx != parent.num && parent.children[idx+1].num >= M) {
            borrowFromRight(parent, idx);
        }
        // Case 2c: 合并兄弟节点
        else {
            if (idx != parent.num) {
                mergeNodes(parent, idx);
            } else {
                mergeNodes(parent, idx-1);
            }
        }
    }

    // 从左兄弟借键
    private void borrowFromLeft(Node parent, int idx) {
        Node child = parent.children[idx];
        Node leftSibling = parent.children[idx-1];

        // 右移child的键
        for (int i = child.num-1; i >= 0; --i) {
            child.keys[i+1] = child.keys[i];
        }

        // 父节点键下移
        child.keys[0] = parent.keys[idx-1];

        // 移动左兄弟最后一个子指针
        if (!child.isLeaf) {
            for (int i = child.num; i >= 0; --i) {
                child.children[i+1] = child.children[i];
            }
            child.children[0] = leftSibling.children[leftSibling.num];
        }

        // 更新父节点键
        parent.keys[idx-1] = leftSibling.keys[leftSibling.num-1];

        child.num++;
        leftSibling.num--;
    }

    // 从右兄弟借键
    private void borrowFromRight(Node parent, int idx) {
        Node child = parent.children[idx];
        Node rightSibling = parent.children[idx+1];

        // 父节点键下移
        child.keys[child.num] = parent.keys[idx];

        // 移动右兄弟第一个子指针
        if (!child.isLeaf) {
            child.children[child.num+1] = rightSibling.children[0];
        }

        // 更新父节点键
        parent.keys[idx] = rightSibling.keys[0];

        // 左移右兄弟的键
        for (int i = 1; i < rightSibling.num; ++i) {
            rightSibling.keys[i-1] = rightSibling.keys[i];
        }

        // 左移右兄弟的子指针
        if (!rightSibling.isLeaf) {
            for (int i = 1; i <= rightSibling.num; ++i) {
                rightSibling.children[i-1] = rightSibling.children[i];
            }
        }

        child.num++;
        rightSibling.num--;
    }

    // 辅助方法：查找键位置
    private int findKeyIndex(Node node, int key) {
        int idx = 0;
        while (idx < node.num && key > node.keys[idx]) {
            idx++;
        }
        return idx;
    }

    // 打印方法（用于测试验证）
    public void print() {
        print(root, 0);
    }

    private void print(Node node, int level) {
        System.out.print("Level " + level + " ");
        for (int i = 0; i < node.num; i++) {
            System.out.print(node.keys[i] + " ");
        }
        System.out.println();

        if (!node.isLeaf) {
            for (int i = 0; i <= node.num; i++) {
                if (node.children[i] != null) {
                    print(node.children[i], level+1);
                }
            }
        }
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

    public static void main(String[] args) {
        BTree tree = new BTree();
        int[] keys = {10, 20, 5, 6, 12, 30, 7, 17};

        // 插入测试数据
        for (int key : keys) {
            tree.insert(key);
        }

        System.out.println("Original tree:");
        tree.print();

        // 删除测试
        tree.delete(6);
        System.out.println("\nAfter deleting 6:");
        tree.print();
    }
}