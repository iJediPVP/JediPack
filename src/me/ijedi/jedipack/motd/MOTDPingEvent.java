package me.ijedi.jedipack.motd;

import me.ijedi.jedipack.JediPackMain;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerListPingEvent;

public class MOTDPingEvent implements Listener {

    @EventHandler
    public void onMotdPing(ServerListPingEvent event){

        // Make sure MOTD is enabled
        if(JediPackMain.isMotdEnabled){

            // Set the MOTD
            String motd = MOTDManager.getMotd();
            event.setMotd(motd);
        }
    }
}
