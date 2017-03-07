package com.wifinder.wifinder.data;

import com.google.android.gms.maps.model.LatLng;
import com.orm.SugarRecord;
import com.wifinder.wifinder.json.UserJSON;

public class User{

    private String name;
    private String macAddr;
    private Double latitude;
    private Double longitude;
    private boolean isInside;

    //Random image related
    private int imgId;

    public User(String name, String macAddr, Double latitude, Double longitude) {
        this.name = name;
        this.macAddr = macAddr;
        this.latitude = latitude;
        this.longitude = longitude;
        this.isInside = true;
    }


    public User(String name, String macAddr, Double latitude, Double longitude,boolean isInside) {
        this.name = name;
        this.macAddr = macAddr;
        this.latitude = latitude;
        this.longitude = longitude;
        this.isInside = isInside;
    }

    public int getImgId() {
        return imgId;
    }

    public void setImgId(int imgId) {
        this.imgId = imgId;
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


    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                ", macAddr='" + macAddr + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", isInside=" + isInside +
                '}';
    }

    public UserJSON toUserJSON(){
        UserJSON userJSON= new UserJSON(this.name,this.macAddr,this.latitude,this.longitude,this.isInside);
        return userJSON;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return !(macAddr != null ? !macAddr.equals(user.macAddr) : user.macAddr != null);

    }

    @Override
    public int hashCode() {
        return macAddr != null ? macAddr.hashCode() : 0;
    }
}
