package com.example.hayoung.a20190507_1146;


import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import java.util.TimerTask;

public class MyLocation {

    long minTime = 5000;
    float minDistance = 0;
    LocationManager manager;
    LocationResult locationResult;
    boolean gps_enabled = false;
    boolean network_enabled = false;
    double latitude;
    double longitude;

    public boolean getLocation(Context context, LocationResult result) {
        // 나는 사용자 코드에 MyLocation으로부터 위치 값을 pass하기 위해서 LocationResult 콜백 클래스를 사용한다.
        locationResult = result;
        if (manager == null)
            // 위치관리자 로케이션 메니저를 참조
            manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        //GPS 또는 NETWORK 이용 가능한지 확인 하고 그 결과를 변수에 저장
        try {
            gps_enabled = manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
        }
        try {
            network_enabled = manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {
        }

        // 만약 이용 가능하지 않다면 false 리턴
        if (!gps_enabled && !network_enabled)
            return false;

        // 이 로케이션 메니저는 리스너를 알수 있게 됨
        // 새로 정의한 GPSListener 객체 생성
        //MylocationListenerGps locationListenerGps = new MylocationListenerGps();

        // 만약 GPS가 사용이 가능하다면
        if (gps_enabled) {
            try {
                // 위치 관리자 객체의 requestLocationUpdates()메소드를 호출하여 위치정보 수신 시작
                // 내 위치를 확인해 주세요(어떤걸 사용할꺼야?, 미니멈 타임, 거리의 따라 업데이트 해줘, 어떤 위치 정보를 받을수 있게 해줌)
                manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTime, minDistance, locationListenerGps);
            } catch (SecurityException e) {
                Log.e("PERMISSION_EXCEPTION", "PERMISSION_NOT_GRANTED");
            }
        }
        if (network_enabled) {

            try {

                manager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, minTime, minDistance, locationListenerNetwork);

            } catch (SecurityException e) {
                Log.e("PERMISSION_EXCEPTION", "PERMISSION_NOT_GRANTED");
            }
        }
        //timer1 = new Timer();
        //timer1.schedule(new GetLastLocation(), 2000, 5000);
        return true;
    }

    /*// LocationListener를 구현하는 새로운 GPSListener클래스 정의
    class MylocationListenerGps implements LocationListener{
        @Override
        // 위치 정보가 전달될 때 호출되는 onLocationChanged메소드 정의
        public void onLocationChanged(Location location) {
            // 현재 내 위치를 찾는다.
            latitude = location.getLatitude();
            longitude = location.getLongitude();
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    }*/

    // 첫번째 호출
    LocationListener locationListenerGps = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            locationResult.gotLocation(location);

            try {

                manager.removeUpdates(this);
                manager.removeUpdates(locationListenerNetwork);

            } catch (SecurityException e) {
                Log.e("PERMISSION_EXCEPTION", "PERMISSION_NOT_GRANTED");
            }
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    };
    //곧 바로 호출됨
    LocationListener locationListenerNetwork = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            locationResult.gotLocation(location);

            try {
                // 위치 정보 갱신 불가하게 만듬
                manager.removeUpdates(this);
                manager.removeUpdates(locationListenerGps);

            } catch (SecurityException e) {
                Log.e("PERMISSION_EXCEPTION", "PERMISSION_NOT_GRANTED");
            }
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    };

    class GetLastLocation extends TimerTask {
        @Override
        public void run() {

            try {
                manager.removeUpdates(locationListenerGps);
                manager.removeUpdates(locationListenerNetwork);

            } catch (SecurityException e) {
                Log.e("PERMISSION_EXCEPTION", "PERMISSION_NOT_GRANTED");
            }

            Location net_loc = null, gps_loc = null;

            try {

                if (gps_enabled)
                    gps_loc = manager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (network_enabled)
                    net_loc = manager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

            } catch (SecurityException e) {
                Log.e("PERMISSION_EXCEPTION", "PERMISSION_NOT_GRANTED");
            }




            //if there are both values use the latest one
            if (gps_loc != null && net_loc != null) {
                if (gps_loc.getTime() > net_loc.getTime())
                    locationResult.gotLocation(gps_loc);
                else
                    locationResult.gotLocation(net_loc);
                return;
            }

            if (gps_loc != null) {
                locationResult.gotLocation(gps_loc);
                return;
            }
            if (net_loc != null) {
                locationResult.gotLocation(net_loc);
                return;
            }
            locationResult.gotLocation(null);
        }
    }

    // MapsActivity.java에서 처음 온다.
    public static abstract class LocationResult {
        public abstract void gotLocation(Location location);
    }
}
