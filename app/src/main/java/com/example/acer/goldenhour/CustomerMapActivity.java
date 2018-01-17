package com.example.acer.goldenhour;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomerMapActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    private GoogleMap mMap;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    LocationRequest mLocationRequest;

    SupportMapFragment mapFragment;

    private Button mLogout, mRequest, mSettings,mHospi;
    private LatLng pickupLocation;

    private Boolean requestBol = false;
    private Marker pickupMarker;

    private String requestService;

    private LinearLayout mDriverInfo;
    private ImageView mDriverProfileImage;
    private TextView mDriverName, mDriverPhone, mDriverAmbulance, mDriverAmbulanceNumber;

    private double longi,lati;//variables to store hospital location

    private RadioGroup mRadioGroup;

    final int LOCATION_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(CustomerMapActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
        }
        else {
            mapFragment.getMapAsync(this);
        }

        mDriverInfo = (LinearLayout) findViewById(R.id.driverInfo);

        mDriverProfileImage = (ImageView) findViewById(R.id.driverProfileImage);

        mDriverName = (TextView) findViewById(R.id.driverName);
        mDriverPhone = (TextView) findViewById(R.id.driverPhone);
        mDriverAmbulance = (TextView) findViewById(R.id.driverAmbulance);
        mDriverAmbulanceNumber = (TextView) findViewById(R.id.driverAmbulanceNumber);

        mRadioGroup = (RadioGroup) findViewById(R.id.radioGroup);
        mRadioGroup.check(R.id.normalAmbulance);

        mLogout = (Button) findViewById(R.id.logout);
        mRequest = (Button) findViewById(R.id.request);
        mSettings = (Button) findViewById(R.id.settings);
        mHospi = (Button) findViewById(R.id.hospi);

        mLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(CustomerMapActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
                return;
            }
        });
        mRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (requestBol){
                    requestBol = false;
                    geoQuery.removeAllListeners();
                    driverLocationRef.removeEventListener(driverLocationRefListener);

                    if(driverFoundId != null){
                        DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverFoundId).child("customerRequest");
                        driverRef.removeValue();
                        driverFoundId = null;
                    }
                    driverFound = false;
                    radius = 1;

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
                    mRequest.setText("Call Ambulance");

                    mDriverInfo.setVisibility(View.GONE);
                    mDriverName.setText("");
                    mDriverPhone.setText("");
                    mDriverAmbulance.setText("");
                    mDriverAmbulanceNumber.setText("");
                    mDriverProfileImage.setImageResource(R.mipmap.ic_launcher);
                }
                else {
                    int selectId = mRadioGroup.getCheckedRadioButtonId();

                    final RadioButton radioButton = (RadioButton)  findViewById(selectId);

                    if(radioButton.getText() == null){
                        return;
                    }

                    requestService = radioButton.getText().toString();

                    requestBol = true;

                    String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("customerRequest");
                    GeoFire geoFire = new GeoFire(ref);
                    geoFire.setLocation(userId, new GeoLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude()));

                    pickupLocation = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                    pickupMarker = mMap.addMarker(new MarkerOptions().position(pickupLocation).title("Pickup Here").icon(BitmapDescriptorFactory.fromResource(R.mipmap.ghr_pickup)));

                    mRequest.setText("Getting Your Driver");

                    getClosestDriver();

                }
            }
        });
        mSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CustomerMapActivity.this, CustomerSettingsActivity.class);
                startActivity(intent);
                return;
            }
        });
        mHospi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getHospital();
            }
        });
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
                                    getDriverInfo();
                                    mRequest.setText("Looking For Ambulance Location");
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
                        mRequest.setText("Driver is Here");
                    }
                    else {
                        mRequest.setText("Ambulance Found: " + String.valueOf(distance));
                    }

                    mDriverMarker = mMap.addMarker(new MarkerOptions().position(driverLatLng).title("Your Ambulance").icon(BitmapDescriptorFactory.fromResource(R.mipmap.ghr_driver)));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getDriverInfo(){
        mDriverInfo.setVisibility(View.VISIBLE);
        DatabaseReference mCustomerDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverFoundId);
        mCustomerDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if(map.get("name") != null){
                        mDriverName.setText(map.get("name").toString());
                    }
                    if(map.get("phone") != null){
                        mDriverPhone.setText(map.get("phone").toString());
                    }
                    if(map.get("ambulance") != null){
                        mDriverAmbulance.setText(map.get("ambulance").toString());
                    }
                    if(map.get("ambulanceNumber") != null){
                        mDriverAmbulanceNumber.setText(map.get("ambulanceNumber").toString());
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(CustomerMapActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
            }
        }
        buildGoogleApiClient();
        mMap.setMyLocationEnabled(true);
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
                ActivityCompat.requestPermissions(CustomerMapActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
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
        DatabaseReference hospitalLocation = FirebaseDatabase.getInstance().getReference().child("Hospital");
        GeoFire geoFireH = new GeoFire(hospitalLocation);
        geoQueryH  = geoFireH.queryAtLocation(new GeoLocation(pickupLocation.latitude, pickupLocation.longitude),radiusH);
        geoQueryH.removeAllListeners();

        geoQueryH.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if (!hospitalFound && requestBol){
                    DatabaseReference mCustomerDatabase = FirebaseDatabase.getInstance().getReference().child("Hospital").child(key);
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
                                DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverFoundId).child("customerRequest");
                                HashMap map1 = new HashMap();
                                map1.put("hospitalFoundId", hospitalFoundId);
                                driverRef.updateChildren(map1);
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
}