package com.wifinder.wifinder.broadcast_receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

import com.wifinder.wifinder.activity.HomeActivity;
import com.wifinder.wifinder.listener.HomeCardOnClickListener;
import com.wifinder.wifinder.listener.WDConnectionInfoListener;
import com.wifinder.wifinder.listener.WDGroupInfoListener;
import com.wifinder.wifinder.listener.WDPeerListListener;

/**
 * A BroadcastReceiver that notifies of important Wi-Fi p2p events.
 */
public class WiFiDirectBroadcastReceiver extends BroadcastReceiver {

    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private HomeActivity mActivity;
    private WDConnectionInfoListener mConnectionInfoListener;
    private WDPeerListListener mPeerListListener;
    private WDGroupInfoListener mGroupInfoListener;

    public WiFiDirectBroadcastReceiver(WifiP2pManager mManager, WifiP2pManager.Channel mChannel, HomeActivity mActivity, WDConnectionInfoListener mConnectionInfoListener, WDPeerListListener mPeerListListener, WDGroupInfoListener mGroupInfoListener) {
        this.mManager = mManager;
        this.mChannel = mChannel;
        this.mActivity = mActivity;
        this.mConnectionInfoListener = mConnectionInfoListener;
        this.mPeerListListener = mPeerListListener;
        this.mGroupInfoListener = mGroupInfoListener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            // Check to see if Wi-Fi is enabled and notify appropriate activity
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                Log.d("state","WIFI P2P ENABLED");
            } else {
                Log.d("state", "WIFI P2P DISABLED");
            }

        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            Log.d("state","PEERS CHANGED ");
            //Not needed since using service
            //mManager.requestPeers(mChannel, mPeerListListener);

        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            Log.d("state", "NEW CONNECTION/DISCONNECTION");

            NetworkInfo networkInfo = (NetworkInfo) intent
                    .getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
            Log.d("CONNECTION","Network info: "+networkInfo.getDetailedState().toString());
            Log.d("CONNECTION","Reason:"+networkInfo.getReason());
            if (networkInfo.isConnected()) {
                // We are connected with the other device, request connection
                // info to find group owner IP
                mManager.requestConnectionInfo(mChannel, mConnectionInfoListener);
                mManager.requestGroupInfo(mChannel, mGroupInfoListener);
                Log.d("CONNECTION","Requested connectionInfo e groupInfo");
            }
            // Respond to new connection or disconnections
        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            // Respond to this device's wifi state changing
            Log.d("state","WIFI P2P STATE CHANGED");
        }
    }
}