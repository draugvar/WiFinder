package com.wifinder.wifinder.singleton;

import android.app.Activity;
import android.location.LocationManager;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

import com.wifinder.wifinder.activity.HomeActivity;
import com.wifinder.wifinder.activity.MapActivity;
import com.wifinder.wifinder.adapter.GroupAdapter;
import com.wifinder.wifinder.data.User;
import com.wifinder.wifinder.json.UserJSON;
import com.wifinder.wifinder.json.UserJSONList;
import com.wifinder.wifinder.task.ServerRunnableThread;

import java.nio.channels.Channel;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by stefano_mbpro on 20/11/15.
 */
public class WorkflowManager {

    private static WorkflowManager instance = null;

    //Handles
    private LocationManager mLocationManager;
    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private WifiP2pDevice groupOwner;
    private GroupAdapter groupAdapter;
    private Activity currentActivity;

    //State
    private boolean connected = false;
    private boolean isGroupOwner=false;
    private boolean isServiceProvider=false;

    //Network data
    private List<WifiP2pDevice> clientList = new LinkedList<>();
    private String groupOwnerIPAddress;

    //User data
    private User myself;
    private List<User> userList = new LinkedList<User>(); // SHOULD MAKE LIST THREAD SAFE

    private WorkflowManager() {}

    public static synchronized WorkflowManager getWorkflowManager() {
        if (instance == null) {
            instance = new WorkflowManager();
        }
        return instance;
    }

    public void registerManager(WifiP2pManager mManager){
        Log.d("WORKFLOW","WifiP2PManager registered in singleton");
        this.mManager = mManager;
    }

    public void unregisterManager(){
        Log.d("WORKFLOW","WifiP2PManager unregistered in singleton");
        this.mManager = null;
    }

    public void registerWifiP2PChannel(WifiP2pManager.Channel channel){
        Log.d("WORKFLOW","WifiP2P channel registered in singleton");
        this.mChannel = channel;
    }

    public void unregisterWifiP2PChannel(){
        Log.d("WORKFLOW","WifiP2P channel unregistered in singleton");
        this.mChannel = null;
    }

    public void registerCurrentActivity(Activity activity){
        Log.d("WORKFLOW","CurrentActivity registered in singleton");
        this.currentActivity = activity;
    }

    public void unregisterCurrentActivity(){
        Log.d("WORKFLOW","CurrentActivity unregistered in singleton");
        this.currentActivity = null;
    }

    public void registerGroupAdapter(GroupAdapter groupAdapter){
        Log.d("WORKFLOW","GroupAdapter registered in singleton");
        this.groupAdapter = groupAdapter;
    }

    public void unregisterGroupAdapter(){
        Log.d("WORKFLOW","GroupAdapter unregistered in singleton");
        this.groupAdapter = null;
    }

    public User getMyself() {
        return myself;
    }

    public void setMyself(User myself) {
        this.myself = myself;
        Log.d("WORKFLOW", "User set: " + myself.toString());
    }

    public LocationManager getLocationManager() {
        return mLocationManager;
    }

    public void setLocationManager(LocationManager mLocationManager) {
        this.mLocationManager = mLocationManager;
    }

    public WifiP2pManager.Channel getWIFIP2PChannel() {
        return mChannel;
    }

    public boolean isConnected(){
        return connected;
    }

    public boolean isGroupOwner(){return isGroupOwner;}

    public boolean isServiceProvider() {
        return isServiceProvider;
    }

    public void setIsServiceProvider(boolean isServiceProvider) {
        this.isServiceProvider = isServiceProvider;
    }

    public void setConnected(){
        Log.d("WORKFLOW","Set connected");
        connected = true;
    }

    public void setDisconnected(){
        Log.d("WORKFLOW","Set disconnected");
        connected = false;
    }

    public void setAsGroupOwner(){
        isGroupOwner = true;
    }

    public void setAsGroupClient(){
        isGroupOwner = false;
    }

    public WifiP2pDevice getGroupOwner(){
        return this.groupOwner;
    }

    public void setGroupOwner(WifiP2pDevice owner){
        this.groupOwner = owner;
    }

    public void setClientList(Collection<WifiP2pDevice> list){
        this.clientList.clear();
        this.clientList.addAll(list);
    }

    public List<WifiP2pDevice> getClientList(){
        return this.clientList;
    }

    public String getGroupOwnerIPAddress() {
        return groupOwnerIPAddress;
    }

    public void setGroupOwnerIPAddress(String groupOwnerIPAddress) {
        this.groupOwnerIPAddress = groupOwnerIPAddress;
    }

    // Only adds new user if in the list there is no User with same MAC address. Returns true if added else returns false.
    public boolean addUser(UserJSON userToAdd){
        for(User user: userList){
            if(user.getMacAddr().equals(userToAdd.getMacAddr()))
                return false;
        }
        userList.add(userToAdd.toUser());
        Log.d("WORKFLOW", "Added user: " + userToAdd.getName());
        return true;
    }

    //Finds user and update its fields. Returns false if can't find device with that MAC address.
    public boolean updateUser(UserJSON userUpdated){
        for(User user: userList){
            if(user.getMacAddr().equals(userUpdated.getMacAddr())){
                //UPDATE ALL FIELDS
                user.setName(userUpdated.getName());
                user.setLatitude(userUpdated.getLatitude());
                user.setLongitude(userUpdated.getLongitude());
                user.setIsInside(userUpdated.isInside());
                Log.d("WORKFLOW", "Updated user: " + userUpdated.getName());
                return true;
            }
        }
        return false;
    }

    public boolean removeUser(User userToDelete){
        for(User user: userList) {
            if (user.getMacAddr().equals(userToDelete.getMacAddr())) {
                userList.remove(user);
                Log.d("USERMANAGEMENT", "User: " + user.getMacAddr() + " has been removed");
                return true;
            }
        }
        return false;
    }

    public List<User> getUserList() {
        return userList;
    }

    //To be called only by Server/Group owner
    public UserJSONList getUpdatedUserList(){
        LinkedList<UserJSON> list = new LinkedList<>();

        //First on the list is the group owner
        list.add(myself.toUserJSON());

        //Adding other clients
        for(User user : userList){
            list.add(user.toUserJSON());
        }

        UserJSONList listJSON = new UserJSONList(list);
        return listJSON;
    }

    public WifiP2pManager getWIFIP2PManager() {
        return mManager;
    }

    public void updateGroupAdapter(final User user){
        if(groupAdapter!=null && currentActivity!=null){ //Which means we are on MapActivity
            currentActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    groupAdapter.updateUser(user);
                }
            });

        }
    }

    public void removeGroupAdapter(final User user){
        Log.d("REMOVEUSR", "Removing user: " + user.getMacAddr() + " from group adapter");
        if(groupAdapter!=null && currentActivity!=null){ //Which means we are on MapActivity
            currentActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    groupAdapter.removeUser(user);
                }
            });

        }
    }

    public void updateMapUI() {
        if (currentActivity == null)
                return;
        if (currentActivity.getClass().equals(MapActivity.class)){

            currentActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ((MapActivity)currentActivity).updateMapStatus();
                }
            });
        }
    }

    //Called by ServiceDiscoverThread
    public void discoverServiceUpdate() {
        if (currentActivity == null)
            return;
        if (currentActivity.getClass().equals(HomeActivity.class)) {

            currentActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(currentActivity!=null)
                    ((HomeActivity) currentActivity).discoverService();
                }
            });
        }
    }

}
