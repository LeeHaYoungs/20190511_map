package com.example.hayoung.a20190507_1146;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;

import org.json.JSONArray;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class GeofenceMainActivity extends AppCompatActivity
        implements
        GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks,
        LocationListener,
        OnMapReadyCallback,
        GoogleMap.OnMapClickListener,
        GoogleMap.OnMarkerClickListener {

    String myJSON;

    // List의 값을 출력하기 위한 해쉬 맵 배열
    ArrayList<HashMap<String, String>> personList;

    //그리기 변수
    PolygonOptions options;

    //진동을 울리기 위한 변수
    Vibrator vibrator;
    long[] pattern = {0, 200, 500};

    //소리를 내기 위한 변수
    MediaPlayer music;

    //AlertDiagram 확인 변수
    boolean is_alert = false;

    //저장 아이디
    String temp_store_id = "500000";

    //지오펜스 마커를 생성 했을때 그 값을 가져오는 변수
    double geo_longitude = 0.0;
    double geo_latitude = 0.0;

    // JSON 배열을 저장해 놓는 변수
    JSONArray peoples = null;

    double test = 0.01;


    private static final String TAG = GeofenceMainActivity.class.getSimpleName();

    private String textLat, textLong;
    private GoogleMap map;
    private GoogleApiClient googleApiClient;
    private Location lastLocation;


//private TextView textLat, textLong;

    private MapFragment mapFragment;

    private static final String NOTIFICATION_MSG = "NOTIFICATION MSG";

    public static Intent makeNotificationIntent(Context context, String msg) {
        Intent intent = new Intent(context, GeofenceTransitionService.class);
        intent.putExtra(NOTIFICATION_MSG, msg);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //textLat = (TextView) findViewById(R.id.lat);
        //textLong = (TextView) findViewById(R.id.lon);


        // HashMap 을 가진 변수를 만든다.
        personList = new ArrayList<HashMap<String, String>>();
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        // 소리
        music = MediaPlayer.create(this, R.raw.siren);
        music.setLooping(true);

        initGMaps();

        createGoogleApi();
    }

    private void createGoogleApi() {

        Log.d(TAG, "createGoogleApi()");
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    @Override
    // 중지할때 메소드
    protected void onPause() {
        music = null;
        vibrator = null;
        super.onPause();
    }

    @Override
    // 다시 시작 했을때
    protected void onResume() {
        //진동
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        // 소리
        music = MediaPlayer.create(this, R.raw.siren);
        music.setLooping(true);
        super.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Call GoogleApiClient connection when starting the Activity
        googleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Disconnect GoogleApiClient when stopping Activity
        googleApiClient.disconnect();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.geofence: {
                startGeofence();
                return true;
            }
            case R.id.clear: {
                clearGeofence();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private final int REQ_PERMISSION = 999;

    // Check for permission to access Location
    private boolean checkPermission() {
        Log.d(TAG, "checkPermission()");
        // Ask for permission if it wasn't granted yet
        return (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED);
    }

    // Asks for permission
    private void askPermission() {
        Log.d(TAG, "askPermission()");
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                REQ_PERMISSION
        );
    }

    // Verify user's response of the permission requested
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult()");
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQ_PERMISSION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted
                    getLastKnownLocation();


                } else {
                    // Permission denied
                    permissionsDenied();
                }
                break;
            }
        }
    }

    // App cannot work without the permissions
    private void permissionsDenied() {
        Log.w(TAG, "permissionsDenied()");
        // TODO close app and warn user
    }


    // Initialize GoogleMaps
    private void initGMaps() {
        mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    // Callback called when Map is ready
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady()");


        map = googleMap;

        // 퍼미션 체크
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        // 모서리에 현재 내 위치로 가게 해주는 버튼 메소드
        map.setMyLocationEnabled(true);

        map.setOnMapClickListener(this);
        map.setOnMarkerClickListener(this);
    }


    @Override
    public void onMapClick(LatLng latLng) {
        Log.d(TAG, "onMapClick(" + latLng + ")");
        markerForGeofence(latLng);
    }


    @Override
    public boolean onMarkerClick(Marker marker) {
        Log.d(TAG, "onMarkerClickListener: " + marker.getPosition());
        return false;
    }


    private LocationRequest locationRequest;
    // Defined in mili seconds.
