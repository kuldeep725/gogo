package com.iam725.kunal.map_trial;

import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static com.iam725.kunal.map_trial.Login.email;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "LocationActivity";
    private static final long INTERVAL = 1000 * 10;             //time in milliseconds
    private static final long FASTEST_INTERVAL = 1000 * 5;
    private static final String REQUESTING_LOCATION_UPDATES_KEY = "requesting-location-updates";
    private final String USER = "user";
    private final String LATITUDE = "latitude";
    private final String LONGITUDE = "longitude";
    private final String VEHICLE = "vehicle";
    private int checkBusSelection = 0;

    protected GoogleMap mMap;
    protected DatabaseReference mDatabase;
    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    Location mCurrentLocation;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationCallback mLocationCallback;
    private Boolean mRequestingLocationUpdates;
    TextView distance;





    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        distance = (TextView) findViewById(R.id.distance);

        mRequestingLocationUpdates = false;

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        Log.d(TAG, "onCreate ...............................");

        createLocationRequest();

        //show error dialog if GoolglePlayServices not available
        if (!isGooglePlayServicesAvailable()) {
            finish();
        }

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            mCurrentLocation = location;
                            onMapReady(mMap);
                        }
                    }});

        if (null != mCurrentLocation) {
            String lat = String.valueOf(mCurrentLocation.getLatitude());
            String lng = String.valueOf(mCurrentLocation.getLongitude());
            //onMapReady(mMap);
            mDatabase = FirebaseDatabase.getInstance().getReference();
            if (checkBusSelection != 0) {
                String BUS = "b" + checkBusSelection;
                DatabaseReference userDatabase = mDatabase.child(VEHICLE).child(BUS);
                userDatabase.child(LATITUDE).setValue(lat);
                userDatabase.child(LONGITUDE).setValue(lng);

            }

        } else {
            Log.d(TAG, "location is null ...............");
        }

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    // Update UI with location data
                    // ...
                    mCurrentLocation = location;
                    //onMapReady(mMap);
                    if (null != mCurrentLocation) {
                        String lat = String.valueOf(mCurrentLocation.getLatitude());
                        String lng = String.valueOf(mCurrentLocation.getLongitude());
                        mDatabase = FirebaseDatabase.getInstance().getReference();

                        if (checkBusSelection != 0) {

                            String BUS = "b" + checkBusSelection;
                            DatabaseReference userDatabase = mDatabase.child(VEHICLE).child(BUS);
                            userDatabase.child(LATITUDE).setValue(lat);
                            userDatabase.child(LONGITUDE).setValue(lng);

                        }

                    } else {
                        Log.d(TAG, "location is null ...............");
                    }
                }
            }
        };


    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            //return;
        }
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            //return;
        }
        /*double mylatitiude = mMap.getMyLocation().getLatitude();
        double mylongitude = mMap.getMyLocation().getLongitude();
        //mylocation = {10.802874, 76.820238}
        LatLng mylocation = new LatLng(mylatitiude, mylongitude);
        mMap.addMarker(new MarkerOptions().position(mylocation).title("MyLocation"));
        mMap.animateCamera(CameraUpdateFactory.newLatLng(mylocation));*/
        mMap.setMyLocationEnabled(true);
        if (null != mCurrentLocation) {

            mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()))
                    .title("MY LOCATION"));
            mMap.animateCamera(CameraUpdateFactory.newLatLng(
                    new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude())));

        }

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch (view.getId()) {
            case R.id.bus1:
                if (checked) {
                    checkBusSelection = 1;
                    LatLng bus1_location = new LatLng(getLatFromDatabase (checkBusSelection),
                            getLngFromDatabase (checkBusSelection));
                    LatLng myLocation = new LatLng(mCurrentLocation.getLatitude(),
                            mCurrentLocation.getLongitude());
                    double DIFFERENCE = CalculationByDistance(myLocation, bus1_location);
                    String dist = String.valueOf(DIFFERENCE) + "Km";
                    distance.setText(dist);
                    /*//Getting both the coordinates
        LatLng from = new LatLng(fromLatitude,fromLongitude);
        LatLng to = new LatLng(toLatitude,toLongitude);

        //Calculating the distance in meters
        Double distance = SphericalUtil.computeDistanceBetween(from, to);

        //Displaying the distance
        Toast.makeText(this,String.valueOf(distance+" Meters"),Toast.LENGTH_SHORT).show();*/
                    break;
                }
            case R.id.bus2:
                if (checked) {
                    checkBusSelection = 2;
                    break;
                }
            case R.id.bus3:
                if (checked) {
                    checkBusSelection = 3;
                    break;
                }
            case R.id.bus4:
                if (checked) {
                    checkBusSelection = 4;
                    break;
                }
            case R.id.bus5:
                if (checked) {
                    checkBusSelection = 5;
                    break;
                }
        }
    }

    private double getLngFromDatabase(int checkBusSelection) {

        /*Bundle extras = getIntent().getExtras();
        String userId = extras.getString("email");*/
        String userId = email;
        assert userId != null;
        //String[] temp = userId.split("@");
        //userId = temp[0];
        userId = "121601016";
        final double[] lng = new double[1];

        String BUS = "b" + checkBusSelection;
        mDatabase = FirebaseDatabase.getInstance().getReference();
        DatabaseReference userDatabase = mDatabase.child(USER).child(userId).child(BUS);
        userDatabase.child(LONGITUDE).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String longitude = (String) dataSnapshot.getValue();
                lng[0] = Double.parseDouble(longitude);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        return lng[0];

    }

    private double getLatFromDatabase(int checkBusSelection) {

        /*Bundle extras = getIntent().getExtras();
        String userId = extras.getString("email");*/
        String userId = email;
        assert userId != null;
        /*String[] temp = userId.split("@");
        userId = temp[0];*/
        userId = "121601016";
        final double[] lat = new double[1];

        String BUS = "b" + checkBusSelection;
        mDatabase = FirebaseDatabase.getInstance().getReference();
        DatabaseReference userDatabase = mDatabase.child(USER).child(userId).child(BUS);
        userDatabase.child(LATITUDE).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
               String latitude = (String) dataSnapshot.getValue();
               lat[0] = Double.parseDouble(latitude);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        return lat[0];


    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public double CalculationByDistance(LatLng StartP, LatLng EndP) {
        int Radius = 6371;// radius of earth in Km
        double lat1 = StartP.latitude;
        double lat2 = EndP.latitude;
        double lon1 = StartP.longitude;
        double lon2 = EndP.longitude;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2)
                * Math.sin(dLon / 2);
        double c = 2 * Math.asin(Math.sqrt(a));
        double valueResult = Radius * c;
        /*double km = valueResult / 1;
        DecimalFormat newFormat = new DecimalFormat("####");
        int kmInDec = Integer.valueOf(newFormat.format(km));
        double meter = valueResult % 1000;
        int meterInDec = Integer.valueOf(newFormat.format(meter));
        Log.i("Radius Value", "" + valueResult + "   KM  " + kmInDec
                + " Meter   " + meterInDec);*/
        return Radius * c;
    }

    public void pickMe(View view) {

        /*Bundle extras = getIntent().getExtras();
        String userId = extras.getString("email");
        assert userId != null;
        String[] temp = userId.split("@");
        userId = temp[0];*/

        if (null != mCurrentLocation) {
            String lat = String.valueOf(mCurrentLocation.getLatitude());
            String lng = String.valueOf(mCurrentLocation.getLongitude());
            mDatabase = FirebaseDatabase.getInstance().getReference();
            if (checkBusSelection != 0) {
                String BUS = "b" + checkBusSelection;
                DatabaseReference userDatabase = mDatabase.child(VEHICLE).child(BUS);
                userDatabase.child(LATITUDE).setValue(lat);
                userDatabase.child(LONGITUDE).setValue(lng);

            }

        } else {
            Log.d(TAG, "location is null ...............");
        }

    }

    protected void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        /*PendingResult<Status> pendingResult = FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, (com.google.android.gms.location.LocationListener) this);*/
        Log.d(TAG, "Location update started ..............: ");
        mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                mLocationCallback,
                null /* Looper */);
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "Firing onLocationChanged..............................................");
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult (LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    // Update UI with location data
                    // ...
                    mCurrentLocation = location;
                }
            }
        };
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart fired ..............");
        mGoogleApiClient.connect();
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop fired ..............");
        mGoogleApiClient.disconnect();
        Log.d(TAG, "isConnected ...............: " + mGoogleApiClient.isConnected());
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

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "onConnected - isConnected ...............: " + mGoogleApiClient.isConnected());

        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    private boolean isGooglePlayServicesAvailable() {
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (ConnectionResult.SUCCESS == status) {
            return true;
        } else {
            GooglePlayServicesUtil.getErrorDialog(status, this, 0).show();
            return false;
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "Connection failed: " + connectionResult.toString());
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }


    protected void stopLocationUpdates() {
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        Log.d(TAG, "Location update stopped .......................");
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(REQUESTING_LOCATION_UPDATES_KEY,
                mRequestingLocationUpdates);
        // ...
        super.onSaveInstanceState(outState);
    }
}
