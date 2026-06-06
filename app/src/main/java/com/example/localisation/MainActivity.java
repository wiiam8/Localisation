package com.example.localisation;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final int REQ_LOC = 100;

    private TextView tvLat, tvLon, tvServer;
    private RequestQueue requestQueue;
    private LocationManager locationManager;
    private double lastLat = 0;
    private double lastLon = 0;

    private final String insertUrl = "http://10.0.2.2/localisation/createPosition.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvLat = findViewById(R.id.tvLat);
        tvLon = findViewById(R.id.tvLon);
        tvServer = findViewById(R.id.tvServer);

        Button btnSend = findViewById(R.id.btnSend);
        Button btnMap = findViewById(R.id.btnMap);

        requestQueue = Volley.newRequestQueue(getApplicationContext());
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        btnSend.setOnClickListener(v -> {
            if (lastLat != 0 || lastLon != 0) {
                addPosition(lastLat, lastLon);
            } else {
                getLastLocationAndSend();
            }
        });

        btnMap.setOnClickListener(v -> startActivity(new Intent(this, MapsActivity.class)));

        askLocationPermissionAndStart();
    }

    private void askLocationPermissionAndStart() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.READ_PHONE_STATE
                    },
                    REQ_LOC);
        } else {
            startGpsUpdates();
        }
    }

    @SuppressLint("MissingPermission")
    private void startGpsUpdates() {
        locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                2000,
                0,
                locationListener
        );

        locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                2000,
                0,
                locationListener
        );

        getLastLocationAndSend();
    }

    private final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(@NonNull Location location) {
            handleLocation(location, true);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            String newStatus;

            switch (status) {
                case LocationProvider.OUT_OF_SERVICE:
                    newStatus = "OUT_OF_SERVICE";
                    break;
                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    newStatus = "TEMPORARILY_UNAVAILABLE";
                    break;
                case LocationProvider.AVAILABLE:
                    newStatus = "AVAILABLE";
                    break;
                default:
                    newStatus = "UNKNOWN";
            }

            String msg = String.format(
                    getResources().getString(R.string.provider_new_status),
                    provider,
                    newStatus
            );

            Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onProviderEnabled(@NonNull String provider) {
            String msg = String.format(getResources().getString(R.string.provider_enabled), provider);
            Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onProviderDisabled(@NonNull String provider) {
            String msg = String.format(getResources().getString(R.string.provider_disabled), provider);
            Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
        }
    };

    private void handleLocation(Location location, boolean sendToServer) {
        double lat = location.getLatitude();
        double lon = location.getLongitude();
        double alt = location.getAltitude();
        float acc = location.getAccuracy();

        lastLat = lat;
        lastLon = lon;

        tvLat.setText("Latitude: " + lat);
        tvLon.setText("Longitude: " + lon);

        String msg = String.format(
                getResources().getString(R.string.new_location),
                lat,
                lon,
                alt,
                acc
        );

        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();

        if (sendToServer) {
            addPosition(lat, lon);
        }
    }

    @SuppressLint("MissingPermission")
    private void getLastLocationAndSend() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            askLocationPermissionAndStart();
            return;
        }

        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        if (location == null) {
            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }

        if (location != null) {
            handleLocation(location, true);
        } else {
            Toast.makeText(this, "Aucune position disponible", Toast.LENGTH_SHORT).show();
        }
    }

    private void addPosition(final double lat, final double lon) {
        StringRequest request = new StringRequest(
                Request.Method.POST,
                insertUrl,
                response -> {
                    tvServer.setText("Serveur: " + response);
                    Toast.makeText(getApplicationContext(), response, Toast.LENGTH_SHORT).show();
                },
                (VolleyError error) -> {
                    tvServer.setText("Serveur: erreur réseau");
                    Toast.makeText(getApplicationContext(), "Erreur réseau", Toast.LENGTH_SHORT).show();
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                HashMap<String, String> params = new HashMap<>();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

                params.put("latitude", String.valueOf(lat));
                params.put("longitude", String.valueOf(lon));
                params.put("date", sdf.format(new Date()));
                params.put("imei", getDeviceIdentifier());

                return params;
            }
        };

        requestQueue.add(request);
    }

    private String getDeviceIdentifier() {
        String androidId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        if (androidId != null && !androidId.trim().isEmpty()) {
            return androidId;
        }

        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                    == PackageManager.PERMISSION_GRANTED) {
                TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

                if (tm != null) {
                    String id = tm.getDeviceId();

                    if (id != null && !id.trim().isEmpty()) {
                        return id;
                    }
                }
            }
        } catch (Exception ignored) {
        }

        return "UNKNOWN_DEVICE";
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQ_LOC && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startGpsUpdates();
        } else {
            Toast.makeText(this, "Permission localisation refusée", Toast.LENGTH_LONG).show();
        }
    }
}