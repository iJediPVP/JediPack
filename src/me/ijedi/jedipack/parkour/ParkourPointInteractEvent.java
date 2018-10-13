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



                } else if(ParkourManager.isFinishLocation(location, false, course)){
                    // Finishing location
                    event.getPlayer().sendMessage("Finishing location.");

                    // Get the player info for this player from the parkour manager
                    ParkourPlayerInfo info = ParkourManager.getPlayerInfo(player, courseId);

                    // If the player hasn't started this course, send them a warning.
                    if(!info.hasStartedThisCourse(courseId)){
                        String message = ParkourManager.formatParkourString(String.format("You haven't started course '%s' yet!", courseId), true);
                        player.sendMessage(message);

                    } else {

                        // They finished!
                        String message = ParkourManager.formatParkourString(String.format("Congratulations! You have finished course '%s'!", courseId), false);
                        player.sendMessage(message);
                        // TODO: Give course time and set course record.
                        ParkourManager.removePlayerInfo(player.getUniqueId());
                    }

                } else if(ParkourManager.isCheckpointLocation(location, false, course)){
                    // Checkpoint location
                    event.getPlayer().sendMessage("Checkpoint location.");
                }
            }


        }

    }
}
