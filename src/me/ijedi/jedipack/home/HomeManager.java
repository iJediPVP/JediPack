package me.ijedi.jedipack.home;

import me.ijedi.jedipack.JediPackMain;
import me.ijedi.jedipack.common.MessageTypeEnum;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.UUID;

public class HomeManager {

    public static final String DIRECTORY = "home";

    private static HashMap<UUID, HomePlayerInfo> playerInfos = new HashMap<>();

    // Initialize home things
    public static void initialize(){

        // Check if homes is enabled
        if(!JediPackMain.isHomesEnabled){
            MessageTypeEnum.HomeMessage.logMessage("Homes disabled!");
            return;
        }
        MessageTypeEnum.HomeMessage.logMessage("Homes enabled!");

        // Set up commands
        JavaPlugin plugin = JediPackMain.getThisPlugin();
        plugin.getCommand(HomeCommand.BASE_COMMAND).setExecutor(new HomeCommand());
    }

    // Returns the HomePlayerInfo for the given player id.
    public static HomePlayerInfo getPlayerInfo(UUID playerId){

        // Return existing
        if(playerInfos.containsKey(playerId)){
            return playerInfos.get(playerId);
        }

        // Create new and load the player's file
        HomePlayerInfo info = new HomePlayerInfo(playerId);
        info.loadPlayerInfo();
        playerInfos.put(playerId, info);
        return info;
    }

}
