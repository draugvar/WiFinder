package com.wifinder.wifinder.json;

import java.util.LinkedList;

/**
 * Created by stefano_mbpro on 24/11/15.
 */
public class UserJSONList {

    private LinkedList<UserJSON> userList;


    public UserJSONList(LinkedList<UserJSON> userJSONList) {
        this.userList = userJSONList;
    }

    public UserJSONList() {
    }

    public LinkedList<UserJSON> getUserList() {
        return userList;
    }

    public void setUserList(LinkedList<UserJSON> userList) {
        this.userList = userList;
    }


}
