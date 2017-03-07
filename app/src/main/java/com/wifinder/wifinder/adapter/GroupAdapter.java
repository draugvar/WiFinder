package com.wifinder.wifinder.adapter;

import android.content.Context;
import android.content.res.TypedArray;
import android.media.Image;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.wifinder.wifinder.R;
import com.wifinder.wifinder.activity.MapActivity;
import com.wifinder.wifinder.data.User;
import com.wifinder.wifinder.listener.GroupCardOnClickListener;
import com.wifinder.wifinder.listener.HomeCardOnClickListener;
import com.wifinder.wifinder.singleton.WorkflowManager;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.GroupViewHolder>{


    private Context context;
    private MapActivity activity;
    private List<User> userList = new LinkedList<>();
    private int lastPosition = -1;
    TypedArray uimgs;


    public GroupAdapter(MapActivity context) {
        this.activity = context;
        this.context = context;
        uimgs = context.getResources().obtainTypedArray(R.array.user_icons);
        //Adding myself to the adapter
        Random rnd = new Random();
        WorkflowManager.getWorkflowManager().getMyself().setImgId(rnd.nextInt(uimgs.length()));
        userList.add(WorkflowManager.getWorkflowManager().getMyself());
    }

    public List<User> getUserList() {
        return userList;
    }

    public boolean removeUser(User u){
        int position = userList.indexOf(u);
        boolean value = this.userList.remove(u);
        notifyDataSetChanged();
        notifyItemRangeChanged(position, userList.size());
        Log.d("REMOVEUSR", "User: " + u.getMacAddr() + " has been removed from the adapter");
        return value;
    }

    public void updateUser(User u){
        //Check if an user should have been removed while being in the background
        if((this.userList.size()-1) != WorkflowManager.getWorkflowManager().getUserList().size()){
            Log.d("REMOVEUSR","UserList size in adapter different than in singleton");
            User toDelete=null;
            for(User user : userList){//If userList has an User that isn't in the singleton remove it
                if(user.equals(WorkflowManager.getWorkflowManager().getMyself()))
                    continue;
                if(!WorkflowManager.getWorkflowManager().getUserList().contains(user))
                    toDelete = user;
            }
            if(toDelete!=null)
                removeUser(toDelete);
        }

        //Regular update, if not present will add new user
        for(User user: userList) {
             if (user.getMacAddr().equals(u.getMacAddr())) {
                 //UPDATE ALL FIELDS
                 user.setName(u.getName());
                 user.setLatitude(u.getLatitude());
                 user.setLongitude(u.getLongitude());
                 user.setIsInside(u.isInside());
                 notifyDataSetChanged();
                 return;
             }
         }
        //Must add new User
        Random rnd = new Random();
        u.setImgId(rnd.nextInt(uimgs.length()));
        this.userList.add(u);
        notifyDataSetChanged();
    }

    @Override
    public GroupViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.card_group, viewGroup, false);
        GroupViewHolder holder = new GroupViewHolder(itemView);
        return holder;
    }

    @Override
    public void onBindViewHolder(GroupViewHolder holder, int i) {
        User user = userList.get(i);
        holder.groupCardUsername.setText(user.getName());
        holder.groupCardMacAddress.setText(user.getMacAddr());
        if (user.getLatitude() != null && user.getLongitude() != null){
            holder.groupCardLatitude.setText(user.getLatitude().toString());
            holder.groupCardLongitude.setText(user.getLongitude().toString());
            holder.groupCardLatitude.setVisibility(View.VISIBLE);
            holder.groupCardLongitude.setVisibility(View.VISIBLE);
            holder.groupCardNoCoords.setVisibility(View.GONE);
        }else{
            holder.groupCardLatitude.setVisibility(View.GONE);
            holder.groupCardLongitude.setVisibility(View.GONE);
            holder.groupCardNoCoords.setVisibility(View.VISIBLE);
        }
        boolean isInside = user.isInside();
        if(isInside){
            holder.groupCardIsInside.setText("Inside");
            holder.groupCardIsInside.setTextColor(ContextCompat.getColor(context, R.color.red_700));
        }
        else {
            holder.groupCardIsInside.setText("Outside");
            holder.groupCardIsInside.setTextColor(ContextCompat.getColor(context, R.color.green_700));
        }
        //setAnimation(holder.container, i);
        int imageID = uimgs.getResourceId(user.getImgId(), 0);
        holder.groupCardUserImage.setImageResource(imageID);

        //Color card if it's group owner
        if(user.getMacAddr().equals(WorkflowManager.getWorkflowManager().getGroupOwner().deviceAddress)) {
            holder.groupCardUsername.setTextColor(ContextCompat.getColor(context, R.color.colorAccent));
        }

        //Color card if it's myself
        if(user.getMacAddr().equals(WorkflowManager.getWorkflowManager().getMyself().getMacAddr())) {
            holder.groupCardUsername.setTextColor(ContextCompat.getColor(context, R.color.blue_A400));
        }

        holder.cardView.setOnClickListener(new GroupCardOnClickListener(activity));
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }


    private void setAnimation(View viewToAnimate, int position)
    {
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



    // VIEW HOLDER

    public static class GroupViewHolder extends RecyclerView.ViewHolder {

        protected CardView cardView;
        protected TextView groupCardUsername;
        protected TextView groupCardIsInside;
        protected TextView groupCardLatitude;
        protected TextView groupCardLongitude;
        protected TextView groupCardNoCoords;
        protected TextView groupCardMacAddress;
        protected ImageView groupCardUserImage;
        protected CardView container;

        public GroupViewHolder(View v) {
            super(v);
            groupCardUsername = (TextView) v.findViewById(R.id.group_card_username);
            groupCardIsInside = (TextView) v.findViewById(R.id.group_card_isInside);
            groupCardLatitude = (TextView) v.findViewById(R.id.group_card_latitude);
            groupCardLongitude = (TextView) v.findViewById(R.id.group_card_longitude);
            groupCardNoCoords = (TextView) v.findViewById(R.id.group_card_no_coords);
            groupCardMacAddress = (TextView) v.findViewById(R.id.group_card_mac_address);
            groupCardUserImage = (ImageView) v.findViewById(R.id.user_img);
            container = (CardView) itemView.findViewById(R.id.group_card_view);
            cardView = (CardView)v;
        }
    }

    // END VIEW HOLDER

}
