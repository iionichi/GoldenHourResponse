package com.example.acer.goldenhour;

import android.app.Dialog;
import android.content.Intent;
import android.provider.Settings;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class HospitalActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private String hospitalId, userId, bloodGroup, rhFactor;

    private Boolean requestDonor = false;

    private DrawerLayout mDrawerLayoutHospital;
    private ActionBarDrawerToggle mToggleHospital;
    private NavigationView mNavigationView;

    private TextView mtext;

    private ListView mUserList;
    private ArrayList<String> mCustomerNames = new ArrayList<>();
    ArrayAdapter<String> arrayAdapter;
    Vector customerIdVector = new Vector();

    Spinner mBloodGroup, mRHFactor;
    Dialog mGetDonorDialog, mGetUserInfoDialog;
    NavigationView mHospitalNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hospital);

        hospitalId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        mDrawerLayoutHospital = (DrawerLayout) findViewById(R.id.drawerSaur);
        mToggleHospital = new ActionBarDrawerToggle(this,mDrawerLayoutHospital,R.string.open,R.string.close);
        mDrawerLayoutHospital.addDrawerListener(mToggleHospital);
        mToggleHospital.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mNavigationView = findViewById(R.id.nv2);

        if (mNavigationView != null){
            mNavigationView.setNavigationItemSelectedListener(this);
        }

        //mtext = findViewById(R.id.hospitalText);
        mUserList = findViewById(R.id.customerNameList);
        arrayAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, mCustomerNames);
        mUserList.setAdapter(arrayAdapter);
        mUserList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String userID = customerIdVector.get(i).toString();
                showDialogBox(userID);
            }
        });

        mHospitalNavigation = findViewById(R.id.hospitalNavigation);
        mHospitalNavigation.setNavigationItemSelectedListener(this);
        if (mHospitalNavigation != null) {
            mHospitalNavigation.setNavigationItemSelectedListener(this);
        }

        mGetDonorDialog = new Dialog(this);
        mGetUserInfoDialog = new Dialog(this);

        getListOfPatients();
    }

    private void getListOfPatients(){
        DatabaseReference customersId = FirebaseDatabase.getInstance().getReference().child("Users").child("Hospital").child(hospitalId).child("customerRequestId");
        customersId.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                userId = dataSnapshot.getValue(String.class);
                customerIdVector.addElement(userId);

                DatabaseReference customerName = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(userId).child("name");
                customerName.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){
                            //Adding Names to the ListView
                            mCustomerNames.add(dataSnapshot.getValue(String.class));
                            arrayAdapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                userId = dataSnapshot.getValue(String.class);

                DatabaseReference customerName = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(userId).child("name");
                customerName.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){
                            //Adding Names to the ListView
                            mCustomerNames.remove(dataSnapshot.getValue(String.class));
                            arrayAdapter.notifyDataSetChanged();
                            customerIdVector.remove(userId);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
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
//                Intent intent = new Intent(HospitalActivity.this, HospitalActivity.class);
//                startActivity(intent);

                break;

            case R.id.settingH:
                Intent intent = new Intent(HospitalActivity.this, HospitalSettingsActivity.class);
                startActivity(intent);
                break;

            case R.id.logoutH:
                String deviceId = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
                DatabaseReference HospiLogout = FirebaseDatabase.getInstance().getReference().child("LoggedIn").child(deviceId);
                HospiLogout.removeValue();
                FirebaseAuth.getInstance().signOut();
                Intent intent6 = new Intent(HospitalActivity.this, MainActivity.class);
                startActivity(intent6);
                finish();
                break;

            case R.id.getDonors:
                requestDonor = true;
                Toast.makeText(this, "Requesting Donors", Toast.LENGTH_SHORT).show();
                getBloodGroup();
                break;
        }
        return true;
    }

    private void getBloodGroup(){
        mGetDonorDialog.setContentView(R.layout.select_donor);//Set the layout file to the dialog

        mBloodGroup = (Spinner) mGetDonorDialog.findViewById(R.id.bloodGroup);
        ArrayAdapter<String> bloodGroupAdapter = new ArrayAdapter<String>(HospitalActivity.this,
                android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.bloodG));
        bloodGroupAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);//Setting the spinner to dropdown
        mBloodGroup.setAdapter(bloodGroupAdapter);//This allows the spinner to show the data of the adapter

        mRHFactor = (Spinner) mGetDonorDialog.findViewById(R.id.rhFactor);
        ArrayAdapter<String> rhGroupAdapter = new ArrayAdapter<String>(HospitalActivity.this,
                android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.bloodRH));
        rhGroupAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mRHFactor.setAdapter(rhGroupAdapter);

        mBloodGroup.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                switch (i){
                    case 0:
                        bloodGroup = "A";
                        break;
                    case 1:
                        bloodGroup = "B";
                        break;
                    case 2:
                        bloodGroup = "AB";
                        break;
                    case 3:
                        bloodGroup = "O";
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        mRHFactor.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                switch (i){
                    case 0:
                        rhFactor = "positive";
                        break;
                    case 1:
                        rhFactor = "negative";
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        Button mFindDonor = (Button) mGetDonorDialog.findViewById(R.id.getDonorsDialog);//Button inside dialog
        mFindDonor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                makeDonorRequest();
                mGetDonorDialog.dismiss();//To close the dialog
            }
        });
        mGetDonorDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));//Background color for dialog
        mGetDonorDialog.show();//Show the dialog
    }

    private void makeDonorRequest(){
        DatabaseReference donorRequest = FirebaseDatabase.getInstance().getReference().child("donorRequest").child(bloodGroup).child(rhFactor);
        String requestKey = donorRequest.push().getKey();
        HashMap requestMap = new HashMap();
        requestMap.put("hospitalId", hospitalId);
        donorRequest.child(requestKey).updateChildren(requestMap);
    }

    private void showDialogBox(String userID){
        mGetUserInfoDialog.setContentView(R.layout.watch_customer_details);
        final TextView mName,mMediC, mMediN, mPhone, mEPhone, mBloodGroup;
        mName = mGetUserInfoDialog.findViewById(R.id.userNameH);
        mMediC = mGetUserInfoDialog.findViewById(R.id.userMedicalimCompany);
        mMediN = mGetUserInfoDialog.findViewById(R.id.userMedicalimNumber);
        mPhone = mGetUserInfoDialog.findViewById(R.id.userPhoneNumber);
        mEPhone = mGetUserInfoDialog.findViewById(R.id.emergencyPhoneNumber);
        mBloodGroup = mGetUserInfoDialog.findViewById(R.id.userBloodGroup);

        DatabaseReference userInfoRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(userID);
        userInfoRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    Map<String, Object> mMap = (Map<String, Object>) dataSnapshot.getValue();
                    if (mMap.get("name").toString() != null){
                        mName.setText(mMap.get("name").toString());
                    }
                    if (mMap.get("Medicompany").toString() != null){
                        mMediC.setText(mMap.get("Medicompany").toString());
                    }
                    if (mMap.get("Medino").toString() != null){
                        mMediN.setText(mMap.get("Medino").toString());
                    }
                    if (mMap.get("phone").toString() != null){
                        mPhone.setText(mMap.get("phone").toString());
                    }
                    if (mMap.get("ephone").toString() != null){
                        mEPhone.setText(mMap.get("ephone").toString());
                    }
//                    if (mMap.get("BloodGroup").toString() != null && mMap.get("RHFactor").toString() != null){
//                        mBloodGroup.setText(mMap.get("BloodGroup").toString() + " " + mMap.get("RHFactor").toString());
//                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        mGetUserInfoDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));//Background color for dialog
        mGetUserInfoDialog.show();//Show the dialog
    }

//    private void getDonor(){
//        DatabaseReference donorReference = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers");
//        donorReference.addChildEventListener(new ChildEventListener() {
//            @Override
//            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
//                if (requestDonor){
//                    String donorId = dataSnapshot.getKey();
////                    Toast.makeText(HospitalActivity.this, donorId, Toast.LENGTH_SHORT).show();
//                    getDonorInfo(donorId);
//                }
//            }
//
//            @Override
//            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
//
//            }
//
//            @Override
//            public void onChildRemoved(DataSnapshot dataSnapshot) {
//
//            }
//
//            @Override
//            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
//
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });
//    }
//
//    private void getDonorInfo(String donorId){
//        DatabaseReference donorInfo = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(donorId);
//        donorInfo.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                if (dataSnapshot.exists()){
//                }
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });
//    }
}
