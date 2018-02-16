package com.example.acer.goldenhour;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.TextView;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.wang.avi.AVLoadingIndicatorView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomerMapActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, RoutingListener, NavigationView.OnNavigationItemSelectedListener{

    private static final int SEND_SMS_PERMISSION_REQUEST_CODE = 111, REQUEST_PHONE_CALL = 1;

    private GoogleMap mMap;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    LocationRequest mLocationRequest;

    SupportMapFragment mapFragment;

    private FirebaseAuth mAuth1;
    private String userID1, userId2;

    private int hospitalToggle = 1;

    private Button mRequest,mHospi;
    private LatLng pickupLocation, pickupLocation2, destinationLatLng;

    private Boolean requestBol = false, addedCustomerToHospital = false, onlyHospital = false, stopRequest = false, stopRequestH = false, showHospitalBool = true;
    private Marker pickupMarker, destinationMarker;

    private String requestService, userId, mePhone, msg, requestHospitalId;

    private LinearLayout mDriverInfo, mhospitalInfo;
    private ImageView mDriverProfileImage;
    private TextView mDriverName, mDriverPhone, mDriverAmbulance, mDriverAmbulanceNumber, mHospitalName, mHospitalNumber, mhospitalAddress;

    private RadioGroup mRadioGroup;
    private RatingBar mRatingBar;

    final int LOCATION_REQUEST_CODE = 1;

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;
    private NavigationView mNavigationView;
    private Menu mNavigationMenu;
    private DatabaseReference mCustomerDatabase1;

    //For adding polylines which marks in the map
    private List<Polyline> polylines;
    private static final int[] COLORS = new int[]{R.color.primary_dark_material_light};

    Dialog loaderDialog;
    AVLoadingIndicatorView avi;
    TextView loaderText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_map);

        loaderDialog = new Dialog(this);
        loaderDialog.setContentView(R.layout.loading_file_main);
        avi = (AVLoadingIndicatorView) loaderDialog.findViewById(R.id.aviLoader);
        loaderText = (TextView) loaderDialog.findViewById(R.id.loadingText);

        userId2 = FirebaseAuth.getInstance().getCurrentUser().getUid();
        mCustomerDatabase1 = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(userId2);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer);
        mToggle = new ActionBarDrawerToggle(this,mDrawerLayout,R.string.open,R.string.close);
        mDrawerLayout.addDrawerListener(mToggle);
        mToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        polylines = new ArrayList<>();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(CustomerMapActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
        }
        else {
            mapFragment.getMapAsync(this);
        }

        //Asking for SMS Permissions
        if (ActivityCompat.checkSelfPermission(this,Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(CustomerMapActivity.this, new String[]{Manifest.permission.SEND_SMS}, SEND_SMS_PERMISSION_REQUEST_CODE);
        }


        mDriverInfo = (LinearLayout) findViewById(R.id.driverInfo);
        mDriverProfileImage = (ImageView) findViewById(R.id.driverProfileImage);
        mDriverName = (TextView) findViewById(R.id.driverName);
        mDriverPhone = (TextView) findViewById(R.id.driverPhone);
        mDriverAmbulance = (TextView) findViewById(R.id.driverAmbulance);
        mDriverAmbulanceNumber = (TextView) findViewById(R.id.driverAmbulanceNumber);
        mRatingBar = (RatingBar) findViewById(R.id.ratingbar);

        mRadioGroup = (RadioGroup) findViewById(R.id.radioGroup);
        mRadioGroup.check(R.id.normalAmbulance);

        mhospitalInfo = (LinearLayout) findViewById(R.id.hospitalInfo);
        mHospitalName = (TextView) findViewById(R.id.hospitalName);
        mHospitalNumber = (TextView) findViewById(R.id.hospitalPhone);
        mhospitalAddress = (TextView) findViewById(R.id.hospitalAddress);

        mRequest = findViewById(R.id.request);
        mHospi = findViewById(R.id.hospi);

        mAuth1 = FirebaseAuth.getInstance();
        userID1 = mAuth1.getCurrentUser().getUid();
        mNavigationView = findViewById(R.id.nv);
        mNavigationMenu = mNavigationView.getMenu();
        mNavigationMenu.findItem(R.id.show_hospital).setVisible(false);

        if (mNavigationView != null){
            mNavigationView.setNavigationItemSelectedListener(this);
        }

        mRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (requestBol){
                    //For ending the search of ambulance
                    if (!driverFound){
                        stopRequest = true;
                    }
                    else {
                        endRide();
                    }
                }
                else {
                    int selectId = mRadioGroup.getCheckedRadioButtonId();
                    RadioButton radioButton = (RadioButton)  findViewById(selectId);
                    if(radioButton.getText() == null){
                        return;
                    }
                    requestService = radioButton.getText().toString();

                    requestBol = true;

                    userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
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

        mHospi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (hospitalToggle){
                    case 1:
                        onlyHospital = true;
                        mHospi.setText("Finding Hospital");
                        getHospital();
                        break;
                    case 2:
                        if (!hospitalFound){
                            stopRequestH = true;
                        }
                        else {
                            mNavigationMenu.findItem(R.id.show_hospital).setVisible(false);
                            geoQueryH.removeAllListeners();
                            hospitalFound = false;
                            erasePolyLines();
                            if (destinationMarker != null){
                                destinationMarker.remove();
                            }
                            DatabaseReference hospiRef = FirebaseDatabase.getInstance().getReference().child("customerRequestH").child(userID1);
                            hospiRef.removeValue();
                            //Removing the customer Id from hospital table
                            if (hospitalFoundId != null){
                                String customerId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                DatabaseReference removeCustomerRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Hospital").child(hospitalFoundId).child("customerRequestId").child(customerId);
                                removeCustomerRef.removeValue();
                                hospitalFoundId = null;
                            }
                        }
                        geoQueryH.removeAllListeners();
                        mHospi.setText("Get Hospital");
                        hospitalToggle = 1;
                        onlyHospital = false;
                        break;
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mToggle.onOptionsItemSelected(item)) {

            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        switch (id){
            case R.id.profile_settings:
                Intent intent = new Intent(CustomerMapActivity.this, CustomerSettingsActivity.class);
                startActivity(intent);
                break;

            case R.id.ambu_history:
                Intent intent5 = new Intent(CustomerMapActivity.this, HistoryActivty.class);
                intent5.putExtra("customerOrDriver", "Customers");
                startActivity(intent5);
                break;

            case R.id.db:
                Intent intent2 = new Intent(CustomerMapActivity.this, CustomerMainActivity.class);
                startActivity(intent2);
                break;

            case R.id.call_police:
                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:100"));

                if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    if(ContextCompat.checkSelfPermission(CustomerMapActivity.this, android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED){
                        ActivityCompat.requestPermissions(CustomerMapActivity.this, new String[]{Manifest.permission.CALL_PHONE}, REQUEST_PHONE_CALL);
                    }
                    else {
                        startActivity(callIntent);
                    }
                }
                break;

            case R.id.sos:
                loaderText.setText("Sending SMS");
                loaderDialog.show();
                loaderDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));//Background color for dialog
                loaderDialog.show();//Show the dialog
                loaderDialog.setCanceledOnTouchOutside(false);
                avi.smoothToShow();

                mCustomerDatabase1.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0) {
                            Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                            if (map.get("ephone") != null) {
                                mePhone = map.get("ephone").toString();
                            }
                        }

                        msg = "Sender is in critical emergancy.";

                        if (checkPermission(Manifest.permission.SEND_SMS)) {
                            SmsManager smsManager = SmsManager.getDefault();
                            smsManager.sendTextMessage(mePhone, null, msg, null, null);
                            loaderDialog.dismiss();
                            Toast.makeText(CustomerMapActivity.this, "Message Sent", Toast.LENGTH_SHORT).show();
                        } else {
                            ActivityCompat.requestPermissions(CustomerMapActivity.this,new String[]{Manifest.permission.SEND_SMS},SEND_SMS_PERMISSION_REQUEST_CODE);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
                break;

            case R.id.show_hospital:
                if (showHospitalBool){
                    getHospitalInfo();
                    item.setTitle("Hide Hospital Information");
                    mhospitalInfo.setVisibility(View.VISIBLE);
                    showHospitalBool = false;
                }
                else {
                    mhospitalInfo.setVisibility(View.GONE);
                    item.setTitle("Show Hospital Information");
                    try {
                        if (driverFoundId != null){
                            mDriverInfo.setVisibility(View.VISIBLE);

                        }
                    }catch (Exception e){

                    }
                    showHospitalBool = true;
                }
                break;

            case R.id.help_videos:
                Intent intent1 = new Intent(CustomerMapActivity.this, HelpActivity.class);
                startActivity(intent1);
                break;

            case R.id.Log_out:
                String DeviceID = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
                DatabaseReference removeDev = FirebaseDatabase.getInstance().getReference().child("LoggedIn").child(DeviceID);
                removeDev.removeValue();
                FirebaseAuth.getInstance().signOut();
                Intent intent6 = new Intent(CustomerMapActivity.this, MainActivity.class);
                startActivity(intent6);
                finish();
                break;
        }
        return true;
    }

    private Boolean checkPermission(String permission) {
        int checkPermission = ContextCompat.checkSelfPermission(this, permission);
        return checkPermission == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case SEND_SMS_PERMISSION_REQUEST_CODE:
                if(grantResults.length > 0 && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    Toast.makeText(CustomerMapActivity.this, "SMS Permission Provided", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(CustomerMapActivity.this, "SMS Permission denied", Toast.LENGTH_SHORT).show();
                }
                break;

            case REQUEST_PHONE_CALL:
                if(grantResults.length > 0 && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    Toast.makeText(CustomerMapActivity.this, "Call Permission Provided", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(CustomerMapActivity.this, "Call Permission denied", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private int radius = 1;
    private Boolean driverFound = false;
    private String driverFoundId;

    GeoQuery geoQuery;
    private void getClosestDriver(){
        //For ending the ambulance request if ambulance is not found
        if (stopRequest){
            mRequest.setText("Call Ambulance");
            requestBol = false;
            stopRequest = false;

            String userId = FirebaseAuth.getInstance().getUid();
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("customerRequest");
            GeoFire geoFire = new GeoFire(ref);
            geoFire.removeLocation(userId);

            if (pickupMarker != null){
                pickupMarker.remove();
            }

            geoQuery.removeAllListeners();
            return;
        }

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
                                    getHasRideEnded();
                                    getHospital();
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

    /*--------------------- getDriverInfo -----
    |  Function(s) getDriverInfo
    |
    |  Purpose:  Get all the user information that we can get from the user's database.
    |
    |  Note: --
    *-------------------------------------------------------------------*/

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

                    int ratingSum = 0;
                    float ratingsTotal = 0;
                    float ratingsAvg = 0;
                    for (DataSnapshot child : dataSnapshot.child("rating").getChildren()){
                        ratingSum = ratingSum + Integer.valueOf(child.getValue().toString());
                    }
                    if(ratingsTotal!= 0){
                        ratingsAvg = ratingSum/ratingsTotal;
                        mRatingBar.setRating(ratingsAvg);
                    }

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

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
            DatabaseReference addCustomerToHospital = FirebaseDatabase.getInstance().getReference().child("Users").child("Hospital").child(hospitalFoundId).child("customerRequestId").child(userId);
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
        mRequest.setText("Call Ambulance");

        mNavigationMenu.findItem(R.id.show_hospital).setVisible(false);
        mhospitalInfo.setVisibility(View.GONE);
        mDriverInfo.setVisibility(View.GONE);
        mDriverName.setText("");
        mDriverPhone.setText("");
        mDriverAmbulance.setText("");
        mDriverAmbulanceNumber.setText("");
        mDriverProfileImage.setImageResource(R.mipmap.ic_launcher);
    }

    /*-----------Map specific functions -----
    |  Function(s) onMapReady, buildGoogleApiClient, onLocationChanged, onConnected
    |
    |  Purpose:  Find and update user's location.
    |
    |  Note:
    |	   The update interval is set to 1000Ms and the accuracy is set to PRIORITY_HIGH_ACCURACY,
    |      If you're having trouble with battery draining too fast then change these to lower values
    *-------------------------------------------------------------------*/

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

    Boolean once = true;
    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;

        LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());
        pickupLocation2 = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(18)); //value goes from 1 - 21

        try{
            if (once){
                if (!getIntent().getExtras().getString("hospitalId").isEmpty()){
                    requestHospitalId = getIntent().getExtras().getString("hospitalId");
                    once = false;
                    getDonorToHospital();
                }
            }
        }
        catch (Exception e){

        }
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
        //To stop the request if hospital is not found for long time
        if (stopRequestH){
            geoQueryH.removeAllListeners();
            return;
        }
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
                                    DatabaseReference driverRef2 = FirebaseDatabase.getInstance().getReference().child("customerRequestH").child(userID1);
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
                                //Enabling the navigation view of hospital
                                mNavigationMenu.findItem(R.id.show_hospital).setVisible(true);

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
        DatabaseReference findHospital = FirebaseDatabase.getInstance().getReference().child("customerRequestH").child(userID1);
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
                                        getHospitalInfo();
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
        Toast.makeText(this, "Toggle hospital information from Navigation Drawer", Toast.LENGTH_SHORT).show();
    }

    private void getHospitalInfo(){
        DatabaseReference hospiRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Hospital").child(hospitalFoundId);
        hospiRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    Map<String ,Object> newMap = (Map<String ,Object>) dataSnapshot.getValue();
                    String name = newMap.get("Name").toString();
                    String phone = newMap.get("Phone").toString();
                    String address = newMap.get("Address").toString();

                    mHospitalName.setText(name);
                    mHospitalNumber.setText(phone);
                    mhospitalAddress.setText(address);
                    mHospi.setText("Hospital Found");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getDonorToHospital(){
        DatabaseReference hospitalLocation = FirebaseDatabase.getInstance().getReference().child("Users").child("Hospital").child(requestHospitalId).child("l");
        hospitalLocation.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    if (dataSnapshot.exists() && !requestHospitalId.equals("")) {
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
                .waypoints(pickupLocation2, destinationLatLng)
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

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            erasePolyLines();
            if (destinationMarker != null){
                destinationMarker.remove();
            }
        }
        return super.onKeyDown(keyCode, event);
    }
}