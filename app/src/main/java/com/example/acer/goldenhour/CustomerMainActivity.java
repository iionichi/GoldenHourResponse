package com.example.acer.goldenhour;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.MenuItem;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Map;
import java.util.Vector;

public class CustomerMainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private static final int SEND_SMS_PERMISSION_REQUEST_CODE = 111;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;
    private NavigationView mNavigationView;
    private String  mePhone, msg, userID1, bloodGroup, rhFactor;
    private Boolean donate = false;
    private DatabaseReference mCustomerDatabase1;
    private FirebaseAuth mAuth1;
    private Vector hospitalNameVector, requestIdVector, hospitalIdVector;

    private ListView mHospitalList;
    private ArrayList<String> mHospitalNames = new ArrayList<>();
    private ArrayAdapter<String> mHospitalNamesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_main);

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

        //Creating an ArrayList for the listview and linking it with an array adapter
        mHospitalList = findViewById(R.id.donorsList);
        mHospitalNamesAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, mHospitalNames);
        mHospitalList.setAdapter(mHospitalNamesAdapter);
        mHospitalList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String reqId = hospitalIdVector.get(i).toString();
                Intent donorIntent = new Intent(CustomerMainActivity.this,CustomerMapActivity.class);
                donorIntent.putExtra("hospitalId",reqId);
                startActivity(donorIntent);
            }
        });

        hospitalIdVector = new Vector();
        hospitalNameVector = new Vector();
        requestIdVector = new Vector();

        getDonor();//Get the request list for the blood donation
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
                Intent intent = new Intent(CustomerMainActivity.this, CustomerSettingsActivity.class);
                startActivity(intent);
                break;

            case R.id.get_ambulance:
                Intent intent1 = new Intent(CustomerMainActivity.this, CustomerMapActivity.class);
                intent1.putExtra("hospitalId","");
                startActivity(intent1);
                break;

            case R.id.ambu_history:
                Intent intent5 = new Intent(CustomerMainActivity.this, HistoryActivty.class);
                intent5.putExtra("customerOrDriver", "Customers");
                startActivity(intent5);
                break;

            case R.id.db:
//                Intent intent2 = new Intent(CustomerMainActivity.this, CustomerMainActivity.class);
//                startActivity(intent2);
                break;

            case R.id.call_police:
                final int REQUEST_PHONE_CALL = 1;
                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:100"));

                if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    if(ContextCompat.checkSelfPermission(CustomerMainActivity.this, android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED){
                        ActivityCompat.requestPermissions(CustomerMainActivity.this, new String[]{Manifest.permission.CALL_PHONE}, REQUEST_PHONE_CALL);
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
                        if (checkPermission(Manifest.permission.SEND_SMS)) {
                            SmsManager smsManager = SmsManager.getDefault();
                            smsManager.sendTextMessage(mePhone, null, msg, null, null);
                        } else {
                            Toast.makeText(CustomerMainActivity.this, "Permission denied", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
                break;

            case R.id.Log_out:
                FirebaseAuth.getInstance().signOut();//Signing Out
                //Deleting Entry in logged in node
                String deviceId = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
                DatabaseReference checkLoggedIn =  FirebaseDatabase.getInstance().getReference().child("LoggedIn").child(deviceId);
                checkLoggedIn.removeValue();
                Intent intent6 = new Intent(CustomerMainActivity.this, MainActivity.class);
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
                if(grantResults.length > 0 && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {

                }
                break;
        }
    }

    private void getDonor(){
        DatabaseReference ifDonate = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(userID1);
        ifDonate.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    Map<String, Object> mMap = (Map<String, Object>) dataSnapshot.getValue();
                    if (mMap.get("donate").toString().equals("yes")){
                        donate = true;
                        bloodGroup = mMap.get("BloodGroup").toString();
                        rhFactor = mMap.get("RHFactor").toString();
                        updateDonorList();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    String hospitalName;
    Boolean hName = false;
    private void updateDonorList(){
        if (donate){
            DatabaseReference donorList = FirebaseDatabase.getInstance().getReference().child("donorRequest").child(bloodGroup).child(rhFactor);
            donorList.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    if (dataSnapshot.exists()){
                        String requestKey = dataSnapshot.getKey();//Unique Key for every request
                        Map<String, Object> newMap = (Map<String, Object>) dataSnapshot.getValue();
                        final String hospitalId = newMap.get("hospitalId").toString();//Getting the hospital Id
                        DatabaseReference hospitalNameReference = FirebaseDatabase.getInstance().getReference()
                                .child("Users").child("Hospital").child(hospitalId).child("Name");
                        hospitalNameReference.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()){
                                    hospitalName = dataSnapshot.getValue(String.class);//Getting the hospital name
                                    hName = true;
                                    mHospitalNames.add(hospitalName);
                                    mHospitalNamesAdapter.notifyDataSetChanged();
                                    hospitalIdVector.addElement(hospitalId);
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                            }
                        });
//                        hospitalNameVector.addElement(hospitalName);
//                        requestIdVector.addElement(requestKey);
                    }
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()){
                        String requestKey = dataSnapshot.getKey();//Unique Key for every request
                        Map<String, Object> newMap = (Map<String, Object>) dataSnapshot.getValue();
                        final String hospitalId = newMap.get("hospitalId").toString();//Getting the hospital Id
                        DatabaseReference hospitalNameReference = FirebaseDatabase.getInstance().getReference()
                                .child("Users").child("Hospital").child(hospitalId).child("Name");
                        hospitalNameReference.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()){
                                    hospitalName = dataSnapshot.getValue(String.class);//Getting the hospital name
                                    mHospitalNames.remove(hospitalName);
                                    mHospitalNamesAdapter.notifyDataSetChanged();
                                    hospitalIdVector.remove(hospitalId);
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
//                        hospitalNameVector.remove(hospitalName);
//                        requestIdVector.remove(requestKey);
                    }
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        }
    }
}
