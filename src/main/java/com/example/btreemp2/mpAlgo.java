package com.example.btreemp2;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List; // Added import for List
import java.util.Queue;
import java.util.Scanner;

class mpAlgo {
    Node root;
    private List<Node> searchPath = new ArrayList<>();

    public mpAlgo() {
        root = new Node(true);
    }

    public List<Node> getSearchPath() {
        return searchPath;
    }

    public void insert(int key) {
        if (root.keys.size() == 3) {
            Node newRoot = new Node(false);
            newRoot.children.add(root);
            splitChild(newRoot, 0);
            root = newRoot;
        }
        insertNonFull(root, key);
    }

    private void insertNonFull(Node node, int key) {
        int i = node.keys.size() - 1;

        if (node.isLeaf) {
            while (i >= 0 && key < node.keys.get(i)) {
                i--;
            }
            node.keys.add(i + 1, key);

            if (node.keys.size() > 2) {
                if (node == root) {
                    Node newRoot = new Node(false);
                    newRoot.children.add(root);
                    splitChild(newRoot, 0);
                    root = newRoot;
                } else {
                    Node parent = findParent(root, node);
                    int index = parent.children.indexOf(node);
                    splitChild(parent, index);
                }
            }
        } else {
            while (i >= 0 && key < node.keys.get(i)) {
                i--;
            }
            i++;
            Node child = node.children.get(i);

            if (child.keys.size() == 3) {
                splitChild(node, i);
                if (key > node.keys.get(i)) {
                    i++;
                }
                child = node.children.get(i);
            }
            insertNonFull(child, key);
        }
    }

    private void splitChild(Node parent, int index) {
        Node child = parent.children.get(index);
        Node newNode = new Node(child.isLeaf);

        int middleKey = child.keys.get(1);
        parent.keys.add(index, middleKey);

        newNode.keys.add(child.keys.get(2));
        child.keys.remove(2);
        child.keys.remove(1);
        if (!child.isLeaf) {
            newNode.children.add(child.children.get(2));
            newNode.children.add(child.children.get(3));
            child.children.remove(3);
            child.children.remove(2);
        }

        parent.children.add(index + 1, newNode);
        if (parent.keys.size() > 2) {
            if (parent == root) {
                Node newRoot = new Node(false);
                newRoot.children.add(root);
                splitChild(newRoot, 0);
                root = newRoot;
            } else {
                Node grandParent = findParent(root, parent);
                int parentIndex = grandParent.children.indexOf(parent);
                splitChild(grandParent, parentIndex);
            }
        }
    }

    public boolean search(int key) {
        searchPath.clear();
        return searchKey(root, key);
    }

    private boolean searchKey(Node node, int key) {
        if (node == null) return false;

        searchPath.add(node);
        int i = 0;
        while (i < node.keys.size() && key > node.keys.get(i)) {
            i++;
        }

        if (i < node.keys.size() && key == node.keys.get(i)) {
            return true;
        }

        if (node.isLeaf) {
            return false;
        }

        return searchKey(node.children.get(i), key);
    }

    public void delete(int key) {
        if (!search(key)) {
            System.out.println("Key " + key + " not found in the tree.");
            return;
        }
        deleteKey(root, key);
        if (root.keys.isEmpty() && !root.isLeaf) {
            root = root.children.get(0);
        }
    }

    private void deleteKey(Node node, int key) {
        int i = 0;
        while (i < node.keys.size() && key > node.keys.get(i)) {
            i++;
        }

        if (i < node.keys.size() && node.keys.get(i) == key) {
            if (node.isLeaf) {
                node.keys.remove(i);
                if (node != root && node.keys.size() < 1) {
                    fixUnderflow(findParent(root, node), node);
                }
            } else {
                Node predNode = node.children.get(i);
                while (!predNode.isLeaf) {
                    predNode = predNode.children.get(predNode.children.size() - 1);
                }
                int predKey = predNode.keys.get(predNode.keys.size() - 1);
                node.keys.set(i, predKey);
                deleteKey(node.children.get(i), predKey);
            }
        } else {
            Node child = node.children.get(i);
            if (child.keys.size() == 1 && child != root) {
                fixUnderflow(node, child);
                i = 0;
                while (i < node.keys.size() && key > node.keys.get(i)) {
                    i++;
                }
            }
            deleteKey(node.children.get(i), key);
        }
    }

