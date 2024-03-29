package com.example.hayoung.a20190507_1146;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingEvent;

import java.util.ArrayList;
import java.util.List;

public class GeofenceTransitionService extends IntentService{

    private static final String TAG = GeofenceTransitionService.class.getSimpleName();

    public static final int GEOFENCE_NOTIFICATION_ID = 0;

    public GeofenceTransitionService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        // Handling errors
        if (geofencingEvent.hasError()) {
            String errorMsg = getErrorString(geofencingEvent.getErrorCode());
            Log.e(TAG, errorMsg);
            return;
        }


        int geoFenceTransition = geofencingEvent.getGeofenceTransition();

        // Check if the transition type is of interest
        if (geoFenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
                geoFenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {

            // Get the geofence that were triggered
            List<Geofence> triggeringGeofence = geofencingEvent.getTriggeringGeofences();

            String geofenceTransitionDetails = getGeofenceTransitionDetails(geoFenceTransition, triggeringGeofence);

            // Send notification details as a String
            sendNotification(geofenceTransitionDetails);
        }
    }


    private String getGeofenceTransitionDetails(int geoFenceTransition, List<Geofence> triggeringGeofences) {
        // get the ID of each geofence triggered
        ArrayList<String> triggeringGeofenceList = new ArrayList<>();
        for (Geofence geofence : triggeringGeofences) {
            triggeringGeofenceList.add(geofence.getRequestId());
        }


        String status = null;
        if (geoFenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER){
            status = "아이가 안심존으로 들어왔습니다. ";
            //Toast.makeText(GeofenceTransitionService.this, "아이가 안심존으로 들어왔습니다.", Toast.LENGTH_SHORT).show();
        }
        else if (geoFenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
            status = "아이가 안심존을 벗어났습니다. ";
            //Toast.makeText(GeofenceTransitionService.this, "아이가 안심존을 벗어났습니다. ", Toast.LENGTH_SHORT).show();
        }
        return status + TextUtils.join(", ", triggeringGeofenceList);
    }


    private void sendNotification(String msg) {
        Log.i(TAG, "sendNotification: " + msg);


        // Intent to start the main Activity
        Intent notificationIntent = GeofenceMainActivity.makeNotificationIntent(
                getApplicationContext(), msg);


        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(GeofenceMainActivity.class); // 오류수정:20190604-00:47
        stackBuilder.addNextIntent(notificationIntent);
        PendingIntent notificationPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);


        // Creating and sending Notification
        NotificationManager notificationMng =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationMng.notify(GEOFENCE_NOTIFICATION_ID,
                createNotification(msg, notificationPendingIntent));
    }


    // Create notification
    private Notification createNotification(String msg, PendingIntent notificationPendingIntent) {
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this);
        notificationBuilder
                .setSmallIcon(R.mipmap.ic_launcher)
                .setColor(Color.RED)
                .setContentTitle(msg)
                .setContentText("알림알림")
                .setContentIntent(notificationPendingIntent)
                .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_VIBRATE | Notification.DEFAULT_SOUND)
                .setAutoCancel(true);
        return notificationBuilder.build();
    }


    private static String getErrorString(int errorCode) {
            switch (errorCode) {
            case GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE:
                return "GeoFence not available";
            case GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES:
                return "Too many GeoFences";
            case GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS:
                return "Too many pending intents";
            default:
                return "Unknown error.";
        }
    }


}
