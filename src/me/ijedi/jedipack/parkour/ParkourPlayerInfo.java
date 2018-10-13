package me.ijedi.jedipack.parkour;

import me.ijedi.jedipack.JediPackMain;
import org.bukkit.scheduler.BukkitRunnable;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class ParkourPlayerInfo {

    private UUID playerId;
    private String courseId;
    private Date startDate;
    private int currentCheckpoint;

    private boolean hasStartMessageCoolDown = false;
    private boolean hasFinishMessageCoolDown = false;

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


    // Set the cool down for start messages
    public void beginStartMessageCoolDown(){
        hasStartMessageCoolDown = true;
        new BukkitRunnable(){
            @Override
            public void run(){
                hasStartMessageCoolDown = false;
                this.cancel();
            }
        }.runTaskLater(JediPackMain.getThisPlugin(), 3 * 20L); // 3 seconds
    }

    // Returns if the player has a start message cool down
    public boolean hasStartMessageCoolDown(){
        return hasStartMessageCoolDown;
    }

    // Set the cool down for finish messages
    public void beginFinishMessageCoolDown(){
        hasFinishMessageCoolDown = true;
        new BukkitRunnable(){
            @Override
            public void run(){
                hasFinishMessageCoolDown = false;
                this.cancel();
            }
        }.runTaskLater(JediPackMain.getThisPlugin(), 3 * 20L); // 3 seconds
    }

    // Returns if the player has a finish message cool down
    public boolean hasFinishMessageCoolDown(){
        return hasFinishMessageCoolDown;
    }
}
