package me.ijedi.jedipack.back;

import me.ijedi.jedipack.JediPackMain;
import me.ijedi.jedipack.common.MessageTypeEnum;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.UUID;

public class BackManager {

    private static HashMap<UUID, Location> backLocations = new HashMap<>();

    public static void intialize(){

        if(!JediPackMain.isBackEnabled){
            MessageTypeEnum.GeneralMessage.logMessage("Back command is not enabled!");
            return;
        }
        MessageTypeEnum.GeneralMessage.logMessage("Back command is not enabled!");

        // Register events & commands
        JavaPlugin plugin = JediPackMain.getThisPlugin();
        plugin.getServer().getPluginManager().registerEvents(new BackEvents(), plugin);
        plugin.getCommand(BackCommand.BASE_COMMAND).setExecutor(new BackCommand());
        plugin.getCommand(BackCommand.BASE_COMMAND).setTabCompleter(new BackCommand());
    }

    public static boolean hasBackLocation(UUID playerId){
        return backLocations.containsKey(playerId);
    }

    public static Location getBackLocation(UUID playerId){
        return backLocations.get(playerId);
    }

    public static void setBackLocation(UUID playerId, Location location){
        backLocations.put(playerId, location);
    }

}
