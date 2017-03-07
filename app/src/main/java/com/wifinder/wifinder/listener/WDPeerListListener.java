package com.wifinder.wifinder.listener;

import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.TextView;

import com.wifinder.wifinder.adapter.HomeAdapter;
import com.wifinder.wifinder.singleton.WorkflowManager;

import java.util.LinkedList;
import java.util.List;


// Called automatically by the P2P manager on successful peer discovery.
// DEPRECATED.

public class WDPeerListListener implements WifiP2pManager.PeerListListener {

    private FloatingActionButton fab;
    private WifiP2pDevice device;
    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private HomeAdapter mAdapter;

    public WDPeerListListener (FloatingActionButton fab, WifiP2pManager manager, WifiP2pManager.Channel channel,HomeAdapter adapter){
        this.fab = fab;
        this.mManager = manager;
        this.mChannel = channel;
        device = null;
        this.mAdapter = adapter;
    }

    @Override
    public void onPeersAvailable(WifiP2pDeviceList peers) {
        int peersListSize = peers.getDeviceList().size();
        Log.d("WIFIP2P", "On Peers Available called with device list size: " + peersListSize);
        List<WifiP2pDevice> deviceList = new LinkedList<>();
        deviceList.addAll(peers.getDeviceList());
        mAdapter.setDeviceList(deviceList);
        mAdapter.notifyDataSetChanged();
        if (peersListSize == 0)
            Snackbar.make(fab, "No Peer detected", Snackbar.LENGTH_LONG).setAction("Action", null).show();

    }
}
