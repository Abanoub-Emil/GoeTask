package com.champion.bero.goetask;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.model.Direction;
import com.akexorcist.googledirection.model.Info;
import com.akexorcist.googledirection.model.Leg;
import com.akexorcist.googledirection.model.Route;
import com.akexorcist.googledirection.util.DirectionConverter;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.location.LocationManager.GPS_PROVIDER;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private double lat;
    private double lang;
    private LocationManager locationManager;
    @BindView(R.id.userName)
    TextView userName;
    @BindView(R.id.userImage)
    ImageView myImage;
    @BindView(R.id.locate)
    Button locate;
    @BindView(R.id.latitude)
    EditText etLat;
    @BindView(R.id.longitude)
    EditText etLang;
    private MarkerOptions currentMarker = new MarkerOptions();
    private MarkerOptions desiredMarker = new MarkerOptions();
    private boolean flag = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        ButterKnife.bind(this);

        // get facebook data by id and use picasso to display image
        Intent intent = getIntent();
        userName.setText(intent.getStringExtra("userName"));
        String id = intent.getStringExtra("id");
        Picasso.with(this)
                .load("https://graph.facebook.com/" + id + "/picture?type=large")
                .into(myImage);


        locate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                    Toast.makeText(MapsActivity.this, "Please Enable Location", Toast.LENGTH_SHORT).show();
                }
                final LatLng currentLatlang = new LatLng(lat, lang);
                double myLat = stringToDouble(etLat.getText().toString());
                double myLang = stringToDouble(etLang.getText().toString());
                final LatLng avg = new LatLng((lat+myLat)/2,(lang+myLang)/2);
                final LatLng desiredLatlang = new LatLng(myLat, myLang);
                GoogleDirection.withServerKey("AIzaSyAzvgH0AI818lOhOpU2KQfLP8XUnjDN3Iw")
                        .from(currentLatlang)
                        .to(desiredLatlang)
                        .execute(new DirectionCallback() {
                            @Override
                            public void onDirectionSuccess(Direction direction, String rawBody) {
                                if (direction.getRouteList().size() != 0) {
                                    mMap.clear();
                                    Route route = direction.getRouteList().get(0);
                                    Leg leg = route.getLegList().get(0);
                                    Info distanceInfo = leg.getDistance();
                                    String distance = distanceInfo.getValue();
                                    int myDis = Integer.parseInt(distance);
                                    Toast.makeText(MapsActivity.this, ""+distance, Toast.LENGTH_SHORT).show();
                                    ArrayList<LatLng> directionPositionList = leg.getDirectionPoint();
                                    PolylineOptions polylineOptions = DirectionConverter.createPolyline(MapsActivity.this, directionPositionList, 5, Color.RED);
                                    mMap.addPolyline(polylineOptions);
                                    mMap.addMarker(desiredMarker.position(desiredLatlang).title("Desired Marker"));
                                    mMap.addMarker(currentMarker.position(currentLatlang).title("Home Marker"));
                                    mMap.moveCamera(CameraUpdateFactory.newLatLng(avg));
                                    if(myDis>200000)
                                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(avg, 8.0f), 8, null);
                                    else if(myDis>100000)
                                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(avg, 9.0f), 9, null);
                                    else if (myDis>50000)
                                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(avg, 9.5f), 10, null);
                                    else if (myDis>25000)
                                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(avg, 10.2f), 11, null);
                                    else
                                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(avg, 11.0f), 11, null);

                                    flag = false;
                                }
                            }

                            @Override
                            public void onDirectionFailure(Throwable t) {

                            }
                        });
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();

        // get user's location
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1993);

            return;
        }

        if (!locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            Toast.makeText(this, "Please Enable Location", Toast.LENGTH_SHORT).show();
        }

        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 10f, new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                lat = location.getLatitude();
                lang = location.getLongitude();
                if (flag)
                    onMapReady(mMap);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        });

    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng current = new LatLng(lat, lang);
        mMap.addMarker(currentMarker.position(current).title("Home Marker"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(current));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(current, 12.0f), 12, null);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);
    }


    private double stringToDouble(String string) {

        double myDouble = Double.parseDouble(string);
        return myDouble;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1993: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1993);

                }
                return;
            }

        }
    }
}
