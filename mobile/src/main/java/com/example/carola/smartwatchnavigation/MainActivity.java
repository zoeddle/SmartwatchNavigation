package com.example.carola.smartwatchnavigation;

import android.Manifest;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SearchView;
import android.view.Menu;
import android.view.MenuInflater;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import de.hadizadeh.positioning.controller.PositionManager;
import de.hadizadeh.positioning.exceptions.PositioningPersistenceException;

public class MainActivity extends AppCompatActivity{

    private static final int ASK_MULTIPLE_PERMISSION_REQUEST_CODE = 3;
    String[] permissions;
    private PositionManager positionManager;
    private NewXMLPersistenceManager xmlPersistenceManager;
    ArrayList<Node> allNodes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.example.carola.smartwatchnavigation.R.layout.activity_main);

        permissions = new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_COARSE_LOCATION};
        if(!hasPermissions(MainActivity.this, permissions)){
            ActivityCompat.requestPermissions(MainActivity.this, permissions, ASK_MULTIPLE_PERMISSION_REQUEST_CODE);
        }
        else {
            //show Massage
        }

        allNodes = findExistingNodes();

        final Button buttonSetting = (Button) findViewById(com.example.carola.smartwatchnavigation.R.id.b_settings);
        buttonSetting.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                Intent i = new Intent(MainActivity.this, SettingActivity.class);
                startActivity(i);

            }
        });


        final Button buttonNavigation = (Button) findViewById(com.example.carola.smartwatchnavigation.R.id.b_navigation);
        buttonNavigation.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                //TODO Permission Check
                Singleton.getInstance().setExistingNodes(allNodes);
                Intent i = new Intent(MainActivity.this, NavigationActivity.class);
                //i.putExtra("allExistingNodes", allNodes );
                startActivity(i);

            }
        });

        handleIntent(getIntent());

    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            //use the query to search your data somehow

            doMySearch(query);
        }
    }

    private void doMySearch(String query) {

    }

    private boolean hasPermissions(Context context, String[] permissions) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    private ArrayList<Node> findExistingNodes() {
        File file = new File(Environment.getExternalStorageDirectory().getAbsoluteFile(), "myHome.xml");

        try {
            xmlPersistenceManager = new NewXMLPersistenceManager(file);
            positionManager = new PositionManager(xmlPersistenceManager);
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

        }

        return null;
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the options menu from XML
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(com.example.carola.smartwatchnavigation.R.menu.options_menu, menu);

        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(com.example.carola.smartwatchnavigation.R.id.search).getActionView();
        // Assumes current activity is the searchable activity
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default

        return true;
    }

}
