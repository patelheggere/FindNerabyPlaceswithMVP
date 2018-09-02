package com.patelheggere.goldfarm.presenter;


import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.SphericalUtil;
import com.patelheggere.goldfarm.helper.AppUtils;
import com.patelheggere.goldfarm.model.Place;
import com.patelheggere.goldfarm.model.PlaceDistanceModel;
import com.patelheggere.goldfarm.network.ApiInterface;
import com.patelheggere.goldfarm.network.RetrofitInstance;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.patelheggere.goldfarm.helper.AppUtils.Constants.OK;
import static com.patelheggere.goldfarm.helper.AppUtils.Constants.OVER_QUERY_LIMIT;
import static com.patelheggere.goldfarm.helper.AppUtils.Constants.ZERO_RESULTS;

public class Presenter {

    private List<PlaceDistanceModel> RestDistanceList, HotelsDistanceList, HospitalsDistanceList;

    private static final String TAG = "Presenter";
    private View view;
    private int PROXIMITY_RADIUS = 2000;
    private String nextPageToken;

    public Presenter(View view) {
        this.view = view;
    }

    public void getDetailsWithToken(final double latitude, final double longitude, String token,final String type) {
        RetrofitInstance retrofitInstance = new RetrofitInstance();
        retrofitInstance.setClient();
        ApiInterface apiInterface = retrofitInstance.getClient().create(ApiInterface.class);

        //  Call<Place> call = apiInterface.getNearbyPlaces(type, latitude + "," + longitude, PROXIMITY_RADIUS);
        // Call<Place> call = apiInterface.getNearbyPlaces(type, latitude + "," + longitude);
        Call<Place> call = apiInterface.getNearbyPlacesWithToken(latitude + "," + longitude, token);
        call.enqueue(new Callback<Place>() {
            @Override
            public void onResponse(Call<Place> call, Response<Place> response) {
                Log.d(TAG, "onResponse: " + response.body().getResults().size());
                if(response.body().getStatus().equalsIgnoreCase(OVER_QUERY_LIMIT))
                {
                    view.queryLimit("");
                    view.hideProgressBar();
                    return;
                }
                else if (response.body().getStatus().equalsIgnoreCase(ZERO_RESULTS)){
                    view.noResults(type);
                    view.hideProgressBar();
                    return;
                }
                else if(response.body().getStatus().equalsIgnoreCase(OK)) {
                    RestDistanceList = new ArrayList<>();
                    DecimalFormat df = new DecimalFormat("#.#");
                    try {
                        final String nextPageToken = response.body().getNextPageToken();
                        for (int i = 0; i < response.body().getResults().size(); i++) {
                            Double lat = response.body().getResults().get(i).getGeometry().getLocation().getLat();
                            Double lng = response.body().getResults().get(i).getGeometry().getLocation().getLng();
                            String placeName = response.body().getResults().get(i).getName();
                            String vicinity = response.body().getResults().get(i).getVicinity();
                            String type = response.body().getResults().get(i).getTypes().get(0);
                            LatLng latLng = new LatLng(lat, lng);
                            LatLng latLng2 = new LatLng(latitude, longitude);
                            PlaceDistanceModel placeDistanceModel = new PlaceDistanceModel();
                            double dist = distanceBetween(latLng2, latLng);
                            if(dist<=2000  && type.equalsIgnoreCase("hospital") || type.equalsIgnoreCase("restaurant") || type.equalsIgnoreCase("lodging"))
                            {

                                if (dist >= 1000) {
                                    placeDistanceModel.setDistance("" + df.format(dist / 1000.0)+"KM");
                                } else {
                                    placeDistanceModel.setDistance("" + (int) dist+"M");
                                }
                                placeDistanceModel.setIcon(response.body().getResults().get(i).getIcon());
                                placeDistanceModel.setPagetoken(nextPageToken);
                                placeDistanceModel.setType(type);
                                placeDistanceModel.setName(placeName);
                                RestDistanceList.add(placeDistanceModel);
                            }
                        }
                        view.hideProgressBar();

                        if (RestDistanceList.size() != 0) {
                            //Collections.sort(RestDistanceList, new sortByDistance());
                            view.updateList(RestDistanceList);
                        } else {
                            view.noResults(type);
                        }


                    } catch (Exception e) {
                        Log.d("onResponse", "There is an error");
                        e.printStackTrace();
                        view.hideProgressBar();
                    }
                }
            }

            @Override
            public void onFailure(Call<Place> call, Throwable t) {
                Log.d("", "onFailure: " + t.getLocalizedMessage());
            }
        });
    }

