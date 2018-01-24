package com.example.acer.goldenhour;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class HospitalActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hospital);

        LinearLayout linearLayout= (LinearLayout)findViewById(R.id.addTextView);      //find the linear layout
        linearLayout.removeAllViews();                              //add this too

        DatabaseReference customersId = FirebaseDatabase.getInstance().getReference().child("Hospital").child("customerRequestId");


        for(int i=0; i<5;i++){          //looping to create 5 textviews

            TextView textView= new TextView(this);              //dynamically create textview
            textView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 48)); //select linearlayoutparam- set the width & height
            textView.setGravity(Gravity.CENTER_VERTICAL);                       //set the gravity too
            textView.setText("Textview: "+i);                                    //adding text
            linearLayout.addView(textView);                                     //inflating :)
        }
    }
}
