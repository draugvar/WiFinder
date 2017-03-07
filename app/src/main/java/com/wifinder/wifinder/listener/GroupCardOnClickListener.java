package com.wifinder.wifinder.listener;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.wifinder.wifinder.R;
import com.wifinder.wifinder.activity.MapActivity;
import com.wifinder.wifinder.data.User;
import com.wifinder.wifinder.singleton.WorkflowManager;

import java.util.Map;

/**
 * Created by yogaub on 04/12/15.
 */
public class GroupCardOnClickListener implements View.OnClickListener {

    MapActivity mapActivity;

    public GroupCardOnClickListener (MapActivity activity){
        mapActivity = activity;
    }

    @Override
    public void onClick(View v) {
        String address = ((TextView)v.findViewById(R.id.group_card_mac_address)).getText().toString();
        Log.d("GROUPCARD", "group card clicked with address: " + address);
        if (address.equals(WorkflowManager.getWorkflowManager().getMyself().getMacAddr())){
            mapActivity.centerCamOnUser(WorkflowManager.getWorkflowManager().getMyself());
            return;
        }

        for (User u : WorkflowManager.getWorkflowManager().getUserList()){
            if (u.getMacAddr().equals(address))
                Log.d("GROUPCARD", "user found calling mapcativity centercam");
                mapActivity.centerCamOnUser(u);
                return;
        }

    }




}
