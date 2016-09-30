package com.example.carola.smartwatchnavigation;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.inputmethodservice.Keyboard;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ListView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import de.hadizadeh.positioning.controller.PositionListener;
import de.hadizadeh.positioning.controller.PositionManager;
import de.hadizadeh.positioning.controller.Technology;
import de.hadizadeh.positioning.exceptions.PositioningException;
import de.hadizadeh.positioning.exceptions.PositioningPersistenceException;
import de.hadizadeh.positioning.model.PositionInformation;

public class NavigationActivity extends AppCompatActivity implements PositionListener, TextToSpeech.OnInitListener {

    private ImageView image;
    //private Bitmap mutableBitmap;
    //private Canvas canvas;
    private PositionManager positionManager;
    private NewXMLPersistenceManager xmlPersistenceManager;
    private Node nodeToSearch;
    private Node recievedNode;
    private ArrayList<Node> path;
    private ListView listView;
    private ArrayList<PathInforamtion> pathInforamtionList;
    private PathInformationAdapter adapter;
    private TextToSpeech tts;
    private GoogleApiClient client;
    private String nodeId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.example.carola.smartwatchnavigation.R.layout.activity_navigation);

        listView = (ListView) findViewById(R.id.l_pathInformation);
        
        image = (ImageView) findViewById(R.id.i_floorPlan);

        tts = new TextToSpeech(getApplicationContext(),this);

        //initializeGoogleAPI();
        
        ArrayList<Node> existingNodes = initializationAndFindExistingNodes();

        positionManager.startPositioning(1000);

        adapter = new PathInformationAdapter(this);

        listView.setAdapter(adapter);

        path = null;

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
                    this.finish();
                    // TODO fehlerbehebung
                }
            }
        }
        else {
            this.finish();
            //TODO Fehlerbehebung
        }