    private Node findParent(Node current, Node child) {
        if (current == null || current.isLeaf) return null;
        for (int i = 0; i < current.children.size(); i++) {
            if (current.children.get(i) == child) {
                return current;
            }
            Node found = findParent(current.children.get(i), child);
            if (found != null) return found;
        }
        return null;
    }

    private void fixUnderflow(Node parent, Node child) {
        if (parent == null) return;

        int index = parent.children.indexOf(child);

        if (index > 0 && parent.children.get(index - 1).keys.size() > 1) {
            Node leftSibling = parent.children.get(index - 1);
            child.keys.add(0, parent.keys.get(index - 1));
            parent.keys.set(index - 1, leftSibling.keys.remove(leftSibling.keys.size() - 1));
            if (!child.isLeaf) {
                child.children.add(0, leftSibling.children.remove(leftSibling.children.size() - 1));
            }
        }
        else if (index < parent.children.size() - 1 && parent.children.get(index + 1).keys.size() > 1) {
            Node rightSibling = parent.children.get(index + 1);
            child.keys.add(parent.keys.get(index));
            parent.keys.set(index, rightSibling.keys.remove(0));
            if (!child.isLeaf) {
                child.children.add(rightSibling.children.remove(0));
            }
        }
        else if (index > 0) {
            Node leftSibling = parent.children.get(index - 1);
            leftSibling.keys.add(parent.keys.remove(index - 1));
            leftSibling.keys.addAll(child.keys);
            leftSibling.children.addAll(child.children);
            parent.children.remove(index);
            if (parent != root && parent.keys.size() < 1) {
                fixUnderflow(findParent(root, parent), parent);
            }
        }
        else {
            Node rightSibling = parent.children.get(index + 1);
            child.keys.add(parent.keys.remove(index));
            child.keys.addAll(rightSibling.keys);
            child.children.addAll(rightSibling.children);
            parent.children.remove(index + 1);
            if (parent != root && parent.keys.size() < 1) {
                fixUnderflow(findParent(root, parent), parent);
            }
        }
    }

    public void printTree() {
        printNode(root, 0);
    }

    private void printNode(Node node, int level) {
        if (node == null) return;

        System.out.print("Level " + level + ": ");
        for (int i = 0; i < node.keys.size(); i++) {
            System.out.print(String.format("%04d ", node.keys.get(i)));
        }
        System.out.println();

        if (!node.isLeaf) {
            for (int i = 0; i < node.children.size(); i++) {
                printNode(node.children.get(i), level + 1);
            }
        }
    }

    public static void main(String[] args) {
        mpAlgo tree = new mpAlgo();
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("\n2-3 Tree Operations:");
            System.out.println("1. Insert a number");
            System.out.println("2. Delete a number");
            System.out.println("3. Search for a number");
            System.out.println("4. Print tree");
            System.out.println("5. Exit");
            System.out.print("Enter your choice: ");

            int choice = scanner.nextInt();

            switch (choice) {
                case 1:
                    System.out.print("Enter number to insert: ");
                    int insertKey = scanner.nextInt();
                    tree.insert(insertKey);
                    System.out.println("Inserted " + insertKey);
                    break;
                case 2:
                    System.out.print("Enter number to delete: ");
                    int deleteKey = scanner.nextInt();
                    tree.delete(deleteKey);
                    System.out.println("Deleted " + deleteKey);
                    break;
                case 3:
                    System.out.print("Enter number to search: ");
                    int searchKey = scanner.nextInt();
                    boolean found = tree.search(searchKey);
                    System.out.println(searchKey + (found ? " found in tree" : " not found in tree"));
                    break;
                case 4:
                    System.out.println("Current 2-3 Tree:");
                    tree.printTree();
                    break;
                case 5:
                    System.out.println("Exiting...");
                    scanner.close();
                    return;
                default:
                    System.out.println("Invalid choice. Try again.");
            }
        }
    }
}