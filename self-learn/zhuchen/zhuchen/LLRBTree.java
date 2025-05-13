package zhuchen;

public class LLRBTree {
    private static final boolean RED = true;
    private static final boolean BLACK = false;
    private Node root;

    private class Node {
        int key;
        Node left, right;
        boolean color;

        Node(int key) {
            this.key = key;
            this.color = RED; // 新节点默认红色
        }
    }

    // 判断节点颜色
    private boolean isRed(Node node) {
        return node != null && node.color == RED;
    }

    // 左旋
    private Node rotateLeft(Node h) {
        Node x = h.right;

        h.right = x.left;
        x.left = h;
        x.color = h.color;
        h.color = RED;
        return x;
    }

    // 右旋
    private Node rotateRight(Node h) {
        Node x = h.left;
        h.left = x.right;
        x.right = h;
        x.color = h.color;
        h.color = RED;
        return x;
    }

    // 颜色翻转
    private void flipColors(Node h) {
        h.color = !h.color;
        h.left.color = !h.left.color;
        h.right.color = !h.right.color;
    }

    // 插入操作
    public void insert(int key) {
        root = insert(root, key);
        root.color = BLACK; // 根节点始终黑色
    }

    private Node insert(Node h, int key) {
        if (h == null) return new Node(key);

        // 标准BST插入
        if (key < h.key) h.left = insert(h.left, key);
        else if (key > h.key) h.right = insert(h.right, key);

        // 左倾修复
        if (isRed(h.right) && !isRed(h.left)) h = rotateLeft(h);
        if (isRed(h.left) && isRed(h.left.left)) h = rotateRight(h);
        if (isRed(h.left) && isRed(h.right)) flipColors(h);

        return h;
    }

    // 其他方法（delete、search等）...
}