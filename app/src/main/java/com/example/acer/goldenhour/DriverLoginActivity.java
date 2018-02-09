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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class DriverLoginActivity extends AppCompatActivity {

    private EditText mEmail,mPassword;
    private Button mLogin,mRegistration;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_login);

        mAuth = FirebaseAuth.getInstance();

        firebaseAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if(user != null){
                    Intent intent = new Intent(DriverLoginActivity.this,DriverMainActivity.class);
                    startActivity(intent);
                    finish();
                    return;
                }
            }
        };

        mEmail = (EditText) findViewById(R.id.email);
        mPassword = (EditText) findViewById(R.id.password);

        mLogin = (Button) findViewById(R.id.login);
        mRegistration = (Button) findViewById(R.id.registration);

        mRegistration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                Intent intent = new Intent(DriverLoginActivity.this, DriverRegisterActivity.class);
                startActivity(intent);

                return;
//                final String email = mEmail.getText().toString();
//                final String password = mPassword.getText().toString();
//                if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
//                    Toast.makeText(DriverLoginActivity.this, "Please Fill All The Fields", Toast.LENGTH_SHORT).show();
//                } else {
//                    mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(DriverLoginActivity.this, new OnCompleteListener<AuthResult>() {
//                        @Override
//                        public void onComplete(@NonNull Task<AuthResult> task) {
//                            if (!task.isSuccessful()) {
//                                Toast.makeText(DriverLoginActivity.this, "Sign Up Error", Toast.LENGTH_SHORT).show();
//                            } else {
//                                String userId = mAuth.getCurrentUser().getUid();
//                                DatabaseReference currentUserDB = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(userId).child("name");
//                                currentUserDB.setValue(email);
//                                addAmbulanceLogin(userId);//Adding Ambulance Login
//                            }
//                        }
//                    });
//                }
            }
        });

        mLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String email = mEmail.getText().toString();
                final String password = mPassword.getText().toString();

                if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                    Toast.makeText(DriverLoginActivity.this, "Please Fill All The Fields", Toast.LENGTH_SHORT).show();
                } else {

                    mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(DriverLoginActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (!task.isSuccessful()) {
                                Toast.makeText(DriverLoginActivity.this, "Sign In Error", Toast.LENGTH_SHORT).show();
                            }
                            else {
                                String userId = mAuth.getCurrentUser().getUid();
                                addAmbulanceLogin(userId);//Adding Ambulance Login
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
    private void addAmbulanceLogin(String userId){
        String deviceId = Settings.Secure.getString(getApplicationContext().getContentResolver(),Settings.Secure.ANDROID_ID);
        DatabaseReference device = FirebaseDatabase.getInstance().getReference().child("LoggedIn").child(deviceId);
        Map userInfo = new HashMap();
        userInfo.put("Type","Drivers");
        userInfo.put("Id",userId);
        device.updateChildren(userInfo);
    }
}
