package com.patelheggere.goldfarm.view.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Handler;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.patelheggere.goldfarm.R;
import com.patelheggere.goldfarm.commons.BaseActivity;
import com.patelheggere.goldfarm.helper.AppUtils;
import com.patelheggere.goldfarm.model.PlaceDistanceModel;
import com.patelheggere.goldfarm.presenter.Presenter;
import com.patelheggere.goldfarm.view.activities.PlaceDetailsActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.patelheggere.goldfarm.helper.AppUtils.Constants.THREE_SECOND;

public class SecondMainActivity extends BaseActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        OnMapReadyCallback,
        Presenter.View,
        View.OnClickListener,
        GoogleMap.OnCameraIdleListener,
        GoogleMap.OnMapLoadedCallback,
        GoogleMap.OnCameraMoveListener,
        GoogleMap.OnCameraMoveStartedListener, GoogleMap.OnCameraChangeListener, GoogleMap.OnCameraMoveCanceledListener {

    private GoogleMap mMap;
    private double latitude;
    private double longitude;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private Marker mCurrLocationMarker;
    private LocationRequest mLocationRequest;
    private ConstraintLayout rootLayout;
    private SupportMapFragment mapFragment;
    private PopupWindow mPopupWindow;
    private static final int LOC_REQ_CODE = 1;
    private static final int PLACE_PICKER_REQ_CODE = 2;
    private static final int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;


    private ProgressBar mProgressBar;
    private Button mButtonFind, mButtonNearBy;
    private Presenter mPresenter;
    private int selected;
    private Animation animationBounce;
    private PlaceAutocompleteFragment autocompleteFragment;
    private String address;
    private LatLng latLng;
    private TextInputEditText mCurrentLocation;

    private LinearLayout linearLayoutOverlay;
    private View mViewOverlay;
    private ActionBar mActionBar;

    private TextView textViewGettingAdds;

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

        mViewOverlay = findViewById(R.id.overlay);
        mViewOverlay.bringToFront();

        mCurrentLocation = findViewById(R.id.et_place_name);
        mButtonNearBy = findViewById(R.id.btn_near);
        mButtonNearBy.setOnClickListener(this);

        rootLayout = findViewById(R.id.root_view);

        animationBounce = AnimationUtils.loadAnimation(context, R.anim.btn_bounce);

        textViewGettingAdds = findViewById(R.id.tv_getting_location);

        showProgressBar();
    }

    @Override
    protected void initData() {

        mPresenter = new Presenter(this);
        if (isLocationAccessPermitted()) {
            //CheckGooglePlayServices();
        } else {
            requestLocationAccessPermission();
        }
        //show error dialog if Google Play Services not available
        if (!CheckGooglePlayServices()) {
            Log.d("onCreate", "Google Play Services not available. Ending Test case.");
            finish();
        } else {
            Log.d("onCreate", "Google Play Services available. Continuing.");
        }

        mActionBar = getSupportActionBar();
        mActionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.hide();
        mButtonNearBy.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void initListener() {

        //to call auto place activity
        mCurrentLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent intent =
                            new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                                    .build(activity);
                    startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
                } catch (GooglePlayServicesRepairableException e) {
                    // TODO: Handle the error.
                } catch (GooglePlayServicesNotAvailableException e) {
                    // TODO: Handle the error.
                }

            }
        });
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "onConnected: ");
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(AppUtils.Constants.TEN_SECOND);
        mLocationRequest.setFastestInterval(AppUtils.Constants.TEN_SECOND);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                hideProgressBar();
                mActionBar.show();
                mButtonNearBy.setVisibility(View.VISIBLE);
                mViewOverlay.setVisibility(View.GONE);
            }
        }, THREE_SECOND);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        hideProgressBar();
        Toast.makeText(context, connectionResult.getErrorMessage(), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        Log.d(TAG, "onLocationChanged: "+latitude+" "+longitude);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady: ");
        textViewGettingAdds.setVisibility(View.VISIBLE);
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.setOnCameraIdleListener(this);
        mMap.setOnCameraMoveStartedListener(this);
        mMap.setOnCameraMoveListener(this);
        mMap.setOnCameraMoveCanceledListener(this);

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
        View locationButton = ((View) findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
        RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
        // position on right bottom
        rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
        rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
        rlp.setMargins(0, 140, 30, 0);
        //Get Last Known Location and convert into Current Longitute and Latitude
        final LocationManager mlocManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        final Location currentGeoLocation = mlocManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        mLastLocation = currentGeoLocation;
        if (mCurrLocationMarker != null) {
            mCurrLocationMarker.remove();
        }

        //Place current location marker
        if (currentGeoLocation != null) {
            latitude = currentGeoLocation.getLatitude();
            longitude = currentGeoLocation.getLongitude();
            latLng = new LatLng(currentGeoLocation.getLatitude(), currentGeoLocation.getLongitude());
            address = getAddress(latitude, longitude);
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(18));
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
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                com.google.android.gms.location.places.Place place = PlaceAutocomplete.getPlace(this, data);
                longitude = place.getLatLng().longitude;
                latitude = place.getLatLng().latitude;
                getAddress(latitude, longitude);
                mMap.moveCamera(CameraUpdateFactory.newLatLng(place.getLatLng()));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(16));
                Log.d(TAG, "onActivityResult: "+latitude+" "+longitude);
                if (ContextCompat.checkSelfPermission(context,
                        Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
                }
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                // TODO: Handle the error.
                Log.i(TAG, status.getStatusMessage());

            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
        if (requestCode == LOC_REQ_CODE) {
            if (resultCode == RESULT_OK) {
                //showPlacePicker();
            }
        } else if (requestCode == PLACE_PICKER_REQ_CODE) {
            if (resultCode == RESULT_OK) {
                //place = PlacePicker.getPlace(this, data);
                //name.setText(place.getName());
            }
        }
    }

    private String getAddress(double latitude, double longitude) {
        try {
            Geocoder geo = new Geocoder(this.getApplicationContext(), Locale.getDefault());
            List<Address> addresses = geo.getFromLocation(latitude, longitude, 1);
            if (addresses.isEmpty()) {
            } else {
                if (addresses.size() > 0) {
                    address = "";
                    if (addresses.get(0).getFeatureName() != null)
                        address += addresses.get(0).getFeatureName();
                    if (addresses.get(0).getLocality() != null) {
                        address += " " + addresses.get(0).getLocality();
                    }
                    if (addresses.get(0).getSubAdminArea() != null) {
                        address += " " + addresses.get(0).getSubAdminArea();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace(); // getFromLocation() may sometimes fail
        }
        mCurrentLocation.setText(address);
        return address;
    }

    private boolean CheckGooglePlayServices() {
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int result = googleAPI.isGooglePlayServicesAvailable(this);
        if (result != ConnectionResult.SUCCESS) {
            if (googleAPI.isUserResolvableError(result)) {
                googleAPI.getErrorDialog(this, result,
                        0).show();
            }
            return false;
        }
        return true;
    }

    @Override
    public void updateList(List<PlaceDistanceModel> places) {

        Intent intent = new Intent(context, PlaceDetailsActivity.class);
        intent.putParcelableArrayListExtra(AppUtils.Constants.PLACES, (ArrayList<? extends Parcelable>) places);
        intent.putExtra(AppUtils.Constants.ADDS, address);
        intent.putExtra(AppUtils.Constants.LAT, latitude);
        intent.putExtra(AppUtils.Constants.LON, longitude);
        startActivity(intent);
        // mPopupWindow.showAtLocation(rootLayout, Gravity.CENTER, 0, 0);
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
    public void noResults(String type) {
        Toast.makeText(context, getString(R.string.no_rest_found), Toast.LENGTH_LONG).show();
    }

    @Override
    public void queryLimit(String type) {
        Toast.makeText(context, "Query Limit for this API Key finished for the day or try after some time", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_near) {
            showProgressBar();
            mPresenter.getDetails(AppUtils.Constants.RESTAURANT, latitude, longitude);
        }
    }

    @Override
    public void onCameraIdle() {
       // showUI();
        LatLng midLatLng = mMap.getCameraPosition().target;
        latitude = mMap.getCameraPosition().target.latitude;
        longitude = mMap.getCameraPosition().target.longitude;
        address = getAddress(midLatLng.latitude, midLatLng.longitude);
    }

    @Override
    public void onMapLoaded() {
        Log.d(TAG, "onMapLoaded: ");
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mActionBar.show();
                hideProgressBar();
                mButtonNearBy.setVisibility(View.VISIBLE);
                mViewOverlay.setVisibility(View.GONE);
            }
        }, THREE_SECOND);
    }

    @Override
    public void onCameraMove() {
        Log.d(TAG, "onCameraMove: ");
        mCurrentLocation.setText(getString(R.string.getting_adds));
    }

    @Override
    public void onCameraMoveStarted(int i) {
        Log.d(TAG, "onCameraMoveStarted: ");
       // hideUI();
    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
        Log.d(TAG, "onCameraChange: ");
    }

    @Override
    public void onCameraMoveCanceled() {
        Log.d(TAG, ":camera move cancel ");
    }

    private void hideUI() {
        mActionBar.hide();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(false);
    }

    private void showUI() {
        mActionBar.show();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);
    }
}
