package com.example.hayoung.a20190507_1146;


import android.Manifest;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
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
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;

import org.json.JSONArray;

import java.io.BufferedReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;


public class MapsActivity extends FragmentActivity
        implements
        OnMapReadyCallback,
        GoogleMap.OnMapClickListener,
        GoogleMap.OnMarkerClickListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private GoogleMap mMap;     // 맵을 저장할 변수
    // Json 변수
    String myJSON;

    // PHP내에서 연관 배열 인덱스들의 이름들이다.
    private static final String TAG_RESULTS = "results";
    private static final String TAG_ID = "id";
    private static final String TAG_NAME = "name";
    private static final String TAG_LONGITUDE = "longitude";
    private static final String TAG_LAGITUDE = "lagitude";
    ArrayList<Marker> user_list = new ArrayList<Marker>();


    //지오펜스 테스트
    Geofence geofence;
    GeofencingRequest request;
    private GoogleApiClient googleApiClient;
    private Location lastLocation;
    private String textLat, textLong;
    private static final int REQ_PERMISSION = 1500;
    private Marker locationMarker;
    private Marker geoFenceMarker;
    private static final long GEO_DURATION = 60 * 60 * 1000;
    private static final String GEOFENCE_REQ_ID = "My Geofence";
    private static final float GEOFENCE_RADIUS = 50.0f; // in meters
    private PendingIntent geoFencePendingIntent;
    private final int GEOFENCE_REQ_CODE = 0;

    // 구글 맵에 지오펜스 원을 그린다.
    private Circle geoFenceLimits;
    private static final int geo_id = 1001;

    //double test = 0.0;

    //지오펜스 마커를 생성 했을때 그 값을 가져오는 변수
    double geo_longitude = 0.0;
    double geo_latitude = 0.0;

    // JSON 배열을 저장해 놓는 변수
    JSONArray peoples = null;

    // List의 값을 출력하기 위한 해쉬 맵 배열
    ArrayList<HashMap<String, String>> personList;

    //그리기 변수
    PolygonOptions options;

    //진동을 울리기 위한 변수
    Vibrator vibrator;
    long[] pattern = { 0, 200, 500 };

    //소리를 내기 위한 변수
    MediaPlayer music;

    //AlertDiagram 확인 변수
    boolean is_alert = false;

    //저장 아이디
    String temp_store_id = "500000";

    double test = 0.01;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);      // 화면 새로 고정

        //1
        // 구글 API 클라이언트를 생성
        createGoogleApi();

        // HashMap 을 가진 변수를 만든다.
        personList = new ArrayList<HashMap<String, String>>();
        // getdata 메소드

        //showList();
        //getData("http://gpsproject.iptime.org:7070/child/process.php");
        //getData("http://10.9.116.147:81/child/process.php");

        // 진동
        vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
        // 소리
        music = MediaPlayer.create(this, R.raw.siren);
        music.setLooping(true);

        // 프래그먼트(맵) 값을 얻어온다.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        //onMapReady()클래스 출력
        mapFragment.getMapAsync(this);

    }

    // 1.5
    // 지오펜스를 생성한다.
    @NonNull
    private Geofence createGeofence(LatLng latLng, float radius) {
        Log.d("test", "createGeofence");
        return new Geofence.Builder()
                .setRequestId(GEOFENCE_REQ_ID)
                // 지오펜스 아이디
                .setCircularRegion(latLng.latitude, latLng.longitude, radius)
                // 지오펜스 영역 설정
                .setExpirationDuration(Geofence.NEVER_EXPIRE)                          // 만료 시간
                //.setTransitionTypes( Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT ) //지오펜스 옵션
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                //지오펜스 옵션
                .build();
    }


    //2
    //GoogleApiClient.ConnectionCallback들이 연결됨
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i("test", "onConnected()");
        getLastKnownLocation();
    }

    //3
    //5
    // 마지막으로 알려진 위치를 가져온다.
    private void getLastKnownLocation() {
        Log.d("test", "getLastKnownLocation()");
        if (checkPermission()) {
            lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
            if (lastLocation != null) {
                Log.i("test", "LasKnown location. " +
                        "Long: " + lastLocation.getLongitude() +
                        " | Lat: " + lastLocation.getLatitude());
                writeLastLocation();
                startLocationUpdates();
            } else {
                Log.w("test", "No location retrieved yet");
                startLocationUpdates();
            }
        } else askPermission();
    }

    // 4
    // 8
    // 위치에 액세스 할 수있는 권한 확인
    private boolean checkPermission() {
        Log.d("test", "checkPermission()");
        // 아직 퍼미션이 부여되지 않은 경우 허가 요청한다.
        return (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED);
    }

    //6
    // 10
    // 위치 마커를 생성한다.
    /*
    private void markerLocation(LatLng latLng) {
        Log.i("test", "markerLocation(" + latLng + ")");
        String title = latLng.latitude + ", " + latLng.longitude;
        MarkerOptions markerOptions = new MarkerOptions()
                .position(latLng)
                .title(title);
        if (mMap != null) {
            // 앞쪽 마커를 제거해주세요
            if (locationMarker != null)
                locationMarker.remove();
            locationMarker = mMap.addMarker(markerOptions);
            float zoom = 14f;
            //CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, zoom);
            //mMap.animateCamera(cameraUpdate);
        }
    }*/

    //7
    // 위치 업데이트를 시작합니다.
    private void startLocationUpdates() {
        Log.i("test", "startLocationUpdates()");
        locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL)
                .setFastestInterval(FASTEST_INTERVAL);

        if (checkPermission())
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }

    //9
    @Override
    public void onLocationChanged(Location location) {
        Log.d("test", "onLocationChanged [" + location + "]");

        location.setLatitude(location.getLatitude());
        location.setLongitude(location.getLongitude());
        lastLocation = location;
        writeActualLocation(location);
    }

    // Write location coordinates on UI
    private void writeActualLocation(Location location) {
        textLat = String.valueOf(location.getLatitude());
        textLong = String.valueOf(location.getLongitude());

        //markerLocation(new LatLng(location.getLatitude(), location.getLongitude()));
    }

    private PendingIntent createGeofencePendingIntent() {
        Log.d("test", "createGeofencePendingIntent");
        if (geoFencePendingIntent != null) {
            return geoFencePendingIntent;
        } else {
            Intent intent = new Intent("android.bluetooth.device.action.ACL_CONNECTED");
            //Intent intent = new Intent(this, GeofenceTrasitionService.class);
            //startService(intent);
            return PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }
    }


    // 디바이스의 모니터링리스트에 생성 된 GeofenceRequest를 추가
    private void addGeofence(GeofencingRequest request) {
        Log.d("test", "addGeofence");
        if (checkPermission())
            LocationServices.GeofencingApi
                    .addGeofences(googleApiClient, request,
                            createGeofencePendingIntent())
                    .setResultCallback(new ResultCallback<Status>() {
                        @Override
                        public void onResult(@NonNull Status status) {
                            Log.i("test", "onResult: " + status);
                            if (status.isSuccess()) {
                                drawGeofence();
                            } else {
                                // 실패에 관하여 알림
                            }
                        }
                    });
    }

    // 구글 맵에 지오펜스 원을 그린다.
    private void drawGeofence() {

        Log.d("test", "drawGeofence()");

        if (geoFenceLimits != null)
            geoFenceLimits.remove();

        CircleOptions circleOptions = new CircleOptions()
                .center(geoFenceMarker.getPosition())
                .strokeColor(Color.argb(50, 70, 70, 70))
                .fillColor(Color.argb(100, 150, 150, 150))
                .radius(GEOFENCE_RADIUS);
        geoFenceLimits = mMap.addCircle(circleOptions);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, geo_id, 1, "영역 그리기");
        menu.add(0, 20, 2, "모니터링 작동");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case geo_id:
                startGeofence();
                return true;
            case 20:
                temp_store_id = "999999";
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    // 지오펜스 생성 프로세스를 시작한다.
    private void startGeofence() {
        Log.i("test", "startGeofence()");
        if (geoFenceMarker != null) {
            // 마커의 값을 가져온다.
            geo_latitude = geoFenceMarker.getPosition().latitude;
            geo_longitude = geoFenceMarker.getPosition().longitude;

            Geofence geofence = createGeofence(geoFenceMarker.getPosition(), GEOFENCE_RADIUS);
            GeofencingRequest geofenceRequest = createGeofenceRequest(geofence);
            addGeofence(geofenceRequest);
        } else {
            Log.e("test", "Geofence marker is null");
        }
    }

    // GeofencingRequest를 생성
    @NonNull
    private GeofencingRequest createGeofenceRequest(Geofence geofence) {
        Log.d("test", "createGeofenceRequest");
        return new GeofencingRequest.Builder()
                // 지오펜스가 생성되어질때 알림 트리거
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofence(geofence)                    //  지오펜스를 추가
                .build();
    }

    // 구글 API 인스턴스를 생성
    private void createGoogleApi() {
        Log.d("test", "createGoogleApi()");
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    private LocationRequest locationRequest;
    //  초 단위로 정의 합니다.
    // 이 숫자는 매우 작습니다. 디버그에만 사용해 주세요.
    private final int UPDATE_INTERVAL = 1000;
    private final int FASTEST_INTERVAL = 900;

    // 아래가 좀더 현실적인 구성
    //private final int UPDATE_INTERVAL =   3 * 60 * 1000; // 3 minutes
    //private final int FASTEST_INTERVAL = 30 * 1000;  // 30 secs

    private void writeLastLocation() {
        writeActualLocation(lastLocation);
    }

    // 퍼미션을 요청한다.
    private void askPermission() {
        Log.d("test", "askPermission()");
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQ_PERMISSION);
    }

    //요청한 권한에 대한 사용자의 응답 확인
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d("test", "onRequestPermissionsResult()");
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

    // 앱이 권한이 없이 동작하는 경우
    private void permissionsDenied() {
        Log.w("test", "permissionsDenied()");
    }

    // 맵을 클릭 했을때
    @Override
    public void onMapClick(LatLng latLng) {
        Log.d("test", "onMapClick(" + latLng + ")");
        markerForGeofence(latLng);
    }

    // 지오펜스를 생성하기 위한 마커를 만듬
    private void markerForGeofence(LatLng latLng) {
        Log.i("test", "markerForGeofence(" + latLng + ")");
        String title = latLng.latitude + ", " + latLng.longitude;
        // 마커 옵션을 정의
        MarkerOptions markerOptions = new MarkerOptions()
                .position(latLng)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
                .title(title);
        if (mMap != null) {
            // 마지막 지오펜스 마커를 지운다.
            if (geoFenceMarker != null)
                geoFenceMarker.remove();
            geoFenceMarker = mMap.addMarker(markerOptions);
        }
    }


    // 마커를 클릭했을떄
    @Override
    public boolean onMarkerClick(Marker marker) {
        Log.d("test", "onMarkerClickListener: " + marker.getPosition());
        return false;
    }

    //GoogleApiClient.ConnectionCallback들이 일시 중지됨
    @Override
    public void onConnectionSuspended(int i) {
        Log.w("test", "onConnectionSuspended()");
    }

    //GoogleApiClient.OnConnectionFailedListener 연결 실패
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.w("test", "onConnectionFailed()");
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
        vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
        // 소리
        music = MediaPlayer.create(this, R.raw.siren);
        music.setLooping(true);
        super.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // 액티비티가 시작될 때 구글 API 클라이언트 연결을 호출한다.
        googleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        // 액티비티가 시작될 때 구글 API 클라이언트의 연결을 끊는다.
        googleApiClient.disconnect();
    }

    @Override
    public void onMapReady(GoogleMap map) {
  /*    // 마커와 카메라 이동 메소드들
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));*/

        mMap = map;

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
        mMap.setMyLocationEnabled(true);

        // 나의 위치 찾기
        MyLocation.LocationResult locationResult = new MyLocation.LocationResult() {
            @Override
            //카메라 이동
            public void gotLocation(Location location) {
                final LatLng move = new LatLng(location.getLatitude(), location.getLongitude());
                // 위도와 경도를 얻어온다.
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(move, 15));
                // 카메라 레벨을 15로 하고 위도와 경도값으로 카메라를 이동을 한다.

                //현재 위치 텍스트로 출력
                String msg = "lon: " + location.getLongitude() + " -- lat: " + location.getLatitude();
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();

                //drawMarker(location);         // 현재위치와 같이 마커를 출력해준다.
            }
        };

        MyLocation myLocation = new MyLocation();
        myLocation.getLocation(getApplicationContext(), locationResult);  //GPS를 이용해 현재 위치를
        // 얻어온다.

        map.setOnMapClickListener(this);                        // 맵 클릭 리스너
        map.setOnMarkerClickListener(this);                     // 마커 클릭 리스너

        mMap.setOnMapLongClickListener(new OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                Log.i("test", "클릭");
            }
        });
    }


    /*
마커를 이용하여 아두이노의 위치를 받아온다. 아두이노는 서버로 위도와 경도를 저장하는데
스마트폰은 서버에서 아두이노 값을 받아와서 출력해 줄수 있다.
 */
    //   private void drawMarker(Location location) {
    private void drawMarker(String name, double lat, double lon, String id) {

        //person = parsing.callJsonParsing();

        // 위도와 경도를 받아온다. 현재는 현재 위치를 받아오지만 아두이노값을 대체할수 있다.
        //LatLng currentPosition = new LatLng(lat+test, lon+test);
        LatLng currentPosition = new LatLng(lat , lon );
        //currentPosition 위치로 카메라 중심을 옮기고 화면 줌을 조정한다. 줌범위는 2~21, 숫자클수록 확대
        //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom( currentPosition, 17));
        //mMap.animateCamera(CameraUpdateFactory.zoomTo(17), 2000, null);
        Marker m;
        //마커 추가
        m = mMap.addMarker(new MarkerOptions().position(currentPosition).
                icon(BitmapDescriptorFactory.fromResource(R.drawable.child)).title(name + "님 의 현재위치"));
        check_location(lat, lon, id, name);
        user_list.add(m);

        //test = test + 0.0001;
    }

    public void check_location(double lat, double lon, String id, String name){
        Log.i("test", "Lat" + lat + "lon : " + lon);
        if(!is_alert) {
            if (!temp_store_id.equals(id)){
                if (geo_latitude != 0.0 && geo_latitude != 0.0) {
                    double event_check = calDistance(geo_latitude, geo_longitude, lat , lon );
                    Log.i("test", "이벤트 체크의 로그 입니다." + event_check);
                    if (event_check > 50) {
                        alert(id, name);
                    }
                }
            }
        }
    }

    public void alert(String id, String name){
        is_alert=true;
        temp_store_id = id;
        if(music != null && vibrator != null) {
            vibrator.vibrate(pattern,0);
            music.start();
        }
        AlertDialog.Builder b = new AlertDialog.Builder(MapsActivity.this);

        TextView title = new TextView(MapsActivity.this);
        title.setText("아이가 영역을 벗어났습니다.");
        title.setBackgroundColor(0xff670000);
        title.setPadding(10, 15, 10, 15);
        title.setGravity(Gravity.CENTER);
        title.setTextColor(Color.WHITE);
        title.setTextSize(20);
        b.setCustomTitle(title);
        b.setMessage("번호 : " + id + "\n이름 : " + name + "\n 벗어난 시간 : " + GetDateNow() + "\n 누구에게 연락 조치를 취하겠습니까?");
        AlertDialog ad = b.create();
        ad.setCancelable(false); // This blocks the 'BACK' button

        ad.setButton(DialogInterface.BUTTON_POSITIVE,"학부모", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int which)
            {
                is_alert=false;
                vibrator.cancel();
                music.pause();

                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:010-5800-4337"));
                startActivity(intent);
            }
        });
        ad.setButton(DialogInterface.BUTTON_NEUTRAL,"취소", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int which)
            {
                is_alert=false;

                vibrator.cancel();
                music.pause();
            }
        });

        ad.setButton(DialogInterface.BUTTON_NEGATIVE,"경찰서", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int which)
            {
                is_alert=false;
                vibrator.cancel();
                music.pause();
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:112"));
                startActivity(intent);
            }
        });
        ad.show();
    }

    public String GetDateNow(){
        // 1. 위 코드를 2줄로 줄였다.
        SimpleDateFormat sdfNow = new SimpleDateFormat("HH 시 mm 분 ss 초");
        return sdfNow.format(new Date(System.currentTimeMillis()));
    }

    public void getData(String url) {
        class GetDataJSON extends AsyncTask<String, Void, String> {

            @Override
            protected String doInBackground(String... params) {

                String uri = params[0];

                BufferedReader bufferedReader = null;
                try {
                    /*URL url = new URL(uri);
                    // URL 연결 (웹페이지 URL 연결.)
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();*/
                    StringBuilder sb = new StringBuilder();
                    /*
                    bufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream()));

                    String json;
                    while ((json = bufferedReader.readLine()) != null) {
                        sb.append(json + "\n");
                    }

                    Thread.sleep(5000);

                    Log.i("check","계속 진행중");*/

                    return sb.toString().trim();
                    //return null;
                } catch (Exception e) {
                    return null;
                }

            }

            @Override
            protected void onPostExecute(String result) {
                myJSON = result;
                //기존 마커 지우기
                RemoveUser();
                showList();

                getData("웹서버 도메인");
                //getData("http://gpsproject.iptime.org:7070/child/process.php");
                //getData("http://10.9.116.147:81/child/process.php");
            }
        }
        //GetDataJSON g = new GetDataJSON();
        //g.execute(url);
    }

    protected void RemoveUser(){
       /* for(int i = 0; i < user_list.size(); i++) {
            (user_list.get(i)).remove();
        }*/
        (user_list.get(0)).remove();
    }

    protected void showList() {

            /*JSONObject jsonObj = new JSONObject(myJSON);
            peoples = jsonObj.getJSONArray(TAG_RESULTS);


            for (int i = 0; i < peoples.length(); i++) {
                JSONObject c = peoples.getJSONObject(i);

                String id = c.getString(TAG_ID);
                String name = c.getString(TAG_NAME);
                double longitude = c.getDouble(TAG_LONGITUDE);
                double lagitude = c.getDouble(TAG_LAGITUDE);
*/
        String id = "2";
        String name = "홍길동";
        double longitude = 36.7636;
        double lagitude = 127.0731;



        test = test + 0.01;

        drawMarker(name, longitude + test, lagitude + test , id);



    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    // 경위도로 위치 찾기 1 : 지오펜스 중심지 2 : 목적의 위치
    public double calDistance(double lat1, double lon1, double lat2, double lon2) {

        double theta, dist;
        theta = lon1 - lon2;
        dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1))
                * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);

        dist = dist * 60 * 1.1515;
        dist = dist * 1.609344;    // 단위 mile 에서 km 변환.
        dist = dist * 1000.0;      // 단위  km 에서 m 로 변환

        return dist;
    }

    // 주어진 도(degree) 값을 라디언으로 변환
    private double deg2rad(double deg) {
        return (double) (deg * Math.PI / (double) 180d);
    }

    // 주어진 라디언(radian) 값을 도(degree) 값으로 변환
    private double rad2deg(double rad) {
        return (double) (rad * (double) 180d / Math.PI);
    }
}
