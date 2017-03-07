package com.wifinder.wifinder.adapter;

import android.content.Context;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.wifinder.wifinder.R;
import com.wifinder.wifinder.activity.HomeActivity;
import com.wifinder.wifinder.listener.HomeCardOnClickListener;

import java.util.List;

/**
 * Created by stefano_mbpro on 17/11/15.
 */
public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.HomeViewHolder>{


    private List<WifiP2pDevice> deviceList;
    private Context context;
    private HomeActivity activity;
    private WifiP2pManager.Channel mChannel;
    private WifiP2pManager mManager;
    private int lastPosition = -1;

    public HomeAdapter(List<WifiP2pDevice> deviceList, HomeActivity context, WifiP2pManager.Channel mChannel, WifiP2pManager mManager) {
        this.deviceList = deviceList;
        this.context = context;
        this.mChannel = mChannel;
        this.mManager = mManager;
        this.activity = context;
    }

    public List<WifiP2pDevice> getDeviceList() {
        return deviceList;
    }

    public void setDeviceList(List<WifiP2pDevice> deviceList) {
        this.deviceList = deviceList;
    }

    @Override
    public HomeViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.card_home, viewGroup, false);
        HomeViewHolder holder = new HomeViewHolder(itemView);
        return holder;
    }

    public boolean add(WifiP2pDevice device){
        WifiP2pDevice removeTarget = null;
        for (int i = 0; i < this.deviceList.size(); i++){
            WifiP2pDevice dev = this.deviceList.get(i);
            if(device.deviceAddress.equals(dev.deviceAddress)) {
                removeTarget = dev;
            }
        }
        if (removeTarget != null)
            this.deviceList.remove(removeTarget);
        boolean result = this.deviceList.add(device);
        this.notifyDataSetChanged();
        return result;
    }

    @Override
    public void onBindViewHolder(HomeViewHolder holder, int i) {
        WifiP2pDevice device = deviceList.get(i);
        //String devDescription = device.toString().split("\n")[0];
        String mac = device.toString().split("\n")[1].trim().split(" ")[1];
        holder.homeCardName.setText(device.deviceName);
        holder.homeCardMac.setText("Mac address: "+mac);
        holder.cardView.setOnClickListener(new HomeCardOnClickListener(device, mManager, mChannel, activity));
        //setAnimation(holder.container, i);
    }

    private void setAnimation(View viewToAnimate, int position) {
        // If the bound view wasn't previously displayed on screen, it's animated
        if (position > lastPosition)
        {
            Animation animation = AnimationUtils.loadAnimation(context, android.R.anim.slide_in_left);
            viewToAnimate.startAnimation(animation);
            lastPosition = position;
        }else if (position < lastPosition){
            Animation animation = AnimationUtils.loadAnimation(context, android.R.anim.slide_out_right);
            viewToAnimate.startAnimation(animation);
            lastPosition = position;
        }
    }

    @Override
    public int getItemCount() {
        return getDeviceList().size();
    }



    // VIEW HOLDER

    public static class HomeViewHolder extends RecyclerView.ViewHolder {

        protected TextView homeCardName;
        protected TextView homeCardMac;
        protected View cardView;
        protected CardView container;

        public HomeViewHolder(View v) {
            super(v);
            homeCardName= (TextView) v.findViewById(R.id.home_card_name);
            homeCardMac= (TextView) v.findViewById(R.id.home_card_mac);
            cardView = v;
            container = (CardView) itemView.findViewById(R.id.home_card_view);
        }

    }

    // END VIEW HOLDER

}
