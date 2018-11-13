package me.ijedi.jedipack.common;

import me.ijedi.jedipack.JediPackMain;
import me.ijedi.jedipack.parkour.ParkourManager;
import me.ijedi.jedipack.signlock.SignLockEvents;
import me.ijedi.jedipack.signlock.SignLockManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

import java.util.ArrayList;
import java.util.List;

public class CommonEvents implements Listener {

    // Prevent piston extension from breaking things.
    @EventHandler
    public void blockPistonExtendEvent(BlockPistonExtendEvent event){

        // Cancel the event if any of the blocks are a locked location
        event.setCancelled(shouldCancelPistonEvent(event.getBlocks()));
    }

    // Prevent piston retraction from breaking things
    @EventHandler
    public void blockPistonRetractEvent(BlockPistonRetractEvent event){

        // Cancel the event if any of the blocks are a locked location
        event.setCancelled(shouldCancelPistonEvent(event.getBlocks()));
    }

    @EventHandler
    public void entityExplodeEvent(EntityExplodeEvent event){
        // Loop through the blocks being destroyed
        List<Block> explodingBlocks = event.blockList();
        ArrayList<Block> blocksToRemove = new ArrayList<>();
        for(Block block : explodingBlocks){

            if(!isSignLockMaterial(block.getType())){
                Location blockLoc = block.getLocation();
                if(shouldCancelOnBlockLocation(blockLoc)){
                    blocksToRemove.add(block);
                }
            }
        }

        // Remove the blocks we want to save
        event.blockList().removeAll(blocksToRemove);
    }

    private boolean shouldCancelPistonEvent(List<Block> blocks){
        for(Block block : blocks){

            Location blockLocation = block.getLocation();
            if(shouldCancelOnBlockLocation(blockLocation)){
                return true;
            }
        }
        return false;
    }

    private boolean shouldCancelOnBlockLocation(Location blockLoc){
        // Parkour, signlocks
        if((JediPackMain.isParkourEnabled && ParkourManager.isParkourBlockLocation(blockLoc))
                || (JediPackMain.isSignLocksEnabled && SignLockManager.isSignLockLocation(Util.getCenteredBlockLocation(blockLoc)))){
            return true;
        }
        return false;
    }

    public static boolean isSignLockMaterial(Material material){
        return material.equals(Material.WALL_SIGN) || SignLockEvents.LOCKABLE_CONTAINERS.contains(material) || SignLockEvents.LOCKABLE_DOORS.contains(material);
    }

}
