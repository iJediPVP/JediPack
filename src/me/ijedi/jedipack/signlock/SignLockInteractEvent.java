package me.ijedi.jedipack.signlock;

import me.ijedi.jedipack.common.MessageTypeEnum;
import me.ijedi.jedipack.common.Util;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class SignLockInteractEvent implements Listener {

    @EventHandler
    public void onSignLockInteract(PlayerInteractEvent event){

        // We only care about right clicks
        if(!event.getAction().equals(Action.RIGHT_CLICK_BLOCK)){
            return;
        }

        // If this block can be locked, check if it is locked and see if this player has permission.
        Block block = event.getClickedBlock();
        if(SignLockManager.LOCKABLE_CONTAINERS.contains(block.getType())){

            Location blockLoc = Util.centerSignLockLocation(block.getLocation());
            if(SignLockManager.isSignLockLocation(blockLoc)){
                SignLock lock = SignLockManager.getLockFromLocation(blockLoc);

                Player player = event.getPlayer();
                MessageTypeEnum.SignLockMessage.logMessage(player.getUniqueId().toString());
                MessageTypeEnum.SignLockMessage.logMessage(lock.getPlayerId().toString());
                if(!lock.hasContainerAccess(player.getUniqueId())){
                    MessageTypeEnum.SignLockMessage.sendMessage("You do not have permission to use this container!", player, true);
                    event.setCancelled(true);
                }

            }

        }

    }
}
