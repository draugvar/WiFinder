package com.wifinder.wifinder.listener;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;

import com.wifinder.wifinder.activity.MapActivity;
import com.wifinder.wifinder.singleton.WorkflowManager;
import com.wifinder.wifinder.task.ServerService;

/**
 * Created by stefano_mbpro on 14/11/15.
 */
public class WDConnectionInfoListener implements WifiP2pManager.ConnectionInfoListener {

    private Context context;
    private Activity activity;

    public WDConnectionInfoListener(Context context, Activity activity) {
        this.context = context;
        this.activity = activity;
    }

    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo info) {
        WorkflowManager.getWorkflowManager().setConnected();
        WorkflowManager.getWorkflowManager().setGroupOwnerIPAddress(info.groupOwnerAddress.getHostAddress());
        if (info.groupFormed && info.isGroupOwner) {
            //GROUP OWNER
            WorkflowManager.getWorkflowManager().setAsGroupOwner();

            //We also do this in startTask in MapActivity
            //context.startService(new Intent(context, ServerService.class));
        } else if (info.groupFormed) {
            //CLIENT
            WorkflowManager.getWorkflowManager().setAsGroupClient();
        }

        //Just for testing purposes
        Intent intent = new Intent(context, MapActivity.class);
        activity.startActivity(intent);
    }
}
