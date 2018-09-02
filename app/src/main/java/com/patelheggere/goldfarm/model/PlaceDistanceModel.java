package com.patelheggere.goldfarm.model;

import android.os.Parcel;
import android.os.Parcelable;

public class PlaceDistanceModel implements Parcelable {
    private String name;
    private String distance;
    private String pagetoken;
    private String type;
    private String icon;

    public PlaceDistanceModel() {
    }

    public PlaceDistanceModel(String name, String distance, String pagetoken, String type, String icon) {
        this.name = name;
        this.distance = distance;
        this.pagetoken = pagetoken;
        this.type = type;
        this.icon = icon;
    }

    protected PlaceDistanceModel(Parcel in) {
        name = in.readString();
        distance = in.readString();
        pagetoken = in.readString();
        type = in.readString();
        icon = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(distance);
        dest.writeString(pagetoken);
        dest.writeString(type);
        dest.writeString(icon);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<PlaceDistanceModel> CREATOR = new Creator<PlaceDistanceModel>() {
        @Override
        public PlaceDistanceModel createFromParcel(Parcel in) {
            return new PlaceDistanceModel(in);
        }

        @Override
        public PlaceDistanceModel[] newArray(int size) {
            return new PlaceDistanceModel[size];
        }
    };

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public String getPagetoken() {
        return pagetoken;
    }

    public void setPagetoken(String pagetoken) {
        this.pagetoken = pagetoken;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }
}
