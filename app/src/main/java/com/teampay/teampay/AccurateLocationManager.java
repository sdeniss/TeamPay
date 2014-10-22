package com.teampay.teampay;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

/**
 * Created by denis on 13/10/14.
 */
public class AccurateLocationManager {


    public static interface OnLocationAccurateListener{
        void OnLocationAccurate(Location location);
    }



    private LocationListener gpsListener;
    private Location curLocation;
    private LocationManager locationManager;
    private OnLocationAccurateListener onLocationAccurateListener;
    private int minAccuracy;

    public AccurateLocationManager(int minAccuracy, Context context, OnLocationAccurateListener onLocationAccurateListener){
        this.onLocationAccurateListener = onLocationAccurateListener;
        this.minAccuracy = minAccuracy;
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        createGpsListner();
        startGpsListener();
    }

    public AccurateLocationManager(int minAccuracy, LocationManager locationManager, OnLocationAccurateListener onLocationAccurateListener){
        this.onLocationAccurateListener = onLocationAccurateListener;
        this.minAccuracy = minAccuracy;
        this.locationManager = locationManager;
        createGpsListner();
        startGpsListener();
    }


    private void createGpsListner()
    {
        gpsListener = new LocationListener(){
            public void onLocationChanged(android.location.Location location)
            {
                curLocation = location;

                // check if locations has accuracy data
                if(curLocation.hasAccuracy())
                {
                    if(curLocation.getAccuracy() <= minAccuracy)
                    {
                        if(onLocationAccurateListener != null)
                            onLocationAccurateListener.OnLocationAccurate(curLocation);
                        stopGpsListner();
                    }
                }
            }
            public void onProviderDisabled(String provider){}
            public void onProviderEnabled(String provider){}
            public void onStatusChanged(String provider, int status, Bundle extras){}
        };
    }

    private void startGpsListener()
    {

        if(locationManager != null)
            // hit location update in intervals of 5sec and after 10meters offset
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, gpsListener);
    }

    private void stopGpsListner()
    {
        if(locationManager != null)
            locationManager.removeUpdates(gpsListener);
    }
}

