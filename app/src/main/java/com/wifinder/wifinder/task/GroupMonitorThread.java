package com.wifinder.wifinder.task;

import android.os.SystemClock;
import android.util.Log;

import com.wifinder.wifinder.listener.WDGroupInfoListener;
import com.wifinder.wifinder.singleton.WorkflowManager;

/**
 * Created by Martina on 04/12/15.
 */

//Used to get timely update on group status and allowing WDGroupAdapter to delete users that disconnected. Used ONLY by group owner!
public class GroupMonitorThread implements Runnable {

    private WDGroupInfoListener listener;

    public GroupMonitorThread(WDGroupInfoListener listener) {
        this.listener = listener;
    }

    @Override
    public void run() {
        while(WorkflowManager.getWorkflowManager().isConnected()){
            SystemClock.sleep(5000);
            groupMonitorTask();
        }
        //We might write here to manage situations when group owner disconnects and relative impact on UI
    }

    private void groupMonitorTask(){
        //Significant code will be executed inside the listener. We have to do this because the group info is called asynchronously
        Log.d("DISCONNECT","GroupMonitorTask started");
        WorkflowManager.getWorkflowManager().getWIFIP2PManager().requestGroupInfo(WorkflowManager.getWorkflowManager().getWIFIP2PChannel(),listener);
    }
}
