package me.ijedi.jedipack.parkour;

import me.ijedi.jedipack.common.Util;
import org.bukkit.entity.Player;

import java.util.Date;
import java.util.UUID;

public class ParkourPlayerInfo {

    private UUID playerId;
    private String courseId;
    private Date startDate;
    private Date endDate;
    private int currentCheckpoint;

    public ParkourPlayerInfo(UUID playerId, String courseId){
        this.playerId = playerId;
        this.courseId = courseId;
    }

    public boolean hasStartedThisCourse(String courseId){
        if(courseId.toUpperCase().equals(this.courseId)){
            return startDate != null;
        }
        return false;
    }

    public void setStartDate(Date startDate, String courseId){
        this.startDate = startDate;
        this.courseId = courseId;
    }
}
