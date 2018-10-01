package me.ijedi.jedipack.initialize;

import me.ijedi.jedipack.test.PingCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class CommandInitializer {

    // Register commands for this plugin
    public static void RegisterCommands(JavaPlugin plugin){
        plugin.getCommand("ping").setExecutor(new PingCommand());
    }

}
