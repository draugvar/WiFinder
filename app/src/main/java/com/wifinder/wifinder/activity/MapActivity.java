package com.wifinder.wifinder.activity;

import android.content.Intent;
import android.net.wifi.p2p.WifiP2pDevice;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.wifinder.wifinder.R;
import com.wifinder.wifinder.adapter.GroupAdapter;
import com.wifinder.wifinder.data.User;
import com.wifinder.wifinder.singleton.WorkflowManager;
import com.wifinder.wifinder.task.ClientService;
import com.wifinder.wifinder.task.ServerService;

import java.util.LinkedList;
import java.util.List;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback{

    private GroupAdapter groupAdapter;
    private RecyclerView mapRecyclerView;
    private RecyclerView.LayoutManager groupLayoutManager;
    private GoogleMap googleMap;
    private boolean isInit = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Map initialization
        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //UI initialization
        groupAdapter = new GroupAdapter(this);
        mapRecyclerView = (RecyclerView) findViewById(R.id.map_activity_recycler_view);
        mapRecyclerView.setHasFixedSize(true);
        groupLayoutManager = new LinearLayoutManager(this);
        mapRecyclerView.setLayoutManager(groupLayoutManager);
        mapRecyclerView.setAdapter(groupAdapter);
        startTasks();
    }

    private void startTasks(){
        if(WorkflowManager.getWorkflowManager().isConnected() && !WorkflowManager.getWorkflowManager().isGroupOwner()) {
            Log.d("TASKS", "Starting Client Task");
            Intent intent = new Intent(this, ClientService.class);
            intent.putExtra("ADDRESS", WorkflowManager.getWorkflowManager().getGroupOwnerIPAddress());
            startService(intent);
        }else{
            Intent intent = new Intent(this, ServerService.class);
            Log.d("TASKS" ,"Starting Server Task");
            startService(intent);
        }
    }



    //Map initialization related
    @Override
    public void onMapReady(GoogleMap gMap) {
        this.googleMap = gMap;

    }

    @Override
    protected void onResume() {
        super.onResume();
        WorkflowManager.getWorkflowManager().registerGroupAdapter(this.groupAdapter);
        WorkflowManager.getWorkflowManager().registerCurrentActivity(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        WorkflowManager.getWorkflowManager().unregisterGroupAdapter();
        WorkflowManager.getWorkflowManager().unregisterCurrentActivity();
    }

    public void updateMapStatus(){
        List<User> userList = WorkflowManager.getWorkflowManager().getUserList();
        List<Double> lats = new LinkedList<>();
        List<Double> longs = new LinkedList<>();
        googleMap.clear();

        User myself = WorkflowManager.getWorkflowManager().getMyself();
        if (myself.getLatitude() != null && myself.getLongitude() != null) {
            Log.d("LOCATION", "Init with not null coordinates");
            LatLng myPosition = new LatLng(myself.getLatitude(), myself.getLongitude());
            googleMap.addMarker(new MarkerOptions()
                    .position(myPosition)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                    .title(myself.getName()));
            if (myself.getLatitude() != 0.0 && myself.getLongitude() != 0.0){
                lats.add(myself.getLatitude());
                longs.add(myself.getLongitude());
            }
        }

        for (User user : userList){
            if (user.getLatitude() != null && user.getLongitude() != null){
                Log.d("LOCATION", "User " + user.getName() + "  has coordinates: " + user.getLatitude() + ", " + user.getLongitude());
                LatLng userPosition = new LatLng(user.getLatitude(), user.getLongitude());
                lats.add(user.getLatitude());
                longs.add(user.getLongitude());
                //If owner set mark color to accent
                if(user.getMacAddr().equals(WorkflowManager.getWorkflowManager().getGroupOwner().deviceAddress)) {
                    googleMap.addMarker(new MarkerOptions().position(userPosition).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE)).title(user.getName()));
                }
                else{
                    googleMap.addMarker(new MarkerOptions().position(userPosition).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)).title(user.getName()));
                }
            }
            else Log.d("LOCATION","User "+ user.getName() + "  has null coordinates");
        }


            LatLng newCenter = findCenter(lats, longs);
        if (newCenter!= null) {
            Log.d("LOCATION", "New center is: " + newCenter.toString());
            if (isInit) {
                Log.d("LOCATION", "CAMERA initialization center is: " + newCenter.toString());
                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(newCenter)      // Sets the center of the map to my position
                        .zoom(17)                   // Sets the zoom
                        .bearing(90)                // Sets the orientation of the camera to east
                        .tilt(30)                   // Sets the tilt of the camera to 30 degrees
                        .build();                   // Creates a CameraPosition from the builder
                googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                isInit = false;
            }
        }
    }

    private LatLng findCenter(List<Double> lats, List<Double> longs) {
        Log.d("LOCATION", "lists sizes lats: " + lats.size()+ " longs: " + longs.size());
        if (lats.size() == 0 || longs.size() == 0){
            return null;
        }
        double avgLat = 0.0;
        double avgLong = 0.0;
        for (double lat : lats)
            avgLat += lat;
        for (double lon : longs)
            avgLong += lon;
        return new LatLng((avgLat/lats.size()), (avgLong/lats.size()));

    }


    public void centerCamOnUser (User u){
        Log.d("CENTERCAM", "centering camera on user: " + u.getName());
        if (u.getLatitude() != null && u.getLongitude() != null){
            Log.d("CENTERCAM", "coordinates not null");
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(new LatLng(u.getLatitude(), u.getLongitude()))      // Sets the center of the map to my position
                    .zoom(18)                   // Sets the zoom
                    .tilt(30)                   // Sets the tilt of the camera to 30 degrees
                    .build();                   // Creates a CameraPosition from the builder
            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }
    }


}
