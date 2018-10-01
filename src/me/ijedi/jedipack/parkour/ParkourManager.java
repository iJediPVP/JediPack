package me.ijedi.jedipack.parkour;

import me.ijedi.jedipack.JediPackMain;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class ParkourManager {

    public static void initializeParkour(){
        ParkourConfiguration parkourConfiguration = JediPackMain.getParkourConfiguration();
        JavaPlugin plugin = JediPackMain.getThisPlugin();

        // See if parkour is enabled
        if(parkourConfiguration.isEnabled()){
            plugin.getLogger().info("Parkour is enabled!");
            PluginManager pluginManager = plugin.getServer().getPluginManager();

            // Register events
            pluginManager.registerEvents(new ParkourPlayerJoinEvent(), plugin);

            return;
        }

        // Else parkour is disabled.
        plugin.getLogger().info("Parkour is disabled!");
    }

}
