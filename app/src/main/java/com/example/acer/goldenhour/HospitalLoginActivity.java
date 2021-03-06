package com.example.acer.goldenhour;

import android.content.Intent;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
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

public class HospitalLoginActivity extends AppCompatActivity {

    private EditText mEmail,mPassword;
    private Button mLogin,mRegistration;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hospital_login);

        mAuth = FirebaseAuth.getInstance();

        firebaseAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
//                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
//                if(user != null){
//                    Intent intent = new Intent(HospitalLoginActivity.this,HospitalActivity.class);
//                    startActivity(intent);
//                    finish();
//                    return;
//                }
            }
        };

        mEmail = (EditText) findViewById(R.id.email);
        mPassword = (EditText) findViewById(R.id.password);

        mLogin = (Button) findViewById(R.id.login);
        mRegistration = (Button) findViewById(R.id.registration);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mRegistration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(HospitalLoginActivity.this, HospitalRegisterActivity.class);
                startActivity(intent);

                return;
//                final String email = mEmail.getText().toString();
//                final String password = mPassword.getText().toString();
//                mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(HospitalLoginActivity.this, new OnCompleteListener<AuthResult>() {
//                    @Override
//                    public void onComplete(@NonNull Task<AuthResult> task) {
//                        if(!task.isSuccessful()){
//                            Toast.makeText(HospitalLoginActivity.this, "Sign Up Error", Toast.LENGTH_SHORT).show();
//                        }
//                        else {
//                            String userId = mAuth.getCurrentUser().getUid();
//                            DatabaseReference currentUserDB = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(userId);
//                            currentUserDB.setValue(true);
//                        }
//                    }
//                });
            }
        });

        mLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String email = mEmail.getText().toString();
                final String password = mPassword.getText().toString();

                if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                    Toast.makeText(HospitalLoginActivity.this, "Please Fill All The Fields", Toast.LENGTH_SHORT).show();
                } else {
                    mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(HospitalLoginActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (!task.isSuccessful()) {
                                Toast.makeText(HospitalLoginActivity.this, "Sign In Error", Toast.LENGTH_SHORT).show();
                            }
                            else {
                                final String userId = mAuth.getCurrentUser().getUid();

                                DatabaseReference checkCustomer = FirebaseDatabase.getInstance().getReference().child("Users").child("Hospital").child(userId);
                                checkCustomer.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.exists()){
                                            addHospitalLogin(userId);
                                            Intent intent1 = new Intent(HospitalLoginActivity.this,HospitalActivity.class);
                                            startActivity(intent1);
                                            finish();
                                            return;
                                        }
                                        else {
                                            FirebaseAuth.getInstance().signOut();
                                            Toast.makeText(HospitalLoginActivity.this, "Wrong Login", Toast.LENGTH_SHORT).show();
                                            Intent intent1 = new Intent(HospitalLoginActivity.this,MainActivity.class);
                                            startActivity(intent1);
                                            finish();
                                            return;
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                            }
                        }
                    });
                }
            }
        });
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
    private void addHospitalLogin(String userId){
        String deviceId = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        DatabaseReference addHospital = FirebaseDatabase.getInstance().getReference().child("LoggedIn").child(deviceId);
        Map userInfo = new HashMap();
        userInfo.put("Type","Hospital");
        userInfo.put("Id",userId);
        addHospital.updateChildren(userInfo);
    }
}
