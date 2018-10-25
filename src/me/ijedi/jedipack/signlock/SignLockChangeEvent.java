package me.ijedi.jedipack.signlock;

import me.ijedi.jedipack.common.MessageTypeEnum;
import me.ijedi.jedipack.common.Util;
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

    @EventHandler
    public void onSignChange(SignChangeEvent event){

        // We only care about wall signs
        Block eventBlock = event.getBlock();
        if(eventBlock.getType().equals(Material.WALL_SIGN)){

            Block placedOnBlock = Util.getBlockFromPlacedSign(eventBlock);
            if(placedOnBlock == null){
                return;
            }

            // Only for chests and trapped chests
            Material placedType = placedOnBlock.getType();
            if(placedType.equals(Material.CHEST) || placedType.equals(Material.TRAPPED_CHEST)){

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
                    // TODO: Parse command text
                    playerInfo.addNewLock(null, blockLocation, true);
                    MessageTypeEnum.SignLockMessage.sendMessage("Lock created!", player, false);
                }
            }

        }

    }

}
