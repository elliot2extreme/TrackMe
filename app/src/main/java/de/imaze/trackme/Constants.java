package de.imaze.trackme;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.maps.model.LatLng;

import java.util.HashMap;

public class Constants {

    private Constants() {
    }

    public static final String TAG = "TrackMeApp";

    // Request code to attempt to resolve Google Play services connection failures.
    public final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    // For the purposes of this demo, the geofences are hard-coded and should not expire.
    // An app with dynamically-created geofences would want to include a reasonable expiration time.
    public static final long GEOFENCE_EXPIRATION_TIME = Geofence.NEVER_EXPIRE;

    // Geofence parameters for my home area.
    public static final String HOME_ID = "1";
    public static final double HOME_LATITUDE = 49.469700;
    public static final double HOME_LONGITUDE = 8.595900;
    public static final float HOME_RADIUS_METERS = 100.0f;

    // Geofence parameters for my work place.
    public static final String WORK_ID = "2";
    public static final double WORK_LATITUDE = 49.502535;
    public static final double WORK_LONGITUDE = 8.495314;
    public static final float WORK_RADIUS_METERS = 100.0f;
}