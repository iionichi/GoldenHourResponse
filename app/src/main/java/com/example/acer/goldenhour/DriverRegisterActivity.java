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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.Map;


public class DriverRegisterActivity extends AppCompatActivity {
    private EditText mEmail,mPassword,mNameField, mPhoneField, mAmbulanceField, mAmbulanceNumberField;
    private Button mRegistration;

    private FirebaseAuth mAuth;

    private FirebaseAuth.AuthStateListener firebaseAuthListener;
    private String mName;
    private String mPhone;
    private String mAmbulance;
    private String mAmbulanceNumber;
    private String mService;
    private RadioGroup mRadioGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_register);

        mAuth = FirebaseAuth.getInstance();

        firebaseAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if(user != null){
                    Intent intent = new Intent(DriverRegisterActivity.this,DriverMapActivity.class);
                    startActivity(intent);
                    finish();
                    return;
                }
            }
        };

        mEmail = (EditText) findViewById(R.id.email);
        mPassword = (EditText) findViewById(R.id.password);
        mNameField = (EditText) findViewById(R.id.name);
        mPhoneField = (EditText) findViewById(R.id.phone);
        mAmbulanceField = (EditText) findViewById(R.id.ambulance);
        mAmbulanceNumberField = (EditText) findViewById(R.id.ambulance_number);

        mRadioGroup = (RadioGroup) findViewById(R.id.radioGroup);



        mRegistration = (Button) findViewById(R.id.registration);


        mRegistration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email = mEmail.getText().toString();
                final String password = mPassword.getText().toString();
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


                if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(mName) || TextUtils.isEmpty(mPhone) || TextUtils.isEmpty(mAmbulance) || TextUtils.isEmpty(mAmbulanceNumber)) {
                    Toast.makeText(DriverRegisterActivity.this, "Please Fill All The Fields", Toast.LENGTH_SHORT).show();
                } else {
                    mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(DriverRegisterActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (!task.isSuccessful()) {
                                Toast.makeText(DriverRegisterActivity.this, "Sign Up Error", Toast.LENGTH_SHORT).show();
                            } else {
                                String userId = mAuth.getCurrentUser().getUid();
//                                DatabaseReference currentUserDB = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(userId).child("name");
//                                currentUserDB.setValue(email);

                                addAmbulanceLogin(userId);//Adding Ambulance Login


                               DatabaseReference  mDriverDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(userId);
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
                    });
                }

            }
        });

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

