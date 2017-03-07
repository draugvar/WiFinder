package com.wifinder.wifinder.listener;

import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pManager.GroupInfoListener;
import android.util.Log;

import com.wifinder.wifinder.data.User;
import com.wifinder.wifinder.singleton.WorkflowManager;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by stefano_mbpro on 20/11/15.
 */
public class WDGroupInfoListener implements GroupInfoListener {

    @Override
    public void onGroupInfoAvailable(WifiP2pGroup group) {
        Log.d("WIFIP2P", "Inside onGroupInfoAvailable");
        if(group != null) {

            WorkflowManager.getWorkflowManager().setClientList(group.getClientList());
            WorkflowManager.getWorkflowManager().setGroupOwner(group.getOwner());

            Log.d("WIFIP2P", "Client list:" + WorkflowManager.getWorkflowManager().getClientList().toString());
            Log.d("WIFIP2P", "Group owner:" + WorkflowManager.getWorkflowManager().getGroupOwner().toString());

            //Remove disconnected users from group by removing from userList
            LinkedList<User> toRemove = new LinkedList<>();
            if (WorkflowManager.getWorkflowManager().isGroupOwner()) {
                List<User> userList = WorkflowManager.getWorkflowManager().getUserList();
                List<WifiP2pDevice> deviceList = WorkflowManager.getWorkflowManager().getClientList();
                Log.d("DISCONNECT", "List sizes: users -> " + userList.size() + " clients -> " + deviceList.size());
                for (User user : userList) {
                    boolean found = false;
                    for (WifiP2pDevice device : deviceList) {
                        Log.d("DISCONNECT", "Scanning device: " + device.deviceAddress);
                        if (device.deviceAddress.equals(user.getMacAddr())) {
                            found = true;
                            Log.d("DISCONNECT", "found mac address match for "+device.deviceAddress);
                            break;
                        }
                    }
                    if (!found) {
                        toRemove.add(user);
                    }
                }
                for(User user : toRemove) {
                    Log.d("DISCONNECT", "Removing " + user.getName());
                    WorkflowManager.getWorkflowManager().removeUser(user);
                    WorkflowManager.getWorkflowManager().removeGroupAdapter(user);
                }
            }
        }
        else{ //This means group is null and that there is no connection anymore. CHECK IF CALLED BY BOTH OWNER AND CLIENT
            Log.d("DISCONNECT", "Group is null. We set as disconnected");
            List<User> userList = WorkflowManager.getWorkflowManager().getUserList();
            List<WifiP2pDevice> deviceList = WorkflowManager.getWorkflowManager().getClientList();
            Log.d("DISCONNECT", "List sizes: users -> " + userList.size() + " clients -> " + deviceList.size());
            for(User user : WorkflowManager.getWorkflowManager().getUserList()){
                WorkflowManager.getWorkflowManager().removeUser(user);
                WorkflowManager.getWorkflowManager().removeGroupAdapter(user);
            }
            WorkflowManager.getWorkflowManager().getUserList().clear();
            WorkflowManager.getWorkflowManager().getClientList().clear();
            WorkflowManager.getWorkflowManager().setDisconnected();
        }
    }
}
