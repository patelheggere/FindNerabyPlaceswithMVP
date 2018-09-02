package com.patelheggere.goldfarm.view.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.patelheggere.goldfarm.R;
import com.patelheggere.goldfarm.commons.BaseActivity;
import com.patelheggere.goldfarm.view.hospital.HospitalFragment;
import com.patelheggere.goldfarm.view.hotel.HotelFragment;
import com.patelheggere.goldfarm.view.restaurant.RestFragment;

public class MainActivity extends BaseActivity implements
        RestFragment.OnFragmentInteractionListener,
        HotelFragment.OnFragmentInteractionListener,
        HospitalFragment.OnFragmentInteractionListener{

    private static final String TAG = "MainActivity";

    private static final int LOC_REQ_CODE = 1;
    private static final int PLACE_PICKER_REQ_CODE = 2;


    @Override
    protected int getContentView() {
        return R.layout.activity_main;
    }

    @Override
    protected void initView() {

        BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
                = new BottomNavigationView.OnNavigationItemSelectedListener() {

            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item)
            {
                FragmentManager fragmentManager = getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_in_right);
                Fragment curFrag = fragmentManager.getPrimaryNavigationFragment();
                switch (item.getItemId()) {
                    case R.id.navigation_rest:
                        Log.d(TAG, "onNavigationItemSelected: ");
                        if (curFrag != null) {
                            fragmentTransaction.detach(curFrag);
                        }
                        Fragment fragment = fragmentManager.findFragmentByTag("REST");
                        if (fragment == null) {
                            fragment = new RestFragment();
                            fragmentTransaction.add(R.id.contentFrame, fragment, "REST");
                        } else {
                            fragmentTransaction.attach(fragment);
                        }
                        fragmentTransaction.setPrimaryNavigationFragment(fragment);
                        fragmentTransaction.setReorderingAllowed(true);
                        fragmentTransaction.commitNowAllowingStateLoss();

                        return true;
                        /*
                    case R.id.navigation_hotel:
                        if (curFrag != null) {
                            fragmentTransaction.detach(curFrag);
                        }
                        fragment = fragmentManager.findFragmentByTag("HOTEL");
                        if (fragment == null) {
                            fragment = new HotelFragment();
                            fragmentTransaction.add(R.id.contentFrame, fragment, "HOTEL");
                        } else {
                            fragmentTransaction.attach(fragment);
                        }
                        fragmentTransaction.setPrimaryNavigationFragment(fragment);
                        fragmentTransaction.setReorderingAllowed(true);
                        fragmentTransaction.commitNowAllowingStateLoss();
                        // mTextMessage.setText(R.string.title_dashboard);
                        return true;
                    case R.id.navigation_hospital:
                        if (curFrag != null) {
                            fragmentTransaction.detach(curFrag);
                        }
                        fragment = fragmentManager.findFragmentByTag("HOSPITAL");
                        if (fragment == null) {
                            fragment = new HospitalFragment();
                            fragmentTransaction.add(R.id.contentFrame, fragment, "HOSPITAL");
                        } else {
                            fragmentTransaction.attach(fragment);
                        }
                        fragmentTransaction.setPrimaryNavigationFragment(fragment);
                        fragmentTransaction.setReorderingAllowed(true);
                        fragmentTransaction.commitNowAllowingStateLoss();
                        //mTextMessage.setText(R.string.title_notifications);
                        return true;
                        */
                }
                return false;
            }
        };

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        navigation.setSelectedItemId(R.id.navigation_rest);

    }
    @Override
    protected void initData() {
        if(isLocationAccessPermitted())
        {
            //CheckGooglePlayServices();
        }
        else {
            requestLocationAccessPermission();
        }
        //show error dialog if Google Play Services not available
        if (!CheckGooglePlayServices()) {
            Log.d("onCreate", "Google Play Services not available. Ending Test case.");
            finish();
        }
        else {
            Log.d("onCreate", "Google Play Services available. Continuing.");
        }

        ActionBar ab = getSupportActionBar();
        ab.setHomeAsUpIndicator(R.drawable.ic_menu);
        ab.setDisplayHomeAsUpEnabled(true);



    }




    @Override
    protected void initListener() {


    }

    private boolean isLocationAccessPermitted() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return false;
        } else {
            return true;
        }
    }

    private void requestLocationAccessPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                LOC_REQ_CODE);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == LOC_REQ_CODE) {
            if (resultCode == RESULT_OK) {
                //showPlacePicker();
            }
        }else if(requestCode == PLACE_PICKER_REQ_CODE){
            if (resultCode == RESULT_OK) {
                //place = PlacePicker.getPlace(this, data);
                //name.setText(place.getName());
            }
        }
    }

    private boolean CheckGooglePlayServices() {
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int result = googleAPI.isGooglePlayServicesAvailable(this);
        if(result != ConnectionResult.SUCCESS) {
            if(googleAPI.isUserResolvableError(result)) {
                googleAPI.getErrorDialog(this, result,
                        0).show();
            }
            return false;
        }
        return true;
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    public interface sendToFrag{
        void getData(String types);
    }
}
