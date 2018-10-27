package me.ijedi.jedipack.parkour;

import me.ijedi.jedipack.common.MessageTypeEnum;
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
            Player player = event.getPlayer();

            /*// See if the player has perms
            if(ParkourManager.getPermsEnabled() && !player.hasPermission(ParkourCommand.PKPERM_PARKOUR)){
                player.sendMessage(ParkourManager.formatParkourString("You do not have permission to use this!", true));
                return;
            }*/

            // Check for a course id
            String courseId = ParkourManager.getCourseIdFromLocation(location);
            if(!Util.isNullOrEmpty(courseId)){
                ParkourCourse course = ParkourManager.getCourse(courseId);


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
                        MessageTypeEnum.ParkourMessage.sendMessage(String.format("Restarting course '%s'!", courseId), player,false);

                    } else {
                        // Else we haven't started this course. So we should start it.
                        Date startDate = new Date();
                        info.setStartDate(startDate, courseId);
                        MessageTypeEnum.ParkourMessage.sendMessage(String.format("Course '%s' started!", courseId), player, false);
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
                        MessageTypeEnum.ParkourMessage.sendMessage(String.format("You haven't started course '%s' yet!", courseId), player,true);

                    } else {

                        // They finished!
                        long courseTime = info.getCourseTime(new Date());
                        boolean isNewRecord = info.checkForCourseRecord();
                        String timeStr = info.formatTime(courseTime);
                        if(isNewRecord){
                            MessageTypeEnum.ParkourMessage.sendMessage(String.format("Congratulations! You have finished course '%s' with a new record time of '%s'!", courseId, timeStr), player,false);

                        } else {
                            long prevRecordTime = info.getRecordTime();
                            if(prevRecordTime == 0){
                                MessageTypeEnum.ParkourMessage.sendMessage(String.format("Congratulations! You have finished course '%s' with a time of %s!", courseId, timeStr), player,false);
                            } else {
                                String prevRecordTimeStr = info.formatTime(prevRecordTime);
                                MessageTypeEnum.ParkourMessage.sendMessage(String.format("Congratulations! You have finished course '%s' with a time of %s! Your personal best is %s!", courseId, timeStr, prevRecordTimeStr), player,false);
                            }
                            info.finishedCourse();
                        }

                        info.beginFinishMessageCoolDown(true);
                        return;
                    }
                    info.beginFinishMessageCoolDown(false);

                } else if(ParkourManager.isCheckpointLocation(location, false, course)){
                    // Checkpoint location

                    // See if the player has started the course yet
                    ParkourPlayerInfo info = ParkourManager.getPlayerInfo(player, courseId);
                    if(!info.hasStartedThisCourse(courseId)){
                        int activatedCheckpoint = course.getCheckpointFromLocation(event.getClickedBlock().getLocation());
                        if(!info.hasCheckpointMessageCoolDown(activatedCheckpoint)){
                            MessageTypeEnum.ParkourMessage.sendMessage("You haven't started this course yet!", player,true);
                            info.beginCheckpointMessageCoolDown(activatedCheckpoint);
                        }

                        return;
                    }

                    // See if the player has already hit this checkpoint
                    int currentCheckpoint = info.getCurrentCheckpoint();
                    int activatedCheckpoint = course.getCheckpointFromLocation(event.getClickedBlock().getLocation());
                    if(!info.hasCheckpointMessageCoolDown(activatedCheckpoint)){
                        if(activatedCheckpoint > currentCheckpoint){
                            MessageTypeEnum.ParkourMessage.sendMessage("Checkpoint reached!", player,false);
                            info.setCurrentCheckpoint(activatedCheckpoint);

                        } else {
                            MessageTypeEnum.ParkourMessage.sendMessage("You've already reached this checkpoint!", player, true);
                        }
                        info.beginCheckpointMessageCoolDown(activatedCheckpoint);
                    }
                    //info.beginCheckpointMessageCoolDown();
                    return;
                }
            }


        }

    }

}
