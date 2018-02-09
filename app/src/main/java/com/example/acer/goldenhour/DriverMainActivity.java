package com.example.acer.goldenhour;

import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;

public class DriverMainActivity extends AppCompatActivity {
    private DrawerLayout mDrawerLayoutDriver;
    private ActionBarDrawerToggle mToggleDriver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_main);
        mDrawerLayoutDriver = (DrawerLayout) findViewById(R.id.Driver_Main_Activity);
        mToggleDriver = new ActionBarDrawerToggle(this,mDrawerLayoutDriver,R.string.open,R.string.close);
        mDrawerLayoutDriver.addDrawerListener(mToggleDriver);
        mToggleDriver.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item_driver) {
        if (mToggleDriver.onOptionsItemSelected(item_driver)) {
            return true;
        }
        return super.onOptionsItemSelected(item_driver);
    }
}
