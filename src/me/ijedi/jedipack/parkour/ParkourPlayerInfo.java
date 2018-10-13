package me.ijedi.jedipack.parkour;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class ParkourPlayerInfo {

    private UUID playerId;
    private String courseId;
    private Date startDate;
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

    public long getCourseTime(Date finishDate){
        long courseTime = (finishDate.getTime() - startDate.getTime());
        return courseTime;
    }

    public String formatTime(long time){
        SimpleDateFormat format = new SimpleDateFormat("mm:ss:SSS");
        return format.format(time);
    }
}
