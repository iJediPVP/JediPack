package me.ijedi.jedipack.initialize;

import me.ijedi.jedipack.test.PingCommand;
import me.ijedi.jedipack.test.PingEnableCommand;
import me.ijedi.jedipack.test.TestMenuCommand;
import me.ijedi.jedipack.test.TestStandCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class CommandInitializer {

    // Register commands for this plugin
    public static void RegisterCommands(JavaPlugin plugin){
        FileConfiguration config = plugin.getConfig();

        if(config.getBoolean("pingCommandEnabled")){
            plugin.getCommand("ping").setExecutor(new PingCommand());
        }

        plugin.getCommand("pingEnable").setExecutor(new PingEnableCommand());

        plugin.getCommand("testMenu").setExecutor(new TestMenuCommand());
        plugin.getCommand("testStand").setExecutor(new TestStandCommand());

    }

}
