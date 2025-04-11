package com.example.btreemp2;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class mpAlgo {
    Node root;
    private List<Node> searchPath = new ArrayList<>();

    public mpAlgo() {
        root = new Node(true);
    }

    public List<Node> getSearchPath() {
        return searchPath;
    }

    public void insert(String key) {
        if (root.keys.size() == 3) {
            Node newRoot = new Node(false);
            newRoot.children.add(root);
            splitChild(newRoot, 0);
            root = newRoot;
        }
        insertNonFull(root, key);
    }

    private void insertNonFull(Node node, String key) {
        int i = node.keys.size() - 1;

        if (node.isLeaf) {
            while (i >= 0 && key.compareTo(node.keys.get(i)) < 0) {
                i--;
            }
            node.keys.add(i + 1, key);

            if (node.keys.size() > 2) {
                Node parent = findParent(root, node);
                if (parent == null) {
                    Node newRoot = new Node(false);
                    newRoot.children.add(node);
                    splitChild(newRoot, 0);
                    root = newRoot;
                } else {
                    int index = parent.children.indexOf(node);
                    splitChild(parent, index);
                }
            }
        } else {
            while (i >= 0 && key.compareTo(node.keys.get(i)) < 0) {
                i--;
            }
            i++;
            Node child = node.children.get(i);

            if (child.keys.size() == 3) {
                splitChild(node, i);
                if (key.compareTo(node.keys.get(i)) > 0) {
                    i++;
                }
            }
            insertNonFull(node.children.get(i), key);
        }
    }

    private void splitChild(Node parent, int index) {
        Node child = parent.children.get(index);
        Node newNode = new Node(child.isLeaf);

        String middleKey = child.keys.get(1);
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

        while (parent.keys.size() > 2) {
            Node grandParent = findParent(root, parent);
            if (grandParent == null) {
                Node newRoot = new Node(false);
                newRoot.children.add(parent);
                splitChild(newRoot, 0);
                root = newRoot;
                break;
            } else {
                int parentIndex = grandParent.children.indexOf(parent);
                splitChild(grandParent, parentIndex);
                parent = grandParent;
            }
        }
    }

    public boolean search(String key) {
        searchPath.clear();
        return searchKey(root, key);
    }

    private boolean searchKey(Node node, String key) {
        if (node == null) return false;

        searchPath.add(node);
        int i = 0;
        while (i < node.keys.size() && key.compareTo(node.keys.get(i)) > 0) {
            i++;
        }

        if (i < node.keys.size() && key.equals(node.keys.get(i))) {
            return true;
        }

        if (node.isLeaf) {
            return false;
        }

        return searchKey(node.children.get(i), key);
    }

    public void delete(String key) {
        if (!search(key)) {
            System.out.println("Key " + key + " not found in the tree.");
            return;
        }
        deleteKey(root, key);
        if (root.keys.isEmpty() && !root.isLeaf) {
            root = root.children.get(0);
        }
    }

    private void deleteKey(Node node, String key) {
        int i = 0;
        while (i < node.keys.size() && key.compareTo(node.keys.get(i)) > 0) {
            i++;
        }

        if (i < node.keys.size() && node.keys.get(i).equals(key)) {
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
                String predKey = predNode.keys.get(predNode.keys.size() - 1);
                node.keys.set(i, predKey);
                deleteKey(node.children.get(i), predKey);
            }
        } else {
            Node child = node.children.get(i);
            if (child.keys.size() == 1 && child != root) {
                fixUnderflow(node, child);
                i = 0;
                while (i < node.keys.size() && key.compareTo(node.keys.get(i)) > 0) {
                    i++;
                }
            }
            deleteKey(node.children.get(i), key);
        }

        if (node.keys.size() > 2) {
            Node parent = findParent(root, node);
            if (parent == null) {
                Node newRoot = new Node(false);
                newRoot.children.add(node);
                splitChild(newRoot, 0);
                root = newRoot;
            } else {
                int index = parent.children.indexOf(node);
                splitChild(parent, index);
            }
        }
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
        } else if (index < parent.children.size() - 1 && parent.children.get(index + 1).keys.size() > 1) {
            Node rightSibling = parent.children.get(index + 1);
            child.keys.add(parent.keys.get(index));
            parent.keys.set(index, rightSibling.keys.remove(0));
            if (!child.isLeaf) {
                child.children.add(rightSibling.children.remove(0));
            }
        } else if (index > 0) {
            Node leftSibling = parent.children.get(index - 1);
            leftSibling.keys.add(parent.keys.remove(index - 1));
            leftSibling.keys.addAll(child.keys);
            leftSibling.children.addAll(child.children);
            parent.children.remove(index);
            if (parent != root && parent.keys.size() < 1) {
                fixUnderflow(findParent(root, parent), parent);
            }
        } else {
            Node rightSibling = parent.children.get(index + 1);
            child.keys.add(parent.keys.remove(index));
            child.keys.addAll(rightSibling.keys);
            child.children.addAll(rightSibling.children);
            parent.children.remove(index + 1);
            if (parent != root && parent.keys.size() < 1) {
                fixUnderflow(findParent(root, parent), parent);
            }
        }

        if (parent.keys.size() > 2) {
            Node grandParent = findParent(root, parent);
            if (grandParent == null) {
                Node newRoot = new Node(false);
                newRoot.children.add(parent);
                splitChild(newRoot, 0);
                root = newRoot;
            } else {
                int parentIndex = grandParent.children.indexOf(parent);
                splitChild(grandParent, parentIndex);
            }
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

    public void printTree() {
        printNode(root, 0);
    }

    private void printNode(Node node, int level) {
        if (node == null) return;

        System.out.print("Level " + level + ": ");
        for (int i = 0; i < node.keys.size(); i++) {
            System.out.print(String.format("%-4s ", node.keys.get(i)));
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
            System.out.println("1. Insert a value");
            System.out.println("2. Delete a value");
            System.out.println("3. Search for a value");
            System.out.println("4. Print tree");
            System.out.println("5. Exit");
            System.out.print("Enter your choice: ");

            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            switch (choice) {
                case 1:
                    System.out.print("Enter value to insert: ");
                    String insertKey = scanner.nextLine();
                    tree.insert(insertKey);
                    System.out.println("Inserted " + insertKey);
                    break;
                case 2:
                    System.out.print("Enter value to delete: ");
                    String deleteKey = scanner.nextLine();
                    tree.delete(deleteKey);
                    System.out.println("Deleted " + deleteKey);
                    break;
                case 3:
                    System.out.print("Enter value to search: ");
                    String searchKey = scanner.nextLine();
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