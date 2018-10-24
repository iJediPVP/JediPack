package me.ijedi.jedipack.signlock;

import me.ijedi.jedipack.common.MessageTypeEnum;
import me.ijedi.jedipack.common.Util;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.ArrayList;

public class SignLockBreakEvent implements Listener {

    private static ArrayList<Material> MAT_BLACKLIST = new ArrayList<Material>(){{
        add(Material.CHEST);
        add(Material.TRAPPED_CHEST);
        add(Material.WALL_SIGN);
    }};

    @EventHandler
    public void onSignLockBreak(BlockBreakEvent event){

        Block block = event.getBlock();

        if(!MAT_BLACKLIST.contains(block.getType())){
            return;
        }

        Location blockLoc = Util.centerSignLockLocation(block.getLocation());

        // If this location is locked, don't allow it to be broken.
        if(SignLockManager.isSignLockLocation(blockLoc)){
            Player player = event.getPlayer();
            if(event.getBlock().getType().equals(Material.WALL_SIGN)){
                MessageTypeEnum.SignLockMessage.sendMessage("This sign lock cannot be broken!", player, true);
            } else {
                MessageTypeEnum.SignLockMessage.sendMessage("This container is locked by a player!", player, true);
            }
            event.setCancelled(true);
        }
    }

}
