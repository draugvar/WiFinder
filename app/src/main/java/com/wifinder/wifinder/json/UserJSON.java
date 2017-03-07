package com.wifinder.wifinder.json;

import com.wifinder.wifinder.data.User;

/**
 * Created by stefano_mbpro on 24/11/15.
 */
public class UserJSON {

    private String name;
    private String macAddr;
    private Double latitude;
    private Double longitude;
    private boolean isInside;

    public UserJSON() {
    }

    public UserJSON(String name, String macAddr, Double latitude, Double longitude, boolean isInside) {
        this.name = name;
        this.macAddr = macAddr;
        this.latitude = latitude;
        this.longitude = longitude;
        this.isInside = isInside;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMacAddr() {
        return macAddr;
    }

    public void setMacAddr(String macAddr) {
        this.macAddr = macAddr;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public boolean isInside() {
        return isInside;
    }

    public void setIsInside(boolean isInside) {
        this.isInside = isInside;
    }

    public User toUser(){
        return new User(this.name,this.macAddr,this.latitude,this.longitude,this.isInside);
    }
}
