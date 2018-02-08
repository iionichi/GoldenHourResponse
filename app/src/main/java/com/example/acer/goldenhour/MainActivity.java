package com.example.acer.goldenhour;

import android.content.Intent;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;

public class MainActivity extends AppCompatActivity {



    private String userType,type;
    private Button mNext;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mNext = (Button) findViewById(R.id.next);
        Spinner Role = (Spinner) findViewById(R.id.role);

        ArrayAdapter<String> myAdapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.role));
        myAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        Role.setAdapter(myAdapter);

        startService(new Intent(MainActivity.this, onAppKilled.class));

        Role.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0){

                    type="customer";


                }

                else if (position==1){

                    type="driver";

                }

                else if (position==2){

                    type="hospital";

                }

                else if (position==3){

                    type="unregistered";

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        mNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (type == "customer"){
                    Intent intent = new Intent(MainActivity.this, CustomerLoginActivity.class);
                    startActivity(intent);
                    return;

                }

                else if(type == "driver") {
                    Intent intent = new Intent(MainActivity.this, DriverLoginActivity.class);
                    startActivity(intent);
                    return;
                }

                else if (type == "hospital"){
                    Intent intent = new Intent(MainActivity.this, HospitalLoginActivity.class);
                    startActivity(intent);
                    return;
                }

                else if (type == "unregistered"){
                    Task<AuthResult> resultTask = mAuth.signInAnonymously();
                    resultTask.addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                        @Override
                        public void onSuccess(AuthResult authResult) {

                            Intent intent = new Intent(MainActivity.this, StrangerMapActivity.class);
                            startActivity(intent);
                            finish();
                            return;
                        }
                    });
                }
            }
        });


        mAuth = FirebaseAuth.getInstance();
        firebaseAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null && type != "unregistered"){
                    String deviceId = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
                    DatabaseReference checkLoggedIn =  FirebaseDatabase.getInstance().getReference().child("LoggedIn").child(deviceId);
                    checkLoggedIn.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()){
                                Map<String, Object> map =  (Map<String, Object>) dataSnapshot.getValue();
                                userType = map.get("Type").toString();

                                if (userType.equals("Customers")){
                                    Intent intentC = new Intent(MainActivity.this,CustomerMapActivity.class);
                                    startActivity(intentC);
                                    finish();
                                    return;
                                }
                                else if (userType.equals("Drivers")){
                                    Intent intentC = new Intent(MainActivity.this,DriverMapActivity.class);
                                    startActivity(intentC);
                                    finish();
                                    return;
                                }
                                else if (userType.equals("Hospital")){
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
                else {

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
