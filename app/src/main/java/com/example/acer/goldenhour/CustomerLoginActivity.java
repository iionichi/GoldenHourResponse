package com.example.acer.goldenhour;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
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
import com.wang.avi.AVLoadingIndicatorView;

import java.util.HashMap;
import java.util.Map;

public class CustomerLoginActivity extends AppCompatActivity {

    private EditText mEmail,mPassword;
    private Button mLogin,mRegistration,mLoginPhone;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthListener;

    Dialog loaderDialog;
    AVLoadingIndicatorView avi;
    TextView loaderText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_login);

        loaderDialog = new Dialog(this);
        loaderDialog.setContentView(R.layout.loading_file_main);
        avi = (AVLoadingIndicatorView) loaderDialog.findViewById(R.id.aviLoader);
        loaderText = (TextView) loaderDialog.findViewById(R.id.loadingText);

        mAuth = FirebaseAuth.getInstance();
        firebaseAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
//                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
//                if(user != null){
//                    Intent intent = new Intent(CustomerLoginActivity.this,CustomerMainActivity.class);
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
        mLoginPhone = (Button) findViewById(R.id.phone_login);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mRegistration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String email = mEmail.getText().toString();
                final String password = mPassword.getText().toString();

                if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                    loaderDialog.dismiss();
                    Toast.makeText(CustomerLoginActivity.this, "Please Fill All The Fields", Toast.LENGTH_SHORT).show();
                } else {
                    loaderText.setText("Checking Credentials");
                    loaderDialog.show();
                    loaderDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));//Background color for dialog
                    loaderDialog.show();//Show the dialog
                    loaderDialog.setCanceledOnTouchOutside(false);
                    avi.smoothToShow();

                    mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(CustomerLoginActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (!task.isSuccessful()) {
                                loaderText.setText("SignUp Error");
                                loaderDialog.dismiss();
                                Toast.makeText(CustomerLoginActivity.this, "Sign Up Error", Toast.LENGTH_SHORT).show();
                            } else {
                                loaderText.setText("Registration Successful");
                                String userId = mAuth.getCurrentUser().getUid();
                                DatabaseReference currentUserDB = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(userId);
                                currentUserDB.setValue(true);
                                addCustomerLogin(userId);//For adding the customer logged in.
                                loaderDialog.dismiss();
                            }
                        }
                    });

                }
            }
        });

        mLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String email = mEmail.getText().toString();
                final String password = mPassword.getText().toString();

                if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                    Toast.makeText(CustomerLoginActivity.this, "Please Fill All The Fields", Toast.LENGTH_SHORT).show();
                } else
                {
                    loaderText.setText("Checking Credentials");
                    loaderDialog.show();
                    loaderDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));//Background color for dialog
                    loaderDialog.show();//Show the dialog
                    loaderDialog.setCanceledOnTouchOutside(false);
                    avi.smoothToShow();

                    mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(CustomerLoginActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (!task.isSuccessful()) {
                                loaderText.setText("Sign In Error");
                                loaderDialog.dismiss();
                                Toast.makeText(CustomerLoginActivity.this, "Sign In Error", Toast.LENGTH_SHORT).show();
                            }
                            else {
                                final String userId = mAuth.getCurrentUser().getUid();
                                DatabaseReference checkCustomer = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(userId);
                                checkCustomer.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.exists()){
                                            loaderText.setText("Signing in as Customer");

                                            addCustomerLogin(userId);//For adding the customer logged in.

                                            loaderDialog.dismiss();

                                            Intent intent1 = new Intent(CustomerLoginActivity.this,CustomerMapActivity.class);
                                            startActivity(intent1);
                                            finish();
                                            return;
                                        }
                                        else {
                                            loaderText.setText("Wrong Login");
                                            loaderDialog.dismiss();

                                            FirebaseAuth.getInstance().signOut();
                                            Toast.makeText(CustomerLoginActivity.this, "Wrong Login", Toast.LENGTH_SHORT).show();
                                            Intent intent1 = new Intent(CustomerLoginActivity.this,MainActivity.class);
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

        mLoginPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CustomerLoginActivity.this, AuthActivity.class);
                startActivity(intent);
                return;
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

    private void addCustomerLogin(String userId){
        String DeviceID = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        DatabaseReference device = FirebaseDatabase.getInstance().getReference().child("LoggedIn").child(DeviceID);
        Map userInfo = new HashMap();
        userInfo.put("Type","Customers");
        userInfo.put("Id", userId);
        device.updateChildren(userInfo);
    }
}
