package de.imaze.trackme;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static de.imaze.trackme.Constants.CONNECTION_FAILURE_RESOLUTION_REQUEST;
import static de.imaze.trackme.Constants.GEOFENCE_EXPIRATION_TIME;
import static de.imaze.trackme.Constants.HOME_ID;
import static de.imaze.trackme.Constants.HOME_LATITUDE;
import static de.imaze.trackme.Constants.HOME_LONGITUDE;
import static de.imaze.trackme.Constants.HOME_RADIUS_METERS;
import static de.imaze.trackme.Constants.TAG;
import static de.imaze.trackme.Constants.WORK_ID;
import static de.imaze.trackme.Constants.WORK_LATITUDE;
import static de.imaze.trackme.Constants.WORK_LONGITUDE;
import static de.imaze.trackme.Constants.WORK_RADIUS_METERS;

public class MainActivity extends Activity implements
        ActivityCompat.OnRequestPermissionsResultCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        ResultCallback<Status> {

    /**
     * Id to identify a location permission request.
     */
    private static final int REQUEST_LOCATION = 0;

    // Internal List of Geofence objects. In a real app, these might be provided by an API based on
    // locations within the user's proximity.
    List<Geofence> geofenceList;

    // These will store hard-coded geofences in this sample app.
    private SimpleGeofence homeGeofence;
    private SimpleGeofence workGeofence;

    private LocationServices mLocationService;
    // Stores the PendingIntent used to request geofence monitoring.
    private PendingIntent geofenceRequestIntent;
    protected GoogleApiClient googleApiClient;

    private Button addGeofenceButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        addGeofenceButton = (Button) findViewById(R.id.add_geofences_button);

        buildGoogleApiClient();
        googleApiClient.connect();

        geofenceList = new ArrayList<Geofence>();
        createGeofences();
    }

    private void createGeofences() {
        // Create internal "flattened" objects containing the geofence data.
        homeGeofence = new SimpleGeofence(
                HOME_ID,                // geofenceId.
                HOME_LATITUDE,
                HOME_LONGITUDE,
                HOME_RADIUS_METERS,
                GEOFENCE_EXPIRATION_TIME,
                Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT
        );
        workGeofence = new SimpleGeofence(
                WORK_ID,                // geofenceId.
                WORK_LATITUDE,
                WORK_LONGITUDE,
                WORK_RADIUS_METERS,
                GEOFENCE_EXPIRATION_TIME,
                Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT
        );

        geofenceList.add(homeGeofence.toGeofence());
        geofenceList.add(workGeofence.toGeofence());
    }

    private void buildGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!googleApiClient.isConnecting() || !googleApiClient.isConnected()) {
            googleApiClient.connect();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (googleApiClient.isConnecting() || googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {
        if (null != geofenceRequestIntent) {
            LocationServices.GeofencingApi.removeGeofences(googleApiClient, geofenceRequestIntent);
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // If the error has a resolution, start a Google Play services activity to resolve it.
        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(this,
                        CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                Log.e(TAG, "Exception while resolving connection error.", e);
            }
        } else {
            int errorCode = connectionResult.getErrorCode();
            Log.e(TAG, "Connection to Google Play services failed with error code " + errorCode);
        }
    }

    @Override
    public void onResult(@NonNull Status status) {
        if (status.isSuccess()) {
            Toast.makeText(
                    this,
                    R.string.geofences_added,
                    Toast.LENGTH_SHORT
            ).show();
        } else {
            // todo 02: Get the status code for the error and log it using a user-friendly message.
            // String errorMessage = GeofenceErrorMessages.getErrorString(this, status.getStatusCode());
            Log.e(TAG, String.valueOf(status.getStatusCode()));
        }
    }

    public void addGeofencesButtonHandler(View view) {
        if (!googleApiClient.isConnected()) {
            Toast.makeText(this, "Google API Client not connected!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if the location permission is already available.
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Location permission has not been granted.
            requestLocationPermission();
        } else {
            addGeofences();
        }
    }

    private void addGeofences() {
        try {
            geofenceRequestIntent = getGeofenceTransitionPendingIntent();
            LocationServices.GeofencingApi.addGeofences(googleApiClient, geofenceList,
                    geofenceRequestIntent).setResultCallback(this);
            /*LocationServices.GeofencingApi.addGeofences(
                    googleApiClient,
                    getGeofencingRequest(),
                    getGeofencePendingIntent()
            ).setResultCallback(this);*/
        } catch (SecurityException securityException) {
            // Catch exception generated if the app does not use ACCESS_FINE_LOCATION permission.
            Log.e(TAG, securityException.getMessage());
        }
    }

    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(geofenceList);
        return builder.build();
    }

    /**
     * Create a PendingIntent that triggers GeofenceTransitionIntentService when a geofence
     * transition occurs.
     */
    private PendingIntent getGeofenceTransitionPendingIntent() {
        Intent intent = new Intent(this, GeofenceTransitionsIntentService.class);
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /**
     * Requests the location permission.
     * If the permission has been denied previously, a SnackBar will prompt the user to grant the
     * permission, otherwise it is requested directly.
     */
    private void requestLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // For example if the user has previously denied the permission.
        } else {
            // Location permission has not been granted yet. Request it directly.
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION);
        }
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        if (requestCode == REQUEST_LOCATION) {
            // Received permission result for location permission.;
            // Check if the only required permission has been granted.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Location permission has been granted.
                addGeofences();
            } else {
                // Location permission has been denied.
                // Disable the functionality that depends on this permission.
            }
        }
    }
}