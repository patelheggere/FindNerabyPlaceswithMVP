package com.patelheggere.goldfarm.presenter;


import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.SphericalUtil;
import com.patelheggere.goldfarm.model.Place;
import com.patelheggere.goldfarm.model.PlaceDistanceModel;
import com.patelheggere.goldfarm.network.ApiInterface;
import com.patelheggere.goldfarm.network.RetrofitInstance;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Presenter {

    private List<PlaceDistanceModel> placeDistanceList;

    private static final String TAG = "RestPresenter";
    private View view;
    private int PROXIMITY_RADIUS = 2000;

    public Presenter(View view) {
        this.view = view;
    }

    public void getDetails(String type, final double latitude, final double longitude) {
        RetrofitInstance retrofitInstance = new RetrofitInstance();
        retrofitInstance.setClient();
        ApiInterface apiInterface = retrofitInstance.getClient().create(ApiInterface.class);

      //  Call<Place> call = apiInterface.getNearbyPlaces(type, latitude + "," + longitude, PROXIMITY_RADIUS);
        Call<Place> call = apiInterface.getNearbyPlaces(type, latitude + "," + longitude);

        call.enqueue(new Callback<Place>() {
            @Override
            public void onResponse(Call<Place> call, Response<Place> response) {
                Log.d(TAG, "onResponse: " + response.body().getResults().size());
                if(response.body().getStatus().equalsIgnoreCase("OVER_QUERY_LIMIT"))
                {
                    view.queryLimit();
                    view.hideProgressBar();
                    return;
                }
                else if (response.body().getStatus().equalsIgnoreCase("ZERO_RESULTS")){
                    view.noResults();
                    view.hideProgressBar();
                    return;
                }
                else if(response.body().getStatus().equalsIgnoreCase("OK")) {
                    placeDistanceList = new ArrayList<>();
                    DecimalFormat df = new DecimalFormat("#.#");
                    try {
                        for (int i = 0; i < response.body().getResults().size(); i++) {
                            Double lat = response.body().getResults().get(i).getGeometry().getLocation().getLat();
                            Double lng = response.body().getResults().get(i).getGeometry().getLocation().getLng();
                            String placeName = response.body().getResults().get(i).getName();
                            String vicinity = response.body().getResults().get(i).getVicinity();
                            LatLng latLng = new LatLng(lat, lng);
                            LatLng latLng2 = new LatLng(latitude, longitude);
                            PlaceDistanceModel placeDistanceModel = new PlaceDistanceModel();
                            double dist = distanceBetween(latLng2, latLng);
                            if(dist<=2000) {
                                if (dist >= 1000) {
                                    placeDistanceModel.setDistance("" + df.format(dist / 1000.0)+"KM");
                                } else {
                                    placeDistanceModel.setDistance("" + (int) dist+"M");
                                }
                                placeDistanceModel.setName(placeName);
                                placeDistanceList.add(placeDistanceModel);
                            }
                        }
                        view.hideProgressBar();

                        if (placeDistanceList.size() != 0) {
                            //Collections.sort(placeDistanceList, new sortByDistance());
                            view.updateList(placeDistanceList);
                        } else {
                            view.noResults();
                        }


                    } catch (Exception e) {
                        Log.d("onResponse", "There is an error");
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<Place> call, Throwable t) {
                Log.d("", "onFailure: " + t.getLocalizedMessage());
            }
        });
    }
    public static Double distanceBetween(LatLng point1, LatLng point2) {
        if (point1 == null || point2 == null) {
            return null;
        }

        return SphericalUtil.computeDistanceBetween(point1, point2);
    }

    public interface View{
        void updateList(List<PlaceDistanceModel> places);
        void showProgressBar();
        void hideProgressBar();
        void noResults();
        void queryLimit();
    }
}
