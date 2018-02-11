package com.example.acer.goldenhour.historyRecyclerView;

/**
 * Created by rbarv on 10/02/2018.
 */

public class HistoryObject {
    private String rideId;
    private String time;

    public HistoryObject(String rideId, String time) {
        this.rideId = rideId;
        this.time = time;
    }

    public String getRideId() {return rideId;}
    public void setRideId(String rideId) {
        this.rideId = rideId;
    }

    public String getTime() {return time;}
    public void setTime(String time) {
        this.time = time;
    }
}
