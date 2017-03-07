package com.wifinder.wifinder.task;

import android.os.SystemClock;

import com.wifinder.wifinder.singleton.WorkflowManager;

/**
 * Created by Martina on 04/12/15.
 */
public class ServiceDiscoverThread implements Runnable {


    @Override
    public void run() {
        while(!WorkflowManager.getWorkflowManager().isConnected()){
            SystemClock.sleep(4000);
            if(!WorkflowManager.getWorkflowManager().isServiceProvider())
             WorkflowManager.getWorkflowManager().discoverServiceUpdate(); //Wont do anything if not on homeActivity
        }
    }
}
