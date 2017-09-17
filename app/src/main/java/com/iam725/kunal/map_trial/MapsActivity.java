package com.iam725.kunal.map_trial;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

        private static final String TAG = "LocationActivity";
        private static final long INTERVAL = 1000 * 10;             //time in milliseconds
        private static final long FASTEST_INTERVAL = 1000 * 5;
        private static final String REQUESTING_LOCATION_UPDATES_KEY = "requesting-location-updates";
        private static final String INTERMEDIATE = "intermediate" ;
        private final String USER = "user";
        private final String LATITUDE = "latitude";
        private final String LONGITUDE = "longitude";
        private final String VEHICLE = "vehicle";
        private int checkBusSelection = 0;
        private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;
        private Context context;
        boolean isGPSEnabled = false;
        boolean isNetworkEnabled = false;
        boolean canGetLocation = false;
        private String userId;
        String BUS;
        String key;

        protected GoogleMap mMap;
        protected DatabaseReference mDatabase;
        LocationRequest mLocationRequest;
        GoogleApiClient mGoogleApiClient;
        Location mCurrentLocation = null;
        private FusedLocationProviderClient mFusedLocationClient;
        private LocationCallback mLocationCallback;
        private Boolean mRequestingLocationUpdates;
        TextView distance;
        TextView duration;
        protected LocationManager locationManager;

        protected void createLocationRequest() {
                mLocationRequest = new LocationRequest();
                mLocationRequest.setInterval(INTERVAL);
                mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
                mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        }

        @Override
        public boolean onCreateOptionsMenu(Menu menu) {
                MenuInflater inflater = getMenuInflater();

                inflater.inflate(R.menu.menu_item, menu);
                return super.onCreateOptionsMenu(menu);
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {

                switch (item.getItemId()) {

                        case R.id.LogOut:
                                Toast.makeText(MapsActivity.this, R.string.logging_out, Toast.LENGTH_LONG).show();
                                signingOut();

                }
                return super.onOptionsItemSelected(item);

        }

        private void signingOut() {

                Intent i = new Intent(MapsActivity.this, Login.class);
                FirebaseAuth.getInstance().signOut();
                startActivity(i);
                finish();

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
                duration = (TextView) findViewById(R.id.time);
                mDatabase = FirebaseDatabase.getInstance().getReference();

                // Find the toolbar view inside the activity layout
                //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
                // Sets the Toolbar to act as the ActionBar for this Activity window.
                // Make sure the toolbar exists in the activity and is not null
                //setSupportActionBar( toolbar);

                /*try
                {
                        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
                        isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                        isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

                        if(!isNetworkEnabled && !isGPSEnabled)
                        {

                        }
                        else
                        {
                                this.canGetLocation = true;
                                if (isNetworkEnabled)
                                {
                                        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BETWEEN_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                                        Log.d("Network", "Network Enabled");
                                        if(locationManager != null)
                                        {
                                                location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                                                if(location != null)
                                                {
                                                        latitude = location.getLatitude();
                                                        longitude = location.getLongitude();
                                                }
                                        }
                                }
                                if(isGPSEnabled)
                                {
                                        if(location == null)
                                        {
                                                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000 * 10, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                                                Log.d("GPS", "GPS Enabled");
                                                if(locationManager != null)
                                                {
                                                        location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                                                        if (location != null)
                                                        {
                                                                latitude = location.getLatitude();
                                                                longitude = location.getLongitude();
                                                        }
                                                }
                                        }
                                }
                        }
                }
                catch (Exception e)
                {
                        e.printStackTrace();
                }*/

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
                                }
                        });
                // Log.d(TAG, "mFusedLocationClient -> Latitude : " + mCurrentLocation.getLatitude() + "Longitude : " + mCurrentLocation.getLongitude());
                if (null != mCurrentLocation) {
                        String lat = String.valueOf(mCurrentLocation.getLatitude());
                        String lng = String.valueOf(mCurrentLocation.getLongitude());
                        //onMapReady(mMap);
                        mDatabase = FirebaseDatabase.getInstance().getReference();
                        if (checkBusSelection != 0) {
                                String BUS = "b" + checkBusSelection;
                                DatabaseReference userDatabase = mDatabase.child(VEHICLE).child(BUS);
                                Map<String, String> userData = new HashMap<>();
                                userData.put(LATITUDE, lat);
                                userData.put(LONGITUDE, lng);
                                key = userDatabase.push().getKey();
                                Map<String, Map<String, String>> mSendingData = new HashMap<>();
                                mSendingData.put(key, userData);
                                Map<String, Map<String, Map<String, String>>> mFinalData = new HashMap<>();
                                mFinalData.put(INTERMEDIATE, mSendingData);
                                userDatabase.setValue(mFinalData);

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
                                        /*if (null != mCurrentLocation) {
                                                String lat = String.valueOf(mCurrentLocation.getLatitude());
                                                String lng = String.valueOf(mCurrentLocation.getLongitude());
                                                mDatabase = FirebaseDatabase.getInstance().getReference();

                                                if (checkBusSelection != 0) {

                                                        String BUS = "b" + checkBusSelection;
                                                        DatabaseReference userDatabase = mDatabase.child(VEHICLE).child(BUS).child(userId);
                                                        userDatabase.child(LATITUDE).setValue(lat);
                                                        userDatabase.child(LONGITUDE).setValue(lng);

                                                }

                                        } else {
                                                Log.d(TAG, "My location is null ...............");
                                        }*/
                                }
                        }

                };
                if (mCurrentLocation != null) {
                        Log.d(TAG, "LocationCallback -> Latitude : " + mCurrentLocation.getLatitude() + "Longitude : " + mCurrentLocation.getLongitude());
                }


        }

        @Override
        public void onMapReady(GoogleMap googleMap) {
                mMap = googleMap;

                // Show Zoom buttons
                mMap.getUiSettings().setZoomControlsEnabled(true);
                // Turns traffic layer on
                mMap.setTrafficEnabled(true);
                // Enables indoor maps
                mMap.setIndoorEnabled(true);
                //Turns on 3D buildings
                mMap.setBuildingsEnabled(true);

                // Add a marker in Sydney and move the camera
        /*LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));*/
        /*double mylatitiude = mMap.getMyLocation().getLatitude();
        double mylongitude = mMap.getMyLocation().getLongitude();
        //mylocation = {10.802874, 76.820238}
        LatLng mylocation = new LatLng(mylatitiude, mylongitude);
        mMap.addMarker(new MarkerOptions().position(mylocation).title("MyLocation"));
        mMap.animateCamera(CameraUpdateFactory.newLatLng(mylocation));*/
                if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                }
                mMap.setMyLocationEnabled(true);
                String str = "My Location";
                if (null != mCurrentLocation) {

                        Geocoder geocoder = new Geocoder(getApplicationContext());

                        try {
                                List<android.location.Address> addressList = geocoder.getFromLocation(mCurrentLocation.getLatitude(),
                                        mCurrentLocation.getLongitude(), 1);
                                str = addressList.get(0).getLocality() + ",";
                                str += addressList.get(0).getCountryName();
                                Log.d(TAG, "GEOCODER STARTED.");
                        } catch (IOException e) {
                                e.printStackTrace();
                                Log.e(TAG, "GEOCODER DIDN'T WORK.");
                        }

                        mMap.addMarker(new MarkerOptions()
                                .position(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()))
                                .title(str)).setIcon(BitmapDescriptorFactory.fromResource(R.drawable.start_blue));
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
                                        makeMarkerOnTheLocation(checkBusSelection);
                                        showDistanceInBetween(checkBusSelection);
                    /*LatLng bus1_location = new LatLng(myClass.latitude,  myClass.longitude);
                    LatLng myLocation = new LatLng(myClass.latitude,  myClass.longitude);
                    double DIFFERENCE = CalculationByDistance(myLocation, bus1_location);
                    //String dist = String.valueOf(DIFFERENCE) + "Km";
                    String dist = mCurrentLocation.getLatitude() + ", " + mCurrentLocation.getLongitude();
                    distance.setText(dist);
                    String theTime = myClass.latitude + ", " +  myClass.longitude;

                    TextView time = (TextView) findViewById(R.id.time);
                    time.setText(theTime);*/
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
                                        makeMarkerOnTheLocation(checkBusSelection);
                                        showDistanceInBetween(checkBusSelection);
                                        break;
                                }
                        case R.id.bus3:
                                if (checked) {
                                        checkBusSelection = 3;
                                        showDistanceInBetween(checkBusSelection);
                                        makeMarkerOnTheLocation(checkBusSelection);
                                        break;
                                }
                        case R.id.bus4:
                                if (checked) {
                                        checkBusSelection = 4;
                                        showDistanceInBetween(checkBusSelection);
                                        makeMarkerOnTheLocation(checkBusSelection);
                                        break;
                                }
                        case R.id.bus5:
                                if (checked) {
                                        checkBusSelection = 5;
                                        showDistanceInBetween(checkBusSelection);
                                        makeMarkerOnTheLocation(checkBusSelection);
                                        break;
                                }
                }
        }

        @Override
        public void onBackPressed() {
                new AlertDialog.Builder(this)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle("Closing Activity")
                        .setMessage("Are you sure you want to close this activity?")
                        .setPositiveButton("EXIT", new DialogInterface.OnClickListener()
                        {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                        finish();
                                }

                        })
                        .setNegativeButton("No", null)
                        .show();
        }

        private void makeMarkerOnTheLocation(final int checkBusSelection) {

                String BUS = "b" + checkBusSelection;
                mDatabase = FirebaseDatabase.getInstance().getReference();
                DatabaseReference userDatabase = mDatabase.child(USER).child(BUS);

                userDatabase.addValueEventListener(new ValueEventListener() {
                        //@RequiresApi(api = Build.VERSION_CODES.N)
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                                GenericTypeIndicator<Map<String, String>> genericTypeIndicator = new GenericTypeIndicator<Map<String, String>>() {
                                };
                                Map<String, String> map = dataSnapshot.getValue(genericTypeIndicator);

                                Log.d(TAG, "Data : " + dataSnapshot.getValue());

                                assert map != null;
                                String latitudeStr = map.get("latitude");
                                String longitudeStr = map.get("longitude");

                                Log.d(TAG, "Latitude = " + latitudeStr);
                                Log.d(TAG, "Longitude = " + longitudeStr);

                                double latitude = Double.parseDouble(latitudeStr);
                                double longitude = Double.parseDouble(longitudeStr);

                                String busName = "BUS " + checkBusSelection;
                                if (null != mCurrentLocation) {

                                        Geocoder geocoder = new Geocoder(getApplicationContext());

                                        try {
                                                List<android.location.Address> addressList = geocoder.getFromLocation(latitude, longitude, 1);
                                                String str = addressList.get(0).getLocality() + ",";
                                                str += addressList.get(0).getCountryName();
                                                str += " (" + busName + ")";
                                                mMap.addMarker(new MarkerOptions()
                                                        .position(new LatLng(latitude, longitude))
                                                        .title(str))
                                                        .setIcon(BitmapDescriptorFactory.fromResource(R.drawable.end_green));
                                                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 12.0f));
                                        } catch (IOException e) {
                                                e.printStackTrace();
                                                mMap.addMarker(new MarkerOptions()
                                                        .position(new LatLng(latitude, longitude))
                                                        .title(busName))
                                                        .setIcon(BitmapDescriptorFactory.fromResource(R.drawable.end_green));
                                                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 12.0f));
                                                Log.e(TAG, "GEOCODER DIDN'T WORK.");
                                        }


                                        Log.d(TAG, "MARKER HAS BEEN MADE TO " + busName);
                                }

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }

                });
        }

        private void showDistanceInBetween(int checkBusSelection) {

        /*Bundle extras = getIntent().getExtras();
        String userId = extras.getString("email");*/
                //String userId = email;
                //assert userId != null;
                //String[] temp = userId.split("@");
                //userId = temp[0];
                String BUS = "b" + checkBusSelection;
                mDatabase = FirebaseDatabase.getInstance().getReference();
                DatabaseReference userDatabase = mDatabase.child(USER).child(BUS);

                userDatabase.addValueEventListener(new ValueEventListener() {
                        //@RequiresApi(api = Build.VERSION_CODES.N)
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                                GenericTypeIndicator<Map<String, String>> genericTypeIndicator = new GenericTypeIndicator<Map<String, String>>() {
                                };
                                Map<String, String> map = dataSnapshot.getValue(genericTypeIndicator);

                                Log.d(TAG, "Data : " + dataSnapshot.getValue());
                                Log.d(TAG, "My Location : " + mCurrentLocation.getLatitude() + ", " + mCurrentLocation.getLongitude());

                                String latitudeStr = map.get("latitude");
                                String longitudeStr = map.get("longitude");

                                Log.d(TAG, " Destination Latitude = " + latitudeStr);
                                Log.d(TAG, "Destination Longitude = " + longitudeStr);

                                double latitude = Double.parseDouble(latitudeStr);
                                double longitude = Double.parseDouble(longitudeStr);

                                // https://maps.googleapis.com/maps/api/directions/json?origin=Toronto&destination=Montreal
                                // &key=YOUR_API_KEY
                                if (mCurrentLocation != null) {
                                        String url = "https://maps.googleapis.com/maps/api/directions/json?origin="
                                                + mCurrentLocation.getLatitude() + "," + mCurrentLocation.getLongitude()
                                                + "&destination=" + latitudeStr + "," + longitudeStr + "&key=AIzaSyChXllnUaESuRZPDpSHtb3oyXgL1edHITg";// + R.string.google_direction_api_key;
                                        /*String url = "https://maps.googleapis.com/maps/api/directions/json?origin="
                                                + "40.81649,-73.907807&destination=40.819585,-73.90177"+ "&key=AIzaSyChXllnUaESuRZPDpSHtb3oyXgL1edHITg";// + R.string.google_direction_api_key;*/
                                         Log.d(TAG, "URL : " + url);
                                        DownloadTask downloadTask = new DownloadTask();

                                        // Start downloading json data from Google Directions API
                                        downloadTask.execute(url);
                                }

                                /*LatLng bus1_location = new LatLng(latitude,  longitude);
                                LatLng myLocation = new LatLng(mCurrentLocation.getLatitude(),  mCurrentLocation.getLongitude());
                                String DIFFERENCE = CalculationByDistance(myLocation, bus1_location);
                                String dist = DIFFERENCE + " Km";*/
                                //distance.setText(dist);
                /*TextView time = (TextView) findViewById(R.id.time);
                String theTime = latitudeStr + ", " +  longitudeStr;
                time.setText(theTime);*/

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                });
                //return myClass.getLongitude();
        }

        public String CalculationByDistance(LatLng StartP, LatLng EndP) {
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
                return (new DecimalFormat("##.###").format(Radius * c));
        /*double distance = 0;
        Location crntLocation = new Location("crntlocation");
        crntLocation.setLatitude(StartP.latitude);
        crntLocation.setLongitude(StartP.longitude);

        Location newLocation = new Location("newlocation");
        newLocation.setLatitude(EndP.latitude);
        newLocation.setLongitude(EndP.longitude);

        distance = crntLocation.distanceTo(newLocation) / 1000;      // in km
        return distance;*/
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
                        if (checkBusSelection != 0) {
                                BUS = "b" + checkBusSelection;
                                DatabaseReference userDatabase = mDatabase.child(VEHICLE).child(BUS);
                                Map<String, String> userData = new HashMap<>();
                                userData.put(LATITUDE, lat);
                                userData.put(LONGITUDE, lng);
                                key = userDatabase.push().getKey();
                                Map<String, Map<String, String>> mSendingData = new HashMap<>();
                                mSendingData.put("LOCATION", userData);
                                /*Map<String, Map<String, Map<String, String>>> mFinalData = new HashMap<>();
                                mFinalData.put(INTERMEDIATE, mSendingData);*/
                                userDatabase.child(key).setValue(mSendingData);

                                Toast.makeText(MapsActivity.this, "REQUEST SENT", Toast.LENGTH_LONG).show();
                                Button button = (Button) findViewById(R.id.pick_me);
                                button.setClickable(false);
                                button.setBackgroundColor(Color.GREEN);

                        }

                } else {
                        Log.d(TAG, "location is null ...............");
                }

        }

        public void cancel(View view) {

                if (null != mCurrentLocation) {

                        String lat = String.valueOf(mCurrentLocation.getLatitude());
                        String lng = String.valueOf(mCurrentLocation.getLongitude());
                        if (checkBusSelection != 0) {
                                //String BUS = "b" + checkBusSelection;
                                DatabaseReference userDatabase = mDatabase.child(VEHICLE).child(BUS);
                                //String key = userDatabase.push().getKey();

                                userDatabase.child(key).removeValue();
                                Toast.makeText(MapsActivity.this, "REQUEST ENDED", Toast.LENGTH_LONG).show();
                                Button button = (Button) findViewById(R.id.pick_me);
                                button.setClickable(true);
                                button.setBackgroundColor(Color.BLUE);

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
                        public void onLocationResult(LocationResult locationResult) {
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
                FirebaseAuth mAuth = FirebaseAuth.getInstance();
                FirebaseUser currentUser = mAuth.getCurrentUser();

                if (currentUser == null) {
                        Intent i = new Intent(MapsActivity.this, Login.class);
                        startActivity(i);
                        finish();
                }
                else {
                	 /*SharedPreferences myPrefs = this.getSharedPreferences("contact", MODE_WORLD_READABLE);
                        userId = myPrefs.getString("email", "none");*/
                	 userId = currentUser.getEmail();
                }
                Log.d(TAG, "onStart fired ..............");
                mGoogleApiClient.connect();
                if (!checkPermissions()) {
                        requestPermissions();
                }
        }

        private boolean checkPermissions() {
                int permissionState = ActivityCompat.checkSelfPermission(this,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION);
                return permissionState == PackageManager.PERMISSION_GRANTED;
        }

        private void requestPermissions() {
                boolean shouldProvideRationale =
                        ActivityCompat.shouldShowRequestPermissionRationale(this,
                                android.Manifest.permission.ACCESS_COARSE_LOCATION);

                // Provide an additional rationale to the user. This would happen if the user denied the
                // request previously, but didn't check the "Don't ask again" checkbox.
                if (shouldProvideRationale) {

                        Log.i(TAG, "Displaying permission rationale to provide additional context.");

                } else {
                        Log.i(TAG, "Requesting permission");
                        // Request permission. It's possible this can be auto answered if device policy
                        // sets the permission in a given state or the user denied the permission
                        // previously and checked "Never ask again".
                        startLocationPermissionRequest();
                }
        }

        private void startLocationPermissionRequest() {
                ActivityCompat.requestPermissions(MapsActivity.this,
                        new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION},
                        REQUEST_PERMISSIONS_REQUEST_CODE);
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

        public void onNormalMap(View view) {
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        }

        public void onSatelliteMap(View view) {
                mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        }

        public void onTerrainMap(View view) {
                mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        }

        public void onHybridMap(View view) {
                mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        }

        /**
         * A method to download json data from url
         */
        private String downloadUrl(String strUrl) throws IOException {
                String data = "";
                InputStream iStream = null;
                HttpURLConnection urlConnection = null;
                try {
                        URL url = new URL(strUrl);
                        Log.d(TAG, "url received in downloadUrl = "+url);

                        // Creating an http connection to communicate with url
                        urlConnection = (HttpURLConnection) url.openConnection();

                        // Connecting to url
                        urlConnection.connect();

                        // Reading data from url
                        iStream = urlConnection.getInputStream();

                        BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

                        StringBuilder sb = new StringBuilder();

                        String line = "";
                        while ((line = br.readLine()) != null) {
                                sb.append(line);
                        }

                        data = sb.toString().trim();

                        br.close();

                } catch (Exception e) {
                        Log.d(TAG, e.toString());
                        Log.e(TAG, "CONNECTION IS TOTALLY FAILED.");
                } finally {
                        assert iStream != null;
                        iStream.close();
                        urlConnection.disconnect();
                }
                return data;
        }

        // Fetches data from url passed
        private class DownloadTask extends AsyncTask<String, Void, String> {

                // Downloading data in non-ui thread
                @Override
                protected String doInBackground(String... url) {

                        // For storing data from web service
                        String data = "";

                        try {
                                // Fetching the data from web service
                                data = downloadUrl(url[0]);
                                if (data != null) {
                                        Log.d(TAG, "data from downloadUrl = " + data);
                                }
                                else {
                                        Log.e(TAG, "data from downloadUrl is not working.");
                                }

                        } catch (Exception e) {
                                Log.d("Background Task", e.toString());
                                Log.e(TAG, "data from downloadUrl = FAILED.");
                        }
                        return data;
                }

                // Executes in UI thread, after the execution of
                // doInBackground()
                @Override
                protected void onPostExecute(String result) {
                        super.onPostExecute(result);

                        ParserTask parserTask = new ParserTask();
                        //if (result != null) {
                                Log.d(TAG, "result in Download Task = " + result);
                                // Invokes the thread for parsing the JSON data
                                parserTask.execute(result);
                        //}
                        //else {
                          //      Log.e(TAG, "result is null.");
                        //}

                }
        }

        /**
         * A class to parse the Google Places in JSON format
         */

        private class ParserTask extends AsyncTask<String, Integer, JSONObject> {

                // Parsing the data in non-ui thread
                @Override
                protected JSONObject doInBackground(String... jsonData) {

                        JSONObject jObject;
                        List<List<HashMap<String, String>>> routes = null;

                        try {
                                jObject = new JSONObject(jsonData[0]);
                                Log.d(TAG, "jsonData[0] = "+jsonData[0] );
                                Log.d(TAG, "jObject.toString() = " + jObject.toString());
                                /*DirectionsJSONParser parser = new DirectionsJSONParser();
                                Log.d(TAG, "parser.toString() = " + parser.toString());
                                Log.d(TAG, "SOMETHING IS HAPPENING");

                                // Starts parsing data
                                routes = parser.parse(jObject);
                                Log.d(TAG, "Executing routes");
                                Log.d(TAG, routes.toString());
                                Log.d(TAG, "routes = " + routes);*/
                                return jObject;
                        } catch (Exception e) {
                                e.printStackTrace();
                                Log.e(TAG, "JSONParser class didn't work properly");
                                return null;
                        }

                }

                // Executes in UI thread, after the parsing process
                @Override
                protected void onPostExecute(JSONObject result) {

                        List<List<HashMap<String, String>>> routes = null;
                        DirectionsJSONParser parser = new DirectionsJSONParser();
                        Log.d(TAG, "parser.toString() = " + parser.toString());
                        Log.d(TAG, "SOMETHING IS HAPPENING");

                        // Starts parsing data
                        routes = parser.parse(result);
                        Log.d(TAG, "Executing routes");
                        Log.d(TAG, routes.toString());
                        Log.d(TAG, "routes = " + routes);

                        ArrayList<LatLng> points = null;
                        PolylineOptions lineOptions = null;
                        MarkerOptions markerOptions = new MarkerOptions();
                        String thedistance = "";
                        String theduration = "";
                        if (routes != null) {
                                Log.d(TAG, "result = " + routes.size());

                                try {
                                        if (routes.size() < 1) {
                                                Toast.makeText(getBaseContext(), "No Points", Toast.LENGTH_SHORT).show();
                                                return;
                                        }
                                } catch (Exception e) {
                                        Log.e(TAG, "result.size()  is null.");
                                }


                                // Traversing through all the routes
                                for (int i = 0; i < routes.size(); i++) {
                                        points = new ArrayList<LatLng>();
                                        //ineOptions = new PolylineOptions();

                                        // Fetching i-th route
                                        List<HashMap<String, String>> path = routes.get(i);
                                        Log.d(TAG, "path = " + path);
                                        // Fetching all the points in i-th route
                                        for (int j = 0; j < path.size(); j++) {
                                                HashMap<String, String> point = path.get(j);
                                                Log.d(TAG, "point = " + point);

                                                if (j == 0) {    // Get distance from the list
                                                        thedistance = (String) point.get("distance");
                                                        Log.d(TAG, "DISTANCE = " + thedistance);
                                                        distance.setText(thedistance);
                                                        continue;
                                                } else if (j == 1) { // Get duration from the list
                                                        theduration = (String) point.get("duration");
                                                        Log.d(TAG, "DURATION = " + theduration);

                                                        duration.setText(theduration);
                                                        continue;
                                                }

                                                double lat = Double.parseDouble(point.get("lat"));
                                                double lng = Double.parseDouble(point.get("lng"));
                                                LatLng position = new LatLng(lat, lng);

                                                points.add(position);
                                        }


                                }
                        }

                        else {
                                        Log.e(TAG, "result is null. result = " + routes);
                                }


                                // Adding all the points in the route to LineOptions
                                /*lineOptions.addAll(points);
                        assert lineOptions != null;
                        lineOptions.width(2);
                                lineOptions.color(Color.RED);*/
                        }



                        //vDistanceDuration.setText("Distance:"+thedistance + ", Duration:"+theduration);

                        // Drawing polyline in the Google Map for the i-th route
                        //mMap.addPolyline(lineOptions);
                }
        

}


