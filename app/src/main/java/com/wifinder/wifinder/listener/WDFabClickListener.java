package com.wifinder.wifinder.listener;

import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;

import com.wifinder.wifinder.activity.HomeActivity;
import com.wifinder.wifinder.singleton.WorkflowManager;

import java.util.HashMap;
import java.util.Map;


public class WDFabClickListener implements View.OnClickListener {

    private HomeActivity activity;
    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    WifiP2pDnsSdServiceInfo serviceInfo;
    View view;

    public WDFabClickListener(WifiP2pManager mManager, WifiP2pManager.Channel mChannel, HomeActivity activity) {
        this.mManager = mManager;
        this.mChannel = mChannel;
        this.activity = activity;
    }

    @Override
    public void onClick(View view) {
        this.view = view;
        if (!activity.isValidName()){
            Snackbar.make(view, "Please choose a valid nickname first!", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            return;
        }
        if(WorkflowManager.getWorkflowManager().isConnected() && WorkflowManager.getWorkflowManager().isGroupOwner()){
            return;
        }else{
            Snackbar.make(view, "Setting up service discoverable by other users", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            registerService();
        }
    }

    private void registerService(){
        //  Create a string map containing information about your service, we just need to put the name of the group owner
        Map record = new HashMap();
        record.put("name", WorkflowManager.getWorkflowManager().getMyself().getName());
        serviceInfo = WifiP2pDnsSdServiceInfo.newInstance("WiFinder", "_presence._tcp", record);

        mManager.clearLocalServices(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                mManager.addLocalService(mChannel, serviceInfo, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        Log.d("SERVICE", "Added local service for discovery");;
                        WorkflowManager.getWorkflowManager().setIsServiceProvider(true);
                        mManager.clearServiceRequests(mChannel, new WifiP2pManager.ActionListener() {
                            @Override
                            public void onSuccess() {
                                Log.d("SERVICE", "Cleared service request");
                            }

                            @Override
                            public void onFailure(int reason) {
                                Log.d("SERVICE", "Error in clearing service request");
                            }
                        });
                    }

                    @Override
                    public void onFailure(int arg0) {
                        Log.d("SERVICE", "Error adding service");
                    }
                });
            }

            @Override
            public void onFailure(int error) {
                // react to failure of clearing the local services
                Snackbar.make(view, "There was a problem setting up your service. Please try again", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

    }
}
