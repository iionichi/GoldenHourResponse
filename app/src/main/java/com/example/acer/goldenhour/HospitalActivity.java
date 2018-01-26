package com.example.acer.goldenhour;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Map;

public class HospitalActivity extends AppCompatActivity {

    private String hospitalId, userId;

    private TextView mtext;

    private ListView mUserList;
    private ArrayList<String> mCustomerNames = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hospital);

        //mtext = findViewById(R.id.hospitalText);
        mUserList = findViewById(R.id.customerNameList);

        hospitalId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, mCustomerNames);
        mUserList.setAdapter(arrayAdapter);

        DatabaseReference customersId = FirebaseDatabase.getInstance().getReference().child("Hospital").child(hospitalId).child("customerRequestId");
        customersId.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                userId = dataSnapshot.getValue(String.class);

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


//        LinearLayout linearLayout= findViewById(R.id.addTextView);      //find the linear layout
//        linearLayout.removeAllViews();                              //add this too
//
//        for(int i=0; i<5;i++){          //looping to create 5 textviews
//
//            TextView textView= new TextView(this);              //dynamically create textview
//            textView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 48)); //select linearlayoutparam- set the width & height
//            textView.setGravity(Gravity.CENTER_VERTICAL);                       //set the gravity too
//            textView.setText("Textview: "+userId);                                    //adding text
//            linearLayout.addView(textView);                                     //inflating :)
//        }
    }
}
