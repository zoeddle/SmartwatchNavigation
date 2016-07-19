package com.example.carola.smartwatchnavigation;

import java.util.ArrayList;

/**
 * Created by Carola on 19.07.16.
 */
public class Singleton {

    private static Singleton mInstance = null;

    private ArrayList<Node> allExistingNodes;
    private Node searchNode;


    public static Singleton getInstance(){
        if(mInstance == null)
        {
            mInstance = new Singleton();
        }
        return mInstance;
    }

    public ArrayList<Node> getExistingNodes(){
        return this.allExistingNodes;
    }

    public void setExistingNodes(ArrayList<Node> value){
        allExistingNodes = value;
    }

    public Node getSearchNode() {
        return searchNode;
    }

    public void setSearchNode(Node searchNode) {
        this.searchNode = searchNode;
    }
}
