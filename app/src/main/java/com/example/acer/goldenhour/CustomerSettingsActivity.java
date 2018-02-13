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
import android.telephony.SmsManager;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class CustomerSettingsActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private EditText mNameField, mPhoneField, mePhoneField,mMedicompanyField,mMedinoField;
    private Button mBack, mConfirm;

    private FirebaseAuth mAuth;
    private DatabaseReference mCustomerDatabase;

    private String userID;
    private String mName;
    private String mPhone;
    private String mMedicompany;
    private String mMedino;

    private static final int SEND_SMS_PERMISSION_REQUEST_CODE = 111;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;
    private NavigationView mNavigationView;
    private DatabaseReference mCustomerDatabase1;
    private String userID1;
    private FirebaseAuth mAuth1;
    private String  mePhone, msg;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_settings);


        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer);
        mToggle = new ActionBarDrawerToggle(this,mDrawerLayout,R.string.open,R.string.close);
        mDrawerLayout.addDrawerListener(mToggle);
        mToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mAuth1 = FirebaseAuth.getInstance();
        userID1 = mAuth1.getCurrentUser().getUid();
        mCustomerDatabase1 = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(userID1);


        mNavigationView = findViewById(R.id.nv);

        if (mNavigationView != null){
            mNavigationView.setNavigationItemSelectedListener(this);
        }


        mNameField = (EditText) findViewById(R.id.name);
        mPhoneField = (EditText) findViewById(R.id.phoneEditText);
        mePhoneField = (EditText) findViewById(R.id.ephone);
        mMedicompanyField = findViewById(R.id.medicompany);
        mMedinoField = findViewById(R.id.medino);

        mBack = (Button) findViewById(R.id.back);
        mConfirm = (Button) findViewById(R.id.confirm);

        mAuth = FirebaseAuth.getInstance();
        userID = mAuth.getCurrentUser().getUid();
        mCustomerDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(userID);

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
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mToggle.onOptionsItemSelected(item)) {

            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        switch (id){
            case R.id.profile_settings:
//                Intent intent = new Intent(CustomerSettingsActivity.this, CustomerSettingsActivity.class);
//                startActivity(intent);
                break;

            case R.id.get_ambulance:
                Intent intent1 = new Intent(CustomerSettingsActivity.this, CustomerMapActivity.class);
                startActivity(intent1);
                break;

            case R.id.ambu_history:
                Intent intent5 = new Intent(CustomerSettingsActivity.this, HistoryActivty.class);
                intent5.putExtra("customerOrDriver", "Customers");
                startActivity(intent5);
                break;

            case R.id.db:
                Intent intent2 = new Intent(CustomerSettingsActivity.this, CustomerMainActivity.class);
                startActivity(intent2);
                break;


            case R.id.call_police:
                final int REQUEST_PHONE_CALL = 1;
                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:100"));

                if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    if(ContextCompat.checkSelfPermission(CustomerSettingsActivity.this, android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED){
                        ActivityCompat.requestPermissions(CustomerSettingsActivity.this, new String[]{android.Manifest.permission.CALL_PHONE}, REQUEST_PHONE_CALL);
                    }
                    else {
                        startActivity(callIntent);
                    }
                }
                break;


            case R.id.sos:
                mCustomerDatabase1.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0) {
                            Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                            if (map.get("ephone") != null) {
                                mePhone = map.get("ephone").toString();
                            }
                        }

                        msg = "Sender is in critical emergancy.";

                        if (checkPermission(android.Manifest.permission.SEND_SMS)) {
                            SmsManager smsManager = SmsManager.getDefault();
                            smsManager.sendTextMessage(mePhone, null, msg, null, null);
                        } else {
                            Toast.makeText(CustomerSettingsActivity.this, "Permission denied", Toast.LENGTH_SHORT).show();
                        }


                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                break;



            case R.id.Log_out:
                FirebaseAuth.getInstance().signOut();
                Intent intent6 = new Intent(CustomerSettingsActivity.this, MainActivity.class);
                startActivity(intent6);
                finish();
                break;




        }
        return true;
    }

    private Boolean checkPermission(String permission) {
        int checkPermission = ContextCompat.checkSelfPermission(this, permission);
        return checkPermission == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case SEND_SMS_PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {

                }
                break;
        }

    }

    private void getUserInfo(){
        mCustomerDatabase.addValueEventListener(new ValueEventListener() {
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

                    if(map.get("ephone") != null){
                        mePhone = map.get("ephone").toString();
                        mePhoneField.setText(mePhone);
                    }
                    if(map.get("Medicompany") != null){
                        mMedicompany = map.get("Medicompany").toString();
                        mMedicompanyField.setText(mMedicompany);
                    }

                    if(map.get("Medino") != null){
                        mMedino = map.get("Medino").toString();
                        mMedinoField.setText(mMedino);
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
        mePhone = mePhoneField.getText().toString();
        mMedicompany = mMedicompanyField.getText().toString();
        mMedino = mMedinoField.getText().toString();

        Map userInfo = new HashMap();
        userInfo.put("name", mName);
        userInfo.put("phone", mPhone);
        userInfo.put("ephone",mePhone);
        userInfo.put("Medicompany",mMedicompany);
        userInfo.put("Medino",mMedino);
        mCustomerDatabase.updateChildren(userInfo);

        finish();
    }
}
