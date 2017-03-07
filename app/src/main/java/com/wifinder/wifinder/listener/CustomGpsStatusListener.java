package com.wifinder.wifinder.listener;

import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.util.Log;

import com.wifinder.wifinder.singleton.WorkflowManager;

/**
 * Created by draugvar on 30/11/15.
 */
public class CustomGpsStatusListener implements GpsStatus.Listener {

    @Override
    public void onGpsStatusChanged(int event) {
        if (event == GpsStatus.GPS_EVENT_SATELLITE_STATUS) {
            int satellites = 0;
            int satellitesInFix = 0;
            for (GpsSatellite sat : WorkflowManager.getWorkflowManager().
                    getLocationManager().getGpsStatus(null).getSatellites()) {
                if (sat.usedInFix()) {
                    satellitesInFix++;
                }
                satellites++;
            }
            Log.v("LOCATION", "Number of satellites: " + satellites + ", used In Last Fix " + satellitesInFix);
            boolean isInside = WorkflowManager.getWorkflowManager().getMyself().isInside();
            if(satellitesInFix>=4) {
                if (WorkflowManager.getWorkflowManager().getMyself().isInside())
                    WorkflowManager.getWorkflowManager().getMyself().setIsInside(false);
            }
            else if (!WorkflowManager.getWorkflowManager().getMyself().isInside())
                WorkflowManager.getWorkflowManager().getMyself().setIsInside(true);
        }
    }
}
