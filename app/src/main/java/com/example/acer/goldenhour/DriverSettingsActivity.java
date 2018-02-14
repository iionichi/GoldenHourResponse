package com.example.acer.goldenhour;

import android.*;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class DriverSettingsActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{



    private DrawerLayout mDrawerLayoutDriver;
    private ActionBarDrawerToggle mToggleDriver;
    private NavigationView mNavigationView;


    private EditText mNameField, mPhoneField, mAmbulanceField, mAmbulanceNumberField;
    private Button mBack, mConfirm;

    private FirebaseAuth mAuth;
    private DatabaseReference mDriverDatabase;

    private String userID;
    private String mName;
    private String mPhone;
    private String mAmbulance;
    private String mAmbulanceNumber;
    private String mService;

    private RadioGroup mRadioGroup;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_settings);


        mDrawerLayoutDriver = (DrawerLayout) findViewById(R.id.drawerD);
        mToggleDriver = new ActionBarDrawerToggle(this,mDrawerLayoutDriver,R.string.open,R.string.close);
        mDrawerLayoutDriver.addDrawerListener(mToggleDriver);
        mToggleDriver.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mNavigationView = findViewById(R.id.nv1);

        if (mNavigationView != null){
            mNavigationView.setNavigationItemSelectedListener(this);
        }

        mNameField = (EditText) findViewById(R.id.name);
        mPhoneField = (EditText) findViewById(R.id.phoneEditText);
        mAmbulanceField = (EditText) findViewById(R.id.ambulance);
        mAmbulanceNumberField = (EditText) findViewById(R.id.ambulance_number);

        mRadioGroup = (RadioGroup) findViewById(R.id.radioGroup);

        mBack = (Button) findViewById(R.id.back);
        mConfirm = (Button) findViewById(R.id.confirm);

        mAuth = FirebaseAuth.getInstance();
        userID = mAuth.getCurrentUser().getUid();
        mDriverDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(userID);

        getUserInfo();

        mConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveUserInformation();
            }
        });

        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                return;
            }
        });
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item_driver) {
        if (mToggleDriver.onOptionsItemSelected(item_driver)) {
            return true;
        }
        return super.onOptionsItemSelected(item_driver);
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        switch (id){
            case R.id.Patient_Service:
                Intent intent = new Intent(DriverSettingsActivity.this, DriverMapActivity.class);
                startActivity(intent);
                break;

            case R.id.call_policeD:
                final int REQUEST_PHONE_CALL = 1;
                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:100"));

                if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    if(ContextCompat.checkSelfPermission(DriverSettingsActivity.this, android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED){
                        ActivityCompat.requestPermissions(DriverSettingsActivity.this, new String[]{android.Manifest.permission.CALL_PHONE}, REQUEST_PHONE_CALL);
                    }
                    else {
                        startActivity(callIntent);
                    }
                }
                break;

            case R.id.profile_settings_driver:
//                Intent intent1 = new Intent(DriverSettingsActivity.this, DriverSettingsActivity.class);
//                startActivity(intent1);
                break;


        }
        return true;
    }

    private void getUserInfo(){
        mDriverDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if(map.get("name") != null){
                        mName = map.get("name").toString();
                        mNameField.setText(mName);
                    }
                    if(map.get("phone") != null){
                        mPhone = map.get("phone").toString();
                        mPhoneField.setText(mPhone);
                    }
                    if(map.get("ambulance") != null){
                        mAmbulance = map.get("ambulance").toString();
                        mAmbulanceField.setText(mAmbulance);
                    }
                    if(map.get("service") != null){
                        mService = map.get("service").toString();
                        switch (mService){
                            case "Normal" :
                                mRadioGroup.check(R.id.normalAmbulance);
                                break;
                            case "Cardiac" :
                                mRadioGroup.check(R.id.cardiacAmbulance);
                                break;
                        }
                    }
                    if(map.get("ambulanceNumber") != null){
                        mAmbulanceNumber = map.get("ambulanceNumber").toString();
                        mAmbulanceNumberField.setText(mAmbulanceNumber);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void saveUserInformation() {
        mName = mNameField.getText().toString();
        mPhone = mPhoneField.getText().toString();
        mAmbulance = mAmbulanceField.getText().toString();
        mAmbulanceNumber = mAmbulanceNumberField.getText().toString();

        int selectId = mRadioGroup.getCheckedRadioButtonId();

        final RadioButton radioButton = (RadioButton)  findViewById(selectId);

        if(radioButton.getText() == null){
            return;
        }

        mService = radioButton.getText().toString();

        Map userInfo = new HashMap();
        userInfo.put("name", mName);
        userInfo.put("phone", mPhone);
        userInfo.put("ambulance", mAmbulance);
        userInfo.put("service", mService);
        userInfo.put("ambulanceNumber", mAmbulanceNumber);
        mDriverDatabase.updateChildren(userInfo);

        finish();
    }
}
