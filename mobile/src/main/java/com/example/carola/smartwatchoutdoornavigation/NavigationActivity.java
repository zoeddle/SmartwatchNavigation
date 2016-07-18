package com.example.carola.smartwatchoutdoornavigation;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class NavigationActivity extends AppCompatActivity {

    private ImageView image;
    private Bitmap mutableBitmap;
    private Canvas canvas;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

        image = (ImageView) findViewById(R.id.i_floorPlan);
        Bitmap bitmap = ((BitmapDrawable) image.getDrawable()).getBitmap();

        mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);

        canvas = new Canvas(mutableBitmap);

        ArrayList<Node> existingNodes = (ArrayList<Node>) getIntent().getSerializableExtra("allExistingNodes");

        if(existingNodes != null){
        for(int i = 0; i<existingNodes.size(); i++){
                    drawNode(existingNodes.get(i).x, existingNodes.get(i).y);
                }
            aStar(existingNodes,existingNodes.get(0), existingNodes.get(7));
        }

        drawLine(existingNodes.get(0), existingNodes.get(1));
        
    }

    private void drawNode(float x, float y) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        paint.setColor(Color.BLUE);
        paint.setStyle(Paint.Style.FILL);

        canvas.drawCircle(x, y, 25, paint);

        image.setImageBitmap(mutableBitmap);
    }

    private void drawLine(Node start, Node end){

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        paint.setColor(Color.GREEN);
        paint.setStrokeWidth(10);

        float startx = start.x;
        float starty = start.y;
        float endx = end.x;
        float endy = end.y;

        canvas.drawLine(startx, starty, endx, endy, paint);

        image.setImageBitmap(mutableBitmap);
    }

    private ArrayList<Node> aStar(ArrayList<Node> nodeList, Node start, Node end){
        ArrayList<Node> openList = new ArrayList<>();
        ArrayList<Node> closedList = new ArrayList<>();
        ArrayList<Node> path = new ArrayList<>();

        Node currentNode = start;


        // Initialisierung der Open List, die Closed List ist noch leer
        // (die Priorität bzw. der f Wert des Startknotens ist unerheblich)

        openList.add(start);

        // diese Schleife wird durchlaufen bis entweder
        // - die optimale Lösung gefunden wurde oder
        // - feststeht, dass keine Lösung existiert

        while(!openList.isEmpty()){

        // Knoten mit dem geringsten f Wert aus der Open List entfernen

        Node nodeWithMinimalF = findNodeWithMinimalF(currentNode, end, nodeList);
            currentNode = nodeWithMinimalF;
        openList.remove(nodeWithMinimalF);

        // Wurde das Ziel gefunden?
        if (currentNode.equals(end)){
        return path;
        }

        // Der aktuelle Knoten soll durch nachfolgende Funktionen
        // nicht weiter untersucht werden damit keine Zyklen entstehen

            closedList.add(currentNode);

        // Wenn das Ziel noch nicht gefunden wurde: Nachfolgeknoten
        // des aktuellen Knotens auf die Open List setzen

        expandNode(currentNode);
        }

        // die Open List ist leer, es existiert kein Pfad zum Ziel
        return null;
    }

    private Node findNodeWithMinimalF(Node currentNode, Node endNode, ArrayList<Node> nodeList) {
        Node nodeWithMinF = null;
        ArrayList<Node> neighbourList = new ArrayList<>();
        for (int i =0; i<currentNode.neighbours.size();i++){
            for (int j = 0; j<nodeList.size();j++){
                if(nodeList.get(j).name.equals(currentNode.neighbours.get(i))){
                    double cost = Math.sqrt((currentNode.x-nodeList.get(j).x) * (currentNode.x-nodeList.get(j).x) + (currentNode.y-nodeList.get(j).y) * (currentNode.y-nodeList.get(j).y));
                    double costEnd = Math.sqrt((endNode.x-nodeList.get(j).x) * (endNode.x-nodeList.get(j).x) + (endNode.y-nodeList.get(j).y) * (endNode.y-nodeList.get(j).y));
                    nodeList.get(j).cost = (float) (currentNode.cost + cost + costEnd);
                    neighbourList.add(nodeList.get(j));
                }
            }
        }
        nodeWithMinF = neighbourList.get(0);
        for (int x = 0; x<neighbourList.size(); x++)
        {
            if(neighbourList.get(x).cost < nodeWithMinF.cost){
                nodeWithMinF = neighbourList.get(x);
            }
        }


        return nodeWithMinF;

    }

    private  void expandNode(Node currentNode){
        // überprüft alle Nachfolgeknoten und fügt sie der Open List hinzu, wenn entweder
// - der Nachfolgeknoten zum ersten Mal gefunden wird oder
// - ein besserer Weg zu diesem Knoten gefunden wird
                foreach successor of currentNode
        // wenn der Nachfolgeknoten bereits auf der Closed List ist – tue nichts
        if closedlist.contains(successor) then
        continue
                // g Wert für den neuen Weg berechnen: g Wert des Vorgängers plus
                // die Kosten der gerade benutzten Kante
                tentative_g = g(currentNode) + c(currentNode, successor)
        // wenn der Nachfolgeknoten bereits auf der Open List ist,
        // aber der neue Weg nicht besser ist als der alte – tue nichts
        if openlist.contains(successor) and tentative_g >= g(successor) then
        continue
                // Vorgängerzeiger setzen und g Wert merken
                successor.predecessor := currentNode
        g(successor) = tentative_g
        // f Wert des Knotens in der Open List aktualisieren
        // bzw. Knoten mit f Wert in die Open List einfügen
        f := tentative_g + h(successor)
        if openlist.contains(successor) then
        openlist.decreaseKey(successor, f)
        else
        openlist.enqueue(successor, f)
        end
                end
    }

    public LinkedList<Node> getPath(Node start, Node exit) {
        LinkedList<Node> foundPath = new LinkedList<Node>();
        LinkedList<Node> opensList= new LinkedList<Node>();
        LinkedList<Node> closedList= new LinkedList<Node>();
        HashMap<Node, Integer> gscore = new HashMap<Node, Integer>();
        HashMap<Node, Node> cameFrom = new HashMap<Node, Node>();
        Node x = new Node();
        gscore.put(start, 0);
        opensList.add(start);
        while(!opensList.isEmpty()){

            int min = -1;
            //searching for minimal F score
            for(Node f : opensList){
                if(min==-1){
                    min = gscore.get(f)+getH(f,exit);
                    x = f;
                }else{
                    int currf = gscore.get(f)+getH(f,exit);
                    if(min > currf){
                        min = currf;
                        x = f;
                    }
                }
            }
            if(x == exit){
                //path reconstruction
                Node curr = exit;
                while(curr != start){
                    foundPath.addFirst(curr);
                    curr = cameFrom.get(curr);
                }
                return foundPath;
            }
            opensList.remove(x);
            closedList.add(x);
            for(Node y : x.getNeighbourhood()){
                if(!(y.getType()==FieldTypes.PAVEMENT ||y.getType() == FieldTypes.GRASS) || closedList.contains(y) || !(y.getStudent()==null))
                {
                    continue;
                }
                int tentGScore = gscore.get(x) + getDist(x,y);
                boolean distIsBetter = false;
                if(!opensList.contains(y)){
                    opensList.add(y);
                    distIsBetter = true;
                }else if(tentGScore < gscore.get(y)){
                    distIsBetter = true;
                }
                if(distIsBetter){
                    cameFrom.put(y, x);
                    gscore.put(y, tentGScore);
                }
            }
        }

        return foundPath;
    }

    private int getH(Node start, Node end){
        int x;
        int y;
        x = start.getX()-end.getX();
        y = start.getY() - end.getY();
        if(x<0){
            x = x* (-1);
        }
        if(y<0){
            y = y * (-1);
        }
        return x+y;
    }
    private int getDist(Node start, Node end){
        int ret = 0;
        if(end.getType() == FieldTypes.PAVEMENT){
            ret = 8;
        }else if(start.getX() == end.getX() || start.getY() == end.getY()){
            ret = 10;
        }else{
            ret = 14;
        }

        return ret;
    }
}
