package me.ijedi.jedipack.signlock;

import me.ijedi.jedipack.common.MessageTypeEnum;
import me.ijedi.jedipack.common.Util;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.inventory.ItemStack;

public class SignLockChangeEvent implements Listener {

    private final String SIGNLOCK = "[SignLock]";

    @EventHandler
    public void onSignChange(SignChangeEvent event){

        // We only care about wall signs
        Block eventBlock = event.getBlock();
        if(eventBlock.getType().equals(Material.WALL_SIGN)){

            // We don't care if the sign lock "command" wasn't given
            boolean isSignLock = event.getLine(0).toLowerCase().equals(SIGNLOCK.toLowerCase());
            if(!isSignLock){
                return;
            }

            // Only when placed on chests and trapped chests
            Block placedOnBlock = Util.getBlockFromPlacedSign(eventBlock);
            if(placedOnBlock == null){
                return;
            }
            Material placedType = placedOnBlock.getType();
            if(SignLockManager.LOCKABLE_CONTAINERS.contains(placedType)){

                Player player = event.getPlayer();
                SignLockPlayerInfo playerInfo = SignLockManager.getPlayerInfo(player.getUniqueId());
                Location blockLocation = Util.centerSignLockLocation(eventBlock.getLocation());
                Location placedOnBlockLocation = Util.centerSignLockLocation(placedOnBlock.getLocation());

                if(playerInfo.hasLockAtLocation(blockLocation, placedOnBlockLocation)){
                    MessageTypeEnum.SignLockMessage.sendMessage("There is already a sign lock on this location.", player, true);
                    event.setCancelled(true);

                    // Set to air and give the player their sing back.
                    eventBlock.getLocation().getBlock().setType(Material.AIR);
                    if(player.getGameMode().equals(GameMode.SURVIVAL)){
                        player.getInventory().addItem(new ItemStack(Material.SIGN));
                    }

                } else {
                    // Create the lock and update the sign text
                    SignLock newLock = playerInfo.addNewLock(null, 0, blockLocation, true);
                    int lockNum = newLock.getLockNumber();
                    event.setLine(0, ChatColor.GREEN + SIGNLOCK);
                    event.setLine(1, ChatColor.GREEN + "#" + Integer.toString(lockNum));

                    MessageTypeEnum.SignLockMessage.sendMessage("Lock created!", player, false);
                }
            }

        }

    }

}