// This number in extremely low, and should be used only for debug
    private final int UPDATE_INTERVAL = 1000;
    private final int FASTEST_INTERVAL = 900;


    // Start location Updates
    private void startLocationUpdates() {
        Log.i(TAG, "startLocationUpdates()");
        locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL)
                .setFastestInterval(FASTEST_INTERVAL);


        if (checkPermission())
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "onLocationChanged [" + location + "]");
        location.setLatitude(location.getLatitude());
        location.setLongitude(location.getLongitude());
        lastLocation = location;
        writeActualLocation(location);
    }


    // GoogleApiClient.ConnectionCallbacks connected
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(TAG, "onConnected()");
        getLastKnownLocation();
        //recoverGeofenceMarker();
    }


    // GoogleApiClient.ConnectionCallbacks suspended
    @Override
    public void onConnectionSuspended(int i) {
        Log.w(TAG, "onConnectionSuspended()");
    }

    // GoogleApiClient.OnConnectionFailedListener fail
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.w(TAG, "onConnectionFailed()");
    }

    // Get last known location
    private void getLastKnownLocation() {
        Log.d(TAG, "getLastKnownLocation()");
        if (checkPermission()) {
            lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
            if (lastLocation != null) {
                Log.i(TAG, "LasKnown location. " +
                        "Long: " + lastLocation.getLongitude() +
                        " | Lat: " + lastLocation.getLatitude());
                writeLastLocation();
                startLocationUpdates();
            } else {
                Log.w(TAG, "No location retrieved yet");
                startLocationUpdates();
            }
        } else askPermission();
    }

    private void writeActualLocation(Location location) {
        textLat = String.valueOf(location.getLatitude());
        textLong = String.valueOf(location.getLongitude());

        markerLocation(new LatLng(location.getLatitude(), location.getLongitude()));
    }

    private Marker locationMarker;

    private void markerLocation(LatLng latLng) {
        Log.i(TAG, "markerLocation(" + latLng + ")");
        String title = latLng.latitude + ", " + latLng.longitude;
        MarkerOptions markerOptions = new MarkerOptions()
                .position(latLng)
                .title(title);
        if (map != null) {
            if (locationMarker != null)
                locationMarker.remove();
            locationMarker = map.addMarker(markerOptions);
            float zoom = 14f;
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, zoom);
            map.animateCamera(cameraUpdate);
        }
    }

    private void writeLastLocation() {
        writeActualLocation(lastLocation);
    }

    private Marker geoFenceMarker;
    private ArrayList<Marker> geofenceMarker = new ArrayList<Marker>();


    private void markerForGeofence(LatLng latLng) {
        Log.i(TAG, "markerForGeofence(" + latLng + ")");
        String title = latLng.latitude + ", " + latLng.longitude;
        // Define marker options
        MarkerOptions markerOptions = new MarkerOptions()
                .position(latLng)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
                .title(title)
                .draggable(true);
        if (map != null) {
            // Remove last geoFenceMarker
            if (geoFenceMarker != null)
                geoFenceMarker.remove();

            geoFenceMarker = map.addMarker(markerOptions);
        }
    }

   /*
    // 새로 추가 코드 20190604-02:12
    private void markerForGeofence(LatLng latLng) {
        Log.i(TAG, "markerForGeofence(" + latLng + ")");
        String title = latLng.latitude + ", " + latLng.longitude;
        // Define marker options
        MarkerOptions markerOptions = new MarkerOptions()
                .position(latLng)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
                .title(title)
                .draggable(true);
        if (map != null) {

            geoFenceMarker = map.addMarker(markerOptions);
            geofenceMarker.add(geoFenceMarker);

            map.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
                @Override
                public void onMarkerDragStart(Marker marker) {

                }

                @Override
                public void onMarkerDrag(Marker marker) {

                }

                @Override
                public void onMarkerDragEnd(Marker marker) {
                    Log.d("System out", "onMarkerDragEnd...");
                    map.animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));
                    double lat=marker.getPosition().latitude;
                    double lng=marker.getPosition().longitude;
                    Toast.makeText(getBaseContext(),""+lat+", "+lng,Toast.LENGTH_SHORT).show();
                    addressDragged(lat,lng);


                }
            });
        }
    }
*/
    // 새로 추가 코드 20190604-02:12
    public void addressDragged(final double lat, final double lng){
        Geocoder geocoder = new Geocoder(getBaseContext(), Locale.getDefault());
       String result = "";
       try {
               List<Address> addressList = geocoder.getFromLocation(
                            lat, lng, 1);
               if (addressList != null && ((List) addressList).size() > 0) {
                       String addres = addressList.get(0).getAddressLine(0);
                       // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                       result = addres;
                   }
               else {
                       result="Failed to retrieve address.";
                   }
           } catch (IOException e) {
               Log.e(TAG, "Unable connect to Geocoder", e);
           }
       Toast.makeText(getBaseContext(), result, Toast.LENGTH_SHORT).show();

    }


    // Start Geofence creation process
    private void startGeofence() {
        Log.i(TAG, "startGeofence()");
        if (geoFenceMarker != null)
        {

            geo_latitude = geoFenceMarker.getPosition().latitude;
            geo_longitude = geoFenceMarker.getPosition().longitude;


            //for(int i = 0; i<geofenceMarker.size();i++)
            //{
            Geofence geofence = createGeofence(geoFenceMarker.getPosition(), GEOFENCE_RADIUS);
            GeofencingRequest geofenceRequest = createGeofenceRequest(geofence);
            addGeofence(geofenceRequest);
            //}
        } else
            {
            Log.e(TAG, "Geofence marker is null");
            }
    }


    private static final long GEO_DURATION = 60 * 60 * 1000;
    private static final String GEOFENCE_REQ_ID = "My Geofence";
    private static final float GEOFENCE_RADIUS = 500.0f; // in meters


    // Create a Geofence
    private Geofence createGeofence(LatLng latLng, float radius) {
        Log.d(TAG, "createGeofence");
        return new Geofence.Builder()
                .setRequestId(GEOFENCE_REQ_ID)
                .setCircularRegion(latLng.latitude, latLng.longitude, radius)
                .setExpirationDuration(GEO_DURATION)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT) // 오류 수정 20190528-01:54
                .build();
    }

    // Create a Geofence Request
    private GeofencingRequest createGeofenceRequest(Geofence geofence) {
        Log.d(TAG, "createGeofenceRequest");
        return new GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofence(geofence)
                .build();
    }

    private PendingIntent geoFencePendingIntent;
    private final int GEOFENCE_REQ_CODE = 0;

    private PendingIntent createGeofencePendingIntent() {
        Log.d(TAG, "createGeofencePendingIntent");
        if (geoFencePendingIntent != null)
        {
            return geoFencePendingIntent;
        }
        else
            {
            Intent intent = new Intent(this, GeofenceTransitionService.class);
            return PendingIntent.getService(
                    this, GEOFENCE_REQ_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            /*return PendingIntent.getService( // 수정 20190528-20:18 //오류지점
                    this, 0, new Intent(this
                            , GeofenceTransitionService.class), PendingIntent.FLAG_UPDATE_CURRENT);*/
        }
    }


    // Add the created GeofenceRequest to the device's monitoring list
    private void addGeofence(GeofencingRequest request) {
        Log.d(TAG, "addGeofence");
        if (checkPermission())
            LocationServices.GeofencingApi
                    .addGeofences(googleApiClient, request,
                            createGeofencePendingIntent()
                    ).setResultCallback(new ResultCallback<Status>() {
                @Override
                public void onResult(@NonNull Status status) {
                    Log.i(TAG, "onResult: " + status);
                    if (status.isSuccess()) {
                        saveGeofence();
                        drawGeofence();
                    } else {
                        Log.d(TAG, "Registering geofence failed: " + status.getStatusMessage() +
                                " : " + status.getStatusCode());// 추가 20190528-18:27
                        // inform about fail
                        // Toast 메세지
                        //Toast.makeText(getBaseContext(),"Could not addGeofence()",Toast.LENGTH_LONG).show();
                    }
                }
            });
    }


    // Draw Geofence circle on GoogleMap
    private Circle geoFenceLimits;

    private void drawGeofence() {
        Log.d(TAG, "drawGeofence()");


        if (geoFenceLimits != null)
            geoFenceLimits.remove();


        CircleOptions circleOptions = new CircleOptions()
                .center(geoFenceMarker.getPosition())
                .strokeColor(Color.argb(50, 70, 70, 70))
                .fillColor(Color.argb(100, 150, 150, 150))
                .radius(GEOFENCE_RADIUS);
        geoFenceLimits = map.addCircle(circleOptions);
    }


    private final String KEY_GEOFENCE_LAT = "GEOFENCE LATITUDE";
    private final String KEY_GEOFENCE_LON = "GEOFENCE LONGITUDE";

/*
    public void check_location(double lat, double lon, String id, String name) {
        Log.i("test", "Lat" + lat + "lon : " + lon);
        if (!is_alert) {
            if (!temp_store_id.equals(id)) {
                if (geo_latitude != 0.0 && geo_latitude != 0.0) {
                    double event_check = calDistance(geo_latitude, geo_longitude, lat, lon);
                    Log.i("test", "이벤트 체크의 로그 입니다." + event_check);
                    if (event_check > 50) {
                        alert(id, name);
                    }
                }
            }
        }
    }

    public void alert(String id, String name) {
        is_alert = true;
        temp_store_id = id;
        if (music != null && vibrator != null) {
            vibrator.vibrate(pattern, 0);
            music.start();
        }
        AlertDialog.Builder b = new AlertDialog.Builder(GeofenceMainActivity.this);

        TextView title = new TextView(GeofenceMainActivity.this);
        title.setText("아이가 영역을 벗어났습니다.");
        title.setBackgroundColor(0xff670000);
        title.setPadding(10, 15, 10, 15);
        title.setGravity(Gravity.CENTER);
        title.setTextColor(Color.WHITE);
        title.setTextSize(20);
        b.setCustomTitle(title);
        b.setMessage("번호 : " + id + "\n이름 : " + name + "\n 벗어난 시간 : " + "\n 누구에게 연락 조치를 취하겠습니까?");
        AlertDialog ad = b.create();
        ad.setCancelable(false); // This blocks the 'BACK' button

        ad.setButton(DialogInterface.BUTTON_POSITIVE, "학부모", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                is_alert = false;
                vibrator.cancel();
                music.pause();

                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:010-5800-4337"));
                startActivity(intent);
            }
        });
        ad.setButton(DialogInterface.BUTTON_NEUTRAL, "취소", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                is_alert = false;

                vibrator.cancel();
                music.pause();
            }
        });

        ad.setButton(DialogInterface.BUTTON_NEGATIVE, "경찰서", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                is_alert = false;
                vibrator.cancel();
                music.pause();
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:112"));
                startActivity(intent);
            }
        });
        ad.show();
    }
*/
    // Saving GeoFence marker with prefs mng
    private void saveGeofence() {
        Log.d(TAG, "saveGeofence()");
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();


        editor.putLong(KEY_GEOFENCE_LAT, Double.doubleToRawLongBits(geoFenceMarker.getPosition().latitude));
        editor.putLong(KEY_GEOFENCE_LON, Double.doubleToRawLongBits(geoFenceMarker.getPosition().longitude));
        editor.apply();
    }

    // Recovering last Geofence marker
    private void recoverGeofenceMarker() {
        Log.d(TAG, "recoverGeofenceMarker");
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);


        if (sharedPref.contains(KEY_GEOFENCE_LAT) && sharedPref.contains(KEY_GEOFENCE_LON)) {
            double lat = Double.longBitsToDouble(sharedPref.getLong(KEY_GEOFENCE_LAT, -1));
            double lon = Double.longBitsToDouble(sharedPref.getLong(KEY_GEOFENCE_LON, -1));
            LatLng latLng = new LatLng(lat, lon);
            markerForGeofence(latLng);
            drawGeofence();
        }
    }

    // Clear Geofence
    private void clearGeofence() {
        Log.d(TAG, "clearGeofence()");
        LocationServices.GeofencingApi.removeGeofences(
                googleApiClient,
                createGeofencePendingIntent()
        ).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {
                if (status.isSuccess()) {
                    //remove drawing
                    removeGeofenceDraw();
                }
            }
        });
    }

    private void removeGeofenceDraw() {
        Log.d(TAG, "removeGeofenceDraw()");
        if (geoFenceMarker != null)
            geoFenceMarker.remove();
        if (geoFenceLimits != null)
            geoFenceLimits.remove();
    }
}
