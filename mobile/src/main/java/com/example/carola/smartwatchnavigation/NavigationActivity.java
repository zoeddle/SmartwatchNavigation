package com.example.carola.smartwatchnavigation;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NavigationActivity extends AppCompatActivity {

    private ImageView image;
    private Bitmap mutableBitmap;
    private Canvas canvas;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.example.carola.smartwatchnavigation.R.layout.activity_navigation);

        image = (ImageView) findViewById(com.example.carola.smartwatchnavigation.R.id.i_floorPlan);
        Bitmap bitmap = ((BitmapDrawable) image.getDrawable()).getBitmap();

        mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);

        canvas = new Canvas(mutableBitmap);

        //ArrayList<Node> existingNodes = (ArrayList<Node>) getIntent().getSerializableExtra("allExistingNodes");

        ArrayList<Node> existingNodes = Singleton.getInstance().getExistingNodes();

        if(existingNodes != null){
        for(int i = 0; i<existingNodes.size(); i++){
                    drawNode(existingNodes.get(i).x, existingNodes.get(i).y);
                }
                ArrayList<Node> path = aStar(existingNodes.get(0), existingNodes.get(7));
            if(path != null){
                Log.e("Liste", "Liste erstellt");
            }

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

    private ArrayList<Node> aStar(Node start, Node end){
        ArrayList<PathNode> openList = new ArrayList<>();
        Set<Node> closedList = new HashSet<>();
        ArrayList<Node> path = new ArrayList<>();

        Node startNode = start;
        PathNode currentNode = new PathNode(null,startNode,0,0);

        // Initialisierung der Open List, die Closed List ist noch leer
        // (die Priorität bzw. der f Wert des Startknotens ist unerheblich)
        openList.add(currentNode);

        // diese Schleife wird durchlaufen bis entweder
        // - die optimale Lösung gefunden wurde oder
        // - feststeht, dass keine Lösung existiert
        while(!openList.isEmpty()){

        // Knoten mit dem geringsten f Wert aus der Open List entfernen
        PathNode nodeWithMinimalF = findNodeWithMinimalF(openList);
            currentNode = nodeWithMinimalF;
        openList.remove(nodeWithMinimalF);

        // Wurde das Ziel gefunden?
        if (currentNode.node == end){
            //path.add(currentNode.node);
            while (currentNode.node != start){
                path.add(currentNode.node);
                currentNode = currentNode.predecessorNode;
            }
            path.add(start);
            return path;
        }

        // Der aktuelle Knoten soll durch nachfolgende Funktionen
        // nicht weiter untersucht werden damit keine Zyklen entstehen
            closedList.add(currentNode.node);

        // Wenn das Ziel noch nicht gefunden wurde: Nachfolgeknoten
        // des aktuellen Knotens auf die Open List setzen
        expandNode(currentNode,openList,closedList,end);
        }

        // die Open List ist leer, es existiert kein Pfad zum Ziel
        return null;
    }

    private PathNode findNodeWithMinimalF(ArrayList<PathNode> openList) {

        PathNode nodeWithMinF = openList.get(0);

        for (PathNode node : openList){
            if (node.f < nodeWithMinF.f){
                nodeWithMinF = node;
            }
        }

        return nodeWithMinF;

    }
    private  void expandNode(PathNode currentNode, ArrayList<PathNode> openList, Set<Node> closedList, Node end){
        // überprüft alle Nachfolgeknoten und fügt sie der Open List hinzu, wenn entweder
        // - der Nachfolgeknoten zum ersten Mal gefunden wird oder
        // - ein besserer Weg zu diesem Knoten gefunden wird

        //for(PathNode successor : currentNode.node.neighbours){
        for(Node successor : (List<Node>)currentNode.node.neighbours){
                boolean foundInOpenList = false;

            // wenn der Nachfolgeknoten bereits auf der Closed List ist – tue nichts
            if (closedList.contains(successor)) {
                continue;
            }

            // Überrpüfen ob Knoten in der Open List, um PathNode zu erhalten
            PathNode successorPathNode = null;

            for(PathNode OpenListNode : openList) {
                if(OpenListNode.node == successor) {
                    successorPathNode = OpenListNode;
                    foundInOpenList = true;
                    break;
                }
            }

            // Falls nicht in OpenList gefunden, wird Knoten das erstemal besucht, d.h. PathNode anlegen für den Knoten
            if(successorPathNode == null) {
                successorPathNode = new PathNode();
                successorPathNode.node = successor;
            }

            // g Wert für den neuen Weg berechnen: g Wert des Vorgängers plus
            // die Kosten der gerade benutzten Kante
            float c = calculateC(currentNode,successorPathNode);
            float tentative_cost = currentNode.cost + c;

            // wenn der Nachfolgeknoten bereits auf der Open List ist,
            // aber der neue Weg nicht besser ist als der alte – tue nichts
            if (foundInOpenList && tentative_cost >= successorPathNode.cost)  {
                continue;
            }

            // Vorgängerzeiger setzen und g Wert merken
            //successor.predecessor := currentNodes
            successorPathNode.predecessorNode = currentNode;
            successorPathNode.cost = tentative_cost;

            // f Wert des Knotens in der Open List aktualisieren
            // bzw. Knoten mit f Wert in die Open List einfügen
            float h = calculateH(successorPathNode, end);
            successorPathNode.f = tentative_cost + h;

            if (foundInOpenList){
                Log.d("node", "node ist gleich");
                //openList.decreaseKey(successor, f);
                continue;
            }
            else
            {
                openList.add(successorPathNode);
            }

        }
    }

    private float calculateH(PathNode successor, Node end) {
        return (float) Math.sqrt((successor.node.x-end.x) * (successor.node.x-end.x) + (successor.node.y-end.y) * (successor.node.y-end.y));
    }

    private float calculateC(PathNode currentNode, PathNode successor) {
        return (float) Math.sqrt((currentNode.node.x-successor.node.x) * (currentNode.node.x-successor.node.x) + (currentNode.node.y-successor.node.y) * (currentNode.node.y-successor.node.y));
    }


}
