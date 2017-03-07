package com.wifinder.wifinder.listener;

import android.content.Context;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.wifinder.wifinder.activity.HomeActivity;
import com.wifinder.wifinder.singleton.WorkflowManager;



public class HomeCardOnClickListener implements View.OnClickListener{

    WifiP2pDevice device;
    private WifiP2pManager mManager;
    WifiP2pManager.Channel mChannel;
    static HomeActivity activity;


    public HomeCardOnClickListener(WifiP2pDevice device, WifiP2pManager mManager, WifiP2pManager.Channel mChannel, HomeActivity activity) {
        this.device = device;
        this.mManager = mManager;
        this.mChannel = mChannel;
        this.activity = activity;
    }

    @Override
    public void onClick(View v) {
        if (!activity.isValidName()){
            Snackbar.make(v, "Please choose a valid nickname first!", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            return;
        }
        final WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = device.deviceAddress;
        config.groupOwnerIntent = 0;

        mManager.clearServiceRequests(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {

                    @Override
                    public void onSuccess() {
                        Toast.makeText(activity, "Requested connection to: " + device.deviceName, Toast.LENGTH_SHORT).show();
                        Log.d("CONNECTION", "Sent request to connect to " + device.deviceName);
                    }

                    @Override
                    public void onFailure(int reason) {
                        Toast.makeText(activity, "Cannot request connection to: " + device.deviceName, Toast.LENGTH_SHORT).show();
                        Log.d("CONNECTION", "Cannot request connection to " + device.deviceName + ". Error code = " + reason);
                    }
                });
            }

            @Override
            public void onFailure(int reason) {
                Log.d("CONNECTION", "Can't clear service request");
            }
        });

    }
}
