package com.nirenorie.didit;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback
        , GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private PendingIntent mGeofencePendingIntent;
    private Circle mCircle;
    private CircleOptions mCircleOptions = new CircleOptions()
            .radius(Constants.GEOFENCE_RADIUS)
            .strokeWidth(10)
            .strokeColor(Color.BLACK)
            .fillColor(Color.argb(50, 255, 0, 0));

    private MarkerOptions mMarkerOptions = new MarkerOptions().title("Geofence Location").draggable(true);
    private SharedPreferences mPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPrefs = getPreferences(MODE_PRIVATE);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    private GeofencingRequest getGeofencingRequest(double lat, double lon) {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER | GeofencingRequest.INITIAL_TRIGGER_EXIT);
        builder.addGeofence(new Geofence.Builder()
                .setRequestId(Constants.GEOFENCE_REQUEST_ID)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                .setCircularRegion(lat, lon, Constants.GEOFENCE_RADIUS)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .build());
        return builder.build();
    }

    private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (mGeofencePendingIntent == null) {
            Intent intent = new Intent(this, GeofenceTransitionsIntentService.class);
            // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
            // calling addGeofences() and removeGeofences().
            mGeofencePendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }
        return mGeofencePendingIntent;
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
        mMap.moveCamera(CameraUpdateFactory.zoomTo(Constants.MAP_DEFAULT_ZOOM));
        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {

            }

            @Override
            public void onMarkerDrag(Marker marker) {

            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                LatLng position = marker.getPosition();
                saveGeofencePosition(position);
                moveGeofence(position);
            }
        });
    }

    @Override
    public void onConnected(Bundle bundle) {
        String lat = mPrefs.getString(Constants.PREF_GEOFENCE_LAT, "");
        String lon = mPrefs.getString(Constants.PREF_GEOFENCE_LON, "");

        if(lat.equals("") || lon.equals("")){
            LatLng lastLatLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
            saveGeofencePosition(lastLatLng);
            mMarkerOptions.position(lastLatLng);
            mMap.addMarker(mMarkerOptions);
            mMap.moveCamera(CameraUpdateFactory.newLatLng(lastLatLng));
            moveGeofence(lastLatLng);
        } else {
            LatLng latLng = loadGeofencePosition();
            mMarkerOptions.position(latLng);
            mMap.addMarker(mMarkerOptions);
            moveCircle(latLng);
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        }
    }

    private void saveGeofencePosition(LatLng pos){
        mPrefs.edit().putString(Constants.PREF_GEOFENCE_LAT, Double.toString(pos.latitude))
                .putString(Constants.PREF_GEOFENCE_LON, Double.toString(pos.longitude))
                .apply();
    }

    private LatLng loadGeofencePosition(){
        String lat = mPrefs.getString(Constants.PREF_GEOFENCE_LAT, "");
        String lon = mPrefs.getString(Constants.PREF_GEOFENCE_LON, "");
        return new LatLng(Double.parseDouble(lat), Double.parseDouble(lon));
    }

    private void moveCircle(LatLng pos){
        if(mCircle != null){
            mCircle.remove();
        }
        mCircleOptions.center(pos);
        mCircle = mMap.addCircle(mCircleOptions);
    }

    private void moveCircle(Double lat, Double lon){
        moveCircle(new LatLng(lat, lon));
    }

    @Override
    public void onConnectionSuspended(int i) {
        Toast.makeText(this, "Connection Suspended", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Toast.makeText(this, "Connection Failed", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    private void moveGeofence(double lat, double lon){
        List<String> ids = new ArrayList<String>();
        ids.add(Constants.GEOFENCE_REQUEST_ID);

        LocationServices.GeofencingApi.removeGeofences(mGoogleApiClient, ids)
            .setResultCallback(new ResultCallback<Status>() {
                @Override
                public void onResult(Status status) {
                    switch (status.getStatusCode()) {
                        case GeofenceStatusCodes.SUCCESS:
                            Toast.makeText(MapsActivity.this,
                                    "Geofence Removed", Toast.LENGTH_SHORT).show();
                            break;
                        default:
                            Toast.makeText(MapsActivity.this
                                    , "Could not remove geofence"
                                    , Toast.LENGTH_SHORT).show();
                            break;
                    }
                }
            });

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

            LocationServices.GeofencingApi.addGeofences(
                    mGoogleApiClient,
                    getGeofencingRequest(lat, lon),
                    getGeofencePendingIntent()
            ).setResultCallback(new ResultCallback<Status>() {
                @Override
                public void onResult(Status status) {
                    switch (status.getStatusCode()) {
                        case GeofenceStatusCodes.SUCCESS:
                            Toast.makeText(MapsActivity.this,
                                    "Geofence Added", Toast.LENGTH_SHORT).show();
                            break;
                        default:
                            Toast.makeText(MapsActivity.this
                                    , "Could not add geofence"
                                    , Toast.LENGTH_SHORT).show();
                            break;
                    }
                }
            });
        }

        moveCircle(lat, lon);
    }

    private void moveGeofence(LatLng latLng){
        moveGeofence(latLng.latitude, latLng.longitude);
    }
}
