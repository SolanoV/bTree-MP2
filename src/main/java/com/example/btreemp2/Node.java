package com.example.btreemp2;

import java.util.ArrayList;

public class Node {
    ArrayList<Integer> keys;
    ArrayList<Node> children;
    boolean isLeaf;

    public Node(boolean isLeaf) {
        this.keys = new ArrayList<>();
        this.children = new ArrayList<>();
        this.isLeaf = isLeaf;
    }

}
