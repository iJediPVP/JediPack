package me.ijedi.jedipack.home;

import org.bukkit.Location;

public class HomeInfo {

    // Class fields
    private String homeName;
    private Location homeLocation;

    public HomeInfo(String homeName, Location homeLocation){
        this.homeName = homeName;
        this.homeLocation = homeLocation;
    }


    //region GETTERS
    public String getHomeName() {
        return homeName;
    }

    public Location getHomeLocation(){
        return  homeLocation;
    }
    //endregion
}
