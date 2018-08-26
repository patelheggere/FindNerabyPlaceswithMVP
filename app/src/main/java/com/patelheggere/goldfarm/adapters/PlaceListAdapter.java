package com.patelheggere.goldfarm.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.patelheggere.goldfarm.R;
import com.patelheggere.goldfarm.model.PlaceDistanceModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class PlaceListAdapter extends RecyclerView.Adapter<PlaceListAdapter.ViewHolder> {

    private static final String TAG = "PlaceListAdapter";
    private Context mContext;

    private List<PlaceDistanceModel> placeList = new ArrayList<PlaceDistanceModel>();
    private int screen;


    public PlaceListAdapter(Context context, List<PlaceDistanceModel> placeList) {
        this.mContext = context;
        this.placeList = placeList;
    }

    @NonNull
    @Override
    public PlaceListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.name_distance_item, parent, false);
        return new PlaceListAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaceListAdapter.ViewHolder holder, int position) {

        PlaceDistanceModel place = placeList.get(position);
        if(place!=null) {
            if (place.getName() != null)
                holder.name.setText(place.getName());
            if (place.getDistance() != null)
               holder.distance.setText(place.getDistance());
        }
    }

    @Override
    public int getItemCount() {
        if (placeList != null) {
            return placeList.size();

        } else {
            return 0;
        }

    }


    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView name;
        TextView distance;

        public ViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.tv_name);
            distance = itemView.findViewById(R.id.tv_distance);
        }
    }

    private String getDate(long time_stamp_server) {
        SimpleDateFormat formatter = new SimpleDateFormat("dd-mm-yyyy");
        return formatter.format(time_stamp_server);
    }

}
