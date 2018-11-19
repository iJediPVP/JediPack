package me.ijedi.jedipack.back;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class BackEvents implements Listener {


    @EventHandler
    public void onTeleport(PlayerTeleportEvent event){
        BackManager.setBackLocation(event.getPlayer().getUniqueId(), event.getFrom());
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event){
        BackManager.setBackLocation(event.getEntity().getUniqueId(), event.getEntity().getLocation());
    }

}
