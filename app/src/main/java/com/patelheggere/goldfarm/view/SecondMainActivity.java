package com.patelheggere.goldfarm.view;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.patelheggere.goldfarm.R;
import com.patelheggere.goldfarm.adapters.PlaceListAdapter;
import com.patelheggere.goldfarm.commons.BaseActivity;
import com.patelheggere.goldfarm.helper.SimpleDividerItemDecoration;
import com.patelheggere.goldfarm.model.PlaceDistanceModel;
import com.patelheggere.goldfarm.presenter.Presenter;
import com.patelheggere.goldfarm.view.hospital.HospitalFragment;
import com.patelheggere.goldfarm.view.hotel.HotelFragment;
import com.patelheggere.goldfarm.view.restaurant.RestFragment;

import java.util.List;

public class SecondMainActivity extends BaseActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener, OnMapReadyCallback, Presenter.View, View.OnClickListener {

    private GoogleMap mMap;
    private double latitude;
    private double longitude;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private Marker mCurrLocationMarker;
    private LocationRequest mLocationRequest;
    private View mView;
    private ConstraintLayout rootLayout;
    private SupportMapFragment mapFragment;
    private PopupWindow mPopupWindow;
    private static final int LOC_REQ_CODE = 1;
    private static final int PLACE_PICKER_REQ_CODE = 2;


    private ProgressBar mProgressBar;
    private Button mButtonFind;
    private Presenter mPresenter;
    private int selected;
    private Animation animationBounce;

    @Override
    protected int getContentView() {
        return R.layout.activity_second_main;
    }

    @Override
    protected void initView() {
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mProgressBar = findViewById(R.id.progress_bar);
        mButtonFind = findViewById(R.id.btn_rest);
        mButtonFind.setOnClickListener(this);

        rootLayout = findViewById(R.id.root_view);

        animationBounce = AnimationUtils.loadAnimation(context, R.anim.btn_bounce);

        BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
                = new BottomNavigationView.OnNavigationItemSelectedListener() {

            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item)
            {
                switch (item.getItemId()) {
                    case R.id.navigation_rest:
                        selected = 1;
                        mButtonFind.setText(getString(R.string.find_rest));
                        mButtonFind.startAnimation(animationBounce);
                        return true;
                    case R.id.navigation_hotel:
                        selected = 2;
                        mButtonFind.setText(getString(R.string.find_hotel));
                        mButtonFind.startAnimation(animationBounce);
                        return true;
                    case R.id.navigation_hospital:
                        selected = 3;
                        mButtonFind.setText(getString(R.string.find_hospital));
                        mButtonFind.startAnimation(animationBounce);
                        return true;
                }
                return false;
            }
        };

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        navigation.setSelectedItemId(R.id.navigation_rest);
        showProgressBar();
    }

    @Override
    protected void initData() {

        mPresenter = new Presenter(this);
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

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "onConnected: ");
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(10000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
        hideProgressBar();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        if (mCurrLocationMarker != null) {
            mCurrLocationMarker.remove();
        }
        //Place current location marker
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("Current Position");

        // Adding colour to the marker
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));

        // Adding Marker to the Map
        mCurrLocationMarker = mMap.addMarker(markerOptions);

        //move map camera
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(18));
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady: ");
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        //Initialize Google Play Services
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(context,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient();
                mMap.setMyLocationEnabled(true);
            }
        } else {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);

        }
    }
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
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
    public void updateList(List<PlaceDistanceModel> places) {
        final LayoutInflater inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
        View customView = inflater.inflate(R.layout.list_layout, null, false);
        ConstraintLayout constraintLayout = customView.findViewById(R.id.cl_places);
        RecyclerView recyclerViewPlaces = customView.findViewById(R.id.rv_places);
        recyclerViewPlaces.setLayoutManager(new LinearLayoutManager(context));
        PlaceListAdapter placeListAdapter = new PlaceListAdapter(context,places );
        recyclerViewPlaces.setAdapter(placeListAdapter);
        recyclerViewPlaces.addItemDecoration(new SimpleDividerItemDecoration(context));
        TextView textViewTitle = customView.findViewById(R.id.tv_title);
        switch (selected)
        {
            case 1:
                textViewTitle.setText(R.string.rest_near);
                break;
            case 2:
                textViewTitle.setText(R.string.hotel_near);
                break;
            case 3:
                textViewTitle.setText(R.string.hospital_near);
                break;
        }

        mPopupWindow = new PopupWindow(
                customView,
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT, true
        );

        mPopupWindow.setOutsideTouchable(true);
        mPopupWindow.setFocusable(true);
        // Removes default background.
        mPopupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        if (Build.VERSION.SDK_INT >= 21) {
            mPopupWindow.setElevation(5.0f);
        }

        Animation animation = AnimationUtils.loadAnimation(context, R.anim.zoom_in_one_sec);
        constraintLayout.startAnimation(animation);

        ImageView closeButton =  customView.findViewById(R.id.iv_close);
        // Set a click listener for the popup window close button
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Dismiss the popup window
                mPopupWindow.dismiss();
            }
        });
        mPopupWindow.showAtLocation(rootLayout, Gravity.CENTER, 0, 0);
    }

    @Override
    public void showProgressBar() {
        mProgressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideProgressBar() {
        mProgressBar.setVisibility(View.GONE);
    }

    @Override
    public void noResults() {
        Toast.makeText(context, "No Restaurants found ", Toast.LENGTH_LONG).show();
    }

    @Override
    public void queryLimit() {
        Toast.makeText(context, "Query Limit for this API Key finished for the day or try after some time", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.btn_rest)
        {
            switch (selected)
            {
                case 1:
                    showProgressBar();
                    mPresenter.getDetails("restaurant", latitude, longitude);
                    break;
                case 2:
                    showProgressBar();
                    mPresenter.getDetails("lodging", latitude, longitude);
                    break;
                case 3:
                    showProgressBar();
                    mPresenter.getDetails("hospital", latitude, longitude);
                    break;

            }
        }
    }
}
