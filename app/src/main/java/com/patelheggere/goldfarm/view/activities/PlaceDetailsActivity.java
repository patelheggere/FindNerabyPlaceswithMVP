package com.patelheggere.goldfarm.view.activities;

import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.patelheggere.goldfarm.R;
import com.patelheggere.goldfarm.adapters.PlaceListAdapter;
import com.patelheggere.goldfarm.commons.BaseActivity;
import com.patelheggere.goldfarm.helper.SimpleDividerItemDecoration;
import com.patelheggere.goldfarm.model.PlaceDistanceModel;
import com.patelheggere.goldfarm.presenter.Presenter;

import java.util.ArrayList;
import java.util.List;

import static com.patelheggere.goldfarm.helper.AppUtils.Constants.ADDS;
import static com.patelheggere.goldfarm.helper.AppUtils.Constants.EIGHT_SECOND;
import static com.patelheggere.goldfarm.helper.AppUtils.Constants.HOSPITAL;
import static com.patelheggere.goldfarm.helper.AppUtils.Constants.HOTEL;
import static com.patelheggere.goldfarm.helper.AppUtils.Constants.LAT;
import static com.patelheggere.goldfarm.helper.AppUtils.Constants.LON;
import static com.patelheggere.goldfarm.helper.AppUtils.Constants.PLACES;
import static com.patelheggere.goldfarm.helper.AppUtils.Constants.TWO_SECOND;

public class PlaceDetailsActivity extends BaseActivity implements Presenter.View, View.OnClickListener {
    private ActionBar mActionBar;
    private List<PlaceDistanceModel> places, listRest, listHotels, listHospital;
    private String address;
    private Presenter mPresenter;
    private RecyclerView recyclerViewRestuarants, recyclerViewHotels, recyclerViewHospital;
    private PlaceListAdapter placeListAdapterRest, placeListAdapterHospital, placeListAdapterHotels;
    private double lat, lon;
    private Button mButtonRest, mButtonHotels, mButtonHospital;
    private boolean isSecond = true;
    private TextView textViewNoRest, textViewNoHotels, textViewNoHospitals;

    @Override
    protected int getContentView() {
        return R.layout.activity_place_details;
    }

    @Override
    protected void initView() {
        mActionBar = getSupportActionBar();
        if(mActionBar!=null)
        {
            mActionBar.setDisplayHomeAsUpEnabled(true);
        }
        listRest = getIntent().getParcelableArrayListExtra(PLACES);
        address = getIntent().getStringExtra(ADDS);
        lat = getIntent().getDoubleExtra(LAT,0.0);
        lon = getIntent().getDoubleExtra(LON,0.0);

        //listRest = new ArrayList<>();

        listHospital = new ArrayList<>();
        listHotels = new ArrayList<>();

        mButtonHospital = findViewById(R.id.btn_hospitals);
        mButtonHotels = findViewById(R.id.btn_hotels);
        mButtonRest = findViewById(R.id.btn_rest);

        textViewNoHospitals = findViewById(R.id.no_hospitals);
        textViewNoHotels = findViewById(R.id.no_hotels);
        textViewNoRest = findViewById(R.id.no_rest);

        }

    @Override
    protected void initData() {
        mPresenter = new Presenter(this);
        Log.d(TAG, "initData: "+lon+" "+lat);
        if(lat!=0.0)
        {
            mPresenter.getDetails(HOTEL, lat, lon);
            mPresenter.getDetails(HOSPITAL, lat, lon);
        }


        mButtonHospital.setOnClickListener(this);
        mButtonHotels.setOnClickListener(this);
        mButtonRest.setOnClickListener(this);

        recyclerViewRestuarants = findViewById(R.id.rv_places);
        recyclerViewRestuarants.setLayoutManager(new LinearLayoutManager(context));
        placeListAdapterRest = new PlaceListAdapter(context,listRest);
        recyclerViewRestuarants.setAdapter(placeListAdapterRest);
        recyclerViewRestuarants.addItemDecoration(new SimpleDividerItemDecoration(context));

        recyclerViewHospital = findViewById(R.id.rv_hospitals);
        recyclerViewHospital.setLayoutManager(new LinearLayoutManager(context));
        placeListAdapterHospital = new PlaceListAdapter(context,listHospital);
        recyclerViewHospital.setAdapter(placeListAdapterHospital);
        recyclerViewHospital.addItemDecoration(new SimpleDividerItemDecoration(context));

        recyclerViewHotels = findViewById(R.id.rv_hotels);
        recyclerViewHotels.setLayoutManager(new LinearLayoutManager(context));
        placeListAdapterHotels = new PlaceListAdapter(context,listHotels);
        recyclerViewHotels.setAdapter(placeListAdapterHotels);
        recyclerViewHotels.addItemDecoration(new SimpleDividerItemDecoration(context));



        TextView textViewTitle = findViewById(R.id.tv_title);
        textViewTitle.setText( getString(R.string.near_by)+"\n"+address);

    }

