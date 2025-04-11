package com.example.btreemp2;

import java.util.ArrayList;
import java.util.List;

public class Node {
    List<String> keys;
    List<Node> children;
    boolean isLeaf;

    public Node(boolean isLeaf) {
        this.keys = new ArrayList<>();
        this.children = new ArrayList<>();
        this.isLeaf = isLeaf;
    }
}