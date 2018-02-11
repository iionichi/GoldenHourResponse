package com.example.acer.goldenhour;

import android.content.ClipData;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.media.MediaPlayer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.internal.NavigationMenuItemView;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioButton;
import android.widget.Switch;
import android.widget.Toast;

import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StrangerMapActivity extends AppCompatActivity implements OnMapReadyCallback,GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, RoutingListener, NavigationView.OnNavigationItemSelectedListener{

    final int LOCATION_REQUEST_CODE = 1;

    private GoogleMap mMap;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    LocationRequest mLocationRequest;
    Marker destinationMarker, pickupMarker;

    private MenuItem mItem;

    private DrawerLayout mDrawerLayoutStranger;
    private ActionBarDrawerToggle mToggleStranger;
    NavigationView mNavigationView;

    private LatLng pickupLocation, destinationLatLng, pickupLocation2;


    private String requestService, userId;

    private int hospitalToggle = 1;

    private Boolean requestBol = false,addedCustomerToHospital = false, onlyHospital = false, removeAnonymous = false;

    //For adding polylines which marks in the map
    private List<Polyline> polylines;
    private static final int[] COLORS = new int[]{R.color.primary_dark_material_light};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stranger_map);

        mDrawerLayoutStranger = (DrawerLayout) findViewById(R.id.drawerSt);
        mToggleStranger = new ActionBarDrawerToggle(this,mDrawerLayoutStranger,R.string.open,R.string.close);
        mDrawerLayoutStranger.addDrawerListener(mToggleStranger);
        mToggleStranger.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        polylines = new ArrayList<>();

        buildGoogleApiClient();
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mNavigationView = findViewById(R.id.strangerNavigation);

        if (mNavigationView != null) {
            mNavigationView.setNavigationItemSelectedListener(this);
        }

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(StrangerMapActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
        } else {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item_driver) {
        if (mToggleStranger.onOptionsItemSelected(item_driver)) {
            return true;
        }
        return super.onOptionsItemSelected(item_driver);
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        switch (id){
            case R.id.callNormalAmbulance:
                if (requestBol){
                    endRide();
                    mItem = null;
                }
                else {
                    Toast.makeText(this, item.getTitle(), Toast.LENGTH_SHORT).show();
                    requestService = "Normal";

                    mItem = item;

                    requestBol = true;

                    userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("customerRequest");
                    GeoFire geoFire = new GeoFire(ref);
                    geoFire.setLocation(userId, new GeoLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude()));

                    pickupLocation = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                    pickupMarker = mMap.addMarker(new MarkerOptions().position(pickupLocation).title("Pickup Here").icon(BitmapDescriptorFactory.fromResource(R.mipmap.ghr_pickup)));

                    item.setTitle("Looking for ambulance");

                    getClosestDriver();
                }
                break;

            case R.id.callCardiacAmbulance:
                requestService = "Cardiac";
                break;

            case R.id.getHospital:
                switch (hospitalToggle){
                    case 1:
                        onlyHospital = true;
                        mItem = item;
                        item.setTitle("Finding Hospital");
                        Toast.makeText(this, "Finding Hospital", Toast.LENGTH_SHORT).show();
                        getHospital();
                        break;
                    case 2:
                        geoQueryH.removeAllListeners();
                        hospitalFound = false;
                        erasePolyLines();
                        if (destinationMarker != null){
                            destinationMarker.remove();
                        }
                        final DatabaseReference hospiRef = FirebaseDatabase.getInstance().getReference().child("customerRequestH").child(userId);
                        hospiRef.removeValue();
                        //Removing the customer Id from hospital table
                        if (hospitalFoundId != null){
                            String customerId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                            DatabaseReference removeCustomerRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Hospital").child(hospitalFoundId).child("customerRequestId").child(customerId);
                            removeCustomerRef.removeValue();
                            hospitalFoundId = null;
                        }
                        item.setTitle("Get Hospital");
                        hospitalToggle = 1;
                        onlyHospital = false;
                        break;
                }
                break;

            case  R.id.logoutS:
                final DatabaseReference removeId = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(userId);
                removeId.removeValue();

                FirebaseUser aUser = FirebaseAuth.getInstance().getCurrentUser();
                aUser.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(StrangerMapActivity.this, "User Deleted", Toast.LENGTH_SHORT).show();
                        FirebaseAuth.getInstance().signOut();
                    }
                });
                Intent intent = new Intent(StrangerMapActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
        }

        return true;
    }

    private int radius = 1;
    private Boolean driverFound = false;
    private String driverFoundId;

    GeoQuery geoQuery;
    private void getClosestDriver(){
        DatabaseReference driverLocation = FirebaseDatabase.getInstance().getReference().child("driversAvailable");
        GeoFire geoFire = new GeoFire(driverLocation);
        geoQuery  = geoFire.queryAtLocation(new GeoLocation(pickupLocation.latitude, pickupLocation.longitude),radius);
        geoQuery.removeAllListeners();

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if (!driverFound && requestBol){
                    DatabaseReference mCustomerDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(key);
                    mCustomerDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if(dataSnapshot.exists() && dataSnapshot.getChildrenCount()> 0){
                                Map<String, Object> driverMap = (Map<String, Object>) dataSnapshot.getValue();
                                if(driverFound){
                                    return;
                                }

                                if(driverMap.get("service").equals(requestService)){
                                    driverFound = true;
                                    driverFoundId = dataSnapshot.getKey();

                                    DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverFoundId).child("customerRequest");
                                    String customerId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                    HashMap map = new HashMap();
                                    map.put("customerRideId", customerId);
                                    driverRef.updateChildren(map);

                                    getDriverLocation();
//                                    getDriverInfo();
                                    getHasRideEnded();
                                    getHospital();

                                    mItem.setTitle("Looking For Ambulance Location");
                                    Toast.makeText(StrangerMapActivity.this, "Looking For Ambulance Location", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }
                    });
                }
            }

            @Override
            public void onKeyExited(String key) {
            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {
            }

            @Override
            public void onGeoQueryReady() {
                if(!driverFound){
                    radius++;
                    getClosestDriver();
                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {
            }
        });
    }

    /*--------------- Map specific functions -----
    |  Function(s) getDriverLocation
    |
    |  Purpose:  Get's most updated driver location and it's always checking for movements.
    |
    |  Note:
    |	   Even tho we used geofire to push the location of the driver we can use a normal
    |      Listener to get it's location with no problem.
    |
    |      0 -> Latitude
    |      1 -> Longitudde
    *-------------------------------------------------------------------*/

    private Marker mDriverMarker;
    private DatabaseReference driverLocationRef;
    private ValueEventListener driverLocationRefListener;
    private void getDriverLocation(){
        driverLocationRef = FirebaseDatabase.getInstance().getReference().child("driversWorking").child(driverFoundId).child("l");
        driverLocationRefListener = driverLocationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && requestBol){
                    List<Object> map = (List<Object>) dataSnapshot.getValue();
                    double locationLat = 0;
                    double locationLng = 0;
                    //mRequest.setText("Driver Found");
                    if(map.get(0) != null){
                        locationLat = Double.parseDouble(map.get(0).toString());
                    }
                    if(map.get(1) != null){
                        locationLng = Double.parseDouble(map.get(1).toString());
                    }
                    LatLng driverLatLng = new LatLng(locationLat,locationLng);
                    if(mDriverMarker != null){
                        mDriverMarker.remove();
                    }
                    Location loc1 = new Location("");
                    loc1.setLatitude(pickupLocation.latitude);
                    loc1.setLongitude(pickupLocation.longitude);

                    Location loc2 = new Location("");
                    loc2.setLatitude(driverLatLng.latitude);
                    loc2.setLongitude(driverLatLng.longitude);

                    float distance = loc1.distanceTo(loc2);

                    if(distance<100){
                        Toast.makeText(StrangerMapActivity.this, "Driver is Here", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        mItem.setTitle("Ambulance Found" + String.valueOf(distance));
                    }

                    mDriverMarker = mMap.addMarker(new MarkerOptions().position(driverLatLng).title("Your Ambulance").icon(BitmapDescriptorFactory.fromResource(R.mipmap.ghr_driver)));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    /*--------------------- getDriverInfo -----
    |  Function(s) getDriverInfo
    |
    |  Purpose:  Get all the user information that we can get from the user's database.
    |
    |  Note: --
    *-------------------------------------------------------------------*/

//    private void getDriverInfo(){
//        mDriverInfo.setVisibility(View.VISIBLE);
//        DatabaseReference mCustomerDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverFoundId);
//        mCustomerDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
//                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
//                    if(map.get("name") != null){
//                        mDriverName.setText(map.get("name").toString());
//                    }
//                    if(map.get("phone") != null){
//                        mDriverPhone.setText(map.get("phone").toString());
//                    }
//                    if(map.get("ambulance") != null){
//                        mDriverAmbulance.setText(map.get("ambulance").toString());
//                    }
//                    if(map.get("ambulanceNumber") != null){
//                        mDriverAmbulanceNumber.setText(map.get("ambulanceNumber").toString());
//                    }
//                }
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//            }
//        });
//    }

    private DatabaseReference driveHasEndedRef;
    private ValueEventListener driveHasEndedRefListener;//This event listener give us the ability to cancel the event listener
    private void getHasRideEnded() {
        driveHasEndedRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverFoundId).child("customerRequest").child("customerRideId");
        driveHasEndedRefListener = driveHasEndedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                }
                else {
                    endRide();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void endRide(){
        requestBol = false;
        hospitalFound = false;
        geoQuery.removeAllListeners();
        geoQueryH.removeAllListeners();
        driverLocationRef.removeEventListener(driverLocationRefListener);
        driveHasEndedRef.removeEventListener(driveHasEndedRefListener);

        if(driverFoundId != null){
            DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverFoundId).child("customerRequest");
            driverRef.removeValue();
            driverFoundId = null;
        }
        //Remove the customer child from the Hospital
        if(addedCustomerToHospital){
            addedCustomerToHospital = false;
            DatabaseReference addCustomerToHospital = FirebaseDatabase.getInstance().getReference().child("Hospital").child(hospitalFoundId).child("customerRequestId");
            addCustomerToHospital.removeValue();
            hospitalFoundId = null;
        }

        driverFound = false;
        radius = 1;
        radiusH = 1;

        String userId = FirebaseAuth.getInstance().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("customerRequest");
        GeoFire geoFire = new GeoFire(ref);
        geoFire.removeLocation(userId);

        if(pickupMarker != null){
            pickupMarker.remove();
        }
        if(mDriverMarker != null){
            mDriverMarker.remove();
        }
        mItem.setTitle("Call Ambulance");

//        mDriverInfo.setVisibility(View.GONE);
//        mDriverName.setText("");
//        mDriverPhone.setText("");
//        mDriverAmbulance.setText("");
//        mDriverAmbulanceNumber.setText("");
//        mDriverProfileImage.setImageResource(R.mipmap.ic_launcher);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(StrangerMapActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
            }
        }
