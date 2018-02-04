package com.example.acer.goldenhour;

import android.content.Intent;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private Button mDriver, mCustomer,mHospital,mButton;

    private String userType,user;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDriver = (Button) findViewById(R.id.driver);
        mCustomer = (Button) findViewById(R.id.customer);
        mHospital = (Button) findViewById(R.id.hospital);
        mButton = findViewById(R.id.button2);
        startService(new Intent(MainActivity.this, onAppKilled.class));

        mDriver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, DriverLoginActivity.class);
                startActivity(intent);
                finish();
                return;
            }
        });

        mCustomer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, CustomerLoginActivity.class);
                startActivity(intent);
                finish();
                return;
            }
        });
        mHospital.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, HospitalLoginActivity.class);
                startActivity(intent);
                finish();
                return;

            }
        });

        mAuth = FirebaseAuth.getInstance();
        firebaseAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null){
                    String deviceId = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
                    DatabaseReference checkLoggedIn =  FirebaseDatabase.getInstance().getReference().child("LoggedIn").child(deviceId);
                    checkLoggedIn.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()){
                                Map<String, Object> map =  (Map<String, Object>) dataSnapshot.getValue();
                                userType = map.get("Type").toString();
                                mButton.setText(userType);
                                if (userType.equals("Customers")){
                                    mButton.setText(userType);
                                    Intent intentC = new Intent(MainActivity.this,CustomerMapActivity.class);
                                    startActivity(intentC);
                                    finish();
                                    return;
                                }
                                else if (userType.equals("Drivers")){
                                    mButton.setText(userType);
                                    Intent intentC = new Intent(MainActivity.this,DriverMapActivity.class);
                                    startActivity(intentC);
                                    finish();
                                    return;
                                }
                                else if (userType.equals("Hospital")){
                                    mButton.setText(userType);
                                    Intent intentC = new Intent(MainActivity.this,HospitalActivity.class);
                                    startActivity(intentC);
                                    finish();
                                    return;
                                }
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(firebaseAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAuth.removeAuthStateListener(firebaseAuthListener);
    }
}
