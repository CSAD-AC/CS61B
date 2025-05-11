package zhuchen;

public class BST {
    // 内部节点类
    private static class Node {
        int num;
        Node left, right;
        public Node(int num) {
            this.num = num;
        }
    }

    private Node root;

    public BST() {
        this.root = null;
    }

    public BST(int num) {
        this.root = new Node(num);
    }

    public void insert(int x) {
        root = insert(x, root);
    }

    private Node insert(int x, Node node) {
        if (node == null) {
            return new Node(x);
        }

        if (x < node.num) {
            node.left = insert(x, node.left);
        } else if (x > node.num) {
            node.right = insert(x, node.right);
        }
        // 如果x == node.num，不做任何操作（不允许重复值）

        return node;
    }

    public Node getRoot() {
        return root;
    }

    public int height() {
        return height(root);
    }

    private int height(Node node) {
        if (node == null) return 0;
        return 1 + Math.max(height(node.left), height(node.right));
    }

    public int size() {
        return size(root);
    }

    private int size(Node node) {
        if (node == null) return 0;
        return 1 + size(node.left) + size(node.right);
    }

    public boolean isEmpty() {
        return root == null;
    }

    public Node delete(int x) {
        root = delete(x, root);
        return root;
    }

    private Node delete(int x, Node node) {
        if (node == null) return null;

        if (x < node.num) {
            node.left = delete(x, node.left);
        } else if (x > node.num) {
            node.right = delete(x, node.right);
        } else {
            if (node.left == null) {
                return node.right;
            } else if (node.right == null) {
                return node.left;
            } else {
                Node minNode = findMin(node.right);
                node.num = minNode.num;
                node.right = delete(minNode.num, node.right);
            }
        }
        return node;
    }

    private Node findMin(Node node) {
        while (node.left != null) {
            node = node.left;
        }
        return node;
    }

    public Node find(int x) {
        return find(x, root);
    }

    private Node find(int x, Node node) {
        if (node == null) return null;

        if (x < node.num) {
            return find(x, node.left);
        } else if (x > node.num) {
            return find(x, node.right);
        } else {
            return node;
        }
    }

    public void print() {
        print(root, "", true);
    }

    private void print(Node node, String prefix, boolean isLeft) {
        if (node != null) {
            System.out.println(prefix + (isLeft ? "├── " : "└── ") + node.num);
            // 递归打印左右子树
            print(node.left, prefix + (isLeft ? "│   " : "    "), true);
            print(node.right, prefix + (isLeft ? "│   " : "    "), false);
        }
    }
}