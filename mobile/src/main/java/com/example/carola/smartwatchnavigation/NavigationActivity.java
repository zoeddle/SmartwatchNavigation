package com.example.carola.smartwatchnavigation;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.os.Environment;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.hadizadeh.positioning.controller.PositionListener;
import de.hadizadeh.positioning.controller.PositionManager;
import de.hadizadeh.positioning.controller.Technology;
import de.hadizadeh.positioning.exceptions.PositioningException;
import de.hadizadeh.positioning.exceptions.PositioningPersistenceException;
import de.hadizadeh.positioning.model.PositionInformation;

public class NavigationActivity extends AppCompatActivity implements PositionListener {

    private ImageView image;
    //private Bitmap mutableBitmap;
    //private Canvas canvas;
    private PositionManager positionManager;
    private NewXMLPersistenceManager xmlPersistenceManager;
    private Node nodeToSearch;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.example.carola.smartwatchnavigation.R.layout.activity_navigation);
        
        image = (ImageView) findViewById(R.id.i_floorPlan);
        
        ArrayList<Node> existingNodes = initializationAndFindExistingNodes();

        positionManager.startPositioning(1000);

        Intent i= getIntent();
        Bundle b = i.getExtras();

        if(b!=null && existingNodes!= null)
        {
            String query =(String) b.get("searchString");
            for(Node searchNode : existingNodes){
                if (searchNode.searchName.toLowerCase().equals(query.toLowerCase())){
                    nodeToSearch=searchNode;
                }
                else {
                    // TODO fehlerbehebung
                }
            }
        }
        else {
            //TODO Fehlerbehebung
        }
        

    }

    private ArrayList<Node> initializationAndFindExistingNodes() {
        File file = new File(Environment.getExternalStorageDirectory().getAbsoluteFile(), "myHome.xml");
        //File file = new File(Environment.getExternalStorageDirectory().getAbsoluteFile(), "og6Information.xml");

        try {
            xmlPersistenceManager = new NewXMLPersistenceManager(file);
            positionManager = new PositionManager(xmlPersistenceManager);
            List<String> keyWhiteList = new ArrayList<String>();
            //weihiteList Julian
//        keyWhiteList.add("88:03:55:0b:22:44".toLowerCase());
//        keyWhiteList.add("bc:05:43:b4:3d:72".toLowerCase());
//        keyWhiteList.add("34:81:c4:f9:22:b5".toLowerCase());
//        keyWhiteList.add("5c:35:3b:ef:e0:ec".toLowerCase());
//        keyWhiteList.add("e8:37:7a:1a:56:5b".toLowerCase());
//        keyWhiteList.add("34:81:c4:c7:46:50".toLowerCase());
            //whiteList zuHause
            keyWhiteList.add("58:8b:f3:50:da:b1".toLowerCase());
            keyWhiteList.add("18:83:bf:d1:ff:72".toLowerCase());
            keyWhiteList.add("00:1e:be:8c:d6:a0".toLowerCase());
            Technology wifiTechnology = new WifiTechnology(this, "WIFI", keyWhiteList);

            try {
                positionManager.addTechnology(wifiTechnology);
            } catch (PositioningException e) {
                e.printStackTrace();
            }
            positionManager.registerPositionListener(this);

            Log.d("positionManager", "initialized");


            List<String> positions = positionManager.getMappedPositions();
            if (positions != null){
                ArrayList<Node> actuallyNodes = new ArrayList<Node>();
                for(String nodeName : positions) {
                    Node nodeToAdd = xmlPersistenceManager.getNodeData(nodeName);
                    actuallyNodes.add(nodeToAdd);
                }
                return actuallyNodes;

            }
        } catch (PositioningPersistenceException e) {
            //TODO fehlermeldung
        }

        return null;
    }

    private void drawNode(float x, float y, Canvas canvas) {

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        paint.setColor(Color.BLUE);
        paint.setStyle(Paint.Style.FILL);

        canvas.drawCircle(x, y, 25, paint);

        //image.setImageBitmap(mutableBitmap);
    }

    private void drawLine(Node start, Node end, Canvas canvas){

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        paint.setColor(Color.BLUE);
        paint.setStrokeWidth(10);

        float startx = start.x;
        float starty = start.y;
        float endx = end.x;
        float endy = end.y;

        canvas.drawLine(startx, starty, endx, endy, paint);

        //image.setImageBitmap(mutableBitmap);
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
            //Pfad rekonsturktion
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


    @Override
    public void positionReceived(PositionInformation positionInformation) {

    }

    @Override
    public void positionReceived(List<PositionInformation> list) {
        String positionName = list.get(0).getName();
        final Node recievedNode = xmlPersistenceManager.getNodeData(positionName);
        final ArrayList<Node> path;

        //TODO performence sparen wenn Node gleich vorheriger
            if(nodeToSearch!= null && recievedNode != null){
                path = aStar(recievedNode, nodeToSearch);
                Log.d("Liste", "Liste erstellt");
            }
            else {
                path = null;
                this.finish();
            }
            if(path != null){

                //TODO Path Informationen berechnen
                ArrayList<PathInforamtion> pathInforamtionList = new ArrayList<>();
                for (int i = 0; i<path.size()-2; i++){
                    double m1,m2,tan,angle;
                    m1= (path.get(i+1).y - path.get(i).y)/(path.get(i+1).x - path.get(i).x);
                    m2= (path.get(i+2).y - path.get(i+1).y)/(path.get(i+2).x - path.get(i+1).x);
                    tan= Math.abs((m1-m2/(1+m1*m2)));
                    double arctan=Math.atan(tan);
                    double test = 7./6.;
                    angle = Math.toDegrees(test);
                    pathInforamtionList.add(new PathInforamtion(m1,m2,tan,angle));
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        image.setImageResource(R.drawable.wohnung_grundriss);
                        Bitmap bitmap = ((BitmapDrawable) image.getDrawable()).getBitmap();

                        Bitmap mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);

                        Canvas canvas = new Canvas(mutableBitmap);

                        drawNode(recievedNode.x, recievedNode.y, canvas);
                        drawNode(nodeToSearch.x, nodeToSearch.y, canvas);

                        Node currNodeToDraw = null;
                        for (Node nextNodeToDraw : path) {
                            if (currNodeToDraw != null) {
                                drawLine(currNodeToDraw, nextNodeToDraw, canvas);
                            }
                            currNodeToDraw = nextNodeToDraw;
                        }

                        image.setImageBitmap(mutableBitmap);
                    }
                });

            }

    }
}