//        buildGoogleApiClient();
        mMap.setMyLocationEnabled(true);
        googleMap.setTrafficEnabled(true);
    }

    protected synchronized void buildGoogleApiClient(){
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;

        LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());
        pickupLocation2 = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(18)); //value goes from 1 - 21

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(StrangerMapActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
            }
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    protected void onStop() {
        super.onStop();
    }


    private int radiusH = 1;
    private Boolean hospitalFound = false;
    private String hospitalFoundId;
    GeoQuery geoQueryH;
    private void getHospital(){
        DatabaseReference hospitalLocation = FirebaseDatabase.getInstance().getReference().child("Users").child("Hospital");
        GeoFire geoFireH = new GeoFire(hospitalLocation);
        geoQueryH  = geoFireH.queryAtLocation(new GeoLocation(pickupLocation2.latitude, pickupLocation2.longitude),radiusH);
        geoQueryH.removeAllListeners();

        geoQueryH.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if (!hospitalFound){
                    DatabaseReference mCustomerDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Hospital").child(key);
                    mCustomerDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if(dataSnapshot.exists() && dataSnapshot.getChildrenCount()> 0){
                                Map<String, Object> driverMap = (Map<String, Object>) dataSnapshot.getValue();
                                if(hospitalFound){
                                    return;
                                }
                                hospitalFound = true;
                                hospitalFoundId = dataSnapshot.getKey();

                                if (onlyHospital) {
                                    DatabaseReference driverRef2 = FirebaseDatabase.getInstance().getReference().child("customerRequestH").child(userId);
                                    HashMap map2 = new HashMap();
                                    map2.put("hospitalFoundId", hospitalFoundId);
                                    driverRef2.updateChildren(map2);
                                    getHospitalLocation();
                                }

                                else {
                                    DatabaseReference driverRef1 = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverFoundId).child("customerRequest");
                                    HashMap map1 = new HashMap();
                                    map1.put("hospitalFoundId", hospitalFoundId);
                                    driverRef1.updateChildren(map1);
                                }

                                //Adding customer information to the hospital found
                                DatabaseReference addCustomerToHospital = FirebaseDatabase.getInstance().getReference().child("Users").child("Hospital").child(hospitalFoundId).child("customerRequestId");
                                String customerId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                HashMap map3 = new HashMap();
                                map3.put(customerId, customerId);
                                addCustomerToHospital.updateChildren(map3);
                                addedCustomerToHospital = true;
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }
                    });
                }
            }

            @Override
            public void onKeyExited(String key) {
            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {
            }

            @Override
            public void onGeoQueryReady() {
                if(!hospitalFound){
                    radiusH++;
                    getHospital();
                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {
            }
        });
    }

    private void getHospitalLocation(){
        DatabaseReference findHospital = FirebaseDatabase.getInstance().getReference().child("customerRequestH").child(userId);
        findHospital.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    Map<String, Object> newMap = (Map<String, Object>) dataSnapshot.getValue();
                    if (newMap.get("hospitalFoundId") != null){
                        final String hosId = newMap.get("hospitalFoundId").toString();
                        DatabaseReference hospitalLocation = FirebaseDatabase.getInstance().getReference().child("Users").child("Hospital").child(hosId).child("l");
                        hospitalLocation.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()){
                                    if (dataSnapshot.exists() && !hosId.equals("")) {
                                        List<Object> map = (List<Object>) dataSnapshot.getValue();
                                        double locationLat = 0;
                                        double locationLng = 0;
                                        if (map.get(0) != null) {
                                            locationLat = Double.parseDouble(map.get(0).toString());
                                        }
                                        if (map.get(1) != null) {
                                            locationLng = Double.parseDouble(map.get(1).toString());
                                        }
                                        destinationLatLng = new LatLng(locationLat, locationLng);
                                        destinationMarker = mMap.addMarker(new MarkerOptions().position(destinationLatLng).title("Hospital Location").icon(BitmapDescriptorFactory.fromResource(R.mipmap.ghr_pickup)));
                                        getRouteToHospital(destinationLatLng);
                                        hospitalToggle = 2;
                                        mItem.setTitle("Hospital Reached");
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getRouteToHospital(LatLng destinationLatLng) {
        Routing routing = new Routing.Builder()
                .travelMode(AbstractRouting.TravelMode.DRIVING)
                .withListener(this)
                .alternativeRoutes(false)
                .waypoints(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()), destinationLatLng)
                .build();
        routing.execute();
    }

    @Override
    public void onRoutingFailure(RouteException e) {
        if(e != null) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }else {
            Toast.makeText(this, "Something went wrong, Try again", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRoutingStart() {
    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int shortestRouteIndex) {
        if(polylines.size()>0) {
            for (Polyline poly : polylines) {
                poly.remove();
            }
        }

        polylines = new ArrayList<>();
        //add route(s) to the map.
        for (int i = 0; i <route.size(); i++) {

            //In case of more than 5 alternative routes
            int colorIndex = i % COLORS.length;

            PolylineOptions polyOptions = new PolylineOptions();
            polyOptions.color(getResources().getColor(COLORS[colorIndex]));
            polyOptions.width(10 + i * 3);
            polyOptions.addAll(route.get(i).getPoints());
            Polyline polyline = mMap.addPolyline(polyOptions);
            polylines.add(polyline);

            Toast.makeText(getApplicationContext(),"Route "+ (i+1) +": distance - "+ route.get(i).getDistanceValue()+": duration - "+ route.get(i).getDurationValue(),Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRoutingCancelled() {
    }

    private void erasePolyLines(){
        for (Polyline line : polylines){
            line.remove();
        }
        polylines.clear();
    }
}
