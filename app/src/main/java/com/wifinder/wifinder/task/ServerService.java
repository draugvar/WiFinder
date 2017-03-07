package com.wifinder.wifinder.task;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.wifinder.wifinder.listener.WDGroupInfoListener;
import com.wifinder.wifinder.singleton.WorkflowManager;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerService extends IntentService {

    private WDGroupInfoListener listener = new WDGroupInfoListener();

    public ServerService(){
        super("ServerWorkerThread");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d("COMMUNICATION", "Server task started");
        new Thread(new GroupMonitorThread(listener)).start();
        Log.d("DISCONNECT","GroupMonitor thread started");
        try{
            ServerSocket serverSocket = new ServerSocket(8888);
            while(WorkflowManager.getWorkflowManager().isConnected()) {
                Socket client = serverSocket.accept();
                Log.d("COMMUNICATION", "Starting new server thread");
                new Thread(new ServerRunnableThread(client)).start();
                WorkflowManager.getWorkflowManager().updateMapUI();
            }
            serverSocket.close();

        }
        catch(IOException e){
            Log.d("COMMUNICATION","IOException in server");
            e.printStackTrace();
        }
    }
}
