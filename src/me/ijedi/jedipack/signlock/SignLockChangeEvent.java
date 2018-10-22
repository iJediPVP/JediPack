package me.ijedi.jedipack.signlock;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.material.Sign;

public class SignLockChangeEvent implements Listener {

    @EventHandler
    public void onSignChange(SignChangeEvent event){

        // We only care about wall signs
        Block signBlock = event.getBlock();
        if(signBlock.getType().equals(Material.WALL_SIGN)){
            Block placedOnBlock = getBlockFromSign(signBlock);

            if(placedOnBlock == null){
                return;
            }

            // Only for chests and trapped chests
            Material placedType = placedOnBlock.getType();
            if(placedType.equals(Material.CHEST) || placedType.equals(Material.TRAPPED_CHEST)){

            }
        }

    }

    private Block getBlockFromSign(Block signBlock){
        Sign sign = (Sign)signBlock.getState().getData();
        Block block = signBlock.getRelative(sign.getAttachedFace());
        return block;
    }
}
