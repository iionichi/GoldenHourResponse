package com.example.acer.goldenhour;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class AuthActivity extends AppCompatActivity {

    private EditText mPhoneText,mCodeText;

    private Button mSendBtn;

    private TextView mErrorText;
    private ImageView mAuthIcon;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    private FirebaseAuth mAuth;

    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        mPhoneText = findViewById(R.id.phoneEditText);
        mCodeText = findViewById(R.id.codeEditText);

        mSendBtn = findViewById(R.id.sendBtn);

        mAuthIcon = findViewById(R.id.authIcon);

        mAuth = FirebaseAuth.getInstance();

        mErrorText = findViewById(R.id.errorText);

        mSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mAuthIcon.setVisibility(View.VISIBLE);
                mCodeText.setVisibility(View.VISIBLE);
                mPhoneText.setEnabled(false);
                mSendBtn.setEnabled(false);

                String phoneNumber = mPhoneText.getText().toString();

                PhoneAuthProvider.getInstance().verifyPhoneNumber(
                        phoneNumber,
                        60,
                        TimeUnit.SECONDS,
                        AuthActivity.this,
                        mCallbacks
                );
            }
        });

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {

                signInWithPhoneAuthCredential(phoneAuthCredential);

            }

            @Override
            public void onVerificationFailed(FirebaseException e) {

                mErrorText.setText("An error occured while logging in");
                mErrorText.setVisibility(View.VISIBLE);

            }

            @Override
            public void onCodeSent(String verificationId,
                                   PhoneAuthProvider.ForceResendingToken token) {


                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId;
                mResendToken = token;

                mSendBtn.setText("Verify Code");

                // ...
            }

        };
    }

        private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
            mAuth.signInWithCredential(credential)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information

                                FirebaseUser user = task.getResult().getUser();

                                Intent intent = new Intent(AuthActivity.this, CustomerMapActivity.class);
                                startActivity(intent);
                                finish();
                                // ...
                            } else {
                                // Sign in failed, display a message and update the UI

                                mErrorText.setText("An error occured while logging in");
                                mErrorText.setVisibility(View.VISIBLE);

                                if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                    // The verification code entered was invalid
                                }
                            }
                        }
                    });
        }

    }

