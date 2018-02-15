package com.example.acer.goldenhour;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.security.Provider;

public class onAppKilled extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        try {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("driversAvailable");
            GeoFire geoFire = new GeoFire(ref);
            geoFire.removeLocation(userId);

//            String userid = FirebaseAuth.getInstance().getCurrentUser().getUid();
//            DatabaseReference strangerRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(userid).child("type");
//            strangerRef.removeValue();
//            FirebaseUser aUser = FirebaseAuth.getInstance().getCurrentUser();
//            aUser.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
//                @Override
//                public void onComplete(@NonNull Task<Void> task) {
//                    FirebaseAuth.getInstance().signOut();
//                    Toast.makeText(onAppKilled.this, "User Deleted", Toast.LENGTH_SHORT).show();
//                }
//            });
        }
        catch (Exception e){

        }
//        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("driversAvailable");
//        GeoFire geoFire = new GeoFire(ref);
//        geoFire.removeLocation(userId);
    }
}
