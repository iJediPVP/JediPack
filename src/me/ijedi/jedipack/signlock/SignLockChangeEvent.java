package me.ijedi.jedipack.signlock;

import me.ijedi.jedipack.common.MessageTypeEnum;
import me.ijedi.jedipack.common.Util;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;

public class SignLockChangeEvent implements Listener {

    @EventHandler
    public void onSignChange(SignChangeEvent event){

        // We only care about wall signs
        Block signBlock = event.getBlock();
        if(signBlock.getType().equals(Material.WALL_SIGN)){
            Block placedOnBlock = Util.getBlockFromPlacedSign(signBlock);

            if(placedOnBlock == null){
                return;
            }

            // Only for chests and trapped chests
            Material placedType = placedOnBlock.getType();
            if(placedType.equals(Material.CHEST) || placedType.equals(Material.TRAPPED_CHEST)){

                Player player = event.getPlayer();
                SignLockPlayerInfo playerInfo = SignLockManager.getPlayerInfo(player.getUniqueId());
                Location blockLocation = signBlock.getLocation();
                blockLocation = Util.centerSignLockLocation(blockLocation);

                if(playerInfo.hasLockAtLocation(blockLocation)){
                    MessageTypeEnum.SignLockMessage.sendMessage("There is already a sign lock on this location.", player, true);
                    event.setCancelled(true);

                } else {
                    playerInfo.addNewLock(null, blockLocation, true);
                    MessageTypeEnum.SignLockMessage.sendMessage("Lock created!", player, false);
                }
            }
        }

    }

}