    @Override
    protected void initListener() {
        mButtonRest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(listRest.size()>0)
                {
                    recyclerViewRestuarants.setVisibility(View.VISIBLE);
                    recyclerViewHospital.setVisibility(View.INVISIBLE);
                    recyclerViewHotels.setVisibility(View.INVISIBLE);

                    textViewNoRest.setVisibility(View.GONE);
                    textViewNoHospitals.setVisibility(View.GONE);
                    textViewNoHotels.setVisibility(View.GONE);
                }
                else {
                    textViewNoRest.setVisibility(View.VISIBLE);
                    textViewNoHospitals.setVisibility(View.GONE);
                    textViewNoHotels.setVisibility(View.GONE);

                    recyclerViewRestuarants.setVisibility(View.INVISIBLE);
                    recyclerViewHospital.setVisibility(View.INVISIBLE);
                    recyclerViewHotels.setVisibility(View.INVISIBLE);
                }
            }
        });

        mButtonHotels.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(listHotels.size()>0)
                {
                    recyclerViewRestuarants.setVisibility(View.INVISIBLE);
                    recyclerViewHospital.setVisibility(View.INVISIBLE);
                    recyclerViewHotels.setVisibility(View.VISIBLE);

                    textViewNoRest.setVisibility(View.GONE);
                    textViewNoHospitals.setVisibility(View.GONE);
                    textViewNoHotels.setVisibility(View.GONE);
                }
                else {
                    textViewNoRest.setVisibility(View.GONE);
                    textViewNoHospitals.setVisibility(View.GONE);
                    textViewNoHotels.setVisibility(View.VISIBLE);

                    recyclerViewHotels.setVisibility(View.INVISIBLE);
                    recyclerViewHospital.setVisibility(View.INVISIBLE);
                    recyclerViewRestuarants.setVisibility(View.INVISIBLE);
                }
            }
        });

        mButtonHospital.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(listHospital.size()>0)
                {
                    recyclerViewRestuarants.setVisibility(View.INVISIBLE);
                    recyclerViewHospital.setVisibility(View.VISIBLE);
                    recyclerViewHotels.setVisibility(View.INVISIBLE);

                    textViewNoRest.setVisibility(View.GONE);
                    textViewNoHospitals.setVisibility(View.GONE);
                    textViewNoHotels.setVisibility(View.GONE);
                }
                else {
                    textViewNoRest.setVisibility(View.GONE);
                    textViewNoHospitals.setVisibility(View.VISIBLE);
                    textViewNoHotels.setVisibility(View.GONE);

                    recyclerViewHospital.setVisibility(View.INVISIBLE);
                    recyclerViewRestuarants.setVisibility(View.INVISIBLE);
                    recyclerViewHotels.setVisibility(View.INVISIBLE);
                }
            }
        });

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void updateList(List<PlaceDistanceModel> places) {
        Log.d(TAG, "updateList: "+places.get(0).getType());
        if(places.get(0).getType().equalsIgnoreCase(HOTEL))
        {
            listHotels = places;
            placeListAdapterHotels = new PlaceListAdapter(context, listHotels);
            recyclerViewHotels.setAdapter(placeListAdapterHotels);
            placeListAdapterHotels.notifyDataSetChanged();
        }
        if(places.get(0).getType().equalsIgnoreCase(HOSPITAL))
        {
            listHospital = places;
            placeListAdapterHospital = new PlaceListAdapter(context, listHospital);
            recyclerViewHospital.setAdapter(placeListAdapterHospital);
            placeListAdapterHospital.notifyDataSetChanged();
        }
    }

    @Override
    public void showProgressBar() {

    }

    @Override
    public void hideProgressBar() {

    }

    @Override
    public void noResults(String type) {
        if(type.equalsIgnoreCase(HOTEL))
        {
            textViewNoHotels.setText(getString(R.string.no_hotels_found));
        }
        else if(type.equalsIgnoreCase(HOSPITAL))
        {
            textViewNoHospitals.setText(getString(R.string.no_hospitals_found));
        }
    }

    @Override
    public void queryLimit(String type) {
        if(type.equalsIgnoreCase(HOTEL))
        {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mPresenter.getDetails(HOTEL, lat, lon);
                }
            },EIGHT_SECOND);

        }
        if(type.equalsIgnoreCase(HOSPITAL))
        {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mPresenter.getDetails(HOSPITAL, lat, lon);
                }
            },EIGHT_SECOND);

        }

    }

    @Override
    public void onClick(View v) {

    }
}
