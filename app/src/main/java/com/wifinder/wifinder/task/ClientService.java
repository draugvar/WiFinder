package com.wifinder.wifinder.task;

import android.app.IntentService;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

import com.google.gson.Gson;
import com.wifinder.wifinder.data.User;
import com.wifinder.wifinder.json.UserJSON;
import com.wifinder.wifinder.json.UserJSONList;
import com.wifinder.wifinder.singleton.WorkflowManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.LinkedList;

/**
 * Created by Joede on 29/11/2015.
 */
public class ClientService extends IntentService{

    private Gson gson;
    private String serverAddress;

    public ClientService(){
        super("ClientWorkerThread");
        this.gson = new Gson();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        this.serverAddress =  intent.getStringExtra("ADDRESS");
        while(WorkflowManager.getWorkflowManager().isConnected()){
            SystemClock.sleep(1500);
            clientTask();
        }
    }


    private void clientTask(){
        Log.v("COMMUNICATION", "Client method started");

        Socket socket = new Socket();
        User myself = WorkflowManager.getWorkflowManager().getMyself();
        String messageInTheBottle = gson.toJson(myself.toUserJSON());
        Log.v("COMMUNICATION", "Message content: " + messageInTheBottle);
        int port = 8888;
        String groupOwnerAddress = serverAddress;
        String response;

        try{
            socket.bind(null);
            socket.connect((new InetSocketAddress(groupOwnerAddress, port)), 500);

            //Init channels
            Log.v("COMMUNICATION", "Trying to init BufferedReader and PrintWriter");
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            //Writing User Data
            out.println(messageInTheBottle);
            //Reading User list from Server
            while ((response = in.readLine()) != null){
                Log.v("COMMUNICATION", "Receiving response: " + response);
                UserJSONList userJSONList = gson.fromJson(response,UserJSONList.class);
                for(UserJSON userJSON: userJSONList.getUserList()){
                    //Don't add if it's myself!
                    if(!userJSON.getMacAddr().equals(myself.getMacAddr())){
                        //Updating user in the singleton. If user doesn't exist create it.
                        if(!WorkflowManager.getWorkflowManager().updateUser(userJSON))
                            WorkflowManager.getWorkflowManager().addUser(userJSON);
                        WorkflowManager.getWorkflowManager().updateGroupAdapter(userJSON.toUser());
                    }
                }

                //Removing users that are in userList but not in the UserJSONList
                if( (WorkflowManager.getWorkflowManager().getUserList().size()+1)!= userJSONList.getUserList().size() ) {
                    LinkedList<User> toRemove = new LinkedList<>();
                    boolean found;
                    for (User user : WorkflowManager.getWorkflowManager().getUserList()) {
                        found = false;
                        for (UserJSON userJSON : userJSONList.getUserList()) {
                            if (user.getMacAddr().equals(userJSON.getMacAddr())) {
                                found = true;
                            }
                        }
                        if (!found) {
                            //No UserJSON matching user, must be deleted
                            toRemove.add(user);
                        }
                    }
                    for (User user : toRemove) {
                        Log.d("DISCONNECT", "Removing from user list: " + user.getName());
                        WorkflowManager.getWorkflowManager().removeUser(user);
                    }
                }

                //Update map UI
                WorkflowManager.getWorkflowManager().updateMapUI();
            }
        }
        catch (IOException e){
            Log.d("COMMUNICATION", "IOException in client");
            e.printStackTrace();
        }
        finally {
            if (socket.isConnected()) {
                try {
                    socket.close();
                    Log.v("COMMUNICATION","SOCKET CLOSED BY CLIENT");
                } catch (IOException e) {
                    Log.d("COMMUNICATION", "Can't close client socket");
                }
            }
        }
    }


}
