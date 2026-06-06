package com.example.localisation;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

public class MapsActivity extends AppCompatActivity {

    private MapView map;
    private RequestQueue requestQueue;

    private final String showUrl = "http://10.0.2.2/localisation/showPositions.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Configuration.getInstance().load(
                getApplicationContext(),
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
        );

        setContentView(R.layout.activity_maps);

        map = findViewById(R.id.map);
        requestQueue = Volley.newRequestQueue(getApplicationContext());

        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);
        map.getController().setZoom(12.0);
        map.getController().setCenter(new GeoPoint(31.6295, -7.9811));

        setUpMap();
    }

    private void setUpMap() {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST,
                showUrl,
                null,
                response -> {
                    try {
                        JSONArray positions = response.getJSONArray("positions");

                        map.getOverlays().clear();

                        for (int i = 0; i < positions.length(); i++) {
                            JSONObject position = positions.getJSONObject(i);

                            double lat = position.getDouble("latitude");
                            double lon = position.getDouble("longitude");
                            String date = position.getString("date");
                            String imei = position.getString("imei");

                            GeoPoint point = new GeoPoint(lat, lon);

                            Marker marker = new Marker(map);
                            marker.setPosition(point);
                            marker.setTitle("Position");
                            marker.setSubDescription("Date: " + date + "\nIMEI: " + imei);
                            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

                            map.getOverlays().add(marker);

                            if (i == 0) {
                                map.getController().animateTo(point);
                                map.getController().setZoom(15.0);
                            }
                        }

                        map.invalidate();

                    } catch (Exception e) {
                        Toast.makeText(this, "Erreur JSON", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Erreur chargement positions", Toast.LENGTH_SHORT).show()
        );

        requestQueue.add(jsonObjectRequest);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (map != null) {
            map.onResume();
            setUpMap();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (map != null) {
            map.onPause();
        }
    }
}