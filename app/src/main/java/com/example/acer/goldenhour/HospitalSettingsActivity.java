package com.example.acer.goldenhour;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
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

import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class HospitalSettingsActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{



    private DrawerLayout mDrawerLayoutHospital;
    private ActionBarDrawerToggle mToggleHospital;
    private NavigationView mNavigationView;


    private EditText mNameField, mPhoneField, mAddressField;
    private Button mBack, mConfirm;

    private FirebaseAuth mAuth;
    private DatabaseReference mHospitalDatabase;

    private String userID;
    private String mName;
    private String mPhone;
    private String mAddress;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hospital_settings);


        mDrawerLayoutHospital = (DrawerLayout) findViewById(R.id.drawerSaur);
        mToggleHospital = new ActionBarDrawerToggle(this,mDrawerLayoutHospital,R.string.open,R.string.close);
        mDrawerLayoutHospital.addDrawerListener(mToggleHospital);
        mToggleHospital.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mNavigationView = findViewById(R.id.nv2);

        if (mNavigationView != null){
            mNavigationView.setNavigationItemSelectedListener(this);
        }

        mNameField = (EditText) findViewById(R.id.name);
        mPhoneField = (EditText) findViewById(R.id.phone);
        mAddressField= (EditText) findViewById(R.id.address);


        mBack = (Button) findViewById(R.id.back);
        mConfirm = (Button) findViewById(R.id.confirm);

        mAuth = FirebaseAuth.getInstance();
        userID = mAuth.getCurrentUser().getUid();
        mHospitalDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Hospital").child(userID);

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
        if (mToggleHospital.onOptionsItemSelected(item_driver)) {
            return true;
        }
        return super.onOptionsItemSelected(item_driver);
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        switch (id){
            case R.id.dashboard:
                Intent intent = new Intent(HospitalSettingsActivity.this, HospitalActivity.class);
                startActivity(intent);
                break;

            case R.id.profile_settings_hospital:

                break;


        }
        return true;
    }

    private void getUserInfo(){
        mHospitalDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if(map.get("Name") != null){
                        mName = map.get("Name").toString();
                        mNameField.setText(mName);
                    }
                    if(map.get("Phone") != null){
                        mPhone = map.get("Phone").toString();
                        mPhoneField.setText(mPhone);
                    }
                    if(map.get("Address") != null){
                        mAddress = map.get("Address").toString();
                        mAddressField.setText(mAddress);
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
        mAddress = mAddressField.getText().toString();


        Map userInfo = new HashMap();
        userInfo.put("Name", mName);
        userInfo.put("Phone", mPhone);
        userInfo.put("Address", mAddress);

        mHospitalDatabase.updateChildren(userInfo);

        finish();
    }
}
