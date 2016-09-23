package com.example.carola.smartwatchnavigation;

import android.Manifest;
import android.app.Notification;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.NotificationCompat.WearableExtender;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SearchView;
import android.view.Menu;
import android.view.MenuInflater;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final int ASK_MULTIPLE_PERMISSION_REQUEST_CODE = 3;
    String[] permissions;
    TextToSpeech tts;
    private NotificationCompat.Builder notification_builder;
    private NotificationManagerCompat notification_manager;
    private int notification_id = 1;
    private final String NOTIFICATION_ID = "notification_id";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        permissions = new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_COARSE_LOCATION};
        if(!hasPermissions(MainActivity.this, permissions)){
            ActivityCompat.requestPermissions(MainActivity.this, permissions, ASK_MULTIPLE_PERMISSION_REQUEST_CODE);
        }
        else {
            //show Massage
        }

        tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    tts.setLanguage(Locale.GERMANY);
//                    tts.setPitch(1.3f);
//                    tts.setSpeechRate(1f);
                }
            }
        });


//        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
//
//        long[] pattern = {0, 400, 200, 400};
//
//        v.vibrate(pattern, -1);
//
//
//        tts.speak("links", TextToSpeech.QUEUE_FLUSH, null);
//
//        //allNodes = findExistingNodes();
//
//        notification_builder = new NotificationCompat.Builder(this)
//                .setVibrate(pattern)
//                .setSmallIcon(R.drawable.ic_media_play)
//                .setLargeIcon(BitmapFactory.decodeResource(
//                        getResources(), R.drawable.pfeil_links))
//                .setContentTitle("Titel")
//                .setContentText("Content");

        //notification_manager = NotificationManagerCompat.from(this);

        final Button buttonSetting = (Button) findViewById(R.id.b_settings);
        buttonSetting.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                //notification_manager.notify(notification_id,notification_builder.build());

                Intent i = new Intent(MainActivity.this, SettingActivity.class);
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

        Log.d("SuchString", query);
        Intent i = new Intent(MainActivity.this, NavigationActivity.class);
        i.putExtra("searchString", query);
        startActivity(i);
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

//    private ArrayList<Node> findExistingNodes() {
//        File file = new File(Environment.getExternalStorageDirectory().getAbsoluteFile(), "myHome.xml");
//        //File file = new File(Environment.getExternalStorageDirectory().getAbsoluteFile(), "og6Information.xml");
//
//        try {
//            xmlPersistenceManager = new NewXMLPersistenceManager(file);
//            positionManager = new PositionManager(xmlPersistenceManager);
//            Log.d("positionManager", "initialized");
//            List<String> positions = positionManager.getMappedPositions();
//            if (positions != null){
//                ArrayList<Node> actuallyNodes = new ArrayList<Node>();
//                for(String nodeName : positions) {
//                    Node nodeToAdd = xmlPersistenceManager.getNodeData(nodeName);
//                    actuallyNodes.add(nodeToAdd);
//                }
//                return actuallyNodes;
//
//            }
//        } catch (PositioningPersistenceException e) {
//
//        }
//
//        return null;
//    }



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
