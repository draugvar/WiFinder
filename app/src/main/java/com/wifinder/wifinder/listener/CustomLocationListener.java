package com.wifinder.wifinder.listener;

import android.app.Activity;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;
import com.wifinder.wifinder.singleton.WorkflowManager;

public class CustomLocationListener implements LocationListener {
    private Activity activity;

    public CustomLocationListener(Activity activity) {
        this.activity = activity;
    }

    @Override
    public void onLocationChanged(Location loc) {
        double longitude = loc.getLongitude();
        double latitude = loc.getLatitude();
        WorkflowManager.getWorkflowManager().getMyself().setLongitude(longitude);
        WorkflowManager.getWorkflowManager().getMyself().setLatitude(latitude);
        Log.d("LOCATION", "Longitude: " + longitude + " Latitude: " + latitude);

        //Updating UI
        WorkflowManager.getWorkflowManager().updateGroupAdapter(WorkflowManager.getWorkflowManager().getMyself());
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}

    @Override
    public void onProviderDisabled(String provider) {}

    @Override
    public void onProviderEnabled(String provider) {}
}
