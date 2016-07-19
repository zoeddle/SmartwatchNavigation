package com.example.carola.smartwatchnavigation;

import java.util.ArrayList;

/**
 * Created by Carola on 18.07.16.
 */
public class PathNode {
    public PathNode predecessorNode;
    public Node node;
    public float cost;
    public float f;

    public PathNode(PathNode predecessorNode, Node node, float cost, float f) {
        this.predecessorNode = predecessorNode;
        this.node = node;
        this.cost = cost;
        this.f = f;
    }
    public PathNode() {

    }
}
