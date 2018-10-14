package me.ijedi.jedipack.parkour;

import me.ijedi.jedipack.JediPackMain;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class ParkourPlayerInfo {

    private final String RECORDTIME = "recordTime";

    private UUID playerId;
    private String courseId;
    private Date startDate;
    private long courseTime;
    private int currentCheckpoint;
    private long recordTime;

    private boolean hasStartMessageCoolDown = false;
    private boolean hasFinishMessageCoolDown = false;

    public ParkourPlayerInfo(UUID playerId, String courseId){
        this.playerId = playerId;
        this.courseId = courseId;
    }

    // Returns if the player has started the given course.
    public boolean hasStartedThisCourse(String courseId){
        if(courseId.toUpperCase().equals(this.courseId)){
            return startDate != null;
        }
        return false;
    }

    // Returns if the player has started ANY course.
    public boolean hasStartedAnyCourse(){
        return startDate != null;
    }

    public String getCourseId(){
        return courseId;
    }

    // Set the start date for the given course.
    public void setStartDate(Date startDate, String courseId){
        this.startDate = startDate;
        this.courseId = courseId;
    }

    // Returns the players time for the current course.
    public long getCourseTime(Date finishDate){
        this.courseTime = (finishDate.getTime() - startDate.getTime());
        return courseTime;
    }

    public long getRecordTime(){
        return recordTime;
    }

    // Format a long into a string
    public String formatTime(long time){
        SimpleDateFormat format = new SimpleDateFormat("mm:ss:SSS");
        return format.format(time);
    }

    // Returns if the player has beaten their previous record.
    public boolean checkForCourseRecord(){
        String folder = JediPackMain.getThisPlugin().getDataFolder() + "/parkour/playerData";
        String fileName = folder + "/" + playerId.toString() + ".yml";
        File file = new File(fileName);
        String recordTimePath = courseId + "." + RECORDTIME;
        JediPackMain.getThisPlugin().getLogger().info(recordTimePath);

        // If the player file doesn't exist, create it and save the current time.
        if(!file.exists()){
            file.getParentFile().mkdirs();
            FileConfiguration config = YamlConfiguration.loadConfiguration(file);
            config.set(recordTimePath, courseTime);
            try{
                config.save(file);
            } catch (IOException e){
                JediPackMain.getThisPlugin().getLogger().info("Error saving parkour player file for player id " + playerId.toString());
            }
            return true;

        } else {
            // File exists, check for existing time.
            FileConfiguration config = YamlConfiguration.loadConfiguration(file);
            long storedRecord = config.getLong(recordTimePath);
            if(storedRecord > courseTime || storedRecord == 0) {
                JediPackMain.getThisPlugin().getLogger().info("stored > coursetime");
                config.set(recordTimePath, courseTime);
                try {
                    config.save(file);
                } catch (IOException e) {
                    JediPackMain.getThisPlugin().getLogger().info("Error saving parkour player file for player id " + playerId.toString());
                }
                return true;

            } else {
                JediPackMain.getThisPlugin().getLogger().info("stored <= coursetime");
                recordTime = storedRecord;
                return false;
            }
        }
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
    public void beginFinishMessageCoolDown(boolean removePlayerInfo){
        hasFinishMessageCoolDown = true;
        new BukkitRunnable(){
            @Override
            public void run(){
                hasFinishMessageCoolDown = false;
                if(removePlayerInfo){
                    ParkourManager.removePlayerInfo(playerId);
                }
                this.cancel();
            }
        }.runTaskLater(JediPackMain.getThisPlugin(), 3 * 19L); // 3 seconds
    }

    // Returns if the player has a finish message cool down
    public boolean hasFinishMessageCoolDown(){
        return hasFinishMessageCoolDown;
    }



    public int getCurrentCheckpoint(){
        return currentCheckpoint;
    }

    public void setCurrentCheckpoint(int checkpoint){
        this.currentCheckpoint = checkpoint;
    }
}
