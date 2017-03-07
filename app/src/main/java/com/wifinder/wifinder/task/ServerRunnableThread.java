package com.wifinder.wifinder.task;

import android.util.Log;

import com.google.gson.Gson;
import com.wifinder.wifinder.json.UserJSON;
import com.wifinder.wifinder.json.UserJSONList;
import com.wifinder.wifinder.singleton.WorkflowManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Created by stefano_mbpro on 24/11/15.
 */
public class ServerRunnableThread implements Runnable{

    private Socket client;
    private Gson gson = new Gson();

    public ServerRunnableThread(Socket client) {
        this.client = client;
    }

    @Override
    public void run() {
        try {
            //Init channels
            Log.v("COMMUNICATION", "Trying to init BufferedReader and PrintWriter");
            BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            PrintWriter out = new PrintWriter(client.getOutputStream(), true);
            Log.v("COMMUNICATION", "Done initialization of channels");

            //Incoming message
            String messageReceived = in.readLine();
            Log.d("COMMUNICATION", "message may cause crash, message received is: " + messageReceived);
            UserJSON receivedUser = gson.fromJson(messageReceived,UserJSON.class); // ALERT!!!!! Crashes when 3rd device is connected
            //Updating user in the singleton. If user doesn't exist create it.
            if(!WorkflowManager.getWorkflowManager().updateUser(receivedUser))
                WorkflowManager.getWorkflowManager().addUser(receivedUser);

            //Updating UI
            WorkflowManager.getWorkflowManager().updateGroupAdapter(receivedUser.toUser());

            Log.v("COMMUNICATION", "From client: " + messageReceived);

            //Response
            UserJSONList list = WorkflowManager.getWorkflowManager().getUpdatedUserList(); //Also returns server as first user
            out.println(gson.toJson(list));
            Log.v("COMMUNICATION", "Sent response from server");

            //Closing
            Log.v("COMMUNICATION", "Closing the socket");
            client.close();
            Log.v("COMMUNICATION", "SOCKET CLOSED");
        } catch (IOException e) {
            Log.d("COMMUNICATION", "IOException in server");
            e.printStackTrace();
        }
    }
}