    public void getDetails(final String type, final double latitude, final double longitude) {
        RetrofitInstance retrofitInstance = new RetrofitInstance();
        retrofitInstance.setClient();
        ApiInterface apiInterface = retrofitInstance.getClient().create(ApiInterface.class);

      //  Call<Place> call = apiInterface.getNearbyPlaces(type, latitude + "," + longitude, PROXIMITY_RADIUS);
        Call<Place> call = apiInterface.getNearbyPlaces(type, latitude + "," + longitude);
        //Call<Place> call = apiInterface.getNearbyPlaces(latitude + "," + longitude);
        call.enqueue(new Callback<Place>() {
            @Override
            public void onResponse(Call<Place> call, Response<Place> response) {
                Log.d(TAG, "onResponse: " + response.body().getResults().size());
                if(response.body().getStatus().equalsIgnoreCase(OVER_QUERY_LIMIT))
                {
                    view.queryLimit(type);
                    view.hideProgressBar();
                    return;
                }
                else if (response.body().getStatus().equalsIgnoreCase(ZERO_RESULTS)){
                    view.noResults(type);
                    view.hideProgressBar();
                    return;
                }
                else if(response.body().getStatus().equalsIgnoreCase(OK))
                {
                    RestDistanceList = new ArrayList<>();
                    Log.d(TAG, "onResponse: OK");
                    DecimalFormat df = new DecimalFormat("#.#");
                    try {
                        nextPageToken = response.body().getNextPageToken();
                        Log.d(TAG, "onResponse: pres:"+nextPageToken);
                        for (int i = 0; i < response.body().getResults().size(); i++) {
                            Double lat = response.body().getResults().get(i).getGeometry().getLocation().getLat();
                            Double lng = response.body().getResults().get(i).getGeometry().getLocation().getLng();
                            String placeName = response.body().getResults().get(i).getName();
                            String vicinity = response.body().getResults().get(i).getVicinity();
                            String type = response.body().getResults().get(i).getTypes().get(0);
                            LatLng latLng = new LatLng(lat, lng);
                            LatLng latLng2 = new LatLng(latitude, longitude);
                            PlaceDistanceModel placeDistanceModel = new PlaceDistanceModel();
                            double dist = distanceBetween(latLng2, latLng);
                            if(dist<=2000 )
                            {
                                Log.d(TAG, "onResponse: distance"+dist);
                                if (dist >= 1000) {
                                    placeDistanceModel.setDistance("" + df.format(dist / 1000.0)+"KM");
                                } else {
                                    placeDistanceModel.setDistance("" + (int) dist+"M");
                                }
                                placeDistanceModel.setIcon(response.body().getResults().get(i).getIcon());
                                placeDistanceModel.setPagetoken(nextPageToken);
                                placeDistanceModel.setType(type);
                                placeDistanceModel.setName(placeName);
                                RestDistanceList.add(placeDistanceModel);
                            }
                        }
                        view.hideProgressBar();

                        if (RestDistanceList.size() != 0) {
                            //Collections.sort(RestDistanceList, new sortByDistance());
                            view.updateList(RestDistanceList);
                        } else {
                            view.noResults(type);
                        }


                    } catch (Exception e) {
                        Log.d("onResponse", "There is an error");
                        e.printStackTrace();
                        view.hideProgressBar();
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
        void noResults(String type);
        void queryLimit(String type);
    }
}
