package me.ijedi.jedipack.parkour;

import me.ijedi.jedipack.common.Util;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Date;

public class ParkourPointInteractEvent implements Listener {

    @EventHandler
    public void onPointInteract(PlayerInteractEvent event){

        // Only check physical action types
        if(event.getAction() == Action.PHYSICAL){

            // Get the block location
            Block block = event.getClickedBlock();
            Location location = block.getLocation();

            // Check for a course id
            String courseId = ParkourManager.getCourseIdFromLocation(location);
            if(!Util.IsNullOrEmpty(courseId)){
                ParkourCourse course = ParkourManager.getCourse(courseId);
                Player player = event.getPlayer();

                // Starting point
                if(ParkourManager.isStartLocation(location, false, course)){

                    // Get the player info for this player from the parkour manager
                    ParkourPlayerInfo info = ParkourManager.getPlayerInfo(player, courseId);

                    // See if there is a start cool down
                    if(info.hasStartMessageCoolDown()){
                        return;
                    }

                    // If the player has already started, reset their start date.
                    if(info.hasStartedThisCourse(courseId)){
                        Date startDate = new Date();
                        info.setStartDate(startDate, courseId);
                        String message = ParkourManager.formatParkourString(String.format("Restarting course '%s'!", courseId), false);
                        player.sendMessage(message);

                    } else {
                        // Else we haven't started this course. So we should start it.
                        Date startDate = new Date();
                        info.setStartDate(startDate, courseId);
                        String message = ParkourManager.formatParkourString(String.format("Course '%s' started!", courseId), false);
                        player.sendMessage(message);
                    }
                    info.beginStartMessageCoolDown();


                } else if(ParkourManager.isFinishLocation(location, false, course)){

                    // Get the player info for this player from the parkour manager
                    ParkourPlayerInfo info = ParkourManager.getPlayerInfo(player, courseId);

                    // See if there is a finish cool down
                    if(info.hasFinishMessageCoolDown()){
                        return;
                    }

                    // If the player hasn't started this course, send them a warning.
                    if(!info.hasStartedThisCourse(courseId)){
                        String message = ParkourManager.formatParkourString(String.format("You haven't started course '%s' yet!", courseId), true);
                        player.sendMessage(message);

                    } else {

                        // They finished!
                        long courseTime = info.getCourseTime(new Date());
                        boolean isNewRecord = info.checkForCourseRecord();
                        String timeStr = info.formatTime(courseTime);
                        if(isNewRecord){
                            String message = ParkourManager.formatParkourString(String.format("Congratulations! You have finished course '%s' with a new record time of '%s'!", courseId, timeStr), false);
                            player.sendMessage(message);

                        } else {
                            long prevRecordTime = info.getRecordTime();
                            if(prevRecordTime == 0){
                                String message = ParkourManager.formatParkourString(String.format("Congratulations! You have finished course '%s' with a time of %s!", courseId, timeStr), false);
                                player.sendMessage(message);
                            } else {
                                String prevRecordTimeStr = info.formatTime(prevRecordTime);
                                String message = ParkourManager.formatParkourString(String.format("Congratulations! You have finished course '%s' with a time of %s! Your personal best is %s!", courseId, timeStr, prevRecordTimeStr), false);
                                player.sendMessage(message);
                            }

                        }

                        // TODO: Set course record.
                        //ParkourManager.removePlayerInfo(player.getUniqueId());
                        info.beginFinishMessageCoolDown(true);
                        return;
                    }
                    info.beginFinishMessageCoolDown(false);

                } else if(ParkourManager.isCheckpointLocation(location, false, course)){
                    // Checkpoint location
                    event.getPlayer().sendMessage("Checkpoint location.");

                    // See if the player has started the course yet
                    ParkourPlayerInfo info = ParkourManager.getPlayerInfo(player, courseId);
                    if(!info.hasStartedThisCourse(courseId)){
                        String message = ParkourManager.formatParkourString("You haven't started this course yet!", false);
                        player.sendMessage(message);
                        return;
                    }

                    // See if the player has already hit this checkpoint
                    int currentCheckpoint = info.getCurrentCheckpoint();
                    int activatedCheckpoint = course.getCheckpointFromLocation(event.getClickedBlock().getLocation());
                    if(activatedCheckpoint > currentCheckpoint){
                        player.sendMessage(ParkourManager.formatParkourString("Checkpoint reached!", false));
                        info.setCurrentCheckpoint(activatedCheckpoint);

                    } else {
                        player.sendMessage(ParkourManager.formatParkourString("You've already reached this checkpoint!", true));
                    }
                    return;
                }
            }


        }

    }
}