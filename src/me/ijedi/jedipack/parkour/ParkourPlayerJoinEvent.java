package me.ijedi.jedipack.parkour;

import me.ijedi.jedipack.JediPackMain;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class ParkourPlayerJoinEvent implements Listener {

    @EventHandler
    public void pJoin(PlayerJoinEvent event){
        //Make sure the player's info is there
        PlayerInfo pInfo = new PlayerInfo(event.getPlayer(), JediPackMain.getThisPlugin());
        if(!pInfo.fileExists()){
            pInfo.setDefaults();
        }
    }
}