//        tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
//            @Override
//            public void onInit(int status) {
//                if (status != TextToSpeech.ERROR) {
//                    tts.setLanguage(Locale.GERMANY);
//                }
//            }
//        });


    }


    private ArrayList<Node> initializationAndFindExistingNodes() {
        //File file = new File(Environment.getExternalStorageDirectory().getAbsoluteFile(), "myHome.xml");
        File file = new File(Environment.getExternalStorageDirectory().getAbsoluteFile(), "og6Information.xml");

        try {
            xmlPersistenceManager = new NewXMLPersistenceManager(file);
            positionManager = new PositionManager(xmlPersistenceManager);
            List<String> keyWhiteList = new ArrayList<String>();
            keyWhiteList.addAll(getMacAdresses());
            //whiteList zuHause
//            keyWhiteList.add("58:8b:f3:50:da:b1".toLowerCase());
//            keyWhiteList.add("1c:74:0d:64:80:7b".toLowerCase());
//            keyWhiteList.add("34:31:c4:0c:cf:7e".toLowerCase());
//            keyWhiteList.add("18:83:bf:d1:ff:72".toLowerCase());
//            keyWhiteList.add("5c:dc:96:bc:39:80".toLowerCase());
//            keyWhiteList.add("a0:e4:cb:a5:41:a1".toLowerCase());
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

    private List<String> getMacAdresses() {
        List<String> macAddresses = new ArrayList<>();
        try {
            List<String> assetList = Arrays.asList(this.getAssets().list(""));
            for (String fileName : assetList) {
                if (fileName.toLowerCase().contains("bssid")) {
                    //readMacAdressesFormTxT(fileName, macAddresses);
                    readMacAddressesFromXls(fileName, macAddresses);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return macAddresses;
    }

    private void readMacAddressesFromXls(String filename, List<String> dest) throws IOException {
        InputStream inputStream = this.getAssets().open(filename);
        POIFSFileSystem poifsFileSystem = new POIFSFileSystem(inputStream);
        HSSFWorkbook hssfWorkbook = new HSSFWorkbook(poifsFileSystem);
        HSSFSheet sheet = hssfWorkbook.getSheetAt(0);
        HSSFRow row;
        HSSFCell cell;

        Iterator<Row> rowIterator = sheet.rowIterator();
        //TODO Clean this up quick way to skip the heading row
        rowIterator.next();

        while (rowIterator.hasNext()) {
            row = (HSSFRow) rowIterator.next();
            Iterator<Cell> cellIterator = row.cellIterator();
            while (cellIterator.hasNext()) {
                cell = (HSSFCell) cellIterator.next();
                if (cell.getColumnIndex() == 4) {
                    String address = addSeparator(cell.toString());
                    if (! address.equals("")){
                        dest.add(address);
                    }
                }
            }
        }

    }

    private String addSeparator(String macAddress) {

        StringBuilder addressWithSeparator = new StringBuilder();

        for (int i = 0; i < macAddress.length(); i++) {
            if (i % 2 == 0 && i != 0) {
                addressWithSeparator.append(":");
            }
            addressWithSeparator.append(macAddress.charAt(i));
        }

        return addressWithSeparator.toString();
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

        // wenn kein Pfad da ist Pfad berechnen und hier auch das erste mal recieve Node setzten
        // Wenn Pfad da überprüfen ob noch gleichen Knoten dann abbrechen sonst überprüfen ob knoten auf pfad wenn ja position ermitteln und sachen wenn nicht pfad neu berechnen

        if(path == null){
            recievedNode = xmlPersistenceManager.getNodeData(positionName);
            buildPath();

        }
        else {
            if(recievedNode.name == positionName){
                Log.d("Gleich", "Die Nodes sind gleich");
                return;
            }
            else {
                recievedNode = xmlPersistenceManager.getNodeData(positionName);

                int position = 0;
                boolean contain = false;
                for(int i = 0; i<path.size(); i++){
                    if(path.get(i).name.equals(positionName)){
                        Log.d("Enthalten", "Knoten ist in Pfad enthalten");
                        contain = true;
                        position = i;
                    }
                }

                if(contain){
                    drawPath(path.get(position).x, path.get(position).y);

                    final ArrayList<PathInforamtion> newPathInforamtionList = new ArrayList<>();

                    for(int i = position; i<pathInforamtionList.size(); i++){
                        newPathInforamtionList.add(new PathInforamtion(pathInforamtionList.get(i).angle,pathInforamtionList.get(i).lenght));
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            adapter.clear();
                            adapter.addAll(newPathInforamtionList);
                            adapter.notifyDataSetChanged();
                        }
                    });

                    if(newPathInforamtionList.get(0).angle<180)
                    {
                        checkSettingsAndReveiveInstruction(1);
                    }
                    else if(newPathInforamtionList.get(0).angle>180){
                        checkSettingsAndReveiveInstruction(2);
                    }
                    else{
                        checkSettingsAndReveiveInstruction(3);
                    }


                } else {
                    buildPath();
                    Log.d("Neu", "Neuer Pfad berechnet");
                }
            }
        }

    }

    private void buildPath(){
        //recievedNode = xmlPersistenceManager.getNodeData(positionName);
        if(nodeToSearch!= null && recievedNode != null){
            if(nodeToSearch == recievedNode){
                Log.d("Ende", "Ziel erreicht");
                return;
            }
            //path = aStar(recievedNode, nodeToSearch);
            path = aStar(nodeToSearch, recievedNode);
            Log.d("Liste", "Liste erstellt");
        }
        else {
            path = null;
            this.finish();
        }
        if(path != null){

            pathInforamtionList = new ArrayList<>();

            for (int i = 0; i<path.size()-1; i++){
                double angle;
                double lenght;

                if (i == 0){
                    lenght = getLenght(path.get(i).x,path.get(i+1).x,path.get(i).y,path.get(i+1).y);
                    pathInforamtionList.add(new PathInforamtion(Double.NaN,lenght));
                }
                else{
                    angle= getVectorAngle(path.get(i - 1).x, path.get(i).x, path.get(i + 1).x, path.get(i - 1).y, path.get(i).y, path.get(i + 1).y);
                    lenght = getLenght(path.get(i).x, path.get(i + 1).x, path.get(i).y, path.get(i + 1).y);
                    pathInforamtionList.add(new PathInforamtion(angle,lenght));
                }
            }

            drawPath(recievedNode.x, recievedNode.y);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    adapter.clear();
                    adapter.addAll(pathInforamtionList);
                    adapter.notifyDataSetChanged();
                }
            });

        }
    }

    private void drawPath(final float x, final float y) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                image.setImageResource(R.drawable.wohnung_grundriss);
                Bitmap bitmap = ((BitmapDrawable) image.getDrawable()).getBitmap();

                Bitmap mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);

                Canvas canvas = new Canvas(mutableBitmap);

                drawNode(x, y, canvas);
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

    private void checkSettingsAndReveiveInstruction(int instruction){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        boolean sound = sharedPref.getBoolean("pref_key_sound", true);
        boolean hapticalFeedback = sharedPref.getBoolean("pref_key_hapticalFeedback", true);

        int walk = 0;

        long[] pattern;

        //sendMessage("test");

        if(sound){
            switch(instruction) {
                case 1:
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        tts.speak("links", TextToSpeech.QUEUE_FLUSH, null, null);
                    }
                    else {
                        tts.speak("links", TextToSpeech.QUEUE_FLUSH, null);
                    }
                    break;
                case 2:
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        tts.speak("rechts", TextToSpeech.QUEUE_FLUSH, null, null);
                    }
                    else {
                        tts.speak("rechts", TextToSpeech.QUEUE_FLUSH, null);
                    }
                    break;
                case 3:
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        tts.speak("geradeaus", TextToSpeech.QUEUE_FLUSH, null, null);
                    }
                    else {
                        tts.speak("geradeaus", TextToSpeech.QUEUE_FLUSH, null);
                    }
                    break;
            }
        }

        if(hapticalFeedback){
            switch(instruction) {
                case 1:
                    walk = Integer.parseInt(sharedPref.getString("pref_key_hapticalFeedbackLeft", "1"));
                    break;
                case 2:
                    walk = Integer.parseInt(sharedPref.getString("pref_key_hapticalFeedbackRight", "2"));
                    break;
                case 3:
                    walk = Integer.parseInt(sharedPref.getString("pref_key_hapticalFeedbackStraightOn", "3"));
                    break;
            }

            switch (walk){
                case 1:
                    pattern = new long[]{0, 400};
                    break;
                case 2:
                    pattern = new long[]{0, 400, 200, 400};
                    break;
                case 3:
                    pattern = new long[]{0, 400, 200, 400, 200, 400};
                    break;
                default:
                    pattern = null;
                    break;
            }

            NotificationCompat.Builder notification_builder;
            NotificationManagerCompat notification_manager;
            int notification_id = 1;

            notification_builder = new NotificationCompat.Builder(this)
                    .setContentTitle("Test")
                    .setContentText("Content")
                    .setSmallIcon(R.drawable.pfeil_links)
                    .setVibrate(pattern);

            notification_manager = NotificationManagerCompat.from(this);

            notification_manager.notify(notification_id, notification_builder.build());
        }

    }

    private double getLenght(double pointOneX, double pointTwoX, double pointOneY, double pointTwoY){
        double referencePointInPixel = 131;
        double referencePointInM = 3.45;
        double lenghtInPixel;
        double lenghtInM;

        lenghtInPixel = Math.sqrt(Math.pow((pointTwoX-pointOneX),2)+Math.pow((pointTwoY-pointOneY),2));
        lenghtInM = (referencePointInM/referencePointInPixel)*lenghtInPixel;
        return lenghtInM;
    }
    private double getVectorAngle(double pointOneX, double pointTwoX, double pointThreeX, double pointOneY, double pointTwoY, double pointThreeY)
    {
        //(return Math.acos(((ax * bx) + (ay * by)) / ((Math.sqrt(Math.pow(ax, 2) + Math.pow(ay, 2))) * (Math.sqrt(Math.pow(bx, 2) + Math.pow(by, 2)))));
        double ax,ay,nax,nay;
        double bx,by,nbx,nby;
        double vectorLenghta, vectorLenghtb;
        double cosa,cosb;
        double angle;

        ax = (pointTwoX - pointOneX)*-1;
        ay = (pointTwoY - pointOneY)*-1;

        bx = (pointThreeX - pointTwoX);
        by = (pointThreeY - pointTwoY);

        vectorLenghta = Math.sqrt(Math.pow(ax,2) + Math.pow(ay,2));
        vectorLenghtb = Math.sqrt(Math.pow(bx,2) + Math.pow(by,2));

        nax = (1/vectorLenghta)*ax;
        nay = (1/vectorLenghta)*ay;

        nbx = (1/vectorLenghtb)*bx;
        nby = (1/vectorLenghtb)*by;

        if(nay>0){
            cosa = Math.toDegrees(Math.acos(nax));
        }else{
            cosa = 360- Math.toDegrees(Math.acos(nax));
        }

        if(nby>0){
            cosb = Math.toDegrees(Math.acos(nbx));
        }else{
            cosb = 360- Math.toDegrees(Math.acos(nbx));
        }

        angle = cosb-cosa;

        if(angle<0){
            angle = angle +360;
        }
        return angle;
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            tts.setLanguage(Locale.GERMANY);
        } else {
            Log.e("TTS", "Initialization failed");
        }
    }

//    private GoogleApiClient getGoogleApiClient(Context context) {
//        return new GoogleApiClient.Builder(context)
//                .addApi(Wearable.API)
//                .build();
//    }
//
//    private void initializeGoogleAPI() {
//
//        client = getGoogleApiClient(this);
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                client.blockingConnect(100, TimeUnit.MILLISECONDS);
//                NodeApi.GetConnectedNodesResult result =
//                        Wearable.NodeApi.getConnectedNodes(client).await();
//                List<com.google.android.gms.wearable.Node> nodes = result.getNodes();
//                if (nodes.size() > 0) {
//                    nodeId = nodes.get(0).getId();
//                }
//                client.disconnect();
//            }
//        }).start();
//    }
//
//
//    private void sendMessage(final String message) {
//        if (nodeId != null) {
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    client.blockingConnect(100, TimeUnit.MILLISECONDS);
//                    Wearable.MessageApi.sendMessage(client, nodeId, message, null);
//                    client.disconnect();
//                }
//            }).start();
//        }
//    }
}
