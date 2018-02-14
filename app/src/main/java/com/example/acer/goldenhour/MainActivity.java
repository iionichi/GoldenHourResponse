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
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
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

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {



    private String userType,type,typeC;
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
                else if (position==4){
                    type = "admin";
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

                else if (type == "admin"){
                    Intent intent = new Intent(MainActivity.this, AdminActivity.class);
                    startActivity(intent);

                    return;
                }

                else if (type == "unregistered"){
                    Task<AuthResult> resultTask = mAuth.signInAnonymously();
                    resultTask.addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                        @Override
                        public void onSuccess(AuthResult authResult) {
                            final String key = FirebaseAuth.getInstance().getCurrentUser().getUid();
                            final DatabaseReference anonymousReference = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers");
                            anonymousReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()){
                                        DatabaseReference addAnon = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(key);
                                        HashMap strangerMap2 = new HashMap();
                                        strangerMap2.put("type","Anonymous");
                                        addAnon.updateChildren(strangerMap2);
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
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

                if (user != null){
                    String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    DatabaseReference anonCheck = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(userId).child("type");
                    anonCheck.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()){
                                if (dataSnapshot.getValue().toString().equals("Anonymous")){
                                    try{
                                        String userId2 = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                        FirebaseUser aUser = FirebaseAuth.getInstance().getCurrentUser();
                                        aUser.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                FirebaseAuth.getInstance().signOut();
                                                Toast.makeText(MainActivity.this, "User Deleted", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                        DatabaseReference strangerRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(userId2).child("type");
                                        strangerRef.removeValue();
                                    }catch (Exception e){

                                    }
                                }
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }

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
                                else {
                                    String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                    DatabaseReference anonymousUser = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(userId);
                                    anonymousUser.addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.exists()){
                                                Map<String, Object> mMap = (Map<String,Object>) dataSnapshot.getValue();
                                                if (mMap.get("type").equals("Anonymous")){
                                                    Intent intentC = new Intent(MainActivity.this,StrangerMapActivity.class);
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
