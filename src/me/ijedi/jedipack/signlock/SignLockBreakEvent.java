package me.ijedi.jedipack.signlock;

import me.ijedi.jedipack.common.Util;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class SignLockBreakEvent implements Listener {

    @EventHandler
    public void onSignLockBreak(BlockBreakEvent event){

        Block block = event.getBlock();
        Location blockLoc = Util.centerLocation(block.getLocation());

        if(SignLockManager.isSignLockLocation(blockLoc)){
            event.setCancelled(true);
        }
    }

}
