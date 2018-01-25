package com.example.acer.goldenhour;

import android.*;
import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.security.Permission;

public class SosActivity extends Activity {

    private static final int SEND_SMS_PERMISSION_REQUEST_CODE = 111;
    private Button sendMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sos);
        sendMessage = findViewById(R.id.send_message);
        final  EditText phone = findViewById(R.id.sos_phono_no);
        final EditText message = findViewById(R.id.sos_message);
        sendMessage.setEnabled(false);

        if (checkPermission(android.Manifest.permission.SEND_SMS)) {
            sendMessage.setEnabled(true);
        } else {
            ActivityCompat.requestPermissions(this, new String[] {android.Manifest.permission.SEND_SMS}, SEND_SMS_PERMISSION_REQUEST_CODE);
        }

        sendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = message.getText().toString();
                String phoneNumber = phone.getText().toString();

                if(!TextUtils.isEmpty(msg) && !TextUtils.isEmpty(phoneNumber)) {

                    if (checkPermission(Manifest.permission.SEND_SMS)) {
                        SmsManager smsManager = SmsManager.getDefault();
                        smsManager.sendTextMessage(phoneNumber, null, msg, null, null);
                    } else {
                        Toast.makeText(SosActivity.this, "Permission denied", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(SosActivity.this, "Enter Message and Phone Number", Toast.LENGTH_SHORT).show();
                }
            }
        });
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
                    sendMessage.setEnabled(true);
                }
                break;
        }
    }
}
