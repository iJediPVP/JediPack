package me.ijedi.jedipack.back;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class BackEvents implements Listener {

    // Set the player back's location upon teleportation
    @EventHandler
    public void onTeleport(PlayerTeleportEvent event){
        PlayerTeleportEvent.TeleportCause cause = event.getCause();
        if(cause.equals(PlayerTeleportEvent.TeleportCause.COMMAND) || cause.equals(PlayerTeleportEvent.TeleportCause.PLUGIN)){
            BackManager.setBackLocation(event.getPlayer().getUniqueId(), event.getFrom());
        }
    }

    // Set the player back's location upon death
    @EventHandler
    public void onDeath(PlayerDeathEvent event){
        BackManager.setBackLocation(event.getEntity().getUniqueId(), event.getEntity().getLocation());
    }

    // Remove the back location when the player quits
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event){
        Player player = event.getPlayer();
        if(BackManager.hasBackLocation(player.getUniqueId())){
            BackManager.removeBackLocation(player.getUniqueId());
        }
    }

}
