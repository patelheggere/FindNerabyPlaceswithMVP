package com.patelheggere.goldfarm.helper;

import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;

public class AppUtils {
    public static boolean hasLollipop() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    public static boolean isLocationEnabled(Context context) {
        int locationMode = 0;
        String locationProviders;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            try {
                locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);

            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
            }
            return locationMode != Settings.Secure.LOCATION_MODE_OFF;
        } else {
            locationProviders = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            return !TextUtils.isEmpty(locationProviders);
        }
    }

    public class Constants{
        public static final long ONE_SECOND = 1000;
        public static final long TWO_SECOND = 2000;
        public static final long THREE_SECOND = 3000;
        public static final long FOUR_SECOND = 4000;
        public static final long TEN_SECOND = 10000;
        public static final long EIGHT_SECOND = 8000;

        public static final String PLACES = "PLACES";
        public static final String ADDS = "ADDS";
        public static final String LAT = "LAT";
        public static final String LON = "LON";
        public static final String RESTAURANT = "restaurant";
        public static final String HOTEL = "lodging";
        public static final String HOSPITAL = "hospital";

        public static final String OVER_QUERY_LIMIT = "OVER_QUERY_LIMIT";
        public static final String ZERO_RESULTS = "ZERO_RESULTS";
        public static final String OK = "OK";
    }
}
