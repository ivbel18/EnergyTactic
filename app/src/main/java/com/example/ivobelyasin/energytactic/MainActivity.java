package com.example.ivobelyasin.energytactic;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

public class MainActivity extends AppCompatActivity {

    private TextView batteryLevel;
    private TextView latitude;
    private TextView longitude;
    private boolean enableLocationUpdates = true;
    private boolean reduceLocationUpdates = false;

    // Location

    private LocationCallback mLocationCallback;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest mLocationRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        batteryLevel = (TextView) findViewById(R.id.batteryLevel);
        latitude = (TextView) findViewById(R.id.latitude);
        longitude = (TextView) findViewById(R.id.longitude);

        startLocationUpdates();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(batteryChangeReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        startLocationUpdates();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(batteryChangeReceiver);
        stopLocationUpdates();
    }

    private BroadcastReceiver batteryChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context c, Intent i) {
            int level = i.getIntExtra("level", 0);
            batteryLevel.setText(String.valueOf(level));

            if (level < 30 && level > 20) {
                stopLocationUpdates();
                enableLocationUpdates = true;
                reduceLocationUpdates = true;
                startLocationUpdates();
                Toast.makeText(c, "Battery level is low, reducing location updates", Toast.LENGTH_LONG).show();
            }

            if (level < 20) {
                enableLocationUpdates = false;
                stopLocationUpdates();
                Toast.makeText(c, "Battery level is too low, stopping location updates", Toast.LENGTH_LONG).show();
            }

            if (level > 30) {
                enableLocationUpdates = true;
                reduceLocationUpdates = false;
                stopLocationUpdates();
                startLocationUpdates();
            }
        }
    };

    private void startLocationUpdates() {
        if (enableLocationUpdates) {
            int interval = reduceLocationUpdates ? 10000 : 5000;
            int fastestInterval = reduceLocationUpdates ? 6000 : 3000;

            mLocationRequest = LocationRequest.create()
                    .setInterval(interval)
                    .setFastestInterval(fastestInterval)
                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

            mLocationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    super.onLocationResult(locationResult);
                    if (locationResult == null) {
                        return;
                    }

                    Location lastLocation = locationResult.getLastLocation();

                    latitude.setText(String.valueOf(lastLocation.getLatitude()));
                    longitude.setText(String.valueOf(lastLocation.getLongitude()));
                }
            };

            if (mFusedLocationClient == null) {
                mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
                if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    String[] permissions = new String[]{
                            android.Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION
                    };

                    ActivityCompat.requestPermissions(this, permissions, 0x1);
                }

                mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                        mLocationCallback,
                        null /* Looper */);
            }
        }
    }

    private void stopLocationUpdates() {
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }
}
