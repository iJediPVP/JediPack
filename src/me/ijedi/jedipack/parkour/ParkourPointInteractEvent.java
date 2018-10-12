package me.ijedi.jedipack.parkour;

import me.ijedi.jedipack.common.Util;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

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

                // Starting point
                if(ParkourManager.isStartLocation(location, false, course)){
                    event.getPlayer().sendMessage("Starting location.");

                } else if(ParkourManager.isFinishLocation(location, false, course)){
                    // Finishing location
                    event.getPlayer().sendMessage("Finishing location.");

                } else if(ParkourManager.isCheckpointLocation(location, false, course)){
                    // Checkpoint location
                    event.getPlayer().sendMessage("Checkpoint location.");
                }
            }


        }

    }
}
