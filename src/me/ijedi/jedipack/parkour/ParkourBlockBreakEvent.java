package me.ijedi.jedipack.parkour;

import me.ijedi.jedipack.common.MessageTypeEnum;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class ParkourBlockBreakEvent implements Listener {

    @EventHandler
    public void onParkourBlockBreak(BlockBreakEvent event){

        // See if the block location matches one of our parkour points
        Location blockLocation = event.getBlock().getLocation();
        if(ParkourManager.isParkourBlockLocation(blockLocation)){
            Player player = event.getPlayer();
            if(player != null){
                MessageTypeEnum.ParkourMessage.sendMessage("You cannot break this. It's part of a parkour course!", player, true);
            }
            event.setCancelled(true);
        }
    }
}
