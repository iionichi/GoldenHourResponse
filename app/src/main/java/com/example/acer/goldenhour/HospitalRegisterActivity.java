package com.example.acer.goldenhour;


import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.provider.Settings;
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

import java.util.HashMap;
import java.util.Map;


public class HospitalRegisterActivity extends AppCompatActivity {

    static final int REQUEST_LOCATION = 1;
    LocationManager locationManager;
    Location mLastLocation;
    LocationRequest mLocationRequest;

    private EditText mEmail, mPassword,mName,mPhone,mAddress;
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
        mName = (EditText) findViewById(R.id.name);
        mAddress = (EditText) findViewById(R.id.address);
        mPhone = (EditText) findViewById(R.id.phone);

        mRegistration = (Button) findViewById(R.id.registration);

        mRegistration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String email = mEmail.getText().toString();
                final String password = mPassword.getText().toString();
                final String name = mName.getText().toString();
                final String address = mAddress.getText().toString();
                final String phone = mPhone.getText().toString();
                if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(name) || TextUtils.isEmpty(address) || TextUtils.isEmpty(phone) ) {
                    Toast.makeText(HospitalRegisterActivity.this, "Please Fill All The Fields", Toast.LENGTH_SHORT).show();
                } else {
                    mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(HospitalRegisterActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (!task.isSuccessful()) {
                                Toast.makeText(HospitalRegisterActivity.this, "Sign Up Error", Toast.LENGTH_SHORT).show();
                            } else {
                                String userId = mAuth.getCurrentUser().getUid();
                                addHospitalLogin(userId);
//



                                DatabaseReference refHospital = FirebaseDatabase.getInstance().getReference().child("Users").child("Hospital");

                                GeoFire geoFireHospital = new GeoFire(refHospital);
                                geoFireHospital.setLocation(userId, new GeoLocation(latitude, longitude));




                                Intent intent = new Intent(HospitalRegisterActivity.this, HospitalActivity.class);
                                startActivity(intent);
                                finish();

                                saveUserInfo();




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
    private void saveUserInfo(){
        final String name = mName.getText().toString();
        final String address = mAddress.getText().toString();
        final String phone = mPhone.getText().toString();
        String userId1 = mAuth.getCurrentUser().getUid();
        addHospitalLogin(userId1);
        DatabaseReference currentUserDB = FirebaseDatabase.getInstance().getReference().child("Users").child("Hospital").child(userId1);
        Map userData = new HashMap();

        userData.put("Address",address);
        userData.put("Phone",phone);
        userData.put("Name",name);
        currentUserDB.updateChildren(userData);

    }

    private void addHospitalLogin(String userId){
        String deviceId = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        DatabaseReference addHospital = FirebaseDatabase.getInstance().getReference().child("LoggedIn").child(deviceId);
        Map userInfo = new HashMap();
        userInfo.put("Type","Hospital");
        userInfo.put("Id",userId);
        addHospital.updateChildren(userInfo);
    }
}


