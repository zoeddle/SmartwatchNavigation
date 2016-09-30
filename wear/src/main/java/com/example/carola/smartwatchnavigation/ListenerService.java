package com.example.carola.smartwatchnavigation;

import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

/**
 * Created by Carola on 29.09.16.
 */
public class ListenerService extends WearableListenerService {

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        NotificationCompat.Builder notification_builder;
        NotificationManagerCompat notification_manager;
        int notification_id = 1;
        final String NOTIFICATION_ID = "notification_id";

        notification_builder = new NotificationCompat.Builder(this)
                .setContentTitle(messageEvent.getPath())
                .setContentText("Content");

        notification_manager = NotificationManagerCompat.from(this);

        notification_manager.notify(notification_id, notification_builder.build());
    }

}
