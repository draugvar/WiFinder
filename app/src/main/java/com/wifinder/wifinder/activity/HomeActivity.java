package com.wifinder.wifinder.activity;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.net.wifi.p2p.nsd.WifiP2pServiceRequest;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.wifinder.wifinder.R;
import com.wifinder.wifinder.adapter.HomeAdapter;
import com.wifinder.wifinder.broadcast_receiver.WiFiDirectBroadcastReceiver;
import com.wifinder.wifinder.data.User;
import com.wifinder.wifinder.fragments.InputNameFragment;
import com.wifinder.wifinder.interfaces.InputNameInterface;
import com.wifinder.wifinder.listener.CustomGpsStatusListener;
import com.wifinder.wifinder.listener.CustomLocationListener;
import com.wifinder.wifinder.listener.WDConnectionInfoListener;
import com.wifinder.wifinder.listener.WDFabClickListener;
import com.wifinder.wifinder.listener.WDGroupInfoListener;
import com.wifinder.wifinder.listener.WDPeerListListener;
import com.wifinder.wifinder.singleton.WorkflowManager;
import com.wifinder.wifinder.task.ServiceDiscoverThread;

import java.net.NetworkInterface;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class HomeActivity extends AppCompatActivity {

    //UI
    private FloatingActionButton fab;
    private RecyclerView homeRecyclerView;
    private HomeAdapter homeAdapter;
    private RecyclerView.LayoutManager homeLayoutManager;
    private WifiManager wifiManager;
    private CardView inputNameCard;
    private CardView instructionCard;


    // WIFI P2P related
    WifiP2pManager mManager;
    WifiP2pManager.Channel mChannel;
    BroadcastReceiver mReceiver;
    IntentFilter mIntentFilter;
    WDPeerListListener mPeerListListener;
    WDConnectionInfoListener mConnectionInfoListener;
    WDGroupInfoListener mGroupInfoListener;

    //Domain
    private String userName;
    private String myAddress;
    private Double myLat;
    private Double myLong;
    private boolean validName;

    //Service discovery
    final HashMap<String, String> serviceMap = new HashMap<String, String>();  // First field is device address, second is name of the user

    ///////////////////////////////
    // ACTIVITY LIFECYCLE
    ///////////////////////////////


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        fab = (FloatingActionButton) findViewById(R.id.fab);

        // Name management
        inputNameCard = (CardView) findViewById(R.id.input_name_card);
        instructionCard = (CardView) findViewById(R.id.instruction_card);
        userName = "";
        validName = false;
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        userName = sharedPref.getString("USERNAME", "");
        if (userName.equals("")) {
            userName = "unknown";
            showCardWithAnim(0);
        }else{
            showCardWithAnim(1);
            validName = true;
        }

        // WIFI P2P initialization
        mConnectionInfoListener = new WDConnectionInfoListener(getApplicationContext(), this);
        mManager = (WifiP2pManager) getSystemService(getBaseContext().WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(), null);

        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        //UI initialization
        homeAdapter = new HomeAdapter(new LinkedList<WifiP2pDevice>(), this, mChannel, mManager);
        homeRecyclerView = (RecyclerView) findViewById(R.id.home_recycler_view);
        homeRecyclerView.setHasFixedSize(true);
        homeLayoutManager = new LinearLayoutManager(this);
        homeRecyclerView.setLayoutManager(homeLayoutManager);
        homeRecyclerView.setAdapter(homeAdapter);

        fab.setOnClickListener(new WDFabClickListener(mManager, mChannel, this));

        //Setting up P2PListeners
        mPeerListListener = new WDPeerListListener(fab, mManager, mChannel, homeAdapter);
        mGroupInfoListener = new WDGroupInfoListener();
        mReceiver = new WiFiDirectBroadcastReceiver(mManager, mChannel, this, mConnectionInfoListener, mPeerListListener, mGroupInfoListener);

        //Registering objects to WorkflowManager singleton
        WorkflowManager.getWorkflowManager().registerManager(mManager);
        WorkflowManager.getWorkflowManager().registerWifiP2PChannel(mChannel);
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {return;}
        locationManager.addGpsStatusListener(new CustomGpsStatusListener());
        WorkflowManager.getWorkflowManager().setLocationManager(locationManager);

        //GPS initialization
        myLat = null;
        myLong= null;
        CustomLocationListener locationListener = new CustomLocationListener(this);
        try{
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 2, locationListener);
            Location myLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (myLocation != null){
                //Obtain user coordinates at startup
                myLat = myLocation.getLatitude();
                myLong = myLocation.getLongitude();
            }
        }
        catch (SecurityException e){
            Log.d("LOCATION","Location not enabled");
        }


        //User initialization
        myAddress = getWIFIP2PMac().toLowerCase();
        if (myAddress.equals(""))
            Log.d("WIFIP2P", "PROBLEM in retrieving the WifiP2P mac address");
        User myself = new User(userName, myAddress, myLat, myLong);
        WorkflowManager.getWorkflowManager().setMyself(myself);

        //Service initialization
        initDiscoverService();
        new Thread(new ServiceDiscoverThread()).start();
    }


    //SERVICE DISCOVERY

    //Adds the service listeners to the manager
    private void initDiscoverService(){
        WifiP2pManager.DnsSdTxtRecordListener txtListener = new WifiP2pManager.DnsSdTxtRecordListener() {

            @Override
            public void onDnsSdTxtRecordAvailable(
                    String fullDomain, Map record, WifiP2pDevice device) {
                Log.d("SERVICE","DnsSdTxtRecord available -" + record.toString());
                serviceMap.put(device.deviceAddress, (String) record.get("name"));
            }
        };

        // serviceListeners adds the services as WIFIP2P device objects to homeAdapter
        WifiP2pManager.DnsSdServiceResponseListener serviceListener = new WifiP2pManager.DnsSdServiceResponseListener() {
            @Override
            public void onDnsSdServiceAvailable(String instanceName, String registrationType,
                                                WifiP2pDevice resourceType) {

                // Update the device name with the user name
                resourceType.deviceName = serviceMap.containsKey(resourceType.deviceAddress) ? serviceMap.get(resourceType.deviceAddress) : resourceType.deviceName;
                homeAdapter.add(resourceType);
                Log.d("SERVICE", "Discovered service " + instanceName);
            }
        };
        mManager.setDnsSdResponseListeners(mChannel, serviceListener, txtListener);
    }


    //Called by WorkflowManager every 3 seconds, timed by serviceDiscoverThread
    public void discoverService() {
        Log.v("SERVICE", "DiscoverService called");
        WifiP2pServiceRequest serviceRequest = WifiP2pDnsSdServiceRequest.newInstance();
        mManager.addServiceRequest(mChannel, serviceRequest, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.v("SERVICE", "addServiceRequest success");
                mManager.discoverServices(mChannel, new WifiP2pManager.ActionListener() {

                    @Override
                    public void onSuccess() {
                        Log.v("SERVICE", "discoverServices success");
                    }

                    @Override
                    public void onFailure(int code) {
                        Log.d("SERVICE", "discoverServices failed");
                    }
                });
            }

            @Override
            public void onFailure(int code) {
                Log.d("SERVICE", "addServiceRequest failed");
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            //return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mReceiver, mIntentFilter);
        WorkflowManager.getWorkflowManager().registerCurrentActivity(this);

        //Turn WiFi on
        wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
        if(!wifiManager.isWifiEnabled()){
            wifiManager.setWifiEnabled(true);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
        WorkflowManager.getWorkflowManager().unregisterCurrentActivity();
    }

    // Utility to get own WIFIP2P MAC address which is different from WIFI's MAC address
    private String getWIFIP2PMac(){

        String address = "";
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface ntwInterface : interfaces) {
                Log.v("NETWORK", "network interface name: " + ntwInterface.getName());
                if (ntwInterface.getName().equalsIgnoreCase("p2p0")) {
                    byte[] byteMac = ntwInterface.getHardwareAddress();
                    if (byteMac==null){
                        return "";
                    }
                    StringBuilder strBuilder = new StringBuilder();
                    for (int i=0; i<byteMac.length; i++) {
                        strBuilder.append(String.format("%02X:", byteMac[i]));
                    }

                    if (strBuilder.length()>0){
                        strBuilder.deleteCharAt(strBuilder.length()-1);
                    }

                    return strBuilder.toString();
                }

            }
        } catch (Exception e) {
            Log.d("WIFIP2P", e.getMessage());
        }

        // If address is still empty it means there are no network interfaces called p2p0
        // Workaround: Get WIFI MAC and add 2 to the first hex number
        if (address.equals("")){
            WifiManager wifiMan = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInf = wifiMan.getConnectionInfo();
            String wifiMac = wifiInf.getMacAddress();
            String[] wifiMacArr = wifiMac.split(":");
            int tempNum = Integer.decode("0x" + wifiMacArr[0]);
            tempNum += 2;
            String newFirstHex = Integer.toHexString(tempNum);
            wifiMacArr[0] = newFirstHex;
            for (String addrPart : wifiMacArr){
                address += (addrPart + ":");
            }
            address = address.substring(0, address.length()-1);
            Log.v("NETWORK", "computed address is : " + address);
        }
        return address;
    }


    // Input name card button action
    public void inputUserName(View view) {
        //Hide keyboard if it is still active
        InputMethodManager inputManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        try{
            inputManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }catch (Exception e){
            //keyboard is not up
        }

        String nickname = "";
        EditText editText = (EditText) inputNameCard.findViewById(R.id.input_name_card_edit_text);
        nickname = editText.getText().toString();

        //check if nickname is valid (at least 2 characters)
        if (nickname.length() < 2){
            Snackbar.make(fab, "Please choose a valid nickname first!", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            return;
        }

        Snackbar.make(fab, "Nickname chosen: " + nickname, Snackbar.LENGTH_LONG).setAction("Action", null).show();
        Log.d("INPUTNAMEDBG", "name added is: " + nickname);

        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("USERNAME", nickname);
        editor.commit();

        userName = nickname;
        User myself = WorkflowManager.getWorkflowManager().getMyself();
        if (myself == null){
            User newself = new User(userName, myAddress, myLat, myLong);
            WorkflowManager.getWorkflowManager().setMyself(newself);
        }else{
            myself.setName(userName);
        }
        validName = true;

        Animation animationOut = AnimationUtils.loadAnimation(this, android.R.anim.slide_out_right);
        inputNameCard.startAnimation(animationOut);
        inputNameCard.setVisibility(View.GONE);
        Animation animationIn = AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left);
        instructionCard.startAnimation(animationIn);
        instructionCard.setVisibility(View.VISIBLE);
    }


    // Instruction card button action
    public void hideInstructionCard(View view) {
        Animation animationOut = AnimationUtils.loadAnimation(this, android.R.anim.slide_out_right);
        instructionCard.startAnimation(animationOut);
        instructionCard.setVisibility(View.GONE);
    }

    // Show the correct card at startup. Input name card is 0, Instruction card is 1
    private void showCardWithAnim (int card){
        Animation animationIn = AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left);
        if (card == 0){
            inputNameCard.startAnimation(animationIn);
            inputNameCard.setVisibility(View.VISIBLE);
        }else{
            instructionCard.startAnimation(animationIn);
            instructionCard.setVisibility(View.VISIBLE);
        }
    }

    public boolean isValidName() {
        return validName;
    }
}
