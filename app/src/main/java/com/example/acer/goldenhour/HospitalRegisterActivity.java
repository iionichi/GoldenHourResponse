package com.example.acer.goldenhour;


import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class HospitalRegisterActivity extends AppCompatActivity {

    static final int REQUEST_LOCATION = 1;
    LocationManager locationManager;
    Location mLastLocation;
    LocationRequest mLocationRequest;

    private EditText mEmail, mPassword;
    private Button mRegistration;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthListener;


    private double latitude;
    private double longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hospital_register);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        getLocation();


        mAuth = FirebaseAuth.getInstance();



        firebaseAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {
                    Intent intent = new Intent(HospitalRegisterActivity.this, HospitalActivity.class);
                    startActivity(intent);
                    finish();
                    return;
                }


            }
        };
        mEmail = (EditText) findViewById(R.id.email);
        mPassword = (EditText) findViewById(R.id.password);

        mRegistration = (Button) findViewById(R.id.registration);

        mRegistration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String email = mEmail.getText().toString();
                final String password = mPassword.getText().toString();
                if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                    Toast.makeText(HospitalRegisterActivity.this, "Please Fill All The Fields", Toast.LENGTH_SHORT).show();
                } else {
                    mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(HospitalRegisterActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (!task.isSuccessful()) {
                                Toast.makeText(HospitalRegisterActivity.this, "Sign Up Error", Toast.LENGTH_SHORT).show();
                            } else {
                                String userId = mAuth.getCurrentUser().getUid();
//                            DatabaseReference currentUserDB = FirebaseDatabase.getInstance().getReference().child("Users").child("Hospital").child(userId);
//                            currentUserDB.setValue(true);
//                            Map locationInfo = new HashMap();
//                            locationInfo.put("Longitude", longitude);
//                            locationInfo.put("Latitude", latitude);
//                            currentUserDB.updateChildren(locationInfo);

                                DatabaseReference refHospital = FirebaseDatabase.getInstance().getReference().child("Hospital");
                                GeoFire geoFireHospital = new GeoFire(refHospital);
                                geoFireHospital.setLocation(userId, new GeoLocation(latitude, longitude));
                                Intent intent = new Intent(HospitalRegisterActivity.this, HospitalActivity.class);
                                startActivity(intent);
                                finish();
                                return;

                            }
                        }
                    });
                }
            }
        });

    }

    void getLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);

        } else {
            Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

            if (location != null) {
                double latti = location.getLatitude();
                double longi = location.getLongitude();

                ((EditText) findViewById(R.id.etLocationLat)).setText("Latitude: " + latti);
                ((EditText) findViewById(R.id.etLocationLong)).setText("Longitude: " + longi);

                latitude = latti;
                longitude = longi;
            } else {
                ((EditText) findViewById(R.id.etLocationLat)).setText("Unable to find correct location.");
                ((EditText) findViewById(R.id.etLocationLong)).setText("Unable to find correct location. ");
            }
        }


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_LOCATION:
                getLocation();
                break;
        }
    }
}


