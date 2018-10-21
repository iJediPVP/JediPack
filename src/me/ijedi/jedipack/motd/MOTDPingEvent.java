package me.ijedi.jedipack.motd;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerListPingEvent;

public class MOTDPingEvent implements Listener {

    @EventHandler
    public void onMotdPing(ServerListPingEvent event){

        // Make sure MOTD is enabled
        if(MOTDManager.isEnabled){

            // Set the MOTD
            String motd = MOTDManager.getMotd();
            event.setMotd(motd);
        }
    }
}
